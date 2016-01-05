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

package speakman.whatsshakingnz.ui.viewmodel;

import android.content.Context;
import android.test.AndroidTestCase;

import org.joda.time.DateTime;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.model.Earthquake;

/**
 * Created by Adam on 12/12/2015.
 */
public class EarthquakeOverviewViewModelTests extends AndroidTestCase {

    private static class TestEarthquake implements Earthquake {

        @Override
        public long getOriginTime() {
            return 0;
        }

        @Override
        public long getUpdatedTime() {
            return 0;
        }

        @Override
        public double getLatitude() {
            return 0;
        }

        @Override
        public double getLongitude() {
            return 0;
        }

        @Override
        public double getMagnitude() {
            return 0;
        }

        @Override
        public double getDepth() {
            return 0;
        }

        @Override
        public String getId() {
            return null;
        }
    }

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

    public void testNorthOfAucklandIsReturned() {
        Earthquake northEarthquake = new TestEarthquake() {
            @Override
            public double getLatitude() {
                return -36.762916;
            }

            @Override
            public double getLongitude() {
                return 174.815451;
            }
        };

        EarthquakeOverviewViewModel northViewModel = new EarthquakeOverviewViewModel(northEarthquake);
        assertTrue(northViewModel.getNearestTownName().equals("Auckland"));
        assertTrue(northViewModel.getDistanceAndDirectionFromNearestTown(getContext()).contains("North"));
    }

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

        Context ctx = getContext();

        EarthquakeOverviewViewModel nowViewModel = new EarthquakeOverviewViewModel(nowEarthquake);
        assertEquals(ctx.getString(R.string.overview_time_passed_now), nowViewModel.getTimePassedSinceOccurrence(ctx));

        EarthquakeOverviewViewModel secondsViewModel = new EarthquakeOverviewViewModel(secondsEarthquake);
        assertEquals(ctx.getResources().getQuantityString(R.plurals.overview_time_passed_seconds, 1, 1), secondsViewModel.getTimePassedSinceOccurrence(ctx));

        EarthquakeOverviewViewModel minutesViewModel = new EarthquakeOverviewViewModel(minutesEarthquake);
        assertEquals(ctx.getResources().getQuantityString(R.plurals.overview_time_passed_minutes, 1, 1), minutesViewModel.getTimePassedSinceOccurrence(ctx));

        EarthquakeOverviewViewModel hoursViewModel = new EarthquakeOverviewViewModel(hoursEarthquake);
        assertEquals(ctx.getResources().getQuantityString(R.plurals.overview_time_passed_hours, 1, 1), hoursViewModel.getTimePassedSinceOccurrence(ctx));

        EarthquakeOverviewViewModel daysViewModel = new EarthquakeOverviewViewModel(daysEarthquake);
        assertEquals(ctx.getResources().getQuantityString(R.plurals.overview_time_passed_days, 1, 1), daysViewModel.getTimePassedSinceOccurrence(ctx));
    }
}
