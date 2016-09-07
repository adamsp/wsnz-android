/*
 * Copyright 2016 Adam Speakman
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

import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Adam on 5/3/2016.
 */
public class DateTimeFormatters {
    public static final DateFormat mediumDateTimeDisplayFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    public static final DateTimeFormatter networkDateTimeReadFormatter = ISODateTimeFormat.dateTimeParser();
    public static final DateTimeFormatter networkDateTimeWriteFormatter = ISODateTimeFormat.dateTime().withChronology(ISOChronology.getInstanceUTC());

    /*
    This requires an extra "S" on the end (ie, 4 'milliseconds' values). This 4th place will
    always be populated with a 0. This is an unfortunate requirement of the API - if we don't
    supply this extra 0, it treats the 'greater than' as 'greater than or equal to', when
    requesting events with an updated time greater than the most recently seen event.
    This is discussed here: https://github.com/GeoNet/help/issues/5
     */
    public static final DateTimeFormatter requestQueryUpdateTimeFormatter
            = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss").withZoneUTC();
}
