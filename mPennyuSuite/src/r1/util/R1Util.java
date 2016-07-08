package r1.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.lang3.ArrayUtils;

import r1.util.iCCConstants.DateTimeFormat;
import r1.util.iCCConstants.ESequentialID;
import r1.util.iCCConstants.MDTFreeQueryCommands;
import r1.util.iCCConstants.ServerFreeQueryCommands;
import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class R1Util 
{
	public static TimeOffset m_offset = new TimeOffset(0, 0);
	private static Lock m_lock = new ReentrantLock();
	
	enum eDeleteMode
	{
		DELETE_PART,
		DELETE_SPACE
	}
	
	private static final Pattern DIR_SEPORATOR = Pattern.compile("/");
	
	public static void SetAlarm(Context mContext, int nTimeInSecond, Class <?>cls) 
	{ 		
    Intent intent = new Intent(mContext, cls);
    PendingIntent sender = PendingIntent.getBroadcast(mContext,0, intent, 0);
    
    AlarmManager am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
    am.cancel(sender);
    
    if (nTimeInSecond > 0)
    	am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + nTimeInSecond*1000, sender);
	}
	
	public static String GetAppVersion(Context m_ctx, String sPackageName)
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
	
	public static String bytesToHex(byte[] bytes) {
	    StringBuilder sbuf = new StringBuilder();
	    for(int idx=0; idx < bytes.length; idx++) {
	        int intVal = bytes[idx] & 0xff;
	        if (intVal < 0x10) sbuf.append("0");
	        sbuf.append(Integer.toHexString(intVal).toUpperCase());
	    }
	    return sbuf.toString();
	}
	
	/**
	 * Get utf8 byte array.
	 * @param str
	 * @return  array of NULL if error was found
	 */
	public static byte[] getUTF8Bytes(String str) {
	    try { return str.getBytes("UTF-8"); } catch (Exception ex) { return null; }
	}

	/**
	 * Load UTF8withBOM or any ansi text file.
	 * @param filename
	 * @return  
	 * @throws java.io.IOException
	 */
	public static String loadFileAsString(String filename) throws java.io.IOException 
	{
    final int BUFLEN=1024;
    BufferedInputStream is = new BufferedInputStream(new FileInputStream(filename), BUFLEN);
    try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFLEN);
        byte[] bytes = new byte[BUFLEN];
        boolean isUTF8=false;
        int read,count=0;           
        while((read=is.read(bytes)) != -1) {
            if (count==0 && bytes[0]==(byte)0xEF && bytes[1]==(byte)0xBB && bytes[2]==(byte)0xBF ) {
                isUTF8=true;
                baos.write(bytes, 3, read-3); // drop UTF8 bom marker
            } else {
                baos.write(bytes, 0, read);
            }
            count+=read;
        }
        return isUTF8 ? new String(baos.toByteArray(), "UTF-8") : new String(baos.toByteArray());
    } finally {
        try{ is.close(); } catch(Exception ex){} 
    }
	}

	/**
	 * Returns MAC address of the given interface name.
	 * @param interfaceName eth0, wlan0 or NULL=use first interface 
	 * @return  mac address or empty string
	 */
	public static String getMACAddress(String interfaceName) 
	{
    try {
        List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface intf : interfaces) {
            if (interfaceName != null) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
            }
            byte[] mac = intf.getHardwareAddress();
            if (mac==null) return "";
            StringBuilder buf = new StringBuilder();
            for (int idx=0; idx<mac.length; idx++)
                buf.append(String.format("%02X:", mac[idx]));       
            if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
            return buf.toString();
        }
    } catch (Exception ex) { } // for now eat exceptions
    return "";
    /*try {
        // this is so Linux hack
        return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
    } catch (IOException ex) {
        return null;
    }*/
	}

