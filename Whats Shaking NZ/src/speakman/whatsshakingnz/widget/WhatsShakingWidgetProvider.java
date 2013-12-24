package speakman.whatsshakingnz.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

public class WhatsShakingWidgetProvider extends AppWidgetProvider {
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        updateWidgets(context);
    }
    
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        updateWidgets(context);
    }
    
    private void updateWidgets(Context context) {
        new WidgetUpdater(context).updateWidgets();
    }
}
