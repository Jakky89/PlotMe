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
	private Set<Plot> ownplots;

	
	public PlotPlayer(int playerId, String playerName)
	{
		id = playerId;
		player = null;
		realname = playerName;
		displayname = playerName;
		ownplots = null;
		PlotManager.registerPlotPlayer(this);
	}
	public PlotPlayer(int playerId, String playerName, String displayName)
	{
		id = playerId;
		player = null;
		realname = playerName;
		displayname = displayName;
		ownplots = null;
		PlotManager.registerPlotPlayer(this);
	}
	
	public PlotPlayer(int ownerId, Player minecraftPlayer)
	{
		id = ownerId;
		player = minecraftPlayer;
		realname = minecraftPlayer.getName();
		displayname = minecraftPlayer.getDisplayName();
		ownplots = null;
		PlotManager.registerPlotPlayer(this);
	}
	
	public void setMinecraftPlayer(Player minecraftPlayer)
	{
		player = minecraftPlayer;
		realname = minecraftPlayer.getName();
		displayname = minecraftPlayer.getDisplayName();
	}
	
	public int getId()
	{
		return id;
	}
	
	public String getRealName()
	{
		return realname;
	}
	
	public String getDisplayName()
	{
		return displayname;
	}
	
	public void addOwnPlot(Plot plot)
	{
		if (ownplots == null)
		{
			ownplots = new HashSet<Plot>();
		}
		this.ownplots.add(plot);
	}
	
	public void removeOwnPlot(Plot plot)
	{
		if (ownplots == null || ownplots.isEmpty())
		{
			return;
		}
		this.ownplots.remove(plot);
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
	public boolean equals(Object o)
	{
	    if (o == null) {
	    	return false;
	    }
	    if (!(o instanceof PlotPlayer)) {
	    	return false;
	    }
		if (this.id == ((PlotPlayer)o).id)
		{
			return true;
		}
		return false;
	}
	
	@Override
	public int compareTo(PlotPlayer po) {
		return this.id - po.getId();
	}
	
}
