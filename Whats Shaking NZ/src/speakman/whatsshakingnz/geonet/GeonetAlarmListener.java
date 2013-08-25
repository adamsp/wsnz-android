package speakman.whatsshakingnz.geonet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.preference.PreferenceManager;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.commonsware.cwac.wakeful.WakefulIntentService.AlarmListener;
import speakman.whatsshakingnz.activities.PreferenceActivity;
import speakman.whatsshakingnz.preferences.DefaultPrefs;

public class GeonetAlarmListener implements AlarmListener {

    public GeonetAlarmListener() {
    }

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
    }

    @Override
    public void sendWakefulWork(Context ctx) {
        WakefulIntentService.sendWakefulWork(ctx, GeonetService.class);
    }

}
