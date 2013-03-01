package com.worldcretornica.plotme;

public class PlotOwner implements Comparable<PlotOwner>
{
	
	public int id;
	
	public String playername;

	
	public PlotOwner(int id, String ownerName)
	{
		this.id = id;
		this.playername = ownerName;
	}
	
	@Override
	public int compareTo(PlotOwner owner2) {
		return this.id-owner2.id;
	}
	
}
