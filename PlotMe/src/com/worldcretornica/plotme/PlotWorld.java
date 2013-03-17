package com.worldcretornica.plotme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.worldcretornica.plotme.utils.Jakky89ItemUtils;
import com.worldcretornica.plotme.utils.Jakky89ItemIdData;
import com.worldcretornica.plotme.utils.Pair;



public class PlotWorld implements Comparable<PlotWorld>
{
	
	private int id;
	
	private World bukkitWorld;

	public boolean PlotsEnabled;
	public int PlotSize;
	public int PlotAutoLimit;
	public boolean AutoClaimOnChestPlace;
	public int DefaultPlayerPlotLimit;
	public int DefaultFreePlotsPerPlayer;
	public int PathWidth;
	
	public Jakky89ItemIdData BottomBlock;
	public Jakky89ItemIdData WallBlock;
	public Jakky89ItemIdData ForSaleWallBlock;
	public Jakky89ItemIdData AuctionWallBlock;
	public Jakky89ItemIdData ProtectedWallBlock;
	public Jakky89ItemIdData PlotFloorBlock;
	public Jakky89ItemIdData PlotFillingBlock;
	
	public Jakky89ItemIdData RoadMainBlock;
	public Jakky89ItemIdData RoadStripeBlock;

	public int RoadHeight;
	public int DaysToExpiration;

	public boolean UseEconomy;
	public boolean CanPutOnSale;
	public boolean CanSellToBank;
	public boolean RefundClaimPriceOnReset;
	public boolean RefundClaimPriceOnSetOwner;
	public float ClaimPrice;
	public float ClearPrice;
	public float AddPlayerPrice;
	public float DenyPlayerPrice;
	public float RemovePlayerPrice;
	public float UndenyPlayerPrice;
	public float PlotHomePrice;
	public boolean CanCustomizeSellPrice;
	public float SellToPlayerPrice;
	public float SellToBankPrice;
	public float BuyFromBankPrice;
	public float AddCommentPrice;
	public float BiomeChangePrice;
	public float ProtectPrice;
	public float DisposePrice;
	
	public boolean AutoLinkPlots;
	public boolean DisableExplosion;
	public boolean DisableIgnition;
	public boolean DisableNetherrackIgnition;
	public boolean DisableObsidianIgnition;
	
	private HashSet<Jakky89ItemIdData> ProtectedBlocks;
	private HashSet<Jakky89ItemIdData> PreventedItems;
	
	public Map<PlotPosition, Plot> plotPositions;
	
	private int minPlotX;
	private int minPlotZ;
	private int maxPlotX;
	private int maxPlotZ;
		
	
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
	
	public void setPlotRect(int x1, int z1, int x2, int z2) {
		minPlotX = Math.min(x1, x2);
		maxPlotX = Math.max(x1, x2);
		minPlotZ = Math.min(z1, z2);
		maxPlotZ = Math.max(z1, z2);
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
		return Jakky89ItemUtils.itemIdValuesToStringList(ProtectedBlocks);
	}

	public boolean isProtectedBlock(short typeId, short dataValue)
	{
		if (ProtectedBlocks == null || ProtectedBlocks.isEmpty())
			return false;
		if (!ProtectedBlocks.contains(new Jakky89ItemIdData(typeId, dataValue)) && !ProtectedBlocks.contains(new Jakky89ItemIdData(typeId, (short)-1)))
			return false;
		return true;
	}
	
	public boolean isProtectedBlock(Block block)
	{
		if (block != null)
			return isProtectedBlock((short)block.getTypeId(), (short)block.getData());
		return true;
	}
	
	public void setProtectedBlocks(Collection <Jakky89ItemIdData> blockSet)
	{
		if (blockSet != null)
			ProtectedBlocks = new HashSet<Jakky89ItemIdData>(blockSet);
		else
			ProtectedBlocks = null;
	}
	
	public void addToProtectedBlocks(Jakky89ItemIdData itemIdData)
	{
		if (itemIdData != null)
		{
			if (ProtectedBlocks == null)
				ProtectedBlocks = new HashSet<Jakky89ItemIdData>();
			
			ProtectedBlocks.add(itemIdData);
		}
	}

	public void removeFromProtectedBlocks(Jakky89ItemIdData itemIdData)
	{
		if (ProtectedBlocks == null || ProtectedBlocks.isEmpty())
			return;

		ProtectedBlocks.remove(itemIdData);
	}
	
