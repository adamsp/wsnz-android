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

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import speakman.whatsshakingnz.model.Earthquake;

/**
 * Created by Adam on 15-06-21.
 */
public class RealmEarthquake extends RealmObject implements Earthquake {

    @PrimaryKey
    private String id;

    private double magnitude;
    private String location;

    // For Realm support
    public RealmEarthquake() { }

    public RealmEarthquake(Earthquake other) {
        this.id = other.getId();
        this.magnitude = other.getMagnitude();
        this.location = other.getLocation();
    }

    @Override
    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
