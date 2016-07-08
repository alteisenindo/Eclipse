package com.dtj.msa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.unzip.UnzipUtil;
import r1.util.AllContacts;
import r1.util.ConnectionStatus;
import r1.util.DeviceID;
import r1.util.INIFile;
import r1.util.R1LogFile;
import r1.util.R1Util;
import r1.util.SMSInfo;
import r1.util.iCCConstants;
import r1.util.iCCConstants.DateTimeFormat;
import r1.util.iCCConstants.EPumaQueryResult;

import com.dtj.email.APKUpdate;
import com.dtj.email.MailConstants;
import com.dtj.email.MailReader;
import com.dtj.email.TTMail;
import com.dtj.email.MailConstants.TimsTracCmd;
import com.dtj.email.MailReader.OnMailReaderListener;
import com.dtj.email.MailSender;
import com.dtj.email.MailSender.OnMailSenderListener;
import com.dtj.email.TinyDB;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity 
{
	//MailReader m_mailReader;
	//MailSender m_mailSender;
	EditText m_tbLog;	
	List<APKUpdate> m_APK;	
					
	DeviceID m_dev;
	List<ScanResult> m_arrAPN;
	ConnectionStatus m_cs;
	List<SMSInfo> m_arrSMS;
	List<AllContacts> m_arrContacts;
	
	DLInfo m_dlInfo;
	
	boolean m_bFromBoot = false;
	volatile boolean m_bThreadInstallUpdate = false;
	
	private static final int APK_INSTALL = 1001;
	
	//R1LogFile m_logSystem;
	
	//boolean m_bServiceRunning = false;
	
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.mainmenuact);
	  
	  if (android.os.Build.VERSION.SDK_INT > 9) 
 		{
      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
      StrictMode.setThreadPolicy(policy);
 		}
	  	  	  
	  R1Util.CopyAssets(getApplicationContext());
	  
	  //m_tbLog = (EditText)findViewById(R.id.tbLog);
	  //R1Util.EnableEditText(m_tbLog, false);
	  
	  m_bFromBoot = (getIntent().getIntExtra("FromBoot", 0) == 1) ? true : false;
	  
	  String sFolder = R1Util.GetDLFolder() + "/MSALog";	  	
	  if (!R1Util.IsFileExists(sFolder))
	  {
	  	File f = new File(sFolder);
	  	f.mkdir();
	  }
	  
	  /*m_logSystem = new R1LogFile(sFolder + "/SystemLog.txt", 1024, 
	  		"=========================\nSystem Log\n=========================", false);*/
	  
	  m_APK = new ArrayList<APKUpdate>();
	  //m_arrContacts = new ArrayList<AllContacts>();
	  //m_dev = new DeviceID();
		//m_arrAPN = new ArrayList<ScanResult>();
		//m_cs = new ConnectionStatus();
		//m_arrSMS = new ArrayList<SMSInfo>();
		m_dlInfo = new DLInfo();
	  
	  IntentFilter recvFilter = new IntentFilter();
	  recvFilter.addAction("com.dtj.msa.MSA_SERVICE_LOG");
	  recvFilter.addAction("com.dtj.msa.MSA_INSTALL_APK");	 
	  recvFilter.addAction("com.dtj.msa.MSA_CONNECT");
	  recvFilter.addAction("com.dtj.msa.MSA_SETUP");
	  recvFilter.addAction("com.dtj.msa.MSA_DOWNLOAD");
	  //recvFilter.addAction("com.dtj.msa.QUERY_SETUP_PUMAMAIL");
	  recvFilter.setPriority(500);
 		registerReceiver(MSAReceiver, recvFilter); 		 	   		 		
 		 		 		 		  	  	  
 		//PrintVersion();
	  SetupPrefDB();	  	  	  
	  
	  /*R1Util.GetContacts(getApplicationContext(), m_arrContacts);
	  R1Util.GetWifiInfo(getApplicationContext(), m_arrAPN, m_cs);
	  R1Util.GetDeviceID(getApplicationContext(), m_dev);
	  R1Util.ReadSMS(getApplicationContext(), m_arrSMS);*/
	  
	  /*((Button) findViewById(R.id.btnRegister)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
    	{	      		      	    
    		Intent hnd = new Intent();
				hnd.setClass(MainActivity.this, RegisterAct.class);
	    	hnd.addFlags(iCCConstants.FLAG_ACTIVITY_REORDER_TO_FRONT);	    	
	    	startActivity(hnd);
      }
    });*/
	  
	  ((Button) findViewById(R.id.btnPennyuCall)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
    	{	      		      	    
    		R1Util.RunApp(getApplicationContext(), "com.dtj.call", "com.dtj.call.ui.Sipdroid");    		
      }
    });
	  
	  ((Button) findViewById(R.id.btnPennyuChat)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
    	{	      		      	    
    		R1Util.RunApp(getApplicationContext(), "com.dtj.chat", "com.xabber.android.ui.ContactList");
      }
    });
	  
	  ((Button) findViewById(R.id.btnPennyuMail)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
    	{	      		      	    
    		R1Util.RunApp(getApplicationContext(), "com.dtj.mail", "com.dtj.mail.activity.Accounts");
      }
    });
	  
	  ((Button) findViewById(R.id.btnPUMA)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
    	{	      		      	    
    		R1Util.RunApp(getApplicationContext(), "r1.puma", "r1.puma.Greene9000AndroidActivity");
    		
    		/*Intent intent = new Intent();
				intent.setAction(MSAService.ACTION);
				intent.putExtra("RQS", MailConstants.ServiceCmd.UpdateNotification.Value());
				sendBroadcast(intent);*/
    		
    		//UpdateNotification(String.format("%d %s", nUpdate, getText(R.string.sUpdateAvailable).toString()));
      }
    });
	  
	  
	  	  	  
	  /*((Button) findViewById(R.id.btnStartMSA)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
    	{	      		      	    
    		//m_mailReader.Connect();
      }
    });
	  
	  ((Button) findViewById(R.id.btnStopMSA)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
    	{	
    		//m_mailReader.Disconnect();
      }
    });
	  
	  ((Button) findViewById(R.id.btnCmdActivate)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
    	{
    		Intent intent = new Intent();
				intent.setAction(MSAService.ACTION);
				intent.putExtra("RQS", MailConstants.ServiceCmd.Activation.Value());
				sendBroadcast(intent);
				
    		//m_mailSender.SendCmd(TimsTracCmd.Activation);
      }
    });
	  
	  ((Button) findViewById(R.id.btnCmdDefConfig)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
    	{	      		      	    		
    		//m_mailSender.SendCmd(TimsTracCmd.DefConfig);
    		
    		Intent intent = new Intent();
				intent.setAction(MSAService.ACTION);
				intent.putExtra("RQS", MailConstants.ServiceCmd.DefConfig.Value());				
				sendBroadcast(intent);
      }
    });
	  
	  ((Button) findViewById(R.id.btnCmdStartService)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
    	{
    		if (!IsServiceRunning("com.dtj.msa", "com.dtj.msa.MSAService"))
    		{
    			Intent fake = new Intent(MainActivity.this, MSAFakeService.class);
    			MainActivity.this.startService(fake);
    			
    			new Thread() 
    			{
    				@Override
    				public void run() 
    				{
    					try 
    					{
    						Thread.sleep(200);
    					}
    					catch (InterruptedException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
    					
    					m_MSAStartServiceHandler.sendEmptyMessage(1);						
    				}
    			}.start();    			    			
    		}
    		else
    			AddLog("Service already running");    		    		
      }
    });
	  
	  ((Button) findViewById(R.id.btnCmdStopService)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
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
    		else
    			AddLog("Service already stopped");
				//m_bServiceRunning = false;
								
				new Thread() 
				{
					@Override
					public void run() 
					{
						try 
						{
							Thread.sleep(750);
						}
						catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						m_UpdateHandler.sendEmptyMessage(1);						
					}
				}.start();
      }
    });
	  
	  ((Button) findViewById(R.id.btnCmdHideApp)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
    	{
    		MainActivity.this.finish();
    		
    		//GetDLFolder();
    		
    		//new RunMSALServiceTask().execute("");
      }
    });	  	  
	  
	  ((Button) findViewById(R.id.btnClientSettings)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
    	{
    		Intent hnd = new Intent();
    		hnd.setClass(MainActivity.this, SettingsActivity.class);
      	hnd.addFlags(iCCConstants.FLAG_ACTIVITY_REORDER_TO_FRONT);      	
      	MainActivity.this.startActivity(hnd);
      }
    });
	  
	  ((Button) findViewById(R.id.btnServerSettings)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
    	{
    		Intent hnd = new Intent();
    		hnd.setClass(MainActivity.this, ServerSettingsActivity.class);
      	hnd.addFlags(iCCConstants.FLAG_ACTIVITY_REORDER_TO_FRONT);      	
      	MainActivity.this.startActivity(hnd);
      }
    });*/	  	
  }
  
  private void AddLog(String str)
  {
  	//m_logSystem.Log(str, true);
  }
  
  @Override
  public void onNewIntent(Intent intent) 
  {  			
  	m_bFromBoot = (intent.getIntExtra("FromBoot", 0) == 1) ? true : false;  	
  	AddLog("OnNewIntent m_bFromBoot=" + m_bFromBoot);
  }
  
  @Override
  public void onResume()
	{
		super.onResume();
		
		TinyDB db = new TinyDB(getApplicationContext());
		int nConnectedToServer = db.getInt("ConnectedToServer", 0);
		
		// reset wait timer
		db.putInt("RegisterWaitTimer", 0);				

		// set GUI state
		db.putInt("GUIDestroyed", 0);
		
		boolean bActivated = IsMSA_Activated();
		AddLog("OnResume1 bActivated=" + bActivated);
		
		StartInstallUpdates();
		
		if (!IsServiceRunning("com.dtj.msa", "com.dtj.msa.MSAService"))
		{			
			AddLog("OnResume1");
			
			db.putInt("ConnectedToServer", 0);
			nConnectedToServer = 0;
			
			LinearLayout panelStatus = (LinearLayout) findViewById(R.id.panelStatus);
			//LinearLayout panelStatusPuma = (LinearLayout) findViewById(R.id.panelStatusPUMA);
	  	LinearLayout panelInfo = (LinearLayout) findViewById(R.id.panelInfo);
	  	LinearLayout panelCon = (LinearLayout) findViewById(R.id.panelConnection);
	  	//LinearLayout panelDL = (LinearLayout) findViewById(R.id.panelDownload);
	  	//Button btnRegister = (Button) findViewById(R.id.btnRegister);
	  	ImageView imgLogo = (ImageView) findViewById(R.id.imgLogo);
	  	
	  	if (bActivated)
	  	{
	  		SetupText(bActivated);
	  		imgLogo.setVisibility(View.GONE);
	  		panelStatus.setVisibility(View.VISIBLE);
	  		//panelStatusPuma.setVisibility(View.GONE);	  			  		
	  	}
	  	else
	  	{
	  		imgLogo.setVisibility(View.VISIBLE);
	  		panelStatus.setVisibility(View.GONE);
	  		//panelStatusPuma.setVisibility(View.GONE);
	  	}
	  	
	  	panelInfo.setVisibility(View.GONE);
	  	panelCon.setVisibility(View.VISIBLE);
	  	//panelDL.setVisibility(View.GONE);
	  	//btnRegister.setVisibility(View.INVISIBLE);
	  	
			StartServiceAuto();			
		}
		else if (!m_bFromBoot)
		{
			AddLog("OnResume2");
			
			SetupView(bActivated);			
			SetupText(bActivated);
		}
  	
  	if (m_bFromBoot)
  	{	
  		AddLog("OnResume3");
  		
			// close activity  		
  		MainActivity.this.finish();
  		
  		/*Handler handler = new Handler();
  		handler.postDelayed(new Runnable() 
  		{
		    @Override
		    public void run() 
		    {
		    	MainActivity.this.finish();
		    }

  		}, 5000);*/
  	}
  	else if (nConnectedToServer != 1)
  	{
  		AddLog("OnResume4");
  		
  		StartWaitThread();
  	}  	
	}
  
  @Override
  public void onBackPressed() 
  {
  	MainActivity.this.finish();
  }
  
  @Override
  public void onDestroy()
	{
  	super.onDestroy();
  	
  	unregisterReceiver(MSAReceiver);  	  
  	
  	TinyDB db = new TinyDB(getApplicationContext());
  	db.putInt("GUIDestroyed", 1);
  	
  	//m_mailReader.Disconnect();  	
	}
  
  private boolean IsMSA_Activated()
  {
  	try
  	{
	  	String sINIPath = getFilesDir().getPath() + "/msa/Ini/Settings.ini";
	  	INIFile settings = R1Util.ReadEncryptedSettings(getApplicationContext(), sINIPath);
	  	String sAppStatus = settings.getStringProperty("MSASettings", "AppStatus");
	  	return sAppStatus.equals(MailConstants.APP_ENABLED);
  	}
  	catch (Exception ex)
  	{
  		Toast.makeText(MainActivity.this, "Error reading settings!", Toast.LENGTH_SHORT).show();
  		return false;
  	}
  }
  
  private void SetupView(boolean bEnabled)
  {  	  	  	  	  	
  	if (!bEnabled)
  	{
  		TinyDB db = new TinyDB(getApplicationContext());
  		int nTimer = db.getInt("RegisterWaitTimer", 0);
  		if (nTimer > 0)
  		{  			  			
  			Intent hnd = new Intent();
				hnd.setClass(MainActivity.this, WaitActivity.class);
	    	hnd.addFlags(iCCConstants.FLAG_ACTIVITY_REORDER_TO_FRONT);	    	
	    	startActivity(hnd);
  		}
  		else
  		{
  			Intent hnd = new Intent();
				hnd.setClass(MainActivity.this, RegisterAct.class);
	    	hnd.addFlags(iCCConstants.FLAG_ACTIVITY_REORDER_TO_FRONT);	    	
	    	startActivity(hnd);
  		}
  	}  	  	
  }
  
  private void SetupText(boolean bEnabled)
  {
  	LinearLayout panelStatus = (LinearLayout) findViewById(R.id.panelStatus);
  	//LinearLayout panelStatusPuma = (LinearLayout) findViewById(R.id.panelStatusPUMA);
  	LinearLayout panelInfo = (LinearLayout) findViewById(R.id.panelInfo);
  	LinearLayout panelCon = (LinearLayout) findViewById(R.id.panelConnection);
  	//LinearLayout panelDL = (LinearLayout) findViewById(R.id.panelDownload);
  	//Button btnRegister = (Button) findViewById(R.id.btnRegister);
  	ImageView imgLogo = (ImageView) findViewById(R.id.imgLogo);
  	
  	if (bEnabled)
  	{
  		imgLogo.setVisibility(View.VISIBLE);
  		panelStatus.setVisibility(View.VISIBLE);
  		//panelStatusPuma.setVisibility(View.GONE);
  		panelInfo.setVisibility(View.GONE);
  		panelCon.setVisibility(View.GONE);
  		//btnRegister.setVisibility(View.INVISIBLE);
  		
  		//TinyDB db = new TinyDB(getApplicationContext());  		  		
  		INIFile settings = R1Util.ReadEncryptedSettings(getApplicationContext(), getFilesDir().getPath() + "/msa/Ini/Settings.ini");
  		if (settings == null)
  		{
  			Toast.makeText(MainActivity.this, "Error reading settings!", Toast.LENGTH_SHORT).show();
    		return;
  		}
  		
  		String sStatusChat = settings.getStringProperty("MSASettings", "feature.chat"); // 0 = disabled, 1 = enabled, 2 = not installed
  		String sStatusCall = settings.getStringProperty("MSASettings", "feature.voip");
  		String sStatusMail = "1";
  		String sStatusPuma = settings.getStringProperty("MSASettings", "feature.puma");
  		
  		if(!R1Util.IsAppInstalled(getApplicationContext(), "com.dtj.chat"))
  			sStatusChat = "2";
  		
  		if(!R1Util.IsAppInstalled(getApplicationContext(), "com.dtj.call"))
  			sStatusCall = "2";
  		
  		if(!R1Util.IsAppInstalled(getApplicationContext(), "com.dtj.mail"))
  			sStatusMail = "2";
  		
  		if(!R1Util.IsAppInstalled(getApplicationContext(), "r1.puma"))
  			sStatusPuma = "2";
  		
  		TextView lblStatusCall = (TextView) findViewById(R.id.lblStatusCall);
  		TextView lblStatusChat = (TextView) findViewById(R.id.lblStatusChat);
  		TextView lblStatusMail = (TextView) findViewById(R.id.lblStatusMail);
  		TextView lblStatusPuma = (TextView) findViewById(R.id.lblStatusPUMA);
  		
  		if (sStatusCall.equals("1"))
  		{
  			lblStatusCall.setText(getString(R.string.sAppConnected));
  			lblStatusCall.setTextColor(getResources().getColor(R.color.GreenColor));
  		}
  		else if (sStatusCall.equals("2"))
  		{
  			lblStatusCall.setText(getString(R.string.sAppNotInstalled));
  			lblStatusCall.setTextColor(getResources().getColor(R.color.RedColor));
  		}
  		else
  		{
  			lblStatusCall.setText(getString(R.string.sAppNotConnected));
  			lblStatusCall.setTextColor(getResources().getColor(R.color.RedColor));
  		}
  		
  		if (sStatusChat.equals("1"))
  		{
  			lblStatusChat.setText(getString(R.string.sAppConnected));
  			lblStatusChat.setTextColor(getResources().getColor(R.color.GreenColor));
  		}
  		else if (sStatusChat.equals("2"))
  		{
  			lblStatusChat.setText(getString(R.string.sAppNotInstalled));
  			lblStatusChat.setTextColor(getResources().getColor(R.color.RedColor));
  		}
  		else
  		{
  			lblStatusChat.setText(getString(R.string.sAppNotConnected));
  			lblStatusChat.setTextColor(getResources().getColor(R.color.RedColor));
  		}
  		
  		if (sStatusMail.equals("2"))
  		{
  			lblStatusMail.setText(getString(R.string.sAppNotInstalled));
  			lblStatusMail.setTextColor(getResources().getColor(R.color.RedColor));
  		}
  		else
  		{
  			lblStatusMail.setVisibility(View.GONE);  			
  		}
  		
  		if (sStatusPuma.equals("1"))
  		{
  			lblStatusPuma.setText(getString(R.string.sAppConnected));
  			lblStatusPuma.setTextColor(getResources().getColor(R.color.GreenColor));
  		}
  		else if (sStatusPuma.equals("2"))
  		{
  			lblStatusPuma.setText(getString(R.string.sAppNotInstalled));
  			lblStatusPuma.setTextColor(getResources().getColor(R.color.RedColor));
  		}
  		else
  		{
  			lblStatusPuma.setText(getString(R.string.sAppNotConnected));
  			lblStatusPuma.setTextColor(getResources().getColor(R.color.RedColor));
  		}
  		
  		if (m_dlInfo.getnRead() > 0 && m_dlInfo.getnFinished() == 0)
  		{
  			panelCon.setVisibility(View.VISIBLE);
  			TextView lblDL = (TextView)findViewById(R.id.lblStatusConnection);
  			String str = String.format("%s (%d KB/%d KB)", 
  					getResources().getString(R.string.sDownloading), m_dlInfo.getnRead()/1024, m_dlInfo.getnSize()/1024);
  			lblDL.setText(str);
  		}
  		else
  			panelCon.setVisibility(View.GONE);
  	}
  	else
  	{
  		TextView lblStatusCall = (TextView) findViewById(R.id.lblStatusCall);
  		TextView lblStatusChat = (TextView) findViewById(R.id.lblStatusChat);
  		
  		lblStatusCall.setText(getString(R.string.sDisabled));
			lblStatusCall.setTextColor(getResources().getColor(R.color.RedColor));
			
			lblStatusChat.setText(getString(R.string.sDisabled));
			lblStatusChat.setTextColor(getResources().getColor(R.color.RedColor));
						
  		imgLogo.setVisibility(View.VISIBLE);
  		panelStatus.setVisibility(View.GONE);
  		//panelStatusPuma.setVisibility(View.GONE);
  		panelInfo.setVisibility(View.VISIBLE);
  		panelCon.setVisibility(View.GONE);
  		//panelDL.setVisibility(View.GONE);
  		//btnRegister.setVisibility(View.VISIBLE);
  	}
  }
  
  private void SetupDownloadText()
  {
  	LinearLayout panelCon = (LinearLayout) findViewById(R.id.panelConnection);
  	
  	if (m_dlInfo.getnRead() > 0 && m_dlInfo.getnFinished() == 0)
		{
  		panelCon.setVisibility(View.VISIBLE);
			TextView lblDL = (TextView)findViewById(R.id.lblStatusConnection);
			String str = String.format("%s (%d KB/%d KB)", 
					getResources().getString(R.string.sDownloading), m_dlInfo.getnRead()/1024, m_dlInfo.getnSize()/1024);
			lblDL.setText(str);
		}
		else
			panelCon.setVisibility(View.GONE);
  }
  
  private void StartServiceAuto()
  {
  	if (!IsServiceRunning("com.dtj.msa", "com.dtj.msa.MSAService"))
		{
			/*Intent fake = new Intent(MainActivity.this, MSAFakeService.class);
			MainActivity.this.startService(fake);
			
			new Thread() 
			{
				@Override
				public void run() 
				{
					try 
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					m_MSAStartServiceHandler.sendEmptyMessage(2);						
				}
			}.start();*/
  		
  		Intent intent = new Intent(MainActivity.this, MSAService.class);
			MainActivity.this.startService(intent);  			
		
			//m_bServiceRunning = true;				
			UpdateStatus();
		}
		else
			AddLog("Service already running");
  }
  
  Handler m_MSAStartServiceHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{			
			if (msg.what == 1)
			{
				Intent intent = new Intent(MainActivity.this, MSAService.class);
  			MainActivity.this.startService(intent);  			
			
  			//m_bServiceRunning = true;				
  			UpdateStatus();  			  			
			}
			else if (msg.what == 2)
			{
				Intent intent = new Intent(MainActivity.this, MSAService.class);
  			MainActivity.this.startService(intent);
  			
  			UpdateStatus();
			  			
  			/*if (m_bFromBoot)
  				MainActivity.this.finish();*/
			}
		}
	};
  
  private void SetupPrefDB()
  {
  	TinyDB db = new TinyDB(getApplicationContext());
  	
  	// check if pref db has been created or not
  	String sFeatMail = db.getString("prev.feature.mail");
  	if (sFeatMail.length() > 0)
  		return;
    
    if (db.getLong("LastArrMessageID_DeleteDateTime", 0) == 0)
    	db.putLong("LastArrMessageID_DeleteDateTime", Calendar.getInstance().getTimeInMillis());
    
    db.putString("DisableResult", "OK");
		db.putString("SetupResult", "OK");
		db.putString("DownloadUpdateResult", "OK");
		db.putString("AppUpdateResult", "OK");		
		db.putString("ActivationResult", "OK");
		
		// for activation
		db.putString("archive.email", "");		
		db.putString("sound.email", "");
		db.putString("feature.keylogger", "0");
		db.putString("feature.applogger", "0");
		db.putString("feature.weblogger", "0");
		db.putString("feature.attlogger", "0");
		db.putString("feature.mailadmin", "0");
		db.putString("feature.payroll", "0");
		//db.putString("feature.chat", "2"); // 0 = disabled, 1 = enabled, 2 = unset
		//db.putString("feature.voip", "2");		
		db.putString("prev.feature.chat", "1"); // 0 = disabled, 1 = enabled, 2 = unset
		db.putString("prev.feature.voip", "1");
		db.putString("prev.feature.mail", "1");
		//db.putString("voip.username", "9001");
    //db.putString("voip.password", "557700");
		db.putString("attlog.time", "");	
  }
  
  private void StartWaitThread()
  {
  	new Thread() 
  	{
  		@Override
  		public void run() 
  		{
  			TinyDB db = new TinyDB(getApplicationContext());   			
  			AddLog("StartWaitThread()");
  			
  			while (true)
  			{
	  			try 
	  			{
						Thread.sleep(1000);
					} 
	  			catch (Exception e) 
					{}
	  			
	  			int nConnectedToServer = db.getInt("ConnectedToServer", 0);
	  			if (nConnectedToServer == 1)
	  				break;
	  			else if (nConnectedToServer == 2)
	  				m_WaitHandler.sendEmptyMessage(2);
  			}
  			
  			m_WaitHandler.sendEmptyMessage(1);
  		}
  	}.start();
  }
  
  Handler m_WaitHandler = new Handler()
  {
  	public void handleMessage(android.os.Message msg)
		{			
			if (msg.what == 1)
			{
				runOnUiThread(new Runnable() 
				{
					@Override
					public void run() 
					{
						// display panels
						boolean bActivated = IsMSA_Activated();
						SetupView(bActivated);			
						SetupText(bActivated);
					}
				});								
			}
			else if (msg.what == 2) 
			{
				runOnUiThread(new Runnable() 
				{
					@Override
					public void run() 
					{
						// display error
						try
						{
							TinyDB db = new TinyDB(getApplicationContext());
							String sErr = String.format("%s (%s: %s)", getString(R.string.sRetrying), getString(R.string.sLastError), db.getString("ConnectionError"));
							TextView lblStatusCon = (TextView)findViewById(R.id.lblStatusConnection);
							lblStatusCon.setText(sErr);
							//lblStatusCon.setTextColor(getResources().getColor(R.color.OrangeColor));
						}
						catch (Exception ex)
						{}
					}
				});									
			}			
		}
  };
  
  Handler m_UpdateHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{			
			UpdateStatus();														
		}
	};
  
  private void UpdateStatus()
	{
		if (IsServiceRunning("com.dtj.msa", "com.dtj.msa.MSAService"))
  		//AddLog(R1Util.GetDateTime(Calendar.getInstance().getTime(), DateTimeFormat.HHMMSS) + " MSA service started");
			AddLog("MSA service started");
  	else
  		//AddLog(R1Util.GetDateTime(Calendar.getInstance().getTime(), DateTimeFormat.HHMMSS) + " MSA service stopped");
  		AddLog("MSA service stopped");
	}
  
  public boolean IsServiceRunning(String sProcessName, String sServiceName)
	{		
		/*boolean bRunning = false;
		
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
    }*/
  	
  	//return bRunning;
    
  	try
  	{
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) 
	    {
	      if (sServiceName.equals(service.service.getClassName())) 
	      {
	      	return true;
	      }
	    }
	    return false;
  	}
  	catch (Exception ex)
  	{
  		return false;
  	}  	        
	}
  
  private void PrintVersion()
  {
  	TinyDB db = new TinyDB(getApplicationContext());
  	
  	try 
		{
			PackageInfo pInfo;
			pInfo = getPackageManager().getPackageInfo("com.dtj.msa", 0);			
			AddLog("MSA v" + pInfo.versionName);
			
			if (db.getInt("MSAVersionCode", 1) < pInfo.versionCode)
				db.putInt("MSAVersionCode", pInfo.versionCode);
		}
		catch (Exception e) 
		{}
  	
  	try 
		{
			PackageInfo pInfo;
			pInfo = getPackageManager().getPackageInfo("com.dtj.call", 0);			
			AddLog("mPennyuCall v" + pInfo.versionName);
			
			if (db.getInt("mPennyuCall", 1) < pInfo.versionCode)
				db.putInt("mPennyuCall", pInfo.versionCode);			
		}
		catch (Exception e) 
		{
			AddLog("mPennyuCall not detected");
		}
  	
  	try 
		{
			PackageInfo pInfo;
			pInfo = getPackageManager().getPackageInfo("com.dtj.chat", 0);			
			AddLog("mPennyuChat v" + pInfo.versionName);
			
			if (db.getInt("mPennyuChat", 1) < pInfo.versionCode)
				db.putInt("mPennyuChat", pInfo.versionCode);			
		}
		catch (Exception e) 
		{
			AddLog("mPennyuChat not detected");
		}
  	
  	try 
		{
			PackageInfo pInfo;
			pInfo = getPackageManager().getPackageInfo("com.dtj.mail", 0);			
			AddLog("mPennyuMail v" + pInfo.versionName);
			
			if (db.getInt("mPennyuMail", 1) < pInfo.versionCode)
				db.putInt("mPennyuMail", pInfo.versionCode);			
		}
		catch (Exception e) 
		{
			AddLog("mPennyuMail not detected");
		}  	  	
  }
         
  /*private String GetDLFolder(String sFilename)
	{		
		String sDLFolder = "";
		
		String[] arrSD = R1Util.getStorageDirectories();
		for(String sDir: arrSD)					
		{
			// check if storage exists
			String sFile = sDir + "/" + sFilename;			
			if (R1Util.IsFileExists(sFile))
			{
				sDLFolder = sDir;				
				break;
			}
		}
						
		AddLog("Storage: " + sDLFolder);
		
		return sDLFolder;
	}*/
     
  /*private void AddLog(final String str) 
	{  	
		runOnUiThread(new Runnable() 
		{
			@Override
			public void run() 
			{
				try			  	
				{
					m_tbLog.append(str + "\n");
				}
		  	catch (Exception ex)
		  	{}
			}
		});  	
  	
  	Log.i("MSA LOG", str);
	}*/    
  
  /*Handler m_MSAUninstallHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{			
			Bundle bnd = msg.getData();
			
			try
			{				
				m_APK.clear();							
				
				String[] arrAPK = bnd.getString("APK").split("\\|");
				for (String str: arrAPK)
				{
					APKUpdate apk = new APKUpdate();
					apk.setM_file(new File(str));
					if (str.toLowerCase().indexOf("call") >= 0)
						apk.setM_sPackageName("com.dtj.call");
					else if (str.toLowerCase().indexOf("chat") >= 0)
						apk.setM_sPackageName("com.dtj.chat");
					else if (str.toLowerCase().indexOf("mail") >= 0)
						apk.setM_sPackageName("com.dtj.mail");
					m_APK.add(apk);
				}
				
				StartInstallationThread();								
			}
			catch (Exception ex)
			{				
			}
		}
	};
	
	private void UninstallAPK(List<APKUpdate> files) // easy way
  {
  	for (int i=0;i<files.size();i++)
  	{
  		APKUpdate apk = files.get(i);
	  	Uri packageUri = Uri.parse("package:" + apk.getM_sPackageName());
	    Intent uninstallIntent =
	      new Intent(Intent.ACTION_DELETE, packageUri);
	    MainActivity.this.startActivity(uninstallIntent);
  	}
  }*/
  
  Handler m_MSAInstallHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{			
			Bundle bnd = msg.getData();
			
			try
			{				
				m_APK.clear();							
				
				String[] arrAPK = bnd.getString("APK").split("\\|");
				for (String str: arrAPK)
				{
					APKUpdate apk = new APKUpdate();
					apk.setM_file(new File(str));
					if (str.toLowerCase().indexOf("call") >= 0)
						apk.setM_sPackageName("com.dtj.call");
					else if (str.toLowerCase().indexOf("chat") >= 0)
						apk.setM_sPackageName("com.dtj.chat");
					else if (str.toLowerCase().indexOf("mail") >= 0)
						apk.setM_sPackageName("com.dtj.mail");
					else if (str.toLowerCase().indexOf("msalauncher") >= 0)
						apk.setM_sPackageName("r1.msalauncher");
					m_APK.add(apk);
				}
				
				StartInstallationThread();								
			}
			catch (Exception ex)
			{				
			}
		}
	};
	
	private void StartInstallationThread()
	{
		new Thread() 
		{
			@Override
			public void run() 
			{
				try
				{
					for (int i=0;i<m_APK.size();i++)
					{							  		
						APKUpdate upd = m_APK.get(i);
						File f = upd.getM_file();
			  		Log.e("APK UPDATE", "Filename: " + upd.getM_file().getPath()); 
			  		
				  	Intent intent = new Intent(Intent.ACTION_VIEW);
				    intent.setDataAndType(Uri.fromFile(f), 
				    		"application/vnd.android.package-archive");
				    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				    startActivity(intent);
				    //MainActivity.this.startActivityForResult(intent, APK_INSTALL + i);
				    
				    /*while (!upd.isM_bProcessed())
				  	{
				  		Thread.sleep(1000);
				  		
				  		if (upd.isM_bProcessed())
				  			break;
				  	}*/
				    
				    Thread.sleep(1000);
					}										
				}
				catch (Exception ex)
				{}
				
				int nCounter = 0;
				while (nCounter < 60) // 60 seconds timeout
		  	{
					try
		  		{
						Thread.sleep(1000);
						nCounter++;
		  				  		
						boolean bOK = true;
		  			for (APKUpdate apk: m_APK)
		  			{
		  				if (!apk.isM_bProcessed())
		  				{
		  					bOK = false;
		  					break;
		  				}
		  			}
		  			
		  			if (bOK)
		  				break;
		  		}
		  		catch (Exception ex)
		  		{
		  			break;
		  		}
		  	}
				
				DeleteAPKUpdate();
			}
		}.start();
	}
	
	private void StartInstallUpdates()
	{								
		if (m_bThreadInstallUpdate)
			return;
		
		m_bThreadInstallUpdate = true;
		
		Executors.newSingleThreadExecutor().execute(new Runnable() 
		{
	    @Override 
	    public void run() 
	    {
	    	int nCount = 1;
				boolean bInstall = false;
				String[] arrSD = R1Util.getStorageDirectories();
				
				AddLog("StartInstallUpdates1");
								
				// if update is copied directly into sdcard
				for(String sDir: arrSD)					
				{
					try
					{
						String[] arrZIP = R1Util.ListFilesInDir(sDir, ".zip");
						if (arrZIP.length > 0)
						{
							for (String sZIP: arrZIP)
							{							
								String sFilename = sDir + "/" + sZIP;							
								if (sZIP.startsWith("mob-update"))
								{
									String sFolder = sDir + "/msaupdate";			        	
				        	if (R1Util.IsFileExists(sFolder))
				        		R1Util.DeleteDir(new File(sFolder));
				        	
				        	File folder = new File (sFolder);
				        	if (!folder.mkdir())
				        		return;
				        	
				        	String sFilenameDecrypt = String.format("%s/decrypt%d.zip", sFolder, nCount);
				        	
									if (!R1Util.ReverseObfuscateFile(sFilename, sFilenameDecrypt))
					        {				
										R1Util.DeleteDir(new File(sFilename));
										R1Util.DeleteDir(new File(sFilenameDecrypt));
					        	continue;
					        }
									
									//String sUnzipFolder = String.format("%s/msaupdate", sDir);				
									Unzip(sFilenameDecrypt, sFolder);
									
									String[] arrAPK = R1Util.ListFilesInDir(sFolder, ".apk");
					  			if (arrAPK.length > 0)
					  			{				  				
					  				for (String sFile: arrAPK)
					  				{
					  					bInstall = true;
					  					String sAPKFile = sFolder + "/" + sFile;
					  					File f = new File(sAPKFile);
								  		Log.e("APK UPDATE", "Filename: " + f.getPath()); 
								  		
									  	Intent intent = new Intent(Intent.ACTION_VIEW);
									    intent.setDataAndType(Uri.fromFile(f), 
									    		"application/vnd.android.package-archive");
									    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									    startActivity(intent);
									    
									    try 
									    {
												Thread.sleep(100);
											} 
									    catch (Exception e) 
											{}
					  				}
					  			}
					  			
					  			R1Util.DeleteDir(new File(sFilename));
									R1Util.DeleteDir(new File(sFilenameDecrypt));
								}
							}												
						}					
					}
					catch (Exception ex)
					{}			
				}
				
				AddLog("StartInstallUpdates2");
				
				TinyDB db = new TinyDB(getApplicationContext());
				int nUpdateAvailable = db.getInt("UpdateAvailable", 0);
				
				// if update is received from server
				if (!bInstall && nUpdateAvailable == 1)
				{										
					try
					{												
						String sFolder = R1Util.GetDLFolder() + "/msaupdate"; 
						String[] arrAPK = R1Util.ListFilesInDir(sFolder, ".apk");
		  			if (arrAPK.length > 0)
		  			{				  				
		  				// make sure mpennyusuite is the first to appear (and thus will be the last to be installed)
		  				for (String sFile: arrAPK)
		  				{		  					
		  					if (sFile.toLowerCase().indexOf("mpennyusuite") >= 0)
		  					{
			  					bInstall = true;
			  					String sAPKFile = sFolder + "/" + sFile;
			  					File f = new File(sAPKFile);
						  		Log.e("APK UPDATE", "Filename: " + f.getPath()); 
						  		
							  	Intent intent = new Intent(Intent.ACTION_VIEW);
							    intent.setDataAndType(Uri.fromFile(f), 
							    		"application/vnd.android.package-archive");
							    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							    startActivity(intent);
		  					}
						    
						    try 
						    {
									Thread.sleep(100);
								} 
						    catch (Exception e) 
								{}
		  				}
		  				
		  				for (String sFile: arrAPK)
		  				{
			  				if (sFile.toLowerCase().indexOf("mpennyusuite") < 0)
		  					{
			  					bInstall = true;
			  					String sAPKFile = sFolder + "/" + sFile;
			  					File f = new File(sAPKFile);
						  		Log.e("APK UPDATE", "Filename: " + f.getPath()); 
						  		
							  	Intent intent = new Intent(Intent.ACTION_VIEW);
							    intent.setDataAndType(Uri.fromFile(f), 
							    		"application/vnd.android.package-archive");
							    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							    startActivity(intent);
		  					}
						    
						    try 
						    {
									Thread.sleep(600);
								} 
						    catch (Exception e) 
								{}
		  				}
		  			}				  					  			
					}
					catch (Exception ex)
					{}
				}
				
				AddLog("StartInstallUpdates3");
				
				//TinyDB db = new TinyDB(getApplicationContext());
				db.putInt("UpdateAvailable", 0);
				
				m_InstallUpdateHandler.sendEmptyMessage(1);
				
				if (bInstall)
				{
					int nCounter = 0;
					while (nCounter < 80) // 80 seconds timeout, after this all updates will be deleted
			  	{
						try
			  		{
							Thread.sleep(1000);
							nCounter++;			  				  								
			  		}
			  		catch (Exception ex)
			  		{
			  			break;
			  		}
			  	}
				}
				
				AddLog("StartInstallUpdates4");
				
				DeleteAPKUpdate();
								
				//m_InstallUpdateHandler.sendEmptyMessage((bInstall) ? 1 : 0);				
				//m_InstallUpdateHandler.sendEmptyMessage(1);
				
				AddLog("StartInstallUpdates5");
				
				m_bThreadInstallUpdate = false;
	    }
	    });				
	}
	
	Handler m_InstallUpdateHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{			
			if (msg.what == 1)
			{
				/*Intent intent = new Intent();
				intent.setAction(MSAService.ACTION);
				intent.putExtra("RQS", MailConstants.ServiceCmd.UpdateNotification.Value());
				sendBroadcast(intent);*/  			  			
				
				Intent intent = new Intent();
				intent.setAction(MSAService.ACTION);
				intent.putExtra("RQS", MailConstants.ServiceCmd.CancelNotification.Value());
				sendBroadcast(intent);
			}			
		}
	};
	
	private void DeleteAPKUpdate()
  {				
		String[] arrSD = R1Util.getStorageDirectories();
		for(String sDir: arrSD)					
		{
			try
			{
				// delete folder /sdcard/msaupdate
				String sUpdateDir = sDir + "/msaupdate";			
				if (R1Util.IsFileExists(sUpdateDir))			
					R1Util.DeleteDir(new File(sUpdateDir));
				
				// delete apk in root dir
				String[] arrAPK = R1Util.ListFilesInDir(sDir, ".apk");
				if (arrAPK.length > 0)
				{
					for (String sAPK: arrAPK)
					{
						String sFilePath = sDir + "/" + sAPK;
						R1Util.DeleteDir(new File(sFilePath));		  					  	          				
					}
				}								
				
				// delete contact.csv
				String[] arrCSV = R1Util.ListFilesInDir(sDir, ".csv");
				if (arrCSV.length > 0)
				{
					for (String sCSV: arrCSV)
					{
						String sFilePath = sDir + "/" + sCSV;
						R1Util.DeleteDir(new File(sFilePath));		  					  	          				
					}
				}
			}
			catch (Exception ex)
			{}
		}								
  }
	
	public boolean Unzip(String sZipFile, String sZipFolder) 
	{
		ZipInputStream is = null;
		OutputStream os = null;
		boolean bOK = false;
		
		try 
		{
			// Initiate the ZipFile
			ZipFile zipFile = new ZipFile(sZipFile);
			String destinationPath = sZipFolder;
			
			// If zip file is password protected then set the password
			if (zipFile.isEncrypted()) {
				zipFile.setPassword("##(com.android.zip)7453708481(dtj.r1.msa)@@");
			}
			
			//Get a list of FileHeader. FileHeader is the header information for all the
			//files in the ZipFile
			List fileHeaderList = zipFile.getFileHeaders();
			
			// Loop through all the fileHeaders
			for (int i = 0; i < fileHeaderList.size(); i++) 
			{
				FileHeader fileHeader = (FileHeader)fileHeaderList.get(i);
				if (fileHeader != null) 
				{					
					//Build the output file
					String outFilePath = destinationPath + System.getProperty("file.separator") + fileHeader.getFileName();
					File outFile = new File(outFilePath);
					
					//Checks if the file is a directory
					if (fileHeader.isDirectory()) 
					{
						//This functionality is up to your requirements
						//For now I create the directory
						outFile.mkdirs();
						continue;
					}
					
					//Check if the directories(including parent directories)
					//in the output file path exists
					File parentDir = outFile.getParentFile();
					if (!parentDir.exists()) {
						parentDir.mkdirs();
					}
					
					//Get the InputStream from the ZipFile
					is = zipFile.getInputStream(fileHeader);
					//Initialize the output stream					
					os = new FileOutputStream(outFile);
					//os = m_ctx.openFileOutput(outFile.getPath(), Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
					
					int readLen = -1;
					byte[] buff = new byte[4096];
					long nRead = 0;
					
					//Loop until End of File and write the contents to the output stream
					while ((readLen = is.read(buff)) != -1) 
					{
						os.write(buff, 0, readLen);
						nRead += readLen;
						
						//UpdateSize(String.format("%s: %d KB", outFile, nRead/1024));
					}
					
					//Please have a look into this method for some important comments
					closeFileHandlers(is, os);
					
					//To restore File attributes (ex: last modified file time, 
					//read only flag, etc) of the extracted file, a utility class
					//can be used as shown below
					UnzipUtil.applyFileAttributes(fileHeader, outFile);
															
					System.out.println("Done extracting: " + fileHeader.getFileName());
					
					bOK = true;
				} 
				else 
				{
					System.err.println("fileheader is null. Shouldn't be here");
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			bOK = false;
		} 
		finally 
		{
			try 
			{
				closeFileHandlers(is, os);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		return bOK;
	}
	
	private void closeFileHandlers(ZipInputStream is, OutputStream os) throws IOException
	{
		//Close output stream
		if (os != null) {
			os.close();
			os = null;
		}
		
		//Closing inputstream also checks for CRC of the the just extracted file.
		//If CRC check has to be skipped (for ex: to cancel the unzip operation, etc)
		//use method is.close(boolean skipCRCCheck) and set the flag,
		//skipCRCCheck to false
		//NOTE: It is recommended to close outputStream first because Zip4j throws 
		//an exception if CRC check fails
		if (is != null) {
			is.close();
			is = null;
		}
	}
	
	/*public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
		int nIndex = requestCode - APK_INSTALL;
		int nRequestCode = requestCode - nIndex;
		
		switch (nRequestCode)
    {
      case APK_INSTALL:
      {
      	if (resultCode == RESULT_OK)
      	{
        	int nResult = data.getIntExtra("result", 0);
        	try
        	{
        		m_APK.get(nIndex).setM_sResult(nResult == 0 ? "Failed" : "OK");
        		m_APK.get(nIndex).setM_bProcessed(true);        		
        	}
        	catch (Exception ex)
        	{        		
        	}
      	}
      	else
      	{
      		m_APK.get(nIndex).setM_bProcessed(true);
      		m_APK.get(nIndex).setM_sResult("Failed");
      	}
      }
    }
  }*/
  
  private BroadcastReceiver MSAReceiver = new BroadcastReceiver()
  {
		@Override
		public void onReceive(Context arg0, Intent arg1) 
		{			
			//stopSelf();
			
			//Log.e("MSAReceiver", arg1.getAction());
			
			if(arg1.getAction().equals("com.dtj.msa.MSA_SERVICE_LOG")) 
	  	{
				String sLog = arg1.getStringExtra("Log");
				AddLog(sLog);
	  	}						
			else if (arg1.getAction().equals("com.dtj.msa.MSA_INSTALL_APK"))
			{
				String sAPK = arg1.getStringExtra("APK");
				
				Message msg = Message.obtain();
				Bundle bnd = new Bundle();							
				bnd.putString("APK", sAPK);										
				msg.setData(bnd);
				
				m_MSAInstallHandler.sendMessage(msg);
			}
			else if (arg1.getAction().equals("com.dtj.msa.MSA_CONNECT"))
			{
				int nConnect = arg1.getIntExtra("Connect", 0);
				//String sErrMsg = arg1.getStringExtra("ErrorMsg");
				
				TinyDB db = new TinyDB(getApplicationContext());
				db.putInt("ConnectedToServer", nConnect);
			}
			else if (arg1.getAction().equals("com.dtj.msa.MSA_SETUP"))
			{
				runOnUiThread(new Runnable() 
				{
					@Override
					public void run() 
					{
						// display panels
						boolean bActivated = IsMSA_Activated();
						SetupView(bActivated);			
						SetupText(bActivated);
					}
				});				
			}
			else if (arg1.getAction().equals("com.dtj.msa.MSA_DOWNLOAD"))
			{
				int nRead = arg1.getIntExtra("Read", 0);
				int nSize = arg1.getIntExtra("Size", 0);
				int nFinished = arg1.getIntExtra("Finished", 0);
				m_dlInfo.setnRead(nRead);
				m_dlInfo.setnSize(nSize);
				m_dlInfo.setnFinished(nFinished);
				
				runOnUiThread(new Runnable() 
				{
					@Override
					public void run() 
					{
						// display panels
						//boolean bActivated = IsMSA_Activated();									
						SetupDownloadText();
					}
				});
			}
			/*else if (arg1.getAction().equals("com.dtj.msa.QUERY_SETUP_PUMAMAIL"))
			{
				runOnUiThread(new Runnable() 
				{
					@Override
					public void run() 
					{
						Log.v("TEST SETUP 1", "Receiving com.dtj.msa.QUERY_SETUP_PUMAMAIL");
						String sINIPath = getApplicationContext().getFilesDir().getPath() + "/msa/Ini/Settings.ini";
						INIFile settings = R1Util.ReadEncryptedSettings(getApplicationContext(), sINIPath);
						String sUsername = settings.getStringProperty("MSASettings", "voip.username");
						String sPassword = settings.getStringProperty("MSASettings", "voip.password");
						String sServer = settings.getStringProperty("MSASettings", "VoipServer");
						
						Log.v("TEST SETUP 1", String.format("%s, %s, %s", sUsername, sPassword, sServer));
						
						Intent br = new Intent();
						br.setAction("org.sipdroid.MSA_SETUP_PARAM");
						br.putExtra("username", sUsername);
						br.putExtra("password", sPassword);
						br.putExtra("server", sServer);
						getApplicationContext().sendBroadcast(br);
					}
				});				
			}*/
			
			/*else if (arg1.getAction().equals("android.intent.action.PHONE_STATE"))
			{
				String sOldState = m_sPhoneState;
				m_sPhoneState = arg1.getStringExtra(TelephonyManager.EXTRA_STATE);
				
				if (m_sPhoneState.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE) && 
						!sOldState.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE))
				{
					MainActivity.this.runOnUiThread(new Runnable()
					{				
						public void run()
						{
							try
							{
								Thread.sleep(500);
							}
							catch (Exception ex)
							{}
							
							// bring our app forward
							Intent intent2 = new Intent(MainActivity.this, MainActivity.class);
							MainActivity.this.startActivity(intent2);
						}
					});	
				}
			}*/
			
			/*else if (arg1.getAction().equals("com.dtj.msa.MSA_INSTALL_APK"))
			{
				m_MSAUninstallHandler.sendMessage(msg);
			}*/
			/*else if (arg1.getAction().equals(Intent.ACTION_PACKAGE_ADDED) ||
							 arg1.getAction().equals(Intent.ACTION_PACKAGE_REPLACED))
			{
				if (R1Util.IsPackageExist(getApplicationContext(), "com.dtj.mail"))
					SetInstallationResult("com.dtj.mail");
				
				if (R1Util.IsPackageExist(getApplicationContext(), "com.dtj.chat"))
					SetInstallationResult("com.dtj.chat");
				
				if (R1Util.IsPackageExist(getApplicationContext(), "com.dtj.call"))
					SetInstallationResult("com.dtj.call");
			}*/			
		}
	};
	
	private void SetInstallationResult(String sPackageName)
	{
		try
		{
			for (APKUpdate apk: m_APK)
			{
				if (apk.getM_sPackageName().equalsIgnoreCase(sPackageName))
				{
					apk.setM_bProcessed(true);
					break;
				}
			}
		}
		catch (Exception ex)
		{}
	}
  
  /*OnMailSenderListener SenderListener = new OnMailSenderListener() 
  {		
		@Override
		public void onMailSent(boolean bSendOK, TimsTracCmd cmd) 
		{
			if (bSendOK)
				AddLog(R1Util.GetDateTime(Calendar.getInstance().getTime(), DateTimeFormat.HHMMSS) +  " Email sent: " + cmd.toString());
			else
				AddLog(R1Util.GetDateTime(Calendar.getInstance().getTime(), DateTimeFormat.HHMMSS) +  " Failed to send email: " + cmd.toString());				
		}
	};  		
	
	OnMailReaderListener ReaderListener = new OnMailReaderListener() 
	{
		@Override
		public void onConnectionLost() 
		{						
			AddLog(R1Util.GetDateTime(Calendar.getInstance().getTime(), DateTimeFormat.HHMMSS) +  " IMAP connection lost");
						
			// reconnect again if it wasnt manually disconnected			
			runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					//if (!m_mailReader.isM_bManualDisconnect())
						//m_mailReader.Connect();
				}
			});
		}
		
		@Override
		public void onConnect(boolean bConnectOK) 
		{
			if (bConnectOK)
				AddLog(R1Util.GetDateTime(Calendar.getInstance().getTime(), DateTimeFormat.HHMMSS) +  " Connected to IMAP server");
			else
				AddLog(R1Util.GetDateTime(Calendar.getInstance().getTime(), DateTimeFormat.HHMMSS) +  " Failed to connect to IMAP server");
		}
		
		@Override
		public void onCommand(TimsTracCmd cmd) 
		{						
			AddLog(R1Util.GetDateTime(Calendar.getInstance().getTime(), DateTimeFormat.HHMMSS) +  " Command received: " + cmd.toString());
		}
		
		@Override
		public void onReply(TimsTracCmd cmd) 
		{						
			AddLog(R1Util.GetDateTime(Calendar.getInstance().getTime(), DateTimeFormat.HHMMSS) +  " Reply received: " + cmd.toString());
		}

		@Override
		public void onError(String sError) 
		{
			AddLog(R1Util.GetDateTime(Calendar.getInstance().getTime(), DateTimeFormat.HHMMSS) + " " + sError);		
		}
		
		@Override
		public void onInfo(String sInfo) 
		{
			AddLog(R1Util.GetDateTime(Calendar.getInstance().getTime(), DateTimeFormat.HHMMSS) + " " + sInfo);		
		}
	};*/
	
	/*class RunMSALServiceTask extends AsyncTask<String, Void, Integer> 
  {
    private Exception exception;

    protected Integer doInBackground(String... urls) 
    {
      try 
      {      	
    		Intent intent = new Intent();
				intent.setAction("r1.msalauncher.START_SERVICE");						
				sendBroadcast(intent);
				
				Thread.sleep(2000);
				
				Intent br = new Intent();
				br.setAction("r1.msalauncher.UPDATE_MSA");						
				sendBroadcast(br);    		
      } 
      catch (Exception e) 
      {
        this.exception = e;
        return 0;
      }
      
      return 1;
    }

    protected void onPostExecute(Integer n) {
        // TODO: check this.exception 
        // TODO: do something with the feed
    }
  }*/
}
