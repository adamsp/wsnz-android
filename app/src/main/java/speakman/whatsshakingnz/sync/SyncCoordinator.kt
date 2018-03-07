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
import rx.Producer
import rx.Subscriber
import rx.internal.operators.BackpressureUtils
import speakman.whatsshakingnz.model.realm.RealmEarthquake
import speakman.whatsshakingnz.network.EarthquakeService
import java.util.*
import javax.inject.Provider

class SyncCoordinator(private val realmProvider: Provider<Realm>, private val earthquakeService: EarthquakeService) {

    private var currentSync: Observable<List<RealmEarthquake>>? = null

    private val lock = Object()

    fun performSync(): Completable {
        synchronized(lock) {
            if (currentSync == null) {
                currentSync = earthquakeService.retrieveNewEarthquakes()
                        .map { RealmEarthquake(it) }
                        // buffering 500 should mean we don't hammer storage, but we also shouldn't fill our memory space.
                        .lift(OperatorBufferDelayError<RealmEarthquake>(500))
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

/**
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Sourced from Rx 1.3.6 linked below. Modified to remove 'skip' option and always emit the buffer
 * before handling an onError notification.
 * https://github.com/ReactiveX/RxJava/blob/6e6c5143afec6cdddf72e4e890a02a683ea14ba3/src/main/java/rx/internal/operators/OperatorBufferWithSize.java
 */
private class OperatorBufferDelayError<T>(internal val count: Int) : Observable.Operator<List<T>, T> {

    init {
        if (count <= 0) {
            throw IllegalArgumentException("count must be greater than 0")
        }
    }

    override fun call(child: Subscriber<in List<T>>): Subscriber<in T> {
        val parent = BufferExact(child, count)

        child.add(parent)
        child.setProducer(parent.createProducer())

        return parent
    }

    internal class BufferExact<T>(val actual: Subscriber<in List<T>>, val count: Int) : Subscriber<T>() {

        var buffer: MutableList<T>? = null

        init {
            this.request(0L)
        }

        override fun onNext(t: T) {
            var b: MutableList<T>? = buffer
            if (b == null) {
                b = ArrayList(count)
                buffer = b
            }

            b.add(t)

            if (b.size == count) {
                buffer = null
                actual.onNext(b)
            }
        }

        override fun onError(e: Throwable) {
            val b = buffer
            buffer = null
            if (b != null) {
                actual.onNext(b)
            }
            actual.onError(e)
        }

        override fun onCompleted() {
            val b = buffer
            if (b != null) {
                actual.onNext(b)
            }
            actual.onCompleted()
        }

        fun createProducer(): Producer {
            return Producer { n ->
                if (n < 0L) {
                    throw IllegalArgumentException("n >= required but it was $n")
                }
                if (n != 0L) {
                    val u = BackpressureUtils.multiplyCap(n, count.toLong())
                    this@BufferExact.request(u)
                }
            }
        }
    }
}