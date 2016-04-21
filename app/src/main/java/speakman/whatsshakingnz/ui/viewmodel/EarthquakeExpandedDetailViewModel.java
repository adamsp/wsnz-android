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

package speakman.whatsshakingnz.ui.viewmodel;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import speakman.whatsshakingnz.model.Earthquake;

/**
 * Created by Adam on 2/29/2016.
 */
@SuppressWarnings("SameParameterValue")
public class EarthquakeExpandedDetailViewModel {

    private final Earthquake earthquake;

    public static final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    public EarthquakeExpandedDetailViewModel(Earthquake earthquake) {
        this.earthquake = earthquake;
    }

    public CharSequence getDetail() {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        this.appendDetail(sb, "ID", earthquake.getId(), true);
        long originTime = earthquake.getOriginTime();
        this.appendDetail(sb, "Origin Time", originTime == 0 ? null : new DateTime(originTime), true);
        long updatedTime = earthquake.getUpdatedTime();
        this.appendDetail(sb, "Updated Time", updatedTime == 0 ? null : new DateTime(updatedTime), true);
        this.appendDetail(sb, "Latitude", earthquake.getLatitude(), true);
        this.appendDetail(sb, "Longitude", earthquake.getLongitude(), true);
        this.appendDetail(sb, "Depth (kilometers)", earthquake.getDepth(), true);
        this.appendDetail(sb, "Magnitude", earthquake.getMagnitude(), true);
        this.appendDetail(sb, "Evaluation Method", earthquake.getEvaluationMethod(), true);
        this.appendDetail(sb, "Evaluation Status", earthquake.getEvaluationStatus(), true);
        this.appendDetail(sb, "Evaluation Mode", earthquake.getEvaluationMode(), true);
        this.appendDetail(sb, "Earth Model", earthquake.getEarthModel(), true);
        this.appendDetail(sb, "Depth Type", earthquake.getDepthType(), true);
        this.appendDetail(sb, "Origin Error", earthquake.getOriginError(), true);
        this.appendDetail(sb, "Used Phase Count", earthquake.getUsedPhaseCount(), true);
        this.appendDetail(sb, "Used Station Count", earthquake.getUsedStationCount(), true);
        this.appendDetail(sb, "Minimum Distance", earthquake.getMinimumDistance(), true);
        this.appendDetail(sb, "Azimuthal Gap", earthquake.getAzimuthalGap(), true);
        this.appendDetail(sb, "Magnitude Type", earthquake.getMagnitudeType(), true);
        this.appendDetail(sb, "Magnitude Uncertainty", earthquake.getMagnitudeUncertainty(), true);
        this.appendDetail(sb, "Magnitude Station Count", earthquake.getMagnitudeStationCount(), false);
        return sb;
    }

    private static final String detailSeparator = ": ";
    private void appendDetail(SpannableStringBuilder sb, String key, String value, boolean newLine) {
        if (value == null) {
            return;
        }
        appendKey(sb, key);
        sb.append(value);
        if (newLine) {
            sb.append("\n");
        }
    }

    private void appendDetail(SpannableStringBuilder sb, String key, DateTime value, boolean newLine) {
        if (value == null) {
            return;
        }
        appendKey(sb, key);
        sb.append(dateFormat.format(value.toDate()));
        if (newLine) {
            sb.append("\n");
        }
    }

    private void appendDetail(SpannableStringBuilder sb, String key, int value, boolean newLine) {
        appendKey(sb, key);
        sb.append(Integer.toString(value));
        if (newLine) {
            sb.append("\n");
        }
    }

    private void appendDetail(SpannableStringBuilder sb, String key, double value, boolean newLine) {
        appendKey(sb, key);
        sb.append(Double.toString(value));
        if (newLine) {
            sb.append("\n");
        }
    }

    private void appendKey(SpannableStringBuilder sb, String key) {
        int start = sb.length();
        int end = start + key.length() + detailSeparator.length();
        sb.append(key);
        sb.append(detailSeparator);
        sb.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }
}
