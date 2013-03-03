package com.worldcretornica.plotme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.worldcretornica.plotme.utils.Pair;



public class PlotWorld implements Comparable<PlotWorld>
{
	
	private int id;
	
	private World MinecraftWorld;

	public int PlotSize;
	public int PlotAutoLimit;
	public int PathWidth;
	
	public short BottomBlockId;
	public byte BottomBlockValue;
	public short WallBlockId;
	public byte WallBlockValue;
	public short ForSaleWallBlockId;
	public byte ForSaleWallBlockValue;
	public short AuctionWallBlockId;
	public byte AuctionWallBlockValue;
	public short ProtectedWallBlockId;
	public byte ProtectedWallBlockValue;
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
	
	private HashSet<Pair<Short, Byte>> ProtectedBlocks;
	private HashSet<Pair<Short, Byte>> PreventedItems;
	
	public Map<PlotPosition, Plot> plotPositions;
	
	
	public PlotWorld(World minecraftWorld)
	{
		id = -1;
		MinecraftWorld = minecraftWorld;
		plotPositions = new HashMap<PlotPosition, Plot>();
		ProtectedBlocks = null;
		PreventedItems = null;
	}

	public PlotWorld(int worldId, World minecraftWorld)
	{
		id = worldId;
		MinecraftWorld = minecraftWorld;
		plotPositions = new HashMap<PlotPosition, Plot>();
		ProtectedBlocks = null;
		PreventedItems = null;
	}
	
	public void setId(int worldId)
	{
		id = worldId;
	}
	
	public int getId()
	{
		return id;
	}
	
	public World getMinecraftWorld()
	{
		return MinecraftWorld;
	}
	
	public String getWorldName()
	{
		if (MinecraftWorld != null)
		{
			return MinecraftWorld.getName();
		}
		return null;
	}
	
	public List<String> getProtectedBlocksAsStringList()
	{
		ArrayList<String> tmpList = new ArrayList<String>();
		Pair<Short, Byte> tmpEntry;
		Iterator<Pair<Short, Byte>> ppIterator = ProtectedBlocks.iterator();
		while (ppIterator.hasNext())
		{
			tmpEntry = ppIterator.next();
			tmpList.add(tmpEntry.getLeft().toString() + ":" + tmpEntry.getRight().toString());
		}
		return tmpList;
	}

	public boolean isProtectedBlock(int typeId, byte dataValue)
	{
		if (typeId >= 0)
		{
			if (ProtectedBlocks == null || ProtectedBlocks.isEmpty())
			{
				return false;
			}
			if (!ProtectedBlocks.contains(new Pair<Integer, Byte>(typeId, dataValue)) && !ProtectedBlocks.contains(new Pair<Integer, Byte>(typeId, null)))
			{
				return false;
			}
		}
		return true;
	}
	
	public boolean isProtectedBlock(Block block)
	{
		if (block != null)
		{
			return isProtectedBlock(block.getTypeId(), block.getData());
		}
		return true;
	}
	
	public void setProtectedBlocks(Set<Pair<Short, Byte>> blockSet)
	{
		if (blockSet != null)
		{
			ProtectedBlocks = new HashSet<Pair<Short, Byte>>(blockSet);
		}
	}
	
	public void addToProtectedBlocks(List<String> typeIdValues)
	{
		if (typeIdValues != null && typeIdValues.size() > 0)
		{
			for (String s : typeIdValues)
			{
				ProtectedBlocks.add(PlotMe.getItemIdValue(s));
			}
		}
	}
	
	public void addToProtectedBlocks(Pair<Short, Byte> itemTypeValue)
	{
		if (itemTypeValue != null)
		{
			if (ProtectedBlocks == null)
			{
				ProtectedBlocks = new HashSet<Pair<Short, Byte>>();
			}
			ProtectedBlocks.add(itemTypeValue);
		}
	}
	
	public void addToProtectedBlocks(int typeId)
	{
		if (typeId >= 0)
		{
			addToProtectedBlocks(new Pair<Short, Byte>((short)typeId, null));
		}
	}
	
