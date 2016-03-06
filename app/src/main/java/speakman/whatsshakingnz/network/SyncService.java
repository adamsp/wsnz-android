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

/**
 * Created by Adam on 2016-03-05.
 */
public class SyncService extends GcmTaskService {

    public static void scheduleSync() {

    }

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        SyncService.scheduleSync();
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        NetworkRunnerService.requestLatest(this);
        return GcmNetworkManager.RESULT_SUCCESS;
    }
}
