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

/**
 * Created by Adam on 2016-03-06.
 */
public class Analytics {
    public static void initialize(Application app) {
        Fabric fabric = new Fabric.Builder(app)
                .kits(new Crashlytics(), new Answers())
                .build();
        Fabric.with(fabric);
    }

    public static void logMainPageViewedFromNotification() {
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Earthquake List viewed from Notification")
                .putContentType("list-view"));
    }

    public static void logEarthquakeViewFromNotification(@NonNull Earthquake earthquake) {
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Earthquake viewed from Notification")
                .putContentType("earthquake-view")
                .putCustomAttribute("source", "notification")
                .putContentId(earthquake.getId()));
    }

    public static void logEarthquakeSelectedOnMap(@NonNull Earthquake earthquake) {
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Earthquake viewed")
                .putContentType("earthquake-view")
                .putCustomAttribute("source", "map")
                .putContentId(earthquake.getId()));
    }

    public static void logEarthquakeSelectedInList(@NonNull Earthquake earthquake) {
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Earthquake viewed")
                .putContentType("earthquake-view")
                .putCustomAttribute("source", "list")
                .putContentId(earthquake.getId()));
    }

    public static void logDetailViewExpanded(@NonNull Earthquake earthquake) {
        Answers.getInstance().logCustom(new CustomEvent("Earthquake Detail Expanded")
                .putCustomAttribute("earthquake-id", earthquake.getId()));
    }

    public static void logNotificationShownForEarthquake(@NonNull Earthquake earthquake) {
        Answers.getInstance().logCustom(new CustomEvent("Notification Shown")
                .putCustomAttribute("earthquake-id", earthquake.getId()));
    }

    public static void logNotificationShownForEarthquakes(@NonNull List<? extends Earthquake> earthquakes) {
        Answers.getInstance().logCustom(new CustomEvent("Notification Shown")
                .putCustomAttribute("earthquakes-count", earthquakes.size()));
    }
}
