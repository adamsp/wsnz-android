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

package speakman.whatsshakingnz.ui.viewmodel;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import speakman.whatsshakingnz.databinding.RowEarthquakeBinding;
import speakman.whatsshakingnz.model.Earthquake;

/**
 * Created by Adam on 11/29/2015.
 */
@SuppressWarnings("unused")
public class EarthquakeListViewModel extends EarthquakeOverviewViewModel {

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public interface OnClickListener {
            void onClick(View v, Earthquake earthquake);
        }

        public RowEarthquakeBinding binding;
        private OnClickListener clickListener;
        public ViewHolder(RowEarthquakeBinding binding, OnClickListener clickListener) {
            super(binding.getRoot());
            this.binding = binding;
            this.clickListener = clickListener;
            this.itemView.setOnClickListener(this);
        }

        public void onClick(View v) {
            this.clickListener.onClick(v, this.binding.getEarthquakeModel().getEarthquake());
        }
    }

    public EarthquakeListViewModel(Earthquake earthquake) {
        super(earthquake);
    }
}
