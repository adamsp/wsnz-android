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

package speakman.whatsshakingnz.notifications

import android.app.NotificationManager
import android.content.Context
import dagger.Module
import dagger.Provides
import speakman.whatsshakingnz.InternalPreferences
import speakman.whatsshakingnz.network.NotificationTimeStore
import speakman.whatsshakingnz.utils.UserSettings

@Module
class NotificationModule {

    @Provides
    fun provideNotificationTimeStore(context: Context): NotificationTimeStore {
        return InternalPreferences(context)
    }

    @Provides
    fun provideNotificationManager(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    fun provideNotificationFactory(context: Context, settings: UserSettings): NotificationFactory {
        return AndroidNotificationFactory(context, settings)
    }
}
