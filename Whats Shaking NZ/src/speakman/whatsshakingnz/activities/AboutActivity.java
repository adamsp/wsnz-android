package speakman.whatsshakingnz.activities;

import speakman.whatsshakingnz.R;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends WhatsShakingActivity {
	
	private static String aboutText = "What's Shaking, NZ? uses the Geonet.org.nz data feed "
        + "to provide you with up to date information about the latest earthquakes around New Zealand.\n\n"
        + "Please tweet (@WhatsShakingNZ) or email (android-support@speakman.net.nz) me with any "
        + "problems you find or suggestions for features you'd like to see in the next version.\n\n"
        + "For news and more information, check out the What's Shaking, NZ? website.\n"
        + "www.whatsshaking.co.nz";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
//		TextView tv = (TextView)findViewById(R.id.about_field);
//		tv.setText(aboutText);
//		tv.setMovementMethod(LinkMovementMethod.getInstance());
	}

}
