/**
 * The following code is based on code originally
 * written by Matthew Wiggins and is released 
 * under the APACHE 2.0 license.
 * Original: http://android.hlidskialf.com/blog/code/android-seekbar-preference
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package speakman.whatsshakingnz.preferences;

import speakman.whatsshakingnz.earthquake.Earthquake;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference implements
		SeekBar.OnSeekBarChangeListener {
	private static final String androidns = "http://schemas.android.com/apk/res/android";
	private static final String customns = "http://schemas.android.com/apk/res/speakman.whatsshakingnz";

	private SeekBar mSeekBar;
	private TextView mSplashText, mValueText;
	private Context mContext;

	private String mDialogMessage, mSuffix;
	/**
	 * mDefault is used for the default value for the SeekBar.
	 */
	private int mDefault = 0;
	/**
	 * mMax is used as the max value for the SeekBar.
	 */
	private int mMax = 0;
	/**
	 * mValue is the current value of the SeekBar and the value we would like to
	 * persist. When writing this to the SeekBar, subtract mMin. When reading
	 * this from the SeekBar, add mMin.
	 */
	private int mValue = 0;
	/**
	 * mMin is the minimum value of the SeekBar. Since the SeekBar can only have
	 * 0 as a minimum value, we use this to manipulate the displayed values.
	 */
	private int mMin = 0;
	
	private boolean mUseDecimal = false;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mMin = attrs.getAttributeIntValue(customns, "seekBarMin", 0);
		mDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");
		mSuffix = attrs.getAttributeValue(androidns, "text");
		mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
		mMax = attrs.getAttributeIntValue(androidns, "max", 100) - mMin;
		mUseDecimal = attrs.getAttributeBooleanValue(customns, "seekBarUseDecimal", false);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if(which == -1)
			if (shouldPersist())
				persistInt(mValue);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// TODO Auto-generated method stub
		super.onDismiss(dialog);
	}

	@Override
	protected View onCreateDialogView() {
		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(6, 6, 6, 6);

		if (mDialogMessage != null) {
			mSplashText = new TextView(mContext);
			mSplashText.setText(mDialogMessage);
			layout.addView(mSplashText);
		}

		mValueText = new TextView(mContext);
		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		mValueText.setTextSize(32);
		mValueText.setTextColor(mContext.getResources().getColor(android.R.color.secondary_text_dark));
		params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.addView(mValueText, params);

		mValue = getPersistedInt(mDefault);
		
		// We specify the listener last, as the setMax call fires the changed event
		mSeekBar = new SeekBar(mContext);
		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue - mMin);
		mSeekBar.setOnSeekBarChangeListener(this);
		layout.addView(mSeekBar, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		
		setProgressText(mValue);
		
		return layout;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue - mMin);
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
		if (restore)
			mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
		else
			mValue = (Integer) defaultValue;
	}

	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		mValue = value + mMin;
		setProgressText(mValue);
		callChangeListener(mValue);//Integer.valueOf(value + mMin));
	}
	
	private void setProgressText(int value) {
		String text = String.valueOf(mUseDecimal ? Earthquake.magnitudeFormat.format((float)value / 10.0f) : value); 
		mValueText.setText(mSuffix == null ? text : text.concat(mSuffix));
	}

	public void onStartTrackingTouch(SeekBar seek) {
	}

	public void onStopTrackingTouch(SeekBar seek) {
	}

	public void setMax(int max) {
		mMax = max;
	}

	public int getMax() {
		return mMax;
	}

	public void setProgress(int progress) {
		mValue = progress + mMin;
		if (mSeekBar != null)
			mSeekBar.setProgress(progress);
	}

	public int getProgress() {
		return mValue - mMin;
	}
}