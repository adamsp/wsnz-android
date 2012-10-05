package speakman.whatsshakingnz.fragments;

import java.util.ArrayList;
import java.util.List;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.activities.PreferenceActivity;
import speakman.whatsshakingnz.activities.QuakeActivity;
import speakman.whatsshakingnz.earthquake.Earthquake;
import speakman.whatsshakingnz.maps.MapOverlay;
import speakman.whatsshakingnz.preferences.DefaultPrefs;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapFragment extends SherlockFragment {
	private MapView mView;
	private ArrayList<Earthquake> mQuakes;
	private Drawable regularDrawable, warningDrawable;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		regularDrawable = this.getResources().getDrawable(R.drawable.mapmarker);
		warningDrawable = this.getResources().getDrawable(
				R.drawable.mapmarker_warn);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mView == null)
			mView = (MapView) inflater.inflate(R.layout.map_view, null, false);
		return mView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// http://stackoverflow.com/questions/6526874/call-removeview-on-the-childs-parent-first
		((ViewGroup) mView.getParent()).removeView(mView);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mView.setBuiltInZoomControls(true);
		MapController mc = mView.getController();
		int latitude = (int) (-41 * 1E6);
		int longitude = (int) (173 * 1E6);
		GeoPoint center = new GeoPoint(latitude, longitude);
		mc.setCenter(center);
		mc.setZoom(getDefaultZoomForDevice());
		updateOverlayItems();
	}

	public void updateQuakes(ArrayList<Earthquake> quakes) {
		mQuakes = quakes;
		updateOverlayItems();
	}

	private void updateOverlayItems() {
		if (null == mView || null == mQuakes)
			return;
		// Move the map view if this is a QuakeActivity, will
		// only have one quake in this instance.
		if (getActivity() instanceof QuakeActivity) {
			MapController mc = mView.getController();
			mc.setCenter(mQuakes.get(0).getPoint());
			mc.setZoom(getDefaultZoomForDevice() + 2);
		}
		List<Overlay> mapOverlays = mView.getOverlays();
		mapOverlays.clear();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		float warnMagnitude = ((float) prefs.getInt(
				PreferenceActivity.KEY_PREF_MIN_HIGHLIGHT_MAGNITUDE,
				DefaultPrefs.MIN_HIGHLIGHT_MAGNITUDE)) / 10.0f;
		// Doing this this way (a new MapOverlay for every earthquake)
		// is very inefficient. However, it solves the problem of the
		// title text for all quakes having the same z-index (and
		// therefore overlapping each other).
		for (Earthquake q : mQuakes) {
			MapOverlay overlay;
			if (q.getRoundedMagnitude() >= warnMagnitude)
				overlay = new MapOverlay(warningDrawable, this.getActivity());
			else
				overlay = new MapOverlay(regularDrawable, this.getActivity());
			overlay.addOverlay(q);
			mapOverlays.add(overlay);
		}
		mView.invalidate();
	}

	/**
	 * Returns the default zoom level for the device size. Different screen
	 * sizes require different default zoom levels.
	 * 
	 * @return The default zoom level to show the whole NZ map, for the current
	 *         device.
	 */
	private int getDefaultZoomForDevice() {
		// TODO Different zoom level for different device screen size/pixels?
		return 6;
	}
}
