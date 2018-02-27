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
import com.nhaarman.mockito_kotlin.*
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import speakman.whatsshakingnz.model.Earthquake
import speakman.whatsshakingnz.model.TestEarthquake
import speakman.whatsshakingnz.network.NotificationTimeStore
import speakman.whatsshakingnz.utils.UserSettings

@RunWith(MockitoJUnitRunner::class)
class EarthquakeNotifierTest {

    lateinit var underTest: EarthquakeNotifier

    @Mock
    lateinit var notificationFactory: NotificationFactory
    @Mock
    lateinit var timeStore: NotificationTimeStore
    @Mock
    lateinit var userSettings: UserSettings
    @Mock
    lateinit var notificationManager: NotificationManager

    val mostRecentTime = DateTime()
    val minimumMagnitude = 4.0

    @Before
    fun setup() {
        // Setup defaults
        whenever(timeStore.mostRecentlySeenEventOriginTime).thenReturn(mostRecentTime)
        whenever(userSettings.minimumNotificationMagnitude()).thenReturn(minimumMagnitude)
        whenever(userSettings.notificationsEnabled()).thenReturn(true)
        underTest = EarthquakeNotifier(notificationFactory, timeStore, userSettings, notificationManager)
    }

    @Test
    fun testNotificationsDisabled_noNotificationsFired() {
        // Notifications disabled
        whenever(userSettings.notificationsEnabled()).thenReturn(false)
        val earthquakes = listOf(object : TestEarthquake() {
            override fun getOriginTime(): Long {
                return mostRecentTime.millis + 1000L
            }

            override fun getMagnitude(): Double {
                return minimumMagnitude + 1.0
            }
        })
        underTest.notifyForNewEarthquakes(earthquakes)
        verifyZeroInteractions(notificationFactory)
        verifyZeroInteractions(notificationManager)
    }

    @Test
    fun testEmptyList_noNotificationsFired() {
        val earthquakes = emptyList<Earthquake>() // Empty list of events
        underTest.notifyForNewEarthquakes(earthquakes)
        verifyZeroInteractions(notificationFactory)
        verifyZeroInteractions(notificationManager)
    }

    @Test
    fun testAlreadySeen_noNotificationsFired() {
        val earthquakes = listOf(object : TestEarthquake() {
            override fun getOriginTime(): Long {
                return mostRecentTime.millis - 1000L // event is before 'most recently seen'
            }

            override fun getMagnitude(): Double {
                return minimumMagnitude + 1.0
            }
        })
        underTest.notifyForNewEarthquakes(earthquakes)
        verifyZeroInteractions(notificationFactory)
        verifyZeroInteractions(notificationManager)
    }

    @Test
    fun testMinimumMagnitudeTooHigh_noNotificationsFired() {
        val earthquakes = listOf(object : TestEarthquake() {
            override fun getOriginTime(): Long {
                return mostRecentTime.millis + 1000L
            }

            override fun getMagnitude(): Double {
                return minimumMagnitude - 1.0 // magnitude below minimum
            }
        })
        underTest.notifyForNewEarthquakes(earthquakes)
        verifyZeroInteractions(notificationFactory)
        verifyZeroInteractions(notificationManager)
    }

    @Test
    fun testSingleEarthquakeValid_singleEarthquakeNotificationFired() {
        val earthquake = object : TestEarthquake() {
            override fun getOriginTime(): Long {
                return mostRecentTime.millis + 1000L
            }

            override fun getMagnitude(): Double {
                return minimumMagnitude + 1.0
            }
        }
        val earthquakes = listOf(earthquake)
        val notification = mock<Notification>()

        whenever(notificationFactory.notificationForSingleEarthquake(earthquake)).thenReturn(notification)

        underTest.notifyForNewEarthquakes(earthquakes)

        verify(notificationFactory).notificationForSingleEarthquake(earthquake)
        verify(notificationManager).notify(underTest.NOTIFICATION_ID, notification)
        verifyNoMoreInteractions(notificationFactory)
        verifyNoMoreInteractions(notificationManager)
    }

    @Test
    fun testMultipleEarthquakesValid_multipleEarthquakesNotificationFired() {
        val earthquakes = listOf(
                object : TestEarthquake() {
                    override fun getOriginTime(): Long {
                        return mostRecentTime.millis + 1000L
                    }

                    override fun getMagnitude(): Double {
                        return minimumMagnitude + 1.0
                    }
                },
                object : TestEarthquake() {
                    override fun getOriginTime(): Long {
                        return mostRecentTime.millis + 2000L
                    }

                    override fun getMagnitude(): Double {
                        return minimumMagnitude + 2.0
                    }
                })
        val notification = mock<Notification>()

        whenever(notificationFactory.notificationForMultipleEarthquakes(eq(earthquakes))).thenReturn(notification)

        underTest.notifyForNewEarthquakes(earthquakes)

        verify(notificationFactory).notificationForMultipleEarthquakes(earthquakes)
        verify(notificationManager).notify(underTest.NOTIFICATION_ID, notification)
        verifyNoMoreInteractions(notificationFactory)
        verifyNoMoreInteractions(notificationManager)
    }
}