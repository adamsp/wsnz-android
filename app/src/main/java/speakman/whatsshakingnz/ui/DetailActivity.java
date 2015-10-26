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

package speakman.whatsshakingnz.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.WhatsShakingApplication;
import speakman.whatsshakingnz.databinding.ActivityDetailBinding;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.EarthquakeStore;

public class DetailActivity extends AppCompatActivity {

    public static String EXTRA_EARTHQUAKE = "speakman.whatsshakingnz.ui.DetailActivity.EXTRA_EARTHQUAKE";
    public static Intent createIntent(Context ctx, Earthquake earthquake) {
        Intent intent = new Intent(ctx, DetailActivity.class);
        intent.putExtra(EXTRA_EARTHQUAKE, earthquake.getId());
        return intent;
    }

    @Inject
    EarthquakeStore store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WhatsShakingApplication.getInstance().inject(this);
        ActivityDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Earthquake earthquake = getEarthquake();
        binding.setEarthquake(earthquake);
    }

    private Earthquake getEarthquake() {
        Earthquake earthquake = store.getEarthquake(getIntent().getStringExtra(EXTRA_EARTHQUAKE));
        return earthquake;
    }

}
