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

package speakman.whatsshakingnz.ui.views;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.joda.time.DateTime;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.databinding.ExpandableDetailCardBinding;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.ui.viewmodel.EarthquakeOverviewViewModel;

/**
 * Created by Adam on 2/20/2016.
 */
public class ExpandableDetailCard extends CardView implements View.OnClickListener {

    private View expandIndicator;
    private TextView detailText;
    private ExpandableDetailCardBinding binding;
    private boolean expanded;

    public ExpandableDetailCard(Context context) {
        super(context);
        init(context);
    }

    public ExpandableDetailCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ExpandableDetailCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void bindEarthquake(Earthquake earthquake) {
        binding.setEarthquakeModel(new EarthquakeOverviewViewModel(earthquake));
        StringBuilder sb = new StringBuilder();
        this.appendDetail(sb, "ID", earthquake.getId(), true);
        this.appendDetail(sb, "Origin Time", new DateTime(earthquake.getOriginTime()), true);
        this.appendDetail(sb, "Updated Time", new DateTime(earthquake.getUpdatedTime()), true);
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
        detailText.setText(sb.toString());
    }

    private void init(Context ctx) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(ctx), R.layout.expandable_detail_card, this, true);
        detailText = (TextView) findViewById(R.id.expandable_detail_text);
        expandIndicator = findViewById(R.id.expandable_detail_indicator);
        findViewById(R.id.expandable_detail_container).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (expanded) {
            collapse();
        } else {
            expand();
        }
    }

    public boolean onBackPressed() {
        if (expanded) {
            collapse();
            return true;
        }
        return false;
    }

    private void expand() {
        expanded = true;
        expandIndicator.setVisibility(View.GONE);
        detailText.setVisibility(View.VISIBLE);
    }

    private void collapse() {
        expandIndicator.setVisibility(View.VISIBLE);
        detailText.setVisibility(View.GONE);
        expanded = false;
    }

    private void appendDetail(StringBuilder sb, String key, String value, boolean newLine) {
        if (value == null) {
            return;
        }
        sb.append(key);
        sb.append(": ");
        sb.append(value);
        if (newLine) {
            sb.append("\n");
        }
    }

    private void appendDetail(StringBuilder sb, String key, DateTime value, boolean newLine) {
        if (value == null) {
            return;
        }
        sb.append(key);
        sb.append(": ");
        sb.append(value);
        if (newLine) {
            sb.append("\n");
        }
    }

    private void appendDetail(StringBuilder sb, String key, int value, boolean newLine) {
        sb.append(key);
        sb.append(": ");
        sb.append(value);
        if (newLine) {
            sb.append("\n");
        }
    }

    private void appendDetail(StringBuilder sb, String key, double value, boolean newLine) {
        sb.append(key);
        sb.append(": ");
        sb.append(value);
        if (newLine) {
            sb.append("\n");
        }
    }

}
