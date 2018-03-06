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

import dagger.Module
import dagger.Provides
import io.realm.Realm
import speakman.whatsshakingnz.dagger.AppScope
import speakman.whatsshakingnz.network.EarthquakeService
import javax.inject.Provider

@Module
class SyncModule {
    @AppScope
    @Provides
    fun provideSyncCoordinator(earthquakeService: EarthquakeService): SyncCoordinator {
        val realmProvider = object : Provider<Realm> {
            override fun get(): Realm {
                return Realm.getDefaultInstance()
            }
        }
        return SyncCoordinator(realmProvider, earthquakeService)
    }
}