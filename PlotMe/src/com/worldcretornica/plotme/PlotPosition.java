package com.worldcretornica.plotme;


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

	@Override
	public final int hashCode()
	{
		if (this.w == null)
		{
			return (Integer.valueOf(x).hashCode() >> 7) ^ Integer.valueOf(z).hashCode();
		}
		return (w.hashCode() >> 13) ^ (Integer.valueOf(x).hashCode() >> 7) ^ Integer.valueOf(z).hashCode();
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
