package speakman.whatsshakingnz.fragments;

import java.util.ArrayList;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.activities.QuakeActivity;
import speakman.whatsshakingnz.earthquake.Earthquake;
import speakman.whatsshakingnz.earthquake.EarthquakeArrayAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class ListFragment extends SherlockListFragment {

    private boolean firstTime = true;

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
                .getActivity().getApplicationContext(), R.layout.row, quakes);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        Intent intent = new Intent(getSherlockActivity(), QuakeActivity.class);
        Earthquake quake = (Earthquake) getListView().getItemAtPosition(position);
        intent.putExtra(QuakeActivity.QUAKE_KEY, quake);
        startActivity(intent);
    }
}
