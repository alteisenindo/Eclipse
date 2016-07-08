package com.dtj.email;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import r1.util.AuthModule;
import r1.util.INIFile;
import r1.util.R1Util;

import com.dtj.email.MailConstants.TimsTracCmd;

import Decoder.BASE64Encoder;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.TextView;

public class RunCreateMail implements Runnable 
{
	Context m_ctx;
	TTMail m_mail;
	TimsTracCmd m_cmd;
	Handler m_handler;	
	//String m_sMACAddress = "";
	
	INIFile m_settings;
  String m_sINIPath;
	
	String _ACTIVATION_KEY_1 = "F125577AC0FE40CE962E8EF1C2595644";
  String _ACTIVATION_KEY_2 = "8F96832F72C44B4C929F1B2E0AA83543";
  String _ACTIVATION_DUPLICATE = "AF791DA7373D4712AC8BFB4CA077F7BF";
	
	public RunCreateMail(Context ctx, TTMail mail, TimsTracCmd cmd, Handler cmHandler, String sINIPath) 
  {
		m_ctx = ctx;  	
  	m_cmd = cmd;  	
  	m_handler = cmHandler;
  	
  	m_mail = new TTMail();
  	m_mail.Copy(mail);
  	  	
  	m_sINIPath = sINIPath;
  }
	
