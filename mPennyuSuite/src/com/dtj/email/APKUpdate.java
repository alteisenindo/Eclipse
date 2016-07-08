package com.dtj.email;

import java.io.File;

public class APKUpdate 
{
	File m_file;
	boolean m_bProcessed; // true = processed (doesn't matter installed or not), false = not processed
	String m_sResult; // OK or ERROR
	String m_sPackageName;
	
	public APKUpdate()
	{
		File m_file = null;
		m_bProcessed = false;
		String m_sResult = "";
		String m_sPackageName = "";
	}

	public File getM_file() {
		return m_file;
	}

	public void setM_file(File m_file) {
		this.m_file = m_file;
	}

	public boolean isM_bProcessed() {
		return m_bProcessed;
	}

	public void setM_bProcessed(boolean m_bProcessed) {
		this.m_bProcessed = m_bProcessed;
	}

	public String getM_sResult() {
		return m_sResult;
	}

	public void setM_sResult(String m_sResult) {
		this.m_sResult = m_sResult;
	}

	public String getM_sPackageName() {
		return m_sPackageName;
	}

	public void setM_sPackageName(String m_sPackageName) {
		this.m_sPackageName = m_sPackageName;
	}	
	
}
