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

package speakman.whatsshakingnz.network;

import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import speakman.whatsshakingnz.dagger.AppScope;
import speakman.whatsshakingnz.network.geonet.GeonetDateTimeAdapter;
import speakman.whatsshakingnz.network.geonet.GeonetService;

/**
 * Created by Adam on 15-06-13.
 */
@Module
public class NetworkModule {
    @Provides
    GeonetService provideGeonetService(RestAdapter restAdapter) {
        return restAdapter.create(GeonetService.class);
    }

    @AppScope @Provides
    RestAdapter provideRestAdapter() {
        return new RestAdapter.Builder()
                .setConverter(new GsonConverter(new GsonBuilder()
                        .registerTypeAdapter(DateTime.class, new GeonetDateTimeAdapter())
                        .create()))
                .setEndpoint("http://wfs.geonet.org.nz")
                .build();
    }
}
