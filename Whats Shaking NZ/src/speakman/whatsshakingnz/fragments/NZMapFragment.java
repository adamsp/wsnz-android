package speakman.whatsshakingnz.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.maps.GeoPoint;
import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.activities.QuakeActivity;
import speakman.whatsshakingnz.earthquake.Earthquake;

import java.util.ArrayList;
import java.util.HashMap;

public class NZMapFragment extends SupportMapFragment implements GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {
    private ArrayList<Earthquake> mQuakes;
    private static final double NZ_CENTRE_LATITUDE = -41;
    private static final double NZ_CENTRE_LONGITUDE = 173;
    private HashMap<String, Earthquake> mMarkerIdToQuake;

    public NZMapFragment() {
        mMarkerIdToQuake = new HashMap<String, Earthquake>();
    }

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

    public void updateQuakes(ArrayList<Earthquake> quakes) {
        mQuakes = quakes;
        updateOverlayItems();
    }

    private void updateOverlayItems() {
        GoogleMap map = getMap();
        if (map == null || null == mQuakes)
            return;

        map.setOnInfoWindowClickListener(this);

        // Move the map view if this is a QuakeActivity, will
        // only have one quake in this instance.
        if (getActivity() instanceof QuakeActivity) {
            Earthquake singleQuake = mQuakes.get(0);
            LatLng latlng = getLatLngForQuake(singleQuake);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng,
                    getDefaultZoomForDevice() + 2);
            map.moveCamera(update);
            map.addMarker(getMarkerForQuake(singleQuake));
        } else {
            for (Earthquake q : mQuakes) {
                MarkerOptions markerOptions = getMarkerForQuake(q);
                // Specify a title so we get an info window when tapped.
                markerOptions.title(q.getFormattedMagnitude())
                        .snippet(q.getFormattedDepth());
                Marker marker = map.addMarker(markerOptions);
                mMarkerIdToQuake.put(marker.getId(), q);
            }
        }
    }

    private MarkerOptions getMarkerForQuake(Earthquake q) {
        MarkerOptions m = new MarkerOptions()
                .position(getLatLngForQuake(q))
                .icon(BitmapDescriptorFactory.defaultMarker(getHueForQuake(q)));
        return m;
    }

    private float getHueForQuake(Earthquake q) {
        // https://developers.google.com/maps/documentation/android/reference/com/google/android/gms/maps/model/BitmapDescriptorFactory
        // Red is 0
        // Orange is 30
        // Yellow is 60
        // Green is 120

        // TODO Apply an appropriate non-linear scale to these - 4.0 not much redder than a 3.0, currently.
        float mostSignificantQuake = 6;
        float percentage = (float) (q.getMagnitude() / mostSignificantQuake);
        float hue = 90 - (percentage * 90);

        if (hue < 0)
            hue = 0;
        else if (hue > 90)
            hue = 90;
        return hue;
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
        int defaultZoom = 5;
        View v = getView();
        if (v != null) {
            int width = v.getWidth();

            if (width < 480)
                defaultZoom = 5;
            else if (width < 720)
                defaultZoom = 6;
            else
                defaultZoom = 7;
        }
        return defaultZoom;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View v = getLayoutInflater(null).inflate(R.layout.quake_marker_view, null);
        final Earthquake quake = mMarkerIdToQuake.get(marker.getId());
        if(quake != null) {
            // TODO Improve this info window layout etc.
            TextView tv = (TextView)v.findViewById(R.id.quake_marker_magnitude);
            tv.setText(quake.getFormattedMagnitude());

            tv = (TextView)v.findViewById(R.id.quake_marker_depth);
            tv.setText(quake.getFormattedDepth());
        }
        return v;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Earthquake quake = mMarkerIdToQuake.get(marker.getId());
        if(quake != null) {
            Intent intent = new Intent(getActivity(), QuakeActivity.class);
            intent.putExtra(QuakeActivity.QUAKE_KEY, quake);
            getActivity().startActivity(intent);
        }
    }
}
