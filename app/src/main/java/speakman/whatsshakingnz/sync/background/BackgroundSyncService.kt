/*
 * Copyright 2018 Adam Speakman
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

package speakman.whatsshakingnz.sync.background

import android.content.Context
import com.firebase.jobdispatcher.*
import rx.Subscription
import speakman.whatsshakingnz.WhatsShakingApplication
import speakman.whatsshakingnz.sync.SyncCoordinator
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BackgroundSyncService : JobService() {

    @Inject
    lateinit var syncCoordinator: SyncCoordinator

    private var subscription: Subscription? = null

    init {
        WhatsShakingApplication.getInstance().inject(this)
    }

    override fun onStartJob(job: JobParameters?): Boolean {
        return job?.let {
            if (it.tag != PERIODIC_SYNC_TAG) {
                return false
            }
            startSync(it)
            return subscription != null // Non-null subscription --> still working
        } ?: false
    }

    override fun onStopJob(job: JobParameters?): Boolean {
        return subscription?.let {
            if (it.isUnsubscribed) {
                return false // Unsubscribed - don't retry
            } else {
                it.unsubscribe()
                return true // Still subscribed  - please retry
            }
        } ?: false // No sync in progress - don't retry
    }

    private fun startSync(job: JobParameters) {
        subscription?.unsubscribe()
        subscription = syncCoordinator.performSync()
                .doOnTerminate { subscription = null }
                .subscribe({ error ->
                    jobFinished(job, true)
                }, {
                    // onComplete
                    jobFinished(job, false)
                })
    }

    companion object {
        val SYNC_PERIOD_SECONDS = TimeUnit.HOURS.toSeconds(1).toInt()
        private val PERIODIC_SYNC_TAG = "speakman.whatsshakingnz.sync.background.BackgroundSyncService.PERIODIC_SYNC"

        @JvmStatic
        fun cancelSync(ctx: Context) {
            val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(ctx))
            dispatcher.cancel(PERIODIC_SYNC_TAG)
        }

        @JvmStatic
        fun scheduleSync(ctx: Context): Boolean {
            Timber.d("Scheduling Firebase sync.")
            val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(ctx))
            dispatcher.cancel(PERIODIC_SYNC_TAG) // Let's not schedule duplicate jobs.
            val job = dispatcher.newJobBuilder()
                    .setService(BackgroundSyncService::class.java)
                    .setTag(PERIODIC_SYNC_TAG)
                    .setReplaceCurrent(true)
                    .setLifetime(Lifetime.FOREVER)
                    .setConstraints(Constraint.ON_ANY_NETWORK)
                    .setTrigger(Trigger.executionWindow(SYNC_PERIOD_SECONDS, SYNC_PERIOD_SECONDS * 2))
                    .build()
            val result = dispatcher.schedule(job)
            if (result == FirebaseJobDispatcher.SCHEDULE_RESULT_SUCCESS) {
                return true
            } else {
                Timber.w("Scheduling Firebase sync failed with error code $result")
                return false
            }
        }
    }
}