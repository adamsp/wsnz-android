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

package speakman.whatsshakingnz.analytics;

import android.app.Application;
import android.support.annotation.NonNull;

import java.util.List;

import speakman.whatsshakingnz.model.Earthquake;
import timber.log.Timber;

/**
 * Created by Adam on 2016-03-06.
 */
public class Analytics {
    public static void initialize(@SuppressWarnings("UnusedParameters") Application app) {
        Timber.d("{analytics disabled} Not enabling any analytics platforms");
    }

    public static void logMainPageViewedFromNotification() {
        Timber.d("Logging earthquake list shown from notification");
    }

    public static void logEarthquakeViewFromNotification(@NonNull Earthquake earthquake) {
        Timber.d("Logging earthquake viewed from notification for event with id { %s }", earthquake.getId());
    }

    public static void logEarthquakeSelectedOnMap(@NonNull Earthquake earthquake) {
        Timber.d("Logging earthquake selected on map for event with id { %s }", earthquake.getId());
    }

    public static void logEarthquakeSelectedInList(@NonNull Earthquake earthquake) {
        Timber.d("Logging earthquake selected in list for event with id { %s }", earthquake.getId());
    }

    public static void logDetailViewExpanded(@NonNull Earthquake earthquake) {
        Timber.d("Logging earthquake detail view expansion for event with id { %s }", earthquake.getId());
    }

    public static void logNotificationShownForEarthquake(@NonNull Earthquake earthquake) {
        Timber.d("Logging notification shown for event with id { %s }", earthquake.getId());
    }

    public static void logNotificationShownForEarthquakes(@NonNull List<? extends Earthquake> earthquakes) {
        Timber.d("Logging notification shown for { %d } earthquakes.", earthquakes.size());
    }
}
