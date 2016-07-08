package r1.util;

public class BitManager 
{
	BitArray bitArr = null;
	
	public BitManager()
	{
		bitArr = new BitArray();
	}
	public void addBits(BitManager bm)
	{
		BitArray bitArrTemp = new BitArray(bm.getBitArray());
    addBitArray(bitArrTemp, bitArrTemp.getSize());
	}
	public void addBits(String sData)
	{
		BitArray bitArrTemp = new BitArray(sData);
    addBitArray(bitArrTemp, bitArrTemp.getSize());
	}
	public void addBits(byte[] byData)
	{
		BitArray bitArrTemp = new BitArray(byData);
    addBitArray(bitArrTemp, bitArrTemp.getSize());
	}
	public void addBitsNew(byte[] byData)
	{
		BitArray bitArrTemp = new BitArray(byData);
    addBitArrayNew(bitArrTemp, bitArrTemp.getSize());
	}
	public void addBitsAppend(byte[] byData)
	{
		BitArray bitArrTemp = new BitArray(byData);
    addBitArrayAppend(bitArrTemp, bitArrTemp.getSize());
	}
	public void addBitsAppend(int num, int numBits)
	{
		BitArray bitArrTemp = new BitArray(num);
    addBitArrayAppend(bitArrTemp, numBits);
	}
	public void addBits(char[] chData)
	{
		BitArray bitArrTemp = new BitArray(chData);
    addBitArray(bitArrTemp, bitArrTemp.getSize());
	}
	public void addBits(int num, int numBits)
	{
		BitArray bitArrTemp = new BitArray(num);
    addBitArray(bitArrTemp, numBits);
	}
	public void addBits(long num, int numBits)
	{
		BitArray bitArrTemp = new BitArray(num);
    addBitArray(bitArrTemp, numBits);
	}
	public void addBitArray(BitArray ba, int numBits)
	{
		for (int i = 0; i < numBits; i++)
    {
      addBit(ba.arr[i]);
    }
	}
	public void addBitArrayNew(BitArray ba, int numBits)
	{
		bitArr.setSize(numBits);
		for (int i = 0; i < numBits; i++)
    {
			if (!ba.arr[i])
	      bitArr.arr[i] = false;
	    else
	    	bitArr.arr[i] = true;			
    }
	}
	public void addBitArrayAppend(BitArray ba, int numBits)
	{
		int nOldSize = bitArr.getSize();		
		bitArr.setSize(nOldSize + numBits);
		
		for (int i = nOldSize, j = 0; i < nOldSize + numBits; i++, j++)
    {
			if (!ba.arr[j])
	      bitArr.arr[i] = false;
	    else
	    	bitArr.arr[i] = true;			
    }
	}
	public void addBitArray(BitArray ba, int nStartIndex, int numBits)
	{
		for (int i = nStartIndex; i < numBits + nStartIndex; i++)
    {						
			addBit(ba.arr[i]);
    }
	}
	public void addBitArrayNew(BitArray ba, int nStartIndex, int numBits)
	{
		bitArr.setSize(numBits);
		for (int i = nStartIndex, j=0; j < numBits; i++,j++)
    {
			if (!ba.arr[i])
	      bitArr.arr[j] = false;
	    else
	    	bitArr.arr[j] = true;        
    }
	}
	public void addBit(boolean val)        //append one bit, val = true or false
  {      
		bitArr.setSize(bitArr.getSize() + 1);
    if (!val)
      bitArr.arr[bitArr.getSize() - 1] = false;
    else
    	bitArr.arr[bitArr.getSize() - 1] = true;   
  }
	public long getBits(int nStart, int nLength) // BIT SHIFT IS ONLY FOR INT
  {
      long temp = 0;
      int i, j;
      for (i = nStart, j = 0; i < nStart + nLength; i++, j++)
      {
          if (bitArr.arr[i])
          	temp += Math.pow(2, j);
      }

      return temp;
  }
	public long getBitsSigned(int nStart, int nLength) // BIT SHIFT IS ONLY FOR INT
  {
      long temp = 0;
      int i, j;
      for (i = nStart, j = 0; i < nStart + nLength; i++, j++)
      {
          if (bitArr.arr[i])
          	temp += Math.pow(2, j);
      }
      
      if (bitArr.arr[nStart + nLength - 1]) // check if signed
        temp -= (int)Math.pow(2, nLength);

      return temp;
  }
	public void removeBuffer(int nStartBit, int nBitsLength)
	{
		bitArr.removeBuffer(nStartBit, nBitsLength);
	}
	public void clear()
	{
		bitArr.removeBuffer(0, bitArr.getSize());
	}
	
	public BitArray getBitArray()
	{
		return bitArr;
	}
	public int getBytesLength()
  {      
		if (bitArr == null)
			return 0;
		else
			return (int)(bitArr.getSize() / 8) + ((bitArr.getSize() % 8) > 0 ? 1 : 0);      
  }
	public byte[] getBytes()
  {      
      byte[] bytes = new byte[(int)(bitArr.getSize() / 8) + ((bitArr.getSize() % 8) > 0 ? 1 : 0)];
      bitArr.copyTo(bytes, 0);
      return bytes;
  }
}
