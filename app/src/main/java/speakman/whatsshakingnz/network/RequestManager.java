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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import speakman.whatsshakingnz.model.realm.RealmEarthquake;
import speakman.whatsshakingnz.network.geonet.GeonetFeature;
import speakman.whatsshakingnz.network.geonet.GeonetResponse;
import speakman.whatsshakingnz.network.geonet.GeonetService;

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
    private final Realm realm;
    private Subscription subscription;

    @Inject
    public RequestManager(Realm realm, GeonetService service, RequestTimeStore timeStore) {
        this.service = service;
        this.realm = realm;
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
                realm.beginTransaction();
                for (GeonetFeature feature : features) {
                    realm.copyToRealmOrUpdate(new RealmEarthquake(feature));
                }
                realm.commitTransaction();
                subscription.unsubscribe();
                subscription = null;
                if (features.size() > 0) {
                    GeonetFeature lastFeature = features.get(features.size() - 1);
                    timeStore.saveMostRecentUpdateTime(new DateTime(lastFeature.getUpdatedTime()));
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
        DateTime mostRecentUpdateTime = timeStore.getMostRecentUpdateTime();
        if (mostRecentUpdateTime == null) {
            mostRecentUpdateTime = DateTime.now().minusDays(DAYS_BEFORE_TODAY);
        }
        String filter = String.format(GeonetService.FILTER_FORMAT_MOST_RECENT_UPDATE, mostRecentUpdateTime.toString(updateTimeFormatter));
        observable = service.getEarthquakes(filter, MAX_EVENTS_PER_REQUEST);
        return observable.observeOn(AndroidSchedulers.mainThread());
    }
}
