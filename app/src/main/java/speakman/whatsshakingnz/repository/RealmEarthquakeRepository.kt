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

package speakman.whatsshakingnz.repository

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.realm.Realm
import io.realm.Sort
import io.realm.rx.RealmObservableFactory
import speakman.whatsshakingnz.model.Earthquake
import speakman.whatsshakingnz.model.realm.RealmEarthquake

class RealmEarthquakeRepository(private val realm: Realm) : EarthquakeRepository {

    val observableFactory: RealmObservableFactory

    init {
        observableFactory = RealmObservableFactory()
    }

    override fun earthquakeForId(id: String): Maybe<Earthquake> {
        return Maybe.create<Earthquake> {
            val earthquake = realm.where(RealmEarthquake::class.java).equalTo("id", id).findFirst()
            if (earthquake == null) {
                it.onComplete()
            } else {
                it.onSuccess(earthquake)
            }
        }
    }

    override fun allEarthquakes(): Flowable<List<Earthquake>> {
        return observableFactory.from(realm,
                realm.where(RealmEarthquake::class.java)
                        .sort(RealmEarthquake.FIELD_NAME_ORIGIN_TIME, Sort.DESCENDING).findAll())
                .map { it.toList() }
    }

    override fun earthquakesWithMagnitudeGreaterThan(magnitude: Double): Flowable<List<Earthquake>> {
        return observableFactory.from(realm,
                realm.where(RealmEarthquake::class.java)
                        .greaterThanOrEqualTo(RealmEarthquake.FIELD_NAME_MAGNITUDE, magnitude)
                        .sort(RealmEarthquake.FIELD_NAME_ORIGIN_TIME, Sort.DESCENDING).findAll())
                .map { it.toList() }
    }
}