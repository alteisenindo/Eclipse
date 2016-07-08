package com.dtj.msa;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.Executors;

import javax.crypto.spec.DESedeKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.dtj.email.MailConstants;
import com.dtj.email.MailReader;
import com.dtj.email.MailReader.OnMailReaderListener;
import com.dtj.email.MailSender;
import com.dtj.email.MailSender.OnMailSenderListener;

import r1.util.GPSData;
import r1.util.GPSEngine;
import r1.util.INIFile;
import r1.util.R1LogFile;
import r1.util.R1Util;
import r1.util.iCCConstants;
import r1.util.GPSEngine.OnGPSEngineListener;
import r1.util.iCCConstants.DateTimeFormat;
import r1.util.iCCConstants.EPumaQueryResult;
import r1.util.iCCConstants.EngineEventTarget;
import r1.util.iCCConstants.EngineEventType;
import r1.util.iCCConstants.GPSMode;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class Engine 
{
	Context m_ctx;
	Handler m_handler;
	
	MailReader m_mailReader;
	MailSender m_mailSender;	
	
	GPSEngine m_gps;
	GPSData m_gpsData;
	GPSData m_gpsDataValid;
	LocationManager m_locMan;
	R1LogFile m_logGPS;
	boolean m_bLogGPS = false;
	R1LogFile m_logSystem;
	
	INIFile m_settings = null;
	String m_sINIPath;
	
	//boolean m_bWaitThread = false;
	OnMailReaderListener m_readerEvent;
	
	public Engine(Context ctx, OnMailSenderListener sender, OnMailReaderListener reader, R1LogFile logSystem)
	{
		m_ctx = ctx;
		m_readerEvent = reader;
		
		m_sINIPath = m_ctx.getFilesDir().getPath() + "/msa/Ini/Settings.ini";
		
		//m_settings = new INIFile(m_sINIPath);
		//String str = m_settings.getStringProperty("MSASettings", "AppStatus");
		
		m_settings = R1Util.ReadEncryptedSettings(m_ctx, m_sINIPath);
		
		/*String sFolder = R1Util.GetDLFolder() + "/MSALog";	  	
	  if (!R1Util.IsFileExists(sFolder))
	  {
	  	File f = new File(sFolder);
	  	f.mkdir();
	  }
		
		m_logSystem = new R1LogFile(sFolder + "/ServiceLog.txt", 1024, 
	  		"Service Log", false);*/
		
		m_logSystem = logSystem;
		
		m_mailSender = new MailSender(m_ctx, m_settings, m_sINIPath);
	  m_mailSender.setOnMailSenderListener(sender);
	  
	  m_mailReader = new MailReader(m_ctx, m_mailSender, m_settings, m_sINIPath, m_logSystem);
	  m_mailReader.setOnMailReaderListener(reader);	  
	  	  	  
	  /*m_logGPS = new R1LogFile(sFolder + "/gps.txt", 1024, 
	  		"=========================\nMobile Suite GPS Log\n=========================", false);				
	  
	  m_locMan = (LocationManager) m_ctx.getSystemService(Context.LOCATION_SERVICE);
	  m_gps = new GPSEngine(m_locMan, m_settings, m_logGPS);
		m_gps.setOnGPSEngineListener(gpslistener);
		m_locMan.addGpsStatusListener(GPSStatListener);
	  
	  m_gpsData = new GPSData("");
	  m_gpsDataValid = new GPSData("");
	  
	  InitGPSData();*/	  	  
	  
	  //m_mailReader.Connect();	  	  
	  
	  // get app status	  
	  //String sAppStatus = R1Util.DESEnryption(false, m_settings.getStringProperty("MSASettings", "AppStatus"));
	  	  
	  String sAppStatus = m_settings.getStringProperty("MSASettings", "AppStatus");
	  
	  // check app status
	  if (sAppStatus.equals(MailConstants.APP_ENABLED))
	  {
	  	/*Intent br = new Intent();
  		br.setAction("com.dtj.mail.ENABLED");
			m_ctx.getApplicationContext().sendBroadcast(br);
			
			Intent br2 = new Intent();
  		br2.setAction("org.sipdroid.ENABLED");
			m_ctx.getApplicationContext().sendBroadcast(br2);
			
			Intent br3 = new Intent();
  		br3.setAction("com.xabber.android.ENABLED");
			m_ctx.getApplicationContext().sendBroadcast(br3);*/
			
			if (!R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexService"))
			{
				Intent intent = new Intent(m_ctx, MSAMutexService.class);
				m_ctx.startService(intent);
			}
			
			if (!R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexChatService"))
			{
				Intent intent = new Intent(m_ctx, MSAMutexChatService.class);
				m_ctx.startService(intent);
			}
			
			if (!R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexMailService"))
			{
				Intent intent = new Intent(m_ctx, MSAMutexMailService.class);
				m_ctx.startService(intent);
			}
	  }
	  else
	  {
	  	/*Intent br = new Intent();
  		br.setAction("com.dtj.mail.DISABLED");
			m_ctx.getApplicationContext().sendBroadcast(br);
			
			Intent br2 = new Intent();
  		br2.setAction("org.sipdroid.DISABLED");
			m_ctx.getApplicationContext().sendBroadcast(br2);
			
			Intent br3 = new Intent();
  		br3.setAction("com.xabber.android.DISABLED");
			m_ctx.getApplicationContext().sendBroadcast(br3);*/
	  	
	  	StopServices();				  	
	  }
	}
	
	private void AddLog(String str)
	{
		//m_logSystem.Log(str, true);
	}
	
	/*private void StartWaitThread()
	{
		if (m_bWaitThread)
			return;
			
		m_bWaitThread = true;
		AddLog("Engine StartWaitThread 1");
		
		Executors.newSingleThreadExecutor().execute(new Runnable() 
  	{   	
  		@Override
  		public void run() 
  		{  			  			
  			try 
  			{
					Thread.sleep(15*1000);
				} 
  			catch (Exception e) 
				{}
  			
  			m_MailReaderHandler.sendEmptyMessage(1);
  		}
  	});
	}
	
	Handler m_MailReaderHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{			
			switch (msg.what)
			{
				case 0:
				{										
					StartWaitThread();
					break;
				}
				case 1:
				{
					AddLog("Engine MailReaderHandler 1");
					
					m_bWaitThread = false;
					
					MailReader mr = new MailReader(m_ctx, m_mailSender, m_settings, m_sINIPath, m_logSystem, m_MailReaderHandler);
					mr.setOnMailReaderListener(m_readerEvent);
					mr.Connect();
					
					break;
				}
			}
		}
	};*/
	
	private void StopServices()
	{
		if (R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexService"))
		{
			Intent intent = new Intent();
			intent.setAction(MSAMutexService.ACTION);
			intent.putExtra("RQS", MailConstants.ServiceCmd.StopService.Value());
			m_ctx.sendBroadcast(intent);
		}
  	
  	if (R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexChatService"))
		{
			Intent intent = new Intent();
			intent.setAction(MSAMutexChatService.ACTION);
			intent.putExtra("RQS", MailConstants.ServiceCmd.StopService.Value());
			m_ctx.sendBroadcast(intent);
		}
  	
  	if (R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexMailService"))
		{
			Intent intent = new Intent();
			intent.setAction(MSAMutexMailService.ACTION);
			intent.putExtra("RQS", MailConstants.ServiceCmd.StopService.Value());
			m_ctx.sendBroadcast(intent);
		}
	}
	
	public void InitGPSData()
	{
		/*double dLat = m_locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
		double dLon = m_locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
		
		m_gpsData.setLatitude(dLat);
		m_gpsData.setLongitude(dLon);*/
		
		try
		{
		// set gps data to last known location				
			Location loc = m_gps.InitData(); // DANGER!! --> MAY RETURN NULL
			if (loc == null)
				return;
			
	    //m_gpsData.setLatitude(loc.getLatitude()); 
			//m_gpsData.setLongitude(loc.getLongitude());
			m_gpsData.set(loc);
		}
		catch (Exception ex)
		{}
	}
	
	private GpsStatus.Listener GPSStatListener = new GpsStatus.Listener()
	{
		@Override
		public void onGpsStatusChanged(int event) 
		{
			//GpsStatus stat = m_locMan.getGpsStatus(null);
			/*if(event != iCCConstants.GPS_EVENT_STOPPED)
				BroadcastEvent(EngineEventTarget.Any, EngineEventType.GPSLocation, new EngineVars(m_gpsData));*/
		}		
	};
	
	private OnGPSEngineListener gpslistener = new OnGPSEngineListener() 
	{
		@Override
		public void onNewLocation(Location locGPS, Location locNetwork) 
		{			
			try
			{
				if (locGPS == null && locNetwork == null)
					return;
				
				Location loc = null;
				if (locGPS != null)
					loc = locGPS;
				else
					loc = locNetwork;
												
				/*if (m_settings.getStringProperty("GPSSettings", "GPSUseExternalBT").equalsIgnoreCase("on"))
				{
					GPSData data = (GPSData) loc;
					m_gpsData.set(data);
					m_gpsData.setnSignalStatus(data.getnSignalStatus());
					m_gpsData.setsValidity(data.getsValidity());
					
					if (m_gpsData.getnSignalStatus() == 1)
					{
						m_gpsDataValid.set(data);
						m_gpsDataValid.setnSignalStatus(data.getnSignalStatus());
						m_gpsDataValid.setsValidity(data.getsValidity());
					}
				}*/
				
				//else
				//{
					m_gpsData.set(loc);
					m_gpsData.getDtCal().setTimeInMillis(loc.getTime());
				
					int nTotalSat = 0;
					GpsStatus stat = m_locMan.getGpsStatus(null);							
					Iterable<GpsSatellite> iSatellites = stat.getSatellites();
			    Iterator<GpsSatellite> it = iSatellites.iterator();			    
			    while (it.hasNext())
			    {
			      GpsSatellite oSat = (GpsSatellite) it.next() ;
			      if (oSat.usedInFix())
			      	nTotalSat++;      
			    }
			    m_gps.getM_gpsData().setnTotalSat(nTotalSat);							
		    		    
					if ((loc.hasAccuracy() && loc.getAccuracy() < 30 && loc.getAccuracy() >= 0) ||
							m_gps.getM_gpsData().getnTotalSat() >= m_settings.getIntegerProperty("GPSSettings", "GPSNumOfSatValid"))
					{
						m_gpsDataValid.set(loc);
						m_gpsDataValid.getDtCal().setTimeInMillis(loc.getTime());
						m_gpsDataValid.setnSignalStatus(1);			
														
						m_gpsData.setnSignalStatus(1);				
					}
					else
						m_gpsData.setnSignalStatus(0);
				//}
				
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(loc.getTime());						
				
				/*int nMode = m_settings.getIntegerProperty("GPSSettings", "GPSMode");
				if(GPSMode.lookup(nMode) != GPSMode.TimeBased)			
					AnalyzeGPSData();*/
										
				// broadcast gps location event
				//BroadcastEvent(EngineEventTarget.Any, EngineEventType.GPSLocation, new EngineVars(m_gpsData));
				
				// log gps and broadcast log gps event
				if (m_bLogGPS)
				{
					String str2 = String.format("[GPS] Lon: %3.6f, Lat: %3.6f, Spd: %3.2f, DTime: %s, Acc: %s",
							loc.getLongitude(),
							loc.getLatitude(), 						
							loc.getSpeed(), 
							R1Util.GetDateTime(cal.getTime(), DateTimeFormat.DDMMYYYY), 
							loc.getAccuracy());
					
					m_logGPS.Log(str2, true);
				}			
				
				/*if (m_settings.getStringProperty("ArgoSettings", "ArgoGPS").equalsIgnoreCase("on"))
					m_argo.onNewGPSData();*/
				
				// UNCOMMENT if XMPP is ready
				/*if (m_xmpp.IsReadyToSendGPSData())
					m_xmpp.SendGPSData(m_gpsData);*/
			}
			catch (Exception ex)
			{}
		}	
	};
			
	/*private void ReadEncryptedSettings()
	{				
		try 
		{	
			// decrypt settings		
			String sCipher = R1Util.ReadFile(m_sINIPath);			
			String sPlain = R1Util.DESEnryption(false, sCipher);
			
			// save to a temp			
			String sDest = String.format("%s/msa/Log/%s.temp", m_ctx.getFilesDir().getPath(), "" + Calendar.getInstance().getTimeInMillis());
			R1Util.SaveFile(sDest, sPlain, false);
			
			// load INI
			Thread.sleep(250);			
			m_settings = new INIFile(sDest);			
			
			// delete temp file
			R1Util.DeleteDir(new File(m_settings.getFileName()));
			Thread.sleep(250);
		}
		catch (Exception ex)
		{
			m_settings = null;
		}						
	}*/
	
	public void Cleanup()
	{
		//m_logSystem.Log("Engine Cleanup()", true);
		
		//m_gps.Cleanup();
		//m_mailReader.Disconnect(true);
		m_mailReader.Cleanup();
		StopServices();
	}

	public MailSender getM_mailSender() {
		return m_mailSender;
	}	 
		
}
