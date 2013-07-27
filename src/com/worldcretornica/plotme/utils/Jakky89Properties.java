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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class Jakky89Properties {

	public Map<String, Object> properties;

	public Jakky89Properties()
	{
		properties = new HashMap<String, Object>();
	}

	public boolean setValue(String pkey, Object pvalue)
	{
		if (pkey == null || pkey.isEmpty())
		{
			return false;
		}
		pkey = pkey.toLowerCase();
		if (pvalue != null)
		{
			if (properties.put(pkey, pvalue) != pvalue)
			{
				return true;
			}
		}
		else
		{
			if (properties.remove(pkey) != null)
			{
				return true;
			}
		}
		return false;
	}
	
	public Object getValue(String pkey)
	{
		if (pkey == null)
		{
			return null;
		}
		return properties.get(pkey.toLowerCase());
	}

	@SuppressWarnings("unchecked")
	public boolean addStringToHashSet(String pkey, String pvalue)
	{
		HashSet<String> tmpSet;
		Object pval = getValue(pkey);
		if (pval != null && (pval instanceof HashSet))
		{
			tmpSet = (HashSet<String>)pval;
		}
		else
		{
			tmpSet = new HashSet<String>();
		}
		if (tmpSet.add(pvalue))
		{
			setValue(pkey, tmpSet);
			return true;
		}
		return false;
	}
		
	public Jakky89Properties getProperties(String pkey)
	{
		Object pval = getValue(pkey);
		if (pval != null && (pval instanceof Jakky89Properties))
		{
			return (Jakky89Properties)pval;
		}

		return null;
	}
	
	public Jakky89Properties getCreateProperties(String pkey)
	{
		Jakky89Properties props = getProperties(pkey);
		if (props == null)
		{
			props = new Jakky89Properties();
			this.properties.put(pkey, props);
		}
		return props;
	}

	public boolean getBoolean(String pkey)
	{
		Object pval = getValue(pkey);
		if (pval != null && (pval instanceof Boolean))
		{
			return (boolean)pval;
		}
		return false;
	}

	public String getString(String pkey)
	{
		Object pval = getValue(pkey);
		if (pval != null && (pval instanceof String))
		{
			return (String)pval;
		}
		return null;
	}

	public Integer getInteger(String pkey)
	{
		Object pval = getValue(pkey);
		if (pval != null && (pval instanceof Integer))
		{
			return (Integer)pval;
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	public List<String> getStringArrayList(String pkey)
	{
		Object pval = getValue(pkey);
		if (pval != null && (pval instanceof ArrayList))
		{
			return (ArrayList<String>)pval;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getLinkedStringList(String pkey)
	{
		Object pval = getValue(pkey);
		if (pval != null && (pval instanceof ArrayList))
		{
			return (ArrayList<String>)pval;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Pair<String, String>> getStringPairArrayList(String pkey)
	{
		Object pval = getValue(pkey);
		if (pval != null && (pval instanceof ArrayList))
		{
			return (ArrayList<Pair<String, String>>)pval;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Pair<String, String>> getLinkedStringPairList(String pkey)
	{
		Object pval = getValue(pkey);
		if (pval != null && (pval instanceof LinkedList))
		{
			return (LinkedList<Pair<String, String>>)pval;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, String> getStringHashMap(String pkey)
	{
		Object pval = getValue(pkey);
		if (pval != null && (pval instanceof HashMap))
		{
			return (HashMap<String, String>)pval;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public HashSet<String> getStringHashSet(String pkey)
	{
		Object pval = getValue(pkey);
		if (pval != null && (pval instanceof HashSet))
		{
			return (HashSet<String>)pval;
		}
		return null;
	}

	public boolean isStringInHashSet(String pkey, String vkey)
	{
		Object pval = getValue(pkey);
		if (pval != null && (pval instanceof HashSet))
		{
			@SuppressWarnings("unchecked")
			HashSet<Object> vhs = (HashSet<Object>)pval;
			if (vhs.contains(vkey))
			{
				return true;
			}
		}
		return false;
	}

	public HashMap<String, Object> getMap()
	{
		if (properties != null)
		{
			return new HashMap<String, Object>(properties);
		}
		return null;
	}

}
