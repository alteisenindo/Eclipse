package r1.util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import r1.util.iCCConstants.DateTimeFormat;
import r1.util.iCCConstants.iCCTaxiStatus;
//import r1.util.iCCMessageStorage.AppId;
//import r1.util.iCCMessageStorage.iCCDriver;
import android.location.Location;

public class GPSData extends Location 
{
	int nSignalStatus;
	String sDateTime;
	//Date dtDateTime;
	Calendar dtCal;
	String sLatitude;
	String sLongitude;
	String sValidity;
	String sFixQuality;
	int nHDOP;
	int nTotalSat;

	public GPSData(String provider) 
	{
		super(provider);	

		dtCal = Calendar.getInstance();		
		
		nSignalStatus = 0;
		sLatitude = "-91.0";
		sLongitude = "-181.0";
		sDateTime = "01-01-2000";
		sValidity = "A";
		sFixQuality = "0";		
		nHDOP = 50;
		nTotalSat = 0;
		setLongitude(-181);
		setLatitude(-91);		
	}			

	public static boolean EncodeGPSData(GPSData data, int nTaxiBusy, BitManager bf, INIFile settings, UserInfo usrInfo, AppId appId)
	{
		String[] latArr; 
		String[] lonArr;
		//String[] spdArr;
		int latMajor, lonMajor, latMinor, lonMinor, speed, signal;
		String delimit = "\\.";            

		try
		{			
			String sLat = String.format("%.6f", data.getLatitude());
			String sLon = String.format("%.6f", data.getLongitude());
			
			//latArr = String.valueOf(data.getLatitude()).split(delimit);
			//lonArr = String.valueOf(data.getLongitude()).split(delimit);
			//spdArr = String.valueOf(data.getSpeed()).split(delimit);
			
			latArr = sLat.split(delimit);
			lonArr = sLon.split(delimit);

			latMajor = Integer.parseInt(latArr[0]);
			lonMajor = Integer.parseInt(lonArr[0]);
			speed = (int)data.getSpeed();
			signal = data.getnSignalStatus();

			latMinor = Integer.parseInt(latArr[1]);
			lonMinor = Integer.parseInt(lonArr[1]);
		}
		catch (Exception ex)
		{
			// create invalid data, this will be ignored by server
			String lat = "91.0";
			String lon = "181.0";
			String spd = "0.0";

			latArr = lat.split(delimit);
			lonArr = lon.split(delimit);
			//spdArr = spd.split(delimit);

			latMajor = Integer.parseInt(latArr[0]);
			lonMajor = Integer.parseInt(lonArr[0]);
			speed = 0;
			signal = data.getnSignalStatus();

			latMinor = Integer.parseInt(latArr[1]);
			lonMinor = Integer.parseInt(lonArr[1]);
		}           

		int year = data.getDtCal().get(Calendar.YEAR) - 2000;
		if (year < 0)
			year = 0;

		try
		{      	
			// Date
			bf.addBits(year, 6);
			bf.addBits(data.getDtCal().get(Calendar.MONTH)+1, 4);
			bf.addBits(data.getDtCal().get(Calendar.DATE), 5);

			// Time			
			bf.addBits(data.getDtCal().get(Calendar.HOUR_OF_DAY), 5);
			bf.addBits(data.getDtCal().get(Calendar.MINUTE), 6);
			bf.addBits(data.getDtCal().get(Calendar.SECOND), 6);

			// Latitude
			bf.addBits(latMajor, 8);
			bf.addBits(latMinor, 20);

			// Longitude
			bf.addBits(lonMajor, 9);
			bf.addBits(lonMinor, 20);

			// Speed
			bf.addBits(speed, 8);

			// Signal
			bf.addBits(signal, 1);

			// total distance
			/*if (settings.getIntegerProperty("Settings", "ProtocolVersion") >= 7)          
				bf.addBits(Integer.parseInt(appId.getTotalGPSDistance()), 32);*/

			// Taxi status
			if (nTaxiBusy == 0)
				bf.addBit(false);
			else
				bf.addBit(true);
			
			/*if (settings.getIntegerProperty("Settings", "ProtocolVersion") >= 9)
			{
				iCCDriver drv = null;
				if (usrInfo.getCurDriver().getnDriverID() > 0)
					drv = usrInfo.getCurDriver();
				else
					drv = usrInfo.getSvrDriver();
				
				if (drv.geteLastTaxiStatus() == iCCTaxiStatus.ToCustomer ||
						drv.geteLastTaxiStatus() == iCCTaxiStatus.AtCustomer ||                        
						drv.geteLastTaxiStatus() == iCCTaxiStatus.ToDest ||
						drv.geteLastTaxiStatus() == iCCTaxiStatus.NearDest)
					bf.addBit(true);
				else
					bf.addBit(false);								
			}*/
		}
		catch (Exception ex)
		{
			return false;
		}         

		return true;
	}
	public static boolean DecodeGPSData(GPSData data, BitManager bm, int start, int[] stopidx)
	{
		if (bm.getBitArray().getSize() - start < 57) // 28 bits for lat, 29 bits for lon
			return false;

		try
		{
			int latMajor = (int)bm.getBitsSigned(start, 8);
			int latMinor = (int)bm.getBits(start + 8, 20);

			int lonMajor = (int)bm.getBitsSigned(start + 28, 9);
			int lonMinor = (int)bm.getBits(start + 37, 20);

			data.setsLatitude(String.format("%s.%s", latMajor, latMinor));
			data.setsLongitude(String.format("%s.%s", lonMajor, lonMinor));
		}
		catch (Exception ex)
		{
			return false;
		}

		stopidx[0] = start + 57 - 1;
		return true;
	}
	
	public String GetStatus()
	{						
		String str = String.format("Accuracy: %s, Signal: %s",
				(hasAccuracy()) ? getAccuracy() : "ERR",        
        ((nSignalStatus == 0) ? "ERR" : "OK"));

    return str;
	}

	public int getnSignalStatus() {
		return nSignalStatus;
	}

	public void setnSignalStatus(int nSignalStatus) {
		this.nSignalStatus = nSignalStatus;
	}

	public String getsDateTime() {
		return sDateTime;
	}

	public void setsDateTime(String sDateTime) {
		this.sDateTime = sDateTime;
	}

	/*public Date getDtDateTime() {
		return dtDateTime;
	}

	public void setDtDateTime(Date dtDateTime) {
		this.dtDateTime = dtDateTime;}
	 */

	public Calendar getDtCal() {
		return dtCal;
	}
	public String getsLatitude() {
		return sLatitude;
	}

	public void setsLatitude(String sLatitude) {
		this.sLatitude = sLatitude;
	}

	public String getsLongitude() {
		return sLongitude;
	}

	public void setsLongitude(String sLongitude) {
		this.sLongitude = sLongitude;
	}

	public String getsValidity() {
		return sValidity;
	}

	public void setsValidity(String sValidity) {
		this.sValidity = sValidity;
	}

	public String getsFixQuality() {
		return sFixQuality;
	}

	public void setsFixQuality(String sFixQuality) {
		this.sFixQuality = sFixQuality;
	}

	public int getnHDOP() {
		return nHDOP;
	}

	public void setnHDOP(int nHDOP) {
		this.nHDOP = nHDOP;
	}

	public int getnTotalSat() {
		return nTotalSat;
	}

	public void setnTotalSat(int nTotalSat) {
		this.nTotalSat = nTotalSat;
	}
	
	
}
