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

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.model.LocalPlace;

/**
 * Created by Adam on 11/24/2015.
 */
public class DistanceUtil {

    public enum Direction {
        North(R.string.direction_north),
        NorthEast(R.string.direction_north_east),
        East(R.string.direction_east),
        SouthEast(R.string.direction_south_east),
        South(R.string.direction_south),
        SouthWest(R.string.direction_south_west),
        West(R.string.direction_west),
        NorthWest(R.string.direction_north_west);

        private final int directionName;
        Direction(@StringRes int localisedName) {
            directionName = localisedName;
        }

        public String localizedName(@NonNull Context ctx) {
            return ctx.getString(directionName);
        }
    }

    // For calculating distance
    private static final double PIx = 3.141592653589793;
    private static final double RADIO = 6378.16; // Radius of the earth, in km

    /**
     * Default places - North to South.
     * For the choice of places, I used a map of NZ on my wall
     * and simply selected all the places that were in a large font.
     * For the latitude & longitude of the places, I used Google Maps.
     */
    private static final List<LocalPlace> places;
    private static final LocalPlace Whangarei = new LocalPlace("Whangarei", new LatLng(-35.725156, 174.323735));
    private static final LocalPlace Auckland = new LocalPlace("Auckland", new LatLng(-36.848457, 174.763351));
    private static final LocalPlace Tauranga = new LocalPlace("Tauranga", new LatLng(-37.687798, 176.165149));
    private static final LocalPlace Hamilton = new LocalPlace("Hamilton", new LatLng(-37.787009, 175.279268));
    private static final LocalPlace Whakatane = new LocalPlace("Whakatane", new LatLng(-37.953419, 176.990813));
    private static final LocalPlace Rotorua = new LocalPlace("Rotorua", new LatLng(-38.136875, 176.249759));
    private static final LocalPlace Gisborne = new LocalPlace("Gisborne", new LatLng(-38.662354, 178.017648));
    private static final LocalPlace Taupo = new LocalPlace("Taupo", new LatLng(-38.685686, 176.070214));
    private static final LocalPlace NewPlymouth = new LocalPlace("New Plymouth", new LatLng(-39.055622, 174.075247));
    private static final LocalPlace Napier = new LocalPlace("Napier", new LatLng(-39.492839, 176.912026));
    private static final LocalPlace Hastings = new LocalPlace("Hastings", new LatLng(-39.639558, 176.839247));
    private static final LocalPlace Wanganui = new LocalPlace("Wanganui", new LatLng(-39.930093, 175.047932));
    private static final LocalPlace PalmerstonNorth = new LocalPlace("Palmerston North", new LatLng(-40.352309, 175.608204));
    private static final LocalPlace Levin = new LocalPlace("Levin", new LatLng(-40.622243, 175.286181));
    private static final LocalPlace Masterton = new LocalPlace("Masterton", new LatLng(-40.951114, 175.657356));
    private static final LocalPlace UpperHutt = new LocalPlace("Upper Hutt", new LatLng(-41.124415, 175.070785));
    private static final LocalPlace Porirua = new LocalPlace("Porirua", new LatLng(-41.133935, 174.840628));
    private static final LocalPlace LowerHutt = new LocalPlace("Lower Hutt", new LatLng(-41.209163, 174.90805));
    private static final LocalPlace Wellington = new LocalPlace("Wellington", new LatLng(-41.28647, 174.776231));
    private static final LocalPlace Nelson = new LocalPlace("Nelson", new LatLng(-41.270632, 173.284049));
    private static final LocalPlace Blenheim = new LocalPlace("Blenheim", new LatLng(-41.513444, 173.961261));
    private static final LocalPlace Greymouth = new LocalPlace("Greymouth", new LatLng(-42.450398, 171.210765));
    private static final LocalPlace Christchurch = new LocalPlace("Christchurch", new LatLng(-43.532041, 172.636268));
    private static final LocalPlace Timaru = new LocalPlace("Timaru", new LatLng(-44.396999, 171.255005));
    private static final LocalPlace Queenstown = new LocalPlace("Queenstown", new LatLng(-45.031176, 168.662643));
    private static final LocalPlace Dunedin = new LocalPlace("Dunedin", new LatLng(-45.878764, 170.502812));
    private static final LocalPlace Invercargill = new LocalPlace("Invercargill", new LatLng(-46.413177, 168.35376));

