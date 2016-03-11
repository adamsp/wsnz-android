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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import javax.inject.Inject;

import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.network.geonet.GeonetFeature;
import speakman.whatsshakingnz.network.geonet.GeonetResponse;
import speakman.whatsshakingnz.network.geonet.GeonetService;
import timber.log.Timber;

/**
 * Created by Adam on 15-05-31.
 */
public class RequestManager {

    /*
    This requires an extra "S" on the end (ie, 4 'milliseconds' values). This 4th place will
    always be populated with a 0. This is an unfortunate requirement of the API - if we don't
    supply this extra 0, it treats the 'greater than' as 'greater than or equal to', when
    requesting events with an updated time greater than the most recently seen event.
    This is discussed here: https://github.com/GeoNet/help/issues/5
     */
    static final DateTimeFormatter updateTimeFormatter =  DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSZ");
    public static final int MAX_EVENTS_PER_REQUEST = 50;
    public static final int DAYS_BEFORE_TODAY = 7;

    private final RequestTimeStore timeStore;
    private final GeonetService service;


    @Inject
    public RequestManager(GeonetService service, RequestTimeStore timeStore) {
        this.service = service;
        this.timeStore = timeStore;
    }

    public Observable<Earthquake> retrieveNewEarthquakes() {
        return Observable.create(new Observable.OnSubscribe<Earthquake>() {
            @Override
            public void call(Subscriber<? super Earthquake> subscriber) {
                String filter = null;
                try {
                    List<GeonetFeature> features;
                    do {
                        filter = getMostRecentEventsFilter();
                        GeonetResponse response = service.getEarthquakes(filter, MAX_EVENTS_PER_REQUEST);
                        features = response.getFeatures();
                        for (GeonetFeature feature : features) {
                            subscriber.onNext(feature);
                        }
                        if (features.size() > 0) {
                            GeonetFeature lastFeature = features.get(features.size() - 1);
                            timeStore.saveMostRecentUpdateTime(new DateTime(lastFeature.getUpdatedTime()));
                        }
                    } while (features.size() >= MAX_EVENTS_PER_REQUEST);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    if (e instanceof RetrofitError) {
                        switch (((RetrofitError) e).getKind()) {
                            case NETWORK: // IO Exception
                                Timber.d(e, "Network error while contacting Geonet.");
                                subscriber.onCompleted();
                                break;
                            case CONVERSION: // Deserialization exception
                                Timber.e(e, "Unexpected error deserializing Geonet response using filter [[ %s ]]", filter);
                                subscriber.onError(e);
                                break;
                            case HTTP: // Non-200 Status
                                Timber.w(e, "Non-200 response from Geonet using filter [[ %s ]]", filter);
                                subscriber.onError(e);
                                break;
                            case UNEXPECTED: // Internal error. Best practice is to re-throw so the app crashes.
                                Timber.e(e, "Unexpected internal error in Retrofit using filter [[ %s ]]. Crashing application.", filter);
                                throw e;
                        }
                    } else {
                        Timber.e(e, "Unexpected error occurred while retrieving updated Earthquakes, using filter [[ %s ]]", filter);
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

    private String getMostRecentEventsFilter() {
        DateTime mostRecentUpdateTime = timeStore.getMostRecentUpdateTime();
        if (mostRecentUpdateTime == null) {
            mostRecentUpdateTime = DateTime.now().minusDays(DAYS_BEFORE_TODAY);
        }
        return String.format(GeonetService.FILTER_FORMAT_MOST_RECENT_UPDATE, mostRecentUpdateTime.toString(updateTimeFormatter));
    }
}
