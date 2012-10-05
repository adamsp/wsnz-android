package speakman.whatsshakingnz.activities;

import java.util.ArrayList;
import java.util.Date;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.earthquake.Earthquake;
import speakman.whatsshakingnz.fragments.MapFragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;

public class QuakeActivity extends WhatsShakingActivity {
	public static String QUAKE_KEY = "speakman.whatsshakingnz.SingleQuake";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quake_view);

		Intent sender = getIntent();
		Earthquake quake = (Earthquake) sender.getParcelableExtra(QUAKE_KEY);
		ArrayList<Earthquake> quakes = new ArrayList<Earthquake>();
		quakes.add(quake);
		MapFragment map = (MapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.quake_map);
		map.updateQuakes(quakes);
		TextView v = (TextView) findViewById(R.id.magnitude_field);
		if (null != v)
			v.setText(quake.getFormattedMagnitude());
		
		v = (TextView) findViewById(R.id.depth_field);
		if (null != v)
			v.setText(quake.getFormattedDepth() + " km");
		
		v = (TextView) findViewById(R.id.date_field);
		if (null != v) {
			Date date = quake.getDate();
			if (null != date) {
				java.text.DateFormat tf = DateFormat.getTimeFormat(this);
				java.text.DateFormat df = DateFormat.getMediumDateFormat(this);
				String dateString = String.format("%s, %s", df.format(date),
						tf.format(date));
				v.setText(dateString);
			}
		}
	}
}
