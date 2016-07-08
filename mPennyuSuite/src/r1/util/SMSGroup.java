package r1.util;

public class SMSGroup 
{
	String sAddress; // could be sender/destination
	String sName; // only if bIsEncrypted = true
	String sMessage;
	String sMessageTimestamp;
	int nTotalConversation;
	boolean bIsEncrypted;
	int nSMSType;
	
	public SMSGroup()
	{
		sAddress = "";
		sName = "";
		sMessage = "";
		sMessageTimestamp = "";
		nTotalConversation = 0;
		bIsEncrypted = true;
		nSMSType = 0;
	}
	
	
	public String getsName() {
		return sName;
	}
	public void setsName(String sName) {
		this.sName = sName;
	}
	public int getnSMSType() {
		return nSMSType;
	}
	public void setnSMSType(int nSMSType) {
		this.nSMSType = nSMSType;
	}
	public String getsAddress() {
		return sAddress;
	}
	public void setsAddress(String sAddress) {
		this.sAddress= sAddress;
	}
	public String getsMessage() {
		return sMessage;
	}
	public void setsMessage(String sMessage) {
		this.sMessage = sMessage;
	}
	public String getsMessageTimestamp() {
		return sMessageTimestamp;
	}
	public void setsMessageTimestamp(String sMessageTimestamp) {
		this.sMessageTimestamp = sMessageTimestamp;
	}

	public int getnTotalConversation() {
		return nTotalConversation;
	}

	public void setnTotalConversation(int nTotalConversation) {
		this.nTotalConversation = nTotalConversation;
	}

	public boolean isbIsEncrypted() {
		return bIsEncrypted;
	}

	public void setbIsEncrypted(boolean bIsEncrypted) {
		this.bIsEncrypted = bIsEncrypted;
	}	
}
