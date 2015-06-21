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

import android.support.annotation.Nullable;
import android.test.AndroidTestCase;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mockito.ArgumentCaptor;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import speakman.whatsshakingnz.model.EarthquakeStore;
import speakman.whatsshakingnz.network.geonet.GeonetFeature;
import speakman.whatsshakingnz.network.geonet.GeonetResponse;
import speakman.whatsshakingnz.network.geonet.GeonetService;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by Adam on 15-06-07.
 */
public class RequestManagerTest extends AndroidTestCase {

    public void testRequestLastNDaysWhenNoMostRecentEventDateIsAvailable() throws InterruptedException {
        GeonetService service = mock(GeonetService.class);
        EarthquakeStore store = mock(EarthquakeStore.class);
        RequestTimeStore timeStore = mock(RequestTimeStore.class);
        RequestManager mgr = new RequestManager(store, service, timeStore);

        when(timeStore.getMostRecentRequestTime()).thenReturn(null);
        when(service.getEarthquakesSince(notNull(DateTime.class), eq(RequestManager.MAX_EVENTS_PER_REQUEST)))
                .thenReturn(Observable.<GeonetResponse>empty());

        mgr.retrieveNewEarthquakes();
        Thread.sleep(20);

        ArgumentCaptor<DateTime> argumentCaptor = ArgumentCaptor.forClass(DateTime.class);
        verify(service).getEarthquakesSince(argumentCaptor.capture(), eq(RequestManager.MAX_EVENTS_PER_REQUEST));
        verifyNoMoreInteractions(service);
        LocalDate requestedDate = argumentCaptor.getValue().toLocalDate();
        LocalDate today = DateTime.now().toLocalDate();
        // We want to ensure that the request was for DAYS_BEFORE_TODAY days ago.
        assertEquals(RequestManager.DAYS_BEFORE_TODAY, Days.daysBetween(requestedDate, today).getDays());
    }

    public void testRequestOnlyEventsSinceLastEventDate() throws InterruptedException {
        GeonetService service = mock(GeonetService.class);
        EarthquakeStore store = mock(EarthquakeStore.class);
        RequestTimeStore timeStore = mock(RequestTimeStore.class);
        RequestManager mgr = new RequestManager(store, service, timeStore);

        DateTime mostRecentRequestTime = new DateTime();

        when(timeStore.getMostRecentRequestTime()).thenReturn(mostRecentRequestTime);
        when(service.getEarthquakesSince(mostRecentRequestTime, RequestManager.MAX_EVENTS_PER_REQUEST))
                .thenReturn(Observable.<GeonetResponse>empty());

        mgr.retrieveNewEarthquakes();
        Thread.sleep(20);

        verify(service).getEarthquakesSince(mostRecentRequestTime, RequestManager.MAX_EVENTS_PER_REQUEST);
        verifyNoMoreInteractions(service);
    }

    public void testAllEventsAreProvidedToStore() throws InterruptedException {
        GeonetService service = mock(GeonetService.class);
        EarthquakeStore store = mock(EarthquakeStore.class);
        RequestTimeStore timeStore = mock(RequestTimeStore.class);
        RequestManager mgr = new RequestManager(store, service, timeStore);

        DateTime mostRecentRequestTime = new DateTime();

        int eventCount = 20;
        List<GeonetFeature> events = new ArrayList<>();
        for (int i = 0; i < eventCount; i++) {
            events.add(new GeonetFeature());
        }
        GeonetResponse response = new GeonetResponse(events);
        when(timeStore.getMostRecentRequestTime()).thenReturn(mostRecentRequestTime);
        when(service.getEarthquakesSince(mostRecentRequestTime, RequestManager.MAX_EVENTS_PER_REQUEST))
                .thenReturn(Observable.just(response));

        mgr.retrieveNewEarthquakes();
        Thread.sleep(20);

        verify(store).setEarthquakes(events);
        verifyNoMoreInteractions(store);
    }

