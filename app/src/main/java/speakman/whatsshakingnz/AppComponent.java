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

import dagger.Component;
import speakman.whatsshakingnz.dagger.AppScope;
import speakman.whatsshakingnz.model.ModelModule;
import speakman.whatsshakingnz.network.NetworkModule;
import speakman.whatsshakingnz.network.NetworkRunnerService;
import speakman.whatsshakingnz.ui.activities.DetailActivity;
import speakman.whatsshakingnz.ui.activities.MainActivity;
import speakman.whatsshakingnz.ui.activities.MapActivity;

/**
 * Created by Adam on 15-06-13.
 */
@AppScope
@Component(modules = { NetworkModule.class, ModelModule.class, AppModule.class })
public interface AppComponent {
    void inject(MainActivity activity);
    void inject(DetailActivity activity);
    void inject(MapActivity activity);
    void inject(NetworkRunnerService service);
}
