/*
 * Copyright 2015 Adam Speakman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package speakman.whatsshakingnz.ui.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import speakman.whatsshakingnz.BuildConfig;
import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.WhatsShakingApplication;
import speakman.whatsshakingnz.analytics.Analytics;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.realm.RealmEarthquake;
import speakman.whatsshakingnz.network.NetworkRunnerService;
import speakman.whatsshakingnz.network.NotificationTimeStore;
import speakman.whatsshakingnz.ui.DividerItemDecoration;
import speakman.whatsshakingnz.ui.EarthquakeHeadersAdapter;
import speakman.whatsshakingnz.ui.EarthquakeListAdapter;
import speakman.whatsshakingnz.ui.LicensesFragment;
import speakman.whatsshakingnz.ui.StickyHeadersDecoration;
import speakman.whatsshakingnz.ui.maps.MapMarkerOptionsFactory;
import speakman.whatsshakingnz.ui.viewmodel.EarthquakeListViewModel;
import speakman.whatsshakingnz.utils.NotificationUtil;
import speakman.whatsshakingnz.utils.UserSettings;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, RealmChangeListener, EarthquakeListViewModel.ViewHolder.OnClickListener {

    public static final String EXTRA_FROM_NOTIFICATION = "speakman.whatsshakingnz.ui.activities.MainActivity.EXTRA_FROM_NOTIFICATION";
    private static final int ACTIVITY_REQUEST_CODE_SETTINGS = 1;

    public static Intent createIntentFromNotification(Context ctx) {
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_FROM_NOTIFICATION, true);
        return intent;
    }

    private Realm realm;
    private MapView map;
    private EarthquakeListAdapter dataAdapter;
    private EarthquakeHeadersAdapter headerAdapter;
    private final List<Marker> mapMarkers = new ArrayList<>();
    private View emptyListView;
    private RealmResults<RealmEarthquake> earthquakes;

    @Inject
    MapMarkerOptionsFactory mapMarkerOptionsFactory;

    @Inject
    Lazy<NotificationUtil> notiticationUtil;

    @Inject
    NotificationTimeStore notificationTimeStore;

    @Inject
    UserSettings userSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WhatsShakingApplication) getApplication()).inject(this);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.activity_main_toolbar));
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(R.string.activity_main_title);
        }
        realm = Realm.getDefaultInstance();
        map = ((MapView)findViewById(R.id.activity_main_map));
        assert map != null;
        map.onCreate(savedInstanceState == null ? null : savedInstanceState.getBundle("mapState"));
        dataAdapter = new EarthquakeListAdapter(this);
        headerAdapter = new EarthquakeHeadersAdapter();
        RecyclerView mainList = (RecyclerView) findViewById(R.id.activity_main_list);
        if (mainList != null) {
            mainList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
            mainList.addItemDecoration(new StickyHeadersDecoration(headerAdapter));
            mainList.setHasFixedSize(true);
            mainList.setLayoutManager(new LinearLayoutManager(this));
            mainList.setAdapter(dataAdapter);
        }
        emptyListView = findViewById(R.id.activity_main_list_empty_view);
        requestForegroundSync();
        if (savedInstanceState == null && getIntent().getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)) {
            logNotificationClick();
        }
        getEarthquakesAsync();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        cancelNotifications();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.onDestroy();
        if (earthquakes != null) {
            earthquakes.removeChangeListener(this);
        }
        realm.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapState = new Bundle();
        map.onSaveInstanceState(mapState);
        outState.putBundle("mapState", mapState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_action_licences:
                LicensesFragment.displayLicensesFragment(getFragmentManager(), true);
                return true;
            case R.id.menu_action_settings:
                navigateToSettingsActivity();
                return true;
            case R.id.menu_action_show_single_notif:
                showSingleNotif();
                return true;
            case R.id.menu_action_show_multi_notif:
                showMultiNotif();
                return true;
            case R.id.menu_action_trigger_exception:
                triggerFatalException();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == SettingsActivity.RESULT_CODE_SETTING_CHANGED) {
            getEarthquakesAsync();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setOnMapClickListener(this);
        googleMap.setOnMarkerClickListener(this);
        for (Marker marker : mapMarkers) {
            marker.remove();
        }
        if (earthquakes != null && earthquakes.size() > 0) {
            int count = Math.min(10, earthquakes.size());
            for (int i = 0; i < count; i++) {
                MarkerOptions markerOptions = mapMarkerOptionsFactory.getMarkerOptions(earthquakes.get(i));
                Marker marker = googleMap.addMarker(markerOptions);
                mapMarkers.add(marker);
            }
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Analytics.logMainPageMapClicked();
        navigateToMapActivity();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Analytics.logMainPageMapMarkerClicked();
        navigateToMapActivity();
        return true;
    }

    @Override
    public void onChange() {
        dataAdapter.notifyDataSetChanged();
        map.getMapAsync(this);
        storeMostRecentEventOriginTime();
        if (this.earthquakes != null && this.earthquakes.size() > 0) {
            emptyListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEarthquakeClick(View v, Earthquake earthquake) {
        Analytics.logEarthquakeSelectedInList(earthquake);
        ActivityOptionsCompat options = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this, v, v.getTransitionName());
        }
        Intent intent = DetailActivity.createIntent(this, earthquake);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && options != null) {
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    private void navigateToSettingsActivity() {
        startActivityForResult(SettingsActivity.createIntent(this), ACTIVITY_REQUEST_CODE_SETTINGS);
    }

    private void navigateToMapActivity() {
        Intent intent = MapActivity.createIntent(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this, map, map.getTransitionName());
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    private void getEarthquakesAsync() {
        if (earthquakes != null) {
            earthquakes.removeChangeListener(this);
        }
        earthquakes = realm.where(RealmEarthquake.class).greaterThanOrEqualTo(RealmEarthquake.FIELD_NAME_MAGNITUDE, userSettings.minimumDisplayMagnitude())
                    .findAllSortedAsync(RealmEarthquake.FIELD_NAME_ORIGIN_TIME, Sort.DESCENDING);
        earthquakes.addChangeListener(this);
        headerAdapter.updateList(earthquakes);
        dataAdapter.updateList(earthquakes);
    }

    private void requestForegroundSync() {
        NetworkRunnerService.requestLatest(this);
    }

    private void storeMostRecentEventOriginTime() {
        if (earthquakes != null && earthquakes.size() > 0) {
            notificationTimeStore.saveMostRecentlySeenEventOriginTime(new DateTime(earthquakes.first().getOriginTime()));
        }
    }

    private void logNotificationClick() {
        Timber.i("User clicked multi-earthquake detail notification.");
        Analytics.logMainPageViewedFromNotification();
    }

    private void cancelNotifications() {
        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mgr.cancelAll();
    }

//region Debug Features
    private void showSingleNotif() {
        if(!BuildConfig.DEBUG) {
            return;
        }
        if (earthquakes != null && earthquakes.size() > 0) {
            Notification notification = notiticationUtil.get().notificationForSingleEarthquake(earthquakes.get(0));
            NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mgr.notify(NotificationUtil.NOTIFICATION_ID, notification);
        }
    }

    private void showMultiNotif() {
        if(!BuildConfig.DEBUG) {
            return;
        }
        if (earthquakes != null && earthquakes.size() > 0) {
            Notification notification = notiticationUtil.get().notificationForMultipleEarthquakes(earthquakes);
            NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mgr.notify(NotificationUtil.NOTIFICATION_ID, notification);
        }
    }

    private void triggerFatalException() {
        throw new RuntimeException("Manually triggered debug exception");
    }
//endregion
}
