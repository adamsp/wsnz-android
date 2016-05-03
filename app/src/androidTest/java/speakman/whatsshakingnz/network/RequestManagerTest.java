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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.Subscriber;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.network.geonet.GeonetDateTimeAdapter;
import speakman.whatsshakingnz.network.geonet.GeonetFeature;
import speakman.whatsshakingnz.network.geonet.GeonetResponse;
import speakman.whatsshakingnz.network.geonet.GeonetService;
import speakman.whatsshakingnz.utils.DateTimeFormatters;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by Adam on 15-06-07.
 */
public class RequestManagerTest extends AndroidTestCase {

    private GeonetService mockedGeonetService;
    private RequestTimeStore mockedRequestTimeStore;

    private String updateTimeToFilterString(DateTime updateTime) {
        return String.format(GeonetService.FILTER_FORMAT_MOST_RECENT_UPDATE, updateTime.toString(DateTimeFormatters.requestQueryUpdateTimeFormatter));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
        mockedGeonetService = mock(GeonetService.class);
        mockedRequestTimeStore = mock(RequestTimeStore.class);
    }

    public void testRequestLastNDaysWhenNoMostRecentEventDateIsAvailable() throws InterruptedException {
        RequestManager mgr = new RequestManager(mockedGeonetService, mockedRequestTimeStore);

        when(mockedRequestTimeStore.getMostRecentUpdateTime()).thenReturn(null);
        when(mockedGeonetService.getEarthquakes(notNull(String.class), eq(RequestManager.MAX_EVENTS_PER_REQUEST)))
                .thenReturn(new GeonetResponse());

        mgr.retrieveNewEarthquakes().subscribe(new Subscriber<Earthquake>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable e) {
                fail("Should not fail retrieving earthquakes: " + e.getMessage());
            }

            @Override
            public void onNext(Earthquake earthquake) { }
        });
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockedGeonetService).getEarthquakes(argumentCaptor.capture(), eq(RequestManager.MAX_EVENTS_PER_REQUEST));
        verifyNoMoreInteractions(mockedGeonetService);

        DateTime now = DateTime.now();
        LocalDate today = now.toLocalDate();
        String argumentDate = argumentCaptor.getValue().substring("modificationtime>".length(), "modificationtime>".length()
                + now.toString(DateTimeFormatters.requestQueryUpdateTimeFormatter).length());
        LocalDate requestedDate = new DateTime(argumentDate).toLocalDate();
        // We want to ensure that the request was for DAYS_BEFORE_TODAY days ago.
        assertEquals(RequestManager.DAYS_BEFORE_TODAY, Days.daysBetween(requestedDate, today).getDays());
    }

    public void testRequestOnlyEventsSinceLastEventDate() throws InterruptedException {
        RequestManager mgr = new RequestManager(mockedGeonetService, mockedRequestTimeStore);
        DateTime mostRecentUpdateTime = new DateTime();
        String expectedFilter = updateTimeToFilterString(mostRecentUpdateTime);

        when(mockedRequestTimeStore.getMostRecentUpdateTime()).thenReturn(mostRecentUpdateTime);
        when(mockedGeonetService.getEarthquakes(expectedFilter, RequestManager.MAX_EVENTS_PER_REQUEST))
                .thenReturn(new GeonetResponse());

        mgr.retrieveNewEarthquakes().subscribe(new Subscriber<Earthquake>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable e) {
                fail("Should not fail retrieving earthquakes: " + e.getMessage());
            }

            @Override
            public void onNext(Earthquake earthquake) { }
        });

        // Verify our service was called with the expected filter applied.
        verify(mockedGeonetService).getEarthquakes(expectedFilter, RequestManager.MAX_EVENTS_PER_REQUEST);
        verifyNoMoreInteractions(mockedGeonetService);
    }

    public void testAllEventsAreProvidedToObserver() throws InterruptedException {
        RequestManager mgr = new RequestManager(mockedGeonetService, mockedRequestTimeStore);

        DateTime mostRecentUpdateTime = new DateTime();
        String filter = updateTimeToFilterString(mostRecentUpdateTime);

        final int eventCount = 20;
        List<GeonetFeature> events = new ArrayList<>();
        for (int i = 0; i < eventCount; i++) {
            events.add(new GeonetFeature());
        }
        GeonetResponse response = new GeonetResponse(events);
        when(mockedRequestTimeStore.getMostRecentUpdateTime()).thenReturn(mostRecentUpdateTime);
        when(mockedGeonetService.getEarthquakes(filter, RequestManager.MAX_EVENTS_PER_REQUEST))
                .thenReturn(response);

        mgr.retrieveNewEarthquakes().toList().subscribe(new Subscriber<List<Earthquake>>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable e) {
                fail("Should not fail retrieving earthquakes: " + e.getMessage());
            }

            @Override
            public void onNext(List<Earthquake> earthquakes) {
                assertEquals(eventCount, earthquakes.size());
            }
        });
    }

    public void testAllPagedEventsAreRetrieved() throws InterruptedException {
        // Easier to just implement this than to mock it in order to allow paging to work.
        RequestTimeStore timeStore = new RequestTimeStore() {
            DateTime time;
            @Override
            public void saveMostRecentUpdateTime(DateTime dateTime) {
                time = dateTime;
            }

            @Nullable
            @Override
            public DateTime getMostRecentUpdateTime() {
                return time;
            }
        };
        RequestManager mgr = new RequestManager(mockedGeonetService, timeStore);

        DateTime mostRecentRequestTime = new DateTime();
        timeStore.saveMostRecentUpdateTime(mostRecentRequestTime);

        final int eventCount = RequestManager.MAX_EVENTS_PER_REQUEST;
        List<GeonetFeature> events1 = new ArrayList<>();
        List<GeonetFeature> events2 = new ArrayList<>();
        List<GeonetFeature> events3 = new ArrayList<>();
        // We add 1 less than max so that we can manually set the final event.
        for (int i = 0; i < eventCount - 1; i++) {
            events1.add(new GeonetFeature());
            events2.add(new GeonetFeature());
            events3.add(new GeonetFeature());
        }
        Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new GeonetDateTimeAdapter()).create();
        GeonetFeature feature1 = gson.fromJson("{ \"properties\": { \"modificationtime\": \"2015-05-29T21:10:45.867Z\" } }", GeonetFeature.class);
        events1.add(feature1);
        DateTime modificationtime1 = new DateTime("2015-05-29T21:10:45.867Z");

        GeonetFeature feature2 = gson.fromJson("{ \"properties\": { \"modificationtime\": \"2015-05-30T21:10:45.867Z\" } }", GeonetFeature.class);
        events2.add(feature2);
        DateTime modificationtime2 = new DateTime("2015-05-30T21:10:45.867Z");

        GeonetFeature feature3 = gson.fromJson("{ \"properties\": { \"modificationtime\": \"2015-05-31T21:10:45.867Z\" } }", GeonetFeature.class);
        events3.add(feature3);
        DateTime modificationtime3 = new DateTime("2015-05-31T21:10:45.867Z");

        GeonetResponse page1 = new GeonetResponse(events1);
        GeonetResponse page2 = new GeonetResponse(events2);
        GeonetResponse page3 = new GeonetResponse(events3);
        when(mockedGeonetService.getEarthquakes(updateTimeToFilterString(mostRecentRequestTime), RequestManager.MAX_EVENTS_PER_REQUEST))
                .thenReturn(page1);
        when(mockedGeonetService.getEarthquakes(updateTimeToFilterString(modificationtime1), RequestManager.MAX_EVENTS_PER_REQUEST))
                .thenReturn(page2);
        when(mockedGeonetService.getEarthquakes(updateTimeToFilterString(modificationtime2), RequestManager.MAX_EVENTS_PER_REQUEST))
                .thenReturn(page3);
        when(mockedGeonetService.getEarthquakes(updateTimeToFilterString(modificationtime3), RequestManager.MAX_EVENTS_PER_REQUEST))
                .thenReturn(new GeonetResponse()); // empty response

        mgr.retrieveNewEarthquakes().toList().subscribe(new Subscriber<List<Earthquake>>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable e) {
                fail("Should not fail retrieving earthquakes: " + e.getMessage());
            }

            @Override
            public void onNext(List<Earthquake> earthquakes) {
                assertEquals(eventCount * 3, earthquakes.size()); // 3 pages of max event count per page.
            }
        });

        verify(mockedGeonetService).getEarthquakes(updateTimeToFilterString(mostRecentRequestTime), RequestManager.MAX_EVENTS_PER_REQUEST);
        verify(mockedGeonetService).getEarthquakes(updateTimeToFilterString(modificationtime1), RequestManager.MAX_EVENTS_PER_REQUEST);
        verify(mockedGeonetService).getEarthquakes(updateTimeToFilterString(modificationtime2), RequestManager.MAX_EVENTS_PER_REQUEST);
        verify(mockedGeonetService).getEarthquakes(updateTimeToFilterString(modificationtime3), RequestManager.MAX_EVENTS_PER_REQUEST);
        verifyNoMoreInteractions(mockedGeonetService);
        assertEquals(modificationtime3, timeStore.getMostRecentUpdateTime());
    }

    public void testLastEventTimeIsUpdatedWhenFirstPageSucceedsButFollowingPageFails() throws InterruptedException {
        // Easier to just implement this than to mock it in order to allow paging to work.
        RequestTimeStore timeStore = new RequestTimeStore() {
            DateTime time;
            @Override
            public void saveMostRecentUpdateTime(DateTime dateTime) {
                time = dateTime;
            }

            @Nullable
            @Override
            public DateTime getMostRecentUpdateTime() {
                return time;
            }
        };
        RequestManager mgr = new RequestManager(mockedGeonetService, timeStore);

        DateTime mostRecentRequestTime = new DateTime();
        timeStore.saveMostRecentUpdateTime(mostRecentRequestTime);

        int eventCount = RequestManager.MAX_EVENTS_PER_REQUEST;
        List<GeonetFeature> events = new ArrayList<>();
        for (int i = 0; i < eventCount - 1; i++) {
            events.add(new GeonetFeature());
        }

        Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new GeonetDateTimeAdapter()).create();
        GeonetFeature feature = gson.fromJson("{ \"properties\": { \"modificationtime\": \"2015-05-31T21:10:45.867Z\" } }", GeonetFeature.class);
        events.add(feature);
        DateTime lastTimeFirstPage = new DateTime("2015-05-31T21:10:45.867Z");
        GeonetResponse response = new GeonetResponse(events);

        // First page succeeds
        when(mockedGeonetService.getEarthquakes(updateTimeToFilterString(mostRecentRequestTime), RequestManager.MAX_EVENTS_PER_REQUEST))
                .thenReturn(response);
        // Second page fails
        when(mockedGeonetService.getEarthquakes(updateTimeToFilterString(lastTimeFirstPage), RequestManager.MAX_EVENTS_PER_REQUEST))
                .thenThrow(new RuntimeException());

        //noinspection unchecked
        Observer<Earthquake> mockedObserver = mock(Observer.class);
        mgr.retrieveNewEarthquakes().subscribe(mockedObserver);

        // Verify that our onError was called.
        verify(mockedObserver).onError(any(Throwable.class));
        verify(mockedObserver, times(RequestManager.MAX_EVENTS_PER_REQUEST)).onNext(any(Earthquake.class));
        verifyNoMoreInteractions(mockedObserver);
        // Verify that the "last time on the first page" is the one that is stored (so we can skip it next time!)
        assertEquals(lastTimeFirstPage, timeStore.getMostRecentUpdateTime());
    }
}
