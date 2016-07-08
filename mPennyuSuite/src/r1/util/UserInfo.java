package r1.util;

import java.util.ArrayList;
import java.util.List;

public class UserInfo 
{
	//boolean m_bSMSReceived;
	boolean m_bContactsUpdated;
	boolean m_bContactKeyCreated;
	BitManager m_bmContactKey;
	String m_sContactPIN;
	boolean m_bLoggedIn;
	
	//private volatile boolean m_bContactsSelected;
	//List<AllContacts> m_arrContacts;
	
	public UserInfo()
	{
		//m_bSMSReceived = false;
		m_bContactsUpdated = false;
		m_bContactKeyCreated = false;
		m_bmContactKey = new BitManager();
		m_sContactPIN = "";
		m_bLoggedIn = false;
		
		//m_bContactsSelected = false;
		//m_arrContacts = new ArrayList<AllContacts>();
	}

	/*public boolean isM_bSMSReceived() {
		return m_bSMSReceived;
	}

	public void setM_bSMSReceived(boolean m_bSMSReceived) {
		this.m_bSMSReceived = m_bSMSReceived;
	}*/

	public boolean isM_bContactsUpdated() {
		return m_bContactsUpdated;
	}

	public void setM_bContactsUpdated(boolean m_bContactsUpdated) {
		this.m_bContactsUpdated = m_bContactsUpdated;
	}	

	public boolean isM_bContactKeyCreated() {
		return m_bContactKeyCreated;
	}

	public void setM_bContactKeyCreated(boolean m_bContactKeyCreated) {
		this.m_bContactKeyCreated = m_bContactKeyCreated;
	}

	public BitManager getM_bmContactKey() {
		return m_bmContactKey;
	}

	public String getM_sContactPIN() {
		return m_sContactPIN;
	}

	public void setM_sContactPIN(String m_sContactPIN) {
		this.m_sContactPIN = m_sContactPIN;
	}

	public synchronized boolean isM_bLoggedIn() {
		return m_bLoggedIn;
	}

	public synchronized void setM_bLoggedIn(boolean m_bLoggedIn) {
		this.m_bLoggedIn = m_bLoggedIn;
	}

	/*public synchronized boolean isM_bContactsSelected() {
		return m_bContactsSelected;
	}

	public synchronized void setM_bContactsSelected(boolean m_bContactsSelected) {
		this.m_bContactsSelected = m_bContactsSelected;
	}

	public synchronized List<AllContacts> getM_arrContacts() {
		return m_arrContacts;
	}*/
}
