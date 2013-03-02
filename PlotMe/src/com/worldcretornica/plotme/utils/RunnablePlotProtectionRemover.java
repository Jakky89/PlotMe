package com.worldcretornica.plotme.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.block.Block;

import com.griefcraft.model.Protection;


public class RunnablePlotProtectionRemover implements Runnable {

	private List<Block> blockQueue;
	
	public RunnablePlotProtectionRemover(List<Block> blockQueue)
	{
		blockQueue = new ArrayList<Block>(blockQueue);
	}
	
	@Override
	public void run() {
		Block block;
		Iterator<Block> blockQueueIterator = blockQueue.iterator();
		while (blockQueueIterator.hasNext())
		{
			block = blockQueueIterator.next();
			if (block != null)
			{
				Protection protection = com.griefcraft.lwc.LWC.getInstance().findProtection(block);
				if (protection != null)
				{
					protection.remove();
				}
			}
		}
	}

}
