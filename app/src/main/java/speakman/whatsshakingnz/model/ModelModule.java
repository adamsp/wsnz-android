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

package speakman.whatsshakingnz.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dagger.Module;
import dagger.Provides;
import speakman.whatsshakingnz.dagger.AppScope;

/**
 * Created by Adam on 15-06-13.
 */
@Module
public class ModelModule {
    @AppScope
    @Provides
    EarthquakeStore provideEarthquakeStore() {
        return new EarthquakeStore() {
            private List<Earthquake> earthquakes = new ArrayList<>();
            private Set<EarthquakeDataChangeObserver> observers = new HashSet<>();

            public List<Earthquake> getEarthquakes() {
                return earthquakes;
            }

            public void setEarthquakes(List<? extends Earthquake> earthquakes) {
                //noinspection unchecked
                this.earthquakes = (List<Earthquake>) earthquakes;
                for (EarthquakeDataChangeObserver observer : observers) {
                    observer.onEarthquakeDataChanged();
                }
            }

            public void registerDataChangeObserver(EarthquakeDataChangeObserver observer) {
                if (observer == null) return;
                observers.add(observer);
            }

            public void unregisterDataChangeObserver(EarthquakeDataChangeObserver observer) {
                if (observer == null) return;
                observers.remove(observer);
            }
        };
    }
}
