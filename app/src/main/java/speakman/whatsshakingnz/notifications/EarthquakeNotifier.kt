/*
 * Copyright 2018 Adam Speakman
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

package speakman.whatsshakingnz.notifications

import android.app.Notification
import android.app.NotificationManager
import speakman.whatsshakingnz.analytics.Analytics
import speakman.whatsshakingnz.model.Earthquake
import speakman.whatsshakingnz.network.NotificationTimeStore
import speakman.whatsshakingnz.utils.UserSettings
import timber.log.Timber
import javax.inject.Inject

class EarthquakeNotifier @Inject constructor(val factory: NotificationFactory, val timeStore: NotificationTimeStore,
                         val userSettings: UserSettings, val notificationManager: NotificationManager) {
    fun notifyForNewEarthquakes(earthquakes: List<Earthquake>) {
        if (!userSettings.notificationsEnabled() || earthquakes.isEmpty()) {
            return
        }
        val mostRecentlySeenEventOriginTime = timeStore.mostRecentlySeenEventOriginTime?.millis ?: 0L
        val minimumNotificationMagnitude = userSettings.minimumNotificationMagnitude() // TODO Getters
        val filtered = earthquakes.filter { it.originTime > mostRecentlySeenEventOriginTime }
                .filter { it.magnitude >= minimumNotificationMagnitude }
        notifyUserAboutEarthquakes(filtered)
    }

    private fun notifyUserAboutEarthquakes(earthquakes: List<Earthquake>) {
        val notification: Notification
        if (earthquakes.size == 1) {
            val earthquake = earthquakes[0]
            notification = factory.notificationForSingleEarthquake(earthquake)
            Analytics.logNotificationShownForEarthquake(earthquake)
        } else {
            notification = factory.notificationForMultipleEarthquakes(earthquakes)
            Analytics.logNotificationShownForEarthquakes(earthquakes)
        }
        notificationManager.notify(NotificationUtil.NOTIFICATION_ID, notification)
        Timber.d("Showing notification for %d earthquakes", earthquakes.size)
    }
}