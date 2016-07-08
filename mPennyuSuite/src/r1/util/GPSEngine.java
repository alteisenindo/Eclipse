package r1.util;

import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import r1.util.iCCConstants.DateTimeFormat;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.Toast;
import android.app.Activity;

public class GPSEngine implements LocationListener 
{
	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
	
	LocationManager locationManager;	
	GPSData m_gpsData;
	GPSData m_gpsDataOld;
	INIFile m_settings;
	R1LogFile m_logFile;
	Lock m_lock;
	
	String m_sLineBuffer;
	boolean m_bRegister = false;
	
	// thread	
	private ThGPSDataParser m_ThParser;
	private volatile boolean m_bThParser = false;
	private volatile StringBuilder m_sbBuffer;
		
	//boolean m_bLogGPS;
	
	private String[] PROVIDERS_NAME = new String[] 
		{ 
			LocationManager.GPS_PROVIDER,
			LocationManager.NETWORK_PROVIDER 
		};
	
	OnGPSEngineListener OnGPSEvent = null; // this is the equivalent event in C#
	
	public GPSEngine(LocationManager locMan, INIFile settings, R1LogFile logFile)
	{		
		locationManager = locMan; // must be created in activity
		m_settings = settings;
		m_logFile = logFile;
		
		m_gpsData = new GPSData(LocationManager.GPS_PROVIDER);
		m_gpsDataOld = new GPSData(LocationManager.GPS_PROVIDER);
		m_lock = new ReentrantLock();		
		m_sLineBuffer = "";				
		
		// Thread --> for External BT GPS
	  /*m_sbBuffer = new StringBuilder();
	  m_bThParser = true;
	  m_ThParser = new ThGPSDataParser();
	  m_ThParser.start();*/
		
		//m_logFile = logFile;
		//m_bLogGPS = bLogGPS;
    
		/*for (String provider : PROVIDERS_NAME) 
		{
	    locationManager.requestLocationUpdates(
	    		provider, 
	    		MINIMUM_TIME_BETWEEN_UPDATES, 
	    		MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
	    		this);
	    
	    Location location = locationManager.getLastKnownLocation(provider);
			if (location == null)
				continue;
			else
				break;
		}
        
    // post event immediately to get the cached gps loc
    onLocationChanged(null);*/
	}
	public void Cleanup()
	{
		m_bThParser = false;
		
		/*try 		
    {
			m_ThParser.join();
    }
		catch (InterruptedException e) 
		{}*/
	}
	public void ClearGPSBuffer()
	{
		try
		{
			if (getM_sbBuffer().length() > 0)		
			getM_sbBuffer().delete(0, getM_sbBuffer().length()-1);
		}
		catch (Exception ex)
		{}			
	}
	
