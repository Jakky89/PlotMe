package com.worldcretornica.plotme.utils;

import org.bukkit.Material;

public class Jakky89ItemIdData {

	private short typeId;
	// We need to use short instead of byte because of potions data values go to max 32768
	private short dataValue;
	
	
	public Jakky89ItemIdData(short itemTypeId, short itemDataValue)
	{
		typeId = itemTypeId;
		dataValue = itemDataValue;
	}
	
	public Jakky89ItemIdData(Material itemMaterial, short itemDataValue)
	{
		typeId = (short)itemMaterial.getId();
		dataValue = itemDataValue;
	}
	
	public Jakky89ItemIdData(short itemTypeId)
	{
		typeId = itemTypeId;
		dataValue = 0;
	}
	
	public Jakky89ItemIdData(Material itemMaterial)
	{
		typeId = (short)itemMaterial.getId();
		dataValue = 0;
	}
	
	public short getTypeId()
	{
		return typeId;
	}
	
	public short getDataValue()
	{
		return dataValue;
	}
	
	public void setTypeId(short itemTypeId)
	{
		typeId = itemTypeId;
	}
	
	public void setDataValue(short itemDataValue)
	{
		dataValue = itemDataValue;
	}
	
	@Override
	public String toString()
	{
		try
		{
			// Try to get the material name (preferred to use this instead of number)
			if (dataValue > 0)
			{
				return Material.getMaterial(typeId).toString() + ":" + String.valueOf(dataValue);
			}
			else
			{
				return Material.getMaterial(typeId).toString();
			}
		}
		catch (IllegalArgumentException ex) {}
			
		if (dataValue >= 0)
			return String.valueOf(typeId) + ":" + String.valueOf(dataValue);
		else
			return String.valueOf(typeId);
	}
	
	@Override
	public int hashCode()
	{
		return (typeId * 32769) + dataValue;
	}
	
}
