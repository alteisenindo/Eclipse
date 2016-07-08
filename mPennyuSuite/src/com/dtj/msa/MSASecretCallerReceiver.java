package com.dtj.msa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MSASecretCallerReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context arg0, Intent arg1)  
  {		
		try
		{
			if(arg1.getAction().equals("android.provider.Telephony.SECRET_CODE"))
			{
				Intent intentone = new Intent(arg0.getApplicationContext(), MainActivity.class);
		  	//intentone.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		  	intentone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		  	intentone.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		  	arg0.getApplicationContext().startActivity(intentone);
			}
		}
		catch (Exception ex)
		{}
  }		
}
