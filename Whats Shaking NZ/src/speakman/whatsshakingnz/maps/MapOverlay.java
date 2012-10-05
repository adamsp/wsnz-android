package speakman.whatsshakingnz.maps;

import java.util.ArrayList;
import java.util.Collection;

import speakman.whatsshakingnz.activities.QuakeActivity;
import speakman.whatsshakingnz.earthquake.Earthquake;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.TypedValue;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

public class MapOverlay extends ItemizedOverlay<Earthquake> {

	private final float FONT_SIZE;
	private final float TITLE_MARGIN;
	private int markerHeight;

	private ArrayList<Earthquake> mOverlays = new ArrayList<Earthquake>();
	private Context mContext;

	public MapOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		FONT_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, context.getResources().getDisplayMetrics());
		TITLE_MARGIN = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics());
		mContext = context;
		markerHeight = ((BitmapDrawable) defaultMarker).getBitmap().getHeight();
	}

	@Override
	protected boolean onTap(int index) {
		// If displaying quake details, we don't want to navigate forward to the
		// same quake, so just do nothing.
		if(mContext instanceof QuakeActivity)
			return true;
		Intent intent = new Intent(mContext, QuakeActivity.class);
		Earthquake quake = mOverlays.get(index);
		intent.putExtra(QuakeActivity.QUAKE_KEY, quake);
		mContext.startActivity(intent);
		return true;
	}

	public void addOverlay(Earthquake overlay) {
		mOverlays.add(overlay);
		populate();
	}

	public void addOverlays(Collection<Earthquake> overlays) {
		mOverlays.addAll(overlays);
		populate();
	}

	public void removeAllOverlays() {
		mOverlays.clear();
		populate();
	}

	@Override
	public void draw(android.graphics.Canvas canvas, MapView mapView,
			boolean shadow) {
		super.draw(canvas, mapView, shadow);

		// The following code draws heavily from this website:
		// http://binwaheed.blogspot.co.nz/2011/05/android-display-title-on-marker-in.html
		
		// go through all OverlayItems and draw title for each of them
		for (Earthquake item : mOverlays) {
			/*
			 * Converts latitude & longitude of this overlay item to coordinates
			 * on screen. As we have called boundCenterBottom() in constructor,
			 * so these coordinates will be of the bottom center position of the
			 * displayed marker.
			 */
			GeoPoint point = item.getPoint();
			Point markerBottomCenterCoords = new Point();
			mapView.getProjection().toPixels(point, markerBottomCenterCoords);

			/* Find the width and height of the title */
			TextPaint paintText = new TextPaint();

			Rect rect = new Rect();
			paintText.setTextSize(FONT_SIZE);
			paintText.getTextBounds(item.getFormattedMagnitude(), 0, item.getFormattedMagnitude()
					.length(), rect);

			rect.inset((int)-TITLE_MARGIN, (int)-TITLE_MARGIN);
			rect.offsetTo(markerBottomCenterCoords.x - rect.width() / 2,
					markerBottomCenterCoords.y - markerHeight / 2 - rect.height());

			paintText.setTextAlign(Paint.Align.CENTER);
			paintText.setTextSize(FONT_SIZE);
			paintText.setARGB(255, 255, 255, 255);

			canvas.drawText(item.getFormattedMagnitude(), rect.left + rect.width() / 2,
					rect.bottom - TITLE_MARGIN, paintText);
		}
	}

	@Override
	protected Earthquake createItem(int index) {
		return mOverlays.get(index);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

}
