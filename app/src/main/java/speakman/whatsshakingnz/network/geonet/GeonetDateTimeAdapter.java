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

package speakman.whatsshakingnz.network.geonet;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.joda.time.DateTime;

import java.io.IOException;

import speakman.whatsshakingnz.utils.DateTimeFormatters;

/**
 * Created by Adam on 15-06-07.
 */
public class GeonetDateTimeAdapter extends TypeAdapter<DateTime> {



    @Override
    public void write(JsonWriter writer, DateTime value) throws IOException {
        if (value == null) {
            writer.nullValue();
            return;
        }
        String dateTime = value.toString(DateTimeFormatters.networkDateTimeWriteFormatter);
        writer.value(dateTime);
    }

    @Override
    public DateTime read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        String dateTime = reader.nextString();
        return DateTime.parse(dateTime, DateTimeFormatters.networkDateTimeReadFormatter);
    }
}