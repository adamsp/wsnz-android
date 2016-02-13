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
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.WhatsShakingApplication;
import speakman.whatsshakingnz.databinding.ActivityDetailBinding;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.realm.RealmEarthquake;
import speakman.whatsshakingnz.ui.maps.MapMarkerOptionsFactory;
import speakman.whatsshakingnz.ui.viewmodel.EarthquakeOverviewViewModel;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback, RealmChangeListener {

    public static String EXTRA_EARTHQUAKE = "speakman.whatsshakingnz.ui.activities.DetailActivity.EXTRA_EARTHQUAKE";


    public static Intent createIntent(Context ctx, Earthquake earthquake) {
        Intent intent = new Intent(ctx, DetailActivity.class);
        intent.putExtra(EXTRA_EARTHQUAKE, earthquake.getId());
        return intent;
    }

    private Realm realm;
    private RealmEarthquake earthquake;
    private MapView mapView;
    private ActivityDetailBinding binding;
    private Marker mapMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WhatsShakingApplication.getInstance().inject(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);
        realm = Realm.getDefaultInstance();
        mapView = (MapView) findViewById(R.id.activity_detail_map);
        mapView.onCreate(savedInstanceState == null ? null : savedInstanceState.getBundle("mapState"));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        refreshUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (earthquake != null) {
            earthquake.removeChangeListener(this);
        }
        realm.close();
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
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        if (mapMarker != null) {
            mapMarker.remove();
        }
        MarkerOptions markerOptions = MapMarkerOptionsFactory.getMarkerOptions(getEarthquake());
        mapMarker = googleMap.addMarker(markerOptions);
    }

    @Override
    public void onChange() {
        refreshUI();
    }

    private Earthquake getEarthquake() {
        if (earthquake == null) {
            earthquake = realm.where(RealmEarthquake.class).equalTo("id", getIntent().getStringExtra(EXTRA_EARTHQUAKE)).findFirst();
            earthquake.addChangeListener(this);
        }
        return earthquake;
    }

    private void refreshUI() {
        Earthquake earthquake = getEarthquake();
        EarthquakeOverviewViewModel viewModel = new EarthquakeOverviewViewModel(earthquake);
        binding.setEarthquakeModel(viewModel);
        mapView.getMapAsync(this);
    }
}