/**
 * Get IP address from first non-localhost interface
 * @param ipv4  true=return ipv4, false=return ipv6
 * @return  address or empty string
 */
	public static String getIPAddress(boolean useIPv4) 
	{
    try {
        List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface intf : interfaces) {
            List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
            for (InetAddress addr : addrs) {
                if (!addr.isLoopbackAddress()) {
                    String sAddr = addr.getHostAddress();
                    //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                    boolean isIPv4 = sAddr.indexOf(':')<0;

                    if (useIPv4) {
                        if (isIPv4) 
                            return sAddr;
                    } else {
                        if (!isIPv4) {
                            int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                            return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                        }
                    }
                }
            }
        }
    } catch (Exception ex) { } // for now eat exceptions
    return "";
	}
			
	public static String GetDLFolder()
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
	}
	
	public static boolean RunApp(Context ctx, String sPackage, String sLauncherAct)
  { 
		boolean bOK = false;
		/*if (sPackage.equalsIgnoreCase("r1.puma"))
		{
			// send intent
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction("PUMA_ACTIVATE_MAIN_MENU");
			ctx.sendBroadcast(broadcastIntent);
		}
		else
		{*/		
	  	try
	 		{
	  		// FIRST METHOD
				/*Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.setComponent(new ComponentName(sPackage, sLauncherAct));
				ctx.startActivity(intent);*/
	  			
				// SECOND METHOD
	 			Intent LaunchIntent = ctx.getPackageManager().getLaunchIntentForPackage(sPackage);
	 			if (LaunchIntent == null)
	 				Log.v("AppUpdater", "Package not found");
	 			else
	 			{
	 				//LaunchIntent.putExtra("Email", sExtra);
	 				ctx.startActivity(LaunchIntent);
	 			}
				
				bOK = true;
	 		}
			catch (Exception ex)
			{
				bOK = false;
				//AddLog("Exception: Package not found: " + sPackage + "\n");
				//Log.v("QSMS", "Exception: Package not found");					
			}
		//}
  	
  	return bOK;
 	}
	
	public static boolean IsAppInstalled(Context ctx, String packageName) 
	{
    PackageManager pm = ctx.getPackageManager();
    boolean installed = false;
    try 
    {
       pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
       installed = true;
    } 
    catch (PackageManager.NameNotFoundException e) 
    {
       installed = false;
    }
    return installed;
	}
	
	public static boolean ReverseObfuscateFile(String sSrc, String sDest)
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
	}
	
	public static void GetContacts(Context ctx, List<AllContacts> arrContacts)
  {
  	ContentResolver cr = ctx.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
		if (cur.getCount() > 0) 
		{
	    while (cur.moveToNext()) 
	    {
        String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
        String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
        {
          Cursor pCur = cr.query(
                     ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                     null,
                     ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                     new String[]{id}, null);
          
          while (pCur.moveToNext()) 
          {
            String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            
            AllContacts ac = new AllContacts();
            ac.setsPhoneNo(phoneNo);
    				ac.setsName(name);
    				arrContacts.add(ac);
    				
            //Toast.makeText(NativeContentProvider.this, "Name: " + name + ", Phone No: " + phoneNo, Toast.LENGTH_SHORT).show();
          }
          pCur.close();
        }
	    }
		}
  }
	
	public static int ReadSMS(Context ctx, List<SMSInfo> arrSMS)
	{
		//[0: _id,1: thread_id,2: address,3: person,4: date,5: protocol,6: read,7: status,8: type,9: reply_path_present,
		//10: subject,11: body,12: service_center,13: locked,14: error_code,15: seen]
    Uri smsUri = Uri.parse("content://sms");
    int count = 0;
    try
    {
	    //Cursor c = context.getContentResolver().query(deleteUri, null, null, null, null);
    	Cursor c = ctx.getContentResolver().query(smsUri, null, null, null, null);
	    while (c.moveToNext()) 
	    {      
	    	// print	    	
	    	String str = String.format("SMS: %s, %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
	    			c.getColumnCount(),
	    			c.getString(0), c.getString(1), c.getString(2), c.getString(3), 
	    			c.getString(4), c.getString(5), c.getString(6), c.getString(7),
	    			c.getString(8), c.getString(9), c.getString(10), c.getString(11),
	    			c.getString(12), c.getString(13), c.getString(14), c.getString(15)	    			
	    			);
	    	Log.v("SMSInfo", str);	    	
	    	
	    	SMSInfo sms = new SMSInfo();
	    	sms.setsAddress(c.getString(2));
	    	sms.setsDateTime(c.getString(4));
	    	sms.setsRead(c.getString(6));
	    	sms.setsType(c.getString(8));
	    	sms.setsMessage(c.getString(11));
	    	sms.setsServiceCenter(c.getString(12));
	    	sms.setsSeen(c.getString(15));
	    	arrSMS.add(sms);
	    }	    
    }
    catch (SecurityException e)
    {
    	Log.e("SMSInfo", "Read SMS exception: " + e.getMessage());
    }
    catch (Exception e) 
    {
    	Log.e("SMSInfo", "Read SMS exception: " + e.getMessage());
    }
    
    //Toast.makeText(context, "SMS checked", Toast.LENGTH_SHORT).show();
    
    return count;
	}
	
	public static String GetWifiInfo(Context ctx, List<ScanResult> arrAPN, ConnectionStatus cs)
	{
		String sInfo = "";
		
		try
		{
			ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo Info = cm.getActiveNetworkInfo();
      if (!Info.isConnectedOrConnecting())
      	cs.setnConnType(-1);
      else
      	cs.setnConnType(Info.getType()); // ConnectivityManager.TYPE_WIFI OR ConnectivityManager.MOBILE
      
			WifiManager wm = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);				
			if (wm.isWifiEnabled())
			{
				sInfo = "Wifi: On";								
													      
	      cs.setsWifiAPN(wm.getConnectionInfo().getSSID());
	      cs.setnWifiAPNSignalStrength(wm.getConnectionInfo().getRssi());
	      cs.setnWifiStatus(wm.getWifiState());	      	      
	      
	      List<ScanResult> sr = wm.getScanResults();
	    	arrAPN.clear();
	      for (ScanResult res: sr)	    	   		      	      	      
	      	arrAPN.add(res);
			}
			else
				sInfo = "Wifi: Off";						
		}
		catch (Exception ex)
		{}
		
		return sInfo;
	}
	
	public static String GetDeviceID(Context ctx, DeviceID dev)
	{
		String m_szLongID = "";
		
		try
		{
			//TelephonyManager TelephonyMgr = (TelephonyManager)m_ctx.getSystemService(m_ctx.TELEPHONY_SERVICE);
			TelephonyManager TelephonyMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
			String m_szImei = TelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE	
			dev.setsImei(m_szImei);
			
			String m_szDevIDShort = "35" + //we make this look like a valid IMEI
	      	Build.BOARD.length()%10+ Build.BRAND.length()%10 + 
	      	Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 + 
	      	Build.DISPLAY.length()%10 + Build.HOST.length()%10 + 
	      	Build.ID.length()%10 + Build.MANUFACTURER.length()%10 + 
	      	Build.MODEL.length()%10 + Build.PRODUCT.length()%10 + 
	      	Build.TAGS.length()%10 + Build.TYPE.length()%10 + 
	      	Build.USER.length()%10 ; //13 digits
			dev.setsDeviceID(m_szDevIDShort);
			
			String m_szAndroidID = Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID);
			dev.setsDeviceID(m_szAndroidID);
			
			String m_szWLANMAC = null;
			/*try
			{
				WifiManager wm = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);				
				m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
				dev.setsWLANMAC(m_szWLANMAC);
			}
			catch (Exception ex)
			{}*/
			
			String m_szBTMAC = null;
			/*try
			{
				BluetoothAdapter m_BluetoothAdapter	= null; // Local Bluetooth adapter
				m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				m_szBTMAC = m_BluetoothAdapter.getAddress();
				dev.setsBTMAC(m_szBTMAC);
			}
			catch (Exception ex)
			{}*/
	  	  	  	
	  	m_szLongID = m_szImei + "|" + m_szDevIDShort + "|" + m_szAndroidID+
	  			(m_szWLANMAC == null ? "" : "|" + m_szWLANMAC) +
	  			(m_szBTMAC == null ? "" : "|" + m_szBTMAC);
		}
		catch (Exception ex)
		{}
  	  	  	
  	//return GetShortMD5(m_szLongID);  	
  	return m_szLongID;
	}
	
	public static boolean IsPackageExist(Context ctx, String sPackageName)
	{
		/*List<PackageInfo> arrInf = ctx.getPackageManager().getInstalledPackages(0);
		for (PackageInfo inf: arrInf)
		{
			if (inf.packageName.equalsIgnoreCase(sPackageName))
				return true;
		}*/
		
		try 
		{
			PackageInfo pInfo;
			pInfo = ctx.getPackageManager().getPackageInfo(sPackageName, 0);
			String str = pInfo.versionName;
			
			return true;
		}
		catch (Exception e) 
		{
			return false;
		}				
	}
			
	
	public static boolean KillProcess(Context ctx, String sProcess)
	{
		try
		{
			if (IsProcessRunning(ctx, sProcess))
			{
				ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
				activityManager.killBackgroundProcesses(sProcess);
			}
		}
		catch (Exception ex)
		{}

	  return false;
	}
	
	public static boolean IsProcessRunning(Context ctx, String sProcess)
	{
		try
		{
		  ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		  List<RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
		  for(int i = 0; i < procInfos.size(); i++)
		  {
		  	if(procInfos.get(i).processName.equals(sProcess)) 	      
		  		return true;	       		  	
		  }
		}
		catch (Exception ex)
		{}

	  return false;
	}
	
	public static synchronized void SaveEncryptedSettings(INIFile m_settings, String m_sINIPath)
	{		
		m_lock.lock();
		
		try 
		{
			// save to temp
			m_settings.save();
			Thread.sleep(100);
			
			// read settings
			String sPlain = R1Util.ReadFile(m_settings.getFileName());
						
			// encrypt settings							
			String sCipher = R1Util.DESEnryption(true, sPlain);
			
			// save cipher
			R1Util.SaveFile(m_sINIPath, sCipher, false);
				
			// delete temp file
			Thread.sleep(100);
			R1Util.DeleteDir(new File(m_settings.getFileName()));			
		}
		catch (Exception ex)
		{}
		
		m_lock.unlock();
	}		
	
	public static synchronized INIFile ReadEncryptedSettings(Context m_ctx, String m_sINIPath)
	{		
		m_lock.lock();
		
		INIFile m_settings = null;
		try 
		{	
			// decrypt settings		
			String sCipher = R1Util.ReadFile(m_sINIPath);			
			String sPlain = R1Util.DESEnryption(false, sCipher);
			
			// save to a temp			
			String sDest = String.format("%s/msa/Log/%s.temp", m_ctx.getFilesDir().getPath(), "" + Calendar.getInstance().getTimeInMillis());
			R1Util.SaveFile(sDest, sPlain.trim(), false);
			
			// load INI
			Thread.sleep(100);			
			m_settings = new INIFile(sDest);			
			
			// delete temp file
			R1Util.DeleteDir(new File(m_settings.getFileName()));
			Thread.sleep(100);
		}
		catch (Exception ex)
		{
			m_settings = null;
		}
		
		m_lock.unlock();
		
		return m_settings;
	}
	
	public static boolean SaveFile(String sFile, String sData, boolean bAppend)
	{
		boolean bOK = false;
		
		try
		{
			FileOutputStream fos = new FileOutputStream(sFile, bAppend);			      			     
	    fos.write(sData.getBytes());
	    fos.flush();
	    fos.close();
	    
	    bOK = true;
		}
		catch (Exception ex)
		{}
		
		return bOK;
	}
			
	public static String DESEnryption(boolean bEncrypt, String sData)
	{
		String sText = "";
	
		try
		{
			// only the first 8 Bytes of the constructor argument are used 
			// as material for generating the keySpec
			DESKeySpec keySpec = new DESKeySpec("EagleEye".getBytes("UTF8")); 
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey key = keyFactory.generateSecret(keySpec);
			//BASE64Encoder base64encoder = new BASE64Encoder();
			//BASE64Decoder base64decoder = new BASE64Decoder();		
			
			if (bEncrypt)
			{
				// ENCODE plainTextPassword String
				byte[] cleartext = sData.getBytes("UTF8");      
			
				Cipher cipher = Cipher.getInstance("DES"); // cipher is not thread safe
				cipher.init(Cipher.ENCRYPT_MODE, key);
				//sText = base64encoder.encode(cipher.doFinal(cleartext));
				sText = Base64.encodeToString(cipher.doFinal(cleartext), Base64.NO_WRAP);
				// now you can store it 
			}
			else
			{	
				// DECODE encryptedPwd String
				//byte[] encrypedPwdBytes = base64decoder.decodeBuffer(sData);
				byte[] encrypedPwdBytes = Base64.decode(sData, Base64.DEFAULT);
			
				Cipher cipher = Cipher.getInstance("DES");// cipher is not thread safe
				cipher.init(Cipher.DECRYPT_MODE, key);
				byte[] plainTextPwdBytes = (cipher.doFinal(encrypedPwdBytes));
				
				sText = new String(plainTextPwdBytes, "UTF8");
			}
		}
		catch (Exception ex)
		{}
		
		return sText;
	}
	
	public static boolean IsServiceRunning(Context ctx, String sProcessName, String sServiceName)
	{		
		/*boolean bRunning = false;
		
		ActivityManager am = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(100);
    String message = null;

    for (int i=0; i<rs.size(); i++) 
    {
      RunningServiceInfo rsi = rs.get(i);
      if (rsi.process.equals(sProcessName) && rsi.service.getClassName().equals(sServiceName))
      {
      	bRunning = true;
      	break;
      }      
      //Log.v("Service", "Process " + rsi.process + " with component " + rsi.service.getClassName());
      //message = message+rsi.process;
    }
    
    return bRunning;*/
		
		try
  	{
	    ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) 
	    {
	      if (sServiceName.equals(service.service.getClassName())) 
	      {
	      	return true;
	      }
	    }
	    return false;
  	}
  	catch (Exception ex)
  	{
  		return false;
  	}
	}
	
	public static void EnableEditText(EditText tb, boolean bEnabled)
	{
		//tb.setEnabled(bEnabled); // WILL CHANGE BG COLOR TO GRAY
		tb.setFocusableInTouchMode(bEnabled);
		tb.setFocusable(bEnabled);
		tb.setKeyListener(null);
		
		/*if (!bEnabled)
			tb.setInputType(InputType.TYPE_NULL);*/
	}

	/**
	 * Raturns all available SD-Cards in the system (include emulated)
	 *
	 * Warning: Hack! Based on Android source code of version 4.3 (API 18)
	 * Because there is no standart way to get it.
	 * TODO: Test on future Android versions 4.4+
	 *
	 * @return paths to all available SD-Cards in the system (include emulated)
	 */
	public static String[] getStorageDirectories()
	{
	    // Final set of paths
	    final Set<String> rv = new HashSet<String>();
	    // Primary physical SD-CARD (not emulated)
	    final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
	    // All Secondary SD-CARDs (all exclude primary) separated by ":"
	    final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
	    // Primary emulated SD-CARD
	    final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
	    if(TextUtils.isEmpty(rawEmulatedStorageTarget))
	    {
	        // Device has physical external storage; use plain paths.
	        if(TextUtils.isEmpty(rawExternalStorage))
	        {
	            // EXTERNAL_STORAGE undefined; falling back to default.
	            rv.add("/storage/sdcard0");
	        }
	        else
	        {
	            rv.add(rawExternalStorage);
	        }
	    }
	    else
	    {
	        // Device has emulated storage; external storage paths should have
	        // userId burned into them.
	        final String rawUserId;
	        //if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
	        if(Build.VERSION.SDK_INT < 16)
	        {
	            rawUserId = "";
	        }
	        else
	        {
	            final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
	            final String[] folders = DIR_SEPORATOR.split(path);
	            final String lastFolder = folders[folders.length - 1];
	            boolean isDigit = false;
	            try
	            {
	                Integer.valueOf(lastFolder);
	                isDigit = true;
	            }
	            catch(NumberFormatException ignored)
	            {
	            }
	            rawUserId = isDigit ? lastFolder : "";
	        }
	        // /storage/emulated/0[1,2,...]
	        if(TextUtils.isEmpty(rawUserId))
	        {
	            rv.add(rawEmulatedStorageTarget);
	        }
	        else
	        {
	            rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
	        }
	    }
	    // Add all secondary storages
	    if(!TextUtils.isEmpty(rawSecondaryStoragesStr))
	    {
	        // All Secondary SD-CARDs splited into array
	        final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
	        Collections.addAll(rv, rawSecondaryStorages);
	    }
	    return rv.toArray(new String[rv.size()]);
	}
	
	public static boolean IsGPSDataValid(double fLat, double fLon)
	{
		if (fLat < -90 || fLat > 90 || fLon < -180 || fLon > 180)
			return false;
		else
			return true;
	}
	
	public static double DoubleValue (String sValue)
	{
		try
		{
			return Double.valueOf(sValue);
		}
		catch (Exception ex)
		{
			return 0;
		}
	}
	public static int IntegerValue (String sValue)
	{
		try
		{
			return Integer.valueOf(sValue);
		}
		catch (Exception ex)
		{
			return 0;
		}
	}
	
	/*public static String FormatMoney(String sText)
	{
		DecimalFormat formatter = new DecimalFormat("#,###");
		try
		{
			return formatter.format(Double.valueOf(sText));
		}
		catch (Exception ex)
		{
			return sText;
		}
	}*/
	/*public static String FormatMoney2(String sText)
	{							
		try
		{
			boolean sMinus = ((Double.valueOf(sText) < 0));
			if (sMinus)
				sText = sText.substring(1);
					
			String[] sArr = sText.split("\\.");
			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			sb2.append(sArr[0]);
			sb2.reverse();
			for (int i=1;i<=sb2.length();i++)
			{
				sb.insert(0,sb2.charAt(i-1));
				if (i % 3 == 0 && i < sb2.length())				
					sb.insert(0,".");
			}					
			
			if (sArr.length > 1)
			{
				sb.append(",");
				sb.append(sArr[1]);								
			}
			
			if (sMinus)
				sb.insert(0, "-");
			
			return sb.toString();
		}
		catch (Exception ex)
		{
			return sText;
		}		
	}*/
	public static String FormatMoney3(String sText, boolean bShowDecimals, boolean bAddCurrency)
	{								
		try
		{
			sText = sText.trim();
			if (sText.startsWith("Rp "))
				sText = sText.substring(3);
			
			boolean sMinus = ((Double.valueOf(sText) < 0));
			if (sMinus)
				sText = sText.substring(1);
					
			String[] sArr = sText.split("\\.");			
			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			sb2.append(sArr[0]);
			sb2.reverse();
			for (int i=1;i<=sb2.length();i++)
			{
				sb.insert(0,sb2.charAt(i-1));
				if (i % 3 == 0 && i < sb2.length())				
					sb.insert(0,".");
			}					
			
			if (sArr.length > 1 && bShowDecimals)
			{
				sb.append(",");
				sb.append(sArr[1]);								
			}
			
			if (sMinus)
				sb.insert(0, "-");
			
			if (bAddCurrency)
				sb.insert(0, "Rp ");
			
			return sb.toString();
		}
		catch (Exception ex)
		{
			return sText;
		}		
	}
	public static double FormatMoneyToDouble(String sMoney, boolean bDisplayDecimal) // sMoney = 1.500.000,00
	{		
		double fVal = 0;
		StringBuilder sbCombi = new StringBuilder();
		String[]arrDec = sMoney.split(",");
		
		try
		{
			if (arrDec.length > 0)
			{			
				String[] arrNum = arrDec[0].split("\\.");
				for(String s: arrNum)			
					sbCombi.append(s);						
			}		
			if (arrDec.length > 1 && bDisplayDecimal)
			{
				sbCombi.append(".");
				sbCombi.append(arrDec[1]);
			}
			
			if (sbCombi.toString().length() <= 0)
				return 0;
					
			fVal = R1Util.DoubleValue(sbCombi.toString());
		}
		catch (Exception ex)
		{}
		
		return fVal;
	}
	
	public static String RemoveDecimal(String sText)
	{
		String sRes = "";
		try
		{			
			 sRes = String.format("%.0f", R1Util.DoubleValue(sText));
		}
		catch (Exception ex)
		{
			return sText;
		}
		
		return sRes;
	}
	
	public static void RestartDevice()
	{
		try 
  	{
      Process proc = Runtime.getRuntime()
                      //.exec(new String[]{ "su", "-c", "reboot -p" });
          						.exec(new String[]{ "su", "-c", "reboot" });
      proc.waitFor();
	  } 
  	catch (Exception ex) {
	      ex.printStackTrace();
	  }
	}
	public static void ShutdownDevice()
	{
		try 
  	{
      Process proc = Runtime.getRuntime()
                      //.exec(new String[]{ "su", "-c", "reboot -p" });
          						.exec(new String[]{ "su", "-c", "shutdown" });
      proc.waitFor();
	  } 
  	catch (Exception ex) {
	      ex.printStackTrace();
	  }
	}
			
	public static double GetSDCardFreeSpace()
	{
		try
		{
			StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
			double sdAvailSize = (double)stat.getAvailableBlocks() *(double)stat.getBlockSize();

			// One binary gigabyte equals 1,073,741,824 bytes.
			return sdAvailSize / (1024*1024);
		}
		catch (Exception ex)
		{		
			return 0;
		}
	}
	public static boolean IsSDCardFreeSpaceAvailable(double dMinFreeSpaceMB)
	{
		if (GetSDCardFreeSpace() > dMinFreeSpaceMB)
			return true;
		else
			return false;
	}
	public static double GetInternalMemoryFreeSpace()
	{
		try
		{
			StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
			double sdAvailSize = (double)stat.getAvailableBlocks() *(double)stat.getBlockSize();

			// One binary gigabyte equals 1,073,741,824 bytes.
			return sdAvailSize / (1024*1024);
		}
		catch (Exception ex)
		{		
			return 0;
		}
	}	
	public static String[] ListFilesInDir(String sDirName, String sFilter)
	{
		R1FileNameFilter filter = new R1FileNameFilter(sFilter);
		File dir = new File(sDirName);				
		
		return dir.list(filter);				
	}	
	public static void ListFilesInDir(File dir, R1FileNameFilter filter, List<String>arrFiles)
	{					
		if (dir.isDirectory())
		{
			String[] children = dir.list();
      for (int i=0; i<children.length; i++) 
      {
        ListFilesInDir(new File(dir, children[i]), filter, arrFiles);
      }      						
		}
		
		try
		{
			String[] arr = dir.list(filter);
			for(String sFile: arr)
				arrFiles.add(dir.getPath() + "/" + sFile);		
		}
		catch (Exception ex)
		{}
	}
	
	public static boolean DeleteDir(File dir) 
	{
		try
		{
	    if (dir.isDirectory()) 
	    {
	      String[] children = dir.list();
	      for (int i=0; i<children.length; i++) 
	      {
	        boolean success = DeleteDir(new File(dir, children[i]));
	        if (!success) 
	        {
	        	return false;
	        }
	      }
	    }
	
	    // The directory is now empty so delete it
	    return dir.delete();
		}
		catch (Exception ex)
		{
			return false;
		}				
	}
	
	public static boolean CopyDir(File dir, String sSrcDir, String sDestDir) 
	{
    if (dir.isDirectory()) 
    {
      String[] children = dir.list();
      for (int i=0; i<children.length; i++) 
      {
        boolean success = CopyDir(new File(dir, children[i]), sSrcDir, sDestDir);
        if (!success) 
        {
        	return false;
        }
      }
    }
            
    if (dir.isFile())
    {
    	// create dirs
	    String sDest = dir.getPath().replace(sSrcDir, sDestDir);
	    File destFile = new File(sDest);
	    boolean bMKDir = destFile.mkdirs();
	    
	    // copy file
	    return CopyFile(dir.getPath(), sDest);
    }
    else
    	return true;
	}
	/*public static boolean CopyDir(File dir, String sDestDir) 
	{
    if (dir.isDirectory()) 
    {
      String[] children = dir.list();
      for (int i=0; i<children.length; i++) 
      {
        boolean success = CopyDir(new File(dir, children[i]), sDestDir);
        if (!success) 
        {
        	return false;
        }
      }
    }
            
    if (dir.isFile())
    {
    	// create dirs
	    String sDest = dir.getPath().replace(Environment.getExternalStorageDirectory().toString(), sDestDir);
	    File destFile = new File(sDest);
	    boolean bMKDir = destFile.mkdirs();
	    
	    // copy file
	    return CopyFile(dir.getPath(), sDest);
    }
    else
    	return true;
	}*/
	public static boolean CopyFile(String sSrc, String sDest)
	{
		try
		{
			File src = new File(GetFolderName(sSrc), GetFileName(sSrc));
			File dst = new File(GetFolderName(sDest), GetFileName(sDest));
			if (dst.exists())			
				DeleteDir(dst);
			//dst.createNewFile();
			
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);
	
	    // Transfer bytes from in to out
	    byte[] buf = new byte[4096];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
		}
		catch (Exception ex)
		{
			String str = ex.getMessage();
			return false;
		}
		
		return true;
	}
	
	public static boolean MoveDir(File src, File dest)
	{
		// Move file/dir to new file/directory
		boolean bOK = src.renameTo(new File(dest, src.getName()));
		return bOK;
	}
	public static boolean MoveDir(String srcPath, String destPath)
	{		
		// File (or directory) to be moved
		/*File src = new File(srcPath);

		// Destination directory
		File dest = new File(destPath);
		
		// Move file/dir to new file/directory
		boolean bOK = src.renameTo(new File(dest, src.getName()));
		return bOK;*/
				
		File src = new File(GetFolderName(srcPath));
		File dest = new File(GetFolderName(destPath));
		File from = new File(src, GetFileName(srcPath));
		File to = new File(dest, GetFileName(destPath));				
		boolean bOK = from.renameTo(to);
		return bOK;
	}	
	public static boolean MoveDir(String sSrcFolder, String sSrcFile, String sDestFolder, String sDestFile)
	{		
		// File (or directory) to be moved
		File srcFolder = new File(sSrcFolder);

		// Destination directory
		File destFolder = new File(sDestFolder);
		
		// Move file/dir to new file/directory
		File from = new File(srcFolder, sSrcFile);
		File to = new File(destFolder, sDestFile);
		boolean bOK = from.renameTo(to);
		return bOK;
	}	
	//public static void CopyFile(File src, File dst) 
	/*public static void CopyFile(String sSrc, String sDest)
	{
		try
		{
			File src = new File(GetFolderName(sSrc), GetFileName(sSrc));
			File dst = new File(GetFolderName(sDest), GetFileName(sDest));
			
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);
	
	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
		}
		catch (Exception ex)
		{}
	}*/
	
	/**  
  * Check that a date is formatted according to the following convention:  
  * DD/MM/YYYY OR DD.MM.YYYY OR DD-MM-YYYY  
  * @param dateStr a date string.  
  * @return true if the date text should be rejected  
  */  
  private static boolean invalidDate(String dateStr) // REGEX only checks date format, doesn't check date validity
  {  
	  String regex = "^(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.](19|20)\\d\\d$";  
	  return !Pattern.matches(regex, dateStr);  
  }  
  /**  
  * Check that a time is formatted according to the following convention:  
  * HH:MM:SS AM/PM  
  * @param timeStr a time string.  
  * @return true if the time text should be rejected  
  */  
  private static boolean invalidTime(String timeStr) 
  {  
	  String regex = "^(([0]?[1-9])|([1][0-2])):(([0-5][0-9])|([1-9])):([0-5][0-9]) [AP][M]$";  
	  return !Pattern.matches(regex, timeStr);  
  }  
  
