package com.worldcretornica.plotme.utils;

import java.util.Collections;
import java.util.Iterator;

import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotMe;

public class RunnableExpiredPlotsRemover implements Runnable {

	public void run()
	{
		Plot testplot;
		
		long currentTime = Math.round(System.currentTimeMillis()/1000);

		if (PlotManager.expiredPlots != null || PlotManager.expiredPlots.size() > 0)
		{
			if (PlotManager.expiredPlotDeletionsProcessed >= PlotMe.MAX_EXPIRED_PLOT_DELETIONS_PER_HOUR)
			{
				if (PlotManager.lastExpiredPlotDeletion > (currentTime - 3600))
				{
					return;
				}
				PlotManager.expiredPlotDeletionsProcessed = 0;
			}
			if (PlotManager.expiredPlotDeletionsProcessed < PlotMe.MAX_EXPIRED_PLOT_DELETIONS_PER_HOUR)
			{
				PlotManager.lastExpiredPlotDeletion = currentTime;
				Collections.sort(PlotManager.expiredPlots, new ExpiredPlotsComparator());
				Iterator<Plot> expiredPlotsIterator = PlotManager.expiredPlots.iterator();
				int expireDiff;
				while (expiredPlotsIterator.hasNext() && PlotManager.expiredPlotDeletionsProcessed < PlotMe.MAX_EXPIRED_PLOT_DELETIONS_PER_HOUR)
				{
					testplot = expiredPlotsIterator.next();
					if (testplot != null)
					{
						if (testplot.isProtected() || testplot.isFinished())
						{
							expiredPlotsIterator.remove();
						}
						else
						{
							PlotManager.expiredPlotDeletionsProcessed++;
							expireDiff = Math.round(testplot.getExpiration() - currentTime);
							if (expireDiff <= 0)
							{
								PlotManager.removePlot(testplot);
								expiredPlotsIterator.remove();
							}
							else if (testplot.getExpiration() > 0 && PlotManager.nextExpiredPlotsCheck > testplot.getExpiration())
							{
								PlotManager.nextExpiredPlotsCheck = testplot.getExpiration();
							}
						}
					}
				}
			}
		}
	}
}
