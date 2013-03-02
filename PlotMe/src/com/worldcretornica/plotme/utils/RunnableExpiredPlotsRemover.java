package com.worldcretornica.plotme.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.World;

import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotMe;
import com.worldcretornica.plotme.PlotMeSqlManager;

public class RunnableExpiredPlotsRemover implements Runnable {

	public void run()
	{
		Plot testplot;
		Iterator<Plot> expireIterator = PlotManager.allPlots.iterator();
		while (expireIterator.hasNext())
		{
			testplot = expireIterator.next();
			if (testplot.isprotected || testplot.finisheddate > 0 || testplot.expireddate > Math.round(System.currentTimeMillis()/1000))
			{
				continue;
			}
			else if (PlotManager.expiredPlots.size() < PlotMe.nbperdeletionprocessingexpired)
			{
				PlotManager.expiredPlots.add(testplot);
			}
			else
			{
				break;
			}
		}
		if (PlotManager.expiredPlots.size() == 0)
		{
			PlotMe.counterexpired = 0;
		}
		else
		{
			Collections.sort(PlotManager.expiredPlots, new PlotExpiredComparator());
			Plot expiredplot;
			String ids = "";
			while (PlotManager.expiredPlots.size() > 0)
			{
				expiredplot = PlotManager.expiredPlots.get(0);
				PlotManager.clear(expiredplot);
				PlotManager.expiredPlots.remove(0);
				PlotMeSqlManager.removePlot(expiredplot);
				ids += ChatColor.RED + String.valueOf(expiredplot.getId()) + ChatColor.RESET + ", ";
			}
			if (PlotMe.counterexpired == 0)
			{
				PlotMe.cscurrentlyprocessingexpired.sendMessage(PlotMe.PREFIX + PlotMe.caption("MsgDeleteSessionFinished"));
				PlotMe.worldcurrentlyprocessingexpired = null;
				PlotMe.cscurrentlyprocessingexpired = null;
			}
		}
	}
}
