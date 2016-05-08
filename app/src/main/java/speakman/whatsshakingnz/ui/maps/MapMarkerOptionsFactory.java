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

package speakman.whatsshakingnz.ui.maps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.TypedValue;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.model.Earthquake;

/**
 * Created by Adam on 1/3/2016.
 */
public class MapMarkerOptionsFactory {
    private final Context ctx;
    private final Map<String, BitmapDescriptor> iconCache;
    public MapMarkerOptionsFactory(Context ctx) {
        this.ctx = ctx;
        /*
        Have considered using an LRU Cache or similar for this however the effective upper bound on
        the number of entries for this is likely to be around 50 - we'll see at most ten magnitudes
        for every number, and earthquakes over magnitude 5 are rare, so 5x10 = 50.
         */
        this.iconCache = new HashMap<>();
    }

    public MarkerOptions getMarkerOptions(@NonNull Earthquake earthquake) {
        return new MarkerOptions()
                .icon(getIconForQuake(earthquake))
                .title(getMagnitudeForEarthquake(earthquake) + " magnitude")
                .position(new LatLng(earthquake.getLatitude(), earthquake.getLongitude()));
    }

    private BitmapDescriptor getIconForQuake(Earthquake earthquake) {
        String magnitude = getMagnitudeForEarthquake(earthquake);
        BitmapDescriptor cachedIcon = iconCache.get(magnitude);
        if (cachedIcon != null) {
            return cachedIcon;
        }
        Resources res = ctx.getResources();
        // Set up the colour for our marker image
        Drawable marker = ContextCompat.getDrawable(ctx, R.drawable.map_marker);
        int markerWidth = marker.getIntrinsicWidth();
        int markerHeight = marker.getIntrinsicHeight();

        // Set up the text paint & location (bounds) for painting onto our marker
        TextPaint textPaint = new TextPaint();
        Rect textBounds = new Rect();
        float fontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, res.getDisplayMetrics());
        textPaint.setTextSize(fontSize);
        textPaint.getTextBounds(magnitude, 0, magnitude.length(), textBounds);
        textBounds.offsetTo(markerWidth / 3 - textBounds.width() / 2, // text will be on left third of marker
                markerHeight - markerHeight / 2 - textBounds.height()); // this aligns it in top 1/3 of our marker
        textPaint.setARGB(255, 0, 0, 0);

        // Now we paint our marker image & text
        Bitmap bmp = Bitmap.createBitmap(markerWidth, markerHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.map_marker), 0, 0, null);
        canvas.drawText(magnitude, textBounds.left, textBounds.bottom, textPaint);

        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bmp);
        iconCache.put(magnitude, icon);
        return icon;
    }

    @SuppressLint("DefaultLocale")
    private String getMagnitudeForEarthquake(Earthquake earthquake) {
        return String.format("%.1f", earthquake.getMagnitude());
    }
}
