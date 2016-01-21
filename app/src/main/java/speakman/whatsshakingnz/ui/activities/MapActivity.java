/*
 * Copyright 2016 Adam Speakman
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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.WhatsShakingApplication;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.EarthquakeStore;
import speakman.whatsshakingnz.ui.maps.MapMarkerOptionsFactory;

/**
 * Created by Adam on 1/20/2016.
 */
public class MapActivity extends AppCompatActivity implements EarthquakeStore.EarthquakeDataChangeObserver, OnMapReadyCallback {

    public static Intent createIntent(Context ctx) {
        Intent intent = new Intent(ctx, MapActivity.class);
        return intent;
    }

    @Inject
    EarthquakeStore store;

    private MapView mapView;
    private List<Marker> mapMarkers = new ArrayList<>();
    private Map<String, Earthquake> markerEarthquakeMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setSupportActionBar((Toolbar) findViewById(R.id.activity_map_toolbar));
        WhatsShakingApplication.getInstance().inject(this);
        mapView = (MapView) findViewById(R.id.activity_map_map);
        mapView.onCreate(savedInstanceState == null ? null : savedInstanceState.getBundle("mapState"));
        refreshUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        store.registerDataChangeObserver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        store.unregisterDataChangeObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapState = new Bundle();
        mapView.onSaveInstanceState(mapState);
        outState.putBundle("mapState", mapState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onEarthquakeDataChanged() {
        refreshUI();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        markerEarthquakeMap.clear();
        for (Marker marker : mapMarkers) {
            marker.remove();
        }
        List<? extends Earthquake> latestEarthquakes = getEarthquakes();
        for (Earthquake earthquake : latestEarthquakes) {
            MarkerOptions markerOptions = MapMarkerOptionsFactory.getMarkerOptions(earthquake);
            Marker marker = googleMap.addMarker(markerOptions);
            mapMarkers.add(marker);
            markerEarthquakeMap.put(marker.getId(), earthquake);
        }
    }

    private List<? extends Earthquake> getEarthquakes() {
        List<? extends Earthquake> earthquakes = store.getEarthquakes();
        return earthquakes.subList(0, Math.min(10, earthquakes.size()));
    }

    private void refreshUI() {
        mapView.getMapAsync(this);
    }


}