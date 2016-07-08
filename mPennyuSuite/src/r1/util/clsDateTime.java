package r1.util;

import java.text.SimpleDateFormat;
import java.util.*;
import r1.util.iCCConstants.DateTimeFormat;

public class clsDateTime 
{
	int m_nYear;
  int m_nMonth;
  int m_nDay;
  int m_nHour;
  int m_nMinute;
  int m_nSecond;

  boolean m_bDefault;

  public clsDateTime()
  {
      m_nYear = 2000;
      m_nMonth = 1;
      m_nDay = 1;
      m_nHour = 6;
      m_nMinute = 0;
      m_nSecond = 0;

      m_bDefault = true;
  }
  
  public clsDateTime(int nYear, int nMonth, int nDay, int nHour, int nMinute, int nSecond)
  {
      m_nYear = nYear;
      m_nMonth = nMonth;
      m_nDay = nDay;
      m_nHour = nHour;
      m_nMinute = nMinute;
      m_nSecond = nSecond;
  }
  public void SetDateTime(int nYear, int nMonth, int nDay, int nHour, int nMinute, int nSecond)
  {
      m_nYear = nYear;
      m_nMonth = nMonth;
      m_nDay = nDay;
      m_nHour = nHour;
      m_nMinute = nMinute;
      m_nSecond = nSecond;
  }
  public void SetDateTime(Date dt)
  {
      m_nYear = dt.getYear();
      m_nMonth = dt.getMonth();
      m_nDay = dt.getDay();
      m_nHour = dt.getHours();
      m_nMinute = dt.getMinutes();
      m_nSecond = dt.getSeconds();
  }
  public void Copy(clsDateTime dt)
  {
      m_nDay = dt.getM_nDay();
      m_nHour = dt.getM_nHour();
      m_nMinute = dt.getM_nMinute();
      m_nMonth = dt.getM_nMonth();
      m_nSecond = dt.getM_nSecond();
      m_nYear = dt.getM_nYear();

      if (m_nYear != 2000)
          m_bDefault = false;
  }
  
