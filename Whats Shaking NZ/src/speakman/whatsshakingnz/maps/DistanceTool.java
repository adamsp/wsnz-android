package speakman.whatsshakingnz.maps;

import java.text.DecimalFormat;
import java.util.HashMap;

import com.google.android.maps.GeoPoint;


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
	private static HashMap<String, GeoPoint> locations;
	private static final GeoPoint Whangarei = new GeoPoint(-35725156,174323735);
	private static final GeoPoint Auckland = new GeoPoint(-36848457,174763351);
	private static final GeoPoint Tauranga = new GeoPoint(-37687798,176165149);
	private static final GeoPoint Hamilton = new GeoPoint(-37787009,175279268);
	private static final GeoPoint Whakatane = new GeoPoint(-37953419,176990813);
	private static final GeoPoint Rotorua = new GeoPoint(-38136875,176249759);
	private static final GeoPoint Gisborne = new GeoPoint(-38662354,178017648);
	private static final GeoPoint Taupo = new GeoPoint(-38685686,176070214);
	private static final GeoPoint NewPlymouth = new GeoPoint(-39055622,174075247);
	private static final GeoPoint Napier = new GeoPoint(-39492839,176912026);
	private static final GeoPoint Hastings = new GeoPoint(-39639558,176839247);
	private static final GeoPoint Wanganui = new GeoPoint(-39930093,175047932);
	private static final GeoPoint PalmerstonNorth = new GeoPoint(-40352309,175608204);
	private static final GeoPoint Levin = new GeoPoint(-40622243,175286181);
	private static final GeoPoint Masterton = new GeoPoint(-40951114,175657356);
	private static final GeoPoint UpperHutt = new GeoPoint(-41124415,175070785);
	private static final GeoPoint Porirua = new GeoPoint(-41133935,174840628);
	private static final GeoPoint LowerHutt = new GeoPoint(-41209163,17490805);
	private static final GeoPoint Wellington = new GeoPoint(-4128647,174776231);
	private static final GeoPoint Nelson = new GeoPoint(-41270632,173284049);
	private static final GeoPoint Blenheim = new GeoPoint(-41513444,173961261);
	private static final GeoPoint Greymouth = new GeoPoint(-42450398,171210765);
	private static final GeoPoint Christchurch = new GeoPoint(-43532041,172636268);
	private static final GeoPoint Timaru = new GeoPoint(-44396999,171255005);
	private static final GeoPoint Queenstown = new GeoPoint(-45031176,168662643);
	private static final GeoPoint Dunedin = new GeoPoint(-45878764,170502812);
	private static final GeoPoint Invercargill = new GeoPoint(-46413177,16835376);

	static {
		locations = new HashMap<String, GeoPoint>();
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
	public static double distanceBetweenPlaces(GeoPoint point1, GeoPoint point2) {
		double lon1 = point1.getLongitudeE6() / 1E6;
		double lat1 = point1.getLatitudeE6() / 1E6;
		double lon2 = point2.getLongitudeE6() / 1E6;
		double lat2 = point2.getLatitudeE6() / 1E6;
		double dlon = radians(lon2 - lon1);
		double dlat = radians(lat2 - lat1);

		double a = (Math.sin(dlat / 2) * Math.sin(dlat / 2))
				+ Math.cos(radians(lat1)) * Math.cos(radians(lat2))
				* (Math.sin(dlon / 2) * Math.sin(dlon / 2));
		double angle = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return angle * RADIO;
	}

	public static String getClosestTown(GeoPoint quakeEpicenter) {
		// Find the distance from the closest town
		double closestTownDistance = -1;
		String closestTownName = null;
		GeoPoint closestTown = null;
		for(String location : locations.keySet())
		{
			if(closestTownDistance < 0)
			{
				closestTownDistance = distanceBetweenPlaces(quakeEpicenter, locations.get(location));
				closestTownName = location;
				closestTown = locations.get(location);
			}
			else
			{
				double distance = distanceBetweenPlaces(quakeEpicenter, locations.get(location));
				if(distance < closestTownDistance)
				{
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

	private static String getDirectionFromTown(GeoPoint closestTown,
			GeoPoint quakeEpicenter) {
		double dLon = Math.abs(quakeEpicenter.getLongitudeE6() - closestTown.getLongitudeE6());
		double dLat = Math.abs(quakeEpicenter.getLatitudeE6() - closestTown.getLatitudeE6());
		double brng = Math.atan(dLat / dLon);
		
		String direction;
		String eastOrWest;
		String northOrSouth;
		// Quake is West of town
		if(quakeEpicenter.getLongitudeE6() < closestTown.getLongitudeE6())
			eastOrWest = "west";
		else // Quake is East of town
			eastOrWest = "east";
		
		// Quake is North of town
		if(quakeEpicenter.getLatitudeE6() > closestTown.getLatitudeE6()) 
			northOrSouth = "north";
		else // Quake is South of town
			northOrSouth = "south";
		
		if(brng < Math.PI / 8)
			direction = eastOrWest;
		else if (brng < 3 * Math.PI / 8)
			direction = northOrSouth + "-" + eastOrWest;
		else
			direction = northOrSouth;
		
		return direction;
	}
}
