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

package speakman.whatsshakingnz;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import org.joda.time.DateTime;

import speakman.whatsshakingnz.network.NotificationTimeStore;
import speakman.whatsshakingnz.network.RequestTimeStore;

/**
 * Created by Adam on 15-06-07.
 */
public class InternalPreferences implements RequestTimeStore, NotificationTimeStore {

    private static final String PREFERENCES_FILENAME = "speakman.whatsshakingnz.InternalPreferences.PREFERENCES_FILENAME";
    private static final String KEY_MOST_RECENT_REQUEST_TIME = "speakman.whatsshakingnz.InternalPreferences.KEY_MOST_RECENT_REQUEST_TIME";
    private static final String KEY_MOST_RECENTLY_SEEN_TIME = "speakman.whatsshakingnz.InternalPreferences.KEY_MOST_RECENTLY_SEEN_TIME";

    private SharedPreferences sharedPrefs;

    public InternalPreferences(Context ctx) {
        sharedPrefs = ctx.getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);
    }

    @Override
    public void saveMostRecentUpdateTime(@Nullable DateTime dateTime) {
        if (dateTime == null) {
            sharedPrefs.edit().remove(KEY_MOST_RECENT_REQUEST_TIME).apply();
        } else {
            sharedPrefs.edit().putLong(KEY_MOST_RECENT_REQUEST_TIME, dateTime.getMillis()).apply();
        }
    }

    @Nullable
    @Override
    public DateTime getMostRecentUpdateTime() {
        if (sharedPrefs.contains(KEY_MOST_RECENT_REQUEST_TIME)) {
            return new DateTime(sharedPrefs.getLong(KEY_MOST_RECENT_REQUEST_TIME, 0));
        } else {
            return null;
        }
    }

    @Override
    public void saveMostRecentlySeenEventOriginTime(@Nullable DateTime dateTime) {
        if (dateTime == null) {
            sharedPrefs.edit().remove(KEY_MOST_RECENTLY_SEEN_TIME).apply();
        } else {
            sharedPrefs.edit().putLong(KEY_MOST_RECENTLY_SEEN_TIME, dateTime.getMillis()).apply();
        }
    }

    @Nullable
    @Override
    public DateTime getMostRecentlySeenEventOriginTime() {
        if (sharedPrefs.contains(KEY_MOST_RECENTLY_SEEN_TIME)) {
            return new DateTime(sharedPrefs.getLong(KEY_MOST_RECENTLY_SEEN_TIME, 0));
        } else {
            return null;
        }
    }
}
