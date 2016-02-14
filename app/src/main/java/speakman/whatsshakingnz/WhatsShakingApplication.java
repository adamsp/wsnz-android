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

import net.danlew.android.joda.JodaTimeAndroid;

import speakman.whatsshakingnz.analytics.Forest;
import speakman.whatsshakingnz.network.NetworkRunnerService;
import speakman.whatsshakingnz.ui.activities.DetailActivity;
import speakman.whatsshakingnz.ui.activities.MainActivity;
import speakman.whatsshakingnz.ui.activities.MapActivity;
import timber.log.Timber;

/**
 * Created by Adam on 15-06-07.
 */
public class WhatsShakingApplication extends Application {

    private static WhatsShakingApplication instance;
    private AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(Forest.uproot());
        instance = this;
        JodaTimeAndroid.init(this);
        component = DaggerAppComponent.create();
    }

    public void inject(MainActivity activity) {
        logInjection(activity);
        component.inject(activity);
    }

    public void inject(DetailActivity activity) {
        logInjection(activity);
        component.inject(activity);
    }

    public void inject(MapActivity activity) {
        logInjection(activity);
        component.inject(activity);
    }

    public void inject(NetworkRunnerService service) {
        logInjection(service);
        component.inject(service);
    }

    public static WhatsShakingApplication getInstance() {
        return instance;
    }

    private void logInjection(Object object) {
        Timber.d("Injecting %s inside Application class", object == null ? "null" : object.getClass().getSimpleName());
    }
}
