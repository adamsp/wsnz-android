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

package speakman.whatsshakingnz.utils;

import android.test.AndroidTestCase;

import com.google.android.gms.maps.model.LatLng;

import speakman.whatsshakingnz.model.LocalPlace;

/**
 * Created by Adam on 11/24/2015.
 */
public class DistanceUtilTests extends AndroidTestCase {

    public void testClosestPlaceIsCorrect() {
        LocalPlace closestPlace = DistanceUtil.getClosestPlace(new LatLng(-36.806, 174.814));
        assertEquals("Auckland", closestPlace.name);

        closestPlace = DistanceUtil.getClosestPlace(new LatLng(-35.778, 174.217));
        assertEquals("Whangarei", closestPlace.name);

        closestPlace = DistanceUtil.getClosestPlace(new LatLng(-43.584, 172.65));
        assertEquals("Christchurch", closestPlace.name);
    }

    public void testDirectionIsCorrect() {
        LatLng center = new LatLng(-37, 170);
        LatLng north = new LatLng(-36, 170);
        LatLng northEast = new LatLng(-36, 171);
        LatLng east = new LatLng(-37, 171);
        LatLng southEast = new LatLng(-38, 171);
        LatLng south = new LatLng(-38, 170);
        LatLng southWest = new LatLng(-38, 169);
        LatLng west = new LatLng(-37, 169);
        LatLng northWest = new LatLng(-36, 169);

        assertEquals(DistanceUtil.Direction.North, DistanceUtil.getDirection(center, north));
        assertEquals(DistanceUtil.Direction.NorthEast, DistanceUtil.getDirection(center, northEast));
        assertEquals(DistanceUtil.Direction.East, DistanceUtil.getDirection(center, east));
        assertEquals(DistanceUtil.Direction.SouthEast, DistanceUtil.getDirection(center, southEast));
        assertEquals(DistanceUtil.Direction.South, DistanceUtil.getDirection(center, south));
        assertEquals(DistanceUtil.Direction.SouthWest, DistanceUtil.getDirection(center, southWest));
        assertEquals(DistanceUtil.Direction.West, DistanceUtil.getDirection(center, west));
        assertEquals(DistanceUtil.Direction.NorthWest, DistanceUtil.getDirection(center, northWest));
    }

    public void testDirectionIsCorrectAroundAntimeridian() {
        LatLng center = new LatLng(-37, 179);
        LatLng east = new LatLng(-37, -179);

        assertEquals(DistanceUtil.Direction.East, DistanceUtil.getDirection(center, east));
    }
}
