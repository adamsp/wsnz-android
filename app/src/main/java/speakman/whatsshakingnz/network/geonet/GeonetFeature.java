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

import org.joda.time.DateTime;

import speakman.whatsshakingnz.model.Earthquake;

/**
 * Created by Adam on 15-05-31.
 */
public class GeonetFeature implements Earthquake {

    static class Properties {
        double latitude;
        double longitude;
        double depth;
        double magnitude;
        DateTime origintime;
        DateTime modificationtime;
        String publicid;
    }

//    {
//        type: "Feature",
//                id: "quake_search_v1.2015p407790",
//                geometry: {
//            type: "Point",
//                    coordinates: [
//            175.4685716,
//                    -40.39407718
//            ]
//        },
//            geometry_name: "origin_geom",
//                    properties: {
//            publicid: "2015p407790",
//                    eventtype: null,
//                    origintime: "2015-05-31T21:10:45.867Z",
//                    modificationtime: "2015-05-31T21:14:43.188Z",
//                    latitude: -40.39407718,
//                    longitude: 175.4685716,
//                    depth: 99.6875,
//                    magnitude: 2.923555525,
//                    evaluationmethod: "NonLinLoc",
//                    evaluationstatus: null,
//                    evaluationmode: "automatic",
//                    earthmodel: "nz3drx",
//                    depthtype: null,
//                    originerror: 0.5916628805,
//                    usedphasecount: 24,
//                    usedstationcount: 24,
//                    minimumdistance: 0.4863447969,
//                    azimuthalgap: 155.9689617,
//                    magnitudetype: "M",
//                    magnitudeuncertainty: null,
//                    magnitudestationcount: 18,
//                    bbox: [
//            175.4685716,
//                    -40.39407718,
//                    175.4685716,
//                    -40.39407718
//            ]
//        }
//    }

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
    public String getLocation() {
        return properties == null ? null : String.format("%.3f / %.3f (%.0f km)", properties.latitude, properties.longitude, properties.depth);
    }

    @Override
    public String getId() {
        return properties == null ? null : properties.publicid;
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
