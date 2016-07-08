package com.dtj.email;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import r1.util.INIFile;
import r1.util.R1LogFile;
import r1.util.R1Util;
import r1.util.RootUtil;
import r1.util.TinyDB;

import com.dtj.email.MailConstants.TimsTracCmd;
import com.dtj.msa.MSAMutexChatService;
import com.dtj.msa.MSAMutexMailService;
import com.dtj.msa.MSAMutexService;
import com.dtj.msa.MSAService;
import com.dtj.msa.MainActivity;
import com.dtj.msa.OneShotAlarm2;
import com.dtj.msa.StartServiceAlarm;
import com.dtj.msa.WifiConfigManager;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootTools.Result;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;

import Decoder.BASE64Encoder;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class MailReader 
{
	Context m_ctx;
  
  //Calendar m_calLastRead = null;
  
  MailSender m_mailSender;
  INIFile m_settings;
  String m_sINIPath;
  R1LogFile m_logSys;
  
  static String _ACTIVATION_KEY_1 = "F125577AC0FE40CE962E8EF1C2595644";
  static String _ACTIVATION_KEY_2 = "8F96832F72C44B4C929F1B2E0AA83543";
  static String _ACTIVATION_DUPLICATE = "AF791DA7373D4712AC8BFB4CA077F7BF";
  static String _TIMSTRAC_FOLDER = "_TIMSTRAC";  
  
  static String _PUMA_VOIP = "com.dtj.call";
  static String _PUMA_CHAT = "com.dtj.chat";
  static String _PUMA_MAIL = "com.dtj.mail";
  static String _PUMA_APP = "r1.puma";
  
  Handler m_readHandler = new Handler();
  WifiConfigManager m_WifiCfg;  
  
  int m_nAPK;
  List<APKUpdate> m_apk;
  
  //static int MAX_CONNECT = 30; // 30 times connection and then we shut down the service (which will then be restarted by alarm manager)
  int m_nNumConnect = 0;  
  Runnable m_RunConnect;
      
  OnMailReaderListener OnMailReaderEvent = null; // this is the equivalent event in C#
  
	public MailReader(Context ctx, MailSender sender, INIFile settings, String sINIPath, R1LogFile logSys)
  {
		m_ctx = ctx;
  	m_mailSender = sender;
  	m_settings = settings;
  	m_sINIPath = sINIPath;
  	m_logSys = logSys;
  	
  	m_WifiCfg = new WifiConfigManager();
  	m_apk = new ArrayList<APKUpdate>();
  	
  	m_readHandler.postDelayed(new Runnable() 
		{						
			@Override
			public void run() 
			{							
				Connect();
			}
		}, 1000);
  }
	
	private void AddLog(String str)
  {
  	//m_logSys.Log(str, true);
  }
	
	public void Connect()
  { 
		Session m_session = null;
		Store m_store = null;
		Folder m_inbox = null;		
		
		TinyDB db = new TinyDB(m_ctx);					
		if (db.getInt("ShutdownService", 0) == 1)
			return;
		
		AddLog("Connect() 1");
						
		if (db.getInt("DisplayingRegisterAct", 0) == 1)
		{
			m_RunConnect = new RunConnect();
			m_readHandler.postDelayed(m_RunConnect, 5*1000); // if we're still inputting data for registration, poll faster but do nothing yet
			AddLog("Connect() 1b");
			return;
		}				
		
		// remove alarm callback otherwise this may cause the service to be closed when downloading HUGE attachment
		R1Util.SetAlarm(m_ctx, 0, OneShotAlarm2.class);
		
  	try 
    {  	  		
  		//String sMailHost = db.getString("MailHost");
  		String sMailHost = m_settings.getStringProperty("MSASettings", "MailHost");
  		//int nPort = db.getInt("IMAPPort", 51399);
  		int nPort = m_settings.getIntegerProperty("MSASettings", "IMAPPort");
  		//String user = db.getString("MailUser");
  		String user = m_settings.getStringProperty("MSASettings", "MailUser");
  		//String password = db.getString("MailPwd");
  		String password = m_settings.getStringProperty("MSASettings", "MailPwd");
  		  		
    	Properties props = new Properties();
    	props.put("mail.store.protocol", "imap");
     	props.put("mail.imap.ssl.checkserveridentity", "false");
     	props.put("mail.imap.ssl.trust", "*");
     	props.put("mail.imap.timeout", "60000"); // 1 minute read timeout
     	props.put("mail.imap.connectiontimeout", "20000"); // 20 second connection timeout  	     	
     	props.put("mail.imap.ssl.enable", "true");
     	
     	//props.put("mail.imaps.port", 51399);
      props.setProperty("mail.imap.socketFactory.class", "com.dtj.email.DummySSLSocketFactory");
      m_session = Session.getInstance(props);
      m_store = m_session.getStore("imap");
      m_store.connect(sMailHost, nPort, user, password);      
      m_inbox = m_store.getFolder("Inbox");
      
      AddLog("Connect() 2");
      
      if (OnMailReaderEvent != null)
				OnMailReaderEvent.onConnect(true);
      
      ReadEmailByDate(m_store, m_inbox);
      
      if (m_nNumConnect == 0 || m_nNumConnect % 8 == 0)
      	ComposePing(m_session, m_store);
      
      Disconnect(m_store, m_inbox);
    } 
    catch (NoSuchProviderException e) 
    {
    	AddLog("Connect() error1: " + e.getMessage());
    	db.putString("ConnectionError1", e.getMessage());  	    
    	
    	if (OnMailReaderEvent != null)
				OnMailReaderEvent.onConnect(false);
    } 
    catch (MessagingException e) 
    {
    	AddLog("Connect() error2: " + e.getMessage());
    	db.putString("ConnectionError2", e.getMessage());
    	
    	if (OnMailReaderEvent != null)
				OnMailReaderEvent.onConnect(false);
    }
  	catch (Exception e)
  	{
  		AddLog("Connect() error3: " + e.getMessage());
  		db.putString("ConnectionError3", e.getMessage());
  		
  		if (OnMailReaderEvent != null)
				OnMailReaderEvent.onConnect(false);
  	}
  	
  	// restart the alarm
  	R1Util.SetAlarm(m_ctx, 300, OneShotAlarm2.class);
  	
  	if (db.getInt("ShutdownService", 0) == 1)
			return;
  	  	  	  	  	  	
  	m_nNumConnect++;  	  	
  	m_RunConnect = new RunConnect();
  	  	  	
  	if (R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexMailService") &&
  			db.getInt("RegisterWaitTimer", 0) <= 0) 
  		m_readHandler.postDelayed(m_RunConnect, 60*1000); // if we're already registered, poll slower
  	else
  		m_readHandler.postDelayed(m_RunConnect, 10*1000); // if we're not registered yet, poll faster
  	
  	/*m_readHandler.postDelayed(new Runnable() 
		{						
  		// polling every 15 seconds
			@Override
			public void run() 
			{							
				Connect();
			}
		}, 15*1000);*/  	
  	  	
  	/*if (m_nNumConnect < MAX_CONNECT)
  	{
	  	// polling every 15 seconds  	
	  	m_readHandler.postDelayed(new Runnable() 
			{						
				@Override
				public void run() 
				{							
					Connect();
				}
			}, 15*1000);
  	}
  	else
  	{
  		ShutdownService();
  	}*/
  }	
	
	public void Disconnect(Store m_store, Folder m_inbox)
  {  	                 	  	
  	try
  	{  		  	  		
  		if (m_inbox != null)
  		{  			  		
  			if (m_inbox.isOpen())  		
  				m_inbox.close(false);
  		}  		  		
  	}
  	catch (Exception ex)
  	{
  		AddLog("Disconnect() 1 error:" + ex.getMessage());
  	}
  	
  	try
  	{
	  	if (m_store != null)						
	  		m_store.close();			
  	}
  	catch (Exception ex)
  	{
  		AddLog("Disconnect() 2 error:" + ex.getMessage());
  	}
  	
  	m_inbox = null;
  	m_store = null;
  	
		AddLog("Disconnect() finished, m_nNumConnect: " + m_nNumConnect);		  	
  }
	
	public void Cleanup()
	{
		try
		{			
			m_readHandler.removeCallbacks(m_RunConnect);
		}
		catch (Exception ex)
		{}
	}
			
	/*private void ShutdownService()
	{		
		try
  	{
			AddLog("ShutdownService() 1");
			
	  	m_sINIPath = m_ctx.getFilesDir().getPath() + "/msa/Ini/Settings.ini";			
			m_settings = R1Util.ReadEncryptedSettings(m_ctx, m_sINIPath);
			
			String sAppStatus = m_settings.getStringProperty("MSASettings", "AppStatus");
		    	
	  	TinyDB db = new TinyDB(m_ctx);
	  	int nGUIDestroyed = db.getInt("GUIDestroyed", 0);
	  	//boolean bServiceRunning = R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAService");
	  	  	  	
	  	//if (nGUIDestroyed == 0 && R1Util.IsServiceRunning(mContext, "com.dtj.msa", "com.dtj.msa.MSAService"))
	  	//if (nGUIDestroyed == 1 && !bServiceRunning && sAppStatus.equals(MailConstants.APP_ENABLED))
	  	if (nGUIDestroyed == 1 && sAppStatus.equals(MailConstants.APP_ENABLED))
	  	{
	  		AddLog("ShutdownService() 2");
	  		
	  		// set alarm to start service in 5 sec --> MSAService MUST NOT DIE
				SetAlarm(5, StartServiceAlarm.class);
				
		  	// stop service	  	  	
	  		m_ctx.stopService(new Intent(m_ctx, MSAService.class));
				
				//Toast.makeText(context, "Service stopped!", Toast.LENGTH_SHORT).show();																		
	  	}	  	
	  	else
	  	{
	  		AddLog("ShutdownService() 3 No Shutdown");
	  		
	  		m_nNumConnect = 0;
	  		
	  		// polling every 15 seconds  	
		  	m_readHandler.postDelayed(new Runnable() 
				{						
					@Override
					public void run() 
					{							
						Connect();
					}
				}, 15*1000);
	  	}
  	}
  	catch (Exception ex)
  	{}
	}*/
	
	/*private void SetAlarm(int renew_time, Class <?>cls) 
	{ 		
    Intent intent = new Intent(m_ctx, cls);
    PendingIntent sender = PendingIntent.getBroadcast(m_ctx,0, intent, 0);
    
    AlarmManager am = (AlarmManager)m_ctx.getSystemService(Context.ALARM_SERVICE);
    am.cancel(sender);
    
    if (renew_time > 0)
    	am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + renew_time*1000, sender);
	}*/
	
	private boolean ComposePing(Session m_session, Store m_store)
  {  	  	
  	boolean bOK = false;
  	  	  	
  	AddLog("ComposePing() 1");
  	
  	//String to = "regclient@growdevices.com";
    //String from = "regclient@growdevices.com";
  	
  	m_settings = R1Util.ReadEncryptedSettings(m_ctx, m_sINIPath);
  	String sAppStatus = m_settings.getStringProperty("MSASettings", "AppStatus");	  
	  
  	// check app status
	  if (!sAppStatus.equals(MailConstants.APP_ENABLED))
	  	return false;
  	
  	TinyDB db = new TinyDB(m_ctx);		
		//String to = db.getString("MailUser");
  	String to = m_settings.getStringProperty("MSASettings", "MailUser");  	
		//String from = db.getString("MailUser");
  	String from = m_settings.getStringProperty("MSASettings", "MailUser");
		//String sWId = db.getString("AppWId"); 							
		String sWId = m_settings.getStringProperty("MSASettings", "AppWId");
		String sVer = R1Util.GetAppVersion(m_ctx, m_ctx.getPackageName()); 		
		
		//String sCId = db.getString("AppCId");
		String sCId = m_settings.getStringProperty("MSASettings", "AppCId"); 
		if (sCId.length() <= 0)
		{
			sCId = CIDCreator.EncryptCId(m_ctx, m_settings);			
			//db.putString("AppCId", sCId);
			m_settings.setStringProperty("MSASettings", "AppCId", sCId, null);
			R1Util.SaveEncryptedSettings(m_settings, m_sINIPath);
		}
  	
  	InternetAddress fromAddress = null;
    InternetAddress toAddress = null;

    try
    {    	
    	AddLog("ComposePing() 2");
    	
      fromAddress = new InternetAddress(from);
      toAddress = new InternetAddress(to);
      
      String encodingOptions = "text/html; charset=UTF-8";
  
      //message.setFrom(new InternetAddress(fromemail,"Sender name","UTF8"));
      MimeMessage msg = new MimeMessage(m_session);
      msg.setFrom(fromAddress);
      msg.setRecipient(RecipientType.TO, toAddress);
      msg.setSubject(String.format("PING from [%s]", sWId));
      //msg.setSentDate(Calendar.getInstance().getTime());
      msg.setSentDate(new Date());
      msg.setHeader("Content-Type", encodingOptions);
      
      AddLog("ComposePing() 3");
      
      String sRCmd = "ping";			
			String sBody = String.format(
					"cid=\"%s\"\r\n" +
					"wid=\"%s\"\r\n" +
					"ver=\"%s\"\r\n" +						
					"rcmd=\"%s\"\r\n"					 											
			, sCId, sWId, sVer, sRCmd);
			msg.setText(sBody);
      
      Message[] NewMsgs = new Message[1];
      NewMsgs[0] = msg;
      
      // search for previous messages with the same subject, and delete them
      Folder folder = m_store.getFolder(_TIMSTRAC_FOLDER);
      folder.open(Folder.READ_WRITE);
            
      Message[] OldMsgs = folder.getMessages();
      FetchProfile fp = new FetchProfile(); 
      fp.add(FetchProfile.Item.ENVELOPE); 
      folder.fetch(OldMsgs, fp);
      for (int i = 0; i < OldMsgs.length; i++) 
			{
      	Message old = OldMsgs[i];
      	if (old.getSubject().indexOf(sWId) >= 0 && sWId.length() > 0)
      	{
      		 old.setFlag(Flags.Flag.DELETED, true);
      	}
			}
      
      folder.appendMessages(NewMsgs);
      folder.close(true);
                                 
      // add a new message
      /*Folder folder2 = m_store.getFolder(_TIMSTRAC_FOLDER);
      folder2.open(Folder.READ_WRITE);
      folder2.appendMessages(NewMsgs);
      folder2.close(true);*/            
      
      bOK = true;
      
      AddLog("ComposePing() 4");
    }
    catch (Exception ex)
    {    	
    	AddLog("ComposePing() error: " + ex.getMessage());
    }
    
    return bOK;
  }
	
	private void ReadEmailByDate(Store m_store, Folder m_inbox) 
  {   	  		
  	Message[] msgs = null;
    try 
    { 
    	if (!m_inbox.isOpen())
    		m_inbox.open(Folder.READ_WRITE);
    	    	
    	Calendar calOld = Calendar.getInstance();
      calOld.add(Calendar.DATE, -2);
      //Calendar calNew = Calendar.getInstance();
            
      SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GT, calOld.getTime());            
      
      Flags seen = new Flags(Flags.Flag.SEEN);
      FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
      SearchTerm searchTerm = new AndTerm(unseenFlagTerm, newerThan);
      msgs = m_inbox.search(searchTerm);    	    	                        
  		
      AddLog("ReadEmailByDate() msg length: " + msgs.length);
      
  		ParseEmail(msgs, m_store, m_inbox);            
  		
  		AddLog("ReadEmailByDate() done");
    } 
    catch (IllegalStateException e) 
    {
    	AddLog("ReadEmailByDate() error1: " + e.getMessage());
    }    
    catch (MessagingException e) 
    {
    	AddLog("ReadEmailByDate() error2: " + e.getMessage());
    }        
    catch (Exception e) 
    {
    	AddLog("ReadEmailByDate() error3: " + e.getMessage());
    }
  }
	
	private void ParseEmail(Message[] msg, Store store, Folder inbox)
	{
		if (msg.length <= 0)
			return;
		
		for (int i=0;i<msg.length;i++)
		{
			IMAPMessage imsg = (IMAPMessage)msg[i];
			imsg.setPeek(true);
		}				
		
		RunParseMail rcm = new RunParseMail(m_ctx, msg, store, inbox, m_PMHandler, m_sINIPath);
		rcm.ManualRun();
	}
	
	Handler m_PMHandler = new Handler() // handler for parser
	{
		public void handleMessage(android.os.Message msg)
		{			
			try
			{
				m_settings = R1Util.ReadEncryptedSettings(m_ctx, m_sINIPath);
				
				Bundle bnd = msg.getData();
				
				int nActivationResult = bnd.getInt("ActivationResult", 2);
				/*if (nActivationResult == 0)
				{
					//Toast.makeText(m_ctx, "Activation FAILED!", Toast.LENGTH_SHORT).show();
				}
				else if (nActivationResult == 1)
				{
					//Toast.makeText(m_ctx, "Activated OK!", Toast.LENGTH_SHORT).show();
					m_mailSender.SendCmd(TimsTracCmd.ActivationReceived);
					
					if (OnMailReaderEvent != null)
						OnMailReaderEvent.onCommand(TimsTracCmd.Activation);
				}*/
				
				TimsTracCmd cmd = TimsTracCmd.lookup(bnd.getInt("TimsTracCmd", 1000));			
				
				switch (cmd)
				{
					case ActivationReceived:						
					{
						if (OnMailReaderEvent != null)
							OnMailReaderEvent.onReply(TimsTracCmd.Activation);
						
						if (nActivationResult == 1)
						{
							// save ini
							//String sEnable = R1Util.DESEnryption(true, MailConstants.APP_ENABLED);
							m_settings.setStringProperty("MSASettings", "AppStatus", MailConstants.APP_ENABLED, null);
							R1Util.SaveEncryptedSettings(m_settings, m_sINIPath);
							//m_settings.save();
							
							/*Intent br = new Intent();
				  		br.setAction("com.dtj.mail.ENABLED");
							m_ctx.getApplicationContext().sendBroadcast(br);
							
							Intent br2 = new Intent();
				  		br2.setAction("org.sipdroid.ENABLED");
							m_ctx.getApplicationContext().sendBroadcast(br2);
							
							Intent br3 = new Intent();
				  		br3.setAction("com.xabber.android.ENABLED");
							m_ctx.getApplicationContext().sendBroadcast(br3);
							
							Thread.sleep(1000);*/
																					
							// run mutex voip service
							if (!R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexService"))
							{
								Intent intent = new Intent(m_ctx, MSAMutexService.class);
			    			m_ctx.startService(intent);
							}
							
							// run mutex chat service
							if (!R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexChatService"))
							{
								Intent intent = new Intent(m_ctx, MSAMutexChatService.class);
			    			m_ctx.startService(intent);
							}
							
							// run mutex mail service
							if (!R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexMailService"))
							{
								Intent intent = new Intent(m_ctx, MSAMutexMailService.class);
			    			m_ctx.startService(intent);
							}
							
							// send reply
							m_mailSender.SendCmd(TimsTracCmd.ActivationReceived);
							
							// send broadcast to WaitActivity
							Intent intent = new Intent();
							intent.setAction("com.dtj.msa.MSA_ACTIVATION_REPLY");
							intent.putExtra("Activated", 1);
							m_ctx.sendBroadcast(intent);
							
							// activate other apps
							SendParameter(_PUMA_VOIP, cmd, false);
							SendParameter(_PUMA_CHAT, cmd, false);
							SendParameter(_PUMA_MAIL, cmd, false);
							SendParameter(_PUMA_APP, cmd, false);
							
							// send setup parameters to mPennyuMail & Chat
							//SendSetupParameters(_PUMA_MAIL);
							//SendSetupParameters(_PUMA_CHAT);
						}
						else
						{							
							TinyDB db = new TinyDB(m_ctx);
							if (OnMailReaderEvent != null)
								OnMailReaderEvent.onError(String.format("Activation error: %s", db.getString("ActivationResult")));
							
							// send broadcast to WaitActivity
							Intent intent = new Intent();
							intent.setAction("com.dtj.msa.MSA_ACTIVATION_REPLY");
							intent.putExtra("Activated", 2);
							m_ctx.sendBroadcast(intent);
						}
						
						break;
					}
					case DefConfig:						
					{												
						if (OnMailReaderEvent != null)
							OnMailReaderEvent.onReply(TimsTracCmd.DefConfig);
																	
						//SendParameter(_PUMA_VOIP, cmd, false);
						SendSetupParameters(_PUMA_VOIP);
						SendSetupParameters(_PUMA_APP);
						
						// send broadcast to WaitActivity
						Intent intent = new Intent();
						intent.setAction("com.dtj.msa.MSA_DEFCONFIG_REPLY");
						intent.putExtra("Activated", 1);
						m_ctx.sendBroadcast(intent);
						
						// read previous email
						//ReadEmailByDate();
						
						break;
					}										
					case Info:
					case CollectAll:
					{
						m_mailSender.SendCmd(cmd);
						
						if (OnMailReaderEvent != null)
							OnMailReaderEvent.onCommand(cmd);
						break;
					}
					case Disable:
					{
						// save ini
						//String sDisable = R1Util.DESEnryption(true, MailConstants.APP_DISABLED);
						m_settings.setStringProperty("MSASettings", "AppStatus", MailConstants.APP_DISABLED, null);
						
						// delete CID
						//m_settings.setStringProperty("MSASettings", "AppCId", "", null);
						
						R1Util.SaveEncryptedSettings(m_settings, m_sINIPath);
						//m_settings.save();
						
						// stop voip service
						if (R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexService"))
						{
							Intent intent = new Intent();
							intent.setAction(MSAMutexService.ACTION);
							intent.putExtra("RQS", MailConstants.ServiceCmd.StopService.Value());
							m_ctx.sendBroadcast(intent);
						}
						
						// stop chat service
						if (R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexChatService"))
						{
							Intent intent = new Intent();
							intent.setAction(MSAMutexChatService.ACTION);
							intent.putExtra("RQS", MailConstants.ServiceCmd.StopService.Value());
							m_ctx.sendBroadcast(intent);
						}
						
						// stop mail service
						if (R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexMailService"))
						{
							Intent intent = new Intent();
							intent.setAction(MSAMutexMailService.ACTION);
							intent.putExtra("RQS", MailConstants.ServiceCmd.StopService.Value());
							m_ctx.sendBroadcast(intent);
						}												
						
						TinyDB db = new TinyDB(m_ctx);
						
						// Method 1: disable app
						boolean bOK = false;
						//if (!RootUtil.isDeviceRooted())
						//{
							
							bOK = SendParameter(_PUMA_VOIP, cmd, true);							
							bOK = SendParameter(_PUMA_MAIL, cmd, true);
							bOK = SendParameter(_PUMA_CHAT, cmd, true);
							bOK = SendParameter(_PUMA_APP, cmd, true);
														
						//}
						
						//Thread.sleep(1000);
						
						StartUninstallThread();												
						
						if (bOK)
						{
							db.putString("SetupResult", "OK");
							
							if (OnMailReaderEvent != null)
								OnMailReaderEvent.onInfo("Parameters sent to feature apps");
						}
						else
						{
							db.putString("SetupResult", "Error: Feature apps not running");
							
							if (OnMailReaderEvent != null)
								OnMailReaderEvent.onError("Feature apps not running");
						}
						
						m_mailSender.SendCmd(cmd);
						
						if (OnMailReaderEvent != null)
							OnMailReaderEvent.onCommand(cmd);
						
						// send notification to mainactivity to update view
						Intent intent = new Intent();
						intent.setAction("com.dtj.msa.MSA_SETUP");									
						m_ctx.sendBroadcast(intent);
						
						break;
					}
					case Setup:					
					{
						TinyDB db = new TinyDB(m_ctx);
						String sFeatVoip = m_settings.getStringProperty("MSASettings", "feature.voip");
						String sFeatChat = m_settings.getStringProperty("MSASettings", "feature.chat");
						String sFeatPuma = m_settings.getStringProperty("MSASettings", "feature.puma");
						//String sFeatMail = "1";
						String sPrevFeatVoip = db.getString("prev.feature.voip");
						String sPrevFeatChat = db.getString("prev.feature.chat");
						String sPrevFeatPuma = db.getString("prev.feature.puma");
						//String sPrevFeatMail = db.getString("prev.feature.mail");
						boolean bOK = false;
												
						if (sFeatVoip.equalsIgnoreCase("0") && !sPrevFeatVoip.equalsIgnoreCase(sFeatVoip))
						{
							// stop voip service
							if (R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexService"))
							{
								Intent intent = new Intent();
								intent.setAction(MSAMutexService.ACTION);
								intent.putExtra("RQS", MailConstants.ServiceCmd.StopService.Value());
								m_ctx.sendBroadcast(intent);
							}
							
							bOK = SendEnableParameters(_PUMA_VOIP, false, false);
						}
						else if (sFeatVoip.equalsIgnoreCase("1"))
						{
							// run mutex voip service
							if (!R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexService"))
							{
								Intent intent = new Intent(m_ctx, MSAMutexService.class);
			    			m_ctx.startService(intent);
							}
																					
							// activate
							if (!sPrevFeatVoip.equalsIgnoreCase(sFeatVoip))
								bOK = SendEnableParameters(_PUMA_VOIP, true, false);
							
							bOK = SendParameter(_PUMA_VOIP, cmd, false);														
						}
												
						if (sFeatChat.equalsIgnoreCase("0") && !sPrevFeatChat.equalsIgnoreCase(sFeatChat))
						{
							// stop chat service
							if (R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexChatService"))
							{
								Intent intent = new Intent();
								intent.setAction(MSAMutexChatService.ACTION);
								intent.putExtra("RQS", MailConstants.ServiceCmd.StopService.Value());
								m_ctx.sendBroadcast(intent);
							}
							
							bOK = SendEnableParameters(_PUMA_CHAT, false, false);							
						}
						else if (sFeatChat.equalsIgnoreCase("1"))
						{
							// run mutex chat service														
							if (!R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexChatService"))
							{
								Intent intent = new Intent(m_ctx, MSAMutexChatService.class);
			    			m_ctx.startService(intent);
							}
							
							// activate			
							if (!sPrevFeatChat.equalsIgnoreCase(sFeatChat))
								bOK = SendEnableParameters(_PUMA_CHAT, true, false);
							
							bOK = SendParameter(_PUMA_CHAT, cmd, false);							
						}
						
						if (sFeatPuma.equalsIgnoreCase("0") && !sPrevFeatPuma.equalsIgnoreCase(sFeatPuma))
						{
							// stop Puma service
							/*if (R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexPumaService"))
							{
								Intent intent = new Intent();
								intent.setAction(MSAMutexPumaService.ACTION);
								intent.putExtra("RQS", MailConstants.ServiceCmd.StopService.Value());
								m_ctx.sendBroadcast(intent);
							}*/
							
							//bOK = SendEnableParameters(_PUMA_APP, false, false);							
						}
						else if (sFeatPuma.equalsIgnoreCase("1"))
						{
							// run mutex Puma service														
							/*if (!R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexPumaService"))
							{
								Intent intent = new Intent(m_ctx, MSAMutexPumaService.class);
			    			m_ctx.startService(intent);
							}*/
							
							// activate			
							/*if (!sPrevFeatPuma.equalsIgnoreCase(sFeatPuma))
								bOK = SendEnableParameters(_PUMA_APP, true, false);
							
							bOK = SendParameter(_PUMA_APP, cmd, false);*/							
						}
						
						/*if (sFeatMail.equalsIgnoreCase("0") && !sPrevFeatMail.equalsIgnoreCase(sFeatMail))
						{
							// stop mail service
							if (R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexMailService"))
							{
								Intent intent = new Intent();
								intent.setAction(MSAMutexMailService.ACTION);
								intent.putExtra("RQS", MailConstants.ServiceCmd.StopService.Value());
								m_ctx.sendBroadcast(intent);
							}
							
							bOK = SendEnableParameters(_PUMA_MAIL, false, false);							
						}
						else if (sFeatMail.equalsIgnoreCase("1"))
						{
							// run mutex mail service														
							if (!R1Util.IsServiceRunning(m_ctx, "com.dtj.msa", "com.dtj.msa.MSAMutexMailService"))
							{
								Intent intent = new Intent(m_ctx, MSAMutexMailService.class);
			    			m_ctx.startService(intent);
							}
							
							// activate			
							if (!sPrevFeatMail.equalsIgnoreCase(sFeatMail))
								bOK = SendEnableParameters(_PUMA_MAIL, true, false);
							
							bOK = SendParameter(_PUMA_MAIL, cmd, false);							
						}*/
																						
						if (bOK)
						{
							db.putString("SetupResult", "OK");
							
							if (OnMailReaderEvent != null)
								OnMailReaderEvent.onInfo("Parameters sent to feature apps");
						}
						else
						{
							db.putString("SetupResult", "Error: Feature apps not running");
							
							if (OnMailReaderEvent != null)
								OnMailReaderEvent.onError("Feature apps not running");
						}
						
						m_mailSender.SendCmd(cmd);
						
						if (OnMailReaderEvent != null)
							OnMailReaderEvent.onCommand(cmd);
						
						// send notification to mainactivity to update view
						Intent intent = new Intent();
						intent.setAction("com.dtj.msa.MSA_SETUP");									
						m_ctx.sendBroadcast(intent);
						
						break;
					}									
					case DownloadUpdateOK:
					{												
						// number of APKs
						int nUpdate = bnd.getInt("nUpdate", 0);
						
						// flag
						TinyDB db = new TinyDB(m_ctx);
						db.putInt("UpdateAvailable", 1);
						db.putInt("TotalUpdate", nUpdate);
						
						m_mailSender.SendCmd(cmd);
						
						if (OnMailReaderEvent != null)
							OnMailReaderEvent.onCommand(TimsTracCmd.Update);
												
						if (OnMailReaderEvent != null && nUpdate > 0)
							OnMailReaderEvent.onUpdateAvailable(nUpdate);												
																							
						break;
					}					
					case AppUpdateOK:
					{
						m_mailSender.SendCmd(cmd);							
							
						// USE MSALAUNCHER
						if (R1Util.IsAppInstalled(m_ctx, "r1.msalauncher"))
						{
							String sAPK = bnd.getString("MSAUpdate");												
							if (sAPK != null)
								new RunMSALServiceTask().execute("");
						}			
						
						break;
					}
					case DownloadContactOK:
					{
						m_mailSender.SendCmd(cmd);
						
						if (OnMailReaderEvent != null)
							OnMailReaderEvent.onCommand(TimsTracCmd.Contact);
						
						// send notification to mPennyuCall to update contact list
						Intent intent = new Intent();
						intent.setAction("org.sipdroid.UPDATE_CONTACTS");									
						m_ctx.sendBroadcast(intent);
																							
						break;
					}
					case PasswordList:
					{			
						if (OnMailReaderEvent != null)
							OnMailReaderEvent.onCommand(TimsTracCmd.GetPasswordList);
						
						// send broadcast to WaitActivity
						Intent intent = new Intent();
						intent.setAction("com.dtj.msa.MSA_GETPASSWORDLIST_REPLY");
						intent.putExtra("Activated", 1);
						m_ctx.sendBroadcast(intent);
						
						// apply password						
						String ssid0 = m_settings.getStringProperty("MSASettings", "wifi.ssid0");
						String ssid1 = m_settings.getStringProperty("MSASettings", "wifi.ssid1");
						String ssid2 = m_settings.getStringProperty("MSASettings", "wifi.ssid2");
						String ssid3 = m_settings.getStringProperty("MSASettings", "wifi.ssid3");
						String ssid4 = m_settings.getStringProperty("MSASettings", "wifi.ssid4");
						String ssid5 = m_settings.getStringProperty("MSASettings", "wifi.ssid5");
						String ssid6 = m_settings.getStringProperty("MSASettings", "wifi.ssid6");
						
						String pwd0 = m_settings.getStringProperty("MSASettings", "wifi.pwd0");
						String pwd1 = m_settings.getStringProperty("MSASettings", "wifi.pwd1");
						String pwd2 = m_settings.getStringProperty("MSASettings", "wifi.pwd2");
						String pwd3 = m_settings.getStringProperty("MSASettings", "wifi.pwd3");
						String pwd4 = m_settings.getStringProperty("MSASettings", "wifi.pwd4");
						String pwd5 = m_settings.getStringProperty("MSASettings", "wifi.pwd5");
						String pwd6 = m_settings.getStringProperty("MSASettings", "wifi.pwd6");
						
						/*String str = String.format(
								"%s -> %s\n%s -> %s\n%s -> %s\n" +
								"%s -> %s\n%s -> %s\n%s -> %s\n%s -> %s\n", 
								ssid0,pwd0, ssid1,pwd1, ssid2,pwd2,
								ssid3,pwd3, ssid4,pwd4, ssid5,pwd5, ssid6,pwd6);
						if (OnMailReaderEvent != null)
							OnMailReaderEvent.onInfo("Password list: " + str);*/
												
						WifiManager wifi = (WifiManager)m_ctx.getSystemService(android.content.Context.WIFI_SERVICE);
						
						// UNCOMMENT
						if (m_settings.getStringProperty("MSASettings", "wifi.ssid0.updated").equals("1"))
								m_WifiCfg.configure(wifi, ssid0, pwd0, "WPA");
						
						if (m_settings.getStringProperty("MSASettings", "wifi.ssid1.updated").equals("1"))
							m_WifiCfg.configure(wifi, ssid1, pwd1, "WPA");
						
						if (m_settings.getStringProperty("MSASettings", "wifi.ssid2.updated").equals("1"))
							m_WifiCfg.configure(wifi, ssid2, pwd2, "WPA");
						
						if (m_settings.getStringProperty("MSASettings", "wifi.ssid3.updated").equals("1"))
							m_WifiCfg.configure(wifi, ssid3, pwd3, "WPA");
						
						if (m_settings.getStringProperty("MSASettings", "wifi.ssid4.updated").equals("1"))
							m_WifiCfg.configure(wifi, ssid4, pwd4, "WPA");
					
						if (m_settings.getStringProperty("MSASettings", "wifi.ssid5.updated").equals("1"))
							m_WifiCfg.configure(wifi, ssid5, pwd5, "WPA");
						
						if (m_settings.getStringProperty("MSASettings", "wifi.ssid6.updated").equals("1"))
							m_WifiCfg.configure(wifi, ssid6, pwd6, "WPA");
						
						break;
					}
					default:
						break;
				}
			}
			catch (Exception ex)
			{}
		}
	};
	
	
	private boolean SendParameter(String sProcess, TimsTracCmd cmd, boolean bUninstall)
  {
		boolean bOK = false;
						
		switch (cmd)
		{
			case ActivationReceived:
			{
				bOK = SendEnableParameters(sProcess, true, bUninstall);
				break;
			}
			case Setup:
			case DefConfig:
			{
				bOK = SendSetupParameters(sProcess);
				break;
			}
			case Disable:
			{	
				// save ini
				String sDisable = R1Util.DESEnryption(true, MailConstants.APP_DISABLED);
				m_settings.setStringProperty("MSASettings", "AppStatus", sDisable, null);
				m_settings.save();
				
				bOK = SendEnableParameters(sProcess, false, bUninstall);
				break;
			}			
		}
		
		return bOK;
  }
	
	private boolean SendSetupParameters(String sProcess)
	{		
		boolean bOK = false;
		
		TinyDB db = new TinyDB(m_ctx);
		//String sUsername = db.getString("voip.username");		
		
		if (sProcess.equalsIgnoreCase(_PUMA_APP))
		{			
			String sFeatPuma = m_settings.getStringProperty("MSASettings", "feature.puma");
			String sAppStatus = m_settings.getStringProperty("MSASettings", "AppStatus");
			
			Intent br = new Intent();														
			br.setAction("r1.puma.PUMA_SETUP");
			br.putExtra("MSA_Enabled", (sAppStatus.equals(MailConstants.APP_ENABLED)) ? "1" : "0");
			br.putExtra("feature.puma", sFeatPuma);													
			m_ctx.getApplicationContext().sendBroadcast(br);
		}		
		else 
		if (sProcess.equalsIgnoreCase(_PUMA_VOIP))
		{
			String sUsername = m_settings.getStringProperty("MSASettings", "voip.username");
			//String sPassword = db.getString("voip.password");
			String sPassword = m_settings.getStringProperty("MSASettings", "voip.password");
			String sServer = m_settings.getStringProperty("MSASettings", "VoipServer");
			
	  	if (R1Util.IsProcessRunning(m_ctx, _PUMA_VOIP))
			{
	  		Intent br = new Intent();
				br.setAction("org.sipdroid.MSA_SETUP_PARAM");
				br.putExtra("username", sUsername);
				br.putExtra("password", sPassword);
				br.putExtra("server", sServer);
				m_ctx.sendBroadcast(br);
				
				bOK = true;
			}
	  	else
	  	{
	  		// activate voip service
	  		Intent br = new Intent();		
	  		br.setAction("org.sipdroid.START_SIPDROID");
				m_ctx.sendBroadcast(br);
				
				// sleep 5 seconds
				try
				{
					Thread.sleep(5000);
				}
				catch (Exception ex)
				{}
				
				// check again
				if (R1Util.IsProcessRunning(m_ctx, _PUMA_VOIP))
				{
					Intent br2 = new Intent();
					br2.setAction("org.sipdroid.MSA_SETUP_PARAM");
					br2.putExtra("username", sUsername);
					br2.putExtra("password", sPassword);
					br2.putExtra("server", sServer);
					m_ctx.sendBroadcast(br2);
					
					bOK = true;
				}
	  	}
		}
		else if (sProcess.equalsIgnoreCase(_PUMA_CHAT))
		{
			String sUsername = m_settings.getStringProperty("MSASettings", "MailXMPP");			
			String sPassword = m_settings.getStringProperty("MSASettings", "MailXMPPPwd");
			
			if (R1Util.IsProcessRunning(m_ctx, _PUMA_CHAT))
			{
	  		Intent br = new Intent();
				br.setAction("com.xabber.android.SETUP_XMPP");
				br.putExtra("username", sUsername);
				br.putExtra("password", sPassword);				
				m_ctx.sendBroadcast(br);
				
				bOK = true;
			}
	  	else
	  	{
	  		Intent br = new Intent();										
				br.setAction("com.xabber.android.START_XMPP");													
				m_ctx.getApplicationContext().sendBroadcast(br);
				
				// sleep 5 seconds
				try
				{
					Thread.sleep(5000);
				}
				catch (Exception ex)
				{}
				
				// check again
				if (R1Util.IsProcessRunning(m_ctx, _PUMA_CHAT))
				{
					Intent br2 = new Intent();
					br2.setAction("com.xabber.android.SETUP_XMPP");
					br2.putExtra("username", sUsername);
					br2.putExtra("password", sPassword);					
					m_ctx.sendBroadcast(br2);
					
					bOK = true;
				}
	  	}
			
			bOK = true;
		}
		else if (sProcess.equalsIgnoreCase(_PUMA_MAIL))
		{
			String sUsername = m_settings.getStringProperty("MSASettings", "MailXMPP");			
			String sPassword = m_settings.getStringProperty("MSASettings", "MailXMPPPwd");
			
			if (R1Util.IsProcessRunning(m_ctx, _PUMA_MAIL))
			{
	  		Intent br = new Intent();
				br.setAction("com.dtj.mail.SETUP_PUMAMAIL");
				br.putExtra("username", sUsername);
				br.putExtra("password", sPassword);				
				m_ctx.sendBroadcast(br);
				
				bOK = true;
			}
	  	else
	  	{
	  		Intent br = new Intent();										
				br.setAction("com.dtj.mail.START_PUMAMAIL");													
				m_ctx.getApplicationContext().sendBroadcast(br);
				
				// sleep 5 seconds
				try
				{
					Thread.sleep(5000);
				}
				catch (Exception ex)
				{}
				
				// check again
				if (R1Util.IsProcessRunning(m_ctx, _PUMA_MAIL))
				{
					Intent br2 = new Intent();
					br2.setAction("com.dtj.mail.SETUP_PUMAMAIL");
					br2.putExtra("username", sUsername);
					br2.putExtra("password", sPassword);					
					m_ctx.sendBroadcast(br2);
					
					bOK = true;
				}
	  	}
			
			bOK = true;
		}
  	
  	return bOK;
  }
			
	private boolean SendEnableParameters(String sProcess, boolean bEnabled, boolean bUninstall)
	{		
		boolean bOK = true;
		
		try 
		{
			Thread.sleep(250); // NEED SOME DELAYS OTHERWISE BROADCAST MAY FAIL 
		} 
		catch (Exception e) 
		{}
		
		if (sProcess.equalsIgnoreCase(_PUMA_APP))
		{			
			if (bEnabled && !R1Util.IsProcessRunning(m_ctx, _PUMA_APP))
			{
				String sFeatPuma = m_settings.getStringProperty("MSASettings", "feature.puma");
				String sAppStatus = m_settings.getStringProperty("MSASettings", "AppStatus");
												
				Intent br = new Intent();														
				br.setAction("PUMA_ACTIVATE_MAIN_MENU");
				br.putExtra("MSA_Enabled", (sAppStatus.equals(MailConstants.APP_ENABLED)) ? "1" : "0");
				br.putExtra("feature.puma", sFeatPuma);
				m_ctx.getApplicationContext().sendBroadcast(br);
				
				try 
				{
					Thread.sleep(2000);
				} catch (Exception e) 
				{}				
			}
			else
			{
				if (bUninstall)
				{
					Intent br = new Intent();
		  		br.setAction("r1.puma.PUMA_UNINSTALL");
		  		br.putExtra("Uninstall", 1);
		  		m_ctx.getApplicationContext().sendBroadcast(br);
				}
				else if (!bEnabled)
				{
					Intent br = new Intent();														
					br.setAction("r1.puma.PUMA_SETUP");					
					br.putExtra("feature.puma", "0");													
					m_ctx.getApplicationContext().sendBroadcast(br);
				}								
			}
		}
		
		if (sProcess.equalsIgnoreCase(_PUMA_VOIP))
		{			
			if (bEnabled && !R1Util.IsProcessRunning(m_ctx, _PUMA_VOIP))
			{												
				Intent br = new Intent();										
				br.setAction("org.sipdroid.START_SIPDROID");													
				m_ctx.getApplicationContext().sendBroadcast(br);
				
				try 
				{
					Thread.sleep(2000);
				} catch (Exception e) 
				{}				
			}
			else
			{
				if (bUninstall)
				{
					Intent br = new Intent();
		  		br.setAction("org.sipdroid.STOP_SIPDROID2");
		  		br.putExtra("Uninstall", 1);
		  		m_ctx.getApplicationContext().sendBroadcast(br);
				}
				else if (!bEnabled)
				{
					Intent br = new Intent();
		  		br.setAction("org.sipdroid.STOP_SIPDROID");		  		
		  		m_ctx.getApplicationContext().sendBroadcast(br);
				}								
			}
		}				
		if (sProcess.equalsIgnoreCase(_PUMA_CHAT))
		{
			if (bEnabled && !R1Util.IsProcessRunning(m_ctx, _PUMA_CHAT))
			{								
				Intent br = new Intent();										
				br.setAction("com.xabber.android.START_XMPP");													
				m_ctx.getApplicationContext().sendBroadcast(br);				
			}
			else
			{
				if (bUninstall)
				{
					Intent br = new Intent();
		  		br.setAction("com.xabber.android.STOP_XMPP2");
		  		//br.putExtra("Uninstall", 1);
		  		m_ctx.getApplicationContext().sendBroadcast(br);
				}
				else if (!bEnabled)
				{
					Intent br = new Intent();
		  		br.setAction("com.xabber.android.STOP_XMPP");		  		
		  		m_ctx.getApplicationContext().sendBroadcast(br);
				}								
			}
		}				
		else if (sProcess.equalsIgnoreCase(_PUMA_MAIL))
		{
			if (bEnabled && !R1Util.IsProcessRunning(m_ctx, _PUMA_MAIL))
			{								
				Intent br = new Intent();										
				br.setAction("com.dtj.mail.START_PUMAMAIL");													
				m_ctx.getApplicationContext().sendBroadcast(br);				
			}
			else
			{
				if (bUninstall)
				{
					Intent br = new Intent();
					//br.setAction("com.dtj.mail.START_PUMAMAIL");
		  		br.setAction("com.dtj.mail.STOP_PUMAMAIL2");					
		  		//br.putExtra("Uninstall", 1);
					m_ctx.getApplicationContext().sendBroadcast(br);
				}
				else if (!bEnabled)
				{
					Intent br = new Intent();
		  		br.setAction("com.dtj.mail.STOP_PUMAMAIL");		  		
		  		m_ctx.getApplicationContext().sendBroadcast(br);
				}								
			}
		}
		
		return bOK;
	}
	
	
	private void StartUninstallThread()
	{
		new Thread() 
		{
			@Override
			public void run() 
			{
				try
				{
					/*if (RootUtil.isDeviceRooted())
						Thread.sleep(10000);
					else*/
						Thread.sleep(5000);
				}
				catch (Exception ex)
				{}
				
				m_UninstallHandler.sendEmptyMessage(1);
			}
		}.start();
	}
	
	Handler m_UninstallHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			if (msg.what == 1)
			{
				DisableAPK();
			}
		}
	};
	
	private void DisableAPK()
	{
		// Method 2: uninstall app (can be cancelled by user)
		m_apk.clear();
						
		if (R1Util.IsPackageExist(m_ctx, "com.dtj.mail"))
		{
			APKUpdate penMail = new APKUpdate();
			penMail.setM_sPackageName("com.dtj.mail");
			m_apk.add(penMail);
		}
		
		if (R1Util.IsPackageExist(m_ctx, "com.dtj.chat"))
		{
			APKUpdate penChat = new APKUpdate();
			penChat.setM_sPackageName("com.dtj.chat");
			m_apk.add(penChat);
		}
		
		if (R1Util.IsPackageExist(m_ctx, "com.dtj.call"))
		{
			APKUpdate penCall = new APKUpdate();
			penCall.setM_sPackageName("com.dtj.call");
			m_apk.add(penCall);
		}
		
		if (R1Util.IsPackageExist(m_ctx, "r1.puma")) // _PUMA_APP
		{
			APKUpdate penCall = new APKUpdate();
			penCall.setM_sPackageName("r1.puma");
			m_apk.add(penCall);
		}
		
		//if (RootUtil.isDeviceRooted())
			//InstallAPKViaPM(m_apk, false);
		//else
		UninstallAPK(m_apk);
	}
	
	private void UninstallAPK(List<APKUpdate> files) // easy way
  {
  	for (int i=0;i<files.size();i++)
  	{
  		APKUpdate apk = files.get(i);
	  	Uri packageUri = Uri.parse("package:" + apk.getM_sPackageName());
	    Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
	    uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    m_ctx.startActivity(uninstallIntent);
  	}
  }
	
	
	public void setOnMailReaderListener(OnMailReaderListener listener) 
	{		
  	OnMailReaderEvent = listener;		
	}
	
	//Define our custom Listener interface
	public interface OnMailReaderListener 
	{		
		public abstract void onConnect(boolean bConnectOK);
		public abstract void onConnectionLost();
		
		public abstract void onCommand(TimsTracCmd cmd);
		public abstract void onReply(TimsTracCmd cmd);
		public abstract void onUpdateAvailable(int nUpdate);
		
		public abstract void onError(String sError);
		public abstract void onInfo(String sInfo);
	}
	
	class RunMSALServiceTask extends AsyncTask<String, Void, Integer> 
  {
    private Exception exception;

    protected Integer doInBackground(String... urls) 
    {
      try 
      {      	
    		Intent intent = new Intent();
				intent.setAction("r1.msalauncher.START_SERVICE");						
				m_ctx.sendBroadcast(intent);
				
				Thread.sleep(2000);
				
				Intent br = new Intent();
				br.setAction("r1.msalauncher.UPDATE_MSA");						
				m_ctx.sendBroadcast(br);    		
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
  }
	
	class RunConnect implements Runnable 
  {
		@Override
		public void run() 
		{
			Connect();			
		}    
  }
}