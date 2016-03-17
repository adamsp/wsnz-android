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

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.databinding.RowEarthquakeBinding;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.realm.RealmEarthquake;
import speakman.whatsshakingnz.ui.viewmodel.EarthquakeListViewModel;

/**
 * Created by Adam on 15-10-12.
 */
public class EarthquakeListAdapter extends RecyclerView.Adapter<EarthquakeListViewModel.ViewHolder> {

    private List<RealmEarthquake> earthquakes;
    private EarthquakeListViewModel.ViewHolder.OnClickListener earthquakeClickListener;

    public EarthquakeListAdapter(@NonNull EarthquakeListViewModel.ViewHolder.OnClickListener clickListener) {
        this.earthquakeClickListener = clickListener;
    }

    @Override
    public EarthquakeListViewModel.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowEarthquakeBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_earthquake, parent, false);
        return new EarthquakeListViewModel.ViewHolder(binding, this.earthquakeClickListener);
    }

    @Override
    public void onBindViewHolder(EarthquakeListViewModel.ViewHolder holder, int position) {
        Earthquake earthquake = earthquakes.get(position);
        EarthquakeListViewModel viewModel = new EarthquakeListViewModel(earthquake);
        holder.binding.setEarthquakeModel(viewModel);
    }

    @Override
    public int getItemCount() {
        return earthquakes == null ? 0 : earthquakes.size();
    }

    public void updateList(List<RealmEarthquake> earthquakes) {
        this.earthquakes = earthquakes;
        notifyDataSetChanged();
    }
}
