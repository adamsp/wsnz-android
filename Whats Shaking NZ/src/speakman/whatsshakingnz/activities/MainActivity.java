package speakman.whatsshakingnz.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
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
        OnSharedPreferenceChangeListener, ActionBar.TabListener, EarthquakeTapListener,
        LoaderManager.LoaderCallbacks<ArrayList<Earthquake>> {

    private static final String FRAGMENT_TAG_LIST = "fragment_list";
    private static final String FRAGMENT_TAG_MAP = "fragment_map";
    private static final String FRAGMENT_TAG_DETAIL = "fragment_detail";

    private String tabTitleList, tabTitleMap;

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
        // Note this turns it on by default, ABS thing (so only on 2.x devices).
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabletMode = findViewById(R.id.activity_main_tabs) == null;

        mListFragment = (ListFragment) getSupportFragmentManager()
                .findFragmentByTag(FRAGMENT_TAG_LIST);
        mMapFragment = (NZMapFragment) getSupportFragmentManager()
                .findFragmentByTag(FRAGMENT_TAG_MAP);

        mListFragment.setOnEarthquakeTapListener(this);
        mMapFragment.setOnEarthquakeTapListener(this);

        if(!mTabletMode) {
            tabTitleList = getString(R.string.tab_title_list);
            tabTitleMap = getString(R.string.tab_title_map);
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            addTab(tabTitleList);
            addTab(tabTitleMap);
        }

        // First start-up
        if (null == savedInstanceState) {
             // Set this to true, so that quakes are downloaded and preference items
             // are updated in the onResume call.
            mPreferencesUpdated = true;
        }
        // App was killed by the OS
        else {
            mDownloading = savedInstanceState.getBoolean("mDownloading");
            mQuakes = savedInstanceState.getParcelableArrayList("mQuakes");
            if (mDownloading || mQuakes == null) {
                mPreferencesUpdated = true;
            } else {
                hideProgress();
            }
            if(!mTabletMode) { // tabbed layout
                mSelectedTab = savedInstanceState.getString("mSelectedTab");
                getSupportActionBar().setSelectedNavigationItem(mSelectedTab.equals(tabTitleList) ? 0 : 1);
            }
        }

        updateItemsFromPreferences();
        updateQuakesDisplay();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mSelectedTab", mSelectedTab);
        outState.putParcelableArrayList("mQuakes", mQuakes);
        outState.putBoolean("mDownloading", mDownloading);
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
        showSelectedTab();
    }

    private void updateItemsFromPreferences() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        mMaxNumberOfQuakes = prefs.getInt(
                PreferenceActivity.KEY_PREF_NUM_QUAKES_TO_SHOW, DefaultPrefs.NUM_QUAKES_TO_DISPLAY);
        mMinDisplay = prefs.getInt(PreferenceActivity.KEY_PREF_MIN_DISPLAY_MAGNITUDE,
                DefaultPrefs.MIN_DISPLAY_MAGNITUDE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        mRefreshMenuItem = menu.findItem(R.id.menu_refresh);
        if(mDownloading) mRefreshMenuItem.setVisible(false);
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

    private void addTab(String title) {
        ActionBar.Tab tab = getSupportActionBar().newTab();
        tab.setText(title);
        tab.setTag(title);
        tab.setTabListener(this);
        getSupportActionBar().addTab(tab);
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        mSelectedTab = (String) tab.getTag();
        showSelectedTab();
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {

    }

    private void showSelectedTab() {
        if(mTabletMode) return;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if(tabTitleList.equals(mSelectedTab)) {
            ft.hide(mMapFragment);
            ft.show(mListFragment);
        } else {
            ft.hide(mListFragment);
            ft.show(mMapFragment);
        }
        ft.commit();
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
        mDownloading = true;
        showProgress();
        getSupportLoaderManager().initLoader(0, null, this);
    }

    private void hideProgress() {
        if (null != mRefreshMenuItem)
            mRefreshMenuItem.setVisible(true);
        setSupportProgressBarIndeterminateVisibility(false);
    }

    private void showProgress() {
        if (null != mRefreshMenuItem)
            mRefreshMenuItem.setVisible(false);
        setSupportProgress(Window.PROGRESS_END);
        setSupportProgressBarIndeterminateVisibility(true);
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
                mMapFragment.clearHighlight();
            } else {
                detailFragment.setQuake(quake);
                v.show(true);
                mMapFragment.highlightQuake(quake);
            }
        }
    }

    @Override
    public void onEarthquakeLostFocus(Earthquake quake) {
        if (!mTabletMode) return;
        AnimatingLinearLayout v = (AnimatingLinearLayout)findViewById(R.id.earthquake_details_animating_overlay);
        if (v != null && v.isVisible()) {
            v.hide(true);
            mMapFragment.clearHighlight();
        }
    }

    @Override
    public Loader<ArrayList<Earthquake>> onCreateLoader(int i, Bundle bundle) {
        AsyncTaskLoader<ArrayList<Earthquake>> loader = new AsyncTaskLoader<ArrayList<Earthquake>>(this) {
            @Override
            public ArrayList<Earthquake> loadInBackground() {
                return GeonetAccessor.getQuakes();
            }
        };
        loader.forceLoad();
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Earthquake>> objectLoader, ArrayList<Earthquake> results) {
        mDownloading = false;
        getSupportLoaderManager().destroyLoader(0);
        hideProgress();
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

    @Override
    public void onLoaderReset(Loader<ArrayList<Earthquake>> objectLoader) {

    }

}