	public void addToProtectedBlocks(int typeId, byte dataValue)
	{
		if (typeId >= 0)
		{
			addToProtectedBlocks(new Pair<Short, Byte>((short)typeId, dataValue));
		}
	}
	
	public void addToProtectedBlocks(Block block)
	{
		if (block != null)
		{
			addToProtectedBlocks((short)block.getTypeId(), block.getData());
		}
	}
	
	public void addToProtectedBlocks(Material material)
	{
		if (material != null)
		{
			addToProtectedBlocks(material.getId(), (byte)0);
		}
	}
	
	public void removeFromProtectedBlocks(int typeId, byte dataValue)
	{
		if (typeId < 0 || ProtectedBlocks == null || ProtectedBlocks.isEmpty())
		{
			return;
		}
		ProtectedBlocks.remove(new Pair<Short, Byte>((short)typeId, dataValue));
	}
	
	public List<String> getPreventedItemsAsStringList()
	{
		ArrayList<String> tmpList = new ArrayList<String>();
		Pair<Short, Byte> tmpEntry;
		Iterator<Pair<Short, Byte>> piIterator = PreventedItems.iterator();
		while (piIterator.hasNext())
		{
			tmpEntry = piIterator.next();
			tmpList.add(tmpEntry.getLeft().toString() + ":" + tmpEntry.getRight().toString());
		}
		return tmpList;
	}
	
	public boolean isPreventedItem(int typeId, byte dataValue)
	{
		if (typeId >= 0)
		{
			if (PreventedItems == null || PreventedItems.isEmpty())
			{
				return false;
			}
			if (!PreventedItems.contains(new Pair<Short, Byte>((short)typeId, dataValue)) && !PreventedItems.contains(new Pair<Short, Byte>((short)typeId, null)))
			{
				return false;
			}
		}
		return true;
	}
	
	public boolean isPreventedItem(Material material)
	{
		if (material != null)
		{
			return isPreventedItem(material.getId(), (byte)0);
		}
		return true;
	}
	
	public void setPreventedItems(Set<Pair<Short, Byte>> itemSet)
	{
		if (itemSet != null)
		{
			PreventedItems = new HashSet<Pair<Short, Byte>>(itemSet);
		}
	}
	
	public void addToPreventedItems(List<String> typeIdValues)
	{
		if (typeIdValues != null && typeIdValues.size() > 0)
		{
			for (String s : typeIdValues)
			{
				PreventedItems.add(PlotMe.getItemIdValue(s));
			}
		}
	}
	
	public void addToPreventedItems(int typeId)
	{
		if (typeId >= 0)
		{
			PreventedItems.add(new Pair<Short, Byte>((short)typeId, null));
		}
	}
	
	public void addToPreventedItems(int typeId, byte dataValue)
	{
		if (typeId >= 0)
		{
			if (PreventedItems == null)
			{
				PreventedItems = new HashSet<Pair<Short, Byte>>();
			}
			PreventedItems.add(new Pair<Short, Byte>((short)typeId, dataValue));
		}
	}
	
	public void addToPreventedItems(Material material)
	{
		if (material != null)
		{
			addToPreventedItems(material.getId());
		}
	}
	
	public void addToPreventedItems(Material material, byte dataValue)
	{
		if (material != null && dataValue >= 0)
		{
			if (PreventedItems == null)
			{
				PreventedItems = new HashSet<Pair<Short, Byte>>();
			}
			PreventedItems.add(new Pair<Short, Byte>((short)material.getId(), dataValue));
		}
	}
	
	public void removeFromPreventedItems(int typeId, byte dataValue)
	{
		if (typeId < 0 || dataValue <= 0 || PreventedItems == null || PreventedItems.isEmpty())
		{
			return;
		}
		ProtectedBlocks.remove(new Pair<Integer, Byte>(typeId, dataValue));
	}
	
