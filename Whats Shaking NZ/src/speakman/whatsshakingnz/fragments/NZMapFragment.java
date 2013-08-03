package speakman.whatsshakingnz.fragments;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;
import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.activities.PreferenceActivity;
import speakman.whatsshakingnz.activities.QuakeActivity;
import speakman.whatsshakingnz.earthquake.Earthquake;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import speakman.whatsshakingnz.preferences.DefaultPrefs;

public class NZMapFragment extends SupportMapFragment {
    private ArrayList<Earthquake> mQuakes;
    private Drawable regularDrawable, warningDrawable;
    private static final double NZ_CENTRE_LATITUDE = -41;
    private static final double NZ_CENTRE_LONGITUDE = 173;

    public static NZMapFragment newInstance() {
        GoogleMapOptions defaultOptions = new GoogleMapOptions();
        CameraPosition pos = new CameraPosition(new LatLng(NZ_CENTRE_LATITUDE, NZ_CENTRE_LONGITUDE), // target
                // TODO Zoom should probably change by device...
                5, // zoom
                0, // tilt
                0); // bearing
        defaultOptions.camera(pos);
        return NZMapFragment.newInstance(defaultOptions);
    }

    public static NZMapFragment newInstance(GoogleMapOptions options) {
        NZMapFragment fragment = new NZMapFragment();

        Bundle args = new Bundle();
        // See http://stackoverflow.com/a/13783463/1217087
        args.putParcelable("MapOptions", options); //obtained by decompiling google-play-services.jar
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        regularDrawable = this.getResources().getDrawable(R.drawable.mapmarker);
        warningDrawable = this.getResources().getDrawable(
                R.drawable.mapmarker_warn);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateOverlayItems();
    }

    public void updateQuakes(ArrayList<Earthquake> quakes) {
        mQuakes = quakes;
        updateOverlayItems();
    }

    private void updateOverlayItems() {
        GoogleMap map = getMap();
        if (map == null || null == mQuakes)
            return;
        // Move the map view if this is a QuakeActivity, will
        // only have one quake in this instance.
        if (getActivity() instanceof QuakeActivity) {
            LatLng latlng = getLatLngForQuake(mQuakes.get(0));
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng,
                    getDefaultZoomForDevice() + 2);
            map.moveCamera(update);
        }
//        List<Overlay> mapOverlays = mView.getOverlays();
//        mapOverlays.clear();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        float warnMagnitude = ((float) prefs.getInt(
                PreferenceActivity.KEY_PREF_MIN_HIGHLIGHT_MAGNITUDE,
                DefaultPrefs.MIN_HIGHLIGHT_MAGNITUDE)) / 10.0f;
        // Doing this this way (a new MapOverlay for every earthquake)
        // is very inefficient. However, it solves the problem of the
        // title text for all quakes having the same z-index (and
        // therefore overlapping each other).
        for (Earthquake q : mQuakes) {
            map.addMarker(new MarkerOptions()
                .position(getLatLngForQuake(q))
                .title(q.getFormattedMagnitude()));
//            MapOverlay overlay;
//            if (q.getRoundedMagnitude() >= warnMagnitude)
//                overlay = new MapOverlay(warningDrawable, this.getActivity());
//            else
//                overlay = new MapOverlay(regularDrawable, this.getActivity());
//            overlay.addOverlay(q);
//            mapOverlays.add(overlay);
        }
//        mView.invalidate();
    }

    private LatLng getLatLngForQuake(Earthquake quake) {
        GeoPoint quakeLocation = quake.getPoint();
        LatLng latlng = new LatLng(quakeLocation.getLatitudeE6() / 1E6, quakeLocation.getLongitudeE6() / 1E6);
        return latlng;
    }

    /**
     * Returns the default zoom level for the device size. Different screen
     * sizes require different default zoom levels.
     *
     * @return The default zoom level to show the whole NZ map, for the current
     * device.
     */
    private int getDefaultZoomForDevice() {
        WindowManager wm = (WindowManager) getActivity().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = display.getWidth();

        if (width < 480)
            return 5;
        else if (width < 720)
            return 6;
        else
            return 7;
    }
}
