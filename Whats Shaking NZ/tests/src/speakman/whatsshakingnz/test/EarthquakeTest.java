package speakman.whatsshakingnz.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import speakman.whatsshakingnz.earthquake.Earthquake;
import android.os.Parcel;
import android.test.AndroidTestCase;

import com.google.android.maps.GeoPoint;

public class EarthquakeTest extends AndroidTestCase {
	private SimpleDateFormat df;
	double mag;
	double depth;
	GeoPoint point;
	String reference;
	Date date;
	String agency;
	String status;
	
	public EarthquakeTest() {
		super();
	}
	
	@Override
	protected void setUp() throws Exception {
		df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		mag = 3.13;
		depth = 102.73;
		point = new GeoPoint(-36848457,174763351);
		reference = "2013p123456";
		date = df.parse("2012-08-25 05:47:05.133000");
		agency = "WEL(GNS_Primary)";
		status = "reviewed";
		super.setUp();
	}
	
	public void testSuppliedMagnitudeIsReturned() {
		Earthquake quake = new Earthquake(mag, depth, point, reference, date, agency, status);
		assertEquals(mag, quake.getMagnitude());
	}
	
	public void testSuppliedDepthIsReturned() {
		Earthquake quake = new Earthquake(mag, depth, point, reference, date, agency, status);
		assertEquals(depth, quake.getDepth());
	}
	
	public void testSuppliedPointIsReturned() {
		Earthquake quake = new Earthquake(mag, depth, point, reference, date, agency, status);
		assertEquals(point, quake.getPoint());
	}
	
	public void testSuppliedReferenceIsReturned() {
		Earthquake quake = new Earthquake(mag, depth, point, reference, date, agency, status);
		assertEquals(reference, quake.getReference());
	}
	
	public void testSuppliedDateIsReturned() {
		Earthquake quake = new Earthquake(mag, depth, point, reference, date, agency, status);
		assertEquals(date, quake.getDate());
	}
	
	public void testSuppliedAgencyIsReturned() {
		Earthquake quake = new Earthquake(mag, depth, point, reference, date, agency, status);
		assertEquals(agency, quake.getAgency());
	}
	
	public void testSuppliedStatusIsReturned() {
		Earthquake quake = new Earthquake(mag, depth, point, reference, date, agency, status);
		assertEquals(status, quake.getStatus());
	}

	public void testEarthquakeSerializesAndDeserializesCorrectly() throws ParseException {
		Earthquake quake = new Earthquake(mag, depth, point, reference, date, agency, status);
		Parcel parcel = Parcel.obtain();
		quake.writeToParcel(parcel, 0);
		parcel.setDataPosition(0);
		Earthquake createdFromParcel = Earthquake.CREATOR.createFromParcel(parcel);

		assertEquals(quake, createdFromParcel);
	}
	
	public void testMagnitudeFormatsCorrectly() {
		Earthquake quake = new Earthquake(3.13, depth, point, reference, date, agency, status);
		assertEquals("3.1", quake.getFormattedMagnitude());

		quake = new Earthquake(3.02, depth, point, reference, date, agency, status);
		assertEquals("3.0", quake.getFormattedMagnitude());
		
		quake = new Earthquake(3.96, depth, point, reference, date, agency, status);
		assertEquals("4.0", quake.getFormattedMagnitude());
		
		quake = new Earthquake(3.15, depth, point, reference, date, agency, status);
		assertEquals("3.2", quake.getFormattedMagnitude());
		
		quake = new Earthquake(3.0, depth, point, reference, date, agency, status);
		assertEquals("3.0", quake.getFormattedMagnitude());
	}
	
	public void testMagnitudeRoundsCorrectly() {
		Earthquake quake = new Earthquake(3.13, depth, point, reference, date, agency, status);
		assertEquals(3.1, quake.getRoundedMagnitude());

		quake = new Earthquake(3.02, depth, point, reference, date, agency, status);
		assertEquals(3.0, quake.getRoundedMagnitude());
		
		quake = new Earthquake(3.96, depth, point, reference, date, agency, status);
		assertEquals(4.0, quake.getRoundedMagnitude());
		
		quake = new Earthquake(3.15, depth, point, reference, date, agency, status);
		assertEquals(3.2, quake.getRoundedMagnitude());
		
		quake = new Earthquake(3.0, depth, point, reference, date, agency, status);
		assertEquals(3.0, quake.getRoundedMagnitude());
	}
	
	public void testDepthFormatsCorrectly() {
		Earthquake quake = new Earthquake(mag, 102.13, point, reference, date, agency, status);
		assertEquals("102", quake.getFormattedDepth());
		
		quake = new Earthquake(mag, 102.03, point, reference, date, agency, status);
		assertEquals("102", quake.getFormattedDepth());

		quake = new Earthquake(mag, 102.73, point, reference, date, agency, status);
		assertEquals("103", quake.getFormattedDepth());
		
		quake = new Earthquake(mag, 102.5, point, reference, date, agency, status);
		assertEquals("103", quake.getFormattedDepth());
				
		quake = new Earthquake(mag, 102.0, point, reference, date, agency, status);
		assertEquals("102", quake.getFormattedDepth());
	}
}
