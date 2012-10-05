package speakman.whatsshakingnz.earthquake;

import java.util.ArrayList;
import java.util.Collection;

public class EarthquakeFilter {
	
	public static ArrayList<Earthquake> filterQuakes(Collection<Earthquake> quakes, float minMagnitude, int maxNumQuakes) {
		ArrayList<Earthquake> filtered = new ArrayList<Earthquake>();
		for(Earthquake q : quakes){
			if (q.getRoundedMagnitude() >= minMagnitude)
				filtered.add(q);
		}
		if(filtered.size() > maxNumQuakes)
			return new ArrayList<Earthquake>(filtered.subList(0, maxNumQuakes));
		else
			return filtered;
	}
}
