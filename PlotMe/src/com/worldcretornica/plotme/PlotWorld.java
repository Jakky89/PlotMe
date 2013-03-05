package com.worldcretornica.plotme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.worldcretornica.plotme.utils.Pair;



public class PlotWorld implements Comparable<PlotWorld>
{
	
	private int id;
	
	private World bukkitWorld;

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
	public boolean DisableNetherrackIgnition;
	public boolean DisableObsidianIgnition;
	
	private HashSet<Pair<Short, Byte>> ProtectedBlocks;
	private HashSet<Pair<Short, Byte>> PreventedItems;
	
	public Map<PlotPosition, Plot> plotPositions;
		
	
	public PlotWorld()
	{
		id = -1;
		bukkitWorld = null;
		plotPositions = new HashMap<PlotPosition, Plot>();
		ProtectedBlocks = null;
		PreventedItems = null;
	}
	
	public PlotWorld(World minecraftWorld)
	{
		id = -1;
		bukkitWorld = minecraftWorld;
		plotPositions = new HashMap<PlotPosition, Plot>();
		ProtectedBlocks = null;
		PreventedItems = null;
	}

	public PlotWorld(int worldId, World minecraftWorld)
	{
		id = worldId;
		bukkitWorld = minecraftWorld;
		plotPositions = new HashMap<PlotPosition, Plot>();
		ProtectedBlocks = null;
		PreventedItems = null;
	}

	public int getId()
	{
		return id;
	}
	
	public World getMinecraftWorld()
	{
		return bukkitWorld;
	}
	
