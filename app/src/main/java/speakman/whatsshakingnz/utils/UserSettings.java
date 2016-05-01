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

package speakman.whatsshakingnz.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Adam on 2/29/2016.
 */
public interface UserSettings {

    boolean notificationsEnabled();

    boolean notificationSoundEnabled();

    boolean notificationVibrationEnabled();

    boolean notificationLEDEnabled();

    double minimumNotificationMagnitude();

    double minimumDisplayMagnitude();

    @SuppressWarnings("unused")
    class UserSettingsImpl implements  UserSettings {

        private static final double DEFAULT_MINIMUM_NOTIFICATION_MAGNITUDE = 4.0;
        private static final double DEFAULT_MINIMUM_DISPLAY_MAGNITUDE = 0.0;

        // These are carried over from v1. Not all are used in v2 (yet?) but are preserved here anyway.
        private static final String KEY_PREF_MIN_DISPLAY_MAGNITUDE = "pref_minDisplayMagnitude";
        private static final String KEY_PREF_MIN_NOTIFICATION_MAGNITUDE = "pref_minHighlightMagnitude";
        private static final String KEY_PREF_NUM_QUAKES_TO_SHOW = "pref_numQuakesToShow";
        private static final String KEY_PREF_BG_NOTIFICATIONS_FREQ = "pref_backgroundNotificationsFrequency";
        private static final String KEY_PREF_ALLOW_BG_NOTIFICATIONS = "pref_allowBackgroundNotifications";
        private static final String KEY_PREF_BG_NOTIFICATIONS_SOUND = "pref_backgroundNotificationsSound";
        private static final String KEY_PREF_BG_NOTIFICATIONS_VIBRATE = "pref_backgroundNotificationsVibrate";
        private static final String KEY_PREF_BG_NOTIFICATIONS_LIGHT = "pref_backgroundNotificationsLight";
        private static final String KEY_PREF_BG_NOTIFICATIONS_REVIEWED_ONLY = "pref_backgroundNotificationsReviewed";

        private final SharedPreferences sharedPreferences;

        public UserSettingsImpl(Context context) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        @Override
        public boolean notificationsEnabled() {
            return sharedPreferences.getBoolean(KEY_PREF_ALLOW_BG_NOTIFICATIONS, true);
        }

        @Override
        public boolean notificationSoundEnabled() {
            return sharedPreferences.getBoolean(KEY_PREF_BG_NOTIFICATIONS_SOUND, true);
        }

        @Override
        public boolean notificationVibrationEnabled() {
            return sharedPreferences.getBoolean(KEY_PREF_BG_NOTIFICATIONS_VIBRATE, true);
        }

        @Override
        public boolean notificationLEDEnabled() {
            return sharedPreferences.getBoolean(KEY_PREF_BG_NOTIFICATIONS_LIGHT, true);
        }

        @Override
        public double minimumNotificationMagnitude() {
            int stored = sharedPreferences.getInt(KEY_PREF_MIN_NOTIFICATION_MAGNITUDE, -1);
            if (stored <= 0) {
                return DEFAULT_MINIMUM_NOTIFICATION_MAGNITUDE;
            } else {
                return stored / 10.0f;
            }
        }

        @Override
        public double minimumDisplayMagnitude() {
            int stored = sharedPreferences.getInt(KEY_PREF_MIN_DISPLAY_MAGNITUDE, -1);
            if (stored <= 0) {
                return DEFAULT_MINIMUM_DISPLAY_MAGNITUDE;
            } else {
                return stored / 10.0f;
            }
        }
    }
}
