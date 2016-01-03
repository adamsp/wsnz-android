/*
 * Copyright 2015 Adam Speakman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package speakman.whatsshakingnz.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.MapView;

import javax.inject.Inject;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.WhatsShakingApplication;
import speakman.whatsshakingnz.model.EarthquakeStore;
import speakman.whatsshakingnz.network.RequestManager;
import speakman.whatsshakingnz.ui.DividerItemDecoration;
import speakman.whatsshakingnz.ui.EarthquakeListAdapter;
import speakman.whatsshakingnz.ui.LicensesFragment;
import speakman.whatsshakingnz.ui.viewmodel.EarthquakeListViewModel;

public class MainActivity extends AppCompatActivity implements EarthquakeStore.EarthquakeDataChangeObserver {

    @Inject
    EarthquakeStore store;
    @Inject
    RequestManager requestManager;

    private MapView map;
    private RecyclerView.Adapter<EarthquakeListViewModel.ViewHolder> dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.activity_main_toolbar));
        WhatsShakingApplication.getInstance().inject(this);
        map = ((MapView)findViewById(R.id.activity_main_map));
        map.onCreate(savedInstanceState == null ? null : savedInstanceState.getBundle("mapState"));
        RecyclerView mainList = (RecyclerView) findViewById(R.id.activity_main_list);
        mainList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mainList.setHasFixedSize(true);
        mainList.setLayoutManager(new LinearLayoutManager(this));
        dataAdapter = new EarthquakeListAdapter(store);
        mainList.setAdapter(dataAdapter);
        requestManager.retrieveNewEarthquakes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        store.registerDataChangeObserver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
        store.unregisterDataChangeObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapState = new Bundle();
        map.onSaveInstanceState(mapState);
        outState.putBundle("mapState", mapState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_action_licences:
                LicensesFragment.displayLicensesFragment(getFragmentManager(), true);
                return true;
            case R.id.menu_action_google_licences:
                LicensesFragment.displayGooglePlayServicesLicensesFragment(getFragmentManager(), true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }

    @Override
    public void onEarthquakeDataChanged() {
        dataAdapter.notifyDataSetChanged();
    }
}
