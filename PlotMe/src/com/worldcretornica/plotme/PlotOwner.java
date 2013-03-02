package com.worldcretornica.plotme;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;


public class PlotOwner implements Comparable<PlotOwner>
{
	
	public int id;
	public List<Plot> ownedplots;
	public Player minecraftplayer;
	public String playername;
	public String displayname;

	
	public PlotOwner(int id, String ownerName)
	{
		this.id = id;
		this.playername = ownerName;
		this.displayname = ownerName;
		this.ownedplots = new LinkedList<Plot>();
	}
	
	public PlotOwner(int id, Player minecraftPlayer)
	{
		this.id = id;
		this.minecraftplayer = minecraftPlayer;
		this.playername = minecraftPlayer.getName();
		this.displayname = minecraftPlayer.getDisplayName();
		this.ownedplots = new LinkedList<Plot>();
	}
	
	public void addOwnedPlot(Plot plot)
	{
		this.ownedplots.add(plot);
	}
	
	public void removeOwnedPlot(Plot plot)
	{
		this.ownedplots.remove(plot);
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