	public void refreshNeighbours(Plot plot)
	{
		if (plot == null || plot.getPlotWorld() == null || !plot.getPlotWorld().equals(this))
		{
			return;
		}
		
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
		if (plot == null || plot.getPlotWorld() == null || !plot.getPlotWorld().equals(this) || plot.getPlotPosition() == null)
		{
			return false;
		}
		Plot oldPlot = plotPositions.put(plot.getPlotPosition(), plot);
		if (oldPlot == null || !oldPlot.equals(plot))
		{
			refreshNeighbours(plot);
			return true;
		}
		return false;
	}

	public boolean unregisterPlot(Plot plot)
	{
		if (plot == null || plot.getPlotWorld() == null || !plot.getPlotWorld().equals(this) || plot.getPlotPosition() == null || plotPositions == null || plotPositions.isEmpty())
		{
			return false;
		}
		Plot removedPlot = plotPositions.remove(plot.getPlotPosition());
		if (removedPlot != null && removedPlot.equals(plot))
		{
			plot.resetNeighbourPlots();
			return true;
		}
		return false;
	}
	
	public Plot getPlotAtPlotPosition(PlotPosition plotPosition)
	{
		if (plotPosition != null && plotPosition.getPlotWorld() != null && plotPosition.getPlotWorld().equals(this) && plotPositions != null && !plotPositions.isEmpty())
		{
			return plotPositions.get(plotPosition);
		}
		return null;		
	}

	public Plot getPlotAtPlotPosition(int plotX, int plotZ)
	{
		return getPlotAtPlotPosition(new PlotPosition(this, Integer.valueOf(plotX), Integer.valueOf(plotZ)));
	}
	
	public double getPlotBlockPositionMultiplier()
	{
		return (double)(PlotSize + (PathWidth / 2));
	}
	
	public PlotPosition blockToPlotPosition(double blockX, double blockZ)
	{
		double divi = getPlotBlockPositionMultiplier();
		return new PlotPosition(this, (int)Math.floor((double)(blockX / divi)), (int)Math.floor((double)(blockZ / divi)));
	}
	
	public int getPlotPositionVectorDirection(PlotPosition plotpos, double posX, double posZ)
	{
		int dir = -1;
		
		if (plotpos.getPlotWorld().equals(this))
		{
			double divi = getPlotBlockPositionMultiplier();
			
			int centerx = (int)Math.round((double)(plotpos.getPlotX() * divi) + (double)(PlotSize / 2));
			int centerz = (int)Math.round((double)(plotpos.getPlotZ() * divi) + (double)(PlotSize / 2));

			dir = PlotMe.getDirection(centerx, centerz, posX, posZ);
		}
		return dir;
	}
	
	public boolean isOnRoad(double posX, double posZ)
	{
		double divi = getPlotBlockPositionMultiplier();
		
		double bsx = posX % divi;
		double bsz = posZ % divi;
		
		double pph = (double)(PathWidth / 2);
		
		if (bsx <= pph || bsx >= PlotSize || bsz <= pph || bsz >= PlotSize)
		{
			return true;
		}

		return false;
	}
	
	/**
	 * TODO: NEEDS TESTING!!!
	 */
	public Plot getPlotAtBlockPosition(double posX, double posZ)
	{
		double divi = getPlotBlockPositionMultiplier();
		
		int roundplotx = (int)Math.floor((double)(posX / divi));
		int roundplotz = (int)Math.floor((double)(posZ / divi));
		
		double psh = (double)(PlotSize / 2);
		
		double centerx = Math.round((double)(roundplotx * divi) + psh);
		double centerz = Math.round((double)(roundplotz * divi) + psh);
		
		byte dir = PlotMe.getDirection(centerx, centerz, posX, posZ);

		// Get nearest plot
		Plot plot = getPlotAtPlotPosition(roundplotx, roundplotz);
		
		if (plot != null && AutoLinkPlots)
		{
			double bsx = posX % divi;
			double bsz = posZ % divi;
			
			double pph = (double)(PathWidth / 2);
			
			if (bsx <= pph || bsx >= PlotSize || bsz <= pph || bsz >= PlotSize)
			{
				if (plot.neighbourplots != null && plot.neighbourplots.length > 0)
				{
					if (plot.neighbourplots[dir].owner != null && plot.neighbourplots[dir].owner == plot.owner)
					{
						return plot;
					}
				}
			}
		}
		// FALLBACK
		return null;
	}
	
