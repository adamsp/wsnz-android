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

package speakman.whatsshakingnz.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import javax.inject.Inject;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.WhatsShakingApplication;
import speakman.whatsshakingnz.databinding.RowEarthquakeBinding;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.EarthquakeStore;
import speakman.whatsshakingnz.network.RequestManager;

public class MainActivity extends AppCompatActivity implements EarthquakeStore.EarthquakeDataChangeObserver {

    @Inject
    EarthquakeStore store;
    @Inject
    RequestManager requestManager;

    private RecyclerView.Adapter<Earthquake.ViewHolder> dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar)findViewById(R.id.activity_main_toolbar));
        WhatsShakingApplication.getInstance().inject(this);
        RecyclerView mainList = (RecyclerView) findViewById(R.id.activity_main_list);
        mainList.setHasFixedSize(true);
        mainList.setLayoutManager(new LinearLayoutManager(this));
        dataAdapter = new RecyclerView.Adapter<Earthquake.ViewHolder>() {

            @Override
            public Earthquake.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                RowEarthquakeBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_earthquake, parent, false);
                return new Earthquake.ViewHolder(binding);
            }

            @Override
            public void onBindViewHolder(Earthquake.ViewHolder holder, int position) {
                holder.binding.setEarthquake(store.getEarthquakes().get(position));
            }

            @Override
            public int getItemCount() {
                return store.getEarthquakes().size();
            }
        };
        mainList.setAdapter(dataAdapter);
        requestManager.retrieveNewEarthquakes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        store.registerDataChangeObserver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        store.unregisterDataChangeObserver(this);
    }

    @Override
    public void onEarthquakeDataChanged() {
        dataAdapter.notifyDataSetChanged();
    }
}
