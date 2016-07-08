package r1.util;

public class DeviceID 
{
	String sImei;
	String sAndroidID;
	String sDeviceID;
	String sWLANMAC;
	String sBTMAC;
	
	public DeviceID()
	{
		sImei = "";
		sAndroidID = "";
		sDeviceID = "";
		sWLANMAC = "";
		sBTMAC = "";
	}

	public String getsImei() {
		return sImei;
	}

	public void setsImei(String sImei) {
		this.sImei = sImei;
	}

	public String getsAndroidID() {
		return sAndroidID;
	}

	public void setsAndroidID(String sAndroidID) {
		this.sAndroidID = sAndroidID;
	}

	public String getsDeviceID() {
		return sDeviceID;
	}

	public void setsDeviceID(String sDeviceID) {
		this.sDeviceID = sDeviceID;
	}

	public String getsWLANMAC() {
		return sWLANMAC;
	}

	public void setsWLANMAC(String sWLANMAC) {
		this.sWLANMAC = sWLANMAC;
	}

	public String getsBTMAC() {
		return sBTMAC;
	}

	public void setsBTMAC(String sBTMAC) {
		this.sBTMAC = sBTMAC;
	}
	
	
}
