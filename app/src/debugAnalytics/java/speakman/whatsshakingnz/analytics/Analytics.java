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

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;

import java.util.List;

import io.fabric.sdk.android.Fabric;
import speakman.whatsshakingnz.model.Earthquake;
import timber.log.Timber;

/**
 * Created by Adam on 2016-03-06.
 */
public class Analytics {
    public static void initialize(Application app) {
        Timber.d("{analytics enabled} Enabling Crashlytics");
        Timber.d("{analytics enabled} Enabling Answers");
        Fabric fabric = new Fabric.Builder(app)
                .kits(new Crashlytics(), new Answers())
                .debuggable(true)
                .build();
        Fabric.with(fabric);
    }

    public static void logMainPageViewedFromNotification() {
        Timber.d("Logging earthquake list shown from notification");
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Earthquake List viewed from Notification")
                .putContentType("list-view"));
    }

    public static void logEarthquakeViewFromNotification(@NonNull Earthquake earthquake) {
        Timber.d("Logging earthquake viewed from notification for event with id { %s }", earthquake.getId());
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Earthquake viewed from Notification")
                .putContentType("earthquake-view")
                .putCustomAttribute("source", "notification")
                .putContentId(earthquake.getId()));
    }

    public static void logEarthquakeSelectedOnMap(@NonNull Earthquake earthquake) {
        Timber.d("Logging earthquake selected on map for event with id { %s }", earthquake.getId());
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Earthquake viewed")
                .putContentType("earthquake-view")
                .putCustomAttribute("source", "map")
                .putContentId(earthquake.getId()));
    }

    public static void logEarthquakeSelectedInList(@NonNull Earthquake earthquake) {
        Timber.d("Logging earthquake selected in list for event with id { %s }", earthquake.getId());
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Earthquake viewed")
                .putContentType("earthquake-view")
                .putCustomAttribute("source", "list")
                .putContentId(earthquake.getId()));
    }

    public static void logDetailViewExpanded(@NonNull Earthquake earthquake) {
        Timber.d("Logging earthquake detail view expansion for event with id { %s }", earthquake.getId());
        Answers.getInstance().logCustom(new CustomEvent("Earthquake Detail Expanded")
                .putCustomAttribute("earthquake-id", earthquake.getId()));
    }

    public static void logNotificationShownForEarthquake(@NonNull Earthquake earthquake) {
        Timber.d("Logging notification shown for event with id { %s }", earthquake.getId());
        Answers.getInstance().logCustom(new CustomEvent("Notification Shown")
                .putCustomAttribute("earthquake-id", earthquake.getId()));
    }

    public static void logNotificationShownForEarthquakes(@NonNull List<? extends Earthquake> earthquakes) {
        Timber.d("Logging notification shown for { %d } earthquakes.", earthquakes.size());
        Answers.getInstance().logCustom(new CustomEvent("Notification Shown")
                .putCustomAttribute("earthquakes-count", earthquakes.size()));
    }
}
