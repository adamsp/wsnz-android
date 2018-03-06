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

package speakman.whatsshakingnz.sync

import io.realm.Realm
import rx.Completable
import rx.Observable
import speakman.whatsshakingnz.model.realm.RealmEarthquake
import speakman.whatsshakingnz.network.EarthquakeService
import javax.inject.Provider

class SyncCoordinator(private val realmProvider: Provider<Realm>, private val earthquakeService: EarthquakeService) {

    private var currentSync: Observable<List<RealmEarthquake>>? = null

    private val lock = Object()

    fun performSync(): Completable {
        synchronized(lock) {
            if (currentSync == null) {
                currentSync = earthquakeService.retrieveNewEarthquakes()
                        .map { RealmEarthquake(it) }
                        // buffer(500) vs toList() should mean we don't hammer storage, but we also shouldn't fill our memory space.
                        .buffer(500) // TODO Docs used to say (incorrectly) that this would emit the buffer on error
                        .doOnEach { earthquakes ->
                            if (earthquakes.hasValue()) {
                                val realm = realmProvider.get()
                                realm.beginTransaction()
                                @Suppress("UNCHECKED_CAST")
                                realm.copyToRealmOrUpdate(earthquakes.value as List<RealmEarthquake>)
                                realm.commitTransaction()
                                realm.close()
                            }
                        }
                        .publish()
                        .autoConnect()
                        .doOnTerminate {
                            synchronized(lock) {
                                // This could run before the fromObservable below is called, in case of immediate error.
                                currentSync = null
                            }
                        }
            }
            // We could have a null current sync here even with the lock, as we could have immediate
            // failure on the same thread - so the lock doesn't have to be released.
            return if (currentSync == null) Completable.complete() else Completable.fromObservable(currentSync)
        }
    }
}