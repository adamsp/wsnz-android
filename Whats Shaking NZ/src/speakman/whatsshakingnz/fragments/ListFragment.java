package speakman.whatsshakingnz.fragments;

import java.util.ArrayList;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.activities.MainActivity;
import speakman.whatsshakingnz.activities.QuakeActivity;
import speakman.whatsshakingnz.earthquake.Earthquake;
import speakman.whatsshakingnz.earthquake.EarthquakeArrayAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import speakman.whatsshakingnz.earthquake.EarthquakeTapListener;

public class ListFragment extends SherlockListFragment {

    private boolean firstTime = true;
    private EarthquakeTapListener mTapListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void updateQuakes(ArrayList<Earthquake> quakes) {
        // TODO Bug here if user switches to map view before
        // first download has completed, then switches back.
        // Content view not created. If no quakes have been
        // found in this situation, the list view will display
        // an empty window.
        if (firstTime) {
            try {
                setEmptyText(getString(R.string.no_quakes));
            } catch (IllegalStateException ex) {
            }
            firstTime = false;
        }
        EarthquakeArrayAdapter adapter = new EarthquakeArrayAdapter(this
                .getActivity(), R.layout.row, quakes);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        if (mTapListener != null) {
            Earthquake quake = (Earthquake) getListView().getItemAtPosition(position);
            mTapListener.onEarthquakeTap(quake);
        }
    }


    public void setOnEarthquakeTapListener(EarthquakeTapListener listener) {
        mTapListener = listener;
    }
}