	public class ThGPSDataParser extends Thread
	{
		@Override
		public void run() 
		{
			while (m_bThParser)
			{				
				String sLine = "";								
				int nPos = getM_sbBuffer().indexOf("\n");
				if (nPos >= 0)
				{
					// get a line
					sLine = getM_sbBuffer().toString().substring(0,nPos+1);
					
					//m_logFile.Log("Pos: " + nPos + ", Parsing line: " + sLine.trim(), true);										
					
					// delete line from buffer
					getM_sbBuffer().delete(0, nPos + 1);
					
					// parse line
					if (sLine.indexOf("GPRMC") >= 0 ||
							sLine.indexOf("GPGGA") >= 0)
					{	        	
						ParseGPSData(sLine.trim());
					}
				}
				//else
					//m_logFile.Log("Pos: " + nPos + ", Length: " + getM_sbBuffer().length(), true);
				
				if (getM_sbBuffer().length() <= 10)
				{
					try 
					{
						Thread.sleep(250);
					}
					catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	public void AppendGPSData(String sData)
	{
		getM_sbBuffer().append(sData);
	}
		
	/*public void AppendGPSData(String sData)
	{		
		m_lock.lock();
		
		try
    {		
			StringBuilder sb = new StringBuilder();
			sb.append(sData);
			
			// if there's a line in the buffer then add it to array
			if (m_sLineBuffer.length() > 0)
			{
				sb.insert(0, m_sLineBuffer);
				m_sLineBuffer = "";
			}
			
			String[] strArr = sb.toString().split("\\$");			
      int nLength = strArr.length;

      if (nLength <= 0)
          return;            

      // if the last line is cut then save it to buffer
      if (strArr[strArr.length - 1].indexOf("\n") < 0)
      {
        m_sLineBuffer = strArr[strArr.length - 1];
        strArr[strArr.length - 1] = "";
        nLength--;
      }

      for (int i = 0; i < nLength; i++)
      {
        if (strArr[i].indexOf("GPRMC") >= 0 ||
            strArr[i].indexOf("GPGGA") >= 0)
        	ParseGPSData(strArr[i].trim());
      }						
    }
		catch (Exception ex)
		{
			m_sLineBuffer = "";		
			//m_logFile.Log("Error in AppendGPSData()", true);
		}
		
		m_lock.unlock();
	}*/
	
	private void ParseGPSData(String sText)
	{
		String[] colArr = sText.split(",");
    boolean update = false;
    
    //m_logFile.Log("Parsing GPS Data, Total Column: " + colArr.length+ ", Line: " + sText, true);    

    if (colArr.length <= 0)
        return;
    if (colArr.length < 12)
        return;    
    
    if (colArr[0].indexOf("GPRMC") >= 0) //&& colArr[2].CompareTo("A") == 0)
    {      
      try
      {
        // Checksum                    
        try
        {                                                
          //int nPos = sText.indexOf("\\*");
          //String sCRC = sText.substring(nPos + 1, nPos + 1 + 3);
        	String sCRC = sText.substring(sText.length() - 2);
        	String sResCRC = GetCheckSum(sText.substring(0, sText.length()-3));                       

          if (sCRC.trim().equals(sResCRC.trim()))
          	m_gpsData.setnSignalStatus(1);
          else 
          	m_gpsData.setnSignalStatus(0);
        }
        catch (Exception ex)
        {
        	m_gpsData.setnSignalStatus(0);          
        }
        
        //validity
        if (colArr[2].length() > 0)
        	m_gpsData.setsValidity(colArr[2]);
        else
        	m_gpsData.setsValidity("V");            

        // Latitude        
        if (colArr[3].length() > 0)
        {
          double deg = Double.parseDouble(colArr[3].substring(0, 2));
          double min = Double.parseDouble(colArr[3].substring(2));
          
          m_gpsData.setLatitude(deg + min / 60.0);          
          if (colArr[4].equalsIgnoreCase("s"))          
          	m_gpsData.setLatitude(m_gpsData.getLatitude() * -1);

          m_gpsData.setnSignalStatus(1);
        }
        else
        {
        	m_gpsData.setnSignalStatus(0);
        }

        // Longitude       
        if (colArr[5].length() > 0)
        {
        	double deg = Double.parseDouble(colArr[5].substring(0, 3));
          double min = Double.parseDouble(colArr[5].substring(3));        
          
          m_gpsData.setLongitude(deg + min / 60.0);          
          if (colArr[4].equalsIgnoreCase("w"))          
          	m_gpsData.setLongitude(m_gpsData.getLongitude() * -1);                    
        }
        
        // Speed                    
        if (colArr[7].length() > 0)        
        	m_gpsData.setSpeed((float) (Double.parseDouble(colArr[7]) * 1.852));                  
        else        
        	m_gpsData.setSpeed(0);        

        // Course
        if (colArr[8].length() > 0)        
        	m_gpsData.setBearing(Float.parseFloat(colArr[8]));                  
        else        
          m_gpsData.setBearing(0);
        
        // Datetime
        if (colArr[9].length() > 0 && colArr[1].length() > 0)
        {        	            
          int day = Integer.parseInt(colArr[9].substring(0, 2));
          int month = Integer.parseInt(colArr[9].substring(2, 4));
          int year = Integer.parseInt(colArr[9].substring(4));
          int hour = Integer.parseInt(colArr[1].substring(0, 2));
          int min = Integer.parseInt(colArr[1].substring(2, 4));
          int sec = Integer.parseInt(colArr[1].substring(4, 6));
          
          Calendar cal = Calendar.getInstance();
          cal.set(2000 + year, month-1, day, hour, min, sec);
          
          // convert to real time
          cal.add(Calendar.HOUR_OF_DAY, m_settings.getIntegerProperty("Settings", "Timezone"));
          
          m_gpsData.setTime(cal.getTimeInMillis());          
        }
        
        if (!
        		(Integer.parseInt(m_gpsData.getsFixQuality()) >= m_settings.getIntegerProperty("GPSSettings","GPSFixQuality") &&
            (m_gpsData.getsValidity().equalsIgnoreCase("a") || m_gpsData.getsValidity().equalsIgnoreCase(m_settings.getStringProperty("GPSSettings","GPSValidity"))) && // "V" = take all gps data, "A" = valid only
            (m_gpsData.getnTotalSat() >= m_settings.getIntegerProperty("GPSSettings","GPSNumOfSatValid")) &&
            m_gpsData.getnHDOP() <=  m_settings.getIntegerProperty("GPSSettings","GPSHDOP"))
           )
        {                             
        	m_gpsData.setnSignalStatus(0);
        }
        
        update = true;
      }
      catch (Exception ex)
      {
      	m_gpsData.setnSignalStatus(0);
      	m_logFile.Log("Error parsing GPRMC", true);
      }
    }	
    else if (colArr[0].indexOf("GPGGA") >= 0)
    {
    	try
      {
    		// fix quality
        if (colArr[6].length() > 0)
            m_gpsData.setsFixQuality(colArr[6]);
        else
            m_gpsData.setsFixQuality("0");

        // total satellite
        if (colArr[7].length() > 0)        
        	m_gpsData.setnTotalSat(Integer.parseInt(colArr[7]));                    
        else        
        	m_gpsData.setnTotalSat(0);        

        if (colArr[8].length() > 0)        
        	m_gpsData.setnHDOP((int)Float.parseFloat(colArr[8]));
        else
        	m_gpsData.setnHDOP(50);
        
        // Checksum                    
        try
        {                                                
          //int nPos = sText.indexOf("\\*");
          //String sCRC = sText.substring(nPos + 1, nPos + 1 + 3);
        	String sCRC = sText.substring(sText.length() - 2);
        	String sResCRC = GetCheckSum(sText.substring(0, sText.length()-3));                       

          if (sCRC.trim().equals(sResCRC.trim()))
          	m_gpsData.setnSignalStatus(1);
          else 
          	m_gpsData.setnSignalStatus(0);
        }
        catch (Exception ex)
        {
        	m_gpsData.setnSignalStatus(0);          
        }
                
        if (m_gpsData.getnHDOP() > m_settings.getIntegerProperty("GPSSettings", "GPSHDOP"))        
          m_gpsData.setnSignalStatus(0);                                     
      }
      catch (Exception ex)
      { 
      	m_logFile.Log("Error parsing GPGGA", true);
      }
    }
            
    if (update)
    {   
    	Calendar cal = Calendar.getInstance();
  		cal.setTimeInMillis(m_gpsData.getTime());
  		
    	/*String str = String.format("%s,%s,%s", m_gpsData.getLatitude(), m_gpsData.getLongitude(), 
    			R1Util.GetDateTime(cal.getTime(), DateTimeFormat.DDMMYYYY));  	
    	m_logFile.Log("Line parsed OK: " + str, true);*/
  		
  		if (m_gpsDataOld.getLatitude() != m_gpsData.getLatitude() &&
  				m_gpsDataOld.getLongitude() != m_gpsData.getLongitude())
  		{
	  		m_gpsDataOld.setLatitude(m_gpsData.getLatitude());
	  		m_gpsDataOld.setLongitude(m_gpsData.getLongitude());
	    	
	    	if (OnGPSEvent != null && 
	    			m_settings.getStringProperty("GPSSettings", "GPSUseExternalBT").equalsIgnoreCase("on"))
	  			OnGPSEvent.onNewLocation(m_gpsData, null);
  		}
    }
	}
	
	private String GetCheckSum(String msg) 
	{
    // perform NMEA checksum calculation
    int chk = 0;
    //run through each character of the message length
    //and XOR the value of chk with the byte value
    //of the character that is being evaluated
    for (int i = 1; i < msg.length(); i++) {
        chk ^= msg.charAt(i);
    }

    //convert the retreived integer to a HexString in uppercase
    String chk_s = Integer.toHexString(chk).toUpperCase();

    // checksum must be 2 characters!
    // if it falls short, add a zero before the checksum
    while (chk_s.length() < 2) {
        chk_s = "0" + chk_s;
    }

    //show the calculated checksum
    // System.out.println("    calculated checksum : " + chk_s);

    //return the calculated checksum
    return chk_s;
	}
		

	@Override
	public void onLocationChanged(Location loc) 
	{	
		/*if (m_bLogGPS)
		{
			String str = String.format("[GPS]%1$s, %2$s", location.getLongitude(), location.getLatitude());		
			m_logFile.Log(str, true);
		}*/
				
		if (OnGPSEvent != null && 
				!m_settings.getStringProperty("GPSSettings", "GPSUseExternalBT").equalsIgnoreCase("on"))
			OnGPSEvent.onNewLocation(
					locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER),
					locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
					);
	}

	@Override
	public void onProviderDisabled(String provider) 
	{		
	}

	@Override
	public void onProviderEnabled(String provider) 
	{		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) 
	{		
	}
	
	public Location InitData() // MAY RETURN NULL
	{		
		//return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		Location location = null;
		if (m_settings.getStringProperty("GPSSettings", "GPSUseExternalBT").equalsIgnoreCase("on") || 
				m_bRegister)
			return null;
		
		for (String provider : PROVIDERS_NAME) 
		{
	    locationManager.requestLocationUpdates(
	    		provider, 
	    		MINIMUM_TIME_BETWEEN_UPDATES, 
	    		MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
	    		this);
	    
	    location = locationManager.getLastKnownLocation(provider);
			if (location == null)
				continue;			
		}
		
		m_bRegister = true;
		return location;
	}						
	
	public void Stop()
	{
		locationManager.removeUpdates(this);
		m_bRegister = false;
	}
		
	public synchronized GPSData getM_gpsData() {
		return m_gpsData;
	}	
	public synchronized StringBuilder getM_sbBuffer() {
		return m_sbBuffer;
	}

	//Define our custom Listener interface
	public interface OnGPSEngineListener 
	{
		public abstract void onNewLocation(Location locGPS, Location locNetwork);		
	}

  //Allows the user to set a Listener and react to the event
	public void setOnGPSEngineListener(OnGPSEngineListener listener) 
	{
		OnGPSEvent = listener;		
	}	
}
