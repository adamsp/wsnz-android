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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.databinding.RowEarthquakeBinding;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.EarthquakeStore;
import speakman.whatsshakingnz.ui.viewmodel.EarthquakeListViewModel;

/**
 * Created by Adam on 15-10-12.
 */
public class EarthquakeListAdapter extends RecyclerView.Adapter<EarthquakeListViewModel.ViewHolder> implements EarthquakeListViewModel.ViewHolder.OnClickListener {

    EarthquakeStore store;

    public EarthquakeListAdapter(EarthquakeStore store) {
        this.store = store;
    }

    @Override
    public EarthquakeListViewModel.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowEarthquakeBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_earthquake, parent, false);
        return new EarthquakeListViewModel.ViewHolder(binding, this);
    }

    @Override
    public void onBindViewHolder(EarthquakeListViewModel.ViewHolder holder, int position) {
        Earthquake earthquake = store.getEarthquakes().get(position);
        EarthquakeListViewModel viewModel = new EarthquakeListViewModel(earthquake);
        holder.binding.setEarthquakeModel(viewModel);
    }

    @Override
    public int getItemCount() {
        return store.getEarthquakes().size();
    }

    @Override
    public void onClick(View v, Earthquake earthquake) {
        Context ctx = v.getContext();
        ActivityOptionsCompat options = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && ctx instanceof Activity) {
            options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation((Activity) ctx, v, v.getTransitionName());
        }
        Intent intent = DetailActivity.createIntent(ctx, earthquake);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && options != null) {
            ctx.startActivity(intent, options.toBundle());
        } else {
            ctx.startActivity(intent);
        }
    }
}
