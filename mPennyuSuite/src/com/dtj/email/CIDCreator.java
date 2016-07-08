package com.dtj.email;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import r1.util.INIFile;
import r1.util.R1Util;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Base64;
import Decoder.BASE64Encoder;

public class CIDCreator 
{
	static String _ACTIVATION_KEY_1 = "F125577AC0FE40CE962E8EF1C2595644";
  static String _ACTIVATION_KEY_2 = "8F96832F72C44B4C929F1B2E0AA83543";
  static String _ACTIVATION_DUPLICATE = "AF791DA7373D4712AC8BFB4CA077F7BF";
  
	public static String EncryptCId(Context ctx, INIFile m_settings)
	{		
		boolean bWifiOriginalState = true;
		//String sPlain = Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID).toUpperCase();
		
		// UNCOMMENT THIS ON RELEASE MODE
		/*WifiManager wm = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
		if (!wm.isWifiEnabled())
		{
			bWifiOriginalState = false;
  		wm.setWifiEnabled(true);
  		
  		int count = 0;
      while (!wm.isWifiEnabled()) 
      {
        if (count >= 10) 
        {
          //Log.i("", "Took too long to enable wi-fi, quitting");
          //m_tbLog.append("Took too long to enable wi-fi, quitting");
          return null;
        }
        
        //Log.i("", "Still waiting for wi-fi to enable...");
        //m_tbLog.append("Still waiting for wi-fi to enable...");
        try 
        {
          Thread.sleep(1000L);
        } 
        catch (InterruptedException ie) {
          // continue
        }
        count++;
      }
		}*/
											
		try
		{
			// UNCOMMENT THIS
			//String sPlain = wm.getConnectionInfo().getMacAddress();			
			//String sPlain = "00:1E:EC:C4:84:82";
						
			String sPlain = R1Util.getMACAddress("wlan0");
			String sIPAddr = R1Util.getIPAddress(true);
			String sPrep = "";			
			//sPlain = sPlain.replace(":", "");
			sPlain = sPlain.replace(":", "-");
			
			// turn it off again if it was originally off
			/*if (!bWifiOriginalState)
				wm.setWifiEnabled(false);*/
			
			//m_sMACAddress = sPlain;
			TinyDB db = new TinyDB(ctx);
			db.putString("MACAddress", sPlain);
			db.putString("IPAddressv4", sIPAddr);
			
			sPlain = sPlain.replace("-", "");
			//String sWSName = db.getString("AppWSName");
			String sWSName = m_settings.getStringProperty("MSASettings", "AppWId");
									
			for (int i=0;i<sPlain.length();i++)
			{
				char ch = sPlain.charAt(i);
				sPrep += GetRandomHex(0, 255) + ch;			
			}
			
			sPrep += GetCurrentDate();
			sPrep += sWSName;
			
			byte[] byCipher = sPrep.getBytes();
			byte[] byKey2 = _ACTIVATION_KEY_2.getBytes();
			for (int i=0,j=0;i<byCipher.length;i++,j++)
			{				
				if (i % byKey2.length == 0 && i > 0)
					j=0;
																
				byCipher[i] = (byte) ((int)byCipher[i] ^ (int)byKey2[j]);
			}
						
			//BASE64Encoder base64encoder = new BASE64Encoder();			
			//return base64encoder.encode(byCipher);
			
			return new String(Base64.encode(byCipher, Base64.NO_WRAP));
		}
		catch (Exception ex)
		{
			return null;
		}		
	}
  
  private static String GetRandomHex(int nMin, int nMax)
	{
		String str = "";		
		Random rnd = new Random(Calendar.getInstance().getTimeInMillis());
		int nRnd = rnd.nextInt((nMax - nMin) + 1) + nMin;
		String sHex = Integer.toHexString(nRnd).toUpperCase();
		if (sHex.length() <= 1)
			sHex = "0" + sHex;
		
		return sHex;
	}
	
	/*private static String GetCurrentTime()
	{
		Calendar cal = Calendar.getInstance();
		Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
		return formatter.format(cal.getTime());
	}*/
	
	private static String GetCurrentDate()
	{
		Calendar cal = Calendar.getInstance();
		Format formatter = new SimpleDateFormat("yyyyMMdd");	
		return formatter.format(cal.getTime());
	}
}
