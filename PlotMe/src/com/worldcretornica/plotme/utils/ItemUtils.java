package com.worldcretornica.plotme.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Material;

public class ItemUtils {
	
	public static Pair<Integer, Byte> stringToItemIdValue(String itemStr)
	{
		if (itemStr == null)
			return null;
		
		itemStr = itemStr.trim();
		if (itemStr.isEmpty())
		{
			return null;
		}
		
		Integer iid;
		Byte iiv = null;
		String itemTypeStr;
		String itemDataStr;
		
		int dbp = itemStr.indexOf(':');
		if (dbp > 0 || dbp >= itemStr.length()-1)
		{
			itemTypeStr = itemStr.substring(0, dbp);
			itemDataStr = itemStr.substring(dbp);
			itemDataStr = itemDataStr.trim();
			if (!itemDataStr.isEmpty())
			{
				try
				{
					iiv = Byte.parseByte(itemDataStr);
				} catch (NumberFormatException ex) {}
			}
		}
		else if (dbp == 0)
		{
			return null;
		}
		else
		{
			itemTypeStr = itemStr;
		}
		itemTypeStr = itemTypeStr.trim();
		if (itemTypeStr.isEmpty())
		{
			return null;
		}
		try
		{
			iid = Integer.parseInt(itemTypeStr);
			if (iid < 0)
			{
				return null;
			}
		}
		catch (NumberFormatException ex)
		{
			try
			{
				iid = Material.valueOf(itemTypeStr.toUpperCase()).getId();
			}
			catch (IllegalArgumentException ia)
			{
				return null;
			}
		}
		
		return new Pair<Integer, Byte>(iid, iiv);
	}
	
	public static List<Pair<Integer, Byte>> stringListToItemIdValues(Collection<String> itemStrList)
	{
		String tmpItemStr;
		Pair<Integer, Byte> tmpEntry;
		List<Pair<Integer, Byte>> tmpList = new ArrayList<Pair<Integer, Byte>>();
		Iterator<String> piIterator = itemStrList.iterator();
		while (piIterator.hasNext())
		{
			tmpItemStr = piIterator.next();
			if (tmpItemStr == null || tmpItemStr.isEmpty())
				continue;
			tmpEntry = stringToItemIdValue(tmpItemStr);
			if (tmpEntry != null)
			{
				tmpList.add(tmpEntry);
			}
		}
		return tmpList;
	}
	
	public static String itemIdValueToString(Pair<Integer, Byte> itemIdValue)
	{
		if (itemIdValue == null || itemIdValue.getLeft() == null || itemIdValue.getLeft() < 0)
			return null;
		
		String itemString = Material.getMaterial(itemIdValue.getLeft()).toString();
		if (itemString == null || itemString.isEmpty())
		{
			itemString = itemIdValue.getLeft().toString();
		}
		if (itemIdValue.getRight() != null && itemIdValue.getRight() >= 0)
		{
			itemString = itemString + ":" + itemIdValue.getRight().toString();
		}
		return itemString;
	}
	
	public static List<String> itemIdValuesToStringList(Collection<Pair<Integer, Byte>> itemIdValueList)
	{
		Pair<Integer, Byte> tmpEntry;
		String tmpItemStr;
		List<String> tmpList = new ArrayList<String>();
		Iterator<Pair<Integer, Byte>> piIterator = itemIdValueList.iterator();
		while (piIterator.hasNext())
		{
			tmpEntry = piIterator.next();
			if (tmpEntry == null)
				continue;
			tmpItemStr = itemIdValueToString(tmpEntry);
			if (tmpItemStr != null && !tmpItemStr.isEmpty())
			{
				tmpList.add(tmpItemStr);
			}
		}
		return tmpList;
	}
	
}
