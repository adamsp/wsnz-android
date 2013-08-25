package speakman.whatsshakingnz.activities;

import java.util.ArrayList;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.earthquake.Earthquake;
import speakman.whatsshakingnz.fragments.EarthquakeDetailFragment;
import speakman.whatsshakingnz.fragments.NZMapFragment;
import android.content.Intent;
import android.os.Bundle;

public class QuakeActivity extends WhatsShakingActivity {
    public static String QUAKE_KEY = "speakman.whatsshakingnz.SingleQuake";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake);

        Intent sender = getIntent();
        final Earthquake quake = sender.getParcelableExtra(QUAKE_KEY);
        NZMapFragment map = (NZMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.quake_map);
        // If we have a saved state, we're being restored or rotated.
        // In this case, we don't want to overwrite the saved map zoom/center location.
        boolean centerOnQuake = savedInstanceState == null;
        map.setQuake(quake, centerOnQuake);
        EarthquakeDetailFragment detail = (EarthquakeDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.quake_detail);
        detail.setQuake(quake);
    }
}
