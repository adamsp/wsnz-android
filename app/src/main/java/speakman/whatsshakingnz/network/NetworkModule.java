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

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import speakman.whatsshakingnz.InternalPreferences;
import speakman.whatsshakingnz.dagger.AppScope;
import speakman.whatsshakingnz.network.geonet.GeonetDateTimeAdapter;

/**
 * Created by Adam on 15-06-13.
 */
@Module
public class NetworkModule {

    @AppScope @Provides
    OkHttpClient provideOkHttp() {
        return new OkHttpClient();
    }

    @AppScope @Provides
    Gson provideGson() {
        return new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new GeonetDateTimeAdapter())
                .create();
    }

    @Provides
    NotificationTimeStore provideNotificationTimeStore(Context context) {
        return new InternalPreferences(context);
    }

    @Provides
    EarthquakeService provideEarthquakeService(OkHttpClient client, Gson gson, RequestTimeStore timeStore) {
        return new GeonetService(client, gson, timeStore);
    }
}
