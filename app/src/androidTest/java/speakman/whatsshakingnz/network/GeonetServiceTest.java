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

import java.io.IOException;
import java.util.List;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import rx.Observer;
import rx.functions.Action1;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.network.geonet.GeonetService;
import speakman.whatsshakingnz.utils.DateTimeFormatters;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Adam on 15-06-07.
 */
public class GeonetServiceTest extends AndroidTestCase {

    private MockWebServer mockedWebServer;
    private RequestTimeStore mockedRequestTimeStore;
    private NetworkModule networkModule;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
        mockedWebServer = new MockWebServer();
        networkModule = new NetworkModule();
        mockedRequestTimeStore = mock(RequestTimeStore.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mockedWebServer.shutdown();
    }

    public void testRequestLastNDaysWhenNoMostRecentEventDateIsAvailable() throws InterruptedException, IOException {
        when(mockedRequestTimeStore.getMostRecentUpdateTime()).thenReturn(null);

        mockedWebServer.enqueue(new MockResponse().setBody("{\"features\":[]}"));
        mockedWebServer.start();

        GeonetService mgr = new GeonetService(networkModule.provideOkHttp(), networkModule.provideGson(), mockedRequestTimeStore, mockedWebServer.url("/").toString());
        mgr.retrieveNewEarthquakes().subscribe();

        RecordedRequest recordedRequest = mockedWebServer.takeRequest();
        String path = recordedRequest.getPath();

        DateTime now = DateTime.now();
        LocalDate today = now.toLocalDate();
        String modificationTime = "modificationtime%3E";
        int startIndex = path.indexOf(modificationTime) + modificationTime.length();
        int endIndex =  startIndex + now.toString(DateTimeFormatters.requestQueryUpdateTimeFormatter).length();
        String argumentDate = path.substring(startIndex, endIndex);
        LocalDate requestedDate = new DateTime(argumentDate).toLocalDate();

        // We want to ensure that the request was for DAYS_BEFORE_TODAY days ago.
        int expected = GeonetService.DAYS_BEFORE_TODAY;
        int actual = Days.daysBetween(requestedDate, today).getDays();
        assertEquals(expected, actual);
        // And also that there was only 1 request
        assertEquals(1, mockedWebServer.getRequestCount());
    }

    public void testRequestOnlyEventsSinceLastEventDate() throws InterruptedException, IOException {
        mockedWebServer.enqueue(new MockResponse().setBody("{\"features\":[]}"));
        mockedWebServer.start();

        GeonetService mgr = new GeonetService(networkModule.provideOkHttp(), networkModule.provideGson(), mockedRequestTimeStore, mockedWebServer.url("/").toString());

        // This is our 'most recently seeen event time' and it should match modification time
        DateTime mostRecentUpdateTime = new DateTime();
        String expectedRequestTime = mostRecentUpdateTime.toString(DateTimeFormatters.requestQueryUpdateTimeFormatter);
        when(mockedRequestTimeStore.getMostRecentUpdateTime()).thenReturn(mostRecentUpdateTime);
        mgr.retrieveNewEarthquakes().subscribe();

        RecordedRequest recordedRequest = mockedWebServer.takeRequest();
        String path = recordedRequest.getPath();

        String modificationTime = "modificationtime%3E";
        int startIndex = path.indexOf(modificationTime) + modificationTime.length();
        int endIndex =  startIndex + expectedRequestTime.length();
        String actualRequestTime = path.substring(startIndex, endIndex);

        assertEquals(expectedRequestTime, actualRequestTime);
        assertEquals(1, mockedWebServer.getRequestCount());
    }

