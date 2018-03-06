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
import io.realm.annotations.Required;
import speakman.whatsshakingnz.model.Earthquake;

/**
 * Created by Adam on 15-06-21.
 */
@SuppressWarnings("unused")
public class RealmEarthquake extends RealmObject implements Earthquake {

    public static final String FIELD_NAME_ORIGIN_TIME = "originTime";
    public static final String FIELD_NAME_MAGNITUDE = "magnitude";

    @PrimaryKey
    @Required
    private String id;

    private long originTime;
    private long updatedTime;
    private double latitude;
    private double longitude;
    private double depth;
    private double magnitude;
    @Nullable
    private String evaluationMethod;
    @Nullable
    private String evaluationStatus;
    @Nullable
    private String evaluationMode;
    @Nullable
    private String earthModel;
    @Nullable
    private String depthType;
    private double originError;
    private int usedPhaseCount;
    private int usedStationCount;
    private double minimumDistance;
    private double azimuthalGap;
    @Nullable
    private String magnitudeType;
    private double magnitudeUncertainty;
    private int magnitudeStationCount;

    // For Realm support
    public RealmEarthquake() {
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RealmEarthquake that = (RealmEarthquake) o;

        if (originTime != that.originTime) return false;
        if (updatedTime != that.updatedTime) return false;
        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;
        if (Double.compare(that.depth, depth) != 0) return false;
        if (Double.compare(that.magnitude, magnitude) != 0) return false;
        if (Double.compare(that.originError, originError) != 0) return false;
        if (usedPhaseCount != that.usedPhaseCount) return false;
        if (usedStationCount != that.usedStationCount) return false;
        if (Double.compare(that.minimumDistance, minimumDistance) != 0) return false;
        if (Double.compare(that.azimuthalGap, azimuthalGap) != 0) return false;
        if (Double.compare(that.magnitudeUncertainty, magnitudeUncertainty) != 0) return false;
        if (magnitudeStationCount != that.magnitudeStationCount) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (evaluationMethod != null ? !evaluationMethod.equals(that.evaluationMethod) : that.evaluationMethod != null)
            return false;
        if (evaluationStatus != null ? !evaluationStatus.equals(that.evaluationStatus) : that.evaluationStatus != null)
            return false;
        if (evaluationMode != null ? !evaluationMode.equals(that.evaluationMode) : that.evaluationMode != null)
            return false;
        if (earthModel != null ? !earthModel.equals(that.earthModel) : that.earthModel != null)
            return false;
        if (depthType != null ? !depthType.equals(that.depthType) : that.depthType != null)
            return false;
        return magnitudeType != null ? magnitudeType.equals(that.magnitudeType) : that.magnitudeType == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (originTime ^ (originTime >>> 32));
        result = 31 * result + (int) (updatedTime ^ (updatedTime >>> 32));
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(depth);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(magnitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (evaluationMethod != null ? evaluationMethod.hashCode() : 0);
        result = 31 * result + (evaluationStatus != null ? evaluationStatus.hashCode() : 0);
        result = 31 * result + (evaluationMode != null ? evaluationMode.hashCode() : 0);
        result = 31 * result + (earthModel != null ? earthModel.hashCode() : 0);
        result = 31 * result + (depthType != null ? depthType.hashCode() : 0);
        temp = Double.doubleToLongBits(originError);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + usedPhaseCount;
        result = 31 * result + usedStationCount;
        temp = Double.doubleToLongBits(minimumDistance);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(azimuthalGap);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (magnitudeType != null ? magnitudeType.hashCode() : 0);
        temp = Double.doubleToLongBits(magnitudeUncertainty);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + magnitudeStationCount;
        return result;
    }
}
