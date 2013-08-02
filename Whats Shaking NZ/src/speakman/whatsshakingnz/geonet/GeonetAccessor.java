package speakman.whatsshakingnz.geonet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import speakman.whatsshakingnz.earthquake.Earthquake;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class GeonetAccessor {
    private static final String url = "http://geonet.org.nz/quakes/services/felt.json";
    private static SimpleDateFormat format;

    static {
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Returns a list of Earthquakes, such that quakes.size() is less
     * than or equal to maxNumQuakes, and such that Earthquake.getRoundedMagnitude()
     * is greater than or equal to minimumMagnitude. <br>
     * The list is ordered such that the latest quake is first in
     * the list.<br>
     * Returns null if there is a problem with the internet connection or Geonet.
     *
     * @param maxNumQuakes
     * @param minimumMagnitude
     * @return
     */
    public static ArrayList<Earthquake> getQuakes() {
        InputStream source = retrieveStream(url);
        if (null == source)
            return null;
        ArrayList<Earthquake> quakes = new ArrayList<Earthquake>();
        String json = getJSONString(source);
        JSONObject o;
        JSONArray features;
        try {
            // Can sometimes receive incorrect JSON from Geonet, apparently.
            Object nextVal = new JSONTokener(json).nextValue();
            if (JSONObject.NULL.equals(nextVal)) return null;

            o = (JSONObject) nextVal;
            features = o.getJSONArray("features");
            for (int i = features.length() - 1; i >= 0; i--) {
                o = (JSONObject) features.get(i);
                Earthquake q = getQuakeFromJSON(o);
                quakes.add(q);
            }
        } catch (JSONException e) {
            Log.e("WSNZ", "Error parsing JSON", e);
            return null;
        } catch (ClassCastException e) {
            Log.e("WSNZ", "Unexpected data from Geonet", e);
            return null;
        }
        Collections.reverse(quakes);
        return quakes;
    }

    private static String getJSONString(InputStream source) {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(source));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null)
                sb.append(line);

            reader.close();
            source.close();
        } catch (IOException e) {
            Log.e("WSNZ", "Error reading JSON string", e);
        }

        return sb.toString();
    }

    private static Earthquake getQuakeFromJSON(JSONObject o)
            throws JSONException {
        JSONArray coords = o.getJSONObject("geometry").getJSONArray(
                "coordinates");
        double longitude = coords.getDouble(0);
        double latitude = coords.getDouble(1);
        JSONObject properties = o.getJSONObject("properties");
        double depth = properties.getDouble("depth");
        double magnitude = properties.getDouble("magnitude");
        String reference = properties.getString("publicid");
        String agency = properties.getString("agency");
        Date date = null;
        try {
            // See http://stackoverflow.com/questions/5636491/date-object-simpledateformat-not-parsing-timestamp-string-correctly-in-java-and
            // for explanation as to why this is necessary.
            // Geonet supplies dates like: 2012-08-25 05:47:05.133000
            String d = properties.getString("origintime").substring(0, 23);
            date = format.parse(d);
        } catch (ParseException e) {
            Log.e("WSNZ", "Error parsing date", e);
        }
        String status = properties.getString("status");

        return new Earthquake(magnitude, depth, new GeoPoint(
                (int) (latitude * 1E6), (int) (longitude * 1E6)), reference,
                date, agency, status);
    }

    private static InputStream retrieveStream(String url) {

        DefaultHttpClient client = new DefaultHttpClient();

        HttpGet getRequest = new HttpGet(url);

        try {
            HttpResponse getResponse = client.execute(getRequest);
            final int statusCode = getResponse.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                Log.w("WSNZ", "Error "
                        + statusCode + " for URL " + url);
                return null;
            }

            HttpEntity getResponseEntity = getResponse.getEntity();
            return getResponseEntity.getContent();

        } catch (IOException e) {
            getRequest.abort();
            Log.w("WSNZ", "Error for URL " + url + ". Returning null.",
                    e);
        }
        return null;
    }
}
