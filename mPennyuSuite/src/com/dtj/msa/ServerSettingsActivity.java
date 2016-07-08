package com.dtj.msa;

import java.util.List;

import r1.util.INIFile;
import r1.util.R1Util;

import com.dtj.email.CIDCreator;
import com.dtj.email.MailConstants;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class ServerSettingsActivity extends Activity 
{
	ScrollView m_panelSettings;
	LinearLayout m_panelReset;
	
	@Override
  protected void onCreate(Bundle savedInstanceState) 
  {
		super.onCreate(savedInstanceState);
	  setContentView(R.layout.serversettings);
	  
	  m_panelSettings = (ScrollView)findViewById(R.id.panelSettings);
	  m_panelReset = (LinearLayout)findViewById(R.id.panelReset);
	  m_panelReset.setVisibility(View.GONE);
	  
	  DisplaySettings();
	  
	  ((Button) findViewById(R.id.btnSaveSettings)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
    	{    		    		
    		EditText tbServerHost = (EditText)findViewById(R.id.tbServerHost);
    		String sServerHost = tbServerHost.getText().toString().trim();
    		
    		EditText tbIMAPPort = (EditText)findViewById(R.id.tbIMAPPort);
    		String sIMAPPort = tbIMAPPort.getText().toString().trim();
    		
    		EditText tbSMTPPort = (EditText)findViewById(R.id.tbSMTPPort);
    		String sSMTPPort = tbSMTPPort.getText().toString().trim();
    		
    		EditText tbEmailAccount = (EditText)findViewById(R.id.tbEmailAccount);
    		String sEmailAccount = tbEmailAccount.getText().toString().trim();
    		
    		EditText tbEmailPassword = (EditText)findViewById(R.id.tbEmailPassword);
    		String sEmailPassword = tbEmailPassword.getText().toString().trim();
    		
    		EditText tbRecipientEmail = (EditText)findViewById(R.id.tbRecipientEmail);
    		String sRecipientEmail = tbRecipientEmail.getText().toString().trim();    		    		
    		
    		boolean bOK = true;
    		if (sServerHost.length() < 5)
    			bOK = false;
    		if (sIMAPPort.length() <= 0)
    			bOK = false;
    		if (sSMTPPort.length() <= 0)
    			bOK = false;
    		if (sEmailAccount.length() <= 5)
    			bOK = false;    		
    		
    		if (bOK)
    		{    			    			
    			String m_sINIPath = getApplicationContext().getFilesDir().getPath() + "/msa/Ini/Settings.ini";
    			INIFile m_settings = R1Util.ReadEncryptedSettings(getApplicationContext(), m_sINIPath);
    			
    			/*String sAppStatus = m_settings.getStringProperty("MSASettings", "AppStatus");
    			if (sAppStatus.equals(MailConstants.APP_ENABLED))
    			{    				
    				//tbWId.setText(m_settings.getStringProperty("MSASettings", "AppWId"));
    				Toast.makeText(getApplicationContext(), "MobileSuite already activated! Settings cannot be changed.", Toast.LENGTH_SHORT).show();
    				return;
    			}*/
    			
    			m_settings.setStringProperty("MSASettings", "MailHost", sServerHost, null);    			    			
    			m_settings.setStringProperty("MSASettings", "IMAPPort", sIMAPPort, null);    			    			
    			m_settings.setStringProperty("MSASettings", "SMTPPort", sSMTPPort, null);    			
    			m_settings.setStringProperty("MSASettings", "MailUser", sEmailAccount, null);
    			m_settings.setStringProperty("MSASettings", "MailPwd", sEmailPassword, null);
    			m_settings.setStringProperty("MSASettings", "MailTo", sRecipientEmail, null);    			
    			    			
  				//String sCId = CIDCreator.EncryptCId(getApplicationContext(), m_settings);
  				//m_settings.setStringProperty("MSASettings", "AppCId", sCId, null);
    			
    			R1Util.SaveEncryptedSettings(m_settings, m_sINIPath);
    			
    			if (IsServiceRunning("com.dtj.msa", "com.dtj.msa.MSAService"))
    			{
	    			m_panelSettings.setVisibility(View.GONE);
	    			m_panelReset.setVisibility(View.VISIBLE);
	    			
	    			new Thread() 
	    			{
	    				@Override
	    				public void run() 
	    				{
	    					ResetService();
	    				}
	    			}.start();
    			}
    			else
    			{
    				ServerSettingsActivity.this.finish();
    			}
    		}
    		else
    			Toast.makeText(getApplicationContext(), "Invalid settings!", Toast.LENGTH_SHORT).show();
      }
    });
  }
	
	private void DisplaySettings()
  {  		
		String m_sINIPath = getApplicationContext().getFilesDir().getPath() + "/msa/Ini/Settings.ini";
		INIFile m_settings = R1Util.ReadEncryptedSettings(getApplicationContext(), m_sINIPath);
		
		EditText tbServerHost = (EditText)findViewById(R.id.tbServerHost);
		String sServerHost = m_settings.getStringProperty("MSASettings", "MailHost");
		tbServerHost.setText(sServerHost);
		
		EditText tbIMAPPort = (EditText)findViewById(R.id.tbIMAPPort);
		String sIMAPPort = m_settings.getStringProperty("MSASettings", "IMAPPort");
		tbIMAPPort.setText(sIMAPPort);
		
		EditText tbSMTPPort = (EditText)findViewById(R.id.tbSMTPPort);
		String sSMTPPort = m_settings.getStringProperty("MSASettings", "SMTPPort");
		tbSMTPPort.setText(sSMTPPort);
		
		EditText tbEmailAccount = (EditText)findViewById(R.id.tbEmailAccount);
		String sEmailAccount = m_settings.getStringProperty("MSASettings", "MailUser");
		tbEmailAccount.setText(sEmailAccount);
		
		EditText tbEmailPassword = (EditText)findViewById(R.id.tbEmailPassword);
		String sEmailPassword = m_settings.getStringProperty("MSASettings", "MailPwd");
		tbEmailPassword.setText(sEmailPassword);
		
		EditText tbRecipientEmail = (EditText)findViewById(R.id.tbRecipientEmail);
		String sRecipientEmail = m_settings.getStringProperty("MSASettings", "MailTo");
		tbRecipientEmail.setText(sRecipientEmail);
  }
	
	private void ResetService()
	{
		if (IsServiceRunning("com.dtj.msa", "com.dtj.msa.MSAService"))
		{
  		Intent intent = new Intent();
			intent.setAction(MSAService.ACTION);
			intent.putExtra("RQS", MailConstants.ServiceCmd.StopService.Value());
			sendBroadcast(intent);
			
			if (IsServiceRunning("com.dtj.msa", "com.dtj.msa.MSAMutexService"))
			{
				Intent run = new Intent();
				run.setAction(MSAMutexService.ACTION);
				run.putExtra("RQS", MailConstants.ServiceCmd.StopService.Value());
				sendBroadcast(run);
			}
			
			if (IsServiceRunning("com.dtj.msa", "com.dtj.msa.MSAMutexChatService"))
			{
				Intent run = new Intent();
				run.setAction(MSAMutexChatService.ACTION);
				run.putExtra("RQS", MailConstants.ServiceCmd.StopService.Value());
				sendBroadcast(run);
			}
			
			if (IsServiceRunning("com.dtj.msa", "com.dtj.msa.MSAMutexMailService"))
			{
				Intent run = new Intent();
				run.setAction(MSAMutexMailService.ACTION);
				run.putExtra("RQS", MailConstants.ServiceCmd.StopService.Value());
				sendBroadcast(run);
			}
		}				
		
		try 
		{
			Thread.sleep(2000);
		} 
		catch (Exception e) {
		}
						
		m_ResetHandler.sendEmptyMessage(1);
	}
	
	public boolean IsServiceRunning(String sProcessName, String sServiceName)
	{		
		boolean bRunning = false;
		
		ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
    List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(100);
    String message = null;

    for (int i=0; i<rs.size(); i++) 
    {
      RunningServiceInfo rsi = rs.get(i);
      if (rsi.process.equals(sProcessName) && rsi.service.getClassName().equals(sServiceName))
      {
      	bRunning = true;
      	break;
      }      
      //Log.v("Service", "Process " + rsi.process + " with component " + rsi.service.getClassName());
      //message = message+rsi.process;
    }
    
    return bRunning;
	}
	
	Handler m_ResetHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{			
			if (msg.what == 1)				
			{
				if (!IsServiceRunning("com.dtj.msa", "com.dtj.msa.MSAService"))
				{
					/*Intent fake = new Intent(ServerSettingsActivity.this, MSAFakeService.class);
					ServerSettingsActivity.this.startService(fake);
					
					try 
					{
						Thread.sleep(200);
					} 
					catch (Exception e) 
					{}*/
					
					Intent intent = new Intent(ServerSettingsActivity.this, MSAService.class);
					ServerSettingsActivity.this.startService(intent);  				      			
				}
							
				ServerSettingsActivity.this.finish();
			}
		}
	};
}
