package com.dtj.msa;

public class DLInfo 
{
	int nSize;
	int nRead;
	int nFinished;
	
	public DLInfo()
	{
		nSize = 0;
		nRead = 0;
		nFinished = 0;
	}

	public int getnSize() {
		return nSize;
	}		

	public void setnSize(int nSize) {
		this.nSize = nSize;
	}

	public int getnRead() {
		return nRead;
	}

	public void setnRead(int nRead) {
		this.nRead = nRead;
	}

	public int getnFinished() {
		return nFinished;
	}

	public void setnFinished(int nFinished) {
		this.nFinished = nFinished;
	}
	
	
}
