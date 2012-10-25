package speakman.whatsshakingnz.earthquake;

import java.text.DecimalFormat;
import java.util.Date;

import speakman.whatsshakingnz.maps.DistanceTool;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class Earthquake extends OverlayItem implements Parcelable {

	private double mMagnitude, mDepth, mRoundedMagnitude;
	private GeoPoint mPoint;
	private String mReference, mLocation, mAgency;
	private Date mDate;
	public static final DecimalFormat magnitudeFormat = new DecimalFormat(
			"#.0");
	private static final DecimalFormat depthFormat = new DecimalFormat("#");

	public Earthquake(double magnitude, double depth, GeoPoint point,
			String reference, Date date, String agency) {
		super(point, magnitudeFormat.format(magnitude), depthFormat.format(depth));
		setGeoPoint(point);
		setMagnitude(magnitude);
		setDepth(depth);
		setGeoPoint(point);
		setReference(reference);
		setDate(date);
		setLocation();
		setAgency(agency);
	}
	
	private Earthquake(Parcel in)
	{
		this(in.readDouble(),
				in.readDouble(),
				new GeoPoint(in.readInt(), in.readInt()),
				in.readString(),
				(Date)in.readSerializable(),
				in.readString());
	}

	private void setGeoPoint(GeoPoint point) {
		mPoint = point;
	}

	private void setReference(String reference) {
		this.mReference = reference;
	}

	private void setDate(Date date) {
		this.mDate = date;
	}

	private void setLocation() {
		mLocation = DistanceTool.getClosestTown(mPoint);
	}

	private void setDepth(double depth) {
		this.mDepth = depth;
	}

	private void setMagnitude(double m) {
		this.mMagnitude = m;
		this.mRoundedMagnitude = (double)Math.round(m * 10) / 10;
	}
	
	private void setAgency(String a){
		this.mAgency = a;
	}

	@Override
	public GeoPoint getPoint() {
		return mPoint;
	}

	public double getMagnitude() {
		return mMagnitude;
	}
	
	public double getRoundedMagnitude(){
		return mRoundedMagnitude;
	}
	
	public String getFormattedMagnitude(){
		return magnitudeFormat.format(mMagnitude);
	}

	public double getDepth() {
		return mDepth;
	}
	
	public String getFormattedDepth(){
		return depthFormat.format(mDepth);
	}

	public String getLocation() {
		return mLocation;
	}

	public String getReference() {
		return mReference;
	}

	public Date getDate() {
		return mDate;
	}
	
	public String getAgency(){
		return mAgency;
	}

	// this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Earthquake> CREATOR = new Parcelable.Creator<Earthquake>() {
        public Earthquake createFromParcel(Parcel in) {
            return new Earthquake(in);
        }

        public Earthquake[] newArray(int size) {
            return new Earthquake[size];
        }
    };

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(mMagnitude);
		dest.writeDouble(mDepth);
		dest.writeInt(mPoint.getLatitudeE6());
		dest.writeInt(mPoint.getLongitudeE6());
		dest.writeString(mReference);
		dest.writeSerializable(mDate);
		dest.writeString(mAgency);
	}
}
