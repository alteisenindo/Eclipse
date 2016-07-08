package com.dtj.msa;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.dtj.email.MailConstants;
import com.dtj.email.TinyDB;
import com.dtj.email.MailConstants.TimsTracCmd;
import com.dtj.email.MailReader.OnMailReaderListener;
import com.dtj.email.MailSender.OnMailSenderListener;

import r1.util.BitManager;
import r1.util.INIFile;
import r1.util.R1LogFile;
import r1.util.R1Util;
import r1.util.iCCConstants.DateTimeFormat;
import r1.util.iCCConstants.EHTTPRequestType;
import r1.util.iCCConstants.EParseResult;
import r1.util.iCCConstants.EPumaQueryResult;
import r1.util.iCCConstants.SendProtocolCode;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class MSAService extends Service 
{				
	final static String ACTION = "MSAServiceAction";
	//final static String STOP_SERVICE = "";
	/*final static int RQS_STOP_SERVICE = 1;
	final static int RQS_CMD_ACTIVATE = 1;
	final static int RQS_STOP_SERVICE = 1;*/
			
	NotifyServiceReceiver notifyServiceReceiver;
	
	private static final int MY_NOTIFICATION_ID=118;
	private static final int NOTI_UPDATE_ID=119;
	//private NotificationManager notificationManager;
	//private Notification myNotification;
	//private final String myBlog = "http://android-er.blogspot.com/";
	
	//SentSMSHandler m_SentSMSObserver = null;
	Engine m_engine;	
	//R1LogFile m_logFile;
	
	R1LogFile m_logSystem;
	INIFile m_settings = null;
	String m_sINIPath;
	
	WakeLock m_wakeLock;
	
	public static Context mContext;
	
	@Override
	public void onCreate() 
	{
		/*m_logFile = new R1LogFile(    		
				//this.getFilesDir().getPath() + "/launcher/Log/SystemLog.txt",
    		GetDLFolder() + "/msaservice.log",
				256, "Service Log", false);*/
		
		mContext = getApplicationContext();
		
		// keep CPU on as long as mPennyuSuite is active --> IMPORTANT because JavaMail may fail if cpu is turned off after a while
		PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		m_wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MSA LOCK");		
		m_wakeLock.acquire();

		
		/*String sFolder = R1Util.GetDLFolder() + "/MSALog";	  	
	  if (!R1Util.IsFileExists(sFolder))
	  {
	  	File f = new File(sFolder);
	  	f.mkdir();
	  }
		
		m_logSystem = new R1LogFile(sFolder + "/ServiceLog.txt", 1024, 
	  		"Service Log", false);*/
		
		TinyDB db = new TinyDB(mContext);
		db.putInt("ShutdownService", 0);
		
		// TODO Auto-generated method stub
		notifyServiceReceiver = new NotifyServiceReceiver();
		m_engine = new Engine(mContext, SenderListener, ReaderListener, m_logSystem);		
		
		super.onCreate();
	}	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{					
		AddLog("MSAService OnStartCommand");
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION);
		registerReceiver(notifyServiceReceiver, intentFilter);
		
		try 
		{
			Thread.sleep(300);
		} 
		catch (Exception e) 
		{}
		
		ShowNotification();
						
		//CreateNotification(mContext);
		
		//SetAlarm(600, OneShotAlarm2.class); // reset service every 10 minutes
		
		//return super.onStartCommand(intent, flags, startId);
		//return START_STICKY;
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() 
	{		
		AddLog("MSAService OnDestroy");
		
		try
		{			
			TinyDB db = new TinyDB(mContext);
			db.putInt("ShutdownService", 1);
			
			m_engine.Cleanup();
			
			//notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			//notificationManager.cancel(MY_NOTIFICATION_ID);
			
			// TODO Auto-generated method stub
			this.unregisterReceiver(notifyServiceReceiver);
		}
		catch (Exception ex)
		{}
		
		m_wakeLock.release();
		
		//SetAlarm(0, OneShotAlarm2.class);				   			
		stopForeground(true);
						
		super.onDestroy();
	}		
	
	private void ShowNotification()
	{
		//final Intent emptyIntent = new Intent();
		//PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);				
		
		NotificationCompat.Builder mBuilder =
		    new NotificationCompat.Builder(this)
		    .setSmallIcon(R.drawable.icon32)
		    .setContentTitle(getText(R.string.app_name))
		    .setContentText(getText(R.string.app_name))		    
		    .setContentIntent(pendingIntent); //Required on Gingerbread and below			
 
		//NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		//notificationManager.notify(MY_NOTIFICATION_ID, mBuilder.build());
		
		startForeground(MY_NOTIFICATION_ID, mBuilder.build());
	}
	
	private void UpdateNotification(String sText)
	{
		/*String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(ns);
		long when = System.currentTimeMillis();
		
		Notification notification = new Notification(R.drawable.icon32, sText, when);		
		mNotificationManager.notify(MY_NOTIFICATION_ID, notification);*/
		
		Intent intent = new Intent(this, MainActivity.class);
		//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);				
		
		NotificationCompat.Builder mBuilder =
		    new NotificationCompat.Builder(this)
		    .setSmallIcon(R.drawable.update_icon)
		    .setContentTitle(getText(R.string.app_name))
		    .setContentText(sText)		    
		    .setContentIntent(pendingIntent); //Required on Gingerbread and below
				
		NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTI_UPDATE_ID, mBuilder.build());		
	}
	
	private void CancelNotification()
	{
		NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTI_UPDATE_ID);
	}
			
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void AddLog(final String str) 
	{
		/*try
		{
			Intent intent = new Intent();
			intent.setAction("com.dtj.msa.MSA_SERVICE_LOG");
			intent.putExtra("Log", str);
			sendBroadcast(intent);
			
			Log.i("MSA SERVICE LOG", str);
		}
		catch (Exception ex)
		{}*/
		
		//m_logFile.Log(str, false);
		
		try
		{
			//m_logSystem.Log(str, true);		
			Log.i("MSA SERVICE LOG", str);
		}
		catch (Exception ex)
		{}
	}
	
	OnMailSenderListener SenderListener = new OnMailSenderListener() 
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
			/*runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					//if (!m_mailReader.isM_bManualDisconnect())
						//m_mailReader.Connect();
				}
			});*/
		}
		
		@Override
		public void onConnect(boolean bConnectOK) 
		{
			if (bConnectOK)
			{
				AddLog(R1Util.GetDateTime(Calendar.getInstance().getTime(), DateTimeFormat.HHMMSS) +  " Connected to IMAP server");
				
				Intent intent = new Intent();
				intent.setAction("com.dtj.msa.MSA_CONNECT");
				intent.putExtra("Connect", 1);				
				sendBroadcast(intent);
			}
			else
			{
				TinyDB db = new TinyDB(getApplicationContext());
				/*AddLog(R1Util.GetDateTime(Calendar.getInstance().getTime(), DateTimeFormat.HHMMSS) +  
						" Failed to connect to IMAP server: " + db.getString("ConnectionError"));*/
				
				Intent intent = new Intent();
				intent.setAction("com.dtj.msa.MSA_CONNECT");
				intent.putExtra("Connect", 2);				
				intent.putExtra("ErrorMsg", db.getString("ConnectionError"));
				sendBroadcast(intent);
			}
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

		@Override
		public void onUpdateAvailable(int nUpdate) 
		{			
			UpdateNotification(String.format("%d %s", nUpdate, getText(R.string.sUpdateAvailable).toString()));				
		}
	};

	public class NotifyServiceReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context arg0, Intent arg1) 
		{			
			//stopSelf();			
			try
			{
				int rqs = arg1.getIntExtra("RQS", 100);
				if (rqs == MailConstants.ServiceCmd.StopService.Value())
				{
					m_engine.Cleanup();
					stopSelf();
				}
				else if (rqs == MailConstants.ServiceCmd.UpdateNotification.Value())
				{
					UpdateNotification(getText(R.string.app_name).toString());
				}
				else if (rqs == MailConstants.ServiceCmd.CancelNotification.Value())
				{
					CancelNotification();
				}
				else if (rqs == MailConstants.ServiceCmd.Activation.Value())
				{
					m_engine.getM_mailSender().SendCmd(TimsTracCmd.Activation);
				}
				else if (rqs == MailConstants.ServiceCmd.DefConfig.Value())
				{
					m_engine.getM_mailSender().SendCmd(TimsTracCmd.DefConfig);
				}			
				else if (rqs == MailConstants.ServiceCmd.GetPasswordList.Value())
				{
					m_engine.getM_mailSender().SendCmd(TimsTracCmd.GetPasswordList);
				}
			}
			catch (Exception ex)
			{}
		}
	}
	
	/*private String GetDLFolder()
	{		
		String sDLFolder = "";
		
		String[] arrSD = R1Util.getStorageDirectories();
		for(String sDir: arrSD)					
		{
			// check if storage exists
			String sFile = sDir + "/test0011.tst";	
			R1Util.SaveFile(sFile, "test");
			if (R1Util.IsFileExists(sFile))
			{
				sDLFolder = sDir;
				R1Util.DeleteDir(new File(sFile));
				break;
			}
		}		
		
		return sDLFolder;
	}*/
		
}
