package r1.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;
import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Base64;

public class AuthModule 
{
	public enum MD5Result
	{
		Long,
		Short,
		PIN
	}
	
	public static int randInt(int min, int max)
  {
      // NOTE: Usually this should be a field rather than a method
      // variable so that it is not re-seeded every call.
      Random rand = new Random();

      // nextInt is normally exclusive of the top value,
      // so add 1 to make it inclusive
      int randomNum = rand.nextInt((max - min) + 1) + min;

      return randomNum;
  }

	public static String GetUniqueID(Context ctx) 
	{
	// TelephonyManager TelephonyMgr = (TelephonyManager)m_ctx.getSystemService(m_ctx.TELEPHONY_SERVICE);
	TelephonyManager TelephonyMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
	String m_szImei = TelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE

      //String m_szDevIDShort = String.valueOf(randInt(10, 9999))
			String m_szDevIDShort = "708"
			+ // we make this look like a valid IMEI
			Build.BOARD.length() % 10 + Build.BRAND.length() % 10 + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10
			+ Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
			+ Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10 + Build.TYPE.length() % 10
			+ Build.USER.length() % 10; // 13 digits
		
		String m_szAndroidID = Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID); 
		
		String m_szWLANMAC = null;
		/*try
		{
			WifiManager wm = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);		
			m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
		}
		catch (Exception ex)
		{}*/
		
		String m_szBTMAC = null;
		/*try
		{
			BluetoothAdapter m_BluetoothAdapter	= null; // Local Bluetooth adapter
			m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			m_szBTMAC = m_BluetoothAdapter.getAddress();
		}
		catch (Exception ex)
		{}*/
  	  	  	
  	String m_szLongID = m_szImei + m_szDevIDShort + m_szAndroidID+
  			(m_szWLANMAC == null ? "" : m_szWLANMAC) +
  			(m_szBTMAC == null ? "" : m_szBTMAC);  	
  	  	  	
  	//return GetShortMD5(m_szLongID);  	
  	return GetMD5(m_szLongID, MD5Result.PIN);
	}
	
	public static String GetMD5(String sText, MD5Result res)
	{
		// compute md5
  	MessageDigest m = null;
		try 
		{
			m = MessageDigest.getInstance("MD5");
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		
		m.update(sText.getBytes(), 0, sText.length());
		//m.update(m_szLongID.getBytes(),0,m_szLongID.length());
		
		// get md5 bytes
		byte p_md5Data[] = m.digest();
		
		// create a hex string
		String m_szUniqueID = new String();
		for (int i=0;i<p_md5Data.length;i++) 
		{
			int b =  (0xFF & p_md5Data[i]);
			// if it is a single digit, make sure it have 0 in front (proper padding)
			if (b <= 0xF) m_szUniqueID+="0";
			// add number to string
			m_szUniqueID+=Integer.toHexString(b); 
		}
		
		// hex string to uppercase
		m_szUniqueID = m_szUniqueID.toUpperCase();
				
		if (res == MD5Result.Short || res == MD5Result.PIN)
		{
			StringBuilder sb = new StringBuilder();
	  	int i = 0, j=0;
	  	for(char ch : m_szUniqueID.toCharArray())
	  	{
	  		if (res == MD5Result.Short)
	  		{
		  		if (i%2 == 0)
		  		{
		  			sb.append(ch);
		  			j++;
		  			  				  			
		  			if (j%4 == 0 && j > 0)  		
		  				sb.append("-");
		  		}
	  		}
  			else if (res == MD5Result.PIN)
  			{
  				if (i%3 == 0 && i > 0)
  				{
  					if (ch == '0')
  						sb.append('A');
  					else
  						sb.append(ch);
  				}
  				
  				if (sb.length() == 8)
  					return sb.toString().toUpperCase();
  			}
	  		
	  		i++;
	  	}
	  	
	  	if (sb.lastIndexOf("-") == sb.length()-1)
	  		return sb.substring(0, sb.length()-1);
	  	else
	  		return sb.toString();  	
		}
		else
			return m_szUniqueID;
	}
	
	/*public static boolean VerifyAuthCode(Context ctx, String sAuth)
	{
		boolean bOK = false;
		
		try 
		{											
			if (sAuth.trim().length() <= 0)
				return false;
						
			String sUniqueID = GetUniqueID(ctx);
			String sCipher = DESEnryption(true, sUniqueID);			
			
			//String sCode = GetShortMD5(sCipher);			
			String sCode = GetMD5(sCipher, MD5Result.PIN);
			if (sCode.equals(sAuth))
			{
				TinyDB tinydb = new TinyDB(ctx);
				
				String sDES = R1Util.DESEnryption(true, sAuth);
				tinydb.putString("AuthCode", sDES);				
				
				bOK = true;
			}
		}
		catch (Exception ex)
		{}
		
		return bOK;
	}*/
	
	public static String DESEnryption(boolean bEncrypt, String sPlain)
	{
		String sText = "";
	
		try
		{
			// only the first 8 Bytes of the constructor argument are used 
			// as material for generating the keySpec
			DESKeySpec keySpec = new DESKeySpec("SolVaris".getBytes("UTF8")); 
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey key = keyFactory.generateSecret(keySpec);
			//BASE64Encoder base64encoder = new BASE64Encoder();
			//BASE64Decoder base64decoder = new BASE64Decoder();		
			
			if (bEncrypt)
			{
				// ENCODE plainTextPassword String
				byte[] cleartext = sPlain.getBytes("UTF8");      
			
				Cipher cipher = Cipher.getInstance("DES"); // cipher is not thread safe
				cipher.init(Cipher.ENCRYPT_MODE, key);
				//sText = base64encoder.encode(cipher.doFinal(cleartext));				
				sText = Base64.encodeToString(cipher.doFinal(cleartext), Base64.NO_WRAP);
				// now you can store it 
			}
			else
			{	
				// DECODE encryptedPwd String
				//byte[] encrypedPwdBytes = base64decoder.decodeBuffer(sPlain);
				byte[] encrypedPwdBytes = Base64.decode(sPlain, Base64.DEFAULT);
			
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
}
