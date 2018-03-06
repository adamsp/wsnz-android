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

import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.realm.Realm
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import rx.Observable
import rx.subjects.PublishSubject
import speakman.whatsshakingnz.ObservableTest
import speakman.whatsshakingnz.model.Earthquake
import speakman.whatsshakingnz.model.TestEarthquake
import speakman.whatsshakingnz.model.realm.RealmEarthquake
import speakman.whatsshakingnz.network.EarthquakeService
import javax.inject.Provider

@RunWith(PowerMockRunner::class)
@PrepareForTest(Realm::class)
class SyncCoordinatorTest : ObservableTest() {

    lateinit var realm: Realm

    @Mock
    lateinit var service: EarthquakeService

    val realmProvider: Provider<Realm>
        get() = object : Provider<Realm> {
            override fun get(): Realm {
                return realm
            }

        }

    lateinit var underTest: SyncCoordinator

    @Before
    fun setup() {
        realm = PowerMockito.mock(Realm::class.java)
        underTest = SyncCoordinator(realmProvider, service)
    }

    @Test
    fun testRequestingSync_goesToNetwork() {
        whenever(service.retrieveNewEarthquakes()).thenReturn(Observable.empty())
        testSubscribe(underTest.performSync())
        verify(service).retrieveNewEarthquakes()
    }

    @Test
    fun testSuccessfulDownload_earthquakesAreStored() {
        val earthquake1 = object : TestEarthquake() {
            override fun getId(): String {
                return "testid1"
            }
        }
        val earthquake2 = object : TestEarthquake() {
            override fun getId(): String {
                return "testid2"
            }
        }
        whenever(service.retrieveNewEarthquakes()).thenReturn(Observable.just(earthquake1, earthquake2))
        testSubscribe(underTest.performSync())
        val expected: Iterable<RealmEarthquake> = listOf(RealmEarthquake(earthquake1), RealmEarthquake(earthquake2))
        verify(realm).copyToRealmOrUpdate(eq(expected))
        verify(realm).close()
    }

    @Test
    fun testSuccessfulDownload_completableIsCompleted() {
        val earthquake = object : TestEarthquake() {
            override fun getId(): String {
                return "testid"
            }
        }
        whenever(service.retrieveNewEarthquakes()).thenReturn(Observable.just(earthquake))
        val subscription = testSubscribe(underTest.performSync())
        subscription.assertCompleted()
    }

    @Test
    fun testErrorWhileDownload_earthquakesStillStored() {
        val earthquake = object : TestEarthquake() {
            override fun getId(): String {
                return "testid"
            }
        }
        val expectedError = Exception("Test exception")
        val testObservable = PublishSubject.create<Earthquake>()

        whenever(service.retrieveNewEarthquakes()).thenReturn(testObservable)
        testObservable.onNext(earthquake)
        testObservable.onError(expectedError)

        testSubscribe(underTest.performSync())
        val expectedEarthquakes: Iterable<RealmEarthquake> = listOf(RealmEarthquake(earthquake))
        verify(realm).copyToRealmOrUpdate(eq(expectedEarthquakes))
        verify(realm).close()
    }

    @Test
    fun testErrorWhileDownload_completableOnErrorCalled() {
        val expectedError = Exception("Test exception")
        whenever(service.retrieveNewEarthquakes()).thenReturn(Observable.error(expectedError))
        val subscription = testSubscribe(underTest.performSync())
        subscription.assertError(expectedError)
    }
}