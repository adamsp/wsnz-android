package speakman.whatsshakingnz.fragments;

import java.util.Date;
import java.util.Locale;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.earthquake.Earthquake;
import speakman.whatsshakingnz.formatting.DateFormatting;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Created by Adam on 8/08/13.
 */
public class EarthquakeDetailFragment extends SherlockFragment {

    private Earthquake mQuake;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_earthquake_details, container, false);
    }

    public void setQuake(Earthquake quake) {
        mQuake = quake;
        displayQuakeDetails();
    }

    @Override
    public void onResume() {
        super.onResume();
        displayQuakeDetails();
    }

    private void displayQuakeDetails() {
        if (mQuake == null) return;
        if (getActivity() == null) return;
        View root = getView();
        if (root == null) return;
        
        TextView v = (TextView) root.findViewById(R.id.magnitude_detail_field);
        if (null != v)
            v.setText(mQuake.getFormattedMagnitude());

        v = (TextView) root.findViewById(R.id.depth_detail_field);
        if (null != v)
            v.setText(getString(R.string.depth_detail, mQuake.getFormattedDepth()));

        v = (TextView) root.findViewById(R.id.date_detail_field);
        if (null != v) {
            Date date = mQuake.getDate();
            if (null != date) {
                String dateString = DateFormatting.getShortDateString(getActivity(), date);
                v.setText(dateString);
            }
        }

        v = (TextView) root.findViewById(R.id.status_detail_field);
        if (null != v) {
            v.setText(mQuake.getStatus());
        }

        View container = root.findViewById(R.id.status_detail_container);
        if (null != container) {
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEarthquakeStatusMessage(mQuake);
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
        Toast.makeText(getActivity(), resourceId, Toast.LENGTH_SHORT).show();
    }

    public Earthquake getQuake() {
        return mQuake;
    }
}
