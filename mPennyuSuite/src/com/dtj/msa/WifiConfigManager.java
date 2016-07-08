package com.dtj.msa;

import java.util.List;
import java.util.regex.Pattern;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;

public class WifiConfigManager 
{
	//EditText m_tbLog;
	
	enum NetworkType 
	{
	  WEP,
	  WPA,
	  NO_PASSWORD;

	  static NetworkType forIntentValue(String networkTypeString) 
	  {
	    if (networkTypeString == null) 
	    {
	      return NO_PASSWORD;
	    }
	    if ("WPA".equals(networkTypeString)) 
	    {
	      return WPA;
	    }
	    if ("WEP".equals(networkTypeString)) 
	    {
	      return WEP;
	    }
	    if ("nopass".equals(networkTypeString)) 
	    {
	      return NO_PASSWORD;
	    }
	    throw new IllegalArgumentException(networkTypeString);
	  }

	}

  private static final String TAG = WifiConfigManager.class.getSimpleName();
  private static final Pattern HEX_DIGITS = Pattern.compile("[0-9A-Fa-f]+");
  
  public WifiConfigManager() 
  {  	
  }

  public void configure(final WifiManager wifiManager, 
                               final String ssid, 
                               final String password, 
                               final String networkTypeString) 
  {
    Runnable configureRunnable = new Runnable() 
    {
      public void run() 
      {
      	try
      	{
	        // Start WiFi, otherwise nothing will work
	        if (!wifiManager.isWifiEnabled()) 
	        {
	          Log.i(TAG, "Enabling wi-fi...");
	          //m_tbLog.append("Enabling wi-fi...");
	          if (wifiManager.setWifiEnabled(true)) 
	          {
	            Log.i(TAG, "Wi-fi enabled");
	            //m_tbLog.append("Wi-fi enabled");
	          } 
	          else 
	          {
	            Log.w(TAG, "Wi-fi could not be enabled!");
	            //m_tbLog.append("Wi-fi could not be enabled!");
	            return;
	          }
	          // This happens very quickly, but need to wait for it to enable. A little busy wait?
	          int count = 0;
	          while (!wifiManager.isWifiEnabled()) 
	          {
	            if (count >= 10) 
	            {
	              Log.i(TAG, "Took too long to enable wi-fi, quitting");
	              //m_tbLog.append("Took too long to enable wi-fi, quitting");
	              return;
	            }
	            
	            Log.i(TAG, "Still waiting for wi-fi to enable...");
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
	        }
	        
	        NetworkType networkType = NetworkType.forIntentValue(networkTypeString);
	        if (networkType == NetworkType.NO_PASSWORD) 
	        {
	          changeNetworkUnEncrypted(wifiManager, ssid);
	        } 
	        else 
	        {
	          if (password == null || password.length() == 0) 
	          {
	            throw new IllegalArgumentException();
	          }
	          if (networkType == NetworkType.WEP) 
	          {
	          	//m_tbLog.append("Changing WEP network...\n");
	            changeNetworkWEP(wifiManager, ssid, password);
	          } 
	          else if (networkType == NetworkType.WPA) 
	          {
	          	//m_tbLog.append("Changing WPA network...\n");
	            changeNetworkWPA(wifiManager, ssid, password);
	          }        
	        }
      	}
      	catch (Exception ex)
      	{
      		//m_tbLog.append("Exception: " + ex.getMessage());
      	}
      }
    };
    new Thread(configureRunnable).start();
  }
  
  Handler m_Handler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			if (msg.what == 0)
			{				
			}
		};
	};

  /**
   * Update the network: either create a new network or modify an existing network
   * @param config the new network configuration
   * @return network ID of the connected network.
   */
  private void updateNetwork(WifiManager wifiManager, WifiConfiguration config) 
  {
    Integer foundNetworkID = findNetworkInExistingConfig(wifiManager, config.SSID);
    if (foundNetworkID != null) 
    {
      Log.i(TAG, "Removing old configuration for network " + config.SSID);
      //m_tbLog.append("Removing old configuration for network " + config.SSID + "\n");
      
      wifiManager.removeNetwork(foundNetworkID);
      wifiManager.saveConfiguration();
    }
    /*else
    	m_tbLog.append("Network not found in list: " + config.SSID);*/

    //m_tbLog.append("Adding network...");
    int networkId = wifiManager.addNetwork(config);
    if (networkId >= 0) 
    {
      // Try to disable the current network and start a new one.
      if (wifiManager.enableNetwork(networkId, true)) 
      {
        Log.i(TAG, "Associating to network " + config.SSID);
        //m_tbLog.append("Associating to network " + config.SSID);
        wifiManager.saveConfiguration();
      } 
      else 
      {
        Log.w(TAG, "Failed to enable network " + config.SSID);
        //m_tbLog.append("Failed to enable network " + config.SSID);
      }
    } 
    else 
    {
      Log.w(TAG, "Unable to add network " + config.SSID);
      //m_tbLog.append("Unable to add network " + config.SSID);
    }
  }

  private WifiConfiguration changeNetworkCommon(String ssid) 
  {
    WifiConfiguration config = new WifiConfiguration();
    config.allowedAuthAlgorithms.clear();
    config.allowedGroupCiphers.clear();
    config.allowedKeyManagement.clear();
    config.allowedPairwiseCiphers.clear();
    config.allowedProtocols.clear();
    
    // Android API insists that an ascii SSID must be quoted to be correctly handled.
    config.SSID = quoteNonHex(ssid);
    config.priority = 40;
    return config;
  }

  // Adding a WEP network
  private void changeNetworkWEP(WifiManager wifiManager, String ssid, String password) 
  {
    WifiConfiguration config = changeNetworkCommon(ssid);
    config.wepKeys[0] = quoteNonHex(password, 10, 26, 58);
    config.wepTxKeyIndex = 0;
    config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
    updateNetwork(wifiManager, config);
  }

  // Adding a WPA or WPA2 network
  private void changeNetworkWPA(WifiManager wifiManager, String ssid, String password) 
  {
    WifiConfiguration config = changeNetworkCommon(ssid);
    
    // Hex passwords that are 64 bits long are not to be quoted.
    config.preSharedKey = quoteNonHex(password, 64);
    config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
    config.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA
    config.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
    config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
    config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
    updateNetwork(wifiManager, config);
  }

  // Adding an open, unsecured network
  private void changeNetworkUnEncrypted(WifiManager wifiManager, String ssid) 
  {
    WifiConfiguration config = changeNetworkCommon(ssid);
    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
    updateNetwork(wifiManager, config);
  }

  private Integer findNetworkInExistingConfig(WifiManager wifiManager, String ssid) 
  {
    List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
    for (WifiConfiguration existingConfig : existingConfigs) 
    {
      if (existingConfig.SSID.equals(ssid)) 
      {
        return existingConfig.networkId;
      }
    }
    return null;
  }

  private String quoteNonHex(String value, int... allowedLengths) 
  {
    return isHexOfLength(value, allowedLengths) ? value : convertToQuotedString(value);
  }

  /**
   * Encloses the incoming string inside double quotes, if it isn't already quoted.
   * @param string the input string
   * @return a quoted string, of the form "input".  If the input string is null, it returns null
   * as well.
   */
  private String convertToQuotedString(String string) 
  {
    if (string == null || string.length() == 0) 
    {
      return null;
    }
    // If already quoted, return as-is
    if (string.charAt(0) == '"' && string.charAt(string.length() - 1) == '"') 
    {
      return string;
    }
    return '\"' + string + '\"';
  }

  /**
   * @param value input to check
   * @param allowedLengths allowed lengths, if any
   * @return true if value is a non-null, non-empty string of hex digits, and if allowed lengths are given, has
   *  an allowed length
   */
  private boolean isHexOfLength(CharSequence value, int... allowedLengths) 
  {
    if (value == null || !HEX_DIGITS.matcher(value).matches()) 
    {
      return false;
    }
    
    if (allowedLengths.length == 0) 
    {
      return true;
    }
    
    for (int length : allowedLengths) 
    {
      if (value.length() == length) 
      {
        return true;
      }
    }
    return false;
  }
}