package com.worldcretornica.plotme;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import com.worldcretornica.plotme.utils.Pair;
import com.worldcretornica.plotme.utils.PlotPosition;


public class Plot implements Comparable<Plot>
{
	public int id;
	
	public PlotPosition plotpos;
	public Plot[] plotneighbours;

	public String owner;
	public Biome biome;
	
	public Map<String, Object> properties; // Flexible plot properties
	public Set<String> playersinplot; // Names of players that are currently in that plot
	
	public long expireddate;
	public List<String[]> comments;
	public double customprice;
	public boolean isforsale;
	public long finisheddate;
	public boolean isprotected;
	public boolean isauctionned;
	public List<Pair<String, Double>> bids;
	
	public void setPlotPosition(int tX, int tZ)
	{
		plotpos = new PlotPosition(tX, tZ);
	}
	
	public void setPlotPosition(PlotWorld tW, int tX, int tZ)
	{
		plotpos = new PlotPosition(tW, tX, tZ);
	}
	
	public Plot()
	{
		id = 0;
		owner = null;
		properties = new HashMap<String, Object>();
		playersinplot = new HashSet<String>();
		biome = Biome.PLAINS;
		expireddate = Math.round(System.currentTimeMillis()/1000) + 604800; // 604800 = 7 days
		comments = new ArrayList<String[]>();
		customprice = 0;
		isforsale = false;
		finisheddate = 0;
		isprotected = false;
		isauctionned = false;
	}
	
	public Plot(int mid, PlotWorld mW, int mX, int mZ, String mowner, String mbiome, long mexpireddate, double mcustomprice, boolean misforsale,
				long mfinisheddate, boolean misprotected, boolean misauctionned)
	{
		id = mid;
		owner = mowner;
		setPlotPosition(mW, mX, mZ);
		biome = Biome.valueOf(mbiome);
		expireddate = Math.round(System.currentTimeMillis()/1000) + mexpireddate;
		customprice = mcustomprice;
		isforsale = misforsale;
		finisheddate = mfinisheddate;
		isprotected = misprotected;
		isauctionned = misauctionned;
	}

	public void setExpire(int days)
	{
		if (days >= 0)
		{
			updateField("expireddate", Math.round(System.currentTimeMillis()/1000) + (days*86400));
		}
	}
	
	public void resetExpire(int days)
	{
		if (days <= 0)
		{
			setExpire(Math.round(System.currentTimeMillis()/1000));
		}
		else
		{
			setExpire(days);
		}
	}
	
	public String getExpire()
	{
		return DateFormat.getDateInstance().format(expireddate);
	}
	
	public void setFinished()
	{
		finisheddate = Math.round(System.currentTimeMillis()/1000);
		SqlManager.updatePlot(id, "finisheddate", finisheddate);
	}
	
	public void setUnfinished()
	{
		finisheddate = 0;
		SqlManager.updatePlot(id, "finisheddate", null);
	}
	
	public Biome getBiome()
	{
		return biome;
	}
	
	public String getOwner()
	{
		return owner;
	}
	
	public int getCommentsCount()
	{
		return comments.size();
	}
	
	public String[] getComments(int i)
	{
		return comments.get(i);
	}
	
	public void setProperty(String property, Object value)
	{
		if (property == null || property.isEmpty())
		{
			return;
		}

		property = property.toLowerCase();
		
		if (value != null)
		{
			if (properties == null)
			{
				properties = new HashMap<String, Object>();
			}
			if (properties.put(property, value) != value)
			{
				properties.put(property, value);
				SqlManager.savePlotProperties(this);
			}
		}
		else
		{
			if (properties != null)
			{
				if (properties.remove(property) != null)
				{
					SqlManager.savePlotProperties(this);
				}
			}
		}
	}
	
	public Object getProperty(String property)
	{
		return properties.get(property.toLowerCase());
	}
	
	public Boolean getBooleanProperty(String property)
	{
		Boolean tmpprop = (Boolean)getProperty(property);
		if (tmpprop != null)
		{
			return tmpprop;
		}
		return false;
	}

	public boolean isAllowed(String name, boolean includeStar, boolean includeGroup)
	{
		if (name == null || name.isEmpty())
		{
			return false;
		}
		
		if (getBooleanProperty("allowall")==true)
		{
			return true;
		}
		
		Player plr = Bukkit.getServer().getPlayerExact(name);
		if (plr != null)
		{
			if (owner != null && owner.equalsIgnoreCase(plr.getName()) || (includeStar && owner.equals("*")))
			{
				return true;
			}
			
			if (includeStar && getBooleanProperty("rights:all:allowed"))
			{
				return true;
			}
			
			if (getBooleanProperty("rights:player:" + plr.getName() + ":allowed"))
			{
				return true;
			}
			
			if (includeGroup && owner.toLowerCase().startsWith("group:"))
			{
				if (plr.hasPermission("plotme.group." + owner.substring(6)))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isDenied(String name)
	{
		if (name == null || name.isEmpty())
		{
			return true;
		}
		
		Player plr = Bukkit.getServer().getPlayerExact(name);
		if (plr != null)
		{
			if (owner != null && owner.equalsIgnoreCase(plr.getName()))
			{
				return false;
			}
		}
		
		if (getBooleanProperty("denyall")==false)
		{
			return false;
		}

		if (isAllowed(name, false, false))
		{
			return false;
		}
		
		if (getBooleanProperty("rights.*.denied")==true)
		{
			return true;
		}
		
		if (getBooleanProperty("rights." + name + ".denied")==true)
		{
			return true;
		}

		return true;
	}

	public void updateField(String field, Object value)
	{
		SqlManager.updatePlot(id, field, value);
	}

	@Override
	public int compareTo(Plot plot2)
	{
		return this.id-plot2.id;
	}
	
	/*private static Map<String, Double> sortByValues(final Map<String, Double> map) 
	{
		Comparator<String> valueComparator = new Comparator<String>() 
		{
		    public int compare(String k1, String k2) 
		    {
		        int compare = map.get(k2).compareTo(map.get(k1));
		        if (compare == 0) 
		        	return 1;
		        else 
		        	return compare;
		    }
		};
		
		Map<String, Double> sortedByValues = new TreeMap<String, Double>(valueComparator);
		sortedByValues.putAll(map);
		return sortedByValues;
	}*/
}