    static {
        places = new ArrayList<>();
        places.add(Whangarei);
        places.add(Auckland);
        places.add(Tauranga);
        places.add(Hamilton);
        places.add(Whakatane);
        places.add(Rotorua);
        places.add(Gisborne);
        places.add(Taupo);
        places.add(NewPlymouth);
        places.add(Napier);
        places.add(Hastings);
        places.add(Wanganui);
        places.add(PalmerstonNorth);
        places.add(Levin);
        places.add(Masterton);
        places.add(UpperHutt);
        places.add(Porirua);
        places.add(LowerHutt);
        places.add(Wellington);
        places.add(Nelson);
        places.add(Blenheim);
        places.add(Greymouth);
        places.add(Christchurch);
        places.add(Timaru);
        places.add(Queenstown);
        places.add(Dunedin);
        places.add(Invercargill);
    }

    private static double radians(double x) {
        return x * PIx / 180;
    }

    /**
     * Returns distance in kilometers between point1 and point2.
     * As seen here: http://stackoverflow.com/questions/27928/how-do-i-calculate-distance-between-two-latitude-longitude-points
     * @return The distance from the first point to the second.
     */
    public static double distanceBetweenPlaces(LatLng point1, LatLng point2) {
        double lon1 = point1.longitude;//getLongitudeE6() / 1E6;
        double lat1 = point1.latitude;//getLatitudeE6() / 1E6;
        double lon2 = point2.longitude;//getLongitudeE6() / 1E6;
        double lat2 = point2.latitude;//getLatitudeE6() / 1E6;
        double dlon = radians(lon2 - lon1);
        double dlat = radians(lat2 - lat1);

        double a = (Math.sin(dlat / 2) * Math.sin(dlat / 2))
                + Math.cos(radians(lat1)) * Math.cos(radians(lat2))
                * (Math.sin(dlon / 2) * Math.sin(dlon / 2));
        double angle = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return angle * RADIO;
    }

    /**
     * Calculates the closest town in New Zealand (in the form of a {@link LocalPlace})
     * to the supplied point.
     * @param quakeEpicenter The point you'd like a town close to.
     * @return The town closest to the supplied location.
     */
    public static LocalPlace getClosestPlace(LatLng quakeEpicenter) {
        // Find the distance from the closest town
        double closestTownDistance = -1;
        LocalPlace closestTown = null;
        for (LocalPlace place : places) {
            if (closestTownDistance < 0) {
                closestTownDistance = distanceBetweenPlaces(quakeEpicenter, place.location);
                closestTown = place;
            } else {
                double distance = distanceBetweenPlaces(quakeEpicenter, place.location);
                if (distance < closestTownDistance) {
                    closestTownDistance = distance;
                    closestTown = place;
                }
            }
        }
        return closestTown;
    }

    /**
     * Calculates the {@link speakman.whatsshakingnz.utils.DistanceUtil.Direction} from the first
     * arg to the second.
     * @param fromPoint The center point used to calculate bearing from.
     * @param toPoint The point whose bearing we'd like from the center.
     * @return The bearing from the first point to the second.
     */
    public static Direction getDirection(LatLng fromPoint, LatLng toPoint) {
        float[] results = new float[3];
        Location.distanceBetween(fromPoint.latitude, fromPoint.longitude,
                toPoint.latitude, toPoint.longitude,
                results);
        float bearing = results[2];

        // Split our compass into 8 pieces - 22.5 degrees either side of directly north (0 degrees)
        // is considered north, then 45 degree intervals for each other direction.
        if (bearing > 0) { // East!
            if (bearing < 22.5) {
                return Direction.North;
            } else if (bearing < 67.5) {
                return Direction.NorthEast;
            } else if (bearing < 112.5) {
                return Direction.East;
            } else if (bearing < 157.5) {
                return Direction.SouthEast;
            } else {
                return Direction.South;
            }
        } else { // West!
            if (bearing > -22.5) {
                return Direction.North;
            } else if (bearing > -67.5) {
                return Direction.NorthWest;
            } else if (bearing > -112.5) {
                return Direction.West;
            } else if (bearing > -157.5) {
                return Direction.SouthWest;
            } else {
                return Direction.South;
            }
        }
    }
}
