package speakman.whatsshakingnz.activities;

import java.util.ArrayList;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.earthquake.Earthquake;
import speakman.whatsshakingnz.earthquake.EarthquakeFilter;
import speakman.whatsshakingnz.fragments.ListFragment;
import speakman.whatsshakingnz.fragments.MapFragment;
import speakman.whatsshakingnz.geonet.GeonetAccessor;
import speakman.whatsshakingnz.geonet.GeonetService;
import speakman.whatsshakingnz.preferences.DefaultPrefs;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class MainActivity extends SherlockFragmentActivity implements
		OnSharedPreferenceChangeListener, ActionBar.TabListener {

	private enum TabTitles {
		List, Map
	}

	private AsyncTask mDownloadTask;
	
	private MapFragment mMapFragment;
	private ListFragment mListFragment;

	private boolean mPreferencesUpdated;

	private int mMinDisplay, mMaxNumberOfQuakes;

	/**
	 * True if the system is currently downloading quakes in the background.
	 */
	private boolean mDownloading = false;

	/**
	 * Holds the Refresh menu item, used to set visibility when processing.
	 */
	private MenuItem mRefreshMenuItem;

	/**
	 * A list of the latest {@link Earthquake}s downloaded from Geonet.
	 */
	private ArrayList<Earthquake> mQuakes;

	/**
	 * The currently selected tab.
	 */
	private TabTitles mSelectedTab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Set defaults before we do anything else.
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// Request Feature must be called before adding content.
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_view);

		// First start-up
		if (null == savedInstanceState) {
			// Create fragments
			mListFragment = new ListFragment();
			mMapFragment = new MapFragment();
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.add(R.id.mainView, mListFragment, TabTitles.List.toString());
			ft.add(R.id.mainView, mMapFragment, TabTitles.Map.toString());
			ft.commit();
			mSelectedTab = TabTitles.List;
		}
		// App was killed by the OS
		else {
			mListFragment = (ListFragment) getSupportFragmentManager()
					.findFragmentByTag(TabTitles.List.toString());
			mMapFragment = (MapFragment) getSupportFragmentManager()
					.findFragmentByTag(TabTitles.Map.toString());
		}

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		addTab(TabTitles.List);
		addTab(TabTitles.Map);

		/**
		 * Set this to true, so that quakes are downloaded and preference items
		 * are updated in the onResume call.
		 */
		mPreferencesUpdated = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
		if (mPreferencesUpdated) {
			mPreferencesUpdated = false;
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			mMaxNumberOfQuakes = prefs.getInt(
					PreferenceActivity.KEY_PREF_NUM_QUAKES_TO_SHOW, 10);
			mMinDisplay = prefs.getInt(PreferenceActivity.KEY_PREF_MIN_DISPLAY_MAGNITUDE, 
					DefaultPrefs.MIN_DISPLAY_MAGNITUDE);
			downloadQuakes();
		}
	}
	
	@Override
	protected void onDestroy() {
		if(null != mDownloadTask) {
			Log.d("WSNZ", "Killing background download task as onDestroy() was called before it returned.");
			mDownloadTask.cancel(true);
		}
		super.onDestroy();
	}

	/**
	 * Will add a new tab to the action bar, with the supplied TabTitles member
	 * as the title of the tab.
	 * 
	 * @param title
	 *            The title of the tab.
	 */
	private void addTab(TabTitles title) {
		ActionBar.Tab tab = getSupportActionBar().newTab();
		tab.setText(title.toString());
		tab.setTag(title);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		mRefreshMenuItem = menu.findItem(R.id.menu_refresh);
		updateRefreshButtonVisibility();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			startActivity(new Intent(this, AboutActivity.class));
			break;
		case R.id.menu_preferences:
			startActivity(new Intent(this, PreferenceActivity.class));
			break;
		case R.id.menu_refresh:
			downloadQuakes();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(PreferenceActivity.KEY_PREF_MIN_DISPLAY_MAGNITUDE)
				|| key.equals(PreferenceActivity.KEY_PREF_MIN_HIGHLIGHT_MAGNITUDE)
				|| key.equals(PreferenceActivity.KEY_PREF_NUM_QUAKES_TO_SHOW)) {
			mPreferencesUpdated = true;
		}
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		switch ((TabTitles) tab.getTag()) {
		case List:
			ft.detach(mMapFragment);
			ft.attach(mListFragment);
			mSelectedTab = TabTitles.List;
			break;
		case Map:
			ft.detach(mListFragment);
			ft.attach(mMapFragment);
			mSelectedTab = TabTitles.Map;
			break;
		}
		updateQuakesDisplay();
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	/**
	 * Called to update the quakes display. Will update whichever view is
	 * selected at the moment. If you have new quakes to display, be sure to set
	 * {@link #mQuakes} first, then call this method.
	 */
	private void updateQuakesDisplay() {
		switch (mSelectedTab) {
		case List:
			updateQuakesList();
			break;
		case Map:
			updateQuakesMap();
			break;
		}
	}

	/**
	 * Updates the map view. Please use {@link #updateQuakesDisplay} instead.
	 */
	private void updateQuakesMap() {
		if (null == mQuakes || null == mMapFragment)
			return;
		mMapFragment.updateQuakes(EarthquakeFilter.filterQuakes(mQuakes, ((float)mMinDisplay) / 10.0f, mMaxNumberOfQuakes));
	}

	/**
	 * Updates the list view. Please use {@link #updateQuakesDisplay} instead.
	 */
	private void updateQuakesList() {
		if (null == mQuakes || null == mListFragment)
			return;
		mListFragment.updateQuakes(EarthquakeFilter.filterQuakes(mQuakes, ((float)mMinDisplay) / 10.0f, mMaxNumberOfQuakes));
	}

	private void downloadQuakes() {
		if(mDownloading)
			return;
		mDownloadTask = new DownloadQuakesTask().execute(mMaxNumberOfQuakes, mMinDisplay);
	}

	/**
	 * Updates the visibility of the Refresh button - hides it if
	 * {@link #mDownloading} is true, shows it if false.
	 */
	protected void updateRefreshButtonVisibility() {
		if (null != mRefreshMenuItem)
			mRefreshMenuItem.setVisible(!mDownloading);
	}

	/**
	 * Downloads the latest earthquakes from Geonet, and populates them back
	 * into the display.
	 * 
	 * @author Adam Speakman
	 * 
	 */
	private class DownloadQuakesTask extends
			AsyncTask<Integer, Void, ArrayList<Earthquake>> {
		/**
		 * Called before the worker thread is executed. Runs on the UI thread.
		 */
		@Override
		protected void onPreExecute() {
			mDownloading = true;
			updateRefreshButtonVisibility();
			setSupportProgress(Window.PROGRESS_END);
			setSupportProgressBarIndeterminateVisibility(true);
		}

		/**
		 * The system calls this to perform work in a worker thread and delivers
		 * it the parameters given to AsyncTask.execute()
		 */
		@Override
		protected ArrayList<Earthquake> doInBackground(Integer... params) {
			return GeonetAccessor.getQuakes();
		}

		/**
		 * The system calls this to perform work in the UI thread and delivers
		 * the result from doInBackground()
		 */
		@Override
		protected void onPostExecute(ArrayList<Earthquake> results) {
			if(isCancelled()) {
				Log.d("WSNZ", "This download task has been killed. Not updating results.");
				return;
			}
			mDownloading = false;
			setSupportProgressBarIndeterminateVisibility(false);
			updateRefreshButtonVisibility();
			if (null == results) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						MainActivity.this);
				dialog.setTitle("No Connection")
						.setMessage(
								"There appears to be a problem "
										+ "with the connection. Please make sure "
										+ "you have internet connectivity.")
						.setNeutralButton("Close", null);
				dialog.show();
				results = new ArrayList<Earthquake>();
			}
			mQuakes = results;
			// Update our "last checked" key in the prefs.
			if (results.size() > 0) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				Editor editor = prefs.edit();
				editor.putString(GeonetService.KEY_PREFS_LAST_CHECKED_ID, results
						.get(0).getReference());
				editor.commit();
			}
			updateQuakesDisplay();
		}
	}

}
