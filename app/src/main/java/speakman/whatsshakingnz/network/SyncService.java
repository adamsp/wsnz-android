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

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import speakman.whatsshakingnz.backgroundsync.BackgroundSyncService;
import timber.log.Timber;

/**
 * Created by Adam on 2016-03-05.
 */
@Deprecated
/**
 * @deprecated Use {@link BackgroundSyncService}.
 */
public class SyncService extends GcmTaskService {

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        Timber.i("Switching from GcmTaskService to Firebase JobService on app upgrade.");
        // Re-schedule periodic task on app upgrade.
        BackgroundSyncService.scheduleSync(getApplicationContext());
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        Timber.w("GcmTaskService called! This should have been cancelled on app upgrade!");
        BackgroundSyncService.scheduleSync(getApplicationContext());
        //noinspection deprecation
        GcmNetworkManager.getInstance(this).cancelAllTasks(SyncService.class);
        return GcmNetworkManager.RESULT_SUCCESS;
    }
}
