/*
 * Copyright 2015 Adam Speakman
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

package speakman.whatsshakingnz;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import net.danlew.android.joda.JodaTimeAndroid;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import jonathanfinerty.once.Once;
import speakman.whatsshakingnz.analytics.Analytics;
import speakman.whatsshakingnz.analytics.Forest;
import speakman.whatsshakingnz.backgroundsync.BackgroundSyncService;
import speakman.whatsshakingnz.network.NetworkRunnerService;
import speakman.whatsshakingnz.ui.activities.DetailActivity;
import speakman.whatsshakingnz.ui.activities.MainActivity;
import speakman.whatsshakingnz.ui.activities.MapActivity;
import timber.log.Timber;

/**
 * Created by Adam on 15-06-07.
 */
public class WhatsShakingApplication extends Application {

    private static final String INIT_SYNC_ON_INSTALL = "speakman.whatsshakingnz.INIT_SYNC_ON_INSTALL";
    private static final String INIT_SYNC_ON_UPGRADE = "speakman.whatsshakingnz.INIT_SYNC_ON_UPGRADE";
    private static WhatsShakingApplication instance;
    private AppComponent component;

    @Inject
    RealmConfiguration realmConfiguration;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        LeakCanary.install(this);
        Timber.plant(Forest.uproot());
        Analytics.initialize(this);
        Once.initialise(this);
        JodaTimeAndroid.init(this);
        Realm.init(this);
        component = DaggerAppComponent.create();
        component.inject(this);
        setupRealm();
        scheduleSync();
    }

    public void inject(NetworkRunnerService service) {
        logInjection(service);
        component.inject(service);
    }

    public void inject(BackgroundSyncService service) {
        logInjection(service);
        component.inject(service);
    }

    public void inject(MainActivity mainActivity) {
        logInjection(mainActivity);
        component.inject(mainActivity);
    }

    public void inject(MapActivity activity) {
        logInjection(activity);
        component.inject(activity);
    }

    public void inject(DetailActivity activity) {
        logInjection(activity);
        component.inject(activity);
    }

    public static WhatsShakingApplication getInstance() {
        return instance;
    }

    private void logInjection(Object object) {
        Timber.d("Injecting %s inside Application class", object == null ? "null" : object.getClass().getSimpleName());
    }

    private void setupRealm() {
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    private void scheduleSync() {
        if (!Once.beenDone(Once.THIS_APP_INSTALL, INIT_SYNC_ON_INSTALL)) {
            Timber.i("Scheduling background sync on first-install.");
            BackgroundSyncService.scheduleSync(this);
            Once.markDone(INIT_SYNC_ON_INSTALL);
        } else if (!Once.beenDone(Once.THIS_APP_VERSION, INIT_SYNC_ON_UPGRADE)) {
            // Firebase is *supposed* to ensure jobs get rescheduled on upgrade. There may be bugs, though:
            // https://github.com/firebase/firebase-jobdispatcher-android/issues/6
            Timber.i("Scheduling background sync on app upgrade.");
            BackgroundSyncService.scheduleSync(this);
            Once.markDone(INIT_SYNC_ON_UPGRADE);
        }
    }
}
