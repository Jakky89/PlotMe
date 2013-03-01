package com.worldcretornica.plotme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.World;

public class PlotRunnableDeleteExpire implements Runnable {

	public void run()
	{
		if (PlotMe.worldcurrentlyprocessingexpired != null)
		{
			World w = PlotMe.worldcurrentlyprocessingexpired;
			List<Plot> expiredplots = new LinkedList<Plot>();
			Plot expiredplot;
			
			Iterator<Entry<Integer, Plot>> expireIterator = PlotManager.allPlots.entrySet().iterator();
			while (expireIterator.hasNext())
			{
				Plot plot = plots.get(id);
				
				if (!plot.isprotected && !plot.isfinished && plot.expireddate != null && plot.expireddate<System.currentTimeMillis())
				{
					expiredplots.add(plot);
				}
				
				if(expiredplots.size() == PlotMe.nbperdeletionprocessingexpired)
				{
					break;
				}
			}
			
			if(expiredplots.size() == 0)
			{
				PlotMe.counterexpired = 0;
			}
			else
			{
				plots = null;
				
				Collections.sort(expiredplots);
				
				String ids = "";
				
				for(int ictr = 0; ictr < PlotMe.nbperdeletionprocessingexpired && expiredplots.size() > 0; ictr++)
				{
					expiredplot = expiredplots.get(0);
					
					expiredplots.remove(0);
					
					PlotManager.clear(w, expiredplot);
					
					String id = expiredplot.id;
					ids += ChatColor.RED + id + ChatColor.RESET + ", ";
					
					PlotManager.getPlots(w).remove(id);
						
					PlotManager.removeOwnerSign(w, id);
					PlotManager.removeSellSign(w, id);
										
					SqlManager.deletePlot(PlotManager.getIdX(id), PlotManager.getIdZ(id), w.getName().toLowerCase());
					
					PlotMe.counterexpired--;
				}
				
				if(ids.substring(ids.length() - 2).equals(", "))
				{
					ids = ids.substring(0, ids.length() - 2);
				}
				
				PlotMe.cscurrentlyprocessingexpired.sendMessage("" + PlotMe.PREFIX + PlotMe.caption("MsgDeletedExpiredPlots") + " " + ids);
			}
			
			if(PlotMe.counterexpired == 0)
			{
				PlotMe.cscurrentlyprocessingexpired.sendMessage("" + PlotMe.PREFIX + PlotMe.caption("MsgDeleteSessionFinished"));
				PlotMe.worldcurrentlyprocessingexpired = null;
				PlotMe.cscurrentlyprocessingexpired = null;
			}
		}
	}
}
