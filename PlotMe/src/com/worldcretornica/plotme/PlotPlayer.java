package com.worldcretornica.plotme;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;



public class PlotPlayer implements Comparable<PlotPlayer>
{
	
	private Integer id;
	private Player player;
	private String realname;
	private String displayname;
	private Integer lastonline;
	private Set<Plot> ownplots;
	private Set<PlotGroup> plotgroups;
	
	public PlotPlayer(int playerId, String playerName)
	{
		id = playerId;
		player = null;
		realname = playerName;
		displayname = playerName;
		ownplots = null;
		lastonline = null;
		PlotManager.registerPlotPlayer(this);
	}
	
	public PlotPlayer(int playerId, String playerName, int lastOnlineTime)
	{
		id = playerId;
		player = null;
		realname = playerName;
		displayname = playerName;
		ownplots = null;
		lastonline = lastOnlineTime;
		PlotManager.registerPlotPlayer(this);
	}
	
	public PlotPlayer(int playerId, String playerName, String displayName, int lastOnlineTime)
	{
		id = playerId;
		player = null;
		realname = playerName;
		displayname = displayName;
		ownplots = null;
		lastonline = lastOnlineTime;
		PlotManager.registerPlotPlayer(this);
	}
	
	public PlotPlayer(int playerId, String playerName, String displayName)
	{
		id = playerId;
		player = null;
		realname = playerName;
		displayname = displayName;
		ownplots = null;
		lastonline = null;
		PlotManager.registerPlotPlayer(this);
	}
	
	public PlotPlayer(int ownerId, Player minecraftPlayer)
	{
		id = ownerId;
		setMinecraftPlayer(minecraftPlayer);
		ownplots = null;
		PlotManager.registerPlotPlayer(this);
	}
	
	public void refreshLastOnlineTime()
	{
		int newLastOnline = Math.round(System.currentTimeMillis() / 1000);
		if (newLastOnline == lastonline)
		{
			return;
		}
		lastonline = newLastOnline;
		PlotDatabase.updateData(id, "players", "lastonline", lastonline);
	}
	
	public void setMinecraftPlayer(Player minecraftPlayer)
	{
		player = minecraftPlayer;
		realname = minecraftPlayer.getName();
		displayname = minecraftPlayer.getDisplayName();
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
		return displayname;
	}
	
	public void setDisplayName(String newDisplayName)
	{
		if (!newDisplayName.equals(displayname))
		{
			displayname = newDisplayName;
			PlotDatabase.updateData(id, "players", "displayname", newDisplayName);
		}
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
	
	public void addToPlotGroup(PlotGroup plotGroup)
	{
		plotgroups.add(plotGroup);
		plotGroup.addPlotPlayer(this);
	}
	
	public void removeFromPlotGroup(PlotGroup plotGroup)
	{
		plotgroups.remove(plotGroup);
		plotGroup.removePlotPlayer(this);
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public int getOwnPlotsCount()
	{
		if (ownplots == null || ownplots.isEmpty())
		{
			return 0;
		}
		return ownplots.size();
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
	public int compareTo(PlotPlayer po) {
		return id - po.getId();
	}
	
	@Override
	public String toString()
	{
		return "PLOTPLAYER" + String.valueOf(id);
	}
	
}
