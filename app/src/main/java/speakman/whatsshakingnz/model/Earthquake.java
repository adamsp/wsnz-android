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

package speakman.whatsshakingnz.model;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import speakman.whatsshakingnz.databinding.RowEarthquakeBinding;

/**
 * Created by Adam on 15-05-31.
 */
@SuppressWarnings("unused")
public interface Earthquake {
    class ViewHolder extends RecyclerView.ViewHolder {

        public interface OnClickListener {
            public void onClick(View v, Earthquake earthquake);
        }

        public RowEarthquakeBinding binding;
        private OnClickListener clickListener;
        public ViewHolder(RowEarthquakeBinding binding, OnClickListener clickListener) {
            super(binding.getRoot());
            this.binding = binding;
            this.clickListener = clickListener;
        }

        public void onClick(View v) {
            this.clickListener.onClick(v, this.binding.getEarthquake());
        }
    }

    long getOriginTime();
    long getUpdatedTime();
    double getLatitude();
    double getLongitude();
    double getMagnitude();
    double getDepth();
    String getLocation();
    String getId();
}
