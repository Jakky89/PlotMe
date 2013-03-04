package com.worldcretornica.plotme.utils;

import java.util.Comparator;
import com.worldcretornica.plotme.Plot;

public class ExpiredPlotsComparator implements Comparator<Plot> {

	@Override
	public int compare(Plot plot1, Plot plot2) {
		if (plot1 == null || plot2 == null)
		{
			return 0;
		}
		return (int)(plot1.getExpiration() - plot2.getExpiration());
	}

}
