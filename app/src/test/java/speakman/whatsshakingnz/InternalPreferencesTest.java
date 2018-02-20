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
import android.content.SharedPreferences;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Adam on 2016-03-17.
 */
@RunWith(MockitoJUnitRunner.class)
public class InternalPreferencesTest {

    @Mock
    private Context ctx;

    @Mock
    private SharedPreferences sharedPrefs;

    @Mock
    private SharedPreferences.Editor editor;

    private InternalPreferences underTest;

    @Before
    public void setup() {
        when(ctx.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs);
        when(sharedPrefs.edit()).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.putFloat(anyString(), anyFloat())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.remove(anyString())).thenReturn(editor);
        underTest = new InternalPreferences(ctx);
    }

    @Test
    public void testMostRecentUpdateTimeIsStored() throws Exception {
        DateTime time = DateTime.now();
        underTest.saveMostRecentUpdateTime(time);
        verify(editor).putLong(InternalPreferences.KEY_MOST_RECENT_REQUEST_TIME, time.getMillis());
        verify(editor).apply();
    }

    @Test
    public void testMostRecentUpdateTimeIsCleared() throws Exception {
        underTest.saveMostRecentUpdateTime(null);
        verify(editor).remove(InternalPreferences.KEY_MOST_RECENT_REQUEST_TIME);
        verify(editor).apply();
    }

    @Test
    public void testMostRecentlySeenTimeIsStored() throws Exception {
        DateTime time = DateTime.now();
        underTest.saveMostRecentlySeenEventOriginTime(time);
        verify(editor).putLong(InternalPreferences.KEY_MOST_RECENTLY_SEEN_TIME, time.getMillis());
        verify(editor).apply();
    }

    @Test
    public void testMostRecentlySeenTimeIsCleared() throws Exception {
        underTest.saveMostRecentlySeenEventOriginTime(null);
        verify(editor).remove(InternalPreferences.KEY_MOST_RECENTLY_SEEN_TIME);
        verify(editor).apply();
    }
}
