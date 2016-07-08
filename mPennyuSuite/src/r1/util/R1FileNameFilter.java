package r1.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class R1FileNameFilter implements FilenameFilter 
{
	String m_sFilter = null;
	List<String> m_arrFilter = new ArrayList<String>();
	
	public R1FileNameFilter(String sFilter)
	{		
		String arr[] = sFilter.split(",");
		if (arr.length <=1)
			m_sFilter = sFilter;
		else
		{
			for(String str: arr)
				m_arrFilter.add(str);
		}
	}
	public boolean accept(File dir, String name) 
  {
		if (m_sFilter != null)
			return name.contains(m_sFilter);
		else
		{
			boolean bFound = false;
			
			for(int i=0;i<m_arrFilter.size();i++)
			{
				if (name.contains(m_arrFilter.get(i)))
				{
					bFound = true;
					break;
				}
			}
			
			return bFound;
		}
  }
}
