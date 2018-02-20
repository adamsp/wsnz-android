/*
 * Copyright 2016 Adam Speakman
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

package speakman.whatsshakingnz.network.geonet;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.network.EarthquakeService;
import speakman.whatsshakingnz.network.RequestTimeStore;
import speakman.whatsshakingnz.utils.DateTimeFormatters;
import timber.log.Timber;

/**
 * Created by Adam on 15-05-31.
 */
public class GeonetService implements EarthquakeService {

    // 200 events is ~16kb of JSON.
    public static final int MAX_EVENTS_PER_REQUEST = 200;
    public static final int DAYS_BEFORE_TODAY = 7;

    private class HttpStatusException extends Exception {
        final Response response;
        final int statusCode;

        public HttpStatusException(String message, int statusCode, Response response) {
            super(message);
            this.statusCode = statusCode;
            this.response = response;
        }
    }

    private class ResponseBodyReadException extends Exception {
        public ResponseBodyReadException(Throwable t) {
            super(t);
        }
    }

    /*
     * Documentation for the Geonet service we're using is available here:
     * http://info.geonet.org.nz/display/appdata/Advanced+Queries
     *
     * Sample complete request. Add "sortBy=modificationtime" for "oldest first" in order to enable our own version
     * of paging (desirable since we don't want to load a few thousand at once):
     * http://wfs.geonet.org.nz/geonet/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=geonet:quake_search_v1&outputFormat=json&cql_filter=modificationtime%3E=%272015-05-31T18:06:16.912Z+AND+eventtype=earthquake&maxFeatures=1
     */
    private final String endpoint;
    private static final String FILTER = "?service=WFS"
            + "&version=1.0.0"
            + "&request=GetFeature"
            + "&typeName=geonet:quake_search_v1"
            + "&outputFormat=json"
            + "&cql_filter=modificationtime>%s+AND+eventtype=earthquake"
            + "&maxFeatures=" + MAX_EVENTS_PER_REQUEST;
    // One day the Geonet service will work consistently. Until that day comes, we disable paging because it is frequently broken.
    // Instead we just take the top MAX_EVENTS most recent - users could potentially lose events, if they're offline for more than MAX_EVENTS.
//            + "&sortBy=modificationtime";

    private final RequestTimeStore timeStore;
    private final OkHttpClient client;
    private Gson gson;

    @Inject
    public GeonetService(OkHttpClient client, Gson gson, RequestTimeStore timeStore) {
        this(client, gson, timeStore, "http://wfs.geonet.org.nz/geonet/ows");
    }

    public GeonetService(OkHttpClient client, Gson gson, RequestTimeStore timeStore, String endpoint) {
        this.client = client;
        this.gson = gson;
        this.timeStore = timeStore;
        this.endpoint = endpoint + FILTER;
    }

