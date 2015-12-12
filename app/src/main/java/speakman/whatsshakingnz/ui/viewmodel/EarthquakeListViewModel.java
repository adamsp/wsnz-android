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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.databinding.RowEarthquakeBinding;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.LocalPlace;
import speakman.whatsshakingnz.utils.DistanceUtil;

/**
 * Created by Adam on 11/29/2015.
 */
@SuppressWarnings("unused")
public class EarthquakeListViewModel {

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

    private Earthquake earthquake;
    private LocalPlace nearestTown;
    private double distanceToNearestTown;
    private DistanceUtil.Direction directionToNearestTown;
    public EarthquakeListViewModel(Earthquake earthquake) {
        this.earthquake = earthquake;
        LatLng earthquakeLocation = new LatLng(earthquake.getLatitude(), earthquake.getLongitude());
        this.nearestTown = DistanceUtil.getClosestPlace(earthquakeLocation);
        this.distanceToNearestTown = DistanceUtil.distanceBetweenPlaces(nearestTown.location, earthquakeLocation);
        this.directionToNearestTown = DistanceUtil.getDirection(nearestTown.location, earthquakeLocation);
    }

    public Earthquake getEarthquake() {
        return earthquake;
    }

    public String getMagnitude() {
        return String.format("%.1f", earthquake.getMagnitude());
    }

    public String getDistanceAndDirectionFromNearestTown(Context context) {
        return context.getString(R.string.list_distance_direction_format, distanceToNearestTown,
                directionToNearestTown.localizedName(context));
    }

    public String getNearestTownName() {
        return nearestTown.name;
    }

    public String getTimePassedSinceOccurrence(Context context) {
        long millisPassed = DateTime.now().getMillis() - earthquake.getOriginTime();
        if (millisPassed < 1000) {
            return context.getString(R.string.list_time_passed_now);
        }
        long secondsPassed = millisPassed / 1000;
        if (secondsPassed < 60) {
            return String.format(context.getString(R.string.list_time_passed_seconds), secondsPassed);
        }
        long minsPassed = secondsPassed / 60;
        if (minsPassed < 60) {
            return String.format(context.getString(R.string.list_time_passed_minutes), minsPassed);
        }
        long hoursPassed = minsPassed / 60;
        if (hoursPassed < 24) {
            return String.format(context.getString(R.string.list_time_passed_hours), hoursPassed);
        }
        long daysPassed = hoursPassed / 24;
        return String.format(context.getString(R.string.list_time_passed_days), daysPassed);
    }
}
