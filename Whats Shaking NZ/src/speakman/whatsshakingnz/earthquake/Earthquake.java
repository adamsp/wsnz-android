package speakman.whatsshakingnz.earthquake;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;
import speakman.whatsshakingnz.maps.DistanceTool;

import java.text.DecimalFormat;
import java.util.Date;

public class Earthquake implements Parcelable {

    private double mMagnitude, mDepth, mRoundedMagnitude;
    private LatLng mLatLng;
    private String mReference, mLocation, mAgency;
    private Date mDate;
    private String mStatus;
    public static final DecimalFormat magnitudeFormat = new DecimalFormat("#.0");
    private static final DecimalFormat depthFormat = new DecimalFormat("#");

    public Earthquake(double magnitude, double depth, LatLng latLng,
                      String reference, Date date, String agency, String status) {
        setMagnitude(magnitude);
        setDepth(depth);
        setLatLng(latLng);
        setReference(reference);
        setDate(date);
        setLocation();
        setAgency(agency);
        setStatus(status);
    }

    private Earthquake(Parcel in) {
        this(in.readDouble(), in.readDouble(), new LatLng(in.readDouble(),
                in.readDouble()), in.readString(), (Date) in.readSerializable(),
                in.readString(), in.readString());
    }

    private void setLatLng(LatLng latLng) {
        mLatLng = latLng;
    }

    private void setReference(String reference) {
        this.mReference = reference;
    }

    private void setDate(Date date) {
        this.mDate = date;
    }

    private void setLocation() {
        mLocation = DistanceTool.getClosestTown(mLatLng);
    }

    private void setDepth(double depth) {
        this.mDepth = depth;
    }

    private void setMagnitude(double m) {
        this.mMagnitude = m;
        this.mRoundedMagnitude = (double) Math.round(m * 10) / 10;
    }

    private void setAgency(String a) {
        this.mAgency = a;
    }

    private void setStatus(String s) {
        this.mStatus = s;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }

    public double getMagnitude() {
        return mMagnitude;
    }

    public double getRoundedMagnitude() {
        return mRoundedMagnitude;
    }

    public String getFormattedMagnitude() {
        return magnitudeFormat.format(mMagnitude);
    }

    public double getDepth() {
        return mDepth;
    }

    public String getFormattedDepth() {
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

    public String getAgency() {
        return mAgency;
    }

    public String getStatus() {
        return mStatus;
    }

    // this is used to regenerate your object. All Parcelables must have a
    // CREATOR that implements these two methods
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
        dest.writeDouble(mLatLng.latitude);
        dest.writeDouble(mLatLng.longitude);
        dest.writeString(mReference);
        dest.writeSerializable(mDate);
        dest.writeString(mAgency);
        dest.writeString(mStatus);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mAgency == null) ? 0 : mAgency.hashCode());
        result = prime * result + ((mDate == null) ? 0 : mDate.hashCode());
        long temp;
        temp = Double.doubleToLongBits(mDepth);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result
                + ((mLocation == null) ? 0 : mLocation.hashCode());
        temp = Double.doubleToLongBits(mMagnitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((mLatLng == null) ? 0 : mLatLng.hashCode());
        result = prime * result
                + ((mReference == null) ? 0 : mReference.hashCode());
        temp = Double.doubleToLongBits(mRoundedMagnitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((mStatus == null) ? 0 : mStatus.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof  Earthquake))
            return false;
        Earthquake other = (Earthquake) obj;
        if (mAgency == null) {
            if (other.mAgency != null)
                return false;
        } else if (!mAgency.equals(other.mAgency))
            return false;
        if (mDate == null) {
            if (other.mDate != null)
                return false;
        } else if (!mDate.equals(other.mDate))
            return false;
        if (Double.doubleToLongBits(mDepth) != Double
                .doubleToLongBits(other.mDepth))
            return false;
        if (mLocation == null) {
            if (other.mLocation != null)
                return false;
        } else if (!mLocation.equals(other.mLocation))
            return false;
        if (Double.doubleToLongBits(mMagnitude) != Double
                .doubleToLongBits(other.mMagnitude))
            return false;
        if (mLatLng == null) {
            if (other.mLatLng != null)
                return false;
        } else if (!mLatLng.equals(other.mLatLng))
            return false;
        if (mReference == null) {
            if (other.mReference != null)
                return false;
        } else if (!mReference.equals(other.mReference))
            return false;
        if (Double.doubleToLongBits(mRoundedMagnitude) != Double
                .doubleToLongBits(other.mRoundedMagnitude))
            return false;
        if (mStatus == null) {
            if (other.mStatus != null)
                return false;
        } else if (!mStatus.equals(other.mStatus))
            return false;
        return true;
    }

    public float getHue() {
            // https://developers.google.com/maps/documentation/android/reference/com/google/android/gms/maps/model/BitmapDescriptorFactory
            // Red is 0
            // Orange is 30
            // Yellow is 60
            // Green is 120

            // TODO Apply an appropriate non-linear scale to these - 4.0 not much redder than a 3.0, currently.
            float mostSignificantQuake = 6;
            float percentage = (float) (getMagnitude() / mostSignificantQuake);
            float hue = 90 - (percentage * 90);

            if (hue < 0)
                hue = 0;
            else if (hue > 90)
                hue = 90;
            return hue;
    }
}
