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

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

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

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.WhatsShakingApplication;
import speakman.whatsshakingnz.analytics.Analytics;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.realm.RealmEarthquake;
import speakman.whatsshakingnz.ui.maps.MapMarkerOptionsFactory;
import speakman.whatsshakingnz.ui.views.ExpandableDetailCard;
import speakman.whatsshakingnz.utils.UserSettings;

/**
 * Created by Adam on 1/20/2016.
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, RealmChangeListener<RealmResults<RealmEarthquake>> {

    private static final int MAX_EARTHQUAKES_TO_DISPLAY = 10;

    public static Intent createIntent(Context ctx) {
        return new Intent(ctx, MapActivity.class);
    }

    private Earthquake selectedEarthquake;
    private ExpandableDetailCard detailView;
    private RealmResults<RealmEarthquake> earthquakes;
    private Realm realm;
    private MapView mapView;
    private final List<Marker> mapMarkers = new ArrayList<>();
    private final Map<String, Earthquake> markerEarthquakeMap = new HashMap<>();

    @Inject
    MapMarkerOptionsFactory mapMarkerOptionsFactory;

    @Inject
    UserSettings userSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        ((WhatsShakingApplication) getApplication()).inject(this);
        setContentView(R.layout.activity_map);
        realm = Realm.getDefaultInstance();
        detailView = (ExpandableDetailCard) findViewById(R.id.activity_map_detail_card);
        assert detailView != null;
        detailView.setVisibility(View.INVISIBLE);
        detailView.setOnDetailExpandListener(new DetailActivity.DetailCardGravityController());
        mapView = (MapView) findViewById(R.id.activity_map_map);
        assert mapView != null;
        mapView.onCreate(savedInstanceState == null ? null : savedInstanceState.getBundle("mapState"));
        getEarthquakesAsync();
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
        if (earthquakes != null) {
            earthquakes.removeChangeListener(this);
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
        if (detailView.onBackPressed()) {
            //noinspection UnnecessaryReturnStatement
            return;
        } else if (selectedEarthquake == null) {
            super.onBackPressed();
        } else {
            selectedEarthquake = null;
            hideDetailView();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        markerEarthquakeMap.clear();
        for (Marker marker : mapMarkers) {
            marker.remove();
        }
        int count = Math.min(MAX_EARTHQUAKES_TO_DISPLAY, earthquakes.size());
        for (int i = 0; i < count; i++) {
            Earthquake earthquake = earthquakes.get(i);
            MarkerOptions markerOptions = mapMarkerOptionsFactory.getMarkerOptions(earthquake);
            Marker marker = googleMap.addMarker(markerOptions);
            mapMarkers.add(marker);
            markerEarthquakeMap.put(marker.getId(), earthquake);
        }
        googleMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Earthquake clickedEarthquake = markerEarthquakeMap.get(marker.getId());
        if (clickedEarthquake == selectedEarthquake) {
            selectedEarthquake = null;
            hideDetailView();
        } else {
            selectedEarthquake = clickedEarthquake;
            detailView.bindEarthquake(selectedEarthquake);
            showDetailView();
            logEarthquakeClick(selectedEarthquake);
        }
        return true;
    }

    @Override
    public void onChange(RealmResults<RealmEarthquake> realmEarthquakes) {
        refreshUI();
    }

    private void hideDetailView() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(detailView, "translationY",
                0f, getDetailViewAnimationTranslation())
                .setDuration(350);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();
    }

    private void showDetailView() {
        detailView.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(detailView, "translationY",
                getDetailViewAnimationTranslation(), 0f)
                .setDuration(350);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    private void refreshUI() {
        mapView.getMapAsync(this);
    }

    private void getEarthquakesAsync() {
        if (earthquakes != null) {
            earthquakes.removeChangeListener(this);
        }
        earthquakes = realm.where(RealmEarthquake.class).greaterThanOrEqualTo(RealmEarthquake.FIELD_NAME_MAGNITUDE, userSettings.minimumDisplayMagnitude())
                .sort(RealmEarthquake.FIELD_NAME_ORIGIN_TIME, Sort.DESCENDING).findAllAsync();
        earthquakes.addChangeListener(this);
    }

    private float getDetailViewAnimationTranslation() {
        return detailView.getMeasuredHeight() + ((CardView.LayoutParams) detailView.getLayoutParams()).bottomMargin;
    }

    private void logEarthquakeClick(Earthquake earthquake) {
        Analytics.logEarthquakeSelectedOnMap(earthquake);
    }
}