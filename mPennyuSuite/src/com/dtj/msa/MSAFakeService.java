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
import com.dtj.email.TinyDB;
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
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class MSAFakeService extends Service 
{				
	final static String ACTION = "MSAServiceAction";
	//NotifyServiceReceiver notifyServiceReceiver;	
	private static final int MY_NOTIFICATION_ID=118;
	
	@Override
	public void onCreate() 
	{
		// TODO Auto-generated method stub
		//notifyServiceReceiver = new NotifyServiceReceiver();
		
		//Log.e("FAKE SERVICE", "onCreate");
		
		new Thread() 
		{
			@Override
			public void run() 
			{
				//Log.e("FAKE SERVICE", "Thread started");
				
				try 
				{
					Thread.sleep(2000);
				}
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				m_MSAFakeHandler.sendEmptyMessage(1);						
			}
		}.start();
		
		super.onCreate();
	}
	
	Handler m_MSAFakeHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{			
			if (msg.what == 1)
			{
				stopSelf();
			}
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		//Log.e("FAKE SERVICE", "onStartCommand");
		
		Context context = getApplicationContext();
						
		/*IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION);
		registerReceiver(notifyServiceReceiver, intentFilter);*/
		
		ShowNotification();							
		
		//return super.onStartCommand(intent, flags, startId);
		//return START_STICKY;
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() 
	{
		/*try
		{			
			//notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			//notificationManager.cancel(MY_NOTIFICATION_ID);
			
			// TODO Auto-generated method stub
			//this.unregisterReceiver(notifyServiceReceiver);			
		}
		catch (Exception ex)
		{}*/
		
		Log.e("FAKE SERVICE", "onDestroy");
		
		stopForeground(true);
						
		super.onDestroy();
	}		
	
	private void ShowNotification()
	{
		final Intent emptyIntent = new Intent();
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
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
			
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
		
	/*public class NotifyServiceReceiver extends BroadcastReceiver
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
					stopSelf();
				}						
			}
			catch (Exception ex)
			{}
		}
	}*/
	
}
