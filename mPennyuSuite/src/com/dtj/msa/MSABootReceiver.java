package com.dtj.msa;

import r1.util.R1Util;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;

public class MSABootReceiver extends BroadcastReceiver
{	
	@Override
	public void onReceive(Context arg0, Intent arg1) 
	{
		/*Intent intentone = new Intent(arg0.getApplicationContext(), MainActivity.class);
		intentone.putExtra("FromBoot", 1);
  	intentone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  	intentone.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
  	arg0.getApplicationContext().startActivity(intentone);*/
					
		if (!R1Util.IsServiceRunning(arg0.getApplicationContext(), "com.dtj.msa", "com.dtj.msa.MSAService"))
		{
			if (android.os.Build.VERSION.SDK_INT > 9) 
	 		{
	      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	      StrictMode.setThreadPolicy(policy);
	 		}
			
			/*Intent fake = new Intent(arg0.getApplicationContext(), MSAFakeService.class);
			arg0.getApplicationContext().startService(fake);*/
			
			/*try 
			{
				Thread.sleep(200);
			} 
			catch (Exception e) 
			{}*/
			
			Intent run = new Intent(arg0.getApplicationContext(), MSAService.class);
			arg0.getApplicationContext().startService(run);
		}
	}		
}
