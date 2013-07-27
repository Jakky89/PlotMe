package com.worldcretornica.plotme;

import org.bukkit.World;


public class PlotPosition {

	public final PlotWorld w;
	public final Integer x;
	public final Integer z;
	
	public PlotPosition(PlotWorld world, Integer xpos, Integer zpos)
	{
		this.w = world;
		this.x = xpos;
		this.z = zpos;
	}
	
	public int getPlotX()
	{
		return x;
	}
	
	public int getPlotZ()
	{
		return z;
	}
	
	public PlotWorld getPlotWorld()
	{
		return w;
	}
	
	public World getMinecraftWorld()
	{
		if (w != null)
		{
			return w.getMinecraftWorld();
		}
		return null;
	}

	@Override
	public final int hashCode()
	{
		return (w.hashCode() ^ x ^ z);
	}

	@Override
	public final boolean equals(Object o)
	{
	    if (o == null) {
	    	return false;
	    }
	    if (!(o instanceof PlotPosition)) {
	    	return false;
	    }
		PlotPosition pp = (PlotPosition)o;
		if (this.getMinecraftWorld() != null && pp.getMinecraftWorld() != null)
		{
			if (!this.getMinecraftWorld().getName().equals(pp.getMinecraftWorld().getName()))
			{
				return false;
			}
		}
		return this.x.equals(pp.x) && this.z.equals(pp.z);
	}

}
