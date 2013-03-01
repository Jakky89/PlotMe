package com.worldcretornica.plotme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import com.worldcretornica.plotme.utils.RunnablePlotProtectionRemover;


public class PlotProtectionRemover {

	public static HashSet<Block> blockQueue;
	//public static HashSet<Plot> plotQueue;
	private static List<Integer> removerTaskIds;
	private static BukkitScheduler buksched;
	
	
	public PlotProtectionRemover()
	{
		buksched = Bukkit.getScheduler();
		removerTaskIds = new LinkedList<Integer>();
		blockQueue = (HashSet<Block>)Collections.synchronizedSet(new HashSet<Block>());
		//plotQueue  =  (HashSet<Plot>)Collections.synchronizedSet(new HashSet<Plot>());
	}
	
	public static void checkRunning()
	{
		Iterator<Integer> removerTaskIdsIterator = removerTaskIds.iterator();
		while (removerTaskIdsIterator.hasNext())
		{
			int taskId = removerTaskIdsIterator.next();
			if (!buksched.isQueued(taskId) && !buksched.isCurrentlyRunning(taskId))
			{
				removerTaskIdsIterator.remove();
			}
		}
	}
	
	public static void addBlockToQueue(Block block)
	{
		blockQueue.add(block);
		if (blockQueue.size() < 100)
		{
			if (removerTaskIds.size() > 0)
			{
				checkRunning();
				return;
			}
		}
		BukkitTask removerTask = buksched.runTaskLaterAsynchronously(PlotMe.self, new RunnablePlotProtectionRemover(new ArrayList<Block>(blockQueue)), 20);
		removerTaskIds.add(removerTask.getTaskId());
		blockQueue.clear();
	}
	
	public static void addPlotToQueue(Plot plot)
	{
		if (plot == null || plot.plotpos == null || plot.plotpos.w == null || plot.plotpos.w.MinecraftWorld == null)
		{
			return;
		}
		
		checkRunning();

		//plotQueue.add(plot);

		double ptbbmulti = plot.plotpos.w.getPlotBlockPositionMultiplier();
		
		final int bottomX	= (int)Math.ceil(plot.plotpos.x        * ptbbmulti);
		final int bottomZ	= (int)Math.ceil(plot.plotpos.z        * ptbbmulti);
		final int topX		= (int)Math.floor((plot.plotpos.x - 1) * ptbbmulti);
		final int topZ		= (int)Math.floor((plot.plotpos.z - 1) * ptbbmulti);
		
		for (int lx = bottomX; lx > topX; lx--)
		{
			for (int lz = bottomZ; lz > topZ; lz--)
			{
				Block highestblock = plot.plotpos.w.MinecraftWorld.getHighestBlockAt(lx, lz);
				final int maxH = highestblock.getLocation().getBlockY();
				for (int ly = 1; ly <= maxH; ly++)
				{
					Block block = plot.plotpos.w.MinecraftWorld.getBlockAt(lx, ly, lz);
					blockQueue.add(block);
				}
		    }
		}
	}
	
	public static void forceRun()
	{
		Bukkit.getScheduler().runTask(PlotMe.self, new RunnablePlotProtectionRemover(new ArrayList<Block>(blockQueue)));
		blockQueue.clear();
	}
	
}
