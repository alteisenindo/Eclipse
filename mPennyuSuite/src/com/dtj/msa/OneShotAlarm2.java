package com.dtj.msa;

/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * 
 * This file is part of Sipdroid (http://www.sipdroid.org)
 * 
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import java.io.File;

import r1.util.INIFile;
import r1.util.R1LogFile;
import r1.util.R1Util;
import r1.util.TinyDB;

import com.dtj.email.MailConstants;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class OneShotAlarm2 extends BroadcastReceiver 
{
	private static Context mContext;
	INIFile m_settings = null;
	String m_sINIPath;
	R1LogFile m_logSystem;
	
  @Override
	public void onReceive(Context context, Intent intent) 
  {  	
  	mContext = context;
  	
  	try
  	{
	  	m_sINIPath = mContext.getFilesDir().getPath() + "/msa/Ini/Settings.ini";			
			m_settings = R1Util.ReadEncryptedSettings(mContext, m_sINIPath);
			
			String sAppStatus = m_settings.getStringProperty("MSASettings", "AppStatus");		    	
			boolean bServiceRunning = R1Util.IsServiceRunning(mContext, "com.dtj.msa", "com.dtj.msa.MSAService");
	  		  		  	  	  	
	  	if (sAppStatus.equals(MailConstants.APP_ENABLED) && !bServiceRunning)
	  	{		  						  						
				// stop service	  	  	
	  		context.stopService(new Intent(context, MSAService.class));
	  		
	  		// start service
	  		Intent run = new Intent(context.getApplicationContext(), MSAService.class);
	  		context.getApplicationContext().startService(run);
	  	}
	  	else
	  	{	  		  		
	  		SetAlarm(300, OneShotAlarm2.class);
	  	}
  	}
  	catch (Exception ex)
  	{
  		SetAlarm(300, OneShotAlarm2.class);
  	}
  	
  	
  	/*String sFolder = R1Util.GetDLFolder() + "/MSAAlarmLog";	  	
	  if (!R1Util.IsFileExists(sFolder))
	  {
	  	File f = new File(sFolder);
	  	f.mkdir();
	  }
		
		m_logSystem = new R1LogFile(sFolder + "/AlarmLog.txt", 1024, 
	  		"Alarm Log", false);

  	try
  	{
	  	m_sINIPath = mContext.getFilesDir().getPath() + "/msa/Ini/Settings.ini";			
			m_settings = R1Util.ReadEncryptedSettings(mContext, m_sINIPath);
			
			String sAppStatus = m_settings.getStringProperty("MSASettings", "AppStatus");
		    	
	  	TinyDB db = new TinyDB(mContext);
	  	int nIsIdle = db.getInt("IsIdle", 1);
	  	//int nGUIDestroyed = db.getInt("GUIDestroyed", 0);
	  	//boolean bServiceRunning = R1Util.IsServiceRunning(mContext, "com.dtj.msa", "com.dtj.msa.MSAService");
	  	
	  	m_logSystem.Log(String.format("APP_ENABLED = %s, nIsIdle = %d", sAppStatus, nIsIdle), true);
	  	  	  	
	  	//if (nGUIDestroyed == 0 && R1Util.IsServiceRunning(mContext, "com.dtj.msa", "com.dtj.msa.MSAService"))
	  	//if (nGUIDestroyed == 1 && !bServiceRunning && sAppStatus.equals(MailConstants.APP_ENABLED))
	  	if (sAppStatus.equals(MailConstants.APP_ENABLED) && nIsIdle == 1)
	  	{		  					
	  		m_logSystem.Log("Restarting service", true);
	  		
				//Toast.makeText(context, "Service stopped!", Toast.LENGTH_SHORT).show();			
				
				// set alarm to start service in 5 sec --> MSAService MUST NOT DIE
				//SetAlarm(5, StartServiceAlarm.class);
				
				// stop service	  	  	
	  		context.stopService(new Intent(context, MSAService.class));
	  		
	  		// start service
	  		Intent run = new Intent(context.getApplicationContext(), MSAService.class);
	  		context.getApplicationContext().startService(run);
	  	}
	  	else
	  	{
	  		m_logSystem.Log("Restarting alarm", true);
	  		
	  		//String str = String.format("Nothing to do, sGUIDestroyed = %d, bServiceRunning = %s, sAppStatus = %s", 
	  				//nGUIDestroyed, (bServiceRunning) ? "true" : "false", sAppStatus);	  		
	  		//Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
	  		
	  		SetAlarm(600, OneShotAlarm2.class);
	  	}
  	}
  	catch (Exception ex)
  	{
  		m_logSystem.Log("Exception: " + ex.getMessage() + ", Restarting alarm", true);
  		SetAlarm(600, OneShotAlarm2.class);
  	}*/
  }
  
  public void SetAlarm(int nTimeInSecond, Class <?>cls) 
	{ 		
    Intent intent = new Intent(mContext, cls);
    PendingIntent sender = PendingIntent.getBroadcast(mContext,0, intent, 0);
    
    AlarmManager am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
    am.cancel(sender);
    
    if (nTimeInSecond > 0)
    	am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + nTimeInSecond*1000, sender);        
	}
}