    public void testAllPagedEventsAreRetrieved() throws InterruptedException {
        GeonetService service = mock(GeonetService.class);
        EarthquakeStore store = mock(EarthquakeStore.class);
        // Can't mock this as we need to update it each time it's set to allow paging to work.
        RequestTimeStore timeStore = new RequestTimeStore() {
            DateTime time;
            @Override
            public void saveMostRecentRequestTime(DateTime dateTime) {
                time = dateTime;
            }

            @Nullable
            @Override
            public DateTime getMostRecentRequestTime() {
                return time;
            }
        };
        RequestManager mgr = new RequestManager(store, service, timeStore);

        DateTime mostRecentRequestTime = new DateTime();
        timeStore.saveMostRecentRequestTime(mostRecentRequestTime);

        int eventCount = RequestManager.MAX_EVENTS_PER_REQUEST;
        List<GeonetFeature> events1 = new ArrayList<>();
        List<GeonetFeature> events2 = new ArrayList<>();
        List<GeonetFeature> events3 = new ArrayList<>();
        for (int i = 0; i < eventCount - 1; i++) {
            events1.add(new GeonetFeature());
            events2.add(new GeonetFeature());
            events3.add(new GeonetFeature());
        }
        GeonetFeature feature1 = new GeonetFeature();
        DateTime originTime1 = new DateTime().plusDays(1);
        feature1.setOriginTime(originTime1);
        events1.add(feature1);

        GeonetFeature feature2 = new GeonetFeature();
        DateTime originTime2 = new DateTime().plusDays(2);
        feature2.setOriginTime(originTime2);
        events2.add(feature2);

        GeonetFeature feature3 = new GeonetFeature();
        DateTime originTime3 = new DateTime().plusDays(3);
        feature3.setOriginTime(originTime3);
        events3.add(feature3);

        GeonetResponse page1 = new GeonetResponse(events1);
        GeonetResponse page2 = new GeonetResponse(events2);
        GeonetResponse page3 = new GeonetResponse(events3);

        when(service.getEarthquakesSince(mostRecentRequestTime, RequestManager.MAX_EVENTS_PER_REQUEST))
                .thenReturn(Observable.just(page1));
        when(service.getEarthquakesSince(originTime1, RequestManager.MAX_EVENTS_PER_REQUEST))
                .thenReturn(Observable.just(page2));
        when(service.getEarthquakesSince(originTime2, RequestManager.MAX_EVENTS_PER_REQUEST))
                .thenReturn(Observable.just(page3));
        when(service.getEarthquakesSince(originTime3, RequestManager.MAX_EVENTS_PER_REQUEST))
                .thenReturn(Observable.<GeonetResponse>empty());

        mgr.retrieveNewEarthquakes();
        Thread.sleep(20);

        verify(store).setEarthquakes(events1);
        verify(store).setEarthquakes(events2);
        verify(store).setEarthquakes(events3);
        verifyNoMoreInteractions(store);

        assertEquals(originTime3, timeStore.getMostRecentRequestTime());
    }

    public void testLastEventTimeIsUpdatedWhenFirstPageSucceedsButFollowingPageFails() throws InterruptedException {
        GeonetService service = mock(GeonetService.class);
        EarthquakeStore store = mock(EarthquakeStore.class);
        // Can't mock this as we need to update it each time it's set to allow paging to work.
        RequestTimeStore timeStore = new RequestTimeStore() {
            DateTime time;
            @Override
            public void saveMostRecentRequestTime(DateTime dateTime) {
                time = dateTime;
            }

            @Nullable
            @Override
            public DateTime getMostRecentRequestTime() {
                return time;
            }
        };
        RequestManager mgr = new RequestManager(store, service, timeStore);

        DateTime mostRecentRequestTime = new DateTime();
        timeStore.saveMostRecentRequestTime(mostRecentRequestTime);

        int eventCount = RequestManager.MAX_EVENTS_PER_REQUEST;
        List<GeonetFeature> events = new ArrayList<>();
        for (int i = 0; i < eventCount - 1; i++) {
            events.add(new GeonetFeature());
        }
        GeonetFeature feature = new GeonetFeature();
        DateTime lastTimeFirstPage = new DateTime().plusDays(1);
        feature.setOriginTime(lastTimeFirstPage);
        events.add(feature);
        GeonetResponse response = new GeonetResponse(events);

        // First page succeeds
        when(service.getEarthquakesSince(mostRecentRequestTime, RequestManager.MAX_EVENTS_PER_REQUEST))
                .thenReturn(Observable.just(response));
        // Second page fails
        when(service.getEarthquakesSince(lastTimeFirstPage, RequestManager.MAX_EVENTS_PER_REQUEST))
                .thenReturn(Observable.<GeonetResponse>error(new UnknownHostException()));

        mgr.retrieveNewEarthquakes();
        Thread.sleep(20);

        verify(store).setEarthquakes(events);
        verifyNoMoreInteractions(store);
        assertEquals(lastTimeFirstPage, timeStore.getMostRecentRequestTime());
    }

}
