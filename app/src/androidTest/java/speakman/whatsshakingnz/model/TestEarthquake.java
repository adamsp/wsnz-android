/*
 * Copyright 2016 Adam Speakman
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

import android.support.annotation.Nullable;

/**
 * Created by Adam on 2/29/2016.
 */
public abstract class TestEarthquake implements Earthquake {
    @Override
    public long getOriginTime() {
        return 0;
    }

    @Override
    public long getUpdatedTime() {
        return 0;
    }

    @Override
    public double getLatitude() {
        return 0;
    }

    @Override
    public double getLongitude() {
        return 0;
    }

    @Override
    public double getMagnitude() {
        return 0;
    }

    @Override
    public double getDepth() {
        return 0;
    }

    @Override
    public String getId() {
        return null;
    }

    @Nullable
    @Override
    public String getEvaluationMethod() {
        return null;
    }

    @Nullable
    @Override
    public String getEvaluationStatus() {
        return null;
    }

    @Nullable
    @Override
    public String getEvaluationMode() {
        return null;
    }

    @Nullable
    @Override
    public String getEarthModel() {
        return null;
    }

    @Nullable
    @Override
    public String getDepthType() {
        return null;
    }

    @Override
    public double getOriginError() {
        return 0;
    }

    @Override
    public int getUsedPhaseCount() {
        return 0;
    }

    @Override
    public int getUsedStationCount() {
        return 0;
    }

    @Override
    public double getMinimumDistance() {
        return 0;
    }

    @Override
    public double getAzimuthalGap() {
        return 0;
    }

    @Nullable
    @Override
    public String getMagnitudeType() {
        return null;
    }

    @Override
    public double getMagnitudeUncertainty() {
        return 0;
    }

    @Override
    public int getMagnitudeStationCount() {
        return 0;
    }
}
