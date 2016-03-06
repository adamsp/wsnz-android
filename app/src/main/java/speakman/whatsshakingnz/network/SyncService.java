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

import timber.log.Timber;

/**
 * Created by Adam on 2016-03-05.
 */
public class SyncService extends GcmTaskService {

    public static long SYNC_PERIOD_SECONDS = 60 * 60; // one hour

    private static String PERIODIC_SYNC_TAG = "speakman.whatsshakingnz.network.SyncService.PERIODIC_SYNC";

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
            NetworkRunnerService.requestLatest(this);
        }
        return GcmNetworkManager.RESULT_SUCCESS;
    }
}
