/*
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
*/


package com.worldcretornica.plotme.utils;

import org.bukkit.Material;

public class Jakky89ItemIdData {

	private Short typeId;
	// We need to use short instead of byte because of potions data values go to max 32768
	private Short dataValue;
	
	
	public Jakky89ItemIdData(Short itemTypeId, Short itemDataValue)
	{
		typeId = itemTypeId;
		dataValue = itemDataValue;
	}
	
	public Jakky89ItemIdData(Material itemMaterial, Short itemDataValue)
	{
		typeId = (short)itemMaterial.getId();
		dataValue = itemDataValue;
	}
	
	public Jakky89ItemIdData(Short itemTypeId)
	{
		typeId = itemTypeId;
		dataValue = null;
	}
	
	public Jakky89ItemIdData(Material itemMaterial)
	{
		typeId = (short)itemMaterial.getId();
		dataValue = null;
	}
	
	public Short getTypeId()
	{
		return typeId;
	}
	
	public Short getDataValue()
	{
		return dataValue;
	}
	
	public void setTypeId(Short itemTypeId)
	{
		typeId = itemTypeId;
	}
	
	public void setDataValue(Short itemDataValue)
	{
		dataValue = itemDataValue;
	}
	
	@Override
	public String toString()
	{
		if (typeId != null)
		{
			try
			{
				// Try to get the material name (preferred to use this instead of number)
				if (dataValue != null && dataValue >= 0)
					return Material.getMaterial(typeId).toString() + ":" + String.valueOf(dataValue);
				else
					return Material.getMaterial(typeId).toString();
			}
			catch (IllegalArgumentException ex) {}
			
			if (dataValue != null && dataValue >= 0)
				return String.valueOf(typeId) + ":" + String.valueOf(dataValue);
			else
				return String.valueOf(typeId);
		}
		return "NONE";
	}
	
	@Override
	public int hashCode()
	{
		return (typeId * 32769) + dataValue;
	}
	
}
