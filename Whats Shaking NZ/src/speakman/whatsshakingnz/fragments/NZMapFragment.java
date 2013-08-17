package speakman.whatsshakingnz.fragments;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.earthquake.Earthquake;
import speakman.whatsshakingnz.earthquake.EarthquakeTapListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NZMapFragment extends SupportMapFragment implements GoogleMap.OnMarkerClickListener {
    private ArrayList<Earthquake> mQuakes;
    private static final double NZ_CENTRE_LATITUDE = -41;
    private static final double NZ_CENTRE_LONGITUDE = 173;
    private HashMap<String, Earthquake> mMarkerIdToQuake;
    private HashMap<String, Marker> mQuakeReferenceToMarker;

    private CameraPosition mDefaultCameraPosition;

    private EarthquakeTapListener mListener;
    private HashMap<String, BitmapDescriptor> mMarkerImageContainer;

    /**
     * For some reason, calling setArguments in the empty constructor doesn't work to specify an initial camera
     * position, where as when we call it from the static methods it does. The empty constructor is the one called when
     * inflating from XML.
     *
     * For this reason, we always manually set the camera to the location specified in the getArguments() Bundle, or if
     * this does not exist then to the default location. We do this when we expect the Map to be ready - in the
     * onActivityCreated call.
     */

    public NZMapFragment() {
        mMarkerIdToQuake = new HashMap<String, Earthquake>();
        mQuakeReferenceToMarker = new HashMap<String, Marker>();
        mMarkerImageContainer = new HashMap<String, BitmapDescriptor>();
        mDefaultCameraPosition = new CameraPosition(new LatLng(NZ_CENTRE_LATITUDE, NZ_CENTRE_LONGITUDE), // target
                // TODO Zoom should probably change by device...
                5, // zoom
                0, // tilt
                0); // bearing
    }

    public static NZMapFragment newInstance() {
        return new NZMapFragment();
    }

    public static NZMapFragment newInstance(GoogleMapOptions options) {
        NZMapFragment fragment = new NZMapFragment();

        // See http://stackoverflow.com/a/13783463/1217087
        Bundle args = new Bundle();
        args.putParcelable("MapOptions", options); //obtained by decompiling google-play-services.jar
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setInitialCameraPosition(savedInstanceState);
        updateOverlayItems();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        GoogleMap map = getMap();
        CameraPosition position = map.getCameraPosition();
        outState.putParcelable("mapPosition", position);
    }

    private void setInitialCameraPosition(Bundle savedInstanceState) {
        GoogleMap map = getMap();
        if (map == null) return;

        CameraPosition camPosition = null;
        if (savedInstanceState != null) {
            camPosition = savedInstanceState.getParcelable("mapPosition");
        }

        if (camPosition == null) {
            Bundle args = getArguments();
            GoogleMapOptions googleMapOptions = null;

            if(args != null)
                googleMapOptions = (GoogleMapOptions)getArguments().get("MapOptions");

            if (googleMapOptions != null) {
                camPosition = googleMapOptions.getCamera();
            } else {
                camPosition = mDefaultCameraPosition;
            }
        }

        map.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
    }

    public void setQuake(Earthquake quake, boolean centerOnQuake) {
        ArrayList<Earthquake> quakes = new ArrayList<Earthquake>();
        quakes.add(quake);
        setQuakes(quakes);
        if (centerOnQuake) {
            mDefaultCameraPosition = new CameraPosition(quake.getLatLng(), // target
                    getDefaultZoomForDevice() + 2, // zoom
                    0, // tilt
                    0); // bearing
            setInitialCameraPosition(null);
        }
    }

    public void setQuakes(ArrayList<Earthquake> quakes) {
        mQuakes = quakes;
        updateOverlayItems();
    }

    private void updateOverlayItems() {
        GoogleMap map = getMap();
        if (map == null || mQuakes == null)
            return;

        map.setOnMarkerClickListener(this);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(mListener != null) mListener.onEarthquakeLostFocus(null);
            }
        });

        for (Earthquake q : mQuakes) {
            MarkerOptions markerOptions = getMarkerForQuake(q);
            // Specify a title so we get an info window when tapped.
            markerOptions.title(q.getFormattedMagnitude())
                    .snippet(q.getFormattedDepth());
            Marker marker = map.addMarker(markerOptions);
            mMarkerIdToQuake.put(marker.getId(), q);
            mQuakeReferenceToMarker.put(q.getReference(), marker);
        }

    }

    private MarkerOptions getMarkerForQuake(Earthquake q) {
        MarkerOptions m = new MarkerOptions()
                .position(q.getLatLng())
                .icon(getIconForQuake(q));
        return m;
    }

    private BitmapDescriptor getIconForQuake(Earthquake q) {
        if(mMarkerImageContainer.containsKey(q.getFormattedMagnitude())) {
            return mMarkerImageContainer.get(q.getFormattedMagnitude());
        }
        // Set up the colour for our marker image
        Drawable marker = getResources().getDrawable(R.drawable.mapmarker);
        int markerWidth = marker.getIntrinsicWidth();
        int markerHeight = marker.getIntrinsicHeight();
        int markerColor = Color.HSVToColor(0xC8, new float[]{q.getHue(), 1, 1});
        PorterDuffColorFilter filter = new PorterDuffColorFilter(markerColor, PorterDuff.Mode.MULTIPLY);
        Paint markerPaint = new Paint();
        markerPaint.setColorFilter(filter);

        // Set up the text paint & location (bounds) for painting onto our marker
        TextPaint textPaint = new TextPaint();
        Rect textBounds = new Rect();
        float fontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
        float textMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        textPaint.setTextSize(fontSize);
        textPaint.getTextBounds(q.getFormattedMagnitude(), 0, q.getFormattedMagnitude().length(), textBounds);
        textBounds.inset((int) -textMargin, (int) -textMargin);
        textBounds.offsetTo(markerWidth / 2 - textBounds.width() / 2, // text will be centered horizontally
                markerHeight - markerHeight / 2 - textBounds.height()); // this aligns it in top 1/3 of our marker
        textPaint.setARGB(255, 0, 0, 0);

        // Now lets paint our marker image & text
        Bitmap bmp = Bitmap.createBitmap(markerWidth, markerHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mapmarker), 0, 0, markerPaint);
        canvas.drawText(q.getFormattedMagnitude(), textBounds.left,
                textBounds.bottom - textMargin, textPaint);

        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bmp);
        // Cache our marker image so we don't have to create new ones that are the same.
        mMarkerImageContainer.put(q.getFormattedMagnitude(), icon);
        return icon;
    }

    /**
     * Returns the default zoom level for the device size. Different screen
     * sizes require different default zoom levels.
     *
     * @return The default zoom level to show the whole NZ map, for the current
     * device.
     */
    private int getDefaultZoomForDevice() {
        // TODO Broken with new maps.
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

    public void highlightQuake(Earthquake q) {
        String quakeRef = q.getReference();
        for(Map.Entry<String, Marker> entry : mQuakeReferenceToMarker.entrySet()) {
            Marker marker = entry.getValue();
            if(entry.getKey().equals(quakeRef)) {
                // Highlight marker.
                //marker.setIcon(bigIcon...);
            } else {
                //marker.setIcon(grayIcon...);
            }
        }
    }

    public void clearHighlight() {
        for(Marker marker : mQuakeReferenceToMarker.values()) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(mMarkerIdToQuake.get(marker.getId()).getHue()));
        }
    }

    public void setOnEarthquakeTapListener(EarthquakeTapListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mQuakes.size() > 1) {
            Earthquake quake = mMarkerIdToQuake.get(marker.getId());
            if(quake != null && mListener != null) {
                mListener.onEarthquakeTap(quake);
                return true;
            }
        }
        return false;
    }
}
