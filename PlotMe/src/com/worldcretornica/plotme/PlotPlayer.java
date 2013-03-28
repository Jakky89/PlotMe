package com.worldcretornica.plotme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;


public class PlotPlayer implements Comparable<PlotPlayer>
{
	private int id;
	private Player player;
	private String realname;
	private String displayname;
	private Integer lastonline;
	private HashSet<Plot> plotswithrights;


	public PlotPlayer(int playerId, String playerName)
	{
		id = playerId;
		player = null;
		realname = playerName;
		displayname = playerName;
		lastonline = -1;
		plotswithrights = new HashSet<Plot>();
	}
		
	public PlotPlayer(int playerId, String playerName, String displayName, int lastOnlineTime)
	{
		id = playerId;
		player = null;
		realname = playerName;
		displayname = displayName;
		lastonline = lastOnlineTime;
		plotswithrights = new HashSet<Plot>();
	}
	
	public boolean hasRight(Plot plot, char right)
	{
		if (plot == null)
		{
			return false;
		}
		return plot.hasFlag(this, right);
	}
		
	public void refreshLastOnlineTime()
	{
		int newLastOnline = Math.round(System.currentTimeMillis() / 1000);
		if (newLastOnline != lastonline)
		{
			lastonline = newLastOnline;
			PlotDatabase.updateIntegerCell(id, "players", "lastonline", newLastOnline);
		}
	}
	
	public void setMinecraftPlayer(Player minecraftPlayer)
	{
		player = minecraftPlayer;
		realname = minecraftPlayer.getName();
		displayname = minecraftPlayer.getDisplayName();
		if (minecraftPlayer.isOnline())
			refreshLastOnlineTime();
	}

	public int getId()
	{
		return id;
	}
	
	public String getName()
	{
		return realname;
	}
	
	public String getDisplayName()
	{
		if (displayname != null)
		{
			return displayname;
		}
		return getName();
	}
	
	public void setDisplayName(String newDisplayName)
	{
		if ((newDisplayName == null && displayname != null) || (displayname == null && newDisplayName != null) || !newDisplayName.equals(displayname))
		{
			displayname = newDisplayName;
			PlotDatabase.updateStringCell(id, "players", "displayname", newDisplayName);
		}
	}
	
	public void addPlotWithRights(Plot plot)
	{
		plotswithrights.add(plot);
	}
	
	public void removePlotWithRights(Plot plot)
	{
		plotswithrights.remove(plot);
	}

	public Player getPlayer()
	{
		return player;
	}
	
	public List<Plot> getOwnPlots(PlotWorld worldFilter)
	{
		if (plotswithrights == null || plotswithrights.isEmpty())
		{
			return null;
		}
		Plot plot;
		List<Plot> tmpList = new ArrayList<Plot>();
		Iterator<Plot> plotIterator = plotswithrights.iterator();
		while (plotIterator.hasNext())
		{
			plot = plotIterator.next();
			if (plot.hasFlag(this, 'o') && (worldFilter == null || plot.getPlotWorld().equals(worldFilter)))
				tmpList.add(plot);
		}
		return tmpList;
	}
	
	public int getOwnPlotsCount(PlotWorld worldFilter)
	{
		if (plotswithrights == null || plotswithrights.isEmpty())
		{
			return 0;
		}
		int cnt = 0;
		Plot plot;
		Iterator<Plot> plotIterator = plotswithrights.iterator();
		while (plotIterator.hasNext())
		{
			plot = plotIterator.next();
			if (plot.hasFlag(this, 'o') && (worldFilter == null || plot.getPlotWorld().equals(worldFilter)))
				cnt++;
		}
		return cnt;
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
	    if (!(o instanceof PlotPlayer)) {
	    	return false;
	    }
		if (this.id == ((PlotPlayer)o).getId())
		{
			return true;
		}
		return false;
	}
	
	@Override
	public int compareTo(PlotPlayer p) {
		return id - p.getId();
	}
	
	@Override
	public String toString()
	{
		return "PLOTPLAYER" + String.valueOf(id);
	}
	
}
