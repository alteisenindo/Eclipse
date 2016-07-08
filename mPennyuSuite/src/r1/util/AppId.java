package r1.util;

public class AppId 
{
	String sUserName;
	String sUserPhoneNumber;
	String sUserPassword;
	String sUserPIN;
	String sMessageId;
	
	public AppId()
	{
		sUserName = "";
		sUserPhoneNumber = "";
		sUserPassword = "";
		sUserPIN = "";
		sMessageId = "";
	}
	
	public String getsMessageId() {
		return sMessageId;
	}

	public void setsMessageId(String sMessageId) {
		this.sMessageId = sMessageId;
	}

	public String getsUserName() {
		return sUserName;
	}

	public void setsUserName(String sUserName) {
		this.sUserName = sUserName;
	}

	public String getsUserPhoneNumber() {
		return sUserPhoneNumber;
	}

	public void setsUserPhoneNumber(String sUserPhoneNumber) {
		this.sUserPhoneNumber = sUserPhoneNumber;
	}

	public String getsUserPIN() {
		return sUserPIN;
	}

	public void setsUserPIN(String sUserPIN) {
		this.sUserPIN = sUserPIN;
	}

	public String getsUserPassword() {
		return sUserPassword;
	}

	public void setsUserPassword(String sUserPassword) {
		this.sUserPassword = sUserPassword;
	}	
}
