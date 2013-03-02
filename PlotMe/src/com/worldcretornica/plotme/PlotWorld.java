package com.worldcretornica.plotme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;



public class PlotWorld implements Comparable<PlotWorld>
{
	
	public int id;
	
	public World MinecraftWorld;

	public int PlotSize;
	public int PlotAutoLimit;
	public int PathWidth;
	
	public short BottomBlockId;
	public byte BottomBlockValue;
	public short WallBlockId;
	public byte WallBlockValue;
	public short PlotFloorBlockId;
	public byte PlotFloorBlockValue;
	public short PlotFillingBlockId;
	public byte PlotFillingBlockValue;
	
	public short RoadMainBlockId;
	public byte RoadMainBlockValue;
	public short RoadStripeBlockId;
	public byte RoadStripeBlockValue;

	public int RoadHeight;
	public int DaysToExpiration;
	
	public String ProtectedWallBlockId;
	public String ForSaleWallBlockId;
	public String AuctionWallBlockId;

	public boolean UseEconomy;
	public boolean CanPutOnSale;
	public boolean CanSellToBank;
	public boolean RefundClaimPriceOnReset;
	public boolean RefundClaimPriceOnSetOwner;
	public double ClaimPrice;
	public double ClearPrice;
	public double AddPlayerPrice;
	public double DenyPlayerPrice;
	public double RemovePlayerPrice;
	public double UndenyPlayerPrice;
	public double PlotHomePrice;
	public boolean CanCustomizeSellPrice;
	public double SellToPlayerPrice;
	public double SellToBankPrice;
	public double BuyFromBankPrice;
	public double AddCommentPrice;
	public double BiomeChangePrice;
	public double ProtectPrice;
	public double DisposePrice;
	
	public boolean AutoLinkPlots;
	public boolean DisableExplosion;
	public boolean DisableIgnition;
	
	public HashSet<Integer> ProtectedBlocks;
	public HashSet<String> PreventedItems;
	
	public Map<PlotPosition, Plot> plotPositions;
	

