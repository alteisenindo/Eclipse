package com.dtj.msa;

import java.io.BufferedReader;
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
import com.dtj.email.MailConstants.TimsTracCmd;
import com.dtj.email.MailReader.OnMailReaderListener;
import com.dtj.email.MailSender.OnMailSenderListener;

import r1.util.BitManager;
import r1.util.R1Util;
import r1.util.iCCConstants.DateTimeFormat;
import r1.util.iCCConstants.EHTTPRequestType;
import r1.util.iCCConstants.EParseResult;
import r1.util.iCCConstants.EPumaQueryResult;
import r1.util.iCCConstants.SendProtocolCode;
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
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class MSAMutexMailService extends Service 
{				
	public final static String ACTION = "MSAMutexMailServiceAction"; // for PUMA MAIL
	final static String STOP_SERVICE = "";
	/*final static int RQS_STOP_SERVICE = 1;
	final static int RQS_CMD_ACTIVATE = 1;
	final static int RQS_STOP_SERVICE = 1;*/
			
	NotifyServiceReceiver notifyServiceReceiver;
	
	private static final int MY_NOTIFICATION_ID = 171;
	private NotificationManager notificationManager;
	private Notification myNotification;
		
	//private final String myBlog = "http://android-er.blogspot.com/";
	
	//SentSMSHandler m_SentSMSObserver = null;	
	
	@Override
	public void onCreate() 
	{
		// TODO Auto-generated method stub
		notifyServiceReceiver = new NotifyServiceReceiver();		
		
		super.onCreate();		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		Context context = getApplicationContext();
						
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION);
		registerReceiver(notifyServiceReceiver, intentFilter);
		
		AddLog("MSAMutexMail service started");
						
		//CreateNotification(context);								
		
		//return super.onStartCommand(intent, flags, startId);
		//return START_STICKY;
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() 
	{
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(MY_NOTIFICATION_ID);
		
		// TODO Auto-generated method stub
		this.unregisterReceiver(notifyServiceReceiver);
						
		super.onDestroy();
	}		
			
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void AddLog(final String str) 
	{
		Intent intent = new Intent();
		intent.setAction("com.dtj.msa.MSA_SERVICE_LOG");
		intent.putExtra("Log", R1Util.GetDateTime(Calendar.getInstance().getTime(), DateTimeFormat.HHMMSS) +  " "  + str);
		sendBroadcast(intent);		  	
	}
			
	public class NotifyServiceReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context arg0, Intent arg1) 
		{			
			//stopSelf();
			
			if (arg1.getAction().equalsIgnoreCase(ACTION))
			{
				int rqs = arg1.getIntExtra("RQS", 100);												
				if (rqs == MailConstants.ServiceCmd.StopService.Value())
				{
					AddLog("MSAMutexMail service stopped");
					stopSelf();
				}
				else if (rqs == MailConstants.ServiceCmd.Setup.Value())
				{
					//m_engine.getM_mailSender().SendCmd(TimsTracCmd.Activation);
				}
				else if (rqs == MailConstants.ServiceCmd.Activation.Value())
				{
					//m_engine.getM_mailSender().SendCmd(TimsTracCmd.Activation);
				}
				else if (rqs == MailConstants.ServiceCmd.DefConfig.Value())
				{
					//m_engine.getM_mailSender().SendCmd(TimsTracCmd.DefConfig);
				}
			}
		}
	}
	
}
