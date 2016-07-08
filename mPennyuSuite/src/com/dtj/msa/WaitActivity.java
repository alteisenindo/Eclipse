package com.dtj.msa;

import r1.util.INIFile;
import r1.util.R1Util;
import r1.util.iCCConstants;

import com.dtj.email.MailConstants;
import com.dtj.email.TinyDB;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WaitActivity extends Activity 
{
	TextView m_lblTimer;
	TinyDB m_db;
	int m_nTimer;
	volatile int m_nActivated = 0;
	volatile int m_nDefConfig = 0;
	volatile int m_nGetPasswordList = 0;
	volatile boolean m_bThreadWait = true;
	
	@Override
  protected void onCreate(Bundle savedInstanceState) 
  {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.waitact);
	  
	  IntentFilter recvFilter = new IntentFilter();
	  recvFilter.addAction("com.dtj.msa.MSA_ACTIVATION_REPLY");	  
	  recvFilter.addAction("com.dtj.msa.MSA_DEFCONFIG_REPLY");
	  recvFilter.addAction("com.dtj.msa.MSA_GETPASSWORDLIST_REPLY");
	  recvFilter.setPriority(500);
 		registerReceiver(MSAWaitReceiver, recvFilter);
	  	  
	  m_db = new TinyDB(getApplicationContext());
	  m_nTimer = m_db.getInt("RegisterWaitTimer", 0);	  	  
	  
	  m_lblTimer = (TextView) findViewById(R.id.lblTimer);
	  m_lblTimer.setText(String.valueOf(m_nTimer));
	  
	  ((Button) findViewById(R.id.btnCancel)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
    	{	      		      	    
    		m_db.putInt("RegisterWaitTimer", 0);
    		m_bThreadWait = false;    		
      }
    });
	  
	  // send cmd	  
	  Intent intent = new Intent();
		intent.setAction(MSAService.ACTION);
		intent.putExtra("RQS", MailConstants.ServiceCmd.Activation.Value());
		getApplicationContext().sendBroadcast(intent);
	  
	  new Thread() 
		{
			@Override
			public void run() 
			{
				RunTimer();
			}
		}.start();
  }
			
	private void RunTimer()
	{
		while (m_nTimer > 0 && m_bThreadWait)
		{
			try 
			{
				Thread.sleep(1000);
			} catch (Exception e) 
			{}
					      
			m_nTimer--;
			m_db.putInt("RegisterWaitTimer", m_nTimer);
			
			m_TimerHandler.sendEmptyMessage(1);
			
			// check for activation reply
			if (m_nActivated != 0)
				break;
		}				
		
		if (m_nActivated == 1)
		{			
			//INIFile settings = R1Util.ReadEncryptedSettings(getApplicationContext(), getFilesDir().getPath() + "/msa/Ini/Settings.ini");
			//String sStatusChat = settings.getStringProperty("MSASettings", "feature.chat"); // 0 = disabled, 1 = enabled, 2 = unset
  		//String sStatusCall = settings.getStringProperty("MSASettings", "feature.voip");  		
  		
  		// wait a while so mail sender wont get clogged up
			try 
			{
				Thread.sleep(2000);
			} catch (Exception e) 
			{}
			
			// get defconfig
			Intent intent = new Intent();
			intent.setAction(MSAService.ACTION);
			intent.putExtra("RQS", MailConstants.ServiceCmd.DefConfig.Value());
			getApplicationContext().sendBroadcast(intent);  		
						  		
			while (m_nTimer > 0 && m_bThreadWait)
			{
				try 
				{
					Thread.sleep(1000);
				} catch (Exception e) 
				{}
						      
				m_nTimer--;
				m_db.putInt("RegisterWaitTimer", m_nTimer);
				
				m_TimerHandler.sendEmptyMessage(1);						
				
				// check for defconfig reply
				if (m_nDefConfig != 0)
					break;
			}
			
			// get password list
			Intent intent2 = new Intent();
			intent2.setAction(MSAService.ACTION);
			intent2.putExtra("RQS", MailConstants.ServiceCmd.GetPasswordList.Value());
			getApplicationContext().sendBroadcast(intent2);
			
			while (m_nTimer > 0 && m_bThreadWait)
			{
				try 
				{
					Thread.sleep(1000);
				} catch (Exception e) 
				{}
						      
				m_nTimer--;
				m_db.putInt("RegisterWaitTimer", m_nTimer);
				
				m_TimerHandler.sendEmptyMessage(1);						
				
				// check for get password list reply
				if (m_nGetPasswordList != 0)
					break;
			}
		}
				
		m_TimerHandler.sendEmptyMessage(2);
	}
	
	Handler m_TimerHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			if (msg.what == 1)
				m_lblTimer.setText(String.valueOf(m_nTimer));
			else if (msg.what == 2)
			{
				m_db.putInt("RegisterWaitTimer", 0);
				
				if (!m_bThreadWait && m_nActivated != 1)
					ShowAlert(getString(R.string.errActivationCancelled));
				else if (m_nTimer <= 0)
					ShowAlert(getString(R.string.errActivationTimeout));
				else if (m_nActivated == 1)
					//ShowAlert(getString(R.string.errActivationOK));
					WaitActivity.this.finish();
				else if (m_nActivated == 2)				
					ShowAlert(getString(R.string.errActivationFailed) + "\n" + m_db.getString("ActivationResult"));				
			}
		}
	};
	
	public void ShowAlert(String sText)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WaitActivity.this);

		// set title
		alertDialogBuilder.setTitle(getString(R.string.app_name2));

		// set dialog message
		alertDialogBuilder
			.setMessage(sText)
			.setCancelable(false)
			.setNeutralButton("OK",new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog,int id) 
				{
					WaitActivity.this.finish();
				}
			});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();		
	}
	
	private BroadcastReceiver MSAWaitReceiver = new BroadcastReceiver()
  {
		@Override
		public void onReceive(Context arg0, Intent arg1) 
		{
			if (arg1.getAction().equals("com.dtj.msa.MSA_ACTIVATION_REPLY")) 
	  	{
				m_nActivated = arg1.getIntExtra("Activated", 0);									
	  	}
			else if (arg1.getAction().equals("com.dtj.msa.MSA_DEFCONFIG_REPLY")) 
	  	{
				m_nDefConfig = 1;									
	  	}			
			else if (arg1.getAction().equals("com.dtj.msa.MSA_GETPASSWORDLIST_REPLY")) 
	  	{
				m_nGetPasswordList = 1;									
	  	}
		}
  };
	
	@Override
  public void onDestroy()
	{
  	super.onDestroy();
  	
  	unregisterReceiver(MSAWaitReceiver);  	    	  
	}
}
