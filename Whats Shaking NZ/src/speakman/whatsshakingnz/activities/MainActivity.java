package speakman.whatsshakingnz.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.TabHost;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.earthquake.Earthquake;
import speakman.whatsshakingnz.earthquake.EarthquakeFilter;
import speakman.whatsshakingnz.earthquake.EarthquakeTapListener;
import speakman.whatsshakingnz.fragments.EarthquakeDetailFragment;
import speakman.whatsshakingnz.fragments.ListFragment;
import speakman.whatsshakingnz.fragments.NZMapFragment;
import speakman.whatsshakingnz.geonet.GeonetAccessor;
import speakman.whatsshakingnz.geonet.GeonetService;
import speakman.whatsshakingnz.preferences.DefaultPrefs;
import speakman.whatsshakingnz.views.AnimatingLinearLayout;

import java.util.ArrayList;

public class MainActivity extends SherlockFragmentActivity implements
        OnSharedPreferenceChangeListener, TabHost.OnTabChangeListener, EarthquakeTapListener {

    private static final String TAB_TAG_LIST = "tab_list";
    private static final String TAB_TAG_MAP = "tab_map";

    private static final String FRAGMENT_TAG_LIST = "fragment_list";
    private static final String FRAGMENT_TAG_MAP = "fragment_map";
    private static final String FRAGMENT_TAG_DETAIL = "fragment_detail";

    private AsyncTask mDownloadTask;

    private NZMapFragment mMapFragment;
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
    private String mSelectedTab;
    private boolean mTabletMode;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Set defaults before we do anything else.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Request Feature must be called before adding content.
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);
        if (tabHost == null)
            mTabletMode = true;
        else
            mTabletMode = false;

        // First start-up
        if (null == savedInstanceState) {
            if (tabHost != null) { // tabbed layout
                mSelectedTab = TAB_TAG_LIST;
            } else { // 2-pane layout
                mListFragment = (ListFragment) getSupportFragmentManager()
                        .findFragmentByTag(FRAGMENT_TAG_LIST);
                mMapFragment = (NZMapFragment) getSupportFragmentManager()
                        .findFragmentByTag(FRAGMENT_TAG_MAP);
            }
            /**
             * Set this to true, so that quakes are downloaded and preference items
             * are updated in the onResume call.
             */
            mPreferencesUpdated = true;
        }
        // App was killed by the OS
        else {
            mListFragment = (ListFragment) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENT_TAG_LIST);
            mMapFragment = (NZMapFragment) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENT_TAG_MAP);
            mQuakes = savedInstanceState.getParcelableArrayList("mQuakes");
            if(tabHost != null) { // tabbed layout
                mSelectedTab = savedInstanceState.getString("mSelectedTab");
            }
        }
        if (mListFragment != null) mListFragment.setOnEarthquakeTapListener(this);
        if (mMapFragment != null) mMapFragment.setOnEarthquakeTapListener(this);

        /**
         * This has to come after we've acquired our fragments and list of quakes from saved state, or we get NPEs with
         * fragments not being attached to the activity etc.
         */
        if(tabHost != null) {
            tabHost.setup();
            tabHost.addTab(tabHost.newTabSpec(TAB_TAG_LIST)
                    .setIndicator(getString(R.string.tab_title_list))
                    .setContent(R.id.list_view_placeholder));
            tabHost.addTab(tabHost.newTabSpec(TAB_TAG_MAP)
                    .setIndicator(getString(R.string.tab_title_map))
                    .setContent(R.id.map_view_placeholder));
            // Set the listener after adding tabs, because we don't want to know about adding them.
            tabHost.setOnTabChangedListener(this);
            /**
             * Have to call both setCurrentTabByTag (because if we've just been restored on map tab, then we have to
             * tell the tab host to show the map tab) and onTabChanged (because if we've just been restored on list tab,
             * then we have to tell the data to update).
             */
            tabHost.setCurrentTabByTag(mSelectedTab);
            onTabChanged(mSelectedTab);
        }

        updateItemsFromPreferences();
        updateQuakesDisplay();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mSelectedTab", mSelectedTab);
        outState.putParcelableArrayList("mQuakes", mQuakes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        if (mPreferencesUpdated) {
            mPreferencesUpdated = false;
            updateItemsFromPreferences();
            downloadQuakes();
        }
    }

    private void updateItemsFromPreferences() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        mMaxNumberOfQuakes = prefs.getInt(
                PreferenceActivity.KEY_PREF_NUM_QUAKES_TO_SHOW, 10);
        mMinDisplay = prefs.getInt(PreferenceActivity.KEY_PREF_MIN_DISPLAY_MAGNITUDE,
                DefaultPrefs.MIN_DISPLAY_MAGNITUDE);
    }

    @Override
    protected void onDestroy() {
        if (null != mDownloadTask) {
            Log.d("WSNZ", "Killing background download task as onDestroy() was called before it returned.");
            mDownloadTask.cancel(true);
        }
        super.onDestroy();
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
    public void onTabChanged(String tabId) {
        mSelectedTab = tabId;
        if(TAB_TAG_LIST.equals(tabId)) {
            if (mListFragment == null) {
                mListFragment = new ListFragment();
                mListFragment.setOnEarthquakeTapListener(this);
                replaceFragment(R.id.list_view_placeholder, mListFragment, FRAGMENT_TAG_LIST);
            }
        } else {
            if (mMapFragment == null) {
                mMapFragment = NZMapFragment.newInstance();
                mMapFragment.setOnEarthquakeTapListener(this);
                replaceFragment(R.id.map_view_placeholder, mMapFragment, FRAGMENT_TAG_MAP);
            }
        }
        updateQuakesDisplay();

    }

    private void replaceFragment(int placeholderId, Fragment replacementFragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(placeholderId, replacementFragment, tag)
                .commit();
    }

    /**
     * Called to update the quakes display. Will update whichever view is
     * selected at the moment. If you have new quakes to display, be sure to set
     * {@link #mQuakes} first, then call this method.
     */
    private void updateQuakesDisplay() {
        updateQuakesList();
        updateQuakesMap();
    }

    /**
     * Updates the map view. Please use {@link #updateQuakesDisplay} instead.
     */
    private void updateQuakesMap() {
        if (null == mQuakes || null == mMapFragment)
            return;
        mMapFragment.setQuakes(EarthquakeFilter.filterQuakes(mQuakes, ((float) mMinDisplay) / 10.0f, mMaxNumberOfQuakes));
    }

    /**
     * Updates the list view. Please use {@link #updateQuakesDisplay} instead.
     */
    private void updateQuakesList() {
        if (null == mQuakes || null == mListFragment)
            return;
        mListFragment.updateQuakes(EarthquakeFilter.filterQuakes(mQuakes, ((float) mMinDisplay) / 10.0f, mMaxNumberOfQuakes));
    }

    private void downloadQuakes() {
        if (mDownloading)
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

    @Override
    public void onEarthquakeTap(Earthquake quake) {
        if (quake == null) return;

        if (!mTabletMode) {
            Intent intent = new Intent(this, QuakeActivity.class);
            intent.putExtra(QuakeActivity.QUAKE_KEY, quake);
            startActivity(intent);
        } else {
            EarthquakeDetailFragment detailFragment = (EarthquakeDetailFragment) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENT_TAG_DETAIL);
            if (detailFragment == null)  return;

            AnimatingLinearLayout v = (AnimatingLinearLayout)findViewById(R.id.earthquake_details_animating_overlay);
            if (v == null) return;

            if (quake.equals(detailFragment.getQuake()) && v.isVisible()) {
                detailFragment.setQuake(null);
                v.hide(true);
            } else {
                detailFragment.setQuake(quake);
                v.show(true);
            }
        }
    }

    @Override
    public void onEarthquakeLostFocus(Earthquake quake) {
        AnimatingLinearLayout v = (AnimatingLinearLayout)findViewById(R.id.earthquake_details_animating_overlay);
        if (v != null) {
            v.hide(true);
        }
    }

    /**
     * Downloads the latest earthquakes from Geonet, and populates them back
     * into the display.
     *
     * @author Adam Speakman
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
            if (isCancelled()) {
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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                Editor editor = prefs.edit();
                editor.putString(GeonetService.KEY_PREFS_LAST_CHECKED_ID, results
                        .get(0).getReference());
                editor.commit();
            }
            updateQuakesDisplay();
        }
    }

}
