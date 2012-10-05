package speakman.whatsshakingnz.geonet;

import speakman.whatsshakingnz.activities.PreferenceActivity;
import speakman.whatsshakingnz.preferences.DefaultPrefs;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.commonsware.cwac.wakeful.WakefulIntentService.AlarmListener;

public class GeonetAlarmListener implements AlarmListener {

	public GeonetAlarmListener() { }
	
	@Override
	public long getMaxAge() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void scheduleAlarms(AlarmManager mgr, PendingIntent pi,
			Context ctx) {
		int freqInMinutes = Integer.parseInt(PreferenceManager
				.getDefaultSharedPreferences(ctx)
				.getString(PreferenceActivity.KEY_PREF_BG_NOTIFICATIONS_FREQ,
						DefaultPrefs.BG_NOTIFICATIONS_FREQ_STRING));
		long freqInMilli = 60 * 1000 * freqInMinutes;
		mgr.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + freqInMilli,
                freqInMilli, pi);
		Log.d("WSNZ", "Alarm scheduled for every " + freqInMilli + " ms");		
	}

	@Override
	public void sendWakefulWork(Context ctx) {
		Log.d("WSNZ", "sendWakefulWork in GeonetAlarmListener called");
		WakefulIntentService.sendWakefulWork(ctx, GeonetService.class);		
	}

}
