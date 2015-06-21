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

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by Adam on 15-05-31.
 * Documentation for the Geonet service we're using is available here:
 * http://info.geonet.org.nz/display/appdata/Advanced+Queries
 *
 * Sample complete request, including ordering for "oldest first" in order to enable our own version
 * of paging (desirable since we don't want to load a few thousand at once):
 * http://wfs.geonet.org.nz/geonet/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=geonet:quake_search_v1&outputFormat=json&cql_filter=origintime%3E=%272015-05-31T18:06:16.912Z%27&sortBy=origintime&maxFeatures=1
 */
public interface GeonetService {

    /**
     * A format string for filtering earthquakes after a given most recent update timestamp.
     */
    String FILTER_FORMAT_MOST_RECENT_UPDATE = "modificationtime>%s AND eventtype='earthquake'";

    @GET("/geonet/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=geonet:quake_search_v1&outputFormat=json&sortBy=modificationtime")
    Observable<GeonetResponse> getEarthquakes(@Query("cql_filter") String filter, @Query("maxFeatures") int maxFeatures);
}
