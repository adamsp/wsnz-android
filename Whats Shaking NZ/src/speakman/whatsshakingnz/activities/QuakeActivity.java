package speakman.whatsshakingnz.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.view.View;
import android.widget.Toast;
import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.earthquake.Earthquake;
import speakman.whatsshakingnz.fragments.EarthquakeDetailFragment;
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
        EarthquakeDetailFragment detail = (EarthquakeDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.quake_detail);
        detail.setQuake(quake);
    }
}
