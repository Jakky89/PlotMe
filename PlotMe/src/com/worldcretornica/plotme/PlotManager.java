package com.worldcretornica.plotme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.worldcretornica.plotme.utils.ExpiredPlotsComparator;
import com.worldcretornica.plotme.utils.Pair;
import com.worldcretornica.plotme.utils.RunnableExpiredPlotsRemover;

public class PlotManager {

    /**
     *  Maps world names to PlotWorld instances 
     */
	public static Map<World, PlotWorld> plotWorlds;
	public static Set<Plot> allPlots;
	public static List<Plot> expiredPlots;
	
	public static Long lastExpiredPlotDeletion;
	public static int expiredPlotDeletionsProcessed;
	public static Integer expiredPlotsCheckTaskId;
	public static Long nextExpiredPlotsCheck;

	
	public PlotManager()
	{
		plotWorlds = new HashMap<World, PlotWorld>();
		allPlots = new HashSet<Plot>();
		
		expiredPlots = new LinkedList<Plot>();
		expiredPlotsCheckTaskId = null;
		nextExpiredPlotsCheck = null;
	}
	
	public static boolean registerPlotWorld(PlotWorld plotWorld)
	{
		if (plotWorld == null || plotWorld.getMinecraftWorld() == null)
		{
			return false;
		}
		
		if (plotWorlds.put(plotWorld.getMinecraftWorld(), plotWorld) != plotWorld)
		{
			return true;
		}
		
		return false;
	}
	
	public static PlotWorld getPlotWorld(World minecraftWorld)
	{
		if (minecraftWorld != null)
		{
			return plotWorlds.get(minecraftWorld);
		}
		return null;
	}
	
	public void resetNeighbours(Plot plot)
	{
		if (plot == null)
		{
			return;
		}
		plot.resetNeighbourPlots();
	}

	public void refreshNeighbours(Plot plot)
	{
		if (plot == null || plot.getPlotPosition() == null || plot.getPlotWorld() == null)
		{
			return;
		}
		plot.getPlotWorld().refreshNeighbours(plot);
	}
	
	public static void checkPlotExpiration(Plot plot)
	{
		if (plot.getExpiration() > 0)
		{
			if (nextExpiredPlotsCheck == null || plot.getExpiration() < nextExpiredPlotsCheck)
			{
				if (expiredPlotsCheckTaskId != null)
				{
					Bukkit.getScheduler().cancelTask(expiredPlotsCheckTaskId);
					expiredPlotsCheckTaskId = null;
				}
				nextExpiredPlotsCheck = plot.getExpiration();
				Long ticksUntilExpiration = (long)Math.round((plot.getExpiration() * 20) + (System.currentTimeMillis() / 50));
				BukkitTask expiredPlotsCheckTask = null;
				if (ticksUntilExpiration > 0)
				{
					expiredPlotsCheckTask = Bukkit.getScheduler().runTaskLaterAsynchronously(PlotMe.self, new RunnableExpiredPlotsRemover(), ticksUntilExpiration);
				}
				else
				{
					expiredPlotsCheckTask = Bukkit.getScheduler().runTaskAsynchronously(PlotMe.self, new RunnableExpiredPlotsRemover());
				}
				expiredPlotsCheckTaskId = expiredPlotsCheckTask.getTaskId();
			}
		}
	}
	
	public static boolean registerPlot(Plot plot)
	{
		if (plot == null || plot.getId() <= 0 || plot.getPlotPosition() == null || plot.getPlotWorld() == null || (plot.getExpiration() > 0 && plot.getExpiration() < (System.currentTimeMillis()/1000)))
		{
			return false;
		}
		
		PlotWorld pwi = plot.getPlotWorld();
		if (pwi == null)
		{
			return false;
		}

		if (pwi.registerPlot(plot))
		{
			setOwnerSign(plot);
			allPlots.add(plot);
			checkPlotExpiration(plot);
			return true;
		}
		
		return false;
	}
	
	public static Location getPlotBlockTop(Plot plot)
	{
		double multi = plot.getPlotWorld().getPlotBlockPositionMultiplier();
		
		int maxX    = (int)Math.ceil(plot.getPlotPosition().x * multi);
		int maxZ    = (int)Math.ceil(plot.getPlotPosition().z * multi);

		return new Location(plot.getMinecraftWorld(), maxX, plot.getMinecraftWorld().getMaxHeight(), maxZ);
	}
	
	public static Plot getPlotAtBlockPosition(PlotWorld plotWorld, Location blockLocation)
	{
		if (plotWorld == null || blockLocation == null || !blockLocation.getWorld().equals(plotWorld.getMinecraftWorld())) {
			return null;
		}
		return plotWorld.getPlotAtBlockPosition(blockLocation);
	}
	
	public static Plot getPlotAtBlockPosition(World minecraftWorld, Location blockLocation)
	{
		if (minecraftWorld == null || blockLocation == null || !blockLocation.getWorld().equals(minecraftWorld))
		{
			return null;
		}
		PlotWorld plotWorld = plotWorlds.get(minecraftWorld);
		if (plotWorld != null && blockLocation != null && blockLocation.getWorld().equals(plotWorld.getMinecraftWorld())) {
			plotWorld.getPlotAtBlockPosition(blockLocation);
		}
		return null;
	}
	
	public static Plot getPlotAtBlockPosition(Location blockLocation)
	{
		if (blockLocation == null)
		{
			return null;
		}

		PlotWorld plotWorld = plotWorlds.get(blockLocation.getWorld());
		if (plotWorld != null && blockLocation != null && blockLocation.getWorld().equals(plotWorld.getMinecraftWorld())) {
			return plotWorld.getPlotAtBlockPosition(blockLocation);
		}
		
		return null;
	}
	
	public static Plot getPlotAtBlockPosition(Player player) 
	{
		return getPlotAtBlockPosition(player.getLocation());
	}
	
	public static Plot getPlotAtBlockPosition(Block block) 
	{
		return getPlotAtBlockPosition(block.getLocation());
	}
	
	public static Plot getPlotAtBlockPosition(BlockState block) 
	{
		return getPlotAtBlockPosition(block.getLocation());
	}
	
