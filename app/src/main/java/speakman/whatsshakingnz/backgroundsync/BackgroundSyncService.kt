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

package speakman.whatsshakingnz.backgroundsync

import android.content.Context
import com.firebase.jobdispatcher.*
import io.realm.Realm
import rx.Observable
import speakman.whatsshakingnz.WhatsShakingApplication
import speakman.whatsshakingnz.model.realm.RealmEarthquake
import speakman.whatsshakingnz.network.EarthquakeService
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BackgroundSyncService : JobService() {

    @Inject
    lateinit var earthquakeService: EarthquakeService

    init {
        WhatsShakingApplication.getInstance().inject(this)
    }

    override fun onStartJob(job: JobParameters?): Boolean {
        if (job?.tag != PERIODIC_SYNC_TAG) {
            return false
        }
        startSync()
        return true
    }

    override fun onStopJob(job: JobParameters?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun startSync() {
        // TODO This should delegate retrieval + storage to some other, testable class
        // TODO That class should expose an API which enables retrieval with success/failure/cancel options (or maybe return downloaded results?...).
        // TODO This is important because we retrieve + store here _and_ from the UI - duplicating risky work, untested!
        // TODO Can Architecture Components help with accessing it at the UI level? A ViewModel so we don't re-query it every time, and exposing LiveData rather than RealmResults?
        earthquakeService.retrieveNewEarthquakes() // TODO This isn't async wtf...
                .map { RealmEarthquake(it) }
                .onErrorResumeNext(Observable.empty()) // Swallow it
                .toList()
                .subscribe({ earthquakes ->
                    val realm = Realm.getDefaultInstance()
                    realm.beginTransaction()
                    realm.copyToRealmOrUpdate(earthquakes)
                    realm.commitTransaction()
                    realm.close()
                })

    }

    companion object {
        val SYNC_PERIOD_SECONDS = TimeUnit.HOURS.toSeconds(1).toInt()
        private val PERIODIC_SYNC_TAG = "speakman.whatsshakingnz.backgroundsync.BackgroundSyncService.PERIODIC_SYNC"

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