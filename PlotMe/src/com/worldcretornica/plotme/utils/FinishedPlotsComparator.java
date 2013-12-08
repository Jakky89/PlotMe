package com.worldcretornica.plotme.utils;

import java.util.Comparator;

import com.worldcretornica.plotme.Plot;

public class FinishedPlotsComparator implements Comparator<Plot>
{
	public int compare(Plot plot1, Plot plot2)
	{
		return (int)(plot1.getFinish() - plot2.getFinish());
	}
}
