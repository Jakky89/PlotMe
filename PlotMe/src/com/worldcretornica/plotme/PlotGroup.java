package com.worldcretornica.plotme;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;



public class PlotGroup implements Comparable<PlotGroup>
{
	
	private Integer id;
	private String groupname;
	private Set<PlotPlayer> plotplayers;
	private Set<Plot> ownplots;
	
	public PlotGroup(int groupId, String groupName)
	{
		id = groupId;
		groupname = groupName;
		plotplayers = null;
		ownplots = null;
	}
	
	public PlotGroup(int groupId, String groupName, int lastOnlineTime)
	{
		id = groupId;
		groupname = groupName;
		plotplayers = null;
		ownplots = null;
	}

	public void addPlotPlayer(PlotPlayer plotPlayer)
	{
		if (plotplayers == null)
		{
			plotplayers = new HashSet<PlotPlayer>();
		}
		plotplayers.add(plotPlayer);
	}
	
	public void removePlotPlayer(PlotPlayer plotPlayer)
	{
		if (plotplayers == null || plotplayers.isEmpty())
		{
			return;
		}
		plotplayers.remove(plotPlayer);
	}
	
	public int getId()
	{
		return id;
	}
	
	public String getName()
	{
		return groupname;
	}

	public void addOwnPlot(Plot plot)
	{
		if (ownplots == null)
		{
			ownplots = new HashSet<Plot>();
		}
		ownplots.add(plot);
	}
	
	public void removeOwnPlot(Plot plot)
	{
		if (ownplots == null || ownplots.isEmpty())
		{
			return;
		}
		ownplots.remove(plot);
	}
	
	public int ownPlotsCount()
	{
		if (ownplots == null || ownplots.isEmpty())
		{
			return 0;
		}
		return ownplots.size();
	}
	
	@Override
	public int hashCode()
	{
		return id;
	}
	
	@Override
	public boolean equals(Object o)
	{
	    if (o == null) {
	    	return false;
	    }
	    if (!(o instanceof PlotGroup)) {
	    	return false;
	    }
		if (this.id == ((PlotGroup)o).getId())
		{
			return true;
		}
		return false;
	}
	
	@Override
	public int compareTo(PlotGroup pg) {
		return id - pg.getId();
	}
	
	@Override
	public String toString()
	{
		return "PLOTGROUP" + String.valueOf(id);
	}
	
}
