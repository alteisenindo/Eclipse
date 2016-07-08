package com.dtj.email;

import java.util.TreeMap;

import r1.util.iCCConstants.EHistoryMode;

public class MailConstants 
{		
	public static String APP_ENABLED  = "b3f491e6-1522-4492-9bf9-ab7ce4d15af3";
	public static String APP_DISABLED = "a324e4f9-5bb0-426c-b5de-43f586288266";
	public static int WAIT_COUNTER = 300; 	
			
	public enum TimsTracCmd
	{
		Activation(0),
		Disable(1),		
		Info(2),
		Setup(3),
		DefConfig(4),
		Update(5),
		DownloadUpdateOK(6),
		AppUpdateOK(7),
		ActivationReceived(8),		
		DownloadContactOK(9),
		Contact(10),
		GetPasswordList(11),
		PasswordList(12),
		CollectAll(13),
				
		Ping(100);
		
		private final int nValue;
		TimsTracCmd(int Value)
  	{
  		this.nValue = Value;
  	}
  	public int Value()
  	{
  		return nValue;
  	}
  	private static TreeMap<Integer, TimsTracCmd> _map;
	   static {
	 	_map = new TreeMap<Integer, TimsTracCmd>();
	     for (TimsTracCmd num: TimsTracCmd.values()) {
	     	_map.put(new Integer(num.Value()), num);
	     }
	   } 	   
	   public static TimsTracCmd lookup(int value) {
	 	  return _map.get(new Integer(value));
	   }
	}
	
	public enum ServiceCmd
	{
		Activation(0),		
		DefConfig(1),
		Setup(2),
		GetPasswordList(3),
		UpdateNotification(4),
		CancelNotification(5),
		
		StopService(100);
		
		private final int nValue;
		ServiceCmd(int Value)
  	{
  		this.nValue = Value;
  	}
  	public int Value()
  	{
  		return nValue;
  	}
  	private static TreeMap<Integer, ServiceCmd> _map;
	   static {
	 	_map = new TreeMap<Integer, ServiceCmd>();
	     for (ServiceCmd num: ServiceCmd.values()) {
	     	_map.put(new Integer(num.Value()), num);
	     }
	   } 	   
	   public static ServiceCmd lookup(int value) {
	 	  return _map.get(new Integer(value));
	   }
	}
	
	public enum DBField
	{
		//"cid","wid","cmd","workstation","archive.mail","sound.mail","feature.keylogger","feature.weblogger","feature.attlogger",
		//"feature.mailadmin","feature.payroll","feature.chat","feature.voip","attlog.time","voip.username","voip.password"
		
		cid(0),
		wid(1),		
		cmd(2),
		workstation(3),
		archive_mail(4),
		sound_mail(5),
		feature_keylogger(6),
		feature_weblogger(7),
		feature_attlogger(8),
		feature_applogger(9),
		feature_mailadmin(10),
		feature_payroll(11),
		feature_chat(12),
		feature_voip(13),		
		feature_puma(14),
		attlog_time(15),
		voip_username(16),
		voip_password(17),
		
		ssid0(18),
		pwd0(19),
		ssid1(20),
		pwd1(21),
		ssid2(22),
		pwd2(23),
		ssid3(24),
		pwd3(25),
		ssid4(26),
		pwd4(27),
		ssid5(28),
		pwd5(29),
		ssid6(30),
		pwd6(31);
		
		private final int nValue;
		DBField(int Value)
  	{
  		this.nValue = Value;
  	}
  	public int Value()
  	{
  		return nValue;
  	}
  	private static TreeMap<Integer, DBField> _map;
	   static {
	 	_map = new TreeMap<Integer, DBField>();
	     for (DBField num: DBField.values()) {
	     	_map.put(new Integer(num.Value()), num);
	     }
	   } 	   
	   public static DBField lookup(int value) {
	 	  return _map.get(new Integer(value));
	   }
	};
}
