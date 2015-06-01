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

import retrofit.RestAdapter;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import speakman.whatsshakingnz.model.EarthquakeStore;
import speakman.whatsshakingnz.network.geonet.GeonetResponse;
import speakman.whatsshakingnz.network.geonet.GeonetService;

/**
 * Created by Adam on 15-05-31.
 */
public class RequestManager {

    private GeonetService service;
    private EarthquakeStore store;
    private Subscription subscription;

    public RequestManager(EarthquakeStore store) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://wfs.geonet.org.nz")
                .build();
        service = restAdapter.create(GeonetService.class);
        this.store = store;
    }

    public void retrieveNewEarthquakes() {
        if (subscription != null) return;
        subscription = service.getEarthquakes().observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<GeonetResponse>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(GeonetResponse geonetResponse) {
                store.setEarthquakes(geonetResponse.getFeatures());
            }
        });
    }
}
