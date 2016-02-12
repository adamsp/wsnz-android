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

package speakman.whatsshakingnz.model.realm;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.EarthquakeStore;

/**
 * Created by Adam on 15-06-21.
 */
public class RealmEarthquakeStore implements EarthquakeStore {

    private Realm realm;
    private Set<EarthquakeDataChangeObserver> observers = new HashSet<>();
    private RealmResults<RealmEarthquake> earthquakes;

    @Inject
    public RealmEarthquakeStore(Context context) {
        RealmConfiguration config = new RealmConfiguration.Builder(context)
                .deleteRealmIfMigrationNeeded()
                .build();
        this.realm = Realm.getInstance(config);
    }

    @Override
    public List<? extends Earthquake> getEarthquakes() {
        if (earthquakes == null) {
            // Realm manages refreshing this for us because it's magic.
            earthquakes = realm.where(RealmEarthquake.class).findAllSorted("originTime", Sort.DESCENDING);
        }
        return earthquakes;
    }

    @Override
    public Earthquake getEarthquake(@NonNull String id) {
        return realm.where(RealmEarthquake.class).equalTo("id", id).findFirst();
    }

    @Override
    public void addEarthquakes(List<? extends Earthquake> earthquakes) {
        if (earthquakes == null || earthquakes.size() == 0) return;
        List<RealmEarthquake> realmEarthquakes = convertToRealmEarthquakes(earthquakes);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmEarthquakes);
        realm.commitTransaction();
        for (EarthquakeDataChangeObserver observer : observers) {
            observer.onEarthquakeDataChanged();
        }
    }

    @Override
    public void registerDataChangeObserver(EarthquakeDataChangeObserver observer) {
        if (observer == null) return;
        observers.add(observer);
    }

    @Override
    public void unregisterDataChangeObserver(EarthquakeDataChangeObserver observer) {
        if (observer == null) return;
        observers.remove(observer);
    }

    private List<RealmEarthquake> convertToRealmEarthquakes(List<? extends Earthquake> earthquakes) {
        List<RealmEarthquake> result = new ArrayList<>(earthquakes.size());
        for (Earthquake earthquake : earthquakes) {
            RealmEarthquake realmEarthquake = new RealmEarthquake(earthquake);
            result.add(realmEarthquake);
        }
        return result;
    }
}