    public void testAllEventsAreProvidedToObserver() throws InterruptedException, IOException {
        final int eventCount = 20;
        String events = "";
        String event = "{\"properties\":{\"origintime\":\"2016-10-29T16:43:52.429Z\",\"modificationtime\":\"2016-10-29T19:26:10.183Z\"}}";
        for (int i = 0; i < eventCount - 1; i++) {
            events += event;
            events += ",";
        }
        events += event; // one without comma
        DateTime mostRecentUpdateTime = new DateTime();
        when(mockedRequestTimeStore.getMostRecentUpdateTime()).thenReturn(mostRecentUpdateTime);

        mockedWebServer.enqueue(new MockResponse().setBody("{\"features\":[" + events + "]}"));
        mockedWebServer.start();

        GeonetService mgr = new GeonetService(networkModule.provideOkHttp(), networkModule.provideGson(), mockedRequestTimeStore, mockedWebServer.url("/").toString());
        mgr.retrieveNewEarthquakes().toList().subscribe(new Action1<List<Earthquake>>() {
            @Override
            public void call(List<Earthquake> earthquakes) {
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

        final int eventCount = GeonetService.MAX_EVENTS_PER_REQUEST;
        String event1 = "{\"properties\":{\"origintime\":\"2016-10-29T16:43:52.429Z\",\"modificationtime\":\"2016-10-29T19:26:10.183Z\"}}";
        String event2 = "{\"properties\":{\"origintime\":\"2016-10-30T16:43:52.429Z\",\"modificationtime\":\"2016-10-30T19:26:10.183Z\"}}";
        String event3 = "{\"properties\":{\"origintime\":\"2016-10-31T16:43:52.429Z\",\"modificationtime\":\"2016-10-31T19:26:10.183Z\"}}";
        String events1 = "";
        String events2 = "";
        String events3 = "";
        for (int i = 0; i < eventCount - 1; i++) {
            events1 += event1;
            events1 += ",";
            events2 += event2;
            events2 += ",";
            events3 += event3;
            events3 += ",";
        }
        events1 += event1;
        events2 += event2;
        events3 += event3;
        mockedWebServer.enqueue(new MockResponse().setBody("{\"features\":[" + events1 + "]}"));
        mockedWebServer.enqueue(new MockResponse().setBody("{\"features\":[" + events2 + "]}"));
        mockedWebServer.enqueue(new MockResponse().setBody("{\"features\":[" + events3 + "]}"));
        mockedWebServer.enqueue(new MockResponse().setBody("{\"features\":[]}")); // 'empty' response

        DateTime mostRecentRequestTime = new DateTime("2016-10-28T16:43:52.429Z"); // Day before our fake response events
        timeStore.saveMostRecentUpdateTime(mostRecentRequestTime);

        GeonetService mgr = new GeonetService(networkModule.provideOkHttp(), networkModule.provideGson(), timeStore, mockedWebServer.url("/").toString());
        mgr.retrieveNewEarthquakes().toList().subscribe(new Action1<List<Earthquake>>() {
            @Override
            public void call(List<Earthquake> earthquakes) {
                assertEquals(eventCount * 3, earthquakes.size()); // 3 pages of max event count per page.
            }
        });

        assertTrue(mockedWebServer.takeRequest().getPath().contains("modificationtime%3E2016-10-28")); // Most recent previous saved date
        assertTrue(mockedWebServer.takeRequest().getPath().contains("modificationtime%3E2016-10-29"));
        assertTrue(mockedWebServer.takeRequest().getPath().contains("modificationtime%3E2016-10-30"));
        assertTrue(mockedWebServer.takeRequest().getPath().contains("modificationtime%3E2016-10-31"));
        assertEquals(4, mockedWebServer.getRequestCount());
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

        final int eventCount = GeonetService.MAX_EVENTS_PER_REQUEST;
        String event = "{\"properties\":{\"origintime\":\"2016-10-29T16:43:52.429Z\",\"modificationtime\":\"2016-10-29T19:26:10.183Z\"}}";
        String events = "";
        for (int i = 0; i < eventCount - 1; i++) {
            events += event;
            events += ",";
        }
        events += event;
        mockedWebServer.enqueue(new MockResponse().setBody("{\"features\":[" + events + "]}"));
        mockedWebServer.enqueue(new MockResponse().setResponseCode(422));

        DateTime mostRecentRequestTime = new DateTime("2016-10-28T16:43:52.429Z"); // Day before our fake response events
        timeStore.saveMostRecentUpdateTime(mostRecentRequestTime);

        GeonetService mgr = new GeonetService(networkModule.provideOkHttp(), networkModule.provideGson(), timeStore, mockedWebServer.url("/").toString());
        //noinspection unchecked
        Observer<Earthquake> mockedObserver = mock(Observer.class);
        mgr.retrieveNewEarthquakes().subscribe(mockedObserver);

        verify(mockedObserver, times(GeonetService.MAX_EVENTS_PER_REQUEST)).onNext(any(Earthquake.class));
        verify(mockedObserver).onError(any(Throwable.class));
        assertEquals(2, mockedWebServer.getRequestCount());

        DateTime lastTimeFirstPage = new DateTime("2016-10-29T19:26:10.183Z");
        assertEquals(lastTimeFirstPage, timeStore.getMostRecentUpdateTime());
    }

    public void testDuplicatedPreviousPageEventIsNotObservedTwice() throws InterruptedException {
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

        final int eventCount = GeonetService.MAX_EVENTS_PER_REQUEST;
        String event1 = "{\"properties\":{\"origintime\":\"2016-10-29T16:43:52.429Z\",\"modificationtime\":\"2016-10-29T19:26:10.183Z\"}}";
        String event2 = "{\"properties\":{\"origintime\":\"2016-10-30T16:43:52.429Z\",\"modificationtime\":\"2016-10-30T19:26:10.183Z\"}}";
        String events1 = event1;
        String events2 = event1; // Same as first item on previous page!
        for (int i = 0; i < eventCount - 1; i++) {
            events1 += ",";
            events1 += event1;
            events2 += ",";
            events2 += event2;
        }

        mockedWebServer.enqueue(new MockResponse().setBody("{\"features\":[" + events1 + "]}"));
        // This second page has one event which is the exact same as the last event the on previous page.
        // This test works around this Geonet bug https://github.com/GeoNet/help/issues/5
        mockedWebServer.enqueue(new MockResponse().setBody("{\"features\":[" + events2 + "]}"));
        mockedWebServer.enqueue(new MockResponse().setBody("{\"features\":[" + event2 + "]}"));

        DateTime mostRecentRequestTime = new DateTime("2016-10-28T16:43:52.429Z"); // Day before our fake response events
        timeStore.saveMostRecentUpdateTime(mostRecentRequestTime);

        GeonetService mgr = new GeonetService(networkModule.provideOkHttp(), networkModule.provideGson(), timeStore, mockedWebServer.url("/").toString());
        mgr.retrieveNewEarthquakes().toList().subscribe(new Action1<List<Earthquake>>() {
            @Override
            public void call(List<Earthquake> earthquakes) {
                assertEquals((eventCount * 2) - 1, earthquakes.size()); // Subtract 1 for our ignored item from previous page.
            }
        });

        assertTrue(mockedWebServer.takeRequest().getPath().contains("modificationtime%3E2016-10-28"));
        assertTrue(mockedWebServer.takeRequest().getPath().contains("modificationtime%3E2016-10-29"));
        assertTrue(mockedWebServer.takeRequest().getPath().contains("modificationtime%3E2016-10-30"));
        assertEquals(3, mockedWebServer.getRequestCount());

        DateTime lastTimeFirstPage = new DateTime("2016-10-30T19:26:10.183Z");
        assertEquals(lastTimeFirstPage, timeStore.getMostRecentUpdateTime());
    }

    public void testDuplicatedFinalPageEventIsNotObservedTwice() throws InterruptedException {
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

        final int eventCount = GeonetService.MAX_EVENTS_PER_REQUEST;
        String event = "{\"properties\":{\"origintime\":\"2016-10-29T16:43:52.429Z\",\"modificationtime\":\"2016-10-29T19:26:10.183Z\"}}";
        String events = "";
        for (int i = 0; i < eventCount - 1; i++) {
            events += event;
            events += ",";
        }
        events += event;
        mockedWebServer.enqueue(new MockResponse().setBody("{\"features\":[" + events + "]}"));
        // This second page has one event,  which is the exact same as the last event the on previous page.
        // This test works around this Geonet bug https://github.com/GeoNet/help/issues/5
        mockedWebServer.enqueue(new MockResponse().setBody("{\"features\":[" + event + "]}"));

        DateTime mostRecentRequestTime = new DateTime("2016-10-28T16:43:52.429Z"); // Day before our fake response events
        timeStore.saveMostRecentUpdateTime(mostRecentRequestTime);

        GeonetService mgr = new GeonetService(networkModule.provideOkHttp(), networkModule.provideGson(), timeStore, mockedWebServer.url("/").toString());
        mgr.retrieveNewEarthquakes().toList().subscribe(new Action1<List<Earthquake>>() {
            @Override
            public void call(List<Earthquake> earthquakes) {
                assertEquals(eventCount, earthquakes.size()); // 1 page only!
            }
        });

        assertTrue(mockedWebServer.takeRequest().getPath().contains("modificationtime%3E2016-10-28"));
        assertTrue(mockedWebServer.takeRequest().getPath().contains("modificationtime%3E2016-10-29"));
        assertEquals(2, mockedWebServer.getRequestCount());

        DateTime lastTimeFirstPage = new DateTime("2016-10-29T19:26:10.183Z");
        assertEquals(lastTimeFirstPage, timeStore.getMostRecentUpdateTime());
    }
}
