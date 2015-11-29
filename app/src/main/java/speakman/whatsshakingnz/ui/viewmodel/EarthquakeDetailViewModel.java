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

import com.google.android.gms.maps.model.LatLng;

import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.LocalPlace;
import speakman.whatsshakingnz.utils.DistanceUtil;

/**
 * Created by Adam on 11/29/2015.
 */
@SuppressWarnings("unused")
public class EarthquakeDetailViewModel {

    private Earthquake earthquake;
    public EarthquakeDetailViewModel(Earthquake earthquake) {
        this.earthquake = earthquake;
    }

    public String getMagnitude() {
        return String.format("%.1f", earthquake.getMagnitude());
    }

    public String getLocation() {
        LatLng earthquakeLocation = new LatLng(earthquake.getLatitude(), earthquake.getLongitude());
        LocalPlace place = DistanceUtil.getClosestPlace(earthquakeLocation);
        double distance = DistanceUtil.distanceBetweenPlaces(place.location, earthquakeLocation);
        DistanceUtil.Direction direction = DistanceUtil.getDirection(place.location, earthquakeLocation);
        return String.format("%.0f km %s of %s", distance, direction.name(), place.name);
    }
}