	@Override
	public void run() 
	{
		TinyDB db = new TinyDB(m_ctx);
		
		// reload
		//R1Util.SaveEncryptedSettings(m_settings, m_sINIPath);
		m_settings = R1Util.ReadEncryptedSettings(m_ctx, m_sINIPath);
		
		String sEmail = m_settings.getStringProperty("MSASettings", "MailXMPP");
		
		//String sWId = db.getString("AppWId");
		String sWId = m_settings.getStringProperty("MSASettings", "AppWId");
		//String sWId = "MOB-" + AuthModule.GetUniqueID(m_ctx);
		String sTime = GetCurrentTime();
		String sVer = GetAppVersion(m_ctx.getPackageName()); 
		//String sWSName = db.getString("AppWSName");
		String sWSName = m_settings.getStringProperty("MSASettings", "AppWSName");
		//String sUsername = db.getString("AppUserName");
		String sUsername = m_settings.getStringProperty("MSASettings", "AppUserName");
		String sDisableResult = db.getString("DisableResult");
		String sSetupResult = db.getString("SetupResult");
		String sDownloadUpdateResult = db.getString("DownloadUpdateResult");
		String sAppUpdateResult = db.getString("AppUpdateResult");
		String sActivationResult = db.getString("ActivationResult");
		String sDownloadContactResult = db.getString("DownloadContactResult");
						
		String sArchiveEmail = db.getString("archive.email");
		String sSoundEmail = db.getString("sound.email");
		
		String sFeatKeyLogger = db.getString("feature.keylogger");
		String sFeatAppLogger = db.getString("feature.applogger");
		String sFeatWebLogger = db.getString("feature.weblogger");
		String sFeatAttLogger = db.getString("feature.attlogger");
		String sFeatMailAdmin = db.getString("feature.mailadmin");
		
		String sFeatPayroll = db.getString("feature.payroll");
		//String sFeatChat = db.getString("feature.chat");
		//String sFeatVoip = db.getString("feature.voip");
		String sFeatChat = m_settings.getStringProperty("MSASettings", "feature.chat");
		String sFeatVoip = m_settings.getStringProperty("MSASettings", "feature.voip");
		String sFeatPuma = m_settings.getStringProperty("MSASettings", "feature.puma");
		
		if (sFeatChat.equals("2"))
			sFeatChat = "0";		
		if (sFeatVoip.equals("2"))
			sFeatVoip = "0";
		//String sVoipUsername = db.getString("voip.username");
		String sVoipUsername = m_settings.getStringProperty("MSASettings", "voip.username");
		//String sVoipPassword = db.getString("voip.password");
		String sVoipPassword = m_settings.getStringProperty("MSASettings", "voip.password");
		String sAttLogTime = db.getString("attlog.time");
		
		String sRCmd = "";
		
		String sOSVersion = "Android version " + android.os.Build.VERSION.SDK_INT;		
		
		//String sCId = db.getString("AppCId");
		String sCId = m_settings.getStringProperty("MSASettings", "AppCId");		
		if (sCId.trim().length() <= 0)		
		{
			sCId = CIDCreator.EncryptCId(m_ctx, m_settings);
			//db.putString("AppCId", sCId);
			m_settings.setStringProperty("MSASettings", "AppCId", sCId, null);
			R1Util.SaveEncryptedSettings(m_settings, m_sINIPath);
		}
		
		if (sCId == null)
		{
			m_handler.sendEmptyMessage(0);
			return;
		}
		
		String sMACAddress = db.getString("MACAddress");
		String sIPAddressv4 = db.getString("IPAddressv4");
		
		switch (m_cmd)
		{
			default:
			{
				break;
			}
			case Activation:
			{
				// must regenerate CId everytime we send activation
				String sAppStatus = m_settings.getStringProperty("MSASettings", "AppStatus");
				if (sAppStatus.equals(MailConstants.APP_DISABLED))
				{
					sCId = CIDCreator.EncryptCId(m_ctx, m_settings);
					//db.putString("AppCId", sCId);
					m_settings.setStringProperty("MSASettings", "AppCId", sCId, null);
					R1Util.SaveEncryptedSettings(m_settings, m_sINIPath);
				}
				
				sRCmd = "activation";
				m_mail.setsSubject("TIMS TRAC Activation Request for " + sWId);
				String sBody = String.format(
						"Dear Administrator,\r\n\r\n" +
						"Please reply this email with the Activation Key in one line.\r\n\r\n" + 
						"wid=\"%s\"\r\n" +
						"cid=\"%s\"\r\n" +
						"rcmd=\"%s\"\r\n" +
						"time=\"%s\"\r\n" +
						"ver=\"%s\"\r\n" + 
						"name=\"%s\"\r\n\r\n" +
						"email=\"%s\"\r\n\r\n" +
						"inf.os=\"%s\"\r\n" +
						"inf.username=\"%s\"\r\n" +
						"inf.mac=\"%s\"\r\n" +
						"inf.ip=\"%s\"\r\n"
				, sWId, sCId, sRCmd, sTime, sVer, sUsername, sEmail, 
					sOSVersion, sWId, sMACAddress, sIPAddressv4);
				m_mail.setsBody(sBody);				
				break;
			}
			case ActivationReceived:
			{								
				sRCmd = "activated";
				m_mail.setsSubject("Re: TIMS TRAC Activation Request for " + sWId);
				String sBody = String.format(
						"cid=\"%s\"\r\n" +
						"wid=\"%s\"\r\n" +
						"ver=\"%s\"\r\n" +
						"rcmd=\"%s\"\r\n" +
						"result=\"%s\"\r\n" +
						"workstation=\"%s\"\r\n" + // equals wid
						"archive.email=\"%s\"\r\n" +
						"sound.email=\"%s\"\r\n" +
						
						"feature.keylogger=\"%s\"\r\n" +						
						"feature.applogger=\"%s\"\r\n" +
						"feature.weblogger=\"%s\"\r\n" +
						"feature.attlogger=\"%s\"\r\n" +
						"feature.mailadmin=\"%s\"\r\n" +						
						"feature.payroll=\"%s\"\r\n" +
						"feature.chat=\"%s\"\r\n" +
						"feature.voip=\"%s\"\r\n" +
						"feature.puma=\"%s\"\r\n" +
						
						"voip.username=\"%s\"\r\n" +
						"voip.password=\"%s\"\r\n" +
						"attlog.time=\"%s\"\r\n"
												
				, sCId, sWId, sVer, sRCmd, sActivationResult, sWId, sArchiveEmail, sSoundEmail, sFeatKeyLogger, sFeatAppLogger,
				  sFeatWebLogger, sFeatAttLogger, sFeatMailAdmin, sFeatPayroll, sFeatChat, sFeatVoip, sFeatPuma, sVoipUsername, sVoipPassword,
				  sAttLogTime);
				m_mail.setsBody(sBody);
				
				break;
			}
			
			case Disable:
			{
				/*Email Subject: Re: Disable
				Email Body:
				cid="fn4Jcg0DAXcHAwB3BQEGewELegIJd3BycQIFDwZzAgQIcn0BCgMDcwcHcQVXLlUxUhJSI182"
				wid="NA-VM"
				ver="1.0.0.551|1.0.0.259"
				rcmd="disable"
				result="OK"      // response result, "OK" or "Error: error message...."*/
				
				sRCmd = "disable";
				m_mail.setsSubject("Re: Disable");
				String sBody = String.format(
						"cid=\"%s\"\r\n" +
						"wid=\"%s\"\r\n" +
						"ver=\"%s\"\r\n" +						
						"rcmd=\"%s\"\r\n" +
						"result=\"%s\"\r\n" 											
				, sCId, sWId, sVer, sRCmd, sDisableResult);
				m_mail.setsBody(sBody);
				break;
			}
			case Info:
			case CollectAll:
			{
				/*Email Subject: Re: Request info
				Email Body:
				cid="D3EJBgsDBHAHC3p3AQcGegELDXV3BncGBHcFfQN3AXEPcg4FCgMDcwcHcQNBMVEx"
				wid="TESTPC-02"
				ver="1.0.0.551|1.0.0.259"
				rcmd="info"
				workstation="TESTPC-02"
				archive.email=""
				sound.email=""
				feature.keylogger="1"
				feature.applogger="1"
				feature.weblogger="1"
				feature.attlogger="1"
				feature.mailadmin="1"
				feature.payroll="0"
				feature.chat="1"
				feature.voip="1"
				voip.username="8010"
				voip.password="12345"
				attlog.time="08:05,16:05"
				inf.os="Microsoft Windows 8.1 N  (6.3.9600)"
				inf.username="user"
				inf.mac="00-0C-29-FC-DB-73"*/
				
				sRCmd = "info";
				if (m_cmd == MailConstants.TimsTracCmd.Info)
					m_mail.setsSubject("Re: Request Info");
				else 
					m_mail.setsSubject("Re: Collect All");
				
				String sBody = String.format(
						"cid=\"%s\"\r\n" +
						"wid=\"%s\"\r\n" +
						"ver=\"%s\"\r\n" +
						"rcmd=\"%s\"\r\n" +						
						"workstation=\"%s\"\r\n" +
						"archive.email=\"%s\"\r\n" +
						"sound.email=\"%s\"\r\n" +
						
						"feature.keylogger=\"%s\"\r\n" +						
						"feature.applogger=\"%s\"\r\n" +
						"feature.weblogger=\"%s\"\r\n" +
						"feature.attlogger=\"%s\"\r\n" +
						"feature.mailadmin=\"%s\"\r\n" +						
						"feature.payroll=\"%s\"\r\n" +
						"feature.chat=\"%s\"\r\n" +
						"feature.voip=\"%s\"\r\n" +
						"feature.puma=\"%s\"\r\n" +
						
						"voip.username=\"%s\"\r\n" +
						"voip.password=\"%s\"\r\n" +
						"attlog.time=\"%s\"\r\n" +
						
						"inf.os=\"%s\"\r\n" +
						"inf.username=\"%s\"\r\n" +
						"inf.mac=\"%s\"\r\n" +
						"inf.ip=\"%s\"\r\n"
												
				, sCId, sWId, sVer, sRCmd, sWId, sArchiveEmail, sSoundEmail, sFeatKeyLogger, sFeatAppLogger,
				  sFeatWebLogger, sFeatAttLogger, sFeatMailAdmin, sFeatPayroll, sFeatChat, sFeatVoip, sFeatPuma, sVoipUsername, sVoipPassword,
				  sAttLogTime, sOSVersion, sUsername, sMACAddress, sIPAddressv4);										
				m_mail.setsBody(sBody);				
				break;
			}						
			
			case Setup:
			{				
				/*Email Subject: Re: Set Configuration
				Email Body:
				cid="AXEJDw8DC3EGCwBxDQFxenhxAAdye3RxCQR5AXYBDXYAf3wECgMDcwcHcQN1EBkCd3ZrCXgG"
				wid="MOB-22"
				ver="1.0"
				rcmd="setup"
				result="OK"
				workstation="MOB-22"
				archive.email=""
				sound.email=""
				feature.keylogger="0"
				feature.applogger="0"
				feature.weblogger="0"
				feature.attlogger="0"
				feature.mailadmin="0"
				feature.payroll="0"
				feature.chat="1"
				feature.voip="0"
				voip.username="9001"
				voip.password="557700"
				attlog.time=""*/
				
				sRCmd = "setup";
				m_mail.setsSubject("Re: Set Configuration");
				String sBody = String.format(
						"cid=\"%s\"\r\n" +
						"wid=\"%s\"\r\n" +
						"ver=\"%s\"\r\n" +
						"rcmd=\"%s\"\r\n" +
						"result=\"%s\"\r\n" +
						"workstation=\"%s\"\r\n" + // equals wid
						"archive.email=\"%s\"\r\n" +
						"sound.email=\"%s\"\r\n" +
						
						"feature.keylogger=\"%s\"\r\n" +						
						"feature.applogger=\"%s\"\r\n" +
						"feature.weblogger=\"%s\"\r\n" +
						"feature.attlogger=\"%s\"\r\n" +
						"feature.mailadmin=\"%s\"\r\n" +						
						"feature.payroll=\"%s\"\r\n" +
						"feature.chat=\"%s\"\r\n" +
						"feature.voip=\"%s\"\r\n" +
						"feature.puma=\"%s\"\r\n" +
						
						"voip.username=\"%s\"\r\n" +
						"voip.password=\"%s\"\r\n" +
						"attlog.time=\"%s\"\r\n"
												
				, sCId, sWId, sVer, sRCmd, sSetupResult, sWId, sArchiveEmail, sSoundEmail, sFeatKeyLogger, sFeatAppLogger,
				  sFeatWebLogger, sFeatAttLogger, sFeatMailAdmin, sFeatPayroll, sFeatChat, sFeatVoip, sFeatPuma, sVoipUsername, sVoipPassword,
				  sAttLogTime);
				m_mail.setsBody(sBody);				
				break;
			}
			case DefConfig:
			{										
				/*Email Subject: Re: Get Default Configuration
				Email Body:
				cid="fnAJAn4DCnMHC3B3AAYGAXwLCXZ3AHEGCQMFDHF3AQAPcQEFCgMDcwcHcQZBMVEx"
				wid="TESTPC-01"
				ver="1.0.0.551"
				rcmd="defconfig"*/
				
				sRCmd = "defconfig";
				m_mail.setsSubject("Re: Get Default Configuration");
				String sBody = String.format(
						"cid=\"%s\"\r\n" +
						"wid=\"%s\"\r\n" +
						"ver=\"%s\"\r\n" +
						"rcmd=\"%s\"\r\n"			
				, sCId, sWId, sVer, sRCmd);
				m_mail.setsBody(sBody);				
				break;
			}
			
			case DownloadUpdateOK:
			{										
				/*Email Subject: Re: Update
					Email Body:
					cid="enV8BA4DB3YBBXQNAAQNcnwHfwR0dgoGB3VxeXVzcHAKdQlyCgMDcgcKcg1aIQc="
					wid="nc-3"
					ver="1.0.0.555"
					rcmd="update"
					result="OK"*/
				
				sRCmd = "update";
				m_mail.setsSubject("Re: Update");
				String sBody = String.format(
						"cid=\"%s\"\r\n" +
						"wid=\"%s\"\r\n" +
						"ver=\"%s\"\r\n" +
						"rcmd=\"%s\"\r\n" +			
						"result=\"%s\"\r\n"
				, sCId, sWId, sVer, sRCmd, sDownloadUpdateResult);
				m_mail.setsBody(sBody);				
				break;
			}
			
			case AppUpdateOK:
			{										
				/*Email Subject: Re: Update
					Email Body:
					cid="enV8BA4DB3YBBXQNAAQNcnwHfwR0dgoGB3VxeXVzcHAKdQlyCgMDcgcKcg1aIQc="
					wid="nc-3"
					ver="1.0.0.555"
					rcmd="updated"
					result="OK"*/
				
				sRCmd = "updated";
				m_mail.setsSubject("Re: Update");
				String sBody = String.format(
						"cid=\"%s\"\r\n" +
						"wid=\"%s\"\r\n" +
						"ver=\"%s\"\r\n" +
						"rcmd=\"%s\"\r\n" +			
						"result=\"%s\"\r\n"
				, sCId, sWId, sVer, sRCmd, sAppUpdateResult);
				m_mail.setsBody(sBody);				
				break;
			}
			
			case DownloadContactOK:
			{										
				/*Email Subject: Re: Contact
					Email Body:
					cid="fn4Jcg0DAXcHAwB3BQEGewELegIJd3BycQIFDwZzAgQIcn0BCgMDcwcHcQVXLlUxUhJSI182"
					wid="NA-VM"
					ver="1.0.0.551|1.0.0.259"
					rcmd="contact"
					result="OK"*/
				
				sRCmd = "contact";
				m_mail.setsSubject("Re: Contact");
				String sBody = String.format(
						"cid=\"%s\"\r\n" +
						"wid=\"%s\"\r\n" +
						"ver=\"%s\"\r\n" +
						"rcmd=\"%s\"\r\n" +			
						"result=\"%s\"\r\n"
				, sCId, sWId, sVer, sRCmd, sDownloadContactResult);
				m_mail.setsBody(sBody);				
				break;
			}
			
			case GetPasswordList:
			{										
				/*Email Subject: Get Password List 
					Email Body:
					wid="NA-VM"
					rcmd="pwdlist"*/
				
				sRCmd = "pwdlist";
				m_mail.setsSubject("Get Password List");
				String sBody = String.format(
						"cid=\"%s\"\r\n" +
						"wid=\"%s\"\r\n" +
						"ver=\"%s\"\r\n" +
						"rcmd=\"%s\"\r\n"						
				, sCId, sWId, sVer, sRCmd);
				m_mail.setsBody(sBody);				
				break;
			}
		}		
		
		boolean bOK = SendEmail();
		
		android.os.Message m1 = android.os.Message.obtain();
		Bundle bnd = new Bundle();
		bnd.putInt("TimsTracCmd", m_cmd.Value());											
		bnd.putInt("SendResult", bOK ? 1 : 0);
		m1.setData(bnd);
		m_handler.sendMessage(m1);				
	}
			
