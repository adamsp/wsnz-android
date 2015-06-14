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

import android.util.Log;

import org.joda.time.DateTime;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import speakman.whatsshakingnz.model.EarthquakeStore;
import speakman.whatsshakingnz.network.geonet.GeonetFeature;
import speakman.whatsshakingnz.network.geonet.GeonetResponse;
import speakman.whatsshakingnz.network.geonet.GeonetService;

/**
 * Created by Adam on 15-05-31.
 */
public class RequestManager {

    public static final int MAX_EVENTS_PER_REQUEST = 50;

    private final RequestTimeStore timeStore;
    private final GeonetService service;
    private final EarthquakeStore store;
    private Subscription subscription;

    @Inject
    public RequestManager(EarthquakeStore store, GeonetService service, RequestTimeStore timeStore) {
        this.service = service;
        this.store = store;
        this.timeStore = timeStore;
    }

    public void retrieveNewEarthquakes() {
        if (subscription != null) return;
        subscription = getMostRecentEventsObservable().subscribe(new Subscriber<GeonetResponse>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable e) {
                Log.e(RequestManager.class.getSimpleName(), "Error retrieving earthquakes", e);
            }

            @Override
            public void onNext(GeonetResponse geonetResponse) {
                List<GeonetFeature> features = geonetResponse.getFeatures();
                store.setEarthquakes(features);
                subscription.unsubscribe();
                subscription = null;
                if (features != null && features.size() > 0) {
                    GeonetFeature lastFeature = features.get(features.size() - 1);
                    timeStore.saveMostRecentRequestTime(lastFeature.getOriginTime());
                    if (features.size() == MAX_EVENTS_PER_REQUEST) {
                        // TODO Figure out a better paging solution
                        retrieveNewEarthquakes();
                    }
                }
            }
        });
    }

    private Observable<GeonetResponse> getMostRecentEventsObservable() {
        Observable<GeonetResponse> observable;
        DateTime mostRecentRequestTime = timeStore.getMostRecentRequestTime();
        if (mostRecentRequestTime == null) {
            observable = service.getEarthquakes(MAX_EVENTS_PER_REQUEST);
        } else {
            observable = service.getEarthquakesSince(mostRecentRequestTime, MAX_EVENTS_PER_REQUEST);
        }
        return observable.observeOn(AndroidSchedulers.mainThread());
    }
}