	public static void adjustLinkedPlots(Plot plot)
	{
		if (plot == null)
		{
			return;
		}

		if (plot.neighbourplots == null)
		{
			plot.getPlotWorld().refreshNeighbours(plot);
		}
		
		if (plot.neighbourplots != null)
		{
			for (int i=0; i<8; i++)
			{
				if (plot.neighbourplots != null)
				{
					if (plot.neighbourplots[i] != null)
					{
						if (plot.neighbourplots[i].owner.equals(plot.owner))
						{
							fillroad(plot.neighbourplots[i], plot);
						}
					}
				}
			}

			if (plot.neighbourplots[7] != null &&
				plot.neighbourplots[0] != null &&
				plot.neighbourplots[1] != null)
			{
				if (plot.neighbourplots[7].owner.equals(plot.owner) &&
					plot.neighbourplots[0].owner.equals(plot.owner) &&
					plot.neighbourplots[1].owner.equals(plot.owner))
				{
					fillmiddleroad(plot.neighbourplots[0], plot);
				}
			}
			
			if (plot.neighbourplots[1] != null &&
				plot.neighbourplots[2] != null &&
				plot.neighbourplots[3] != null)
			{
				if (plot.neighbourplots[1].owner.equals(plot.owner) &&
					plot.neighbourplots[2].owner.equals(plot.owner) &&
					plot.neighbourplots[3].owner.equals(plot.owner))
				{
					fillmiddleroad(plot.neighbourplots[2], plot);
				}
			}
				
			if (plot.neighbourplots[3] != null &&
				plot.neighbourplots[4] != null &&
				plot.neighbourplots[5] != null)
			{
				if (plot.neighbourplots[3].owner.equals(plot.owner) &&
					plot.neighbourplots[4].owner.equals(plot.owner) &&
					plot.neighbourplots[5].owner.equals(plot.owner))
				{
					fillmiddleroad(plot.neighbourplots[4], plot);
				}
			}
			
			if (plot.neighbourplots[5] != null &&
				plot.neighbourplots[6] != null &&
				plot.neighbourplots[7] != null)
			{
				if (plot.neighbourplots[5].owner.equals(plot.owner) &&
					plot.neighbourplots[6].owner.equals(plot.owner) &&
					plot.neighbourplots[7].owner.equals(plot.owner))
				{
					fillmiddleroad(plot.neighbourplots[6], plot);
				}
			}
		}
	}
	
	private static void fillroad(Plot plot1, Plot plot2)
	{
		if (!plot1.getPlotWorld().equals(plot2.getPlotWorld()))
		{
			return;
		}

		double multi = plot1.getPlotWorld().getPlotBlockPositionMultiplier();
		
		int minX1 = (int)Math.floor((plot1.getPlotX() - 1) * multi);
		int minZ1 = (int)Math.floor((plot1.getPlotZ() - 1) * multi);
		int maxX1 =	(int)Math.ceil(plot1.getPlotX() * multi);
		int maxZ1 =	(int)Math.ceil(plot1.getPlotZ() * multi);
		
		int minX2 = (int)Math.floor((plot2.getPlotX() - 1) * multi);
		int minZ2 = (int)Math.floor((plot2.getPlotZ() - 1) * multi);
		int maxX2 = (int)Math.ceil(plot2.getPlotX() * multi);
		int maxZ2 =	(int)Math.ceil(plot2.getPlotZ() * multi);
		
		int minX;
		int maxX;
		int minZ;
		int maxZ;
		boolean isWallX;
			
		if (minX1 == minX2)
		{
			minX = minX1;
			maxX = maxX1;
			
			minZ = Math.min(minZ1, minZ2) + plot1.getPlotWorld().PlotSize;
			maxZ = Math.max(maxZ1, maxZ2) - plot1.getPlotWorld().PlotSize;
		}
		else
		{
			minZ = minZ1;
			maxZ = maxZ1;
			
			minX = Math.min(minX1, minX2) + plot1.getPlotWorld().PlotSize;
			maxX = Math.max(maxX1, maxX2) - plot1.getPlotWorld().PlotSize;
		}
		
		isWallX = (maxX - minX) > (maxZ - minZ);
		
		if (isWallX)
		{
			minX--;
			maxX++;
		}
		else
		{
			minZ--;
			maxZ++;
		}
		
		int maxY = plot1.getMinecraftWorld().getMaxHeight();

		for (int x = minX; x <= maxX; x++)
		{
			for (int z = minZ; z <= maxZ; z++)
			{
				for (int y = plot1.getPlotWorld().RoadHeight; y < maxY; y++)
				{
					if (y >= (plot1.getPlotWorld().RoadHeight + 2))
					{
						plot1.getMinecraftWorld().getBlockAt(x, y, z).setType(Material.AIR);
					}
					else if(y == (plot1.getPlotWorld().RoadHeight + 1))
					{
						if (isWallX && (x == minX || x == maxX))
						{
							plot1.getMinecraftWorld().getBlockAt(x, y, z).setTypeIdAndData(plot1.getPlotWorld().WallBlockId, plot1.getPlotWorld().WallBlockValue, true);
						}
						else if(!isWallX && (z == minZ || z == maxZ))
						{
							plot1.getMinecraftWorld().getBlockAt(x, y, z).setTypeIdAndData(plot1.getPlotWorld().WallBlockId, plot1.getPlotWorld().WallBlockValue, true);
						}
						else
						{
							plot1.getMinecraftWorld().getBlockAt(x, y, z).setType(Material.AIR);
						}
					}
					else
					{
						plot1.getMinecraftWorld().getBlockAt(x, y, z).setTypeIdAndData(plot1.getPlotWorld().PlotFloorBlockId, plot1.getPlotWorld().PlotFloorBlockValue, true);
					}
				}
			}
		}
	}
	
	private static void fillmiddleroad(Plot plot1, Plot plot2)
	{
		if (!plot1.getPlotWorld().equals(plot2.getPlotWorld())) {
			return;
		}
		
		double multi = plot1.getPlotWorld().getPlotBlockPositionMultiplier();
		
		int minX1 =	(int)Math.floor((plot1.getPlotX() - 1) * multi);
		int minZ1 =	(int)Math.floor((plot1.getPlotZ() - 1) * multi);
		int maxX1 = (int)Math.ceil(plot1.getPlotX() * multi);
		int maxZ1 =	(int)Math.ceil(plot1.getPlotZ() * multi);
		
		int minX2 = (int)Math.floor((plot2.getPlotX() - 1) * multi);
		int minZ2 = (int)Math.floor((plot2.getPlotZ() - 1) * multi);
		int maxX2 = (int)Math.ceil(plot2.getPlotX() * multi);
		int maxZ2 = (int)Math.ceil(plot2.getPlotZ() * multi);

		int minX;
		int maxX;
		int minZ;
		int maxZ;

		minX = Math.min(maxX1, maxX2);
		maxX = Math.max(minX1, minX2);
		
		minZ = Math.min(maxZ1, maxZ2);
		maxZ = Math.max(minZ1, minZ2);
		
		int maxY = plot1.getMinecraftWorld().getMaxHeight();
				
		for (int x = minX; x <= maxX; x++)
		{
			for (int z = minZ; z <= maxZ; z++)
			{
				for (int y = plot1.getPlotWorld().RoadHeight; y < maxY; y++)
				{
					if(y >= (plot1.getPlotWorld().RoadHeight + 1))
					{
						plot1.getMinecraftWorld().getBlockAt(x, y, z).setType(Material.AIR);
					}
					else
					{
						plot1.getMinecraftWorld().getBlockAt(x, y, z).setTypeId(plot1.getPlotWorld().PlotFloorBlockId);
					}
				}
			}
		}
	}
	
