package r1.util;

public class ConnectionStatus 
{
	/*Server status: connected/not connected
	Battery strength: 100%
	GSM signal strength: 4/5
	Connection type: Wifi/3G
	Connected to: (router name)
	Wifi Access Point signal strength: -51db*/
	
	boolean bServerConn;
	int nBattStrength;
	int nGSMSignalStrength;
	int nConnType;
	String sWifiAPN;
	int nWifiStatus;
	int nWifiAPNSignalStrength;
	
	public ConnectionStatus()
	{
		bServerConn = false;
		nBattStrength = 0;
		nGSMSignalStrength = 0;
		nConnType = 0;
		sWifiAPN = "";
		nWifiStatus = 0;
		nWifiAPNSignalStrength = -51;
	}
	
	public boolean isbServerConn() {
		return bServerConn;
	}
	public void setbServerConn(boolean bServerConn) {
		this.bServerConn = bServerConn;
	}
	public int getnBattStrength() {
		return nBattStrength;
	}
	public void setnBattStrength(int nBattStrength) {
		this.nBattStrength = nBattStrength;
	}
	public int getnGSMSignalStrength() {
		return nGSMSignalStrength;
	}
	public void setnGSMSignalStrength(int nGSMSignalStrength) {
		this.nGSMSignalStrength = nGSMSignalStrength;
	}
	public int getnConnType() {
		return nConnType;
	}
	public void setnConnType(int nConnType) {
		this.nConnType = nConnType;
	}
	public String getsWifiAPN() {
		return sWifiAPN;
	}
	public void setsWifiAPN(String sWifiAPN) {
		this.sWifiAPN = sWifiAPN;
	}
	public int getnWifiAPNSignalStrength() {
		return nWifiAPNSignalStrength;
	}
	public void setnWifiAPNSignalStrength(int nWifiAPNSignalStrength) {
		this.nWifiAPNSignalStrength = nWifiAPNSignalStrength;
	}

	public int getnWifiStatus() {
		return nWifiStatus;
	}

	public void setnWifiStatus(int nWifiStatus) {
		this.nWifiStatus = nWifiStatus;
	}
	
}
