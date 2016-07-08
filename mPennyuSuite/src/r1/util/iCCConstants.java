package r1.util;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class iCCConstants 
{	
	public enum EActiveApp
	{
		QSMSDialer,
		QSMSLauncher,
		QSMS
	}
	
	public enum BTRequestProtocolCode // from client to server
  {
		Exit(100),
	  Send_Cfg(101),
	  Send_Cfg_OK(102),
	  Send_Cfg_Failed(103),
	  Save_Cfg(104), // [code]|size|data
	  Copy_Data_To_Flash(105),
	  Ping(150),
		
		// specific use only
    Req_Total(1000);
		
		private final int nValue;
		BTRequestProtocolCode(int Value)
  	{
  		this.nValue = Value;
  	}
  	public int Value()
  	{
  		return nValue;
  	}
  	
  	private static TreeMap<Integer, BTRequestProtocolCode> _map;
	   static {
	 	_map = new TreeMap<Integer, BTRequestProtocolCode>();
	     for (BTRequestProtocolCode num: BTRequestProtocolCode.values()) {
	     	_map.put(new Integer(num.Value()), num);
	     }
	   } 	   
	   public static BTRequestProtocolCode lookup(int value) 
	   {
	  	 try
	  	 {
	  		 return _map.get(new Integer(value));
	  	 }
	  	 catch (Exception ex)
	  	 {
	  		 return Req_Total;
	  	 }
	   }
  }
	
	public enum BTReplyProtocolCode // from server to client
  {		
	  Send_Cfg(200), // [code][size][data]	  
	  Send_Cfg_Failed(201),
	  Save_Cfg_OK(202),
	  Save_Cfg_Failed(203),
	  Copy_Data_To_Flash_OK(204),
	  Copy_Data_To_Flash_Failed(205),
	  Ping_OK(250),
		
		// specific use only
    Reply_Total(1000);
		
		private final int nValue;
		BTReplyProtocolCode(int Value)
  	{
  		this.nValue = Value;
  	}
  	public int Value()
  	{
  		return nValue;
  	}
  	
  	private static TreeMap<Integer, BTReplyProtocolCode> _map;
	   static {
	 	_map = new TreeMap<Integer, BTReplyProtocolCode>();
	     for (BTReplyProtocolCode num: BTReplyProtocolCode.values()) {
	     	_map.put(new Integer(num.Value()), num);
	     }
	   } 	   
	   public static BTReplyProtocolCode lookup(int value) 
	   {
	  	 try
	  	 {
	  		 return _map.get(new Integer(value));
	  	 }
	  	 catch (Exception ex)
	  	 {
	  		 return Reply_Total;
	  	 }
	   }
  }
	
	///////////////////////////
	// From TaxiConstants.cs //
	///////////////////////////
	public enum RecvProtocolCode
  {                        
      ERecv_Login_OK(0),                        
      ERecv_Login_Failed(1),
      ERecv_GPS_Data_OK(6),
      ERecv_GPS_Data_Error(7),
      ERecv_New_Dispatch(11),         
      ERecv_Accept_Dispatch_Approved(12),
      ERecv_Accept_Dispatch_Not_Approved(13),
      ERecv_Reject_Dispatch_Approved(14),
      ERecv_Reject_Dispatch_Not_Approved(15),
      ERecv_Customer_NoShow_Approved(16),
      ERecv_Customer_NoShow_Not_Approved(17),
      ERecv_Start_Order_Approved(18),
      ERecv_Start_Order_Not_Approved(19),
      ERecv_Near_Destination_Approved(20),
      ERecv_Near_Destination_Not_Approved(21),
      ERecv_Finish_Order_Approved(22),
      ERecv_Finish_Order_Not_Approved(23),            
      ERecv_Message_Broadcast(33), // receive message
      ERecv_Send_Message_Broadcast_OK(34),
      ERecv_Send_Message_Broadcast_Error(35),
      ERecv_Statistic(36),
      ERecv_Statistic_OK(37),
      ERecv_Statistic_Error(38),
      ERecv_Ping_OK(39),
      ERecv_Balance_OK(40), // auto request by server
      ERecv_Logout_OK(41),
      ERecv_Logout_Error(42),
      ERecv_Park_OK(43),
      ERecv_Park_Error(44),
      ERecv_Unpark_OK(45),
      ERecv_Unpark_Error(46),
      ERecv_DriverIn_OK(47),     // also acts as the reply for ESend_Force_DriverIn
      ERecv_DriverIn_Error(48),  // also acts as the reply for ESend_Force_DriverIn
      ERecv_DriverOut_OK(49),
      ERecv_DriverOut_Error(50),
      ERecv_NoArgo_OK(54),
      ERecv_NoArgo_Error(55),
      ERecv_CheckFile_OK(60),
      ERecv_CheckFile_Error(61),
      ERecv_GetFile_OK(62),
      ERecv_GetFile_Error(63),
      ERecv_ArgoInfo_Ok(66),
      ERecv_ArgoInfo_Error(67),
      ERecv_MessageAck_OK(70),
      ERecv_MessageAck_Error(71),
      ERecv_GPSDelta_OK(78),
      ERecv_GPSDelta_Error(79),
      ERecv_Checkout_OK(89),
      ERecv_Checkout_Error(90),
      ERecv_Checkin_OK(91),
      ERecv_Checkin_Error(92),
      ERecv_Taxi_Status_OK(93),
      ERecv_Taxi_Status_Error(94),
      ERecv_Set_Taxi_Status(97), // sent by server without client's request
      ERecv_Free_Query_OK(98),
      ERecv_Free_Query_Error(99),
      ERecv_Server_Time_OK(100),
      ERecv_Server_Time_Error(101),
      ERecv_Send_Balance_OK(104),    // the reply for ESend_Balance
      ERecv_Send_Balance_Error(105), // the reply for ESend_Balance
      ERecv_Cancel_Dispatch_OK(106),
      ERecv_Cancel_Dispatch_Error(107),
      ERecv_At_Customer_OK(108),
      ERecv_At_Customer_Error(109),
      ERecv_Force_GPS_Data_OK(122),
      ERecv_Force_GPS_Data_Error(123),
      ERecv_Request_Ping(124),
      ERecv_Free_Query_Request(127),
      ERecv_Upload_Log_Request(128),
      ERecv_Upload_Log_OK(129),
      ERecv_Disable_Dispatch_OK(130),
      ERecv_Disable_Dispatch_Error(131),
      ERecv_Enable_Dispatch_OK(132),
      ERecv_Enable_Dispatch_Error(133),            
      ERecv_Free_QueryV10_OK(134),
      ERecv_Free_QueryV10_Error(135),
      ERecv_StatisticV10(136), // the real statistic data
      ERecv_StatisticV10_OK(137), // ack for command ESend_Request_StatisticV10
      ERecv_StatisticV10_Error(138), // ack  for command ESend_Request_StatisticV10
      ERecv_FileExplorer_Result_OK(142),
      ERecv_FileExplorer_Result_Error(143),
      ERecv_FileExplorer_Request(144),
      ERecv_FileExplorer_Upload_Request(146),
      ERecv_FileExplorer_Download_Request(147),

      // specific use only
      ERecv_Total(1000);
      
      private final int nValue;
      RecvProtocolCode(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
    	
    	private static TreeMap<Integer, RecvProtocolCode> _map;
 	   static {
 	 	_map = new TreeMap<Integer, RecvProtocolCode>();
 	     for (RecvProtocolCode num: RecvProtocolCode.values()) {
 	     	_map.put(new Integer(num.Value()), num);
 	     }
 	   } 	   
 	   public static RecvProtocolCode lookup(int value) 
 	   {
 	  	 try
 	  	 {
 	  		 return _map.get(new Integer(value));
 	  	 }
 	  	 catch (Exception ex)
 	  	 {
 	  		 return ERecv_Total;
 	  	 }
 	   }
  }
	public enum SendProtocolCode
  {
      ESend_Login(0),
      ESend_GPS_Data(4),
      ESend_Request_Accept_Dispatch(6),
      ESend_Request_Reject_Dispatch(7),
      ESend_Request_Customer_NoShow(8),
      ESend_Request_Start_Order(9),
      ESend_Request_Near_Destination(10),
      ESend_Request_Finish_Order(11),
      ESend_Request_Cancel_Dispatch(12),
      ESend_Request_At_Customer(13),
      ESend_Request_Message_Broadcast(16), // send message
      ESend_Request_Statistic(17),
      ESend_Request_Ping(18),
      ESend_Balance(19), 
      ESend_Request_Logout(20),
      ESend_Request_Park(21),
      ESend_Request_Unpark(22),
      ESend_Request_DriverIn(23),
      ESend_Request_DriverOut(24),
      ESend_Request_NoArgo(26),
      ESend_Request_CheckFile(29),
      ESend_Request_GetFile(30),
      ESend_ArgoInfo(32),
      ESend_MessageAck(34), // ack for message received from server
      ESend_GPS_Delta(38),
      ESend_Checkout(42),
      ESend_Checkin(43),
      ESend_Taxi_Status(44),
      ESend_Free_Query(46),
      ESend_Server_Time(47),
      ESend_Force_GPS_Data(55),
      ESend_Ping_Response(56),
      ESend_Force_DriverIn(57),
      ESend_Upload_Log(59),
      ESend_Disable_Dispatch(60),
      ESend_Enable_Dispatch(61),            
      ESend_Free_QueryV10(62),
      ESend_Request_StatisticV10(63),
      ESend_FileExplorer_Result(66),
      ESend_FileExplorer_Upload(67),
      ESend_FileExplorer_Download(68),
      
      // PUMA
      ESend_PumaPayment(100),      
      ESend_PumaConfirmSalesOrder(101),
      ESend_PumaBankDepositCash(102),
      ESend_PumaBGCheckHandover(103),
      ESend_PumaBankKliring(104),
      ESend_PumaTransferHandover(105),
      ESend_PumaFinishSalesOrder(106),
      ESend_PumaCancelSalesOrder(107),
      ESend_PumaGetServerDateTime(108),      
      ESend_PumaInsertDeviceInfo(109),
      ESend_PumaGetServerDateTimeReason(110),
      ESend_PumaBlankSpotResult(111),
      ESend_PumaActivityLog(112),
      
      ESend_Total(1000);
      
      private final int nValue;
      SendProtocolCode(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
    	private static TreeMap<Integer, SendProtocolCode> _map;
  	   static {
  	 	_map = new TreeMap<Integer, SendProtocolCode>();
  	     for (SendProtocolCode num: SendProtocolCode.values()) {
  	     	_map.put(new Integer(num.Value()), num);
  	     }
  	   } 	   
  	   public static SendProtocolCode lookup(int value)   	   
  	   {
  	  	 try
   	  	 {
   	  		 return _map.get(new Integer(value));
   	  	 }
   	  	 catch (Exception ex)
   	  	 {
   	  		 return ESend_Total;
   	  	 }  	 	  
  	   }
  }
	public enum SelectProtocolGroup //group of protocol when using "SELECT"
  {
      Single(0),
      General(1),
      GPS(2),
      Job(3),
      Message(4),
      All(5);
      
      private final int nValue;
      SelectProtocolGroup(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
    	private static TreeMap<Integer, SelectProtocolGroup> _map;
  	   static {
  	 	_map = new TreeMap<Integer, SelectProtocolGroup>();
  	     for (SelectProtocolGroup num: SelectProtocolGroup.values()) {
  	     	_map.put(new Integer(num.Value()), num);
  	     }
  	   } 	   
  	   public static SelectProtocolGroup lookup(int value) {
  	 	  return _map.get(new Integer(value));
  	   }
  }
  public enum InsertProtocolGroup //group of protocol when using "INSERT"
  {
      Single(0),
      GetFile(1),
      GPSData(2),
      Job(3),
      JobWithReason(4),
      Argo(5),
      Ping(6),
      Message(7),
      FreeMessage(8),
      FreeQuery(9),
      DriverIn(10),
      ForceDriverIn(11),
      CheckInOut(12),
      UploadLog(13),
      FileEx(14),
      FileExUpload(15),
      PumaCommands(16);
      
      private final int nValue;
      InsertProtocolGroup(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
    	private static TreeMap<Integer, InsertProtocolGroup> _map;
  	   static {
  	 	_map = new TreeMap<Integer, InsertProtocolGroup>();
  	     for (InsertProtocolGroup num: InsertProtocolGroup.values()) {
  	     	_map.put(new Integer(num.Value()), num);
  	     }
  	   } 	   
  	   public static InsertProtocolGroup lookup(int value) {
  	 	  return _map.get(new Integer(value));
  	   }
  }
  public enum EQueryResult
  {
      NotFound,      
      Incomplete,
      NextSessionId,
      OK        
  }
	
	private static Object[][] strCommands =
	{            
		new Object[] {SendProtocolCode.ESend_ArgoInfo,"'Argo Info'"},
    new Object[] {SendProtocolCode.ESend_Balance,"'Send Balance'"},
    new Object[] {SendProtocolCode.ESend_GPS_Data,"'GPS Data'"},
    new Object[] {SendProtocolCode.ESend_GPS_Delta,"'GPS Delta'"},
    new Object[] {SendProtocolCode.ESend_Ping_Response,"'Ping Response'"},
    new Object[] {SendProtocolCode.ESend_Force_GPS_Data,"'Force GPS Data'"},
    new Object[] {SendProtocolCode.ESend_Force_DriverIn,"'Force Driver In'"},
    new Object[] {SendProtocolCode.ESend_Login,"'Login'"},
    new Object[] {SendProtocolCode.ESend_Checkout,"'Checkout'"},
    new Object[] {SendProtocolCode.ESend_Checkin,"'Checkin'"},
    new Object[] {SendProtocolCode.ESend_Taxi_Status,"'Taxi Status'"},
    new Object[] {SendProtocolCode.ESend_Server_Time,"'Server Time'"},
    new Object[] {SendProtocolCode.ESend_Free_Query,"'Free Query'"},
    new Object[] {SendProtocolCode.ESend_MessageAck,"'Message Ack'"},
    new Object[] {SendProtocolCode.ESend_Enable_Dispatch ,"'Enable Dispatch'"},
    new Object[] {SendProtocolCode.ESend_Disable_Dispatch ,"'Disable Dispatch'"},
    new Object[] {SendProtocolCode.ESend_Free_QueryV10 ,"'Free Query v10'"},            

    new Object[] {SendProtocolCode.ESend_Request_At_Customer,"'At Customer'"},
    new Object[] {SendProtocolCode.ESend_Request_Accept_Dispatch,"'Accept Job'"},
    new Object[] {SendProtocolCode.ESend_Request_Customer_NoShow,"'Customer NoShow'"},
    new Object[] {SendProtocolCode.ESend_Request_Cancel_Dispatch,"'Cancel Job'"},
    new Object[] {SendProtocolCode.ESend_Request_DriverIn,"'Driver In'"},
    new Object[] {SendProtocolCode.ESend_Request_DriverOut,"'Driver Out'"},
    new Object[] {SendProtocolCode.ESend_Request_Finish_Order,"'Finish Job'"}, 
    new Object[] {SendProtocolCode.ESend_Request_CheckFile,"'CheckFile'"},
    new Object[] {SendProtocolCode.ESend_Request_GetFile,"'GetFile'"},            
    new Object[] {SendProtocolCode.ESend_Upload_Log,"'Upload Log'"},
    new Object[] {SendProtocolCode.ESend_Request_Logout,"'Logout'"},
    new Object[] {SendProtocolCode.ESend_Request_Message_Broadcast,"'Send Message'"},
    new Object[] {SendProtocolCode.ESend_Request_Near_Destination,"'Near Destination'"},
    new Object[] {SendProtocolCode.ESend_Request_NoArgo,"'No Argo'"},
    new Object[] {SendProtocolCode.ESend_Request_Park,"'Park'"},            
    new Object[] {SendProtocolCode.ESend_Request_Ping,"'Ping'"},
    new Object[] {SendProtocolCode.ESend_Request_Reject_Dispatch,"'Reject Job'"},
    new Object[] {SendProtocolCode.ESend_Request_Start_Order,"'Start Order'"},
    new Object[] {SendProtocolCode.ESend_Request_Statistic,"'Statistic'"},
    new Object[] {SendProtocolCode.ESend_Request_StatisticV10,"'StatisticV10'"},            
    new Object[] {SendProtocolCode.ESend_Request_Unpark,"'Unpark'"},         
    new Object[] {SendProtocolCode.ESend_FileExplorer_Result,"'File Explorer Result'"},  
    new Object[] {SendProtocolCode.ESend_FileExplorer_Upload,"'File Explorer Upload'"},  
    new Object[] {SendProtocolCode.ESend_FileExplorer_Download ,"'File Explorer Download'"}
	};		
	public static String GetCommandString(SendProtocolCode sendCode)
  {
      try
      {
          return SearchString(sendCode);
      }
      catch (Exception ex)
      {
          return "";
      }
  }
  private static String SearchString(Object Code)
  {
      for (int i = 0; i < strCommands.length; i++)
      {
      	if (strCommands[i][0].equals(Code))
          return strCommands[i][1].toString();
      }

      return "Error searching message";
  }
  private static String[] strGPSHistoryRow =
  {
      "Latitude",    
      "Longitude",            
      "Speed",
      "HDOP",
      "DateTime",
      "Num. Of Satellite",
      "Direction",
      "Altitude"            
  };
  public enum GPSHistoryRow
  {
      Latitude(0),
      Longitude(1),            
      Speed(2),
      HDOP(3),
      DateTime(4),
      TotNumOfSatInView(5),
      Direction(6),
      Altitude(7);
      
      private final int nValue;
      GPSHistoryRow(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
  }
  private static String[] striCCMessage =
	{                        
		"Emergency",
		"Request Road Name",
		"Busy",
		"Break",
		"Test"
	};                

  private static String[] strMessageStatus =
  {
  	"New",    
  	"Sent",
  	"Received"            
  };
  public enum iCCMessageStatus
  {
      NewMsg(1),
      Sent(2),
      Received(3);
      
      private final int nValue;
      iCCMessageStatus(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
  };
  private static String[] strTaxiStatus =
  {
  	"Idle",    
  	"Dispatching",
  	"To Customer",
  	"At Customer",
  	"To Destination",
  	"Near Destination",
  	"Finish",
  	"LoggingOut",
  	"Unknown"
  };
  public enum iCCTaxiStatus
  {
      Idle(0),
      Dispatching(1),
      ToCustomer(2),
      AtCustomer(3),
      ToDest(4),
      NearDest(5),
      Finish(6),
      LoggingOut(7),
      Unknown(8);
      
      private final int nValue;
      iCCTaxiStatus(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
    	private static TreeMap<Integer, iCCTaxiStatus> _map;
 	   static {
 	 	_map = new TreeMap<Integer, iCCTaxiStatus>();
 	     for (iCCTaxiStatus num: iCCTaxiStatus.values()) {
 	     	_map.put(new Integer(num.Value()), num);
 	     }
 	   } 	   
 	   public static iCCTaxiStatus lookup(int value) {
 	 	  return _map.get(new Integer(value));
 	   }
  };
  public static String GetTaxiStatus(iCCTaxiStatus Index)
  {
      try
      {
          return strTaxiStatus[Index.Value()];
      }
      catch (Exception ex)
      {
          return "";
      }
  }
  public static String GPSRow(GPSHistoryRow Index)
  {
      try
      {
          return strGPSHistoryRow[Index.Value()];
      }
      catch (Exception ex)
      {
          return "";
      }            
  }                
  public static String GetICCMessageStatus(iCCMessageStatus Index)
  {
      try
      {
          return strMessageStatus[Index.Value()];
      }
      catch (Exception ex)
      {
          return "";
      }            
  } 
  
  private static String[][] strJobStatus =
  {
  	new String[] {"Unknown","Tidak diketahui"},
  	new String[] {"Dispatch","Dispatch"},
  	new String[] {"On-The-Street","Lambaian"},
  	new String[] {"Cancelled","Dibatalkan"},
  	new String[] {"Accepted","Diterima"},
  	new String[] {"Rejected","Ditolak"},
  	new String[] {"Timeout","Timeout"},
  	new String[] {"At Customer","Standby"},
  	new String[] {"Started","Dimulai"},
  	new String[] {"No Show","No Show"},
  	new String[] {"Near Dest","Dekat Tujuan"},
  	new String[] {"Finished","Selesai"}            
  };
  
  public enum iCCJobStatus
  {
  	Unknown(0),
  	NewJob(1),
  	OnTheStreet(2),
  	Cancelled(3),
  	Accepted(4),
  	Rejected(5),
  	Timeout(6),
  	AtCustomer(7),
  	Started(8),
  	NoShow(9),
  	NearDest(10),
  	Done(11);

  	private final int nValue;
  	iCCJobStatus(int Value)
  	{
  		this.nValue = Value;
  	}
  	public int Value()
  	{
  		return nValue;
  	}    	

  	private static TreeMap<Integer, iCCJobStatus> _map;
  	static {
  		_map = new TreeMap<Integer, iCCJobStatus>();
  		for (iCCJobStatus num: iCCJobStatus.values()) {
  			_map.put(new Integer(num.Value()), num);
  		}
  	} 	   
  	public static iCCJobStatus lookup(int value) {
  		return _map.get(new Integer(value));
  	}
  };
  /*public static String GetICCJobStatus(iCCJobStatus Index)
  {            
      try
      {
          int nLang = 0;
          if (!MessageList.m_sLanguage.equals("EN"))
              nLang = 1;

          return strJobStatus[Index.Value()][nLang];
      }
      catch (Exception ex)
      {
          return "";
      }
  }*/
  public enum iCCMessageSender
  {
      Server(0),
      Taxi(1),
      Operator(2);
      
      private final int nValue;
      iCCMessageSender(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
    	
    	private static TreeMap<Integer, iCCMessageSender> _map;
  	   static {
  	 	_map = new TreeMap<Integer, iCCMessageSender>();
  	     for (iCCMessageSender num: iCCMessageSender.values()) {
  	     	_map.put(new Integer(num.Value()), num);
  	     }
  	   } 	   
  	   public static iCCMessageSender lookup(int value) {
  	 	  return _map.get(new Integer(value));
  	   }
  }
  private static String[] strMessageSender =
  {
      "Server",
      "Taxi",
      "Operator"
  };
  
  public static String GetMessageSender(iCCMessageSender Index)
  {
      try
      {
          return strMessageSender[Index.Value()];
      }
      catch (Exception ex) 
      {
          return "";
      }
  }
  public enum EventType
  {
      MouseDown(0),
      MouseUp(1),
      Click(2);
      
      private final int nValue;
      EventType(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
  }
  public enum JobMode
  {
      Manual(0),
      Simulation(0);
      
      private final int nValue;
      JobMode(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
  }
  public enum MessageStatus
  {
      Sent(0),
      Delivered(1),
      NotDelivered(2),
      WaitUpdate(3);
      
      private final int nValue;
      MessageStatus(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}    	
    	private static TreeMap<Integer, MessageStatus> _map;
  	   static {
  	 	_map = new TreeMap<Integer, MessageStatus>();
  	     for (MessageStatus num: MessageStatus.values()) {
  	     	_map.put(new Integer(num.Value()), num);
  	     }
  	   } 	   
  	   public static MessageStatus lookup(int value) {
  	 	  return _map.get(new Integer(value));
  	   }
  }
  public static int GetMessageStatus(int nStatus)
  {
  	MessageStatus stat = MessageStatus.WaitUpdate;
    
  	if (nStatus >= 0 && nStatus <= stat.Value())
      return nStatus;
    else
    	return stat.Value();
  }
  public enum FileType
  {
      Dictionary(0),
      Firmware(1),
      Setting(2),
      DriverPhoto(3),
      TaxiInfo(4),
      Template(5),        
      Unknown(6),
      FileExUpload(100);
      
      private final int nValue;
      FileType(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
    	
    	private static TreeMap<Integer, FileType> _map;
  	   static {
  	 	_map = new TreeMap<Integer, FileType>();
  	     for (FileType num: FileType.values()) {
  	     	_map.put(new Integer(num.Value()), num);
  	     }
  	   } 	   
  	   public static FileType lookup(int value) {
  	 	  return _map.get(new Integer(value));
  	   }
  }
  public static int GetFileType(int nFileType)
  {
  	FileType ftype = FileType.Unknown;
  	
  	if (nFileType >= 0 && nFileType < ftype.Value())
  		return nFileType;
  	else
  		return ftype.Value();
  }
  
  public enum ETaxiStatusUpdate
  {
      CarEngine(0),
      ResetDevice(1),
      ResetGSMModem(3),
      ClearGPSBuffer(4),
      RecheckBalance(5);
      
      private final int nValue;
      ETaxiStatusUpdate(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
    	private static TreeMap<Integer, ETaxiStatusUpdate> _map;
  	   static {
  	 	_map = new TreeMap<Integer, ETaxiStatusUpdate>();
  	     for (ETaxiStatusUpdate num: ETaxiStatusUpdate.values()) {
  	     	_map.put(new Integer(num.Value()), num);
  	     }
  	   } 	   
  	   public static ETaxiStatusUpdate lookup(int value) {
  	 	  return _map.get(new Integer(value));
  	   }
  }
  public enum EResetDevice
  {
      Immediately(0),
      WhenIdle(1),
      Unknown(2);
      
      private final int nValue;
      EResetDevice(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
    	private static TreeMap<Integer, EResetDevice> _map;
 	   static {
 	 	_map = new TreeMap<Integer, EResetDevice>();
 	     for (EResetDevice num: EResetDevice.values()) {
 	     	_map.put(new Integer(num.Value()), num);
 	     }
 	   } 	   
 	   public static EResetDevice lookup(int value) {
 	 	  return _map.get(new Integer(value));
 	   }
  }
  public enum SMSCommandCode
  {
      SendMessage(0),
      LockMDT(1),
      UnlockMDT(2),
      ResetMDT(3),
      FakeMode(4),
      UnlockFakeMode(5);
      
      private final int nValue;
      SMSCommandCode(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
    	
    	private static TreeMap<Integer, SMSCommandCode> _map;
  	   static {
  	 	_map = new TreeMap<Integer, SMSCommandCode>();
  	     for (SMSCommandCode num: SMSCommandCode.values()) {
  	     	_map.put(new Integer(num.Value()), num);
  	     }
  	   } 	   
  	   public static SMSCommandCode lookup(int value) {
  	 	  return _map.get(new Integer(value));
  	   }
  }
  
  public enum EScreenBrightness
  {
      Dim(10),
      Bright(95);
      
      private final int nValue;
      EScreenBrightness(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
  }
  public enum ServerFreeQueryCommands
  {
      MDTERROR(0),
      MDTEMERGENCY(1),
      MDTRESULT(2),
      LISTREGION(3),
      LISTORDER(4),
      BOOK(5),
      UNBOOK(6),
      SMSLOG(7),
      LOCATE(8),
      NONE(9);
      
      private final int nValue;
      ServerFreeQueryCommands(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
  }
  public enum MDTFreeQueryCommands
  {
      HELP(0),
      FREEMEMORY(1),
      GPSSTATUS(2),
      DRIVERLIST(3),
      HARDWAREID(4),
      SMSSEND(5),
      SUSPEND(6),
      UNSUSPEND(7),
      LOCK(8),
      UNLOCK(9),
      FAKEMODE(10),
      UNFAKEMODE(11),
      GSMSIGNALSTR(12),
      APPVERSION(13),
      NETSTAT(14),
      NONE(15);
      
      private final int nValue;
      MDTFreeQueryCommands(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
    	private static TreeMap<Integer, MDTFreeQueryCommands> _map;
  	   static {
  	 	_map = new TreeMap<Integer, MDTFreeQueryCommands>();
  	     for (MDTFreeQueryCommands num: MDTFreeQueryCommands.values()) {
  	     	_map.put(new Integer(num.Value()), num);
  	     }
  	   } 	   
  	   public static MDTFreeQueryCommands lookup(int value) 
  	   {
  	 	  return _map.get(new Integer(value));
  	   }
  }
  public enum MDTFreeQueryResults
  {
      LISTREGION(0),
      LISTORDER(1),
      BOOK(2),
      UNBOOK(3),            
      SMSLOG(4),
      LOCATE(5),
      NONE(6);
      
      private final int nValue;
      MDTFreeQueryResults(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
    	private static TreeMap<Integer, MDTFreeQueryResults> _map;
 	   static {
 	 	_map = new TreeMap<Integer, MDTFreeQueryResults>();
 	     for (MDTFreeQueryResults num: MDTFreeQueryResults.values()) {
 	     	_map.put(new Integer(num.Value()), num);
 	     }
 	   } 	   
 	   public static MDTFreeQueryResults lookup(int value) 
 	   {
 	 	  return _map.get(new Integer(value));
 	   }
  }

  public enum RejectReason
  {
      ByDriver(0),
      DispatchTimeout(1),
      CancelByOperator(2),
      CancelByCustomer(3),
      OrderEdited(4),
      Redispatch(5);
      
      private final int nValue;
      RejectReason(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
    	private static TreeMap<Integer, RejectReason> _map;
  	   static {
  	 	_map = new TreeMap<Integer, RejectReason>();
  	     for (RejectReason num: RejectReason.values()) {
  	     	_map.put(new Integer(num.Value()), num);
  	     }
  	   } 	   
  	   public static RejectReason lookup(int value) {
  	 	  return _map.get(new Integer(value));
  	   }
  }
  public enum DBMaxLength
  {            
      Message(200),
      PhoneNumber(20),
      DriverName(50);
      
      private final int nValue;
      DBMaxLength(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
  }

  public enum EConvertNewLine
  {
      ToWhiteSpace(0),
      ToText(1);
      
      private final int nValue;
      EConvertNewLine(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
  }
  
  public enum EScreenSaverState
  {
      Waiting(0), // waiting for the application to load up
      Disabled(1), // screen saver is not activated because something was pressed
      Ready(2), // screen saver is ready to be activated (will be evaluated by a timer)
      Enabled(3); // screen saver has been activated), main application is hidden
      
      private final int nValue;
      EScreenSaverState(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
  }

  public enum EMDTLockMode
  {
      Unlocked(0),
      Locked(1),
      FakeLock(2);
      
      private final int nValue;
      EMDTLockMode(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
    	private static TreeMap<Integer, EMDTLockMode> _map;
 	   static {
 	 	_map = new TreeMap<Integer, EMDTLockMode>();
 	     for (EMDTLockMode num: EMDTLockMode.values()) {
 	     	_map.put(new Integer(num.Value()), num);
 	     }
 	   } 	   
 	   public static EMDTLockMode lookup(int value) {
 	 	  return _map.get(new Integer(value));
 	   }
  }
  
  public enum MessageLevel
  {
      Info(0),
      Warning(1),
      Error(2),
      Inquiry(3);
      
      private final int nValue;
      MessageLevel(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
  };
  public enum InquiryType
  {
      Restart(1),
      CallIn(2),
      UpdateApp(3),
      None(4);
      
      private final int nValue;
      InquiryType(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
  };	
  
  public enum DateTimeFormat
  {
      DDMMYYYY,
      DDMMYYYY2,
      YYYYDDMM,
      YYYYMMDD,      
      YYYYMMDD_Date,
      DDMMYYYY_Date,
      MMDDYYYY,
      HHMM,
      HHMMSS,
      FileName,      
      FileExplorer,      
  }
  
  public enum ECallStatus
  {
      Idle(0),
      Accepting(1),
      Online(2),
      Roaming(3), // because we don't know the exact status of the dial --> only for Make_Call
      Rejecting(4),
      Calling(5),
      Failed(6);
      
      private final int nValue;
      ECallStatus(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
  }
  
  public enum EFileRequestActionType
  {
      List(0),
      Delete(1),
      Rename(2),
      Copy(3),
      Move(4),
      NewFolder(5),
      Download(6),
      Upload(7);
      
      private final int nValue;
      EFileRequestActionType(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
    	private static TreeMap<Integer, EFileRequestActionType> _map;
 	   static {
 	 	_map = new TreeMap<Integer, EFileRequestActionType>();
 	     for (EFileRequestActionType num: EFileRequestActionType.values()) {
 	     	_map.put(new Integer(num.Value()), num);
 	     }
 	   } 	   
 	   public static EFileRequestActionType lookup(int value) {
 	 	  return _map.get(new Integer(value));
 	   }
  }
  
  public enum EParseResult
  {
      NotFound,
      Found,
      Incomplete
  }
  
  public enum DLStatus
  {
      Ready,
      Downloading,
      //Pending, // happens when there is another download of utmost importance
      Downloaded
  }
  
  public enum EScriptAction
  {
  	ConnectGPRS,
  	ConnectToServer,
  	LoginToICC,
  	DisconnectFromServer,
  	DisconnectGPRS
  }
  
  public enum ESocketState
  {       
  	ConnMgrNotConnected,  // default        
  	ConnMgrConnecting,
  	ConnMgrConnected,       // connected to connMgr, but not connected to gprs yet
  	GPRSConnecting,
  	GPRSConnected,
  	GPRSDisconnecting,            
  	GPRSDisconnected,       // DON'T use this value if m_settings.ConnMgr="WinCE"
  	iCCConnecting,
  	iCCConnected,
  	iCCLoggingIn,
  	iCCLoggedOn,
  	iCCLoginFailed,
  	NoResult
  }
  
  public enum EngineEventTarget
  {
  	Any(0),
  	ActiveOnly(1),
  	Parent(2),
  	ActMain(3),
  	ActSMS(4),
  	ActSMSGroup(5),
  	ActCreateSMS(6);
  	
  	private final int nValue;
  	EngineEventTarget(int Value)
  	{
  		this.nValue = Value;
  	}
  	public int Value()
  	{
  		return nValue;
  	}
  	private static TreeMap<Integer, EngineEventTarget> _map;
	   static {
	 	_map = new TreeMap<Integer, EngineEventTarget>();
	     for (EngineEventTarget num: EngineEventTarget.values()) {
	     	_map.put(new Integer(num.Value()), num);
	     }
	   } 	   
	   public static EngineEventTarget lookup(int value) {
	 	  return _map.get(new Integer(value));
	   }
  }
  public enum EngineEventType
  {
  	Broadcast	
  }
  
  public enum XMPPEventType
  {
  	OnConnected,  	
		onNewMessage,
		onContactListUpdate,
		onPendingListUpdate,
		onNewRecipient,
		onNotification,
		onDisconnected
  }
  
  public enum EViewMode
  {
      MainPanel,
      MsgPanel,
      SystemPanel,
      HideAll,
      ShowGosmore,
      BlackScreen,
      LoginBox,

      // forms
      FrmHelp,
      FrmJobHistory,
      FrmJobStatus,
      FrmJobStatusDetail,
      FrmMessageHistory,
      FrmSendMessage,
      FrmListOrder,
      FrmStatistic,
      FrmVolume
  };
  
  public enum ETimer
  {  		
      ShowLogin,
      ShowLockMDT,
      ShowBTSettings,
      
      ShowLoginAndConnect,  
      ShowMessage,
      ShowWaitDriverIn,      
      ShowLockMDTStartup,
      SetupGUI,
      LastGPSPos,
      AutoPing,
      GPSMode1,
      Clock,      
      AddMessageDriverInError,
      BatteryPower,
      GPSDataAutoSave,
      DisableGPS,
      Tester,
      Fullscreen
  }
  
  public enum ELabelType
  {
  	Title,
  	TitleWithVersion,
  	TitleWithClock,
  	Body,
  	Clock,
  	Version
  }
  
  public enum ELoginDisplayMode
  {
      LoginDriverId(0),
      LoginPIN(1),
      LogoutPIN(2),
      PhoneNumber(3),
      UnlockMDT(4),

      CIF_AskTotalKm(5),
      CIF_AskTotalGas(6),
      CIF_AskTotalArgoRp(7),
      CIF_AskTotalArgoKm(8);
      
      private final int nValue;
      ELoginDisplayMode(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
  }
  
  public enum MsgButton
  {
      Yes,
      No,
      Cancel,
      OK,
      Close,
      Send
  }
  public static String[] ksMonthsINA = { "", "Jan", "Feb", "Mar", "Apr", "Mei", "Juni", "Juli", "Agustus", "September", "Okt", "November", "Desember" };
  public static String[] ksMonthsEN = { "",  "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
  public static String[] ksDaysEN = {"","Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
  public static String[] ksDaysINA = {"","Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab" };
  
  public enum ESetupGUI
  {
      Step1,
      Step2,      
      End
  }
  
  public enum EMainBarMode
  {
      FirstMenu,
      SecondMenu,
      ThirdMenu,
      SpeakMenu
  }
  public enum ESocketError
  {
  	NoError,
  	ClosedNormally,
  	ClosedForcefully
  }
  public enum EConnectionResult
  {
  	Success,
  	Failed,
  	Disconnected
  }
  public enum EKeyboardMode
  {
      EKM_ALPHA,        
      EKM_NUM,
      EKM_SYMBOL
  }
  public enum StatisticState
  {
      Idle,
      Empty,
      WaitStats,
      ShowStats        
  }
  public enum StatisticTab
  {
      PhoneOrder,
      StreetOrder
  }
  
  public enum ListOrderState
  {
      Idle,        
      Empty,
      WaitListOrder,
      ShowOrder,
      WaitBookOrder
  }
  public enum ConfirmActPurpose
  {
  	Unknown(0),
  	NewJob(1),
  	AtCustomer(2),
  	NoShow(3),
  	CancelJob(4),
  	StartJob(5),
  	FinishJob(6),
  	CheckIn(7),
  	BidOrder(8),
  	DriverIn(9),
  	DriverPhoto(10),
  	VoiceRequest(11),
  	eKTA_Register(12),
  	Restart(13),
  	SyncData(14),
  	//SyncProductData(14),
  	//SyncGPSData(15),
  	//SyncAudioData(16),
  	DisplayShopMenu(17),
  	DisplayShopStatus(18),
  	DisplayPayFormBG(19),
  	DisplayPayFormTrans(20),
  	CloseForm(21),
  	DisplayProductSearch(22),
  	CalcOrderLineQty(23),
  	CalcOrderLineUOM(24),
  	DisplayPaymentForm(25),
  	PaymentAuthorized(26),
  	CustomerIDOK(27),
  	SetDateTime(28),
  	PumaProcessPayment(29),
  	PumaCancelPayment(30),  	
  	PumaFinishOrder(31),
  	PumaKonfirmOrder(32),
  	PumaCancelOrder(33),
  	PumaDeleteOrderLine(34),
  	PumaUpdateCustomerInfo(35),
  	PumaProcessSetoranTunai(36),
  	PumaEndVisit(37),
  	PumaUpdateTotalPayment(38),  	
  	PumaProcessBGCekHandover(39),
  	PumaProcessTransferHandover(40),
  	PumaProcessKliring(41),  	
  	PumaCancelHandoverKliring(42),  	
  	ConnectWifiAPN(43),
  	RunUpdater(44),
  	BarcodeAuthorized(45),
  	PumaCreateNewOrder(46),
  	PumaUpdateOrderList(47),
  	DisplayOfflinePaymentForm(48);  	
  	//CloseProductSearch(26);
  	
  	private final int nValue;
  	ConfirmActPurpose(int Value)
  	{
  		this.nValue = Value;
  	}
  	public int Value()
  	{
  		return nValue;
  	}
  	private static TreeMap<Integer, ConfirmActPurpose> _map;
	   static {
	 	_map = new TreeMap<Integer, ConfirmActPurpose>();
	     for (ConfirmActPurpose num: ConfirmActPurpose.values()) {
	     	_map.put(new Integer(num.Value()), num);
	     }
	   } 	   
	   public static ConfirmActPurpose lookup(int value) {
	 	  return _map.get(new Integer(value));
	   }
  }
  public enum VerifyLoginResult
  {
      OK,
      DriverNotFound,
      PINIncorrect,
      UserNotAllowed,
      Error
  }
  public enum DictManagerMode
  {
      GetIndex,
      GetString,
      GetZone,
      GetShortcut
  }
  public enum WaitActPurpose
  {
  	Unknown(0),
  	DriverIn(1),
  	AcceptDispatch(2),
  	WaitProcess_SendMessage(3),
  	WaitProcess_StartJob(4),
  	WaitProcess_FinishJob(5),
  	WaitProcess_NoShowJob(6),
  	WaitProcess_AtCustomerJob(7),
  	WaitProcess_LocalLogin(8),
  	WaitProcess_SyncData(9);
  	//WaitProcess_SyncProductData(9),
  	//WaitProcess_SyncGPSData(10),
  	//WaitProcess_SyncAudioData(11);
  	
  	private final int nValue;
  	WaitActPurpose(int Value)
  	{
  		this.nValue = Value;
  	}
  	public int Value()
  	{
  		return nValue;
  	}
  	private static TreeMap<Integer, WaitActPurpose> _map;
	   static {
	 	_map = new TreeMap<Integer, WaitActPurpose>();
	     for (WaitActPurpose num: WaitActPurpose.values()) {
	     	_map.put(new Integer(num.Value()), num);
	     }
	   } 	   
	   public static WaitActPurpose lookup(int value) {
	 	  return _map.get(new Integer(value));
	   }
  }
  
  public enum ReasonMode
  {
      Reject(0),
      Cancel(1),
      CCNumber(2);   
      
      private final int nValue;
      ReasonMode(int Value)
    	{
    		this.nValue = Value;
    	}
    	public int Value()
    	{
    		return nValue;
    	}
    	private static TreeMap<Integer, ReasonMode> _map;
  	   static {
  	 	_map = new TreeMap<Integer, ReasonMode>();
  	     for (ReasonMode num: ReasonMode.values()) {
  	     	_map.put(new Integer(num.Value()), num);
  	     }
  	   } 	   
  	   public static ReasonMode lookup(int value) {
  	 	  return _map.get(new Integer(value));
  	   }
  }
  
  public enum RequestVoiceResult
  {
  	SendMessage,
  	SendSMSDisplayCC,
  	SendSMSDontDisplayCC  	
  }
  
  public enum GPSMode
  {
  	TimeBased(1),
  	AngleDistanceBased(2),
  	SpeedBased(3);
  	
  	private final int nValue;
  	GPSMode(int Value)
  	{
  		this.nValue = Value;
  	}
  	public int Value()
  	{
  		return nValue;
  	}
  	private static TreeMap<Integer, GPSMode> _map;
	   static {
	 	_map = new TreeMap<Integer, GPSMode>();
	     for (GPSMode num: GPSMode.values()) {
	     	_map.put(new Integer(num.Value()), num);
	     }
	   } 	   
	   public static GPSMode lookup(int value) {
	 	  return _map.get(new Integer(value));
	   }
  }
  
  public enum StatusVehicle
  {
    Stop,
    Move
  };
  
  public enum iCCMenuType
  {
    HorizontalList,
    ScrollTable
  };
  
  public enum EMediaRecMode
	{
		Audio(0),
		Video(1);
		
		private final int nValue;
		EMediaRecMode(int Value)
  	{
  		this.nValue = Value;
  	}
  	public int Value()
  	{
  		return nValue;
  	}
  	private static TreeMap<Integer, EMediaRecMode> _map;
	   static {
	 	_map = new TreeMap<Integer, EMediaRecMode>();
	     for (EMediaRecMode num: EMediaRecMode.values()) {
	     	_map.put(new Integer(num.Value()), num);
	     }
	   } 	   
	   public static EMediaRecMode lookup(int value) {
	 	  return _map.get(new Integer(value));
	   }
	};
	
	public enum EHTTPRequestType
	{						
		MemberAdd(0),
		MemberList(1),
		MemberSearch(2),
		MemberInfo(3),
		MemberCheckIDNumber(4),
		GetDateTime(5),
		NewsList(6),
		NewsDetail(7),
		DeviceData(8),
		LogGPS(9),
		SalesRep(10),
		CustCredit1(11),
		CustCredit2(12),
		CustInvoice1(13),
		CustInvoice2(14),
		PumaProduct(15),
		PumaProductByName(16),
		InsertOrder(17),
		InsertOrderLine(18), 
		UpdateTotalOrderLine(19), 
		PumaCancelOrder(20), // REPLACE
		PumaConfirmOrder(21), // REPLACE
		//PumaUOM(22),
		PumaUOMConversion(23),		
		PumaOrderLines(24), // finish order part 2
		PumaGetDocNoErp(25),
		PumaUpdateStatusLimit(26), // set order_status=C, processed=N
		PumaGetOrderStatus(27),
		PumaUpdateOrderLine(28),
		PumaSimulateOrderStatus(29),
		PumaSimulateOrderLine(30),
		PumaSimulateInsertERPNo(31),
		PumaGetProperUOM(32),
		PumaInsertPayment(33),
		PumaInsertPaymentInvoice(34),
		PumaBankCode(35),
		PumaInsertBankDeposit(36),
		PumaUpdatePayment1(37),
		PumaUpdatePayment2(37),
		PumaGetPaymentList(38),
		PumaSearchOrderHistory(39),
		PumaGetPaidInvoice(40),
		PumaUpdateBankDeposit(41),
		PumaBGInsertBankDeposit(42),
		PumaBGUpdatePayment1(43),
		PumaBGUpdatePayment2(44),
		PumaTranInsertBankDeposit(45),
		PumaTranUpdateBankDeposit(46),
		PumaTranUpdatePayment1(47),
		PumaTranUpdatePayment2(48),
		PumaGetBankDepositNull(49),
		PumaUpdateBankDepositBGKliring(50),
		PumaProductByCode(51),
		PumaSearchPaymentHistory(52),
		PumaSearchDepositHistory(53),
		PumaUpdatePaymentKas(54),
		PumaSalesRepInfo(55),
		PumaBankDepositGetNoERP(56),
		PumaGetPayment(57),
		PumaDeleteOrderLine(58),		
		PumaUpdateOrderLineToDelete(59),
		PumaDeleteOrderPerLine(60),
		PumaUpdateOrderLineToDeleteAll(61),
		PumaDeleteAllOrderLine(62),
		PumaCountProduct(63),
		PumaSearchOrderDetailHistory(64),
		PumaSearchPayAllocHistory(65),
		PumaGetServerDateTime(66),
		PumaGetLastNoDoc(67),
		PumaGetIPList(68),
		PumaCheckSPV(69),
		PumaListSales(70),
		PumaUpdateOrderForSalesId(71),
		PumaGetDeviceInfoList(72),
		PumaSearchOrderStatus(73),
		PumaUpdateProcessedOrder(74),
		PumaProductById(75),		
		PumaInsertDeviceInfoList(76),
		PumaActivityLog(77),
		PumaSearchActivityLog(78),
		
		PumaSyncSalesRep(90),
		PumaSyncCustCredit(91),
		PumaSyncInvoice(92),
		PumaSyncCatalog(93),
		PumaSyncProduct(94),
		PumaSyncStock(95),
		PumaSyncUOM(96);
		
		private final int nValue;
		EHTTPRequestType(int Value)
  	{
  		this.nValue = Value;
  	}
  	public int Value()
  	{
  		return nValue;
  	}
  	private static TreeMap<Integer, EHTTPRequestType> _map;
	   static {
	 	_map = new TreeMap<Integer, EHTTPRequestType>();
	     for (EHTTPRequestType num: EHTTPRequestType.values()) {
	     	_map.put(new Integer(num.Value()), num);
	     }
	   } 	   
	   public static EHTTPRequestType lookup(int value) {
	 	  return _map.get(new Integer(value));
	   }
	}
	
	public enum EMapLibrary
	{		
		GoogleMap,
		MGMap,
		Mapsforge
	}
	
	public enum ERouteType
	{
		Pickup(0),
		Destination(1),
		Address(2);
		
		private final int nValue;
		ERouteType(int Value)
  	{
  		this.nValue = Value;
  	}
  	public int Value()
  	{
  		return nValue;
  	}
  	private static TreeMap<Integer, ERouteType> _map;
	   static {
	 	_map = new TreeMap<Integer, ERouteType>();
	     for (ERouteType num: ERouteType.values()) {
	     	_map.put(new Integer(num.Value()), num);
	     }
	   } 	   
	   public static ERouteType lookup(int value) {
	 	  return _map.get(new Integer(value));
	   }
	}
	
	public enum EXMPPConnResult
	{		
		ConnectError,
		LoginError,
		LoginOK
	}
	
	/*public enum EPaymentMethod
	{		
		Cash,
		BliyetGiro,
		Transfer
	}*/
			
	public enum ESequentialID
	{		
		SalesOrder,
		Payment,
		BankDeposit,
		BGCekHandover,
		BuktiTransferHandover,
		BankKliring
	}
	
	public enum EHistoryMode
	{
		OrderBySalesRepId(0),
		OrderByCustomerId(1),
		PaymentBySalesRepId(2),
		PaymentByCustomerId(3),
		Deposit(4),
		DepotSync(5),
		SystemUpdate(6),
		Activity(7);
		
		private final int nValue;
		EHistoryMode(int Value)
  	{
  		this.nValue = Value;
  	}
  	public int Value()
  	{
  		return nValue;
  	}
  	private static TreeMap<Integer, EHistoryMode> _map;
	   static {
	 	_map = new TreeMap<Integer, EHistoryMode>();
	     for (EHistoryMode num: EHistoryMode.values()) {
	     	_map.put(new Integer(num.Value()), num);
	     }
	   } 	   
	   public static EHistoryMode lookup(int value) {
	 	  return _map.get(new Integer(value));
	   }
	}
	
	public enum EDepositType
	{		
		BGKliring,
		Transfer,
		Kas,
		Null
	}
	
	public enum BTStatus
	{		
		BTClientConnected,
		BTClientNotConnected,
		BTServerConnected,
		BTServerNotConnected
	}
	
	public enum EAuthorizationMode
	{		
		Payment,
		Barcode		
	}
	
	public enum EPumaQueryResult
	{		
		// 0 = connection error, 1 = query error, 2 = query OK, 3 = query no result (but success)
		ConnectionError(0),
		QueryError(1),
		QueryOK(2),
		NoResult(3),
		Illegal(4),
		InsertFailed(5);
		
		private final int nValue;
		EPumaQueryResult(int Value)
  	{
  		this.nValue = Value;
  	}
  	public int Value()
  	{
  		return nValue;
  	}
  	private static TreeMap<Integer, EPumaQueryResult> _map;
	   static {
	 	_map = new TreeMap<Integer, EPumaQueryResult>();
	     for (EPumaQueryResult num: EPumaQueryResult.values()) {
	     	_map.put(new Integer(num.Value()), num);
	     }
	   } 	   
	   public static EPumaQueryResult lookup(int value) {
	 	  return _map.get(new Integer(value));
	   }
	}
	
	public enum ESMSStatus
	{
		Ready,
		Sending,
		SendOK,
		SendError,
		Delivered,		
		DeliveryError,
		Completed
	}
	
	public enum ESMSReceiver
	{
		SMS_Sent,
		SMS_Delivered
	}
  
  public static final int FLAG_ACTIVITY_REORDER_TO_FRONT = 131072;
  public static final int CALL_STATE_IDLE = 0;
	public static final int CALL_STATE_OFFHOOK = 2;
	public static final int CALL_STATE_RINGING = 1;
	public static final int GPS_EVENT_FIRST_FIX = 3;
	public static final int GPS_EVENT_SATELLITE_STATUS = 4;
	public static final int GPS_EVENT_STARTED = 1;
	public static final int GPS_EVENT_STOPPED = 2;
	
	//Message types sent from the BluetoothChatService Handler
  public static final int MESSAGE_STATE_CHANGE = 1;
  public static final int MESSAGE_READ = 2;
  public static final int MESSAGE_WRITE = 3;
  public static final int MESSAGE_DEVICE_NAME = 4;
  public static final int MESSAGE_DEVICE_ADDRESS = 5;
  public static final int MESSAGE_TOAST = 6;
  public static final int MESSAGE_BT_CONN_FAILED = 7;
  public static final int MESSAGE_BT_CONN_LOST = 8;
  
  // Key names received from the BluetoothCommandService Handler
  public static final String DEVICE_NAME = "device_name";
  public static final String TOAST = "toast";
  
  // mgm maps
  public static final int ROUTING_CLOUDMADE = 0;
  public static final int ROUTING_YOURNAVIGATION = 1;
  
  // battery
  public static final int POWER_BY_BATTERY = 0;
  public static final int POWER_BY_AC_LINE = 1;
  public static final int POWER_BY_USB = 2;
  
  // puma thread retry
  public static final int PUMA_THREAD_RETRY = 2;
}
