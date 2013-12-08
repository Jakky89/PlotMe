package com.worldcretornica.plotme;

import java.util.Comparator;

import com.worldcretornica.plotme.Plot;

public class PlotFinishedComparator implements Comparator<Plot>
{
	public int compare(Plot plot1, Plot plot2)
	{
		return (int)(plot1.getFinishDate() - plot2.getFinishDate());
	}
}
