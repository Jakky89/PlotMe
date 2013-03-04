package com.worldcretornica.plotme;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;



public class PlotOwner implements Comparable<PlotOwner>
{
	
	private int id;
	private List<Plot> ownedplots;
	private Player minecraftplayer;
	private String realname;
	private String displayname;
	private Set<PlotOwner> friends;

	
	public PlotOwner(int id, String ownerName)
	{
		this.id = id;
		this.realname = ownerName;
		this.displayname = ownerName;
		this.ownedplots = null;
		PlotManager.registerPlotOwner(this);
	}
	public PlotOwner(int id, String ownerName, String displayName)
	{
		this.id = id;
		this.realname = ownerName;
		this.displayname = displayName;
		this.ownedplots = null;
		PlotManager.registerPlotOwner(this);
	}
	
	public PlotOwner(int id, Player minecraftPlayer)
	{
		id = id;
		minecraftplayer = minecraftPlayer;
		realname = minecraftPlayer.getName();
		displayname = minecraftPlayer.getDisplayName();
		ownedplots = null;
		PlotManager.registerPlotOwner(this);
	}
	
	public void setMinecraftPlayer(Player minecraftPlayer)
	{
		minecraftplayer = minecraftPlayer;
		realname = minecraftPlayer.getName();
		displayname = minecraftPlayer.getDisplayName();
		PlotManager.registerPlotOwner(this);
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
	
	public void addOwnedPlot(Plot plot)
	{
		if (ownedplots == null)
		{
			ownedplots = new LinkedList<Plot>();
		}
		this.ownedplots.add(plot);
	}
	
	public void removeOwnedPlot(Plot plot)
	{
		if (ownedplots == null || ownedplots.isEmpty())
		{
			return;
		}
		this.ownedplots.remove(plot);
	}
	
	public int ownedPlotsCount()
	{
		if (ownedplots == null || ownedplots.isEmpty())
		{
			return 0;
		}
		return ownedplots.size();
	}
	
	@Override
	public boolean equals(Object o)
	{
	    if (o == null) {
	    	return false;
	    }
	    if (!(o instanceof PlotOwner)) {
	    	return false;
	    }
		if (this.id == ((PlotOwner)o).id)
		{
			return true;
		}
		return false;
	}
	
	@Override
	public int compareTo(PlotOwner po) {
		return this.id - po.id;
	}
	
}