	private String GetCurrentTime()
	{
		Calendar cal = Calendar.getInstance();
		Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
		return formatter.format(cal.getTime());
	}
			
	private String GetAppVersion(String sPackageName)
	{
		try 
		{
			PackageInfo pInfo;
			pInfo = m_ctx.getPackageManager().getPackageInfo(sPackageName, 0);			
			return pInfo.versionName;
		}
		catch (Exception e) 
		{
			return "1.0";
		}
	}
	
	public boolean SendEmail()
  {
  	boolean bOK = false;  	  	
  	
  	// SOMEHOW THESE CODES DONT WORK ANYMORE
    /*Properties props = System.getProperties();  	
    props.setProperty("mail.smtp.host", m_mail.getsMailHost()); // STRING TYPE
    props.put("mail.smtp.port", m_mail.getnPort()); // INTEGER TYPE
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable","true");*/
    //props.put("mail.smtp.ssl.trust", "*");
  	
  	/*Properties props = new Properties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.host", m_mail.getsMailHost());
    props.put("mail.smtp.port", m_mail.getnPort() + "");
    props.put("mail.smtp.auth", "true");        
    props.put("mail.smtp.starttls.enable", "true");    
    props.put("mail.smtp.socketFactory.port", m_mail.getnPort() + "");
    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    props.put("mail.smtp.socketFactory.fallback", "true");
    props.setProperty("mail.smtp.socketFactory.class", "com.dtj.email.DummySSLSocketFactory");*/
        
    /*MailSSLSocketFactory sf = null;
    try {
        sf = new MailSSLSocketFactory();
    } catch (GeneralSecurityException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
    }
    sf.setTrustAllHosts(true);
    props.put("mail.smtp.ssl.socketFactory", sf);*/
  	  	  	
  	// IT WORKS
  	Properties props = new Properties();
  	props.put("mail.transport.protocol", "smtp");
  	props.put("mail.smtp.auth", "true");
  	props.put("mail.smtp.starttls.enable", "true");
  	
  	//props.put("mail.smtp.ssl.socketFactory", "com.dtj.email.DummySSLSocketFactory");
  	//props.put("mail.smtp.ssl.checkserveridentity", "false");
  	//props.put("mail.smtp.ssl.trust", "*");  	
  	//props.put("mail.smtp.ssl.trust", "192.168.125.97");
  	//props.put("mail.smtp.ssl.enable", "true");
  	//props.put("mail.smtp.socketFactory.class", "com.dtj.email.DummySSLSocketFactory");
  	
  	//props.put("mail.smtp.ssl.socketFactory", "com.dtj.email.DummySSLSocketFactory");
  	//props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");  	
  	//props.put("mail.smtp.socketFactory.port", m_mail.getnPort() + "");    
    //props.put("mail.smtp.socketFactory.fallback", "false");
  	
  	/*Properties props = new Properties();
  	props.put("mail.smtp.host", m_mail.getsMailHost());
  	props.put("mail.smtp.socketFactory.port", m_mail.getnPort() + "");
  	props.put("mail.smtp.socketFactory.class", "javax.net.SocketFactory");
  	props.put("mail.smtp.auth", "true");
  	props.put("mail.smtp.port", m_mail.getnPort() + "");
  	props.put("mail.smtp.ssl.enable", "false");
  	props.put("mail.smtp.starttls.enable", "true");
  	props.put("mail.smtp.ssl.trust", "*");*/
    
  	props.put("mail.smtp.host", m_mail.getsMailHost());
  	props.put("mail.smtp.port", m_mail.getnPort() + "");  	
  	
  	/*Properties props = new Properties();
  	props.put("mail.smtp.auth", "true");
  	props.put("mail.smtp.starttls.enable", "true");
  	props.put("mail.smtp.host", "mail.growdevices.com");
  	props.put("mail.smtp.port", "51587");*/
        
    //Session session = Session.getDefaultInstance(props, null);
    
    Session session = Session.getInstance(props,
        new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() 
            {
              return new PasswordAuthentication(m_mail.getsUsername(), m_mail.getsPassword());
            	//return new PasswordAuthentication("regclient@growdevices.com", "12345678");
            }
        });
    
