package r1.util;

public class SMSInfo 
{
	//[0: _id,1: thread_id,2: address,3: person,4: date,5: protocol,6: read,7: status,8: type,9: reply_path_present,
	//10: subject,11: body,12: service_center,13: locked,14: error_code,15: seen]
	
	String sAddress;
	String sDateTime;
	String sMessage;
	String sRead;
	String sType;
	String sServiceCenter;
	String sSeen;
	
	public SMSInfo()
	{
		sAddress = "";
		sDateTime = "";
		sMessage = "";
		sRead = "";
		sType = "";
		sServiceCenter = "";
		sSeen = "";		
	}

	public String getsAddress() {
		return sAddress;
	}

	public void setsAddress(String sAddress) {
		this.sAddress = sAddress;
	}

	public String getsDateTime() {
		return sDateTime;
	}

	public void setsDateTime(String sDateTime) {
		this.sDateTime = sDateTime;
	}

	public String getsMessage() {
		return sMessage;
	}

	public void setsMessage(String sMessage) {
		this.sMessage = sMessage;
	}

	public String getsRead() {
		return sRead;
	}

	public void setsRead(String sRead) {
		this.sRead = sRead;
	}

	public String getsType() {
		return sType;
	}

	public void setsType(String sType) {
		this.sType = sType;
	}

	public String getsServiceCenter() {
		return sServiceCenter;
	}

	public void setsServiceCenter(String sServiceCenter) {
		this.sServiceCenter = sServiceCenter;
	}

	public String getsSeen() {
		return sSeen;
	}

	public void setsSeen(String sSeen) {
		this.sSeen = sSeen;
	}
}
