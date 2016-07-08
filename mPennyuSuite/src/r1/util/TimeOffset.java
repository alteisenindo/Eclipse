package r1.util;

public class TimeOffset 
{
	int nTimeDiffMinute;
	int nTimeDiffSecond;
	
	public TimeOffset()
	{
		nTimeDiffMinute = 0;
		nTimeDiffSecond = 0;
	}
	public TimeOffset(int timeDiffMinute, int timeDiffSecond)
	{
		nTimeDiffMinute = timeDiffMinute;
		nTimeDiffSecond = timeDiffSecond;
	}

	public int getnTimeDiffMinute() {
		return nTimeDiffMinute;
	}

	public void setnTimeDiffMinute(int nTimeDiffMinute) {
		this.nTimeDiffMinute = nTimeDiffMinute;
	}

	public int getnTimeDiffSecond() {
		return nTimeDiffSecond;
	}

	public void setnTimeDiffSecond(int nTimeDiffSecond) {
		this.nTimeDiffSecond = nTimeDiffSecond;
	}
}
