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

import android.support.annotation.Nullable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import speakman.whatsshakingnz.model.Earthquake;

/**
 * Created by Adam on 15-06-21.
 */
public class RealmEarthquake extends RealmObject implements Earthquake {

    public static final String FIELD_NAME_ORIGIN_TIME = "originTime";
    public static final String FIELD_NAME_MAGNITUDE = "magnitude";

    @PrimaryKey
    private String id;

    private long originTime;
    private long updatedTime;
    private double latitude;
    private double longitude;
    private double depth;
    private double magnitude;
    @Nullable private String evaluationMethod;
    @Nullable private String evaluationStatus;
    @Nullable private String evaluationMode;
    @Nullable private String earthModel;
    @Nullable private String depthType;
    private double originError;
    private int usedPhaseCount;
    private int usedStationCount;
    private double minimumDistance;
    private double azimuthalGap;
    @Nullable private String magnitudeType;
    private double magnitudeUncertainty;
    private int magnitudeStationCount;

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
        this.evaluationMethod = other.getEvaluationMethod();
        this.evaluationStatus = other.getEvaluationStatus();
        this.evaluationMode = other.getEvaluationMode();
        this.earthModel = other.getEarthModel();
        this.depthType = other.getDepthType();
        this.originError = other.getOriginError();
        this.usedPhaseCount = other.getUsedPhaseCount();
        this.usedStationCount = other.getUsedStationCount();
        this.minimumDistance = other.getMinimumDistance();
        this.azimuthalGap = other.getAzimuthalGap();
        this.magnitudeType = other.getMagnitudeType();
        this.magnitudeUncertainty = other.getMagnitudeUncertainty();
        this.magnitudeStationCount = other.getMagnitudeStationCount();
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

    @Override
    @Nullable
    public String getEvaluationMethod() {
        return evaluationMethod;
    }

    public void setEvaluationMethod(@Nullable String evaluationMethod) {
        this.evaluationMethod = evaluationMethod;
    }

    @Override
    @Nullable
    public String getEvaluationStatus() {
        return evaluationStatus;
    }

    public void setEvaluationStatus(@Nullable String evaluationStatus) {
        this.evaluationStatus = evaluationStatus;
    }

    @Override
    @Nullable
    public String getEvaluationMode() {
        return evaluationMode;
    }

    public void setEvaluationMode(@Nullable String evaluationMode) {
        this.evaluationMode = evaluationMode;
    }

    @Override
    @Nullable
    public String getEarthModel() {
        return earthModel;
    }

    public void setEarthModel(@Nullable String earthModel) {
        this.earthModel = earthModel;
    }

    @Override
    @Nullable
    public String getDepthType() {
        return depthType;
    }

    public void setDepthType(@Nullable String depthType) {
        this.depthType = depthType;
    }

    @Override
    public double getOriginError() {
        return originError;
    }

    public void setOriginError(double originError) {
        this.originError = originError;
    }

    @Override
    public int getUsedPhaseCount() {
        return usedPhaseCount;
    }

    public void setUsedPhaseCount(int usedPhaseCount) {
        this.usedPhaseCount = usedPhaseCount;
    }

    @Override
    public int getUsedStationCount() {
        return usedStationCount;
    }

    public void setUsedStationCount(int usedStationCount) {
        this.usedStationCount = usedStationCount;
    }

    @Override
    public double getMinimumDistance() {
        return minimumDistance;
    }

    public void setMinimumDistance(double minimumDistance) {
        this.minimumDistance = minimumDistance;
    }

    @Override
    public double getAzimuthalGap() {
        return azimuthalGap;
    }

    public void setAzimuthalGap(double azimuthalGap) {
        this.azimuthalGap = azimuthalGap;
    }

    @Override
    @Nullable
    public String getMagnitudeType() {
        return magnitudeType;
    }

    public void setMagnitudeType(@Nullable String magnitudeType) {
        this.magnitudeType = magnitudeType;
    }

    @Override
    public double getMagnitudeUncertainty() {
        return magnitudeUncertainty;
    }

    public void setMagnitudeUncertainty(double magnitudeUncertainty) {
        this.magnitudeUncertainty = magnitudeUncertainty;
    }

    @Override
    public int getMagnitudeStationCount() {
        return magnitudeStationCount;
    }

    public void setMagnitudeStationCount(int magnitudeStationCount) {
        this.magnitudeStationCount = magnitudeStationCount;
    }
}
