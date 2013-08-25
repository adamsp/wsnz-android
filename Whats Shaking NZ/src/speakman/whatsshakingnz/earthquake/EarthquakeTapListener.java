package speakman.whatsshakingnz.earthquake;

/**
 * Created by Adam on 10/08/13.
 */
public interface EarthquakeTapListener {
    public void onEarthquakeTap(Earthquake quake);
    public void onEarthquakeLostFocus(Earthquake quake);
}
