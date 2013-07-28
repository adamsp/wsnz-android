package speakman.whatsshakingnz.geonet;

import java.util.ArrayList;
import java.util.Locale;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.activities.MainActivity;
import speakman.whatsshakingnz.activities.PreferenceActivity;
import speakman.whatsshakingnz.earthquake.Earthquake;
import speakman.whatsshakingnz.earthquake.EarthquakeFilter;
import speakman.whatsshakingnz.preferences.DefaultPrefs;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.preference.PreferenceManager;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class GeonetService extends WakefulIntentService {
	public static final String KEY_PREFS_LAST_CHECKED_ID = "LastCheckedID";

	public GeonetService() {
		super("Geonet Service");
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		Log.d("WSNZ", "doWakefulWork called.");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int minMagnitude = prefs.getInt(
				PreferenceActivity.KEY_PREF_MIN_HIGHLIGHT_MAGNITUDE,
				DefaultPrefs.MIN_HIGHLIGHT_MAGNITUDE);
		Log.d("WSNZ", "Checking with Geonet for new quakes above magnitude "
				+ minMagnitude);
		// Get the last-notified-quake
		ArrayList<Earthquake> quakes = GeonetAccessor.getQuakes();
		if(null == quakes) {
			Log.d("WSNZ", "No internet connection.");
			return;
		}
		quakes = EarthquakeFilter.filterQuakes(quakes
				, ((float) minMagnitude) / 10.0f, 30);
		
		String lastChecked = prefs.getString(KEY_PREFS_LAST_CHECKED_ID,
				"0000p000000");
        boolean reviewedOnly = prefs.getBoolean(PreferenceActivity.KEY_PREF_BG_NOTIFICATIONS_REVIEWED_ONLY,
                false);
		ArrayList<Earthquake> newQuakes = getNewQuakes(lastChecked, quakes, reviewedOnly);
		if (newQuakes.size() > 0) {
			Log.d("WSNZ", newQuakes.size() + " new quakes.");
			notifyUser(newQuakes, prefs);
			Editor editor = prefs.edit();
			editor.putString(KEY_PREFS_LAST_CHECKED_ID, newQuakes.get(0)
					.getReference());
			editor.commit();
		} else {
			Log.d("WSNZ", "No new quakes.");
		}
	}

	private void notifyUser(ArrayList<Earthquake> quakes, SharedPreferences prefs) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
		int icon;
		if (Build.VERSION.SDK_INT < VERSION_CODES.GINGERBREAD) {
			icon = R.drawable.status_bar_icon_pre_2_3;
		} else if (Build.VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
			icon = R.drawable.status_bar_icon_2_3;
		} else {
			icon = R.drawable.status_bar_icon_post_2_3;
		}
		String tickerText, titleText, contentText;
		long when = System.currentTimeMillis();
		if (quakes.size() > 1) {
			tickerText = String.format(getString(R.string.notifications_tickerMultiQuake),
					quakes.size());
			titleText = String.format(getString(R.string.notifications_titleMultiQuake),
					quakes.size());
			contentText = "";
		} else {
			Earthquake latestQuake = quakes.get(0);
			tickerText = String.format(getString(R.string.notifications_tickerSingleQuake),
					latestQuake.getFormattedMagnitude());
			titleText = String.format(getString(R.string.notifications_titleSingleQuake),
					latestQuake.getFormattedMagnitude());
			contentText = latestQuake.getLocation();
		}

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		if(prefs.getBoolean(PreferenceActivity.KEY_PREF_BG_NOTIFICATIONS_SOUND, DefaultPrefs.BG_NOTIFICATIONS_SOUND))
			notification.defaults |= Notification.DEFAULT_SOUND;
		if(prefs.getBoolean(PreferenceActivity.KEY_PREF_BG_NOTIFICATIONS_VIBRATE, DefaultPrefs.BG_NOTIFICATIONS_VIBRATE))
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		if(prefs.getBoolean(PreferenceActivity.KEY_PREF_BG_NOTIFICATIONS_LIGHT, DefaultPrefs.BG_NOTIFICATIONS_LIGHT)) {
			notification.ledARGB = 0xffff0000;
			notification.ledOnMS = 300;
			notification.ledOffMS = 1000;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		}
		Context context = getApplicationContext();
		Intent notificationIntent = new Intent(context, MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, titleText, contentText,
				contentIntent);
		notificationManager.notify(0, notification);
	}

	private ArrayList<Earthquake> getNewQuakes(String lastChecked,
			ArrayList<Earthquake> quakes, boolean reviewedOnly) {
		ArrayList<Earthquake> newQuakes = new ArrayList<Earthquake>();
		if (quakes == null)
			return newQuakes;
		for (Earthquake quake : quakes) {
			if (isNewer(quake.getReference(), lastChecked)) {
                if(reviewedOnly) {
                    if (quake.getStatus().toLowerCase(Locale.ENGLISH).equals("reviewed")) {
                        newQuakes.add(quake);
                    }
                } else {
                    newQuakes.add(quake);
                }
            }
		}
		return newQuakes;
	}

	/**
	 * Returns TRUE if newQuake is more recent than oldQuake, where newQuake and
	 * oldQuake are the result of Earthquake.getReference().
	 * 
	 * @param newQuakeReference
	 * @param oldQuakeReference
	 * @return
	 */
	private boolean isNewer(String newQuakeReference, String oldQuakeReference) {
		int[] newQuakeVals = getIntArray(newQuakeReference.split("p"));
		int[] oldQuakeVals = getIntArray(oldQuakeReference.split("p"));
		// 2013p123456 vs 2012p123456 - the 2013 reference is newer.
		if (newQuakeVals[0] > oldQuakeVals[0])
			return true;
		// 2012p123456 vs 2012p111111
		else if (newQuakeVals[1] > oldQuakeVals[1])
			return true;
		return false;
	}

	private int[] getIntArray(String[] quakeReferenceSplits) {
		return new int[] { Integer.parseInt(quakeReferenceSplits[0]),
				Integer.parseInt(quakeReferenceSplits[1]) };
	}
}
