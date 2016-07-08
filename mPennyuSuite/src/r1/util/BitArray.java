package r1.util;

public class BitArray
{
	boolean[] arr = null;
	
	public BitArray()
	{}
	public BitArray(BitArray ba)
	{		
		arr = resizeArray(ba.getData(), ba.getSize());		
	}
	public BitArray(byte[] src)
	{		
		CreateBooleanArrayNormal(src);				
	}
	public BitArray(char[] src)
	{		
		byte[] byTemp = new byte[src.length];
		CopyArray(src, byTemp, 0, src.length);
		CreateBooleanArrayNormal(byTemp);				
	}
	public BitArray(String sText)
	{		
		byte[] src = sText.getBytes();
		CreateBooleanArrayNormal(src);				
	}
	public BitArray(int num)
	{		
		byte[] src = intToByteArray(num);
		CreateBooleanArrayReverse(src);				
	}
	public BitArray(long num)
	{		
		byte[] src = longToByteArray(num);
		CreateBooleanArrayReverse(src);				
	}
			
	protected void setSize(int nNewSize)
	{
		arr = resizeArray(arr, nNewSize);
	}
	protected void removeBuffer(int nStart, int nLength)
	{
		if (nStart == 0)
			arr = resizeArray(arr, nLength, getSize() - nLength);
		else
		{
			arr = resizeArray(arr, 0, nStart, nStart + nLength, getSize() - nLength);
		}
	}
	public void copyTo(byte[] byDest, int nIndex)
	{
		for(int i=0,j=0;i<getSize();i++,j++)
		{
			byDest[j/8] = (byte) (byDest[j/8] | ((arr[i]) ? 1 << (j%8) : 0));			
		}
	}
	public int getSize()
	{
		if (arr == null)
			return 0;
		else
			return arr.length;
	}				
	public boolean[] getData()
	{
		return arr;
	}
	
	private boolean[] resizeArray(boolean[] bArray, int newSize) 
  {
    boolean[] newArray = new boolean[newSize];
    if (bArray != null)
    {
	    for (int i = 0; i < bArray.length; i++) 
	    {
	      newArray[i] = bArray[i];
	    }
    }
    return newArray;
  }
	private boolean[] resizeArray(boolean[] bArray, int nStart, int newSize) 
  {
    boolean[] newArray = new boolean[newSize];
    if (bArray != null)
    {
	    for (int i=0, j=nStart; j < bArray.length; i++,j++) 
	    {
	      newArray[i] = bArray[j];
	    }
    }
    return newArray;
  }
	private boolean[] resizeArray(boolean[] bArray, int nStart1, int nEnd1, int nStart2, int newSize) 
  {
    boolean[] newArray = new boolean[newSize];
    if (bArray != null)
    {
    	int i = 0;
	    for (int j=nStart1; j < nEnd1; i++,j++) 	    
	      newArray[i] = bArray[j];	    
	    
	    for (int j=nStart2; j <  bArray.length; i++,j++) 	    
	      newArray[i] = bArray[j];
    }
    return newArray;
  }
	private void CreateBooleanArrayReverse(byte[] src)
	{				
		arr = new boolean[src.length * 8];		
		for(int i=0, j=0;i<arr.length;i++,j++)
		{
			arr[i] = IsBitSet(src[(src.length-1) - (i/8)], j);
			
			if (j >=7)
				j=-1;
		}			
	}	
	private void CreateBooleanArrayNormal(byte[] src)
	{				
		arr = new boolean[src.length * 8];		
		for(int i=0, j=0;i<arr.length;i++,j++)
		{
			arr[i] = IsBitSet(src[i/8], j);
			
			if (j >=7)
				j=-1;
		}		
	}	
	private boolean IsBitSet(byte b, int bit) // bit = zero based (0 - 7)
	{
	    return (b & (1 << bit)) != 0;
	}
	private final byte[] intToByteArray(int value) 
	{
		return new byte[]{
				(byte)(value >>> 24),
				(byte)(value >>> 16),
				(byte)(value >>> 8),
				(byte)value};
	}
	private final byte[] longToByteArray(long v) 
	{
    byte[] writeBuffer = new byte[ 8 ];

    writeBuffer[0] = (byte)(v >>> 56);
    writeBuffer[1] = (byte)(v >>> 48);
    writeBuffer[2] = (byte)(v >>> 40);
    writeBuffer[3] = (byte)(v >>> 32);
    writeBuffer[4] = (byte)(v >>> 24);
    writeBuffer[5] = (byte)(v >>> 16);
    writeBuffer[6] = (byte)(v >>>  8);
    writeBuffer[7] = (byte)(v >>>  0);

    return writeBuffer;
	}
	private void CopyArray(char[] chSource, byte[] byDest, int nOffset, int nLength)
	{
		for (int i=0;i<nLength;i++)		
			byDest[i] = (byte)chSource[i];			
	}
}