  public String SQLDateTime(TimeOffset offset)
  {
  	 /*Date date = new Date();
  	 date.setDate(m_nDay);
  	 date.setMonth(m_nMonth);
  	 date.setYear(m_nYear);
  	 date.setHours(m_nHour);
  	 date.setMinutes(m_nMinute);
  	 date.setSeconds(m_nSecond);
  	 
  	 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  	 return df.format(date);*/
  	
  	Calendar cal = null;
  	if (offset != null) 
  		cal = R1Util.GetServerTime(offset);
  	else
  		cal = Calendar.getInstance();
  	
  	cal.set(Calendar.DATE, m_nDay);
  	cal.set(Calendar.MONTH, m_nMonth-1);
  	cal.set(Calendar.YEAR, m_nYear);
  	cal.set(Calendar.HOUR_OF_DAY, m_nHour);
  	cal.set(Calendar.MINUTE, m_nMinute);
  	cal.set(Calendar.SECOND, m_nSecond);
  	
  	return R1Util.GetDateTime(cal.getTime(), DateTimeFormat.YYYYMMDD);
  	
  	/*return String.format("%d-%02d-%02d- %02d:%02d:%02d", m_nYear, m_nMonth, m_nDay,
  			m_nHour, m_nMinute, m_nSecond);*/
  }
  public String ToDateTime()
  {
  	/*Date date = new Date();
  	date.setDate(m_nDay);
  	date.setMonth(m_nMonth);
  	date.setYear(m_nYear);
  	date.setHours(m_nHour);
  	date.setMinutes(m_nMinute);
  	date.setSeconds(m_nSecond);  	

  	SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
  	return df.format(date);*/
  	  	
  	Calendar cal = Calendar.getInstance();  	
  	cal.set(Calendar.DATE, m_nDay);
  	cal.set(Calendar.MONTH, m_nMonth-1);
  	cal.set(Calendar.YEAR, m_nYear);
  	cal.set(Calendar.HOUR_OF_DAY, m_nHour);
  	cal.set(Calendar.MINUTE, m_nMinute);
  	cal.set(Calendar.SECOND, m_nSecond);  	
  	
  	return R1Util.GetDateTime(cal.getTime(), DateTimeFormat.DDMMYYYY);  	
  	
  	/*return String.format("%02d-%02d-%d %02d:%02d:%02d", m_nDay, m_nMonth, m_nYear,
  			m_nHour, m_nMinute, m_nSecond);*/  	
  }
  public void FromString(String sDateTime) // DD-MM-YYYY
  {
  	String delimiter1 = " ";
  	String delimiter2 = "-";
  	String delimiter3 = ":";
      try
      {
          String[] sArr = sDateTime.trim().split(delimiter1);
          String[] sArrDate = sArr[0].split(delimiter2);
          String[] sArrTime = sArr[1].split(delimiter3);

          m_nDay = Integer.parseInt(sArrDate[0]);
          m_nMonth = Integer.parseInt(sArrDate[1]);
          m_nYear = Integer.parseInt(sArrDate[2]);                
          m_nHour = Integer.parseInt(sArrTime[0]);
          m_nMinute = Integer.parseInt(sArrTime[1]);
          m_nSecond = Integer.parseInt(sArrTime[2]);

          m_bDefault = false;
      }
      catch (Exception ex)
      { }
  }
  public void FromString(String sDateTime, iCCConstants.DateTimeFormat eFormat) 
  {
  	String delimiter1 = " ";
  	String delimiter2 = "-";
  	String delimiter3 = ":";
  	
      try
      {
      	String[] sArr = sDateTime.trim().split(delimiter1);
        String[] sArrDate = sArr[0].split(delimiter2);
        String[] sArrTime = sArr[1].split(delimiter3);

          switch (eFormat)
          {          
              case DDMMYYYY:
              {
                  m_nDay = Integer.parseInt(sArrDate[0]);
                  m_nMonth = Integer.parseInt(sArrDate[1]);
                  m_nYear = Integer.parseInt(sArrDate[2]);
                  m_nHour = Integer.parseInt(sArrTime[0]);
                  m_nMinute = Integer.parseInt(sArrTime[1]);
                  m_nSecond = Integer.parseInt(sArrTime[2]);
                  break;
              }
              case YYYYMMDD:
              default:
              {
                  m_nDay = Integer.parseInt(sArrDate[2]);
                  m_nMonth = Integer.parseInt(sArrDate[1]);
                  m_nYear = Integer.parseInt(sArrDate[0]);
                  m_nHour = Integer.parseInt(sArrTime[0]);
                  m_nMinute = Integer.parseInt(sArrTime[1]);
                  m_nSecond = Integer.parseInt(sArrTime[2]);
                  break;
              }
          }                

          m_bDefault = false;
      }
      catch (Exception ex)
      { }
  }    
  public boolean ParseDateTime(BitManager bm, int nStartIndex)
  {
  	boolean bOK = false;        	

  	int nYear = 2000 + (int)bm.getBits(nStartIndex, 6);
  	int nMonth = (int)bm.getBits(nStartIndex + 6, 4);
  	int nDay = (int)bm.getBits(nStartIndex + 10, 5);

  	int nHour = (int)bm.getBits(nStartIndex + 15, 5);
  	int nMinute = (int)bm.getBits(nStartIndex + 20, 6);
  	int nSecond = (int)bm.getBits(nStartIndex + 26, 6);                      

  	if (nMonth >= 1 && nMonth <= 12 &&
  			nDay >= 1 && nDay <= 31 &&
  			nHour >= 0 && nHour <= 23 &&
  			nMinute >= 0 && nMinute <= 59 &&
  			nSecond >= 0 && nSecond <= 59)
  	{
  		SetDateTime(nYear, nMonth, nDay, nHour, nMinute, nSecond);

  		m_bDefault = false;
  		bOK = true;
  	}

  	return bOK;
  }
  public boolean ParseDateTime(byte[] byarr, int nStartIndex)
  {
  	boolean bOK = false;
  	BitManager bm = new BitManager();
  	bm.addBits(byarr);  	
  	
  	int nYear = 2000 + (int)bm.getBits(nStartIndex, 6);
    int nMonth = (int)bm.getBits(nStartIndex + 6, 4);
    int nDay = (int)bm.getBits(nStartIndex + 10, 5);

    int nHour = (int)bm.getBits(nStartIndex + 15, 5);
    int nMinute = (int)bm.getBits(nStartIndex + 20, 6);
    int nSecond = (int)bm.getBits(nStartIndex + 26, 6);                      

    if (nMonth >= 1 && nMonth <= 12 &&
        nDay >= 1 && nDay <= 31 &&
        nHour >= 0 && nHour <= 23 &&
        nMinute >= 0 && nMinute <= 59 &&
        nSecond >= 0 && nSecond <= 59)
    {
        SetDateTime(nYear, nMonth, nDay, nHour, nMinute, nSecond);
        
        m_bDefault = false;
        bOK = true;
    }

    return bOK;
  }
  public boolean ParseDateTime(byte[] byarr, int nStartIndex, clsDateTime temp)
  {
  	boolean bOK = false;
  	BitManager bm = new BitManager();
  	bm.addBits(byarr);

  	int nYear = 2000 + (int)bm.getBits(nStartIndex, 6);
  	int nMonth = (int)bm.getBits(nStartIndex + 6, 4);
  	int nDay = (int)bm.getBits(nStartIndex + 10, 5);

  	int nHour = (int)bm.getBits(nStartIndex + 15, 5);
  	int nMinute = (int)bm.getBits(nStartIndex + 20, 6);
  	int nSecond = (int)bm.getBits(nStartIndex + 26, 6); 

  	temp.SetDateTime(nYear, nMonth, nDay, nHour, nMinute, nSecond);

  	if (nMonth >= 1 && nMonth <= 12 &&
  			nDay >= 1 && nDay <= 31 &&
  			nHour >= 0 && nHour <= 23 &&
  			nMinute >= 0 && nMinute <= 59 &&
  			nSecond >= 0 && nSecond <= 59)
  	{
  		SetDateTime(nYear, nMonth, nDay, nHour, nMinute, nSecond);

  		m_bDefault = false;
  		bOK = true;
  	}

  	return bOK;
  }  

	public int getM_nYear() {
		return m_nYear;
	}

	public void setM_nYear(int m_nYear) {
		this.m_nYear = m_nYear;
	}

	public int getM_nMonth() {
		return m_nMonth;
	}

	public void setM_nMonth(int m_nMonth) {
		this.m_nMonth = m_nMonth;
	}

	public int getM_nDay() {
		return m_nDay;
	}

	public void setM_nDay(int m_nDay) {
		this.m_nDay = m_nDay;
	}

	public int getM_nHour() {
		return m_nHour;
	}

	public void setM_nHour(int m_nHour) {
		this.m_nHour = m_nHour;
	}

	public int getM_nMinute() {
		return m_nMinute;
	}

	public void setM_nMinute(int m_nMinute) {
		this.m_nMinute = m_nMinute;
	}

	public int getM_nSecond() {
		return m_nSecond;
	}

	public void setM_nSecond(int m_nSecond) {
		this.m_nSecond = m_nSecond;
	}

	public boolean isM_bDefault() {
		return m_bDefault;
	}

	public void setM_bDefault(boolean m_bDefault) {
		this.m_bDefault = m_bDefault;
	}
  
  
}
