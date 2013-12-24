package speakman.whatsshakingnz.earthquake;

import java.util.ArrayList;
import java.util.Date;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.activities.PreferenceActivity;
import speakman.whatsshakingnz.formatting.DateFormatting;
import speakman.whatsshakingnz.preferences.DefaultPrefs;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class EarthquakeArrayAdapter extends ArrayAdapter<Earthquake> {

    private ArrayList<Earthquake> mQuakes;
    private float mMinHighlight;
    private LayoutInflater mInflater;

    public EarthquakeArrayAdapter(Context context, int textViewResourceId,
                                  ArrayList<Earthquake> quakes) {
        super(context, textViewResourceId, quakes);
        this.mQuakes = quakes;

        // Setup preferences
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        int minHighlightPref = preferences.getInt(
                PreferenceActivity.KEY_PREF_MIN_HIGHLIGHT_MAGNITUDE,
                DefaultPrefs.MIN_HIGHLIGHT_MAGNITUDE);
        mMinHighlight = ((float) minHighlightPref) / 10.0f;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row, parent, false);
        }

        Earthquake quake = mQuakes.get(position);
        if (quake != null) {
            TextView itemView = (TextView) convertView
                    .findViewById(R.id.magnitude);
            if (itemView != null) {
                itemView.setText(quake.getFormattedMagnitude());
                if (quake.getRoundedMagnitude() >= mMinHighlight)
                    itemView.setTextColor(Color.RED);
                else
                    itemView.setTextColor(Color.BLACK);
            }

            itemView = (TextView) convertView.findViewById(R.id.date);
            if (itemView != null) {
                Date date = quake.getDate();
                if (null != date) {
                    String dateString = DateFormatting.getMediumDateString(getContext(), date);
                    itemView.setText(dateString);
                }
            }

            itemView = (TextView) convertView.findViewById(R.id.location);
            if (itemView != null)
                itemView.setText(quake.getLocation());
        }
        return convertView;
    }
}
