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

import android.support.annotation.Nullable;

import org.joda.time.DateTime;
import org.junit.Test;

import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.TestEarthquake;
import speakman.whatsshakingnz.utils.DateTimeFormatters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Adam on 2/29/2016.
 */
public class EarthquakeExpandedDetailViewModelTests {

    @Test
    public void testNullDatesAreNotPrinted() {
        final DateTime now = DateTime.now();
        Earthquake nullDateTestEarthquake = new TestEarthquake() {
            @Override
            public long getOriginTime() {
                return now.getMillis();
            }

            @Override
            public long getUpdatedTime() {
                return 0; // Shouldn't be printed.
            }
        };
        EarthquakeExpandedDetailViewModel detailViewModel = new EarthquakeExpandedDetailViewModel(nullDateTestEarthquake);
        String detail = detailViewModel.getDetail().toString();
        assertTrue(detail.contains("Origin Time"));
        assertFalse(detail.contains("Updated Time"));
        assertFalse(detail.contains(new DateTime(0).toString()));
    }

    @Test
    public void testNullStringsAreNotPrinted() {
        Earthquake nullStringTestEarthquake = new TestEarthquake() {
            @Nullable
            @Override
            public String getEvaluationMethod() {
                return "Test eval method";
            }

            @Nullable
            @Override
            public String getEvaluationMode() {
                return null; // Shouldn't be printed
            }
        };
        EarthquakeExpandedDetailViewModel detailViewModel = new EarthquakeExpandedDetailViewModel(nullStringTestEarthquake);
        String detail = detailViewModel.getDetail().toString();
        assertTrue(detail.contains("Evaluation Method"));
        assertTrue(detail.contains("Test eval method"));
        assertFalse(detail.contains("Evaluation Mode"));
    }

    @Test
    public void testDatesAreFormattedCorrectly() {
        final DateTime now = DateTime.now();
        Earthquake dateTestEarthquake = new TestEarthquake() {
            @Override
            public long getOriginTime() {
                return now.getMillis();
            }
        };
        EarthquakeExpandedDetailViewModel detailViewModel = new EarthquakeExpandedDetailViewModel(dateTestEarthquake);
        String detail = detailViewModel.getDetail().toString();
        assertTrue(detail.contains("Origin Time"));
        assertTrue(detail.contains(DateTimeFormatters.mediumDateTimeDisplayFormat.format(now.toDate())));
    }
}