	public Plot getPlotAtBlockPosition(Location loc)
	{
		if (loc.getWorld().equals(this.getMinecraftWorld()))
		{
			return getPlotAtBlockPosition(loc.getBlockX(), loc.getBlockZ());
		}
		return null;
	}
	
	
	public Plot getPlotAtBlockPosition(Block b)
	{
		return getPlotAtBlockPosition(b.getLocation());
	}
	
	public Location getMinBlockLocation(Plot plot)
	{
		double multi = getPlotBlockPositionMultiplier();
		
		int locx = (int)Math.floor((double)(plot.getPlotX() * multi));
		int locz = (int)Math.floor((double)(plot.getPlotZ() * multi));
		
		return new Location(MinecraftWorld, locx, 1, locz);
	}
	
	public Location getMaxBlockLocation(Plot plot)
	{
		double multi = getPlotBlockPositionMultiplier();
		
		int locx = (int)Math.ceil((double)((plot.getPlotX() + 1) * multi));
		int locz = (int)Math.ceil((double)((plot.getPlotZ() + 1) * multi));
		
		return new Location(MinecraftWorld, locx, MinecraftWorld.getMaxHeight(), locz);
	}
	
	public Pair<Location, Location> getMinMaxBlockLocation(Plot plot)
	{
		double multi = getPlotBlockPositionMultiplier();
		
		int locminx = (int)Math.floor((double)(plot.getPlotX() * multi));
		int locminz = (int)Math.floor((double)(plot.getPlotZ() * multi));
		
		int locmaxx = (int)Math.ceil((double)((plot.getPlotX() + 1) * multi));
		int locmaxz = (int)Math.ceil((double)((plot.getPlotZ() + 1) * multi));
		
		return new Pair<Location, Location>(new Location(MinecraftWorld, locminx, 1, locminz), new Location(MinecraftWorld, locmaxx, MinecraftWorld.getMaxHeight(), locmaxz));
	}
	
	/**
	 * TODO: NEEDS TESTING !!!
	 */
	public Location getCenterLocation(int plotX, int plotZ)
	{
		double multi = getPlotBlockPositionMultiplier();
		
		int locx = (int)Math.round((double)(plotX * multi) + (double)(PlotSize / 2));
		int locz = (int)Math.round((double)(plotZ * multi) + (double)(PlotSize / 2));
		
		return new Location(MinecraftWorld, locx, RoadHeight, locz);
	}
	
	public Location getCenterLocation(Plot plot)
	{
		if (plot != null)
		{
			return getCenterLocation(plot.getPlotX(), plot.getPlotZ());
		}
		return null;
	}
	
	/**
	 * TODO: NEEDS TESTING !!!
	 */
	public Block getCenterBlock(int plotX, int plotZ)
	{
		double multi = getPlotBlockPositionMultiplier();
		
		int blockx = (int)Math.round((double)(plotX * multi) + (double)(PlotSize / 2));
		int blockz = (int)Math.round((double)(plotZ * multi) + (double)(PlotSize / 2));
		
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
	
	public List<Plot> getPlotsBetween(Location loc1, Location loc2)
	{
		if (!loc1.getWorld().equals(loc2.getWorld()))
		{
			return null;
		}
		
		double divi = getPlotBlockPositionMultiplier();
		
		int minX = (int)Math.floor(Math.min(loc1.getBlockX(), loc2.getBlockX()) / divi);
		int minZ = (int)Math.floor(Math.min(loc1.getBlockZ(), loc2.getBlockZ()) / divi);
		int maxX = (int)Math.ceil(Math.max(loc1.getBlockX(), loc2.getBlockX()) / divi);
		int maxZ = (int)Math.ceil(Math.max(loc1.getBlockZ(), loc2.getBlockZ()) / divi);
		
		List<Plot> tmpList = new ArrayList<Plot>();

		Plot tmpPlot;
		for (int x = minX; x <= maxX; x++)
		{
			for (int z = minZ; z <= maxZ; z++)
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
