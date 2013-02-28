package com.worldcretornica.plotme.utils;

import java.util.Comparator;
import com.worldcretornica.plotme.Plot;

public class PlotExpiredComparator implements Comparator<Plot> {

	@Override
	public int compare(Plot plot1, Plot plot2) {
		if (plot1 == null || plot2 == null)
		{
			return 0;
		}
		return Long.valueOf(plot1.expireddate).compareTo(plot2.expireddate);
	}

}