    //session.setDebug(true);
    try 
    {
      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(m_mail.getsFrom()));
      msg.addRecipient(Message.RecipientType.TO,new InternetAddress(m_mail.getsTo()));
      msg.setSubject(m_mail.getsSubject());
      msg.setText(m_mail.getsBody());
      Transport.send(msg);            
      
      bOK = true;
    } 
    catch (AddressException e) 
    {
    	e.printStackTrace();
    } 
    catch (MessagingException e) 
    {
    	e.printStackTrace();
    }
    
    return bOK;
  }
	
	
	// THIS METHOD ONLY WORKS IF SMTP SERVER HAS A CERTIFICATE THAT'S NOT EXPIRED YET
	/*public boolean SendEmail()
  {
  	boolean bOK = false;  	  	
    Properties props = System.getProperties();
        
    props.put("mail.transport.protocol", "smtp");    
    props.put("mail.smtps.starttls.enable","true");    
    props.put("mail.smtps.ssl.trust", "*");       
   	props.put("mail.smtps.ssl.checkserveridentity", "false");    
   	props.put("mail.smtps.auth", "true");   	 
    
    //props.put("mail.smtp.user", m_mail.getsUsername()); 
    //props.put("mail.smtp.host", m_mail.getsMailHost()); 
    //props.put("mail.smtp.port", "51587"); 
    //props.put("mail.debug", "true"); 
    //props.put("mail.smtp.auth", "true"); 
    //props.put("mail.smtp.starttls.enable","true"); 
    //props.put("mail.smtp.EnableSSL.enable","true");

    //props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");   
    //props.setProperty("mail.smtp.socketFactory.fallback", "false");   
    //props.setProperty("mail.smtp.port", "51587");   
    //props.setProperty("mail.smtp.socketFactory.port", "51587"); 
    
    Session session = Session.getInstance(props);
   	   	   	
    Message simpleMessage = new MimeMessage(session);

    InternetAddress fromAddress = null;
    InternetAddress toAddress = null;

    try
    {
        fromAddress = new InternetAddress(m_mail.getsFrom());
        toAddress = new InternetAddress(m_mail.getsTo());
    
        //message.setFrom(new InternetAddress(fromemail,"Sender name","UTF8"));
        simpleMessage.setFrom(fromAddress);
        simpleMessage.setRecipient(RecipientType.TO, toAddress);
        simpleMessage.setSubject(m_mail.getsSubject());
        simpleMessage.setText(m_mail.getsBody());

        Transport transport = session.getTransport("smtp");        
        transport.connect(m_mail.getsMailHost(), m_mail.getnPort(), m_mail.getsUsername(), m_mail.getsPassword());        
        transport.sendMessage(simpleMessage, simpleMessage.getAllRecipients());
        transport.close();                          
        
        bOK = true;
    }
    catch(AddressException e){
      e.printStackTrace();
    }
    catch(MessagingException e){
        e.printStackTrace();
    }
    
    return bOK;
  }*/

}
