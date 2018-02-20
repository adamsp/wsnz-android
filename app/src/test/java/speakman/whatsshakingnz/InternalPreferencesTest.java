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

package speakman.whatsshakingnz;

import android.content.Context;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Adam on 2016-03-17.
 */
@RunWith(MockitoJUnitRunner.class)
public class InternalPreferencesTest {

    @Mock
    Context ctx;

    private Context getContext() {
        return ctx;
    }

    @Test
    public void testMostRecentUpdateTimeIsStored() throws Exception {
        InternalPreferences prefs = new InternalPreferences(getContext());
        DateTime time = DateTime.now();
        prefs.saveMostRecentUpdateTime(time);
        assertEquals(time, prefs.getMostRecentUpdateTime());
    }

    @Test
    public void testMostRecentUpdateTimeIsCleared() throws Exception {
        InternalPreferences prefs = new InternalPreferences(getContext());
        prefs.saveMostRecentUpdateTime(DateTime.now());
        prefs.saveMostRecentUpdateTime(null);
        assertNull(prefs.getMostRecentUpdateTime());
    }

    @Test
    public void testMostRecentlySeenTimeIsStored() throws Exception {
        InternalPreferences prefs = new InternalPreferences(getContext());
        DateTime time = DateTime.now();
        prefs.saveMostRecentlySeenEventOriginTime(time);
        assertEquals(time, prefs.getMostRecentlySeenEventOriginTime());
    }

    @Test
    public void testMostRecentlySeenTimeIsCleared() throws Exception {
        InternalPreferences prefs = new InternalPreferences(getContext());
        prefs.saveMostRecentlySeenEventOriginTime(DateTime.now());
        prefs.saveMostRecentlySeenEventOriginTime(null);
        assertNull(prefs.getMostRecentlySeenEventOriginTime());
    }
}