	public static boolean isPlotAvailable(PlotPosition plotPosition)
	{
		if (plotPosition == null)
		{
			return false;
		}

		PlotWorld pwi = plotWorlds.get(plotPosition.getMinecraftWorld());
		if (pwi == null)
		{
			return false;
		}
		
		Plot plp = pwi.getPlotAtPlotPosition(plotPosition);
		if (plp == null)
		{
			return true;
		}

		return false;
	}
	
	public static boolean isPlotAvailable(World minecraftWorld, int plotX, int plotZ)
	{
		if (minecraftWorld == null)
		{
			return false;
		}
		
		PlotWorld pwi = plotWorlds.get(minecraftWorld);
		if (pwi == null)
		{
			return false;
		}
			
		Plot plp = pwi.getPlotAtPlotPosition(plotX, plotZ);
		if (plp == null)
		{
			return true;
		}

		return false;
	}
	
	public static boolean isPlotAvailable(String worldName, int plotX, int plotZ)
	{
		return isPlotAvailable(Bukkit.getWorld(worldName), plotX, plotZ);
	}
	
	public static boolean isPlotAvailable(Player player)
	{
		if (player == null)
		{
			return false;
		}
		
		PlotWorld pwi = plotWorlds.get(player.getWorld());
		if (pwi == null)
		{
			return false;
		}
		
		return (pwi.getPlotAtBlockPosition(player.getLocation()) == null);
	}
	
	public static void actualizePlotSignInfoLine(Plot plot)
	{
		if (plot == null || plot.getMinecraftWorld() == null)
		{
			return;
		}
		
		double multi = plot.getPlotWorld().getPlotBlockPositionMultiplier();
		
		int minX = (int)Math.floor((plot.getPlotPosition().x - 1) * multi);
		int minZ = (int)Math.floor((plot.getPlotPosition().z - 1) * multi);

		Location pillar = new Location(plot.getMinecraftWorld(), minX - 1, plot.getPlotWorld().RoadHeight + 1, minZ - 1);
						
		Block bsign = pillar.add(0, 0,-1).getBlock();
		if (!(bsign instanceof Sign))
		{
			return;
		}
		
		Sign sign = (Sign)bsign.getState();
		
		String infoLine = "";
		if (plot.isFinished())
		{
			infoLine = PlotMe.caption("InfoFinished");
		}
		else if (plot.isProtected())
		{
			infoLine = PlotMe.caption("InfoProtected");
		}
		else
		{
			if (plot.expireddate > 0)
			{
				int secsRemain = Math.round(plot.expireddate - (System.currentTimeMillis()/1000));
				if (secsRemain > 0)
				{
					if (secsRemain < 2592000)
					{
						if (secsRemain > 604800)
						{
							infoLine = PlotMe.caption("InfoExpire") + " +" + String.valueOf(Math.floor(secsRemain / 604800)) + "w";
						}
						else if (secsRemain > 86400)
						{
							infoLine = PlotMe.caption("InfoExpire") + " +" + String.valueOf(Math.floor(secsRemain / 86400)) + "d";
						}
						else if (secsRemain > 3600)
						{
							infoLine = PlotMe.caption("InfoExpire") + " +" + String.valueOf(Math.floor(secsRemain / 3600)) + "h";
						}
						sign.update(true);
					}
				}
			}
		}
		if (sign.getLine(3) != infoLine)
		{
			sign.setLine(3, infoLine);
			sign.update(true);
		}
	}