//date validation using SimpleDateFormat
//it will take a string and make sure it's in the proper 
//format as defined by you, and it will also make sure that
//it's a legal date

	public static boolean isValidDate(String date, String sFormat)
	{
	   // set date format, this can be changed to whatever format
	   // you want, MM-dd-yyyy, MM.dd.yyyy, dd.MM.yyyy etc.
	   // you can read more about it here:
	   // http://java.sun.com/j2se/1.4.2/docs/api/index.html
	   
	   SimpleDateFormat sdf = new SimpleDateFormat(sFormat);	   
	   
	   // declare and initialize testDate variable, this is what will hold
	   // our converted string
	   
	   Date testDate = null;
	
	   // we will now try to parse the string into date form
	   try
	   {
	     testDate = sdf.parse(date);
	   }
	
	   // if the format of the string provided doesn't match the format we 
	   // declared in SimpleDateFormat() we will get an exception
	
	   catch (ParseException e)
	   {
	     //errorMessage = "the date you provided is in an invalid date" +" format.";
	     return false;
	   }
	
	   // dateformat.parse will accept any date as long as it's in the format
	   // you defined, it simply rolls dates over, for example, december 32 
	   // becomes jan 1 and december 0 becomes november 30
	   // This statement will make sure that once the string 
	   // has been checked for proper formatting that the date is still the 
	   // date that was entered, if it's not, we assume that the date is invalid
	
	   if (!sdf.format(testDate).equals(date)) 
	   {
	     //errorMessage = "The date that you provided is invalid.";
	     return false;
	   }
	   
	   // if we make it to here without getting an error it is assumed that
	   // the date was a valid one and that it's in the proper format
	
	   return true;
	
	} // end isValidDate
	
	public static boolean isValidTime(String sTime)
	{
		try
		{			
			String[] arrTime = sTime.split(":");
			if (arrTime.length <=1)						
				return false;			
			
			int nHour = Integer.valueOf(arrTime[0]);
			int nMinute = Integer.valueOf(arrTime[1]);
									
			if (nHour < 0 || nHour > 23 || nMinute < 0 || nMinute > 59)			
				return false;
			
			if (arrTime.length >= 3)
			{
				int nSecond = Integer.valueOf(arrTime[2]);
				if (nSecond < 0 || nSecond > 59)			
					return false;
			}
		}
		catch (Exception ex)
		{			
			return false;
		}
		
		return true;
	}

	
	public static String CreateZippedLogFileName(String sBaseName, int nPart)
	{ 
		Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
		return sBaseName + "_" + formatter.format(
				R1Util.GetServerTime(m_offset).getTime()) + "_Part" + String.valueOf(nPart) + ".zip";		
	}
	public static String CreatePumaDocumentNumber(String sSalesRepID, ESequentialID eSeq)
	{ 
		String sHeader = "";
		switch (eSeq)
		{
			case Payment:
				sHeader = "P";
				break;
			case SalesOrder:
				sHeader = "O";
				break;
			case BankDeposit:
			case BGCekHandover:
			case BankKliring:
			case BuktiTransferHandover:
			default:
				sHeader = "D";
				break;
		}
		Format formatter = new SimpleDateFormat("yyMM");
		return sHeader + sSalesRepID + formatter.format(R1Util.GetServerTime(m_offset).getTime());		
	}
	public static String CreateFileNameWithDateTime(String sBaseName)
	{ 
		Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
		return sBaseName + "_" + formatter.format(
				R1Util.GetServerTime(m_offset).getTime());		
	}	
	public static String CreateRecFileName(String sBaseName, Calendar cal)
	{ 
		Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
		//return sBaseName + "_" + formatter.format(cal.getTime());		
		return formatter.format(cal.getTime()) + "_" + sBaseName;
	}
	public static boolean IsFileExists(String sFileFullPath)
	{
		File file = new File(sFileFullPath);
		return file.exists();
	}
	public static long GetFileLength(String sFileFullPath)
	{
		//File file = new File(sFileFullPath);
		File folder = new File(GetFolderName(sFileFullPath));
		File file = new File(folder, GetFileName(sFileFullPath));				
		return file.length();
	}
	public static String GetCurrentPath()
	{
		File file = new File("test");
		return file.getPath();		
	}
	public static String GetFolderName(String sPathWithFileName)
	{
		String sName = "/";
		
		try
		{
			String[] arr = sPathWithFileName.split("/");
			if (arr.length < 0)
				return "/sdcard";
							
			for(int i=1;i<arr.length-1;i++)
			{
				if (i == arr.length - 2)
					sName += arr[i];
				else
					sName += arr[i] + "/";
			}
			
			return sName;
		}
		catch (Exception ex)
		{
			return "/sdcard";
		}
	}
	public static String GetFileName(String sPathWithFileName)
	{				
		try
		{
			String[] arr = sPathWithFileName.split("/");
			if (arr.length < 0)
				return sPathWithFileName;
										
			return arr[arr.length-1];
		}
		catch (Exception ex)
		{
			return sPathWithFileName;
		}
	}
	public static String FixSQLString(String sText)
  {
		return FixSQLString(sText, 10000);
  }
	public static String FixSQLString(String sText, int nMaxLength)
  {
      try
      {
          //sText = sText.Replace("TRUNCATE TABLE", "DELETE FROM");
          //sText = sText.Replace("\r", "");
          //sText = sText.Replace("\n", "");
          sText = sText.replace("'", "''");
          sText = sText.replace("\"", "");
          //sText = sText.Replace("\\'", "''");

          if (sText.length() > nMaxLength)          	
              sText = sText.substring(0, nMaxLength);
      }
      catch (Exception ex)
      {
          return "";
      }

      return sText;
  }
	public static String GetSQLDateTime()
  {  	   	  	 
  	 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  	 return df.format(R1Util.GetServerTime(m_offset).getTime());  	       
  }
	public static Calendar ParseDateTime(String sDateTime, DateTimeFormat eFrom)
	{
		String[] arr = sDateTime.trim().split(" ");
		if (arr.length <= 1)
			sDateTime += " 00:00:00";
		
		clsDateTime cdt = new clsDateTime();
		cdt.FromString(sDateTime, eFrom);
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, cdt.getM_nYear());
		cal.set(Calendar.MONTH, cdt.getM_nMonth()-1);
		cal.set(Calendar.DATE, cdt.getM_nDay());
		cal.set(Calendar.HOUR_OF_DAY, cdt.getM_nHour());
		cal.set(Calendar.MINUTE, cdt.getM_nMinute());
		cal.set(Calendar.SECOND, cdt.getM_nSecond());			
		
		return cal;
	}
	public static String ParseDateTime(String sDateTime, DateTimeFormat eFrom, boolean bDisplaySecond)
	{
		clsDateTime cdt = new clsDateTime();
		cdt.FromString(sDateTime, eFrom);
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, cdt.getM_nYear());
		cal.set(Calendar.MONTH, cdt.getM_nMonth()-1);
		cal.set(Calendar.DATE, cdt.getM_nDay());
		cal.set(Calendar.HOUR_OF_DAY, cdt.getM_nHour());
		cal.set(Calendar.MINUTE, cdt.getM_nMinute());
		cal.set(Calendar.SECOND, cdt.getM_nSecond());
		
		return ParseDateTime(cal, bDisplaySecond);
	}
	public static String ParseDateTime(Calendar cal, boolean bDisplaySecond)
	{
		String sDateTime = "";
		Calendar now = R1Util.GetServerTime(m_offset);
		
		if (cal.get(Calendar.DATE) == now.get(Calendar.DATE) &&
				cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
				cal.get(Calendar.YEAR) == now.get(Calendar.YEAR))
		{
			if (bDisplaySecond)
				sDateTime = GetDateTime(cal.getTime(), DateTimeFormat.HHMMSS);
			else
				sDateTime = GetDateTime(cal.getTime(), DateTimeFormat.HHMM);
		}
		else
			sDateTime = GetDateTime(cal.getTime(), DateTimeFormat.DDMMYYYY);
		
		return sDateTime;
	}
	public static String ParseDateTimeExtra(Calendar cal, boolean bMultiline)
	{		
		Calendar now = R1Util.GetServerTime(m_offset);
		
		if (cal.get(Calendar.DATE) == now.get(Calendar.DATE) &&
				cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
				cal.get(Calendar.YEAR) == now.get(Calendar.YEAR))
		{	
			return GetFullDateExtra(cal, "en", 3, bMultiline);			
		}
		else if (cal.get(Calendar.DATE) == now.get(Calendar.DATE) &&
				cal.get(Calendar.MONTH) == now.get(Calendar.MONTH))
		{
			return GetFullDateExtra(cal, "en", 2, bMultiline);
		}
		else
			return GetFullDateExtra(cal, "en", 1, bMultiline);
		
	}
	public static String GetFullDateExtra(Calendar cal, String sLanguage, int nType, boolean bMultiline) // nType = 1,2,3 ==> full, no year, only time + day
  {
		String sMonth = "";
		String sDay = "";		
							
		try
		{						
			if (sLanguage.equalsIgnoreCase("en"))
			{												
				sMonth = iCCConstants.ksMonthsEN[cal.get(Calendar.MONTH)+1];				
				sDay = iCCConstants.ksDaysEN[cal.get(Calendar.DAY_OF_WEEK)];
			}
			else
			{
				sMonth = iCCConstants.ksMonthsINA[cal.get(Calendar.MONTH)+1];
				sDay = iCCConstants.ksDaysINA[cal.get(Calendar.DAY_OF_WEEK)];
			}
		}
		catch (Exception ex) 
		{ }

		if (nType == 1)
		{
			if (!bMultiline)
				return String.format("%d %s %d, %s %d:%02d",
						cal.get(Calendar.DATE), sMonth, cal.get(Calendar.YEAR), sDay, 
						cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
			else
				return String.format("%d %s %d,\n%s %d:%02d",
						cal.get(Calendar.DATE), sMonth, cal.get(Calendar.YEAR), sDay, 
						cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
		}
		else if (nType == 2)
		{
			if (!bMultiline)
				return String.format("%d %s, %s %d:%02d",
						cal.get(Calendar.DATE), sMonth, sDay, 
						cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
			else
				return String.format("%d %s,\n%s %d:%02d",
						cal.get(Calendar.DATE), sMonth, sDay, 
						cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
		}
		else
		{			
				return String.format("%s %d:%02d", sDay,					
					cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
		}
  }
	
	public static String GetDateTime(Date dt, DateTimeFormat format)
  {
		SimpleDateFormat df;
		switch (format)
		{
			case DDMMYYYY:
				df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				return df.format(dt);				
			case DDMMYYYY2:
				df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				return df.format(dt);
			case YYYYDDMM:
				df = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
				return df.format(dt);			
			case YYYYMMDD:
				df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				return df.format(dt);			
			case YYYYMMDD_Date:
				df = new SimpleDateFormat("yyyy-MM-dd");
				return df.format(dt);
			case DDMMYYYY_Date:
				df = new SimpleDateFormat("dd-MM-yyyy");
				return df.format(dt);
			case HHMM:
				df = new SimpleDateFormat("HH:mm");
				return df.format(dt);
			case HHMMSS:
				df = new SimpleDateFormat("HH:mm:ss");
				return df.format(dt);			
			case FileExplorer:
				int nYear = dt.getYear() + 1900;
				if (nYear < 2000)
				{
					df = new SimpleDateFormat("2000:MM:dd:HH:mm:ss");
					return df.format(dt);
				}
				else
				{
					df = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
					return df.format(dt);  				
				}								
			default:
				df = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
				return df.format(dt);				
		}
  }
	public static String GetFullDate(Calendar cal, String sLanguage) 
  {
		String sMonth = "";
		String sDay = "";		
							
		try
		{						
			if (sLanguage.equalsIgnoreCase("en"))
			{												
				sMonth = iCCConstants.ksMonthsEN[cal.get(Calendar.MONTH)+1];				
				sDay = iCCConstants.ksDaysEN[cal.get(Calendar.DAY_OF_WEEK)];
			}
			else
			{
				sMonth = iCCConstants.ksMonthsINA[cal.get(Calendar.MONTH)+1];
				sDay = iCCConstants.ksDaysINA[cal.get(Calendar.DAY_OF_WEEK)];
			}
		}
		catch (Exception ex) 
		{ }

		return String.format("%s, %d %s %d",
				sDay, cal.get(Calendar.DATE), sMonth, cal.get(Calendar.YEAR));
  }
	public static String GetFullTime(String sLanguage)
  {
		String sMonth = "";
		String sDay = "";      
				
		try
		{
			if (sLanguage.equalsIgnoreCase("en"))
			{												
				sMonth = iCCConstants.ksMonthsEN[GetServerTime(m_offset).get(Calendar.MONTH)+1];				
				sDay = iCCConstants.ksDaysEN[GetServerTime(m_offset).get(Calendar.DAY_OF_WEEK)];
			}
			else
			{
				sMonth = iCCConstants.ksMonthsINA[GetServerTime(m_offset).get(Calendar.MONTH)+1];
				sDay = iCCConstants.ksDaysINA[GetServerTime(m_offset).get(Calendar.DAY_OF_WEEK)];
			}
		}
		catch (Exception ex) 
		{ }

		return String.format("%s, %s %s, %02d:%02d",
				sDay, GetServerTime(m_offset).get(Calendar.DATE), sMonth,
				GetServerTime(m_offset).get(Calendar.HOUR_OF_DAY), 
				GetServerTime(m_offset).get(Calendar.MINUTE));
  }	
	public static Calendar GetServerTime(TimeOffset offset)
	{
		Calendar cal = Calendar.getInstance();		
		cal.add(Calendar.MINUTE, offset.getnTimeDiffMinute());
		cal.add(Calendar.SECOND, offset.getnTimeDiffSecond());		
		
		return cal;
	}
	public static Calendar GetServerTime()
	{
		Calendar cal = Calendar.getInstance();		
		cal.add(Calendar.MINUTE, m_offset.getnTimeDiffMinute());
		cal.add(Calendar.SECOND, m_offset.getnTimeDiffSecond());		
		
		return cal;
	}
	
	/*public static byte[] HexToStream(String raw, int nLength) 	
	{
		byte[] byRes = null;
		
		try
		{
			byRes = new byte[nLength];
			for (int i=0,j=0;i<raw.length();i++)
			{
				//String hex = "ff";
				//int value = Integer.parseInt(hex, 16);
				
				char ch = raw.charAt(i);							
				//System.out.println("Binary: " + Integer.toBinaryString(ch));
				
				byte temp = (byte)Integer.parseInt("" + ch, 16);
				if (i % 2 == 0)
					byRes[j] = temp;
				else					
				{
					byRes[j] = (byte) (((byte)byRes[j] >> 4) & temp);
					j++;
				}
			}	    
		}
		catch (Exception ex)
		{}
		
		return byRes;
	}*/
		
	public static String StreamToHex(byte[] raw, int nLength, String sSeparator) 	
	{		    
		String HEXES = "0123456789ABCDEF";
    final StringBuilder hex = new StringBuilder(nLength); // for char[], use 2 * nLength
    for(int i=0;i<nLength;i++) 
    {    	
      hex.append(HEXES.charAt((raw[i] & 0xF0) >> 4))
         .append(HEXES.charAt((raw[i] & 0x0F)));
      hex.append(sSeparator);
    }
    return hex.toString();		
  }
	public static String StreamToHex(byte[] raw, int nLength, int nMax, String sSeparator) 	
	{		    
		String HEXES = "0123456789ABCDEF";
    final StringBuilder hex = new StringBuilder(nLength); // for char[], use 2 * nLength
    int nLimit = (nLength <= nMax) ? nLength : nMax;
    
    for(int i=0;i<nLimit;i++) 
    {    	
      hex.append(HEXES.charAt((raw[i] & 0xF0) >> 4))
         .append(HEXES.charAt((raw[i] & 0x0F)));
      hex.append(sSeparator);
    }
    return hex.toString();		
  }
	
	public static void CopyAssets(Context ctx) 
	{
    AssetManager am = ctx.getAssets();
    //String[] files1 = null;
    String[] files2 = null;    
    //String[] files4 = null;
        
		String sDir = ctx.getFilesDir().getPath() + "/msa";		
		if (R1Util.IsFileExists(sDir))
  		return;
    
    try 
    {
    	//files1 = am.list("");    	
    	files2 = am.list("Ini");
    	//files4 = am.list("Sound");
    } 
    catch (IOException e) 
    {
      Log.e("tag", "Failed to get asset file list.", e);
    }
    
    String sDir2 = sDir + "/Ini";
    String sDir3 = sDir + "/Log";
    //String sDir4 = sDir + "/Sound";
    //String sDir = Environment.getExternalStorageDirectory().toString() + "/qsms";  	
    //String sDir2 = Environment.getExternalStorageDirectory().toString() + "/qsms/Ini";
    //String sDir3 = Environment.getExternalStorageDirectory().toString() + "/qsms/Log";      	
  	
  	File fDir = new File(sDir);
  	fDir.mkdir();
  	  	
  	File fDir2 = new File(sDir2);
  	fDir2.mkdir();
  	
  	File fDir3 = new File(sDir3);
  	fDir3.mkdir();
  	
  	/*File fDir4 = new File(sDir4);
  	fDir4.mkdir();*/
  	
  	//CopyAssetsInternal(am, "", files1, sDir);
  	CopyAssetsInternal(am, "Ini/", files2, sDir2);  	        
  	//CopyAssetsInternal(am, "Sound/", files4, sDir4);
	}
	private static void CopyAssetsInternal(AssetManager am, String sAssetsDir, String[] files, String sDir)
	{
		for(String filename : files) 
    {
      InputStream in = null;
      OutputStream out = null;
      try 
      {      	            
        in = am.open(sAssetsDir + filename);        
        //File outFile = new File(ctx.getExternalFilesDir(null), filename);
        File outFile = new File(sDir, filename);
        out = new FileOutputStream(outFile);
        copyFile(in, out);
        in.close();
        in = null;
        out.flush();
        out.close();
        out = null;
      } 
      catch(IOException e) 
      {
        //Log.e("tag", "Failed to copy asset file: " + filename, e);
      }       
    }
	}
	private static void copyFile(InputStream in, OutputStream out) throws IOException 
	{
    byte[] buffer = new byte[1024];
    int read;
    while((read = in.read(buffer)) != -1)
    {
      out.write(buffer, 0, read);
    }
	}	
	
	public static void GetTimeDiff(Calendar calHigh, Calendar calLow, TimeDiff td)
	{
		long lHigh = calHigh.getTimeInMillis();
		long lLow = calLow.getTimeInMillis();
		long lDif = lHigh - lLow;		
		
		td.setlDifSeconds(lDif / 1000);		
		td.setlDifMinutes(lDif / (60 * 1000));		
		td.setlDifHours(lDif / (60 * 60 * 1000));
		td.setlDifDays(lDif / (24 * 60 * 60 * 1000));
	}
	
	public static String MaskNumber(String sText, char chMask)
	{
		if (sText.length() <= 0)
			return sText;

		StringBuilder sb = new StringBuilder();
		try
		{
			for (int i=0;i<sText.length();i++)
			{
				char ch = sText.charAt(i);
				if (ch >= '0' && ch <= '9')
					sb.append(chMask);
				else
					sb.append(ch);
			}			
		}
		catch (Exception ex)
		{
			return sText;
		}

		return sb.toString();
	}
	
	public static List<String> SplitByLength(String sText, int nLength)
	{
		int nStart = 0;
		int nEnd = 0;
		int nCount = 0;
		boolean bRun = true;		
		List<String>arr = new ArrayList<String>();		
		
		try
		{
			while(bRun)
			{          				
				nStart = nCount * nLength;
				nEnd = nStart + nLength;
				nCount++;	          
				
				if (sText.length() < nEnd)
				{
					nEnd = sText.length();
					arr.add(sText.substring(nStart, nEnd));
					
					bRun = false;
				}
				else
					arr.add(sText.substring(nStart, nEnd));							          
			}
			
			if (arr.size() > 0)
				return arr;
			else
				return null;
		}
		catch (Exception ex)
		{
			return null;
		}		
	}
	
	public static String ReadFile(String sFileName)
	{		
		// normal way to open/read file		
		try
		{		
			FileReader fr = new FileReader(sFileName);
			char[] buffer = new char[64];
			StringBuilder sb = new StringBuilder();
			int nRead = 0;
			
			while ((nRead = fr.read(buffer)) != -1)
			{
				sb.append(buffer, 0, nRead);    		
			}    		
			fr.close();
			
			//sContent = sb.toString();
			return sb.toString();
		}
		catch (IOException e)
		{		
			return "";
		}		
	}
	public static boolean SaveFile(String sFile, String sData)
	{
		boolean bOK = false;
		
		try
		{
			FileOutputStream fos = new FileOutputStream(sFile, false);			      			     
	    fos.write(sData.getBytes());
	    fos.flush();
	    fos.close();
	    
	    bOK = true;
		}
		catch (Exception ex)
		{}
		
		return bOK;
	}
	
	public static void CopyArray(char[] chSource, byte[] byDest, int nOffset, int nLength)
	{
		for (int i=0;i<nLength;i++)		
			byDest[i] = (byte)chSource[i+nOffset];			
	}
	public static void CopyArray(byte[] bySource, char[] chDest, int nOffset, int nLength)
	{
		for (int i=0;i<nLength;i++)		
			chDest[i] = (char)bySource[i+nOffset];			
	}
	public static void CopyArray(byte[] bySource, byte[] byDest, int nOffset, int nLength)
	{
		for (int i=0;i<nLength;i++)		
			byDest[i] = bySource[i+nOffset];			
	}
	/*public static long GetLongValue(int nLow, int nHigh)
	{
		long temp1, temp2;
    temp1 = (int) nLow;
    temp2 = (int) nHigh;
    temp2 = temp2 << 32;

    return temp1 | temp2;    
	}
	public static int GetUInt(long value, boolean bLowBits)
  {
		long and1;
		long and2;
		long mask;

		if (bLowBits)
		{
			and1 = (long)Math.pow(2, 32);
			and2 = (long)Math.pow(2, 0);
			mask = and1 - and2;

			return (int)(value & mask);
		}
		else      
			return (int)(value >> 32);      
  }
	//starts from the rightmost bit, base 0
  public static int GetValue(BitArray baSrc, int nStart, int nLength)
  {
      int temp = 0, i, j;
      for (i = nStart, j = 0; i < nStart + nLength; i++, j++)
      {
          if (baSrc.GetAt(i))
              temp += 1 << j;
      }

      return temp;
  }
  public static long GetLongValue(BitArray baSrc, int nStart, int nLength)
  {
      long temp = 0;
      int i, j;
      for (i = nStart, j = 0; i < nStart + nLength; i++, j++)
      {
          if (baSrc.GetAt(i))
              temp += 1 << j;
      }

      return temp;
  }*/
	public static String GetString(BitArray baSrc, int nStart, int nLengthPerChar, int[] nStopIndex)
  {
      StringBuilder sb = new StringBuilder();
      int i, j, num = 0;
      byte[] temp = new byte[2];
      for (i = nStart, j = 0; i < baSrc.getSize(); i++, j++)
      {
          if (baSrc.getData()[i])
              num += 1 << j;

          if (j % nLengthPerChar == nLengthPerChar-1)
          {
              if (num <= 0)
                  break;

              temp[0] = (byte)num;
              sb.append((char)temp[0]);
              //sb.Append(Encoding.ASCII.GetString(temp, 0, 1));

              num = 0;
              j = -1;
          }
      }

      nStopIndex[0] = i;
      return sb.toString();
  }
	public static boolean IsBitSet(byte b, int bit) // bit = zero based (0 - 7)
	{
	    return (b & (1 << bit)) != 0;
	}
	public static final byte[] intToByteArray(int value) 
	{
    return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)value};
	}

}
