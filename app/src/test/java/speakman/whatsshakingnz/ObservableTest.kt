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

package speakman.whatsshakingnz

import org.junit.After
import rx.Completable
import rx.Observable
import rx.observers.TestSubscriber
import rx.subscriptions.CompositeSubscription

abstract class ObservableTest {

    private val subscriptions = CompositeSubscription()

    /**
     * Subscribes to the supplied Completable using a [TestSubscriber] which you can query for completion,
     * errors, etc. This is also automatically unsubscribed from when your test has finished.
     */
    protected fun testSubscribe(completable: Completable): TestSubscriber<Void> {
        val subscriber = TestSubscriber<Void>()
        subscriptions.add(completable.toObservable<Void>().subscribe(subscriber))
        return subscriber
    }

    /**
     * Subscribes to the supplied Observable using a [TestSubscriber] which you can query for completion,
     * emissions, errors, etc. This is also automatically unsubscribed from when your test has finished.
     */
    protected fun <T> testSubscribe(observable: Observable<T>): TestSubscriber<T> {
        val subscriber = TestSubscriber<T>()
        subscriptions.add(observable.subscribe(subscriber))
        return subscriber
    }

    @After
    fun tearDown_Subscriptions() {
        subscriptions.clear()
    }
}