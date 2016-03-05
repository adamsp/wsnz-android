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

package speakman.whatsshakingnz.network;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import io.realm.Realm;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import speakman.whatsshakingnz.WhatsShakingApplication;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.realm.RealmEarthquake;

/**
 * Created by Adam on 2/13/2016.
 */
public class NetworkRunnerService extends IntentService {

    public static void requestLatest(Context ctx) {
        ctx.startService(new Intent(ctx, NetworkRunnerService.class));
    }

    @Inject
    RequestManager requestManager;

    public NetworkRunnerService() {
        super("What's Shaking Network Runner");
        WhatsShakingApplication.getInstance().inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Realm realm = Realm.getDefaultInstance();
        requestManager.retrieveNewEarthquakes().map(new Func1<Earthquake, RealmEarthquake>() {
            @Override
            public RealmEarthquake call(Earthquake earthquake) {
                return new RealmEarthquake(earthquake);
            }
        }).doOnSubscribe(new Action0() {
            @Override
            public void call() {
                realm.beginTransaction();
            }
        }).doOnCompleted(new Action0() {
            @Override
            public void call() {
                realm.commitTransaction();
            }
        }).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                realm.cancelTransaction();
            }
        }).subscribe(new Action1<RealmEarthquake>() {
            @Override
            public void call(RealmEarthquake realmEarthquake) {
                realm.copyToRealmOrUpdate(realmEarthquake);
            }
        });
        realm.close();
    }
}
