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

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.realm.RealmEarthquake;
import speakman.whatsshakingnz.network.NetworkRunnerService;
import speakman.whatsshakingnz.ui.DividerItemDecoration;
import speakman.whatsshakingnz.ui.EarthquakeListAdapter;
import speakman.whatsshakingnz.ui.LicensesFragment;
import speakman.whatsshakingnz.ui.maps.MapMarkerOptionsFactory;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, RealmChangeListener {

    public static String EXTRA_FROM_NOTIFICATION = "speakman.whatsshakingnz.ui.activities.MainActivity.EXTRA_FROM_NOTIFICATION";
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
    private List<Marker> mapMarkers = new ArrayList<>();
    private RealmResults<RealmEarthquake> earthquakes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.activity_main_toolbar));
        realm = Realm.getDefaultInstance();
        map = ((MapView)findViewById(R.id.activity_main_map));
        map.onCreate(savedInstanceState == null ? null : savedInstanceState.getBundle("mapState"));
        RecyclerView mainList = (RecyclerView) findViewById(R.id.activity_main_list);
        mainList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mainList.setHasFixedSize(true);
        mainList.setLayoutManager(new LinearLayoutManager(this));
        dataAdapter = new EarthquakeListAdapter();
        mainList.setAdapter(dataAdapter);
        dataAdapter.updateList(realm.allObjectsSorted(RealmEarthquake.class, "originTime", Sort.DESCENDING));
        map.getMapAsync(this);
        requestNewEarthquakes();
        if (savedInstanceState == null && getIntent().getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)) {
            logNotificationClick();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
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
            case R.id.menu_action_google_licences:
                LicensesFragment.displayGooglePlayServicesLicensesFragment(getFragmentManager(), true);
                return true;
            case R.id.menu_action_settings:
                startActivity(SettingsActivity.createIntent(this));
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
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setOnMapClickListener(this);
        for (Marker marker : mapMarkers) {
            marker.remove();
        }
        List<? extends Earthquake> earthquakes = getEarthquakes();
        int count = Math.min(10, earthquakes.size());
        for (int i = 0; i < count; i++) {
            MarkerOptions markerOptions = MapMarkerOptionsFactory.getMarkerOptions(earthquakes.get(i));
            Marker marker = googleMap.addMarker(markerOptions);
            mapMarkers.add(marker);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Intent intent = MapActivity.createIntent(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this, map, map.getTransitionName());
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    @Override
    public void onChange() {
        dataAdapter.notifyDataSetChanged();
        map.getMapAsync(this);
    }

    private List<? extends Earthquake> getEarthquakes() {
        if (earthquakes == null) {
            earthquakes = realm.allObjectsSorted(RealmEarthquake.class, "originTime", Sort.DESCENDING);
            earthquakes.addChangeListener(this);
        }
        return earthquakes;
    }

    private void requestNewEarthquakes() {
        NetworkRunnerService.requestLatest(this);
    }

    private void logNotificationClick() {
        Timber.i("User clicked multi-earthquake detail notification.");
    }
}
