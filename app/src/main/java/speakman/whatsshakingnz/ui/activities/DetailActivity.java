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
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.realm.RealmEarthquake;
import speakman.whatsshakingnz.ui.maps.MapMarkerOptionsFactory;
import speakman.whatsshakingnz.ui.views.ExpandableDetailCard;
import timber.log.Timber;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback, RealmChangeListener {

    public static String EXTRA_EARTHQUAKE = "speakman.whatsshakingnz.ui.activities.DetailActivity.EXTRA_EARTHQUAKE";
    public static String EXTRA_FROM_NOTIFICATION = "speakman.whatsshakingnz.ui.activities.DetailActivity.EXTRA_FROM_NOTIFICATION";

    public static Intent createIntentFromNotification(Context ctx, Earthquake earthquake) {
        Intent intent = createIntent(ctx, earthquake);
        intent.putExtra(EXTRA_FROM_NOTIFICATION, true);
        return intent;
    }

    public static Intent createIntent(Context ctx, Earthquake earthquake) {
        Intent intent = new Intent(ctx, DetailActivity.class);
        intent.putExtra(EXTRA_EARTHQUAKE, earthquake.getId());
        return intent;
    }

    private Realm realm;
    private RealmEarthquake earthquake;
    private MapView mapView;
    private Marker mapMarker;
    private ExpandableDetailCard expandableDetailCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        setContentView(R.layout.activity_detail);
        expandableDetailCard = (ExpandableDetailCard) findViewById(R.id.activity_map_detail_card);
        mapView = (MapView) findViewById(R.id.activity_detail_map);
        mapView.onCreate(savedInstanceState == null ? null : savedInstanceState.getBundle("mapState"));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (savedInstanceState == null && getIntent().getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)) {
            Timber.i("User clicked single-earthquake detail notification.");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Timber.i("User clicked single-earthquake detail notification.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        refreshUI();
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
    public void onBackPressed() {
        if (expandableDetailCard.onBackPressed()) {
            return;
        }
        super.onBackPressed();
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
        expandableDetailCard.bindEarthquake(earthquake);
        mapView.getMapAsync(this);
    }
}
