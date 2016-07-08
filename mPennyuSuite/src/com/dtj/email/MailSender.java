package com.dtj.email;

import java.util.Properties;

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

import r1.util.INIFile;
import r1.util.R1Util;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.dtj.email.MailConstants.TimsTracCmd;
import com.dtj.email.MailReader.OnMailReaderListener;

public class MailSender 
{
	Context m_ctx;
	INIFile m_settings;
  String m_sINIPath;
	
	static String _ACTIVATION_KEY_1 = "F125577AC0FE40CE962E8EF1C2595644";
  static String _ACTIVATION_KEY_2 = "8F96832F72C44B4C929F1B2E0AA83543";
  static String _ACTIVATION_DUPLICATE = "AF791DA7373D4712AC8BFB4CA077F7BF";
  
  OnMailSenderListener OnMailSenderEvent = null; // this is the equivalent event in C#    
  
  public MailSender(Context ctx, INIFile settings, String sINIPath)
  {
  	m_ctx = ctx;  	
  	m_settings = settings;
  	m_sINIPath = sINIPath;
  }
  
  public boolean SendCmd(TimsTracCmd cmd)
  {
  	boolean bOK = false;  	  	  	  	  	  	  	
  	TinyDB db = new TinyDB(m_ctx);
      	  	
  	TTMail mail = new TTMail();
    /*mail.setsTo(db.getString("MailTo"));
    mail.setsFrom(db.getString("MailUser"));
    mail.setsMailHost(db.getString("MailHost"));
    mail.setnPort(db.getInt("SMTPPort", 51587));
    mail.setsUsername(db.getString("MailUser"));
    mail.setsPassword(db.getString("MailPwd"));*/
  	
  	m_settings = R1Util.ReadEncryptedSettings(m_ctx, m_sINIPath);
  	
  	mail.setsTo(m_settings.getStringProperty("MSASettings", "MailTo"));
    mail.setsFrom(m_settings.getStringProperty("MSASettings", "MailUser"));
    mail.setsMailHost(m_settings.getStringProperty("MSASettings", "MailHost"));
    mail.setnPort(m_settings.getIntegerProperty("MSASettings", "SMTPPort"));
    mail.setsUsername(m_settings.getStringProperty("MSASettings", "MailUser"));
    mail.setsPassword(m_settings.getStringProperty("MSASettings", "MailPwd"));
           	   	  	
   	//Session session = CreateSession(mail);   	  	 
    
    RunCreateMail rcm = new RunCreateMail(m_ctx, mail, cmd, m_SenderHandler, m_sINIPath);
    new Thread(rcm).start();
    
    return bOK;
  }
  
  /*public void SendEmail(TTMail mail, TimsTracCmd cmd)
  {  	
  	//Session session = CreateSession(mail);
  	
  	RunCreateMail rcm = new RunCreateMail(m_ctx, mail, cmd, m_SenderHandler, m_sINIPath);
    new Thread(rcm).start();
    
  	//CreateMail(mail, cmd);
  	//RunSenderThread(mail);  	
  }*/
  
  private Session CreateSession(TTMail mail)
  {  	
		Properties props = System.getProperties();
    props.setProperty("mail.smtp.host", mail.getsMailHost());
    props.setProperty("mail.smtp.port", String.valueOf(mail.getnPort()));
    
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.starttls.enable","true");
    props.put("mail.smtp.ssl.trust", "*");    
   	props.put("mail.smtp.ssl.checkserveridentity", "false");    
   	props.put("mail.smtp.auth", "true");
   	
   	final String sUsername = mail.getsUsername();
   	final String sPassword = mail.getsPassword();
   	
  	//Session session = Session.getDefaultInstance(props,
   	Session session = Session.getInstance(props,
        new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sUsername, sPassword);
            }
        });  	
  	
  	return session;
  }
      
  Handler m_SenderHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{	
			try
			{
				Bundle bnd = msg.getData();
				TimsTracCmd cmd = TimsTracCmd.lookup(bnd.getInt("TimsTracCmd", 1000));
				int nSendResult = bnd.getInt("SendResult", 0);
				
				switch (nSendResult)
				{
					case 0:
					{
						if (OnMailSenderEvent != null)
							OnMailSenderEvent.onMailSent(false, cmd);					
						break;
					}
					case 1:
					{										
						if (OnMailSenderEvent != null)
							OnMailSenderEvent.onMailSent(true, cmd);
						
						break;
					}
				}
			}
			catch (Exception ex)
			{}						
		}
	};
  
  public void setOnMailSenderListener(OnMailSenderListener listener) 
	{		
  	OnMailSenderEvent = listener;		
	}
  
  //Define our custom Listener interface
	public interface OnMailSenderListener 
	{		
		public abstract void onMailSent(boolean bSendOK, TimsTracCmd cmd);		
	}
}
