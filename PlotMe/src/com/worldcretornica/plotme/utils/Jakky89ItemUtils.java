/**
.............-:*=@@WW@@=*:-..........................-:*=@@WW@@=*:-............
.........-=@WWWWWWWWWWWWWWWW@=-..................-=@WWWWWWWWWWWWWWWW@=-........
.......*WWWWW#+-........:+#WWWWW*..............*WWWWW#+:........:+#WWWWW*......
.....=WWW@*-.......--.......-*WWWW=..........=WWW@*-................-*WWWW=....
...+WWW@:........*WWWW*........:@WWW+......+WWW@:...........-..........:@WWW+..
..=WWW*.........-#WWWW#-.........*WWW=....=WWW*.......-=WWWWWWWW@+-......*WWW=.
.*WWW:............+##+............:WWW*..*WWW:......:@WWWWWWWWWWWWW#-.....:WWW*
:@WW+..........=WWWWWWWW=..........+WW@::@WW+......:WWWW@:....-*WWWW@:.....+WW@
=WW#...........#WWWWWWWW#...........#WW==WW#.....-:#WWW@+-......-WWWW@......#WW
@WW*...........#WWWWWWWW#...........*WW@@WW*......-#WWW=-........*WWWW:.....*WW
WWW*...........#WWWWWWWW#...........*WWWWWW*........-=-..........*WWWW+.....*WW
@WW=...........#WWWWWWWW#...........=WW@@WW=.......-----.........#WWWW-.....=WW
*WW@-...........-@WWWW@-...........:@WW**WW@:......#WWWW=.......*WWWW*.....:@WW
-#WW#...........-@WWWW@-...........#WW#--#WW#......-#WWWWW#***#WWWWW*......#WW#
.-@WW#-.........-@WWWW@-.........-#WW@-..-@WW#-......:#WWWWWWWWWWW=-.....-#WW@-
..-@WW@:........-@WWWW@-........+WWW@-....-@WW@+........-+==#=*:........+WWW@-.
....*WWW@+......-#@@@@#-......+@WWW*........*WWW@+....................+@WWW*...
.....-=WWWW#:..............:#WWWW=-..........-=WWWW#:..............:#WWWW=-....
........+@WWWWW@#=****=#@WWWWW@+................+@WWWWW@#=****=#@WWWWW@+.......
...........-*#@WWWWWWWWWW@#*-......................-*#@WWWWWWWWWW@#*-..........
###############################################################################

Licensed under CreativeCommons - Attribution-ShareAlike

You are free
> to Share — to copy, distribute and transmit the work
> to Remix — to adapt the work
> to make commercial use of the work

Under the following conditions:
> Attribution — You must attribute the work in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
> Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.

With the understanding that:
> Waiver — Any of the above conditions can be waived if you get permission from the copyright holder.
> Public Domain — Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
> Other Rights — In no way are any of the following rights affected by the license:
> Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
> The author's moral rights;
> Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.

>>> http://creativecommons.org/licenses/by-sa/3.0/ | http://creativecommons.org/licenses/by-sa/3.0/legalcode <<<
**/

package com.worldcretornica.plotme.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;


public class Jakky89ItemUtils {
	
	public static Jakky89ItemIdData stringToItemIdData(String itemStr)
	{
		if (itemStr == null)
			return null;
		
		itemStr = itemStr.trim();
		if (itemStr.isEmpty())
			return null;
		
		Short iid = null;
		Short iiv = null;
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
					iiv = Short.parseShort(itemDataStr);
				}
				catch (NumberFormatException ex)
				{
					iiv = null;
				}
			}
		}
		else if (dbp == -1)
			itemTypeStr = itemStr;
		else
			return null;
		
		itemTypeStr = itemTypeStr.trim();
		if (itemTypeStr.isEmpty())
			return null;
		
		if (itemTypeStr.matches("\\d+"))
		{
			try
			{
				iid = Short.parseShort(itemTypeStr);
				if (iid < 0)
					return null;
			}
			catch (NumberFormatException ex)
			{
				iid = null;
			}
		}
		if (iid == null)
		{
			try
			{
				iid = (short)Material.valueOf(itemTypeStr.toUpperCase()).getId();
			}
			catch (IllegalArgumentException ia)
			{
				return null;
			}
		}
		
		if (iid == null || iid < 0)
			return null;
		
		return new Jakky89ItemIdData(iid, iiv);
	}
	
	public static List<Jakky89ItemIdData> stringListToItemIdDataList(Collection<String> itemStrList)
	{
		String tmpItemStr;
		Jakky89ItemIdData tmpEntry;
		List<Jakky89ItemIdData> tmpList = new ArrayList<Jakky89ItemIdData>();
		
		Iterator<String> piIterator = itemStrList.iterator();
		while (piIterator.hasNext())
		{
			tmpItemStr = piIterator.next();
			if (tmpItemStr == null || tmpItemStr.isEmpty() || tmpItemStr == "NONE")
				continue;
			tmpEntry = stringToItemIdData(tmpItemStr);
			if (tmpEntry != null)
				tmpList.add(tmpEntry);
		}
		
		return tmpList;
	}
	
	public static List<Jakky89ItemIdData> materialListToItemIdDataList(Collection<Material> itemMaterialList)
	{
		Material tmpItemMaterial;
		Jakky89ItemIdData tmpEntry;
		List<Jakky89ItemIdData> tmpList = new ArrayList<Jakky89ItemIdData>();
		
		Iterator<Material> piIterator = itemMaterialList.iterator();
		while (piIterator.hasNext())
		{
			tmpItemMaterial = piIterator.next();
			if (tmpItemMaterial == null)
				continue;
			tmpEntry = new Jakky89ItemIdData(tmpItemMaterial);
			if (tmpEntry != null)
				tmpList.add(tmpEntry);
		}
		
		return tmpList;
	}
	
	public static String itemIdDataToString(Jakky89ItemIdData itemIdData)
	{
		if (itemIdData == null || itemIdData.getTypeId() == null || itemIdData.getTypeId() < 0 || (itemIdData.getDataValue() != null && itemIdData.getDataValue() < 0))
			return "NONE";

		try {
			// Try to get the material name (preferred to use this instead of number)
			if (itemIdData.getDataValue() != null && itemIdData.getDataValue() >= 0)
				return Material.getMaterial(itemIdData.getTypeId()).toString() + ":" + String.valueOf(itemIdData.getDataValue());
			else
				return Material.getMaterial(itemIdData.getTypeId()).toString();
		}
		catch (IllegalArgumentException ex) {}

		// Fallback
		if (itemIdData.getDataValue() != null && itemIdData.getDataValue() >= 0)
			return String.valueOf(itemIdData.getTypeId()) + ":" + String.valueOf(itemIdData.getDataValue());
		else
			return String.valueOf(itemIdData.getTypeId());
	}
	
	public static List<String> itemIdValuesToStringList(Collection<Jakky89ItemIdData> itemIdValueList)
	{
		Jakky89ItemIdData tmpEntry;
		String tmpItemStr;
		List<String> tmpList = new ArrayList<String>();
		Iterator<Jakky89ItemIdData> piIterator = itemIdValueList.iterator();
		while (piIterator.hasNext())
		{
			tmpEntry = piIterator.next();
			if (tmpEntry == null)
				continue;
			tmpItemStr = itemIdDataToString(tmpEntry);
			if (tmpItemStr != null && !tmpItemStr.isEmpty())
				tmpList.add(tmpItemStr);
		}
		return tmpList;
	}
	
}