    /**
     * Retrieves the latest events since the last-seen timestamp in the timestore supplied in the constructor.
     * <p>
     * The Geonet service has been a bit flaky, and potentially will supply the same event twice. There was a period
     * where we were able to page data, but that no longer works - the service doesn't support the query parameter
     * anymore. For this reason, it's possible that some events may be missed - specifically, if more than
     * {@link #MAX_EVENTS_PER_REQUEST} events have occurred since the last request. </p>
     * <p>
     * This method makes a best effort to de-duplicate events, but makes no guarantees - if an event is updated
     * while a request is in progress, it'll show up twice. </p>
     *
     * @return An Observable of new Earthquake events.
     */
    public Observable<Earthquake> retrieveNewEarthquakes() {
        return Observable.create(new Observable.OnSubscribe<Earthquake>() {
            @Override
            public void call(Subscriber<? super Earthquake> subscriber) {
                Request request = null;
                Response response = null;
                String json = null;
                try {
                    List<GeonetFeature> features;
                    do {
                        DateTime mostRecentUpdateTime = timeStore.getMostRecentUpdateTime();
                        if (mostRecentUpdateTime == null) {
                            mostRecentUpdateTime = DateTime.now().minusDays(DAYS_BEFORE_TODAY);
                        }

                        request = new Request.Builder().url(getRequestUrl(mostRecentUpdateTime)).build();
                        response = client.newCall(request).execute();
                        ResponseBody body = response.body();
                        try {
                            json = body.string();
                        } catch (IOException e) {
                            // We wrap this IOException so we don't confuse it with a network error
                            // (which is also an IOException).
                            throw new ResponseBodyReadException(e);
                        } finally {
                            body.close();
                        }
                        checkResponseWasSuccessfulOrThrow(response);
                        GeonetResponse geonetResponse = gson.fromJson(json, GeonetResponse.class);
                        features = geonetResponse.getFeatures();

                        long currentMostRecentUpdateTime = mostRecentUpdateTime.getMillis();
                        if (features.size() == 1 && features.get(0).getUpdatedTime() == currentMostRecentUpdateTime) {
                            // See https://github.com/GeoNet/help/issues/5 (the fix no longer works)
                            // The server sent us the last item from the previous page again - do nothing.
                        } else {
                            long newMostRecentUpdateTime = currentMostRecentUpdateTime;
                            for (GeonetFeature feature : features) {
                                String id = feature.getId();
                                if (id == null || id.isEmpty()) {
                                    continue; // TODO The code makes this look possible, but storage requires an ID.
                                }
                                long featureUpdateTime = feature.getUpdatedTime();
                                if (featureUpdateTime != currentMostRecentUpdateTime) {
                                    newMostRecentUpdateTime = Math.max(newMostRecentUpdateTime, featureUpdateTime);
                                    subscriber.onNext(feature);
                                }
                            }
                            if (features.size() > 0 && newMostRecentUpdateTime != currentMostRecentUpdateTime) {
                                timeStore.saveMostRecentUpdateTime(new DateTime(newMostRecentUpdateTime));
                            }
                        }
                    } while (features.size() >= MAX_EVENTS_PER_REQUEST);
                    subscriber.onCompleted();
                } catch (HttpStatusException e) { // Non-200 status
                    String info = buildLogMessageForFailure(request, response, json);
                    Timber.w(e, "Non-200 response from Geonet.\n%s", info);
                    subscriber.onError(e);
                } catch (IOException e) { // Network exception
                    String info = buildLogMessageForFailure(request, response, json);
                    Timber.i(e, "Network error while contacting Geonet.\n%s", info);
                    subscriber.onCompleted();
                } catch (JsonSyntaxException e) { // Deserialization exception
                    String info = buildLogMessageForFailure(request, response, json);
                    Timber.e(e, "Unexpected error deserializing Geonet response.\n%s", info);
                    subscriber.onError(e);
                } catch (ResponseBodyReadException e) { // Problem reading the response
                    String info = buildLogMessageForFailure(request, response, json);
                    Timber.e(e, "Unexpected error occurred while reading the body from the response.\n%s", info);
                    subscriber.onError(e);
                } catch (Exception e) { // Some other exception.
                    String info = buildLogMessageForFailure(request, response, json);
                    Timber.e(e, "Unexpected error occurred while retrieving updated Earthquakes.\n%s", info);
                    subscriber.onError(e);
                }
            }
        });
    }

    private String getRequestUrl(DateTime mostRecentUpdateTime) {
        // We want events that have been modified after the most recently seen update time.
        String eventsSince = mostRecentUpdateTime.toString(DateTimeFormatters.requestQueryUpdateTimeFormatter);
        return String.format(
                Locale.US,
                endpoint,
                eventsSince);
    }

    private String buildLogMessageForFailure(@Nullable Request request, @Nullable Response response, @Nullable String responseBody) {
        StringBuilder sb = new StringBuilder();
        if (request != null) {
            sb.append("Request:\n");
            sb.append(request);
        } else {
            sb.append("Request is null.");
        }
        if (response != null) {
            sb.append("\nResponse:\n");
            sb.append(response);
        } else {
            sb.append("\nResponse is null.");
        }
        if (responseBody != null) {
            sb.append("\nResponse body:\n");
            sb.append(responseBody);
        } else {
            sb.append("\nResponse body is null.");
        }
        return sb.toString();
    }

    private void checkResponseWasSuccessfulOrThrow(Response response) throws HttpStatusException {
        if (!response.isSuccessful()) {
            throw new HttpStatusException(String.format(Locale.US, "Non-200 Status received from Geonet (%d)", response.code()), response.code(), response);
        }
    }
}
