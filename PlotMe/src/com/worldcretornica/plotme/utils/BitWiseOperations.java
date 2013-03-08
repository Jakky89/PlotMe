package com.worldcretornica.plotme.utils;

public class BitWiseOperations {

	public static int setBit(int currentBits, int bitPosition)
	{
		return currentBits | (1 << bitPosition);
	}

	public static int clearBit(int currentBits, int bitPosition)
	{
		return currentBits & ~(1 << bitPosition);
	}
	
	public static int flipBit(int currentBits, int bitPosition)
	{
		return currentBits ^ (1 << bitPosition);
	}
	
	public static boolean isBitSet(int currentBits, int bitPosition)
	{
		  int mask = 1 << bitPosition;
		  return (currentBits & mask) == mask;
	}

}
