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

import android.test.AndroidTestCase;

import speakman.whatsshakingnz.model.Earthquake;

/**
 * Created by Adam on 12/2/2015.
 */
public class EarthquakeDetailViewModelTests extends AndroidTestCase {

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

        EarthquakeDetailViewModel roundUpViewModel = new EarthquakeDetailViewModel(roundUpEarthquake);
        assertEquals("5.4", roundUpViewModel.getMagnitude());
    }

    public void testRoundedDownMagnitudeIsReturned() {
        Earthquake roundDownEarthquake = new TestEarthquake() {
            @Override
            public double getMagnitude() {
                return 5.34567;
            }
        };

        EarthquakeDetailViewModel roundDownViewModel = new EarthquakeDetailViewModel(roundDownEarthquake);
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

        EarthquakeDetailViewModel northViewModel = new EarthquakeDetailViewModel(northEarthquake);
        assertTrue(northViewModel.getLocation().contains("North"));
        assertTrue(northViewModel.getLocation().contains("Auckland"));
    }
}
