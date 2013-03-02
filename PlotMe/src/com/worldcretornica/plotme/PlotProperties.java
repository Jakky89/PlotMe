package com.worldcretornica.plotme;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;


public class PlotProperties {
	
	public Plot plot;
	public PlotProperties parent;
	public Map<String, Object> properties;

	public PlotProperties(Plot forPlot)
	{
		plot = forPlot;
		parent = null;
		properties = new HashMap<String, Object>();
	}
	
	public PlotProperties(Plot forPlot, PlotProperties parent)
	{
		plot = forPlot;
		properties = new HashMap<String, Object>();
		this.parent = parent;
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
		
	public Object getValue(String pkey)
	{
		if (pkey == null)
		{
			return null;
		}

		return properties.get(pkey.toLowerCase());
	}
	
	public PlotProperties getProperties(String pkey)
	{
		Object pval = getValue(pkey);
		if (pval != null && (pval instanceof PlotProperties))
		{
			return (PlotProperties)pval;
		}

		return null;
	}
	
	public PlotProperties getCreateProperties(String pkey)
	{
		PlotProperties props = getProperties(pkey);
		if (props == null)
		{
			props = new PlotProperties(plot, this);
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
