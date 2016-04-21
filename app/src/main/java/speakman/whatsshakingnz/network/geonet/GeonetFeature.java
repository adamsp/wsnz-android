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

package speakman.whatsshakingnz.network.geonet;

import android.support.annotation.Nullable;

import org.joda.time.DateTime;

import speakman.whatsshakingnz.model.Earthquake;

/**
 * Created by Adam on 15-05-31.
 */
public class GeonetFeature implements Earthquake {

    // RestKit parses the response JSON out into this object.
    @SuppressWarnings("unused")
    static class Properties {
        double latitude;
        double longitude;
        double depth;
        double magnitude;
        DateTime origintime;
        DateTime modificationtime;
        String publicid;
        String evaluationmethod;
        String evaluationstatus;
        String evaluationmode;
        String earthmodel;
        String depthtype;
        double originerror;
        int usedphasecount;
        int usedstationcount;
        double minimumdistance;
        double azimuthalgap;
        String magnitudetype;
        double magnitudeuncertainty;
        int magnitudestationcount;
    }

    @SuppressWarnings("unused")
    Properties properties;

    @Override
    public double getMagnitude() {
        return properties == null ? 0 : properties.magnitude;
    }

    @Override
    public double getDepth() {
        return properties == null ? 0 : properties.depth;
    }

    @Override
    public String getId() {
        return properties == null ? null : properties.publicid;
    }

    @Nullable
    @Override
    public String getEvaluationMethod() {
        return properties == null ? null : properties.evaluationmethod;
    }

    @Nullable
    @Override
    public String getEvaluationStatus() {
        return properties == null ? null : properties.evaluationstatus;
    }

    @Nullable
    @Override
    public String getEvaluationMode() {
        return properties == null ? null : properties.evaluationmode;
    }

    @Nullable
    @Override
    public String getEarthModel() {
        return properties == null ? null : properties.earthmodel;
    }

    @Nullable
    @Override
    public String getDepthType() {
        return properties == null ? null : properties.depthtype;
    }

    @Override
    public double getOriginError() {
        return properties == null ? 0 : properties.originerror;
    }

    @Override
    public int getUsedPhaseCount() {
        return properties == null ? 0 : properties.usedphasecount;
    }

    @Override
    public int getUsedStationCount() {
        return properties == null ? 0 : properties.usedstationcount;
    }

    @Override
    public double getMinimumDistance() {
        return properties == null ? 0 : properties.minimumdistance;
    }

    @Override
    public double getAzimuthalGap() {
        return properties == null ? 0 : properties.azimuthalgap;
    }

    @Nullable
    @Override
    public String getMagnitudeType() {
        return properties == null ? null : properties.magnitudetype;
    }

    @Override
    public double getMagnitudeUncertainty() {
        return properties == null ? 0 : properties.magnitudeuncertainty;
    }

    @Override
    public int getMagnitudeStationCount() {
        return properties == null ? 0 : properties.magnitudestationcount;
    }

    public long getOriginTime() {
        if (properties == null) return 0;
        else if (properties.origintime == null) return 0;
        else return properties.origintime.getMillis();
    }

    @Override
    public long getUpdatedTime() {
        if (properties == null) return 0;
        else if (properties.modificationtime == null) return 0;
        else return properties.modificationtime.getMillis();
    }

    @Override
    public double getLatitude() {
        return properties == null ? 0 : properties.latitude;
    }

    @Override
    public double getLongitude() {
        return properties == null ? 0 : properties.longitude;
    }
    
}
