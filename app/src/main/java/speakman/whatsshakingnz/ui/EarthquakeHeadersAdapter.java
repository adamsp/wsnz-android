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

package speakman.whatsshakingnz.ui;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.List;

import speakman.whatsshakingnz.R;
import speakman.whatsshakingnz.model.Earthquake;

/**
 * Created by Adam on 2016-11-26.
 */

public class EarthquakeHeadersAdapter implements StickyHeadersDecoration.StickyHeadersAdapter {

    private static final long POSITION_NO_EVENTS = -1;
    private static final long POSITION_LAST_24_HOURS = 0;
    private static final long POSITION_LAST_7_DAYS = 1;
    private static final long POSITION_LAST_MONTH = 2;
    private static final long POSITION_LAST_YEAR = 3;
    private static final long POSITION_ALL_TIME = 4;

    private List<Earthquake> earthquakes;

    @Override
    public long getSectionId(int position) {
        if (earthquakes == null || earthquakes.size() == 0) {
            return POSITION_NO_EVENTS;
        }
        Earthquake earthquake = earthquakes.get(position);
        long millisPassed = DateTime.now().getMillis() - earthquake.getOriginTime();

        long secondsPassed = millisPassed / 1000;
        long minsPassed = secondsPassed / 60;
        long hoursPassed = minsPassed / 60;
        long daysPassed = hoursPassed / 24;
        if (hoursPassed < 24) {
            return POSITION_LAST_24_HOURS;
        }
        if (daysPassed < 7) {
            return POSITION_LAST_7_DAYS;
        }
        if (daysPassed < 28) {
            return POSITION_LAST_MONTH;
        }
        if (daysPassed < 365) {
            return POSITION_LAST_YEAR;
        }
        return POSITION_ALL_TIME;
    }

    @Nullable
    @Override
    public View getHeaderViewForSection(long section, RecyclerView parent) {
        @StringRes int text;
        // Can't switch on a long
        if (section == POSITION_NO_EVENTS) {
            text = R.string.header_no_events;
        } else if (section == POSITION_LAST_24_HOURS) {
            text = R.string.header_last_24_hours;
        } else if (section == POSITION_LAST_7_DAYS) {
            text = R.string.header_last_7_days;
        } else if (section == POSITION_LAST_MONTH) {
            text = R.string.header_last_30_days;
        } else if (section == POSITION_LAST_YEAR) {
            text = R.string.header_last_year;
        } else {
            text = R.string.header_all_time;
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_header, parent, false);
        ((TextView) view.findViewById(R.id.row_header_text)).setText(text);
        return view;
    }

    public void updateList(List<Earthquake> earthquakes) {
        this.earthquakes = earthquakes;
    }
}
