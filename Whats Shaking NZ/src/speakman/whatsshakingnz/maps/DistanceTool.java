package speakman.whatsshakingnz.maps;

import java.text.DecimalFormat;
import java.util.HashMap;

import com.google.android.gms.maps.model.LatLng;

public class DistanceTool {
    // For calculating distance
    private static final double PIx = 3.141592653589793;
    private static final double RADIO = 6378.16; // Radius of the earth, in km

    // For formatting the result
    private static final DecimalFormat distanceFormat = new DecimalFormat("#");

    /**
     * Default locations - North to South.
     * For the choice of locations, I used a map of NZ on my wall
     * and simply selected all the locations that were in a large font.
     * For the latitude & longitude of the locations, I used Google Maps.
     */
    private static HashMap<String, LatLng> locations;
    private static final LatLng Whangarei = new LatLng(-35.725156, 174.323735);
    private static final LatLng Auckland = new LatLng(-36.848457, 174.763351);
    private static final LatLng Tauranga = new LatLng(-37.687798, 176.165149);
    private static final LatLng Hamilton = new LatLng(-37.787009, 175.279268);
    private static final LatLng Whakatane = new LatLng(-37.953419, 176.990813);
    private static final LatLng Rotorua = new LatLng(-38.136875, 176.249759);
    private static final LatLng Gisborne = new LatLng(-38.662354, 178.017648);
    private static final LatLng Taupo = new LatLng(-38.685686, 176.070214);
    private static final LatLng NewPlymouth = new LatLng(-39.055622, 174.075247);
    private static final LatLng Napier = new LatLng(-39.492839, 176.912026);
    private static final LatLng Hastings = new LatLng(-39.639558, 176.839247);
    private static final LatLng Wanganui = new LatLng(-39.930093, 175.047932);
    private static final LatLng PalmerstonNorth = new LatLng(-40.352309, 175.608204);
    private static final LatLng Levin = new LatLng(-40.622243, 175.286181);
    private static final LatLng Masterton = new LatLng(-40.951114, 175.657356);
    private static final LatLng UpperHutt = new LatLng(-41.124415, 175.070785);
    private static final LatLng Porirua = new LatLng(-41.133935, 174.840628);
    private static final LatLng LowerHutt = new LatLng(-41.209163, 174.90805);
    private static final LatLng Wellington = new LatLng(-41.28647, 174.776231);
    private static final LatLng Nelson = new LatLng(-41.270632, 173.284049);
    private static final LatLng Blenheim = new LatLng(-41.513444, 173.961261);
    private static final LatLng Greymouth = new LatLng(-42.450398, 171.210765);
    private static final LatLng Christchurch = new LatLng(-43.532041, 172.636268);
    private static final LatLng Timaru = new LatLng(-44.396999, 171.255005);
    private static final LatLng Queenstown = new LatLng(-45.031176, 168.662643);
    private static final LatLng Dunedin = new LatLng(-45.878764, 170.502812);
    private static final LatLng Invercargill = new LatLng(-46.413177, 168.35376);

    static {
        locations = new HashMap<String, LatLng>();
        locations.put("Whangarei", Whangarei);
        locations.put("Auckland", Auckland);
        locations.put("Tauranga", Tauranga);
        locations.put("Hamilton", Hamilton);
        locations.put("Whakatane", Whakatane);
        locations.put("Rotorua", Rotorua);
        locations.put("Gisborne", Gisborne);
        locations.put("Taupo", Taupo);
        locations.put("New Plymouth", NewPlymouth);
        locations.put("Napier", Napier);
        locations.put("Hastings", Hastings);
        locations.put("Wanganui", Wanganui);
        locations.put("Palmerston North", PalmerstonNorth);
        locations.put("Levin", Levin);
        locations.put("Masterton", Masterton);
        locations.put("Upper Hutt", UpperHutt);
        locations.put("Porirua", Porirua);
        locations.put("Lower Hutt", LowerHutt);
        locations.put("Wellington", Wellington);
        locations.put("Nelson", Nelson);
        locations.put("Blenheim", Blenheim);
        locations.put("Greymouth", Greymouth);
        locations.put("Christchurch", Christchurch);
        locations.put("Timaru", Timaru);
        locations.put("Queenstown", Queenstown);
        locations.put("Dunedin", Dunedin);
        locations.put("Invercargill", Invercargill);
    }

    private static double radians(double x) {
        return x * PIx / 180;
    }

    /**
     * Returns distance in kilometers between point1 and point2.
     * As seen here: http://stackoverflow.com/questions/27928/how-do-i-calculate-distance-between-two-latitude-longitude-points
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

    public static String getClosestTown(LatLng quakeEpicenter) {
        // Find the distance from the closest town
        double closestTownDistance = -1;
        String closestTownName = null;
        LatLng closestTown = null;
        for (String location : locations.keySet()) {
            if (closestTownDistance < 0) {
                closestTownDistance = distanceBetweenPlaces(quakeEpicenter, locations.get(location));
                closestTownName = location;
                closestTown = locations.get(location);
            } else {
                double distance = distanceBetweenPlaces(quakeEpicenter, locations.get(location));
                if (distance < closestTownDistance) {
                    closestTownDistance = distance;
                    closestTownName = location;
                    closestTown = locations.get(location);
                }
            }
        }

        // Find direction from the closest town
        String direction = getDirectionFromTown(closestTown, quakeEpicenter);

        // TODO Figure out how to localise this/extract it into strings.xml.
        // The problem is, we don't have access to getString() to fetch from strings.xml
        String location = "%1$s km %2$s of %3$s";
        return String.format(location, distanceFormat.format(closestTownDistance), direction, closestTownName);
    }

    private static String getDirectionFromTown(LatLng closestTown,
                                               LatLng quakeEpicenter) {
        double dLon = Math.abs(quakeEpicenter.longitude - closestTown.longitude);
        double dLat = Math.abs(quakeEpicenter.latitude - closestTown.latitude);
        double brng = Math.atan(dLat / dLon);

        String direction;
        String eastOrWest;
        String northOrSouth;
        // Quake is West of town
        if (quakeEpicenter.longitude < closestTown.longitude)
            eastOrWest = "west";
        else // Quake is East of town
            eastOrWest = "east";

        // Quake is North of town
        if (quakeEpicenter.latitude > closestTown.latitude)
            northOrSouth = "north";
        else // Quake is South of town
            northOrSouth = "south";

        if (brng < Math.PI / 8)
            direction = eastOrWest;
        else if (brng < 3 * Math.PI / 8)
            direction = northOrSouth + "-" + eastOrWest;
        else
            direction = northOrSouth;

        return direction;
    }
}
