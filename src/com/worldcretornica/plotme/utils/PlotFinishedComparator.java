package com.worldcretornica.plotme.utils;

import java.util.Comparator;
import com.worldcretornica.plotme.Plot;

public class PlotFinishedComparator implements Comparator<Plot>
{
	
	@Override
	public int compare(Plot plot1, Plot plot2)
	{
		return Long.valueOf(plot1.finisheddate).compareTo(plot2.finisheddate);
	}
	
}
