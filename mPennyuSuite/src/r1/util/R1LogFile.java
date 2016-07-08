package r1.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.ElementType;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import android.os.Environment;
import android.os.StatFs;
import r1.util.R1Compress;
import java.util.concurrent.locks.*;
//import org.bouncycastle.util.encoders.Base64;
//import com.dtj.qsms.util.AES;

public class R1LogFile
{		
	String m_sFileName;
	int m_nMaxFileSizeKB;
	String m_sHeader;
	boolean m_bFileOK = false;
	boolean m_bFireEvent = false;
	private final int m_knBufferLength = 20;
	private final double m_knMinFreeSpaceMB = 5; // min 5 MB
	private int m_nPart = 1;
	private int m_nTotalPart = 1;
	private int m_nPartToDelete = 1;
	//private final int m_knMaxPart = 170; // 1 week worth of log files
	private final int m_knMaxPart = 24; // 1 day log
	private boolean m_bMustZip = false;
	private boolean m_bEncrypt;
	
	private static Lock m_lock; 			  
	
	private final byte[] m_kR1LogFileKey = {0x22, 0x33, 0x41, 0x21, 0x35, 0x62, 0x71, 0x51, 0x44, 0x66, 0x14, 0x31, 0x50, 0x25, 0x71, 0x47};
	
	OnTextLoggedListener OnTextLoggedEvent = null; // this is the equivalent event in C#
	
	public R1LogFile(String sFileName, int nMaxFileSizeKB, String sHeader, boolean bEncrypt)
	{
		m_sFileName = sFileName;
		m_nMaxFileSizeKB = nMaxFileSizeKB;
		m_sHeader = sHeader;
		m_bEncrypt = bEncrypt;
		
		m_lock = new ReentrantLock(); 
		
		OpenLogFile();
	}	
	public void Cleanup()
	{						
	}
	
	//Allows the user to set an Listener and react to the event
	public void setOnTextLoggedListener(OnTextLoggedListener listener) 
	{
		OnTextLoggedEvent = listener;
	}
	
	private void OpenLogFile()
	{
		// normal way to write a file --> must set permission in manifest.xml				
		try 
		{
			FileWriter fw = new FileWriter(m_sFileName, true);
			fw.write("=======================================\n");
			fw.write(m_sHeader + "\n");
			fw.write("=======================================\n");			
			fw.flush();			
			fw.close();
			
			m_bFileOK = true;
		}
		catch (IOException e) 
		{
			m_bFileOK = false;
			//e.printStackTrace();
		}    		    				
	}
	public boolean Log(String sText, boolean bAddDateTime)
	{							
		boolean bOK = false;
		
		if (!m_bFileOK)
			return bOK;
		
		m_lock.lock();
		
		DeleteRoutine();
		
		if (m_bEncrypt)
		{
			try
			{
				WriteRoutine(sText, bAddDateTime);
				
				// SECURE LOG
				//byte[] byLog = AES.handle(true, sText.getBytes(), m_kR1LogFileKey);				
				//WriteRoutine(new String(Base64.encode(byLog)), bAddDateTime);
			}
			catch (Exception ex)
			{
				WriteRoutine(sText, bAddDateTime);
			}				
		}
		else
			WriteRoutine(sText, bAddDateTime);
		
		m_lock.unlock();
					
		if (R1Util.GetFileLength(m_sFileName) > m_nMaxFileSizeKB * 1024)
		{
			m_bMustZip = true;
			ClockTicked();
		}
														
		return bOK;
	}	
	
	private void DeleteRoutine()
	{							
		/*boolean bDeleteOK = false;
		String sDeletedFile = "";
		
		String sFolder = R1Util.GetFolderName(m_sFileName);
		
		if (!R1Util.IsSDCardFreeSpaceAvailable(m_knMinFreeSpaceMB))
		{									
			while (true)
			{
				String sFilter = String.valueOf(m_nPartToDelete) + ".zip";
				//String[] sFiles = R1Util.ListFilesInDir(Environment.getExternalStorageDirectory().getPath(), sFilter);
				String[] sFiles = R1Util.ListFilesInDir(sFolder, sFilter);
				if (sFiles != null)
				{
					if (sFiles.length > 0)
					{
						bDeleteOK = R1Util.DeleteDir(new File(sFolder + "/" +sFiles[0]));
						sDeletedFile = sFolder + "/" + sFiles[0];
					}
				}
				
				if (!bDeleteOK)
					break;
														
				if (R1Util.IsSDCardFreeSpaceAvailable(this.getMinFreeSpaceMB()))										
					break;
				
				m_nPartToDelete++;				
				m_nTotalPart--;
			}	
			
			m_nPartToDelete++;
			
			if (bDeleteOK)
				WriteRoutine("Deleted file: " + sDeletedFile, true);			
			else
				WriteRoutine("Failed to delete file", true);
		}
		else if (m_nTotalPart > m_knMaxPart)
		{			
			String sFilter = String.valueOf(m_nPartToDelete) + ".zip";
			String[] sFiles = R1Util.ListFilesInDir(sFolder, sFilter);
			if (sFiles != null)
			{
				if (sFiles.length > 0)
				{
					bDeleteOK = R1Util.DeleteDir(new File(sFolder + "/" +sFiles[0]));
					sDeletedFile = sFolder + "/" + sFiles[0];										
				}
			}								

			m_nPartToDelete++;
			m_nTotalPart--;
			
			if (bDeleteOK)
				WriteRoutine("Deleted file: " + sDeletedFile, true);			
			else
				WriteRoutine("Failed to delete file", true);
		}*/				
	}	
	
