package r1.util;

import java.util.ArrayList;
import java.util.List;

public class AllContacts 
{
	String sName;
	String sPhoneNo;
	String sPIN;
	boolean bSelected;
		
	int nGroupId;	
	
	public AllContacts()
	{
		sName = "";
		sPhoneNo = "";
		sPIN = "";
		bSelected = false;
		
		nGroupId = -1;
		//arrContacts = null;
	}

	public String getsName() {
		return sName;
	}

	public void setsName(String sName) {
		this.sName = sName;
	}

	public String getsPhoneNo() {
		return sPhoneNo;
	}

	public void setsPhoneNo(String sPhoneNo) {
		this.sPhoneNo = sPhoneNo;
	}

	public String getsPIN() {
		return sPIN;
	}

	public void setsPIN(String sPIN) {
		this.sPIN = sPIN;
	}

	public boolean isbSelected() {
		return bSelected;
	}

	public void setbSelected(boolean bSelected) {
		this.bSelected = bSelected;
	}
	
	public int getnGroupId() {
		return nGroupId;
	}

	public void setnGroupId(int nGroupId) {
		this.nGroupId = nGroupId;
	}
	
}
