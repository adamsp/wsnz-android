package speakman.whatsshakingnz.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.view.View;
import android.widget.Toast;
import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.earthquake.Earthquake;
import speakman.whatsshakingnz.fragments.NZMapFragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;

public class QuakeActivity extends WhatsShakingActivity {
    public static String QUAKE_KEY = "speakman.whatsshakingnz.SingleQuake";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake);

        Intent sender = getIntent();
        final Earthquake quake = sender.getParcelableExtra(QUAKE_KEY);
        ArrayList<Earthquake> quakes = new ArrayList<Earthquake>();
        quakes.add(quake);
        NZMapFragment map = (NZMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.quake_map);
        map.updateQuakes(quakes);
        TextView v = (TextView) findViewById(R.id.magnitude_detail_field);
        if (null != v)
            v.setText(quake.getFormattedMagnitude());

        v = (TextView) findViewById(R.id.depth_detail_field);
        if (null != v)
            v.setText(quake.getFormattedDepth() + " km");

        v = (TextView) findViewById(R.id.date_detail_field);
        if (null != v) {
            Date date = quake.getDate();
            if (null != date) {
                java.text.DateFormat tf = DateFormat.getTimeFormat(this);
                java.text.DateFormat df = DateFormat.getDateFormat(this);
                String dateString = String.format("%s, %s", df.format(date),
                        tf.format(date));
                v.setText(dateString);
            }
        }

        v = (TextView) findViewById(R.id.status_detail_field);
        if (null != v) {
            v.setText(quake.getStatus());
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEarthquakeStatusMessage(quake);
                }
            });
        }
    }

    private void showEarthquakeStatusMessage(Earthquake quake) {
        int resourceId;
        String status = quake.getStatus().toLowerCase(Locale.US);
        if (status.equals("automatic")) {
            resourceId = R.string.earthquake_status_automatic;
        } else if(status.equals("deleted")) {
            resourceId = R.string.earthquake_status_deleted;
        } else if (status.equals("duplicate")) {
            resourceId = R.string.earthquake_status_duplicate;
        } else if (status.equals("reviewed")) {
            resourceId = R.string.earthquake_status_reviewed;
        } else {
            resourceId = R.string.earthquake_status_unknown;
        }
        Toast.makeText(QuakeActivity.this, resourceId, Toast.LENGTH_SHORT).show();
    }
}
