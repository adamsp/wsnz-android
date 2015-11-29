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

    private long originTime;
    private long updatedTime;
    private double latitude;
    private double longitude;
    private double depth;
    private double magnitude;

    // For Realm support
    public RealmEarthquake() { }

    public RealmEarthquake(Earthquake other) {
        this.id = other.getId();
        this.originTime = other.getOriginTime();
        this.updatedTime = other.getUpdatedTime();
        this.latitude = other.getLatitude();
        this.longitude = other.getLongitude();
        this.depth = other.getDepth();
        this.magnitude = other.getMagnitude();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public long getOriginTime() {
        return originTime;
    }

    public void setOriginTime(long originTime) {
        this.originTime = originTime;
    }

    @Override
    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    @Override
    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }
}
