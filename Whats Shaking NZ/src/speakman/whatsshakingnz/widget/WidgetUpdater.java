package speakman.whatsshakingnz.widget;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.activities.PreferenceActivity;
import speakman.whatsshakingnz.geonet.GeonetService;
import speakman.whatsshakingnz.preferences.DefaultPrefs;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class WidgetUpdater {
    
    private Context mContext;

    public WidgetUpdater(Context context) {
        if (context == null) throw new IllegalArgumentException("Context cannot be null.");
        mContext = context;
    }
    
    public boolean widgetsExist() {
        return getAppWidgetIds().length > 0;
    }
    
    public void updateWidgets() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean backgroundUpdatesEnabled = prefs.getBoolean(PreferenceActivity.KEY_PREF_ALLOW_BG_NOTIFICATIONS,
                DefaultPrefs.BG_NOTIFICATIONS_ENABLED);

        if (!backgroundUpdatesEnabled) {
            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_error);
            
            // Update click to take to preferences
            Intent intent = new Intent(mContext, PreferenceActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_error_parent_container, pendingIntent);
            
            updateWidgets(views);
        } else {
            // Let's get some data for the user! Service will call back into updateWidgets(RemoteViews views).
            WakefulIntentService.sendWakefulWork(mContext, GeonetService.class);
        }
    }
    
    public void updateWidgets(RemoteViews views) {
        getAppWidgetManager().updateAppWidget(getAppWidgetIds(), views);
    }
    
    private AppWidgetManager getAppWidgetManager() {
        return AppWidgetManager.getInstance(mContext);
    }
    
    private int[] getAppWidgetIds() {
        int[] appWidgetIds = getAppWidgetManager().getAppWidgetIds(new ComponentName(mContext, WhatsShakingWidgetProvider.class));
        return appWidgetIds;
    }

}
