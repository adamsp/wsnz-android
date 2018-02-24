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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.LocalPlace;
import speakman.whatsshakingnz.utils.DistanceUtil;

/**
 * Created by Adam on 12/26/2015.
 */
public class EarthquakeOverviewViewModel {

    private final Earthquake earthquake;
    private final LocalPlace nearestTown;
    private final double distanceToNearestTown;
    private final DistanceUtil.Direction directionToNearestTown;
    public EarthquakeOverviewViewModel(Earthquake earthquake) {
        this.earthquake = earthquake;
        LatLng earthquakeLocation = new LatLng(earthquake.getLatitude(), earthquake.getLongitude());
        this.nearestTown = DistanceUtil.getClosestPlace(earthquakeLocation);
        this.distanceToNearestTown = DistanceUtil.distanceBetweenPlaces(nearestTown.location, earthquakeLocation);
        this.directionToNearestTown = DistanceUtil.getDirection(nearestTown.location, earthquakeLocation);
    }

    public Earthquake getEarthquake() {
        return earthquake;
    }

    @SuppressLint("DefaultLocale")
    public String getMagnitude() {
        return String.format("%.1f", earthquake.getMagnitude());
    }

    public String getDistanceAndDirectionFromNearestTown(Resources resources) {
        return resources.getString(R.string.overview_distance_direction_format, distanceToNearestTown,
                directionToNearestTown.localizedName(resources));
    }

    public String getNearestTownName() {
        return nearestTown.name;
    }

    public String getTimePassedSinceOccurrence(Resources res) {
        long millisPassed = DateTime.now().getMillis() - earthquake.getOriginTime();
        if (millisPassed < 1000) {
            return res.getString(R.string.overview_time_passed_now);
        }
        long secondsPassed = millisPassed / 1000;
        if (secondsPassed < 60) {
            return res.getQuantityString(R.plurals.overview_time_passed_seconds, (int) secondsPassed, secondsPassed);
        }
        long minsPassed = secondsPassed / 60;
        if (minsPassed < 60) {
            return res.getQuantityString(R.plurals.overview_time_passed_minutes, (int) minsPassed, minsPassed);
        }
        long hoursPassed = minsPassed / 60;
        if (hoursPassed < 24) {
            return res.getQuantityString(R.plurals.overview_time_passed_hours, (int) hoursPassed, hoursPassed);
        }
        long daysPassed = hoursPassed / 24;
        return res.getQuantityString(R.plurals.overview_time_passed_days, (int) daysPassed, daysPassed);
    }
}

