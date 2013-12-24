package speakman.whatsshakingnz.activities;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.earthquake.Earthquake;
import speakman.whatsshakingnz.geonet.GeonetAlarmListener;
import speakman.whatsshakingnz.preferences.DefaultPrefs;
import speakman.whatsshakingnz.widget.WidgetUpdater;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.wakeful.WakefulIntentService;

public class PreferenceActivity extends SherlockPreferenceActivity implements
        OnSharedPreferenceChangeListener {
    public static final String KEY_PREF_MIN_DISPLAY_MAGNITUDE = "pref_minDisplayMagnitude";
    public static final String KEY_PREF_MIN_HIGHLIGHT_MAGNITUDE = "pref_minHighlightMagnitude";
    public static final String KEY_PREF_NUM_QUAKES_TO_SHOW = "pref_numQuakesToShow";
    public static final String KEY_PREF_BG_NOTIFICATIONS_FREQ = "pref_backgroundNotificationsFrequency";
    public static final String KEY_PREF_ALLOW_BG_NOTIFICATIONS = "pref_allowBackgroundNotifications";
    public static final String KEY_PREF_BG_NOTIFICATIONS_SOUND = "pref_backgroundNotificationsSound";
    public static final String KEY_PREF_BG_NOTIFICATIONS_VIBRATE = "pref_backgroundNotificationsVibrate";
    public static final String KEY_PREF_BG_NOTIFICATIONS_LIGHT = "pref_backgroundNotificationsLight";
    public static final String KEY_PREF_BG_NOTIFICATIONS_REVIEWED_ONLY = "pref_backgroundNotificationsReviewed";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(KEY_PREF_MIN_DISPLAY_MAGNITUDE)) {
            int value = sharedPreferences.getInt(key,
                    DefaultPrefs.MIN_DISPLAY_MAGNITUDE);
            setMinDisplaySummary(value);
        } else if (key.equals(KEY_PREF_MIN_HIGHLIGHT_MAGNITUDE)) {
            int value = sharedPreferences.getInt(key,
                    DefaultPrefs.MIN_HIGHLIGHT_MAGNITUDE);
            setMinHighlightSummary(value);
        } else if (key.equals(KEY_PREF_NUM_QUAKES_TO_SHOW)) {
            int value = sharedPreferences.getInt(key,
                    DefaultPrefs.NUM_QUAKES_TO_DISPLAY);
            setNumQuakesSummary(value);
        } else if (key.equals(KEY_PREF_ALLOW_BG_NOTIFICATIONS)) {
            boolean value = sharedPreferences.getBoolean(key,
                    DefaultPrefs.BG_NOTIFICATIONS_ENABLED);
            if (value) {
                startGeonetAlarm();
                Toast.makeText(
                        this,
                        getString(R.string.pref_backgroundNotificationsEnabled),
                        Toast.LENGTH_SHORT).show();
            } else {
                stopGeonetAlarm();
                Toast.makeText(
                        this,
                        getString(R.string.pref_backgroundNotificationsDisabled),
                        Toast.LENGTH_SHORT).show();
            }
            new WidgetUpdater(this).updateWidgets();
        } else if (key.equals(KEY_PREF_BG_NOTIFICATIONS_FREQ)) {
            String value = sharedPreferences.getString(key,
                    DefaultPrefs.BG_NOTIFICATIONS_FREQ_STRING);
            int freq = Integer.parseInt(value);
            setNotificationsFreqSummary(freq);
            /**
             * We don't have to check if background checks are enabled because
             * if they weren't, we couldn't set a frequency.
             */
            startGeonetAlarm();
        }
    }

    private void setMinDisplaySummary(int value) {
        Preference pref = findPreference(KEY_PREF_MIN_DISPLAY_MAGNITUDE);
        pref.setSummary(String.format(
                getString(R.string.pref_minDisplayMagnitude_summ),
                Earthquake.magnitudeFormat.format((float) value / 10.0f)));
    }

    private void setMinHighlightSummary(int value) {
        Preference pref = findPreference(KEY_PREF_MIN_HIGHLIGHT_MAGNITUDE);
        pref.setSummary(String.format(
                getString(R.string.pref_minHighlightMagnitude_summ),
                Earthquake.magnitudeFormat.format((float) value / 10.0f)));
    }

    private void setNumQuakesSummary(int value) {
        Preference pref = findPreference(KEY_PREF_NUM_QUAKES_TO_SHOW);
        pref.setSummary(String.format(
                getString(R.string.pref_maxNumQuakesToShow_summ), value));
    }

    private void setNotificationsFreqSummary(int freq) {
        Preference pref = findPreference(KEY_PREF_BG_NOTIFICATIONS_FREQ);
        String summary;
        if (freq > 60)
            summary = String
                    .format(getString(R.string.pref_backgroundNotificationsFrequency_summ_hours),
                            (freq / 60));
        else
            summary = String
                    .format(getString(R.string.pref_backgroundNotificationsFrequency_summ_mins),
                            freq);
        pref.setSummary(summary);
    }

    private void startGeonetAlarm() {
        stopGeonetAlarm();
        WakefulIntentService.scheduleAlarms(new GeonetAlarmListener(),
                getApplicationContext());
    }

    private void stopGeonetAlarm() {
        WakefulIntentService.cancelAlarms(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(this);
        setMinDisplaySummary(prefs.getInt(KEY_PREF_MIN_DISPLAY_MAGNITUDE,
                DefaultPrefs.MIN_DISPLAY_MAGNITUDE));
        setMinHighlightSummary(prefs.getInt(
                KEY_PREF_MIN_HIGHLIGHT_MAGNITUDE,
                DefaultPrefs.MIN_HIGHLIGHT_MAGNITUDE));
        setNumQuakesSummary(prefs.getInt(KEY_PREF_NUM_QUAKES_TO_SHOW,
                DefaultPrefs.NUM_QUAKES_TO_DISPLAY));
        setNotificationsFreqSummary(Integer.parseInt(prefs.getString(
                KEY_PREF_BG_NOTIFICATIONS_FREQ,
                DefaultPrefs.BG_NOTIFICATIONS_FREQ_STRING)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
