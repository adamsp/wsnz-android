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

package speakman.whatsshakingnz.utils;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Adam on 2016-05-08.
 */
public class DateTimeFormattersTests {

    @Test
    public void testRequestQueryUpdateTimeFormatterPrintsCorrectly() {
        DateTime date = new DateTime(1462726536395L); // 2016-05-08 16:55:36.395 UTC
        String expected = "2016-05-08T16:55:36.395Z";
        String actual = date.toString(DateTimeFormatters.requestQueryUpdateTimeFormatter);
        assertEquals(expected, actual);
    }

    @Test
    public void testNetworkDateTimeIsReadCorrectly() {
        String date = "2016-01-08T18:16:16.549Z";
        DateTime expected = new DateTime(1452276976549L);
        DateTime actual = DateTimeFormatters.networkDateTimeReadFormatter.parseDateTime(date);
        assertEquals(expected, actual);
    }

    @Test
    public void testNetworkDateTimeIsWrittenCorrectly() {
        DateTime date = new DateTime(1452276976549L);
        String expected = "2016-01-08T18:16:16.549Z";
        String actual = date.toString(DateTimeFormatters.networkDateTimeWriteFormatter);
        assertEquals(expected, actual);
    }
}
