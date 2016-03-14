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

import android.content.Context;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.TaskParams;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import speakman.whatsshakingnz.WhatsShakingApplication;
import speakman.whatsshakingnz.model.Earthquake;
import speakman.whatsshakingnz.model.realm.RealmEarthquake;
import speakman.whatsshakingnz.utils.UserSettings;
import timber.log.Timber;

/**
 * Created by Adam on 2016-03-05.
 */
public class SyncService extends GcmTaskService {

    public static final long SYNC_PERIOD_SECONDS = 60 * 60; // one hour
    public static final double MINIMUM_NOTIFICATION_MAGNITUDE = 4.0;

    private static final String PERIODIC_SYNC_TAG = "speakman.whatsshakingnz.network.SyncService.PERIODIC_SYNC";

    public static void scheduleSync(Context ctx) {
        Timber.d("Scheduling periodic sync.");
        GcmNetworkManager gcmNetworkManager = GcmNetworkManager.getInstance(ctx);
        PeriodicTask periodicTask = new PeriodicTask.Builder()
                .setPeriod(SYNC_PERIOD_SECONDS)
                .setRequiredNetwork(PeriodicTask.NETWORK_STATE_CONNECTED)
                .setTag(PERIODIC_SYNC_TAG)
                .setService(SyncService.class)
                .setPersisted(true)
                .setUpdateCurrent(true)
                .build();
        gcmNetworkManager.schedule(periodicTask);
    }

    @Inject
    RequestManager requestManager;

    public SyncService() {
        super();
        WhatsShakingApplication.getInstance().inject(this);
    }

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        Timber.i("Re-scheduling periodic sync due to app upgrade.");
        // Re-schedule periodic task on app upgrade.
        SyncService.scheduleSync(this);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        if (PERIODIC_SYNC_TAG.equals(taskParams.getTag())) {
            Timber.d("Requesting periodic sync.");
            requestNewEarthquakes();
        }
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    private void requestNewEarthquakes() {
        final List<RealmEarthquake> earthquakesToNotifyAbout = new ArrayList<>();
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
                realm.commitTransaction(); // We need to save everything that came through, even on error.
            }
        }).subscribe(new Action1<RealmEarthquake>() {
            @Override
            public void call(RealmEarthquake realmEarthquake) {
                realmEarthquake = realm.copyToRealmOrUpdate(realmEarthquake);
                if (realmEarthquake.getMagnitude() > MINIMUM_NOTIFICATION_MAGNITUDE) {
                    earthquakesToNotifyAbout.add(realmEarthquake);
                }
            }
        });
        realm.close();
        notifyUserAboutEarthquakes(earthquakesToNotifyAbout);
    }

    private void notifyUserAboutEarthquakes(List<? extends Earthquake> earthquakes) {
        UserSettings settings = new UserSettings(this);
        if (!settings.notificationsEnabled() || earthquakes.size() == 0) {
            return;
        }
        // Else notify
    }
}