	public List<String> getPreventedItemsAsStringList()
	{
		return Jakky89ItemUtils.itemIdValuesToStringList(PreventedItems);
	}
	
	public boolean isPreventedItem(short typeId, short dataValue)
	{
		if (PreventedItems == null || PreventedItems.isEmpty())
			return false;
		if (!PreventedItems.contains(new Jakky89ItemIdData(typeId, dataValue)) && !PreventedItems.contains(new Jakky89ItemIdData(typeId, (short)-1)))
			return false;
		return true;
	}
	
	public boolean isPreventedItem(Material material, short dataValue)
	{
		if (material != null)
			return isPreventedItem((short)material.getId(), dataValue);

		return true;
	}
	
	public boolean isPreventedItem(Material material)
	{
		if (material != null)
			return isPreventedItem((short)material.getId(), (short)-1);

		return true;
	}

	public void setPreventedItems(Collection<Jakky89ItemIdData> itemSet)
	{
		if (itemSet != null && itemSet.size() > 0)
			PreventedItems = new HashSet<Jakky89ItemIdData>(itemSet);
		else
			PreventedItems = null;
	}

	public void addToPreventedItems(Jakky89ItemIdData itemIdData)
	{
		if (itemIdData != null)
		{
			addToPreventedItems(itemIdData);
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

		if (plot.getPlotX() < minPlotX)
			minPlotX = plot.getPlotX();
		if (plot.getPlotX() > maxPlotX)
			maxPlotX = plot.getPlotX();
		if (plot.getPlotZ() < minPlotZ)
			minPlotZ = plot.getPlotZ();
		if (plot.getPlotZ() > maxPlotZ)
			maxPlotZ = plot.getPlotZ();
		plotPositions.put(plot.getPlotPosition(), plot);
		plot.refreshNeighbourPlots();
		return true;
	}

	public boolean unregisterPlot(Plot plot)
	{
		if (plot == null || plot.getPlotWorld() == null || !plot.getPlotWorld().equals(this) || plot.getPlotPosition() == null || plotPositions == null || plotPositions.isEmpty())
		{
			return false;
		}
		
		Plot removedPlot = plotPositions.remove(plot.getPlotPosition());
		if (removedPlot != null)
		{
			removedPlot.resetNeighbourPlots();
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
		return (double)(PlotSize + ((PathWidth + 1) / 2));
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
		
		if (bsx < pph && bsx > (ppp + 1) && bsz < pph && bsz > (ppp + 1))
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
		
		double pph = (double)(PathWidth + 1 / 2);
		
		int locx = (int)Math.ceil((double)(plot.getPlotX() * multi) + pph + 1);
		int locz = (int)Math.ceil((double)(plot.getPlotZ() * multi) + pph + 1);
		
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
		return bukkitWorld.getBlockAt((int)Math.round((double)(plotX * multi) + (double)(PlotSize / 2)), RoadHeight, (int)Math.round((double)(plotZ * multi) + (double)(PlotSize / 2)));
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
		
		int minX = (int)Math.ceil(Math.min(loc1.getBlockX(), loc2.getBlockX()) / divi);
		int minZ = (int)Math.ceil(Math.min(loc1.getBlockZ(), loc2.getBlockZ()) / divi);
		int maxX = (int)Math.floor(Math.max(loc1.getBlockX(), loc2.getBlockX()) / divi);
		int maxZ = (int)Math.floor(Math.max(loc1.getBlockZ(), loc2.getBlockZ()) / divi);
		
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
		
		int minChunkX = (int)Math.ceil((double)(minX / 16));
		int minChunkZ = (int)Math.ceil((double)(minZ / 16));
		int maxChunkX = (int)Math.floor((double)(maxX / 16));
		int maxChunkZ = (int)Math.floor((double)(maxZ / 16));

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
		
		Location baseLocation = getMinBlockLocation(plot);
		return getAreaEntities(baseLocation.getBlockX()+3, baseLocation.getBlockZ()+3, baseLocation.getBlockX()+PlotSize-3, baseLocation.getBlockZ()+PlotSize-3, includePlayers);
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
	public String toString()
	{
		return getName();
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
	    
	    if (this.id == ((PlotWorld)o).getId())
	    {
	    	return true;
	    }
	    
		return false;
	}

	@Override
	public int compareTo(PlotWorld pw2) {
		return this.id - pw2.getId();
	}
	
}
