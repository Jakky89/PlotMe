package com.worldcretornica.plotme.utils;

import com.worldcretornica.plotme.PlotWorld;

public class PlotPosition {

	public final PlotWorld w;
	public final Integer x;
	public final Integer z;

	public PlotPosition(Integer pX, Integer pZ)
	{
		this.w = null;
		this.x = pX;
		this.z = pZ;
	}
	
	public PlotPosition(PlotWorld pW, Integer pX, Integer pZ)
	{
		this.w = pW;
		this.x = pX;
		this.z = pZ;
	}

	@Override
	public final int hashCode()
	{
		if (this.w == null)
		{
			return x.hashCode() ^ z.hashCode();
		}
		return w.hashCode() ^ x.hashCode() ^ z.hashCode();
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
		if (this.w != null && pp.w != null)
		{
			if (this.w.MinecraftWorld.getName() != pp.w.MinecraftWorld.getName())
			{
				return false;
			}
		}
		return this.x.equals(pp.x) && this.z.equals(pp.z);
	}

}
