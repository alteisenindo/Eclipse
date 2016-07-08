package com.dtj.msa;

import java.util.List;

import com.dtj.email.MailConstants;

import r1.util.INIFile;
import r1.util.R1Util;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class MSAReceiver extends BroadcastReceiver
{
  @Override
  public void onReceive(Context context, Intent intent) 
  {
  	try
  	{
	  	if(intent.getAction().equals("com.dtj.msa.START_SERVICE")) 
	  	{  		  		  	
	  		if (!R1Util.IsServiceRunning(context.getApplicationContext(), "com.dtj.msa", "com.dtj.msa.MSAService"))
	  		{
	  			if (android.os.Build.VERSION.SDK_INT > 9) 
	  	 		{
	  	      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	  	      StrictMode.setThreadPolicy(policy);
	  	 		}
	  			
	  			/*Intent fake = new Intent(context.getApplicationContext(), MSAFakeService.class);
	  			context.getApplicationContext().startService(fake);
	  			
	  			Thread.sleep(200);*/
    			
	  			Intent run = new Intent(context.getApplicationContext(), MSAService.class);
	  			context.getApplicationContext().startService(run);
	  		}
	  	}
	  	else if(intent.getAction().equals("com.dtj.msa.STOP_SERVICE"))
	  	{  
	  		if (R1Util.IsServiceRunning(context.getApplicationContext(), "com.dtj.msa", "com.dtj.msa.MSAService"))
	  		{
	  			Intent run = new Intent();
	  			run.setAction(MSAService.ACTION);
	  			run.putExtra("RQS", MailConstants.ServiceCmd.StopService.Value());
					context.getApplicationContext().sendBroadcast(run);
	  		}
	  	}  	
	  	else if(intent.getAction().equals("com.dtj.msa.ACTIVATE"))
	  	{  
	  		if (R1Util.IsServiceRunning(context.getApplicationContext(), "com.dtj.msa", "com.dtj.msa.MSAService"))
	  		{
	  			Intent run = new Intent();
	  			intent.setAction(MSAService.ACTION);
					intent.putExtra("RQS", MailConstants.ServiceCmd.Activation.Value());
					context.getApplicationContext().sendBroadcast(run);
	  		}
	  	}
	  	else if(intent.getAction().equals("com.dtj.msa.DEFCONFIG"))
	  	{  
	  		if (R1Util.IsServiceRunning(context.getApplicationContext(), "com.dtj.msa", "com.dtj.msa.MSAService"))
	  		{
	  			Intent run = new Intent();
	  			intent.setAction(MSAService.ACTION);
					intent.putExtra("RQS", MailConstants.ServiceCmd.DefConfig.Value());
					context.getApplicationContext().sendBroadcast(run);
	  		}
	  	}
	  	else if(intent.getAction().equals("com.dtj.msa.RUN_APP"))
	  	{  
	  		Intent intentone = new Intent(context.getApplicationContext(), MainActivity.class);
	    	//intentone.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	    	intentone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	intentone.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    	context.getApplicationContext().startActivity(intentone);
	  	}
	  	else if (intent.getAction().equals("com.dtj.msa.QUERY_SETUP"))
			{				
	  		try
	  		{
		  		String sFeat = intent.getStringExtra("Feat");	  		
		  		String sINIPath = context.getFilesDir().getPath() + "/msa/Ini/Settings.ini";
		  		
		  		if (sFeat.equalsIgnoreCase("voip"))
		  		{
						//Log.v("TEST SETUP 1", "Receiving com.dtj.msa.QUERY_SETUP_PUMAMAIL");					
						INIFile settings = R1Util.ReadEncryptedSettings(context.getApplicationContext(), sINIPath);
						String sUsername = settings.getStringProperty("MSASettings", "voip.username");
						String sPassword = settings.getStringProperty("MSASettings", "voip.password");
						String sServer = settings.getStringProperty("MSASettings", "VoipServer");
						
						//Log.v("TEST SETUP 1", String.format("%s, %s, %s", sUsername, sPassword, sServer));
						
						Intent br = new Intent();
						br.setAction("org.sipdroid.MSA_SETUP_PARAM");
						br.putExtra("username", sUsername);
						br.putExtra("password", sPassword);
						br.putExtra("server", sServer);
						context.getApplicationContext().sendBroadcast(br);
		  		}
		  		else if (sFeat.equalsIgnoreCase("mail") || sFeat.equalsIgnoreCase("chat"))
		  		{
		  			INIFile settings = R1Util.ReadEncryptedSettings(context.getApplicationContext(), sINIPath);
		  			String sUsername = settings.getStringProperty("MSASettings", "MailXMPP");			
		  			String sPassword = settings.getStringProperty("MSASettings", "MailXMPPPwd");
											
		  			if (sFeat.equalsIgnoreCase("mail"))
		  			{
			  			Intent br = new Intent();
							br.setAction("com.dtj.mail.SETUP_PUMAMAIL");
							br.putExtra("username", sUsername);
							br.putExtra("password", sPassword);				
							context.getApplicationContext().sendBroadcast(br);
		  			}
		  			else
		  			{
		  				Intent br = new Intent();
		  				br.setAction("com.xabber.android.SETUP_XMPP");
		  				br.putExtra("username", sUsername);
		  				br.putExtra("password", sPassword);				
		  				context.getApplicationContext().sendBroadcast(br);
		  			}
		  		}
		  		else if (sFeat.equalsIgnoreCase("puma")) 
		  		{
		  			INIFile settings = R1Util.ReadEncryptedSettings(context.getApplicationContext(), sINIPath);
		  			String sFeatPuma = settings.getStringProperty("MSASettings", "feature.puma");
		  			String sAppStatus = settings.getStringProperty("MSASettings", "AppStatus");		  			
		  			
		  			Intent br = new Intent();
						br.setAction("r1.puma.PUMA_QUERY_RESULT");
						br.putExtra("MSA_Enabled", (sAppStatus.equals(MailConstants.APP_ENABLED)) ? "1" : "0");
						br.putExtra("feature.puma", sFeatPuma);									
						context.getApplicationContext().sendBroadcast(br);
		  		}
	  		}
	  		catch (Exception ex)
	  		{}
			}
	  	
	  	/*else if (intent.equals(Intent.ACTION_NEW_OUTGOING_CALL))
	    {
	  		String number = getResultData();
	  		if (number == null)
	  		{
	  			setResultData(null);
	      	return;
	  		}
	  		
	  		if (number.equalsIgnoreCase("75391"))
	  		{
	  			Intent intentone = new Intent(context.getApplicationContext(), MainActivity.class);
	      	//intentone.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	      	intentone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	      	intentone.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	      	context.getApplicationContext().startActivity(intentone);
	  		}
	  		
	  		setResultData(null);
	    	return;
	    }*/
  	}
  	catch (Exception ex)
  	{}
  }    
}