	public String getWorldName()
	{
		if (bukkitWorld != null)
		{
			return bukkitWorld.getName();
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
	
	public boolean isPreventedItem(BlockState blockState)
	{
		if (blockState != null)
		{
			return isPreventedItem(blockState.getTypeId(), blockState.getRawData());
		}
		return true;
	}
	
	public boolean isPreventedItem(Block block)
	{
		if (block != null)
		{
			return isPreventedItem(block.getTypeId(), block.getData());
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
		
		plot.setNeighbourPlot((byte)0, getPlotAtPlotPosition( px    , pz - 1 ));
		plot.setNeighbourPlot((byte)1, getPlotAtPlotPosition( px + 1, pz - 1 ));
		plot.setNeighbourPlot((byte)2, getPlotAtPlotPosition( px + 1, pz     ));
		plot.setNeighbourPlot((byte)3, getPlotAtPlotPosition( px + 1, pz + 1 ));
		plot.setNeighbourPlot((byte)4, getPlotAtPlotPosition( px    , pz + 1 ));
		plot.setNeighbourPlot((byte)5, getPlotAtPlotPosition( px - 1, pz + 1 ));
		plot.setNeighbourPlot((byte)6, getPlotAtPlotPosition( px - 1, pz     ));
		plot.setNeighbourPlot((byte)7, getPlotAtPlotPosition( px - 1, pz - 1 ));
		
		plot.refreshNeighbourPlots();
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
	
	/**
	 * TODO: NEEDS TESTING !!!
	 */
	public Location getCenterBlockLocation(Plot plot)
	{
		if (plot != null && plot.getPlotWorld().equals(this))
		{
			Location baseLocation = getMinBlockLocation(plot);
			return new Location(bukkitWorld, ((double)baseLocation.getBlockX() + (double)(PlotSize / 2)), RoadHeight, ((double)baseLocation.getBlockZ() + (double)(PlotSize / 2)));
		}
		return null;
	}

	
	public int getPlotPositionVectorDirection(Plot plot, double posX, double posZ)
	{
		int dir = -1;
		if (plot.getPlotWorld().equals(this))
		{
			Location centerLocation = getCenterBlockLocation(plot);
			dir = PlotMe.getDirection(centerLocation.getX(), centerLocation.getZ(), posX, posZ);
		}
		return dir;
	}
	
	
	public boolean isOnRoad(double posX, double posZ)
	{
		double multi = getPlotBlockPositionMultiplier();
		
		double bsx = posX % multi;
		double bsz = posZ % multi;
		
		double pph = (double)(PathWidth / 2);
		double ppp = (double)(PlotSize + pph);
		
		if (bsx > pph && bsx < ppp && bsz > pph && bsz < ppp)
		{
			return false;
		}

		return true;
	}
	
	public boolean isOnRoad(Location loc)
	{
		if (loc != null && loc.getWorld().equals(bukkitWorld))
		{
			return isOnRoad(loc.getX(), loc.getZ());
		}

		return false;
	}
	
	public boolean isOnRoad(BlockState blockState)
	{
		if (blockState != null && blockState.getWorld().equals(bukkitWorld))
		{
			return isOnRoad(blockState.getX(), blockState.getZ());
		}

		return false;
	}
	
	public boolean isOnRoad(Block block)
	{
		if (block != null && block.getWorld().equals(bukkitWorld))
		{
			return isOnRoad(block.getX(), block.getZ());
		}

		return false;
	}
	
	/**
	 * TODO: NEEDS TESTING!!!
	 */
	public Plot getPlotAtBlockPosition(double posX, double posZ)
	{
		double divi = getPlotBlockPositionMultiplier();

		// Get nearest plot
		Plot plot = getPlotAtPlotPosition((int)Math.floor((double)(posX / divi)), (int)Math.floor((double)(posZ / divi)));
		if (plot != null)
		{
			if (isOnRoad(posX, posZ))
			{
				if (plot.hasNeighbourPlots())
				{
					int dir = getPlotPositionVectorDirection(plot, posX, posZ);
					if (dir >= 0)
					{
						if (plot.getNeighbourPlot(dir).getOwner() != null && plot.getNeighbourPlot(dir).getOwner().equals(plot.getOwner()))
						{
							return plot;
						}
					}
				}
			}
			else
			{
				return plot;
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
	
	public Plot getPlotAtBlockPosition(BlockState bs)
	{
		return getPlotAtBlockPosition(bs.getLocation());
	}
	
	public Plot getPlotAtBlockPosition(Block b)
	{
		return getPlotAtBlockPosition(b.getLocation());
	}
	
	public Location getMinBlockLocation(Plot plot)
	{
		double multi = getPlotBlockPositionMultiplier();
		
		double pph = (double)(PathWidth / 2);
		
		int locx = (int)Math.floor((double)(plot.getPlotX() * multi) + pph);
		int locz = (int)Math.floor((double)(plot.getPlotZ() * multi) + pph);
		
		return new Location(bukkitWorld, locx, 1, locz);
	}
	
	public Location getMaxBlockLocation(Plot plot)
	{
		Location baseLocation = getMinBlockLocation(plot);
		return new Location(bukkitWorld, baseLocation.getBlockX() + PlotSize, bukkitWorld.getMaxHeight(), baseLocation.getBlockZ() + PlotSize);
	}
	
	public Pair<Location, Location> getMinMaxBlockLocation(Plot plot)
	{
		Location baseLocation = getMinBlockLocation(plot);
		return new Pair<Location, Location>(new Location(bukkitWorld, baseLocation.getBlockX(), 1, baseLocation.getBlockZ()), new Location(bukkitWorld, baseLocation.getBlockX() + PlotSize, bukkitWorld.getMaxHeight(), baseLocation.getBlockZ() + PlotSize));
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
		
		return bukkitWorld.getBlockAt(blockx, RoadHeight, blockz);
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
	
	public List<Entity> getAreaEntities(double x1, double z1, double x2, double z2, boolean includePlayers)
	{
		List<Entity> tmpList = new ArrayList<Entity>();
		
		double minX = Math.min(x1, x2);
		double minZ = Math.min(z1, z2);
		double maxX = Math.max(x1, x2);
		double maxZ = Math.max(z1, z2);
		
		int minChunkX = (int)Math.floor((double)(minX / 16));
		int minChunkZ = (int)Math.floor((double)(minZ / 16));
		int maxChunkX = (int)Math.ceil((double)(maxX / 16));
		int maxChunkZ = (int)Math.ceil((double)(maxZ / 16));

		for (int cx = minChunkX; cx <= maxChunkX; cx++)
		{			
			for (int cz = minChunkZ; cz <= maxChunkZ; cz++)
			{
				Chunk chunk = bukkitWorld.getChunkAt(cx, cz);
				if (chunk != null)
				{
					Entity[] entities = chunk.getEntities();
					if (entities.length > 0)
					{
						for (Entity entity : entities)
						{
							Location entityloc = entity.getLocation();
							if (entityloc.getBlockX() > minX && entityloc.getBlockX() < maxX &&
								entityloc.getBlockZ() > minZ && entityloc.getBlockZ() < maxZ)
							{
								if (!(entity instanceof Player) || includePlayers)
								{
									tmpList.add(entity);
								}
							}
						}
					}
				}
			}
		}
		
		return tmpList;
	}
	
	public List<Entity> getSinglePlotEntities(Plot plot, boolean includePlayers)
	{
		if (bukkitWorld == null || plot == null || !plot.getPlotWorld().equals(this))
		{
			return null;
		}
		
		double multi = getPlotBlockPositionMultiplier();
		
		double x1 = (double)(plot.getPlotX() * multi);
		double z1 = (double)(plot.getPlotZ() * multi);
		double x2 = (double)(x1 + PlotSize);
		double z2 = (double)(z1 + PlotSize);
		
		return getAreaEntities(x1, z1, x2, z2, includePlayers);
	}
	
	public int getPlotSize()
	{
		return PlotSize;
	}
	
	public String getName()
	{
		if (bukkitWorld != null)
		{
			return bukkitWorld.getName();
		}
		return null;
	}
	
	@Override
	public int hashCode()
	{
		return id;
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
