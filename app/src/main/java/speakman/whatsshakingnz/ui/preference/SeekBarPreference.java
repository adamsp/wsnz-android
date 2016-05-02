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

package speakman.whatsshakingnz.ui.preference;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import speakman.whatsshakingnz.R;

public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {
    private static final String androidns = "http://schemas.android.com/apk/res/android";
    private static final String customns = "http://schemas.android.com/apk/res/speakman.whatsshakingnz";

    private TextView valueText;
    private String formatString = "%.1f";
    private String titleString;

    private int defaultValue = 0;
    private int maxValue = 0;
    private int currentValue = 0;
    /**
     * minValue is the minimum value of the SeekBar. Since the SeekBar can only have
     * 0 as a minimum value, we use this to manipulate the displayed values.
     */
    private int minValue = 0;
    private boolean useDecimal;

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    @SuppressWarnings("unused")
    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @SuppressWarnings("unused")
    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        minValue = attrs.getAttributeIntValue(customns, "seekBarMin", 0);
        defaultValue = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
        maxValue = attrs.getAttributeIntValue(androidns, "max", 100) - minValue;
        useDecimal = attrs.getAttributeBooleanValue(customns, "seekBarUseDecimal", false);
        formatString = useDecimal ? "%.1f" : "%d";
        int titleRes = attrs.getAttributeResourceValue(androidns, "title", 0);
        if (titleRes > 0) {
            titleString = getContext().getString(titleRes);
        }
        if (titleString == null) {
            throw new IllegalArgumentException("Must specify a title");
        }
        setLayoutResource(R.layout.preference_seekbar);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        currentValue = getPersistedInt(defaultValue);
        valueText = (TextView) view.findViewById(R.id.preference_seekbar_value_text);
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.preference_seekbar);
        TextView title = (TextView) view.findViewById(R.id.preference_seekbar_title);
        title.setText(titleString);
        seekBar.setMax(maxValue);
        seekBar.setProgress(currentValue - minValue);
        seekBar.setOnSeekBarChangeListener(this);
        setProgressText(currentValue);
        return view;
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore) {
            currentValue = shouldPersist() ? getPersistedInt(this.defaultValue) : 0;
        } else {
            currentValue = (Integer) defaultValue;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        currentValue = value + minValue;
        setProgressText(currentValue);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seek) { }

    @Override
    public void onStopTrackingTouch(SeekBar seek) {
        callChangeListener(currentValue);
        persistInt(currentValue);
    }

    private void setProgressText(int value) {
        @SuppressLint("DefaultLocale")
        String text = String.format(formatString, useDecimal ? ((float) value / 10.0f) : value);
        valueText.setText(text);
    }
}
