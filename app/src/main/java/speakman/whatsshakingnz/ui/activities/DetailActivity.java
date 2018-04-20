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
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.WhatsShakingApplication;
import speakman.whatsshakingnz.analytics.Analytics;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.repository.EarthquakeRepository;
import speakman.whatsshakingnz.ui.maps.IgnoreClicksMapMarkerClickListener;
import speakman.whatsshakingnz.ui.maps.MapMarkerOptionsFactory;
import speakman.whatsshakingnz.ui.views.ExpandableDetailCard;
import speakman.whatsshakingnz.utils.Strings;
import timber.log.Timber;

public class DetailActivity extends WhatsShakingActivity {

    static class DetailCardGravityController implements ExpandableDetailCard.OnDetailExpandListener {
        @Override
        public void onExpand(ExpandableDetailCard card) {
            if (card.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) card.getLayoutParams();
                layoutParams.gravity = Gravity.CENTER;
                card.setLayoutParams(layoutParams);
            }
        }

        @Override
        public void onCollape(ExpandableDetailCard card) {
            if (card.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) card.getLayoutParams();
                layoutParams.gravity = Gravity.BOTTOM;
                card.setLayoutParams(layoutParams);
            }
        }
    }

    private static final String EXTRA_EARTHQUAKE = "speakman.whatsshakingnz.ui.activities.DetailActivity.EXTRA_EARTHQUAKE";
    private static final String EXTRA_FROM_NOTIFICATION = "speakman.whatsshakingnz.ui.activities.DetailActivity.EXTRA_FROM_NOTIFICATION";

    public static Intent createIntentFromNotification(Context ctx, Earthquake earthquake) {
        Intent intent = createIntent(ctx, earthquake);
        intent.putExtra(EXTRA_FROM_NOTIFICATION, true);
        // Ensure we update the activity if it's already on the top.
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    public static Intent createIntent(Context ctx, Earthquake earthquake) {
        Intent intent = new Intent(ctx, DetailActivity.class);
        intent.putExtra(EXTRA_EARTHQUAKE, earthquake.getId());
        return intent;
    }

    private MapView mapView;
    private Marker mapMarker;
    private ExpandableDetailCard expandableDetailCard;

    @Inject
    MapMarkerOptionsFactory mapMarkerOptionsFactory;

    @Inject
    EarthquakeRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        ((WhatsShakingApplication) getApplication()).inject(this);
        setContentView(R.layout.activity_detail);
        mapMarkerOptionsFactory = new MapMarkerOptionsFactory(this);
        expandableDetailCard = findViewById(R.id.activity_detail_detail_card);
        expandableDetailCard.setOnDetailExpandListener(new DetailCardGravityController());
        mapView = findViewById(R.id.activity_detail_map);
        assert mapView != null;
        mapView.onCreate(savedInstanceState == null ? null : savedInstanceState.getBundle("mapState"));
        if (savedInstanceState == null && getIntent().getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)) {
            logNotificationClick();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // If we're looking at a new Earthquake, we can't animate back to the old one!
            getWindow().setSharedElementReturnTransition(null);
        }
        logNotificationClick();
        refreshUI();
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

    private void updateMapMarker(GoogleMap googleMap, Earthquake earthquake) {
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setOnMarkerClickListener(new IgnoreClicksMapMarkerClickListener());
        if (mapMarker != null) {
            mapMarker.remove();
        }
        MarkerOptions markerOptions = mapMarkerOptionsFactory.getMarkerOptions(earthquake);
        mapMarker = googleMap.addMarker(markerOptions);
    }

    private void refreshUI() {
        safeSubscribe(repository.earthquakeForId(getEarthquakeId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(earthquake -> {
                    expandableDetailCard.bindEarthquake(earthquake);
                    expandableDetailCard.setForegroundGravity(Gravity.CENTER_VERTICAL);
                    mapView.getMapAsync(map -> updateMapMarker(map, earthquake));
                }));
    }

    private void logNotificationClick() {
        Timber.i("User clicked single-earthquake detail notification.");
        Analytics.logEarthquakeViewFromNotification(getEarthquakeId());
    }

    private String getEarthquakeId() {
        return Strings.nullToEmpty(getIntent().getStringExtra(EXTRA_EARTHQUAKE));
    }
}
