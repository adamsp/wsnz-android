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

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.analytics.Analytics;
import speakman.whatsshakingnz.databinding.ExpandableDetailCardBinding;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.ui.viewmodel.EarthquakeExpandedDetailViewModel;
import speakman.whatsshakingnz.ui.viewmodel.EarthquakeOverviewViewModel;

/**
 * Created by Adam on 2/20/2016.
 */
public class ExpandableDetailCard extends CardView implements View.OnClickListener {

    private View expandIndicator;
    private TextView detailText;
    private ExpandableDetailCardBinding binding;
    private boolean expanded;
    private Earthquake earthquake;

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

    private void init(Context ctx) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(ctx), R.layout.expandable_detail_card, this, true);
        detailText = (TextView) findViewById(R.id.expandable_detail_text);
        expandIndicator = findViewById(R.id.expandable_detail_indicator);
        findViewById(R.id.expandable_detail_container).setOnClickListener(this);
    }

    public void bindEarthquake(Earthquake earthquake) {
        this.earthquake = earthquake;
        binding.setEarthquakeModel(new EarthquakeOverviewViewModel(earthquake));
        binding.setEarthquakeDetail(new EarthquakeExpandedDetailViewModel(earthquake));
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
        Analytics.logDetailViewExpanded(earthquake);
    }

    private void collapse() {
        expandIndicator.setVisibility(View.VISIBLE);
        detailText.setVisibility(View.GONE);
        expanded = false;
    }
}