	private void WriteRoutine(String sText, boolean bAddDateTime)
	{				
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String sFullText = "";
    if (bAddDateTime)
   	 sFullText = df.format(new java.util.Date()) +" " + sText + "\n";
    else
   	 sFullText = sText + "\n";
    
    boolean bWriteOK = false;		

    // normal way to write a file --> must set permission in manifest.xml				
    try 
    {			
    	FileWriter fw = new FileWriter(m_sFileName, true);			
    	fw.write(sFullText);					  

    	fw.flush();
    	fw.close();			

    	bWriteOK = true;
    }
    catch (IOException e) 
    {}
    catch (Exception e)
    {}		
    
		if(OnTextLoggedEvent != null && m_bFireEvent) 		
			OnTextLoggedEvent.onTextLogged(sFullText);
	}	
	
	public void ClockTicked()
	{						
		/*if (m_bMustZip)
	  {						
			m_bMustZip = false;	
			m_lock.lock();
			
			String sZipFileName = R1Util.CreateZippedLogFileName(this.m_sFileName, m_nPart); 
			sZipFileName = sZipFileName.replace(".txt", "");
			
			String[] arrFileList = new String[1];
			arrFileList[0] = this.m_sFileName;
			
			boolean bZipOK = false;
					
			try
			{						
				// zip log file
				R1Compress zip = new R1Compress(arrFileList, sZipFileName);  			  			    	 
				bZipOK = zip.Zip();

				// delete current log file
				File file = new File(this.m_sFileName);
				R1Util.DeleteDir(file);	  		  	
			}
			catch (Exception ex)
			{};
			
			if (bZipOK)
	  		Log("File zipped: " + sZipFileName, true);
	  	else
	  		Log("Failed to zip file zipped: " + sZipFileName, true);
												 					
			m_nPart++;
			m_nTotalPart++;						
			
			m_lock.unlock();			
	  }*/
	}	
	
	public String DisplayContent()
	{		
		// normal way to open/read file		
		try
		{		
			FileReader fr = new FileReader(m_sFileName);
			char[] buffer = new char[m_knBufferLength];
			StringBuilder sb = new StringBuilder();
			int nRead = 0;
			
			while ((nRead = fr.read(buffer)) != -1)
			{
				sb.append(buffer, 0, nRead);    		
			}    		
			fr.close();
			
			//sContent = sb.toString();
			return sb.toString();
		}
		catch (IOException e)
		{		
			return "";
		}		
	}
	public String DisplayContent(String sFileName)
	{		
		// normal way to open/read file		
		try
		{		
			FileReader fr = new FileReader(sFileName);
			char[] buffer = new char[m_knBufferLength];
			StringBuilder sb = new StringBuilder();
			int nRead = 0;
			
			while ((nRead = fr.read(buffer)) != -1)
			{
				sb.append(buffer, 0, nRead);    		
			}    		
			fr.close();
			
			//sContent = sb.toString();
			return sb.toString();
		}
		catch (IOException e)
		{		
			return "";
		}		
	}
						
	public boolean IsFileOpened() 
	{
		return m_bFileOK;
	}
	public void setFireEvent(boolean bFireEvent) 
	{
		m_bFireEvent = bFireEvent;
	}
	public boolean IsFireEvent() 
	{
		return m_bFireEvent;
	}	
	public String FileName() 
	{
		return m_sFileName;
	}
	public void DecreaseTotalPart()
	{
		m_nTotalPart--;	
	}	
	public int getMaxPartNumber()
	{
		return m_knMaxPart;
	}
	public int getPartNumberToDelete()
	{
		return m_nPartToDelete;
	}
	public void setPartNumberToDelete(int nValue)
	{
		m_nPartToDelete = nValue;
	}
	public double getMinFreeSpaceMB()
	{
		return m_knMinFreeSpaceMB;
	}
	public void setMustZip(boolean bMustZip)
	{
		m_bMustZip = bMustZip;
	}		

	//Define our custom Listener interface
	public interface OnTextLoggedListener 
	{
		public abstract void onTextLogged(String sText);
	}
	
}