	public static void setOwnerSign(Plot plot)
	{
		if (plot == null || plot.getMinecraftWorld() == null)
		{
			return;
		}
		
		double multi = plot.getPlotWorld().getPlotBlockPositionMultiplier();
		
		int minX = (int)Math.floor((plot.getPlotPosition().x - 1) * multi);
		int minZ = (int)Math.floor((plot.getPlotPosition().z - 1) * multi);

		Location pillar = new Location(plot.getMinecraftWorld(), minX - 1, plot.getPlotWorld().RoadHeight + 1, minZ - 1);
						
		Block bsign = pillar.add(0, 0,-1).getBlock();
		bsign.setType(Material.AIR);
		bsign.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte)2, false);
		if (!(bsign instanceof Sign))
		{
			return;
		}
		
		Sign sign = (Sign)bsign.getState();
		
		sign.setLine(0, PlotMe.caption("SignId") + String.valueOf(plot.id));
		
		String tmpOwnerCaption;
		if (PlotMe.useDisplayNamesOnSigns)
		{
			tmpOwnerCaption = PlotMe.caption("SignOwner") + plot.owner.getDisplayName();
		}
		else
		{
			tmpOwnerCaption = PlotMe.caption("SignOwner") + plot.owner.getRealPlayerName();
		}
		if (tmpOwnerCaption.length() > 16)
		{
			sign.setLine(2, tmpOwnerCaption.substring(0, 16));
			if (tmpOwnerCaption.length() > 32)
			{
				sign.setLine(3, tmpOwnerCaption.substring(16, 32));
			}
			else
			{
				sign.setLine(3, tmpOwnerCaption.substring(16));
			}
			sign.update(true);
		}
		else
		{
			sign.setLine(2, tmpOwnerCaption);
			actualizePlotSignInfoLine(plot);
		}
		
	}
	
	public static void setSellSign(Plot plot)
	{
		if (plot == null || plot.getMinecraftWorld() == null)
		{
			return;
		}
		
		removeSellSign(plot);
		
		if (plot.isforsale || plot.isauctionned)
		{
			double multi = plot.getPlotWorld().getPlotBlockPositionMultiplier();
			
			int minX = (int)Math.floor((plot.getPlotPosition().x - 1) * multi);
			int minZ = (int)Math.floor((plot.getPlotPosition().z - 1) * multi);

			Location pillar = new Location(plot.getMinecraftWorld(), minX - 1, plot.getPlotWorld().RoadHeight + 1, minZ - 1);
						
			Block bsign = pillar.clone().add(-1, 0, 0).getBlock();
			bsign.setType(Material.AIR);
			bsign.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte)4, false);
			
			Sign sign = (Sign) bsign.getState();

			if (plot.isforsale)
			{
				sign.setLine(0, PlotMe.caption("SignForSale"));
				sign.setLine(1, PlotMe.caption("SignPrice"));
				int tmpPrice = (int)Math.round(plot.sellprice * 100);
				sign.setLine(2, PlotMe.caption("SignPriceColor") + String.valueOf(tmpPrice / 100));
				sign.setLine(3, "/plotme " + PlotMe.caption("CommandBuy"));
				sign.update(true);
			}

			if (plot.isauctionned)
			{				
				if (plot.isforsale)
				{
					bsign = pillar.clone().add(-1, 0, 1).getBlock();
					bsign.setType(Material.AIR);
					bsign.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte)4, false);
					sign = (Sign) bsign.getState();
				}
				
				sign.setLine(0, PlotMe.caption("SignOnAuction"));
				
				if (plot.auctionbids != null && plot.auctionbids.size()>0)
				{
					sign.setLine(1, PlotMe.caption("SignCurrentBid"));
					PlotAuctionBid highestBid = plot.auctionbids.get(0);
					int tmpAmount = (int)Math.round(highestBid.getBidMoneyAmount() * 100);
					String tmpAuctionLine = PlotMe.caption("SignCurrentBidColor") + Math.round(tmpAmount / 100);
					if (tmpAuctionLine.length() < 16)
					{
						sign.setLine(2, tmpAuctionLine);
					}
					else
					{
						sign.setLine(2, ">" + tmpAuctionLine.substring(0, 14));
					}
					String bname;
					if (PlotMe.useDisplayNamesOnSigns)
					{
						bname = highestBid.getBidderDisplayName();
					}
					else
					{
						bname = highestBid.getBidderRealPlayerName();
					}
					if (bname.length() < 16)
					{
						sign.setLine(3, PlotMe.caption("SignCurrentBidColor") + bname);
					}
					else
					{
						sign.setLine(3, PlotMe.caption("SignCurrentBidColor") + bname.substring(0, 12) + "...");
					}
				}
				else
				{
					sign.setLine(1, PlotMe.caption("SignMinimumBid"));
				}

				sign.setLine(3, "/plotme " + PlotMe.caption("CommandBid") + " <x>");
				
				sign.update(true);
			}
		}
	}
	
	public static void removeOwnerSign(Plot plot)
	{
		if (plot == null || plot.getMinecraftWorld() == null)
		{
			return;
		}
		
		if (plot.getOwnerSign() != null)
		{
			plot.getOwnerSign().setType(Material.AIR);
			plot.setOwnerSign(null);
			return;
		}
		
		Location baseLocation = plot.getWorldMinBlockLocation();
		
		Location pillar = new Location(plot.getMinecraftWorld(), baseLocation.getBlockX() - 1, plot.getPlotWorld().RoadHeight + 1, baseLocation.getBlockZ() - 1);
		
		Block bsign = pillar.add(0, 0, -1).getBlock();
		if (bsign != null && (bsign instanceof Sign) && (bsign.getType() == Material.SIGN_POST || bsign.getType() == Material.WALL_SIGN))
		{
			bsign.setType(Material.AIR);
		}
	}
	
	public static void removeSellSign(Plot plot)
	{
		if (plot == null || plot.getMinecraftWorld() == null)
		{
			return;
		}
		
		if (plot.getSellSign() != null)
		{
			plot.getSellSign().setType(Material.AIR);
			plot.setSellSign(null);
			return;
		}
		
		Location baseLocation = plot.getWorldMinBlockLocation();
		
		Location pillar = new Location(plot.getMinecraftWorld(), baseLocation.getBlockX() - 1, plot.getPlotWorld().RoadHeight + 1, baseLocation.getBlockZ() - 1);
		
		Block bsign = pillar.add(-1, 0, 0).getBlock();
		if (bsign != null && (bsign instanceof Sign) && (bsign.getType() == Material.SIGN_POST || bsign.getType() == Material.WALL_SIGN))
		{
			bsign.setType(Material.AIR);
		}
	}
	
	public static void refreshPlotChunks(Plot plot)
	{
		if (plot == null || plot.getMinecraftWorld() == null)
		{
			return;
		}
		
		Location baseLocation = plot.getWorldMinBlockLocation();
		
		for (int x = 0; x <= plot.getPlotSize(); x++)
		{
			for (int z = 0; z <= plot.getPlotSize(); z++)
			{
				plot.getMinecraftWorld().refreshChunk(baseLocation.getBlockX() + x, baseLocation.getBlockZ() + z);
			}
		}
	}
	
	public static void setBiome(Plot plot, Biome bio)
	{
		if (plot == null || plot.getMinecraftWorld() == null || bio == null)
		{
			return;
		}
		
		if (plot.setBiome(bio))
		{
			Location baseLocation = plot.getWorldMinBlockLocation();
	
			for (int x = 0; x <= plot.getPlotSize(); x++)
			{
				for (int z = 0; z <= plot.getPlotSize(); z++)
				{
					plot.getMinecraftWorld().setBiome(baseLocation.getBlockX() + x, baseLocation.getBlockZ() + z, bio);
				}
			}
			refreshPlotChunks(plot);
		}
	}

	public static void clear(Location loc1, Location loc2)
	{
		if (loc1 == null || loc2 == null || loc1.getWorld() == null || loc2.getWorld() == null || !loc1.getWorld().equals(loc2.getWorld()))
		{
			return;
		}
		
		World mw = loc1.getWorld();
		PlotWorld pwi = getPlotWorld(mw);
		if (pwi == null)
		{
			return;
		}
		
		Location minLocation = new Location(mw, Math.min(loc1.getX(), loc2.getX()), Math.min(loc1.getY(), loc2.getY()), Math.min(loc1.getZ(), loc2.getZ()));
		Location maxLocation = new Location(mw, Math.max(loc1.getX(), loc2.getX()), Math.max(loc1.getY(), loc2.getY()), Math.max(loc1.getZ(), loc2.getZ()));

		int minX = minLocation.getBlockX();
		int minZ = minLocation.getBlockZ();
		int maxX = maxLocation.getBlockX();
		int maxZ = maxLocation.getBlockZ();

		int minChunkX = (int)Math.floor((double)minX / 16);
		int minChunkZ = (int)Math.floor((double)minZ / 16);
		int maxChunkX = (int)Math.ceil((double)maxX / 16);
		int maxChunkZ = (int)Math.ceil((double)maxZ / 16);

		for (int cx = minChunkX; cx <= maxChunkX; cx++)
		{			
			for (int cz = minChunkZ; cz <= maxChunkZ; cz++)
			{
				Chunk chunk = mw.getChunkAt(cx, cz);
				Entity[] entities = chunk.getEntities();
				if (entities.length > 0)
				{
					for (Entity entity : entities)
					{
						Location entityloc = entity.getLocation();
						if (!(entity instanceof Player) && entityloc.getBlockX() >= minLocation.getBlockX() && entityloc.getBlockX() <= maxLocation.getBlockX() &&
														   entityloc.getBlockZ() >= minLocation.getBlockZ() && entityloc.getBlockZ() <= maxLocation.getBlockZ())
						{
							entity.remove();
						}
					}
				}
			}
		}

		for (int x = minX; x <= maxX; x++)
		{
			for (int z = minZ; z <= maxZ; z++)
			{
				Block block = new Location(minLocation.getWorld(), x, 0, z).getBlock();
				block.setBiome(Biome.PLAINS);
				
				for (int y = minLocation.getWorld().getMaxHeight(); y >= 0; y--)
				{
					block = new Location(mw, x, y, z).getBlock();
					BlockState bstate = block.getState();
					if (bstate instanceof InventoryHolder)
					{
						InventoryHolder holder = (InventoryHolder)bstate;
						holder.getInventory().clear();
					}

					if (bstate instanceof Jukebox)
					{
						((Jukebox)bstate).setPlaying(null);
					}
					
					if (y == 0)
					{
						block.setTypeId(pwi.BottomBlockId);
					}
					else if (y < pwi.RoadHeight)
					{
						block.setTypeId(pwi.PlotFillingBlockId);
					}
					else if (y == pwi.RoadHeight)
					{
						block.setTypeId(pwi.PlotFloorBlockId);
					}
					else
					{
						if (y > pwi.RoadHeight + 1 && 
								 (x < minX - 1 || 
								  x > maxX + 1 ||
								  z < minZ - 1 || 
								  z > maxZ + 1))
						{
							block.setTypeIdAndData(0, (byte)0, false);
						}
					}
				}
			}
		}
		adjustWall(minLocation);
	}
	
	public static void clearPlot(Plot plot)
	{
		Pair<Location, Location> locations = plot.getPlotWorld().getMinMaxBlockLocation(plot);
		removeOwnerSign(plot);
		removeSellSign(plot);
		clear(locations.getLeft(), locations.getRight());
		
		removeLWCProtections(plot);

		//regen(plot);
	}
	
	public static void removePlot(Plot plot)
	{
		clearPlot(plot);
		PlotMeSqlManager.removePlot(plot);
		allPlots.remove(plot);
	}
		
	public static void adjustWall(Location loc)
	{
		if (loc == null)
		{
			return;
		}

		PlotWorld pwi = plotWorlds.get(loc.getWorld());
		if (pwi == null)
		{
			return;
		}

		List<Pair<Short, Byte>> wallids = new ArrayList<Pair<Short, Byte>>();
		
		Plot plot = pwi.getPlotAtBlockPosition(loc);
		if (plot != null)
		{
			if (plot.isprotected)
			{
				wallids.add(new Pair<Short, Byte>(pwi.ProtectedWallBlockId, null));
			}
			if (plot.isauctionned)
			{
				wallids.add(new Pair<Short, Byte>(pwi.AuctionWallBlockId, null));
			}
			if (plot.isforsale)
			{
				wallids.add(new Pair<Short, Byte>(pwi.ForSaleWallBlockId, null));
			}
		}
		
		if (wallids.size() == 0){
			wallids.add(new Pair<Short, Byte>(pwi.WallBlockId, pwi.WallBlockValue));
		}
		
		int ctr = 0;
			
		Pair<Location, Location> locations = plot.getWorldMinMaxBlockLocations();
		
		int x;
		int z;
		
		Pair<Short, Byte> currentblockid;
		Block block;
		
		for (x = locations.getLeft().getBlockX() - 1; x < locations.getRight().getBlockX() + 1; x++)
		{
			z = locations.getLeft().getBlockZ() - 1;
			currentblockid = wallids.get(ctr);
			ctr = (ctr == wallids.size()-1)? 0 : ctr + 1;
			block = pwi.getMinecraftWorld().getBlockAt(x, pwi.RoadHeight + 1, z);
			setWall(block, currentblockid);
		}
		
		for (z = locations.getLeft().getBlockZ() - 1; z < locations.getRight().getBlockZ() + 1; z++)
		{
			x = locations.getRight().getBlockX() + 1;
			currentblockid = wallids.get(ctr);
			ctr = (ctr == wallids.size()-1)? 0 : ctr + 1;
			block = pwi.getMinecraftWorld().getBlockAt(x, pwi.RoadHeight + 1, z);
			setWall(block, currentblockid);
		}
		
		for (x = locations.getRight().getBlockX() + 1; x > locations.getLeft().getBlockX() - 1; x--)
		{
			z = locations.getRight().getBlockZ() + 1;
			currentblockid = wallids.get(ctr);
			ctr = (ctr == wallids.size()-1)? 0 : ctr + 1;
			block = pwi.getMinecraftWorld().getBlockAt(x, pwi.RoadHeight + 1, z);
			setWall(block, currentblockid);
		}
		
		for (z = locations.getRight().getBlockZ() + 1; z > locations.getLeft().getBlockZ() - 1; z--)
		{
			x = locations.getLeft().getBlockX() - 1;
			currentblockid = wallids.get(ctr);
			ctr = (ctr == wallids.size()-1)? 0 : ctr + 1;
			block = pwi.getMinecraftWorld().getBlockAt(x, pwi.RoadHeight + 1, z);
			setWall(block, currentblockid);
		}
	}
	
	
	private static void setWall(Block block, Pair<Short, Byte> blockIdData)
	{
		if (block == null)
		{
			return;
		}
		
		PlotWorld pwi = plotWorlds.get(block.getWorld());
		if (pwi == null)
		{
			return;
		}
		
		if (blockIdData != null)
		{
			if (blockIdData.getLeft() != null && blockIdData.getRight() != null)
			{
				block.setTypeIdAndData(blockIdData.getLeft(), blockIdData.getRight(), true);
				return;
			}
			else if (blockIdData.getRight() == null)
			{
				block.setTypeId(blockIdData.getLeft());
				return;
			}
		}
		block.setTypeIdAndData(pwi.WallBlockId, pwi.WallBlockValue, true);
	}
	
	
	public static boolean isBlockInPlot(Plot plot, Location blockLocation)
	{
		if (!plot.getMinecraftWorld().equals(blockLocation.getWorld()))
		{
			return false;
		}
		
		Pair<Location, Location> locations = plot.getPlotWorld().getMinMaxBlockLocation(plot);
		
		if (blockLocation.getBlockX() >= locations.getLeft().getBlockX() && blockLocation.getBlockX() <= locations.getRight().getBlockX()
		 && blockLocation.getBlockZ() >= locations.getLeft().getBlockZ() && blockLocation.getBlockZ() <= locations.getRight().getBlockZ())
		{
			return true;
		}
		return false;
	}
	
	public static List<Pair<BlockState, Biome>> getPlotBlocksSnapshot(Plot plot, boolean includeAir)
	{
		if (plot == null || plot.getMinecraftWorld() == null)
		{
			return null;
		}
		
		ArrayList<Pair<BlockState, Biome>> tmpList = new ArrayList<Pair<BlockState, Biome>>();
		
		Location baseLocation = plot.getWorldMinBlockLocation();
		
		World minecraftWorld = plot.getMinecraftWorld();
		
		int minx = baseLocation.getBlockX();
		int minz = baseLocation.getBlockZ();
		int maxx = minx + plot.getPlotWorld().PlotSize;
		int maxz = minz + plot.getPlotWorld().PlotSize;
		
		int x;
		int y;
		int z;
		
		int maxY = 0;
		if (includeAir)
		{
			maxY = minecraftWorld.getMaxHeight();
		}
		Block block;
		
		if (maxY > 0)
		{
			for (x = minx; x <= maxx; x++)
			{
				for (z = minz; z <= maxz; z++)
				{
					if (!includeAir)
					{
						maxY = minecraftWorld.getHighestBlockYAt(x, z);
					}
					for (y = 0; y < maxY; y++)
					{
						block = minecraftWorld.getBlockAt(x, y, z);
						if (block.getType() != Material.AIR || includeAir)
						{
							tmpList.add(new Pair<BlockState, Biome>(block.getState(), block.getBiome()));
						}
					}
				}
			}
		}
		
		return tmpList;
	}
	
	public static boolean moveOverwritePlot(Plot plot, PlotPosition targetPlotPosition)
	{
		if (plot == null || plot.getMinecraftWorld() == null || targetPlotPosition == null || targetPlotPosition.getMinecraftWorld() == null)
		{
			return false;
		}

		PlotWorld pwiFrom = plot.getPlotWorld();
		PlotWorld pwiTo   = targetPlotPosition.getPlotWorld();
		
		Plot testPlot = pwiTo.getPlotAtPlotPosition(targetPlotPosition);
		if (testPlot != null)
		{
			removePlot(testPlot);
		}
		
		removeOwnerSign(plot);
		removeSellSign(plot);
		removeLWCProtections(plot);
		
		double multiFrom  = pwiFrom.getPlotBlockPositionMultiplier();
		double multiTo    = pwiTo.getPlotBlockPositionMultiplier();
		
		World mwiFrom     = pwiFrom.getMinecraftWorld();
		World mwiTo       = pwiTo.getMinecraftWorld();
		
		int maxY = Math.min(mwiFrom.getMaxHeight(), mwiTo.getMaxHeight());
		if (maxY <= 0)
		{
			return false;
		}
		
		// road height delta
		int rhd = pwiTo.RoadHeight - pwiFrom.RoadHeight;

		Location baseFrom = plot.getWorldMinBlockLocation();
		
		Location baseTo   = new Location(mwiTo, Math.floor(targetPlotPosition.getPlotX() * multiTo), 0, Math.floor(targetPlotPosition.getPlotZ() * multiTo));
		
		int x;
		int y;
		int z;
		
		Block sourceBlock;
		Biome sourceBiome;
		BlockState sourceBlockState;
		ItemStack[] sourceInventoryContents;
		
		Block targetBlock;

		for (x = 0; x <= pwiFrom.PlotSize; x++)
		{
			for (z = 0; z <= pwiFrom.PlotSize; z++)
			{
				inner2 : for (y = 0; y < maxY; y++)
				{
					if (y + rhd >= maxY || y + rhd <= 0)
					{
						break inner2;
					}
					
					// Copy source block data
					sourceBlock = mwiFrom.getBlockAt(baseFrom.getBlockX() + x, y, baseFrom.getBlockZ() + z);
					sourceBiome = sourceBlock.getBiome();
					sourceBlockState = sourceBlock.getState();
					if (sourceBlock instanceof InventoryHolder)
					{
						sourceInventoryContents = ((InventoryHolder)sourceBlock).getInventory().getContents().clone();
						((InventoryHolder)sourceBlock).getInventory().clear();
					}
					else
					{
						sourceInventoryContents = null;
					}
					
					// For security reasons we first remove the old block
					if (y > pwiFrom.RoadHeight)
					{
						sourceBlock.setType(Material.AIR);
					}
					else
					{
						sourceBlock.setTypeIdAndData(pwiFrom.BottomBlockId, pwiFrom.BottomBlockValue, false);
					}
					
					// Move to destination
					targetBlock = mwiTo.getBlockAt(baseTo.getBlockX() + x, y + rhd, baseTo.getBlockZ() + z);
					if (targetBlock != null)
					{
						targetBlock.setBiome(sourceBiome);
						targetBlock.setTypeIdAndData(sourceBlockState.getTypeId(), sourceBlockState.getRawData(), false);
						if (sourceInventoryContents != null)
						{
							((InventoryHolder)targetBlock).getInventory().setContents(sourceInventoryContents);
						}
					}
				}
			}
		}
		removePlot(plot);
		
		return true;
	}
	
	public static boolean swapPlots(Plot plot1, Plot plot2)
	{
		if (plot1 == null || plot2 == null || plot1.getMinecraftWorld() == null || plot2.getMinecraftWorld() == null)
		{
			return false;
		}
		
		PlotWorld plotWorld1 = plot1.getPlotWorld();
		PlotWorld plotWorld2 = plot2.getPlotWorld();
				
		// Remove signs
		removeOwnerSign(plot1);
		removeOwnerSign(plot2);
		removeSellSign(plot1);
		removeSellSign(plot2);
		
		// Remove protections
		removeLWCProtections(plot1);
		removeLWCProtections(plot2);
		
		// Take a snapshot of entities (also including players -> will be more fun when players standing on plot will move with it ;) )

		List<Entity> tempEntities1 = plotWorld1.getSinglePlotEntities(plot1, true);
		List<Entity> tempEntities2 = plotWorld1.getSinglePlotEntities(plot2, true);
		
		World minecraftWorld1 = plotWorld1.getMinecraftWorld();
		World minecraftWorld2 = plotWorld2.getMinecraftWorld();
		
		int maxY = Math.min(minecraftWorld1.getMaxHeight(), minecraftWorld2.getMaxHeight());
		if (maxY <= 0)
		{
			return false;
		}
		
		int plotSize = Math.min(plotWorld1.PlotSize, plotWorld2.PlotSize);
		
		// half of plot size delta
		int phd = Math.round((plotWorld2.PlotSize - plotWorld1.PlotSize) / 2);
		
		// road height delta
		int rhd = plotWorld1.RoadHeight - plotWorld2.RoadHeight;

		Location baseLocation1 = plot1.getWorldMinBlockLocation();
		Location baseLocation2 = plot2.getWorldMinBlockLocation();

		int x;
		int y;
		int z;
		
		int maxY1 = 1;
		int maxY2 = 1;
		
		Block block1;
		Biome biome1;
		BlockState blockState1;
		ItemStack[] blockInventoryContents1;
		
		Block block2;
		Biome biome2;
		BlockState blockState2;
		ItemStack[] blockInventoryContents2;
		
		Block targetBlock;
		
		for (x = 0; x < plotSize; x++)
		{
			for (z = 0; z < plotSize; z++)
			{
				inner2 : for (y = 0; y < maxY; y++)
				{
					if (rhd != 0)
					{
						if (y + Math.abs(rhd) >= maxY1 || y + Math.abs(rhd) >= maxY2 || y - Math.abs(rhd) < 1)
						{
							break inner2;
						}
					}

					// Get block data copies
					
					// Plot 1
					block1 = plot1.getMinecraftWorld().getBlockAt(baseLocation1.getBlockX() + x, y, baseLocation1.getBlockZ() + z);
					biome1 = block1.getBiome();
					blockState1 = block1.getState();
					if (block1 instanceof InventoryHolder)
					{
						blockInventoryContents1 = ((InventoryHolder)block1).getInventory().getContents().clone();
						((InventoryHolder)block1).getInventory().clear();
					}
					else
					{
						blockInventoryContents1 = null;
					}
					// For security reasons we first "remove" the old block
					if (y > plotWorld1.RoadHeight)
					{
						block1.setType(Material.AIR);
					}
					else
					{
						block1.setTypeIdAndData(plotWorld1.BottomBlockId, plotWorld1.BottomBlockValue, false);
					}
					
					// Plot 2
					block2 = plot2.getMinecraftWorld().getBlockAt(baseLocation2.getBlockX() + x, y, baseLocation2.getBlockZ() + z);
					biome2 = block2.getBiome();
					blockState2 = block2.getState();
					if (block2 instanceof InventoryHolder)
					{
						blockInventoryContents2 = ((InventoryHolder)block2).getInventory().getContents().clone();
						((InventoryHolder)block2).getInventory().clear();
					}
					else
					{
						blockInventoryContents2 = null;
					}
					// For security reasons we first "remove" the old block
					if (y > plotWorld2.RoadHeight)
					{
						block2.setType(Material.AIR);
					}
					else
					{
						block2.setTypeIdAndData(plotWorld2.BottomBlockId, plotWorld2.BottomBlockValue, false);
					}
					
					block2.setBiome(biome1);
					block2.setTypeIdAndData(blockState1.getTypeId(), blockState1.getRawData(), false);
					
					block1.setTypeIdAndData(blockState2.getTypeId(), blockState2.getRawData(), false);
					block1.setBiome(biome2);
					
					if (blockInventoryContents1 != null)
					{
						((InventoryHolder)block2).getInventory().setContents(blockInventoryContents2);
					}
					if (blockInventoryContents2 != null)
					{
						((InventoryHolder)block1).getInventory().setContents(blockInventoryContents1);
					}

					// Compensate road height and plot size differences
					if (rhd != 0)
					{
						block1.getLocation().add(-phd, -rhd, -phd);
						block2.getLocation().add(phd, rhd, phd);
					}
					
				}
			}
		}

		Iterator<Entity> entityIterator;
		Entity tmpEntity;
		Location tmpLoc;
		
		entityIterator = tempEntities1.iterator();
		while (entityIterator.hasNext())
		{
			tmpEntity = entityIterator.next();
			tmpLoc = tmpEntity.getLocation();
			tmpEntity.teleport(new Location(minecraftWorld2, (tmpLoc.getX() - baseLocation1.getX()) + baseLocation2.getX() + phd, tmpLoc.getY() + rhd, (tmpLoc.getZ() - baseLocation1.getZ()) +  baseLocation2.getZ() + phd));
		}
		
		entityIterator = tempEntities2.iterator();
		while (entityIterator.hasNext())
		{
			tmpEntity = entityIterator.next();
			tmpLoc = tmpEntity.getLocation();
			tmpEntity.teleport(new Location(minecraftWorld1, (tmpLoc.getX() - baseLocation2.getX()) + baseLocation1.getX() + phd, tmpLoc.getY() - rhd, (tmpLoc.getZ() - baseLocation2.getZ()) + baseLocation1.getX() + phd));
		}

		PlotMeSqlManager.updatePlotData(plot1, "xpos", plot2.getPlotX());
		PlotMeSqlManager.updatePlotData(plot1, "zpos", plot2.getPlotZ());
		PlotMeSqlManager.updatePlotData(plot1, "zpos", plot2.getPlotWorld().getId());
		
		PlotMeSqlManager.updatePlotData(plot2, "xpos", plot1.getPlotX());
		PlotMeSqlManager.updatePlotData(plot2, "zpos", plot1.getPlotZ());
		PlotMeSqlManager.updatePlotData(plot2, "zpos", plot1.getPlotWorld().getId());

		return true;
	}

	public static boolean isPlotWorld(String worldName)
	{
		if (worldName != null)
		{
			return PlotManager.plotWorlds.containsKey(worldName);
		}
		return false;
	}
	
	public static boolean isPlotWorld(World w)
	{
		if (w != null)
		{
			return isPlotWorld(w.getName());
		}
		return false;
	}
	

	
	public static boolean isPlotWorld(Location l)
	{
		if (l != null)
		{
			return isPlotWorld(l.getWorld());
		}
		return false;
	}
	
	public static boolean isPlotWorld(Player p)
	{
		if (p != null)
		{
			return isPlotWorld(p.getWorld());
		}
		return false;
	}
	
	public static boolean isPlotWorld(Block b)
	{
		if (b != null)
		{
			return isPlotWorld(b.getWorld());
		}
		return false;
	}
	
	public static boolean isPlotWorld(BlockState b)
	{
		if  (b != null)
		{
			return isPlotWorld(b.getWorld());
		}
		return false;
	}

	public static boolean isEconomyEnabled(World w)
	{
		if (w != null)
		{
			PlotWorld pwi = plotWorlds.get(w);
			if (pwi != null)
			{
				return pwi.UseEconomy;
			}
		}
		return false;
	}
	

	
	public static boolean isEconomyEnabled(Player p)
	{
		if (p != null)
		{
			return isEconomyEnabled(p.getWorld());
		}
		return false;
	}
	
	public static boolean isEconomyEnabled(Block b)
	{
		if (b != null)
		{
			return isEconomyEnabled(b.getWorld());
		}
		return false;
	}
	
	public static void scanExpirationsExpensive()
	{
		expiredPlots.clear();

		long currentTime = Math.round(System.currentTimeMillis() / 1000);

		Iterator<Plot> expireIterator = allPlots.iterator();
		Plot testplot;
		
		while (expireIterator.hasNext())
		{
			testplot = expireIterator.next();
			if (!testplot.isprotected && testplot.finisheddate <= 0 && testplot.expireddate > 0 && testplot.expireddate <= currentTime)
			{
				PlotManager.expiredPlots.add(testplot);

			}
		}
		
		if (PlotManager.expiredPlots.size() <= 0)
		{
			nextExpiredPlotsCheck = null;
			expiredPlotsCheckTaskId = null;
		}
		else
		{
			Collections.sort(expiredPlots, new ExpiredPlotsComparator());
			testplot = expiredPlots.get(0);
			if (testplot.getExpiration() > 0 && testplot.getExpiration() < PlotManager.nextExpiredPlotsCheck)
			{
				PlotManager.nextExpiredPlotsCheck = testplot.getExpiration();
			}
		}
	}
	

	public static void regen(Plot plot, CommandSender sender)
	{
		if (plot == null || plot.getMinecraftWorld() == null)
		{
			return;
		}
		
		World mw = plot.getMinecraftWorld();
		
		Pair<Location, Location> locations = plot.getWorldMinMaxBlockLocations();
		
		int minChunkX = (int)Math.floor((double)locations.getLeft().getBlockX() / 16);
		int minChunkZ = (int)Math.floor((double)locations.getLeft().getBlockZ() / 16);
		int maxChunkX = (int)Math.ceil((double)locations.getRight().getBlockX() / 16);
		int maxChunkZ = (int)Math.ceil((double)locations.getRight().getBlockZ() / 16);
		
		HashMap<Location, Biome> biomes = new HashMap<Location, Biome>();
		
		for (int cx = minChunkX; cx <= maxChunkX; cx++)
		{
			int xx = cx << 4;
			for (int cz = minChunkZ; cz <= maxChunkZ; cz++)
			{	
				int zz = cz << 4;
				BlockState[][][] blocks = new BlockState[16][16][plot.getMinecraftWorld().getMaxHeight()];
				for (int x = 0; x < 16; x++)
				{
					for (int z = 0; z < 16; z++)
					{
						biomes.put(new Location(mw, x + xx, 0, z + zz), mw.getBiome(x + xx, z + zz));
						for (int y = 0; y < mw.getHighestBlockYAt(x, z); y++)
						{
							Block block = plot.getMinecraftWorld().getBlockAt(x + xx, y, z + zz);
							blocks[x][z][y] = block.getState();
							
							if (PlotMe.usinglwc)
							{
								LWC lwc = com.griefcraft.lwc.LWC.getInstance();
								Material material = block.getType();
								
								boolean ignoreBlockDestruction = Boolean.parseBoolean(lwc.resolveProtectionConfiguration(material, "ignoreBlockDestruction"));
								
								if (!ignoreBlockDestruction)
								{
									Protection protection = lwc.findProtection(block);

									if (protection != null)
									{
										protection.remove();
									}
								}
							}
						}
					}
				}
				
				try
				{
					plot.getMinecraftWorld().regenerateChunk(cx, cz);
		        } catch (Throwable t) {
		        	PlotMe.logger.severe(PlotMe.PREFIX + "ERROR while regenerating chunk at chunk position " + String.valueOf(cx) + "," + String.valueOf(cz) + " :");
		            t.printStackTrace();
		        }
				
				for (int x = 0; x < 16; x++)
				{
					for (int z = 0; z < 16; z++)
					{						
						for (int y = 0; y < plot.getMinecraftWorld().getMaxHeight(); y++)
						{
							if ((x + xx) < locations.getLeft().getX() || (x + xx) > locations.getRight().getX() || (z + zz) < locations.getLeft().getZ() || (z + zz) > locations.getRight().getZ())
							{
								Block newblock = plot.getMinecraftWorld().getBlockAt(x + xx, y, z + zz);
								BlockState oldblock = blocks[x][z][y];
								newblock.setTypeIdAndData(oldblock.getTypeId(), oldblock.getRawData(), false);
								oldblock.update();
							}
						}
					}
				}
			}
		}
		
		for (Location loc : biomes.keySet())
		{
			int x = loc.getBlockX();
			int z = loc.getBlockX();
			plot.getMinecraftWorld().setBiome(x, z, biomes.get(loc));
		}

		refreshPlotChunks(plot);
	}
	
	public static Location getPlotHome(Plot plot)
	{
		Location hl = null;
		
		if (plot != null && isPlotWorld(plot.getMinecraftWorld()))
		{
			hl = plot.getPlotWorld().getCenterLocation(plot).add(0, 3, 0);
		}
		else
		{
			hl = plot.getMinecraftWorld().getSpawnLocation();
		}

		return PlotMe.getAirSpawnPosition(hl);
	}
	
	public static void removeLWCProtections(final Plot plot)
	{
		if (!PlotMe.usinglwc || plot == null || plot.getMinecraftWorld() == null)
		{
			return;
		}

		Pair<Location, Location> locations = plot.getWorldMinMaxBlockLocations();

		final int x1 = locations.getLeft().getBlockX();
		final int y1 = locations.getLeft().getBlockY();
    	final int z1 = locations.getLeft().getBlockZ();
    	final int x2 = locations.getRight().getBlockX();
    	final int y2 = locations.getRight().getBlockY();
    	final int z2 = locations.getRight().getBlockZ();
    	
		Bukkit.getScheduler().runTaskAsynchronously(PlotMe.self, new Runnable() 
		{	
			public void run() 
			{
				LWC lwc = com.griefcraft.lwc.LWC.getInstance();
				List<Protection> protections = lwc.getPhysicalDatabase().loadProtections(plot.getMinecraftWorld().getName(), x1, x2, y1, y2, z1, z2);
				if (protections != null && protections.size()>0)
				{
					Protection tmpp;
					Iterator<Protection> protectionIterator = protections.iterator();
					while (protectionIterator.hasNext())
					{
						tmpp = protectionIterator.next();
						if (tmpp != null)
						{
							tmpp.remove();
						}
					}
				}
			}
		});
	}
}
