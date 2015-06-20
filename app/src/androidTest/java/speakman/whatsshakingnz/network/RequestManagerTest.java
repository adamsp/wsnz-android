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

package speakman.whatsshakingnz.network;

import android.test.AndroidTestCase;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mockito.ArgumentCaptor;

import rx.Observable;
import speakman.whatsshakingnz.model.EarthquakeStore;
import speakman.whatsshakingnz.network.geonet.GeonetResponse;
import speakman.whatsshakingnz.network.geonet.GeonetService;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Adam on 15-06-07.
 */
public class RequestManagerTest extends AndroidTestCase {

    public void testRequestLastNDaysWhenNoMostRecentEventDateIsAvailable() {
        GeonetService service = mock(GeonetService.class);
        EarthquakeStore store = new EarthquakeStore();
        RequestTimeStore timeStore = mock(RequestTimeStore.class);
        RequestManager mgr = new RequestManager(store, service, timeStore);

        when(timeStore.getMostRecentRequestTime()).thenReturn(null);
        when(service.getEarthquakesSince(notNull(DateTime.class), eq(RequestManager.MAX_EVENTS_PER_REQUEST)))
                .thenReturn(Observable.<GeonetResponse>empty());

        mgr.retrieveNewEarthquakes();

        ArgumentCaptor<DateTime> argumentCaptor = ArgumentCaptor.forClass(DateTime.class);
        verify(service).getEarthquakesSince(argumentCaptor.capture(), eq(RequestManager.MAX_EVENTS_PER_REQUEST));
        LocalDate requestedDate = argumentCaptor.getValue().toLocalDate();
        LocalDate today = DateTime.now().toLocalDate();
        // We want to ensure that the request was for DAYS_BEFORE_TODAY days ago.
        assertEquals(RequestManager.DAYS_BEFORE_TODAY, Days.daysBetween(requestedDate, today).getDays());
    }

    public void testRequestOnlyEventsSinceLastEventDate() {

    }

    public void testAllEventsAreProvidedToStore() {

    }

    public void testSpecificEventIsRetrieved() { // We can request updated/detailed info on a specific event, maybe?

    }

    public void testAllPagedEventsAreRetrieved() {

    }

    public void testLastEventTimeIsStored() {

    }

    public void testLastEventTimeIsNotUpdatedWhenFirstPageFails() {

    }

    public void testLastEventTimeIsStoredWhenASubsequentPageFails() {

    }

}
