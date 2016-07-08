package com.dtj.msa;

import r1.util.R1Util;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MSACallerReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context arg0, Intent arg1)  
  {
		/*debugOut("arg0: " + arg0.toString());
    debugOut("arg1: " + arg1.toString());
    debugOut("isOrderedBroadcast = " + isOrderedBroadcast());*/
    		
		try
		{
			if (arg1.getAction().equals("android.intent.action.NEW_OUTGOING_CALL"))
			{
				String number = getResultData();
				if (number != null)
				{				
					if (number.equalsIgnoreCase("#75391"))
					{
						Intent intentone = new Intent(arg0.getApplicationContext(), MainActivity.class);
			    	//intentone.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			    	intentone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    	intentone.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			    	arg0.getApplicationContext().startActivity(intentone);
			    	
			    	setResultData(null);
			    	return;
					}
				}
			}
			/*else if (arg1.equals("android.intent.action.BOOT_COMPLETED"))
			{
				Intent intentone = new Intent(arg0.getApplicationContext(), MainActivity.class);
	    	intentone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	intentone.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    	arg0.getApplicationContext().startActivity(intentone);
			}*/
			/*else if (arg1.getAction().equals("android.intent.action.PHONE_STATE"))
			{
				String sPhoneState = arg1.getStringExtra(TelephonyManager.EXTRA_STATE);
				
				Log.e("MSACallerReceiver", sPhoneState);
				
				if (sPhoneState.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE) &&
						!R1Util.IsProcessRunning(arg0.getApplicationContext(), "com.dtj.msa"))
				{
					Intent intentone = new Intent(arg0.getApplicationContext(), MainActivity.class);
		    	//intentone.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		    	intentone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    	intentone.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		    	arg0.getApplicationContext().startActivity(intentone);
				}
			}*/
			/*else if (arg1.getAction().equals("android.net.wifi.STATE_CHANGE"))
			{
				NetworkInfo info = arg1.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
	      if(info != null) 
	      {
	        if(info.isConnected())
	        {
	          Intent intentone = new Intent(arg0.getApplicationContext(), MainActivity.class);
	   	    	//intentone.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	   	    	intentone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	   	    	intentone.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	   	    	arg0.getApplicationContext().startActivity(intentone); 
	
	          // e.g. To check the Network Name or other info:
	          //WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	          //WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	          //String ssid = wifiInfo.getSSID();
	        }
	      }
			}*/
		}
		catch (Exception ex)
		{}
  }
	
	private static void debugOut(String str) {
    Log.e("DialerReceiver", str);
}
}
