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

package speakman.whatsshakingnz.ui.viewmodel;

import android.content.Context;
import android.content.res.Resources;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.TestEarthquake;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Adam on 12/12/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class EarthquakeOverviewViewModelTests {

    @Mock
    Resources resources;

    @Test
    public void testRoundedUpMagnitudeIsReturned() {
        Earthquake roundUpEarthquake = new TestEarthquake() {
            @Override
            public double getMagnitude() {
                return 5.35093;
            }
        };

        EarthquakeOverviewViewModel roundUpViewModel = new EarthquakeOverviewViewModel(roundUpEarthquake);
        assertEquals("5.4", roundUpViewModel.getMagnitude());
    }

    @Test
    public void testRoundedDownMagnitudeIsReturned() {
        Earthquake roundDownEarthquake = new TestEarthquake() {
            @Override
            public double getMagnitude() {
                return 5.34567;
            }
        };

        EarthquakeOverviewViewModel roundDownViewModel = new EarthquakeOverviewViewModel(roundDownEarthquake);
        assertEquals("5.3", roundDownViewModel.getMagnitude());
    }

    @Test
    public void testNorthOfAucklandIsReturned() {
        Earthquake northEarthquake = new TestEarthquake() {
            @Override
            public double getLatitude() {
                return -36.762916;
            }

            @Override
            public double getLongitude() {
                return 174.763351;
            }
        };

        EarthquakeOverviewViewModel northViewModel = new EarthquakeOverviewViewModel(northEarthquake);
        assertTrue(northViewModel.getNearestTownName().equals("Auckland"));

        northViewModel.getDistanceAndDirectionFromNearestTown(resources);
        verify(resources).getString(R.string.direction_north);
    }

    @Test
    public void testTimePassedValues() {
        final DateTime now = DateTime.now();
        Earthquake nowEarthquake = new TestEarthquake() {
            @Override
            public long getOriginTime() {
                return now.getMillis();
            }
        };

        Earthquake secondsEarthquake = new TestEarthquake() {
            @Override
            public long getOriginTime() {
                return now.getMillis() - 1000; // 1 second ago
            }
        };

        Earthquake minutesEarthquake = new TestEarthquake() {
            @Override
            public long getOriginTime() {
                return now.getMillis() - (60 * 1000); //1 minute ago
            }
        };

        Earthquake hoursEarthquake = new TestEarthquake() {
            @Override
            public long getOriginTime() {
                return now.getMillis() - (60 * 60 * 1000); //1 hour ago
            }
        };

        Earthquake daysEarthquake = new TestEarthquake() {
            @Override
            public long getOriginTime() {
                return now.getMillis() - (24 * 60 * 60 * 1000); // 1 day ago
            }
        };

        EarthquakeOverviewViewModel nowViewModel = new EarthquakeOverviewViewModel(nowEarthquake);
        when(resources.getString(R.string.overview_time_passed_now)).thenReturn("now");
        assertEquals("now", nowViewModel.getTimePassedSinceOccurrence(resources));

        EarthquakeOverviewViewModel secondsViewModel = new EarthquakeOverviewViewModel(secondsEarthquake);
        when(resources.getQuantityString(R.plurals.overview_time_passed_seconds, 1, 1L)).thenReturn("seconds");
        assertEquals("seconds", secondsViewModel.getTimePassedSinceOccurrence(resources));

        EarthquakeOverviewViewModel minutesViewModel = new EarthquakeOverviewViewModel(minutesEarthquake);
        when(resources.getQuantityString(R.plurals.overview_time_passed_minutes, 1, 1L)).thenReturn("minutes");
        assertEquals("minutes", minutesViewModel.getTimePassedSinceOccurrence(resources));

        EarthquakeOverviewViewModel hoursViewModel = new EarthquakeOverviewViewModel(hoursEarthquake);
        when(resources.getQuantityString(R.plurals.overview_time_passed_hours, 1, 1L)).thenReturn("hours");
        assertEquals("hours", hoursViewModel.getTimePassedSinceOccurrence(resources));

        EarthquakeOverviewViewModel daysViewModel = new EarthquakeOverviewViewModel(daysEarthquake);
        when(resources.getQuantityString(R.plurals.overview_time_passed_days, 1, 1L)).thenReturn("days");
        assertEquals("days", daysViewModel.getTimePassedSinceOccurrence(resources));
    }
}
