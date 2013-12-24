package speakman.whatsshakingnz.formatting;

import java.util.Date;

import android.content.Context;
import android.text.format.DateFormat;

public class DateFormatting {

    /**
     * Returns a date time string formatted similar to 12:13 PM 28/12/2013.
     * Respects users device locale & 12/24 hour settings.
     */
    public static String getShortDateString(Context context, Date date) {
        if (context == null || date == null) return null;
        java.text.DateFormat tf = DateFormat.getTimeFormat(context);
        java.text.DateFormat df = DateFormat.getDateFormat(context);
        String dateString = String.format("%s, %s", df.format(date),
                tf.format(date));
        return dateString;
    }

    /**
     * Returns a date time string formatted similar to 12:13 PM Dec 28, 2013.
     * Respects users device locale & 12/24 hour settings.
     */
    public static String getMediumDateString(Context context, Date date) {
        if (context == null || date == null) return null;
        java.text.DateFormat tf = DateFormat.getTimeFormat(context);
        java.text.DateFormat df = DateFormat.getMediumDateFormat(context);
        String dateString = String.format("%s, %s", df.format(date),
                tf.format(date));
        return dateString;
    }

}