	public PlotWorld(int id, World world)
	{
		this.MinecraftWorld = world;
		this.plotPositions = new HashMap<PlotPosition, Plot>();
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public void refreshNeighbours(Plot plot)
	{
		/**
		 * +++++++++++++++++++++++++
		 * +  #7   +  #0   +  #1   +
		 * +(-1,-1)+( 0,-1)+( 1,-1)+
		 * +++++++++++++++++++++++++
		 * +  #6   +       +  #2   +
		 * +(-1,0 )+       +( 1,0 )+
		 * +++++++++++++++++++++++++
		 * +  #5   +  #4   +  #3   +
		 * +(-1,1 )+( 0,1 )+( 1,1 )+
		 * +++++++++++++++++++++++++
		 */
		
		Integer px = plot.getPlotX();
		Integer pz = plot.getPlotZ();
		
		plot.neighbourplots[0] = getPlotAtPlotPosition( px    , pz - 1 );
		plot.neighbourplots[1] = getPlotAtPlotPosition( px + 1, pz - 1 );
		plot.neighbourplots[2] = getPlotAtPlotPosition( px + 1, pz     );
		plot.neighbourplots[3] = getPlotAtPlotPosition( px + 1, pz + 1 );
		plot.neighbourplots[4] = getPlotAtPlotPosition( px    , pz + 1 );
		plot.neighbourplots[5] = getPlotAtPlotPosition( px - 1, pz + 1 );
		plot.neighbourplots[6] = getPlotAtPlotPosition( px - 1, pz     );
		plot.neighbourplots[7] = getPlotAtPlotPosition( px - 1, pz - 1 );
		
		plot.notifyNeighbourPlots();
	}
	
	public boolean registerPlot(Plot plot) {
		if (plot == null || plot.plotpos == null || plot.plotpos.w == null || !plot.plotpos.w.equals(this))
		{
			return false;
		}
		Plot oldPlot = plotPositions.put(plot.plotpos, plot);
		if (oldPlot == null || !oldPlot.equals(plot))
		{
			refreshNeighbours(plot);
			return true;
		}
		return false;
	}

	public void unregisterPlot(Plot plot)
	{
		if (plot == null)
		{
			return;
		}
		plot.resetNeighbourPlots();
		if (plot.plotpos == null)
		{
			return;
		}
		plotPositions.remove(plot.plotpos);
	}
	
	public Plot getPlotAtPlotPosition(PlotPosition ppos)
	{
		if (ppos != null)
		{
			return plotPositions.get(ppos);
		}
		return null;		
	}

	public Plot getPlotAtPlotPosition(int plotX, int plotZ)
	{
		return plotPositions.get(new PlotPosition(this, Integer.valueOf(plotX), Integer.valueOf(plotZ)));
	}
	
	public double getPlotBlockPositionMultiplier()
	{
		return (double)(PlotSize + (PathWidth/2));
	}
	
	public PlotPosition blockToPlotPosition(double blockX, double blockZ)
	{
		double multi = getPlotBlockPositionMultiplier();
		
		int roundplotx = (int)Math.round((double)(blockX / multi));
		int roundplotz = (int)Math.round((double)(blockZ / multi));
			
		return new PlotPosition(this, roundplotx, roundplotz);
	}
	
	public Plot getPlotAtBlockPosition(Location loc)
	{
		if (loc.getWorld().getName() == this.MinecraftWorld.getName())
		{
			return getPlotAtBlockPosition(loc.getBlockX(), loc.getBlockZ());
		}
		
		return null;
	}
	
	/**
	 * TODO: NEEDS TESTING !!!
	 */
	public Location getCenterLocation(Plot plot)
	{
		double multi = getPlotBlockPositionMultiplier();
		
		int locx = (int)Math.round((double)(plot.getPlotX() * multi) + (double)(PlotSize / 2));
		int locz = (int)Math.round((double)(plot.getPlotZ() * multi) + (double)(PlotSize / 2));

		return new Location(MinecraftWorld, locx, RoadHeight, locz);
	}
	
	/**
	 * TODO: NEEDS TESTING !!!
	 */
	public Block getCenterBlock(int plotX, int plotZ)
	{
		double multi = getPlotBlockPositionMultiplier();
		
		int blockx = (int)Math.round((double)(plotX * multi) - (double)(PlotSize / 2));
		int blockz = (int)Math.round((double)(plotZ * multi) - (double)(PlotSize / 2));
		
		PlotMe.logger.info(PlotMe.PREFIX + "DEBUG: centerBlockX " + String.valueOf(blockx) + "  centerBlockZ " + String.valueOf(blockz));
		
		return MinecraftWorld.getBlockAt(blockx, RoadHeight, blockz);
	}
	
	public Block getCenterBlock(Plot plot)
	{
		if (plot != null)
		{
			return getCenterBlock(plot.getPlotX(), plot.getPlotZ());
		}
		return null;
	}
	
	/**
	 * TODO: NEEDS TESTING!!!
	 */
	public Plot getPlotAtBlockPosition(double blockX, double blockZ)
	{
		boolean road = false;

		double divi = getPlotBlockPositionMultiplier();
		
		double realpsh = (double)((PlotSize  / 2) / divi);
		
		double realplotx = (double)(blockX / divi);
		double realplotz = (double)(blockZ / divi);
		
		int roundplotx = (int)Math.ceil(realplotx);
		int roundplotz = (int)Math.ceil(realplotz);
		
		PlotMe.logger.info(PlotMe.PREFIX + "DEBUG: roundPlotX " + String.valueOf(roundplotx) + "  roundPlotZ " + String.valueOf(roundplotz));
		
		int dir = -1;
		
		if (realplotx < (roundplotx - realpsh))
		{
			road = true;
			if (realplotz < (roundplotz - realpsh))
			{
				dir = 7;
			}
			else if (realplotz > (roundplotz + realpsh))
			{
				dir = 5;
			}
			else
			{
				dir = 6;
			}
		}
		else if (realplotx > (roundplotx + realpsh))
		{
			road = true;
			if (realplotz < (roundplotz - realpsh))
			{
				dir = 1;
			}
			else if (realplotz > (roundplotz + realpsh))
			{
				dir = 3;
			}
			else
			{
				dir = 2;
			}
		}
		

		
		// Get nearest plot
		Plot plot = getPlotAtPlotPosition(roundplotx, roundplotz);
		if (plot == null)
		{
			return null;
		}

		if (!road)
		{
			return plot;
		}

		if (AutoLinkPlots)
		{
			if (plot.neighbourplots == null)
			{
				return null;
			}
			if (dir >= 0)
			{
				if (plot.neighbourplots[dir].owner != null && plot.neighbourplots[dir].owner == plot.owner)
				{
					return plot;
				}
			}
		}
		// FALLBACK
		return null;
	}
	
	public Plot getPlotAtBlockPosition(Block b)
	{
		return getPlotAtBlockPosition(b.getLocation());
	}
	
	public List<Plot> getPlotsBetween(Location l1, Location l2)
	{
		if (!l1.getWorld().equals(l2.getWorld()))
		{
			return null;
		}
		
		double divi = getPlotBlockPositionMultiplier();
		
		int minX = (int)Math.floor(Math.min(l1.getBlockX(), l2.getBlockX()) / divi);
		int minZ = (int)Math.floor(Math.min(l1.getBlockZ(), l2.getBlockZ()) / divi);
		int maxX = (int)Math.ceil(Math.max(l1.getBlockX(), l2.getBlockX()) / divi);
		int maxZ = (int)Math.ceil(Math.max(l1.getBlockZ(), l2.getBlockZ()) / divi);
		
		List<Plot> tmpList = new ArrayList<Plot>();
		Plot tmpPlot;
		for (int x=minX; x<maxX; x++)
		{
			for (int z=minZ; z<maxZ; z++)
			{
				tmpPlot = getPlotAtPlotPosition(x, z);
				if (tmpPlot != null)
				{
					tmpList.add(tmpPlot);
				}
			}
		}
		return tmpList;
	}
	
	@Override
	public int hashCode()
	{
		return MinecraftWorld.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
	    if (o == null) {
	    	return false;
	    }
	    
	    if (!(o instanceof PlotWorld)) {
	    	return false;
	    }
	    
	    if (this.id == ((PlotWorld)o).id)
	    {
	    	return true;
	    }
	    
		return false;
	}

	@Override
	public int compareTo(PlotWorld pw) {
		return this.id - pw.id;
	}
	
}
