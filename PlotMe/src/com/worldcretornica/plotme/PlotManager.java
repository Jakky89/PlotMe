package com.worldcretornica.plotme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitTask;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.worldcretornica.plotme.utils.PlotExpiredComparator;
import com.worldcretornica.plotme.utils.RunnableExpiredPlotsRemover;

public class PlotManager {

    /**
     *  Maps world names to PlotWorld instances 
     */
	public static Map<World, PlotWorld> plotWorlds;
	public static Set<Plot> allPlots;
	public static List<Plot> expiredPlots;
	
	public static Integer expirationCheckTaskId;
	public static Long nextExpirationCheck;
	
	
	public PlotManager()
	{
		plotWorlds = new HashMap<World, PlotWorld>();
		allPlots = new HashSet<Plot>();
		expiredPlots = new LinkedList<Plot>();
		expirationCheckTaskId = null;
		nextExpirationCheck = null;
	}
	
	public static boolean registerPlotWorld(PlotWorld plotWorld)
	{
		if (plotWorld == null || plotWorld.MinecraftWorld == null)
		{
			return false;
		}
		
		if (plotWorlds.put(plotWorld.MinecraftWorld, plotWorld) != plotWorld)
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
		if (plot == null || plot.plotpos == null || plot.plotpos.w == null)
		{
			return;
		}
		plot.plotpos.w.refreshNeighbours(plot);
	}
	
	public static void checkNextExpiration(Plot plot)
	{
		if (plot.getExpiration() > 0)
		{
			if (nextExpirationCheck == null || plot.getExpiration() < nextExpirationCheck)
			{
				if (expirationCheckTaskId != null)
				{
					Bukkit.getScheduler().cancelTask(expirationCheckTaskId);
					expirationCheckTaskId = null;
				}
				nextExpirationCheck = plot.getExpiration();
				Long ticksUntilExpiration = (long)Math.round((plot.getExpiration() * 20) + (System.currentTimeMillis() / 50));
				BukkitTask expireCheckTask = null;
				if (ticksUntilExpiration > 0)
				{
					expireCheckTask = Bukkit.getScheduler().runTaskLaterAsynchronously(PlotMe.self, new RunnableExpiredPlotsRemover(), ticksUntilExpiration);
				}
				else
				{
					expireCheckTask = Bukkit.getScheduler().runTaskAsynchronously(PlotMe.self, new RunnableExpiredPlotsRemover());
				}
				expirationCheckTaskId = expireCheckTask.getTaskId();
			}
		}
	}
	
	public static boolean registerPlot(Plot plot)
	{
		if (plot == null || plot.id <= 0 || plot.plotpos == null || plot.plotpos.w == null || (plot.getExpiration()>0 && plot.getExpiration()<(System.currentTimeMillis()/1000)))
		{
			return false;
		}
		
		PlotWorld pwi = plot.plotpos.w;
		if (pwi == null)
		{
			return false;
		}

		if (pwi.registerPlot(plot))
		{
			setOwnerSign(plot);
			allPlots.add(plot);
			checkNextExpiration(plot);
			return true;
		}
		
		return false;
	}
	
	public static Location getPlotBlockTop(Plot plot)
	{
		double multi = plot.plotpos.w.getPlotBlockPositionMultiplier();
		
		int topX    = (int)Math.ceil(plot.plotpos.x * multi);
		int topZ    = (int)Math.ceil(plot.plotpos.z * multi);

		return new Location(plot.plotpos.w.MinecraftWorld, topX, plot.plotpos.w.MinecraftWorld.getMaxHeight(), topZ);
	}
	
	public static Location getPlotBlockBottom(Plot plot)
	{
		double multi = plot.plotpos.w.getPlotBlockPositionMultiplier();
		
		int bottomX = (int)Math.floor((plot.plotpos.x - 1) * multi);
		int bottomZ = (int)Math.floor((plot.plotpos.z - 1) * multi);
		
		return new Location(plot.plotpos.w.MinecraftWorld, bottomX, 1, bottomZ);
	}

	public static Plot getPlotAtBlockPosition(PlotWorld plotWorld, Location blockLocation)
	{
		if (plotWorld == null || blockLocation == null || !blockLocation.getWorld().equals(plotWorld.MinecraftWorld)) {
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
		if (plotWorld != null && blockLocation != null && blockLocation.getWorld().equals(plotWorld.MinecraftWorld)) {
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
		if (plotWorld != null && blockLocation != null && blockLocation.getWorld().equals(plotWorld.MinecraftWorld)) {
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
			plot.plotpos.w.refreshNeighbours(plot);
		}
		
		if (plot.neighbourplots != null)
		{
			for (int i=0; i<8; i++)
			{
				if (plot.neighbourplots != null)
				{
					if (plot.neighbourplots[i] != null)
					{
						if (plot.neighbourplots[i].owner.playername.equals(plot.owner.playername))
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
		if (!plot1.plotpos.w.equals(plot2.plotpos.w.id))
		{
			return;
		}

		double multi = plot1.plotpos.w.getPlotBlockPositionMultiplier();
		
		int bottomX1 = (int)Math.floor((plot1.plotpos.x - 1) * multi);
		int bottomZ1 = (int)Math.floor((plot1.plotpos.z - 1) * multi);
		int topX1    = (int)Math.ceil(plot1.plotpos.x * multi);
		int topZ1    = (int)Math.ceil(plot1.plotpos.z * multi);
		
		int bottomX2 = (int)Math.floor((plot2.plotpos.x - 1) * multi);
		int bottomZ2 = (int)Math.floor((plot2.plotpos.z - 1) * multi);
		int topX2    = (int)Math.ceil(plot2.plotpos.x * multi);
		int topZ2    = (int)Math.ceil(plot2.plotpos.z * multi);
		
		int minX;
		int maxX;
		int minZ;
		int maxZ;
		boolean isWallX;
			
		if (bottomX1 == bottomX2)
		{
			minX = bottomX1;
			maxX = topX1;
			
			minZ = Math.min(bottomZ1, bottomZ2) + plot1.plotpos.w.PlotSize;
			maxZ = Math.max(topZ1, topZ2) - plot1.plotpos.w.PlotSize;
		}
		else
		{
			minZ = bottomZ1;
			maxZ = topZ1;
			
			minX = Math.min(bottomX1, bottomX2) + plot1.plotpos.w.PlotSize;
			maxX = Math.max(topX1, topX2) - plot1.plotpos.w.PlotSize;
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
		
		int maxY = plot1.plotpos.w.MinecraftWorld.getMaxHeight();

		for (int x = minX; x <= maxX; x++)
		{
			for (int z = minZ; z <= maxZ; z++)
			{
				for (int y = plot1.plotpos.w.RoadHeight; y < maxY; y++)
				{
					if (y >= (plot1.plotpos.w.RoadHeight + 2))
					{
						plot1.plotpos.w.MinecraftWorld.getBlockAt(x, y, z).setType(Material.AIR);
					}
					else if(y == (plot1.plotpos.w.RoadHeight + 1))
					{
						if (isWallX && (x == minX || x == maxX))
						{
							plot1.plotpos.w.MinecraftWorld.getBlockAt(x, y, z).setTypeIdAndData(plot1.plotpos.w.WallBlockId, plot1.plotpos.w.WallBlockValue, true);
						}
						else if(!isWallX && (z == minZ || z == maxZ))
						{
							plot1.plotpos.w.MinecraftWorld.getBlockAt(x, y, z).setTypeIdAndData(plot1.plotpos.w.WallBlockId, plot1.plotpos.w.WallBlockValue, true);
						}
						else
						{
							plot1.plotpos.w.MinecraftWorld.getBlockAt(x, y, z).setType(Material.AIR);
						}
					}
					else
					{
						plot1.plotpos.w.MinecraftWorld.getBlockAt(x, y, z).setTypeIdAndData(plot1.plotpos.w.PlotFloorBlockId, plot1.plotpos.w.PlotFloorBlockValue, true);
					}
				}
			}
		}
	}
	
	private static void fillmiddleroad(Plot plot1, Plot plot2)
	{
		if (!plot1.plotpos.w.equals(plot2.plotpos.w)) {
			return;
		}
		
		double multi = plot1.plotpos.w.getPlotBlockPositionMultiplier();
		
		int bottomX1 = (int)Math.floor((plot1.plotpos.x - 1) * multi);
		int bottomZ1 = (int)Math.floor((plot1.plotpos.z - 1) * multi);
		int topX1    = (int)Math.ceil(plot1.plotpos.x * multi);
		int topZ1    = (int)Math.ceil(plot1.plotpos.z * multi);
		
		int bottomX2 = (int)Math.floor((plot2.plotpos.x - 1) * multi);
		int bottomZ2 = (int)Math.floor((plot2.plotpos.z - 1) * multi);
		int topX2    = (int)Math.ceil(plot2.plotpos.x * multi);
		int topZ2    = (int)Math.ceil(plot2.plotpos.z * multi);

		int minX;
		int maxX;
		int minZ;
		int maxZ;

		minX = Math.min(topX1, topX2);
		maxX = Math.max(bottomX1, bottomX2);
		
		minZ = Math.min(topZ1, topZ2);
		maxZ = Math.max(bottomZ1, bottomZ2);
		
		int maxY = plot1.plotpos.w.MinecraftWorld.getMaxHeight();
				
		for (int x = minX; x <= maxX; x++)
		{
			for (int z = minZ; z <= maxZ; z++)
			{
				for (int y = plot1.plotpos.w.RoadHeight; y < maxY; y++)
				{
					if(y >= (plot1.plotpos.w.RoadHeight + 1))
					{
						plot1.plotpos.w.MinecraftWorld.getBlockAt(x, y, z).setType(Material.AIR);
					}
					else
					{
						plot1.plotpos.w.MinecraftWorld.getBlockAt(x, y, z).setTypeId(plot1.plotpos.w.PlotFloorBlockId);
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

		PlotWorld pwi = plotWorlds.get(plotPosition.w.MinecraftWorld);
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
		PlotWorld pwi = plotWorlds.get(worldName);
		if (pwi == null)
		{
			return false;
		}
			
		PlotPosition plp = new PlotPosition(pwi, plotX, plotZ);
		
		Plot tst = pwi.getPlotAtPlotPosition(plp);
		if (tst == null)
		{
			return true;
		}

		return false;
	}
	
	public static boolean isPlotAvailable(Player player)
	{
		if (player == null)
		{
			return false;
		}
		
		PlotWorld pwi = plotWorlds.get(player.getWorld().getName());
		if (pwi == null)
		{
			return false;
		}
		
		return (pwi.getPlotAtBlockPosition(player.getLocation()) == null);
	}
	
	public static void actualizePlotSignInfoLine(Plot plot)
	{
		double multi = plot.plotpos.w.getPlotBlockPositionMultiplier();
		
		int bottomX = (int)Math.floor((plot.plotpos.x - 1) * multi);
		int bottomZ = (int)Math.floor((plot.plotpos.z - 1) * multi);

		Location pillar = new Location(plot.plotpos.w.MinecraftWorld, bottomX - 1, plot.plotpos.w.RoadHeight + 1, bottomZ - 1);
						
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
		double multi = plot.plotpos.w.getPlotBlockPositionMultiplier();
		
		int bottomX = (int)Math.floor((plot.plotpos.x - 1) * multi);
		int bottomZ = (int)Math.floor((plot.plotpos.z - 1) * multi);

		Location pillar = new Location(plot.plotpos.w.MinecraftWorld, bottomX - 1, plot.plotpos.w.RoadHeight + 1, bottomZ - 1);
						
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
			tmpOwnerCaption = PlotMe.caption("SignOwner") + plot.owner.displayname;
		}
		else
		{
			tmpOwnerCaption = PlotMe.caption("SignOwner") + plot.owner.playername;
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
		removeSellSign(plot);
		
		if (plot.isforsale || plot.isauctionned)
		{
			double multi = plot.plotpos.w.getPlotBlockPositionMultiplier();
			
			int bottomX = (int)Math.floor((plot.plotpos.x - 1) * multi);
			int bottomZ = (int)Math.floor((plot.plotpos.z - 1) * multi);

			Location pillar = new Location(plot.plotpos.w.MinecraftWorld, bottomX - 1, plot.plotpos.w.RoadHeight + 1, bottomZ - 1);
						
			Block bsign = pillar.clone().add(-1, 0, 0).getBlock();
			bsign.setType(Material.AIR);
			bsign.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte)4, false);
			
			Sign sign = (Sign) bsign.getState();

			if (plot.isforsale)
			{
				sign.setLine(0, PlotMe.caption("SignForSale"));
				sign.setLine(1, PlotMe.caption("SignPrice"));
				int tmpPrice = (int)Math.round(plot.customprice * 100);
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
					int tmpAmount = (int)Math.round(highestBid.bidamount * 100);
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
						bname = highestBid.biddisplayname;
					}
					else
					{
						bname = highestBid.bidplayername;
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
		Location bottom = getPlotBlockBottom(plot);
		
		Location pillar = new Location(plot.getMinecraftWorld(), bottom.getX() - 1, plot.getPlotWorld().RoadHeight + 1, bottom.getZ() - 1);
		
		Block bsign = pillar.add(0, 0, -1).getBlock();
		bsign.setType(Material.AIR);
	}
	
	public static void removeSellSign(Plot plot)
	{
		Location bottom = getPlotBlockBottom(plot);
		
		Location pillar = new Location(plot.getMinecraftWorld(), bottom.getX() - 1, plot.getPlotWorld().RoadHeight + 1, bottom.getZ() - 1);
		
		Block bsign = pillar.clone().add(-1, 0, 0).getBlock();
		bsign.setType(Material.AIR);
						
		bsign = pillar.clone().add(-1, 0, 1).getBlock();
		bsign.setType(Material.AIR);
	}
	
	public static void setBiome(Plot plot, Biome bio)
	{
		Location bottom = getPlotBlockBottom(plot);
		Location top    = getPlotBlockTop(plot);
		
		int bottomX = bottom.getBlockX() - 1;
		int bottomZ = bottom.getBlockZ() - 1;
		int topX = top.getBlockX() + 1;
		int topZ = top.getBlockZ() + 1;

		for (int x = bottomX; x <= topX; x++)
		{
			for (int z = bottomZ; z <= topZ; z++)
			{
				plot.getMinecraftWorld().getBlockAt(x, 0, z).setBiome(bio);
			}
		}
		
		if (plot.setBiome(bio))
		{
			refreshPlotChunks(plot);
		}
	}
	
	public static void refreshPlotChunks(Plot plot)
	{
		if (plot.getPlotWorld() == null || plot.getMinecraftWorld() == null)
		{
			return;
		}
		
		Location bottom = getPlotBlockBottom(plot);
		Location top    = getPlotBlockTop(plot);
		
		int minChunkX = (int)Math.floor((double)bottom.getBlockX() / 16);
		int minChunkZ = (int)Math.floor((double)bottom.getBlockZ() / 16);
		int maxChunkX = (int)Math.floor((double)top.getBlockX() / 16);
		int maxChunkZ = (int)Math.floor((double)top.getBlockZ() / 16);
		
		if (plot.getMinecraftWorld() != null)
		{
			for (int x = minChunkX; x <= maxChunkX; x++)
			{
				for (int z = minChunkZ; z <= maxChunkZ; z++)
				{
					plot.getMinecraftWorld().refreshChunk(x, z);
				}
			}
		}
	}
	
	public static void clear(Plot plot)
	{
		clear(plot);
		
		RemoveLWC(plot);
		
		//regen(plot);
	}
	
	public static void clear(Location bottom, Location top)
	{
		if (!bottom.getWorld().equals(top.getWorld()))
		{
			return;
		}
		
		int bottomX = bottom.getBlockX();
		int topX = top.getBlockX();
		int bottomZ = bottom.getBlockZ();
		int topZ = top.getBlockZ();
		
		World w = bottom.getWorld();
		PlotWorld pwi = getPlotWorld(w);
		
		int minChunkX = (int)Math.floor((double)bottomX / 16);
		int maxChunkX = (int)Math.floor((double)topX / 16);
		int minChunkZ = (int)Math.floor((double)bottomZ / 16);
		int maxChunkZ = (int)Math.floor((double)topZ / 16);

		for (int cx = minChunkX; cx <= maxChunkX; cx++)
		{			
			for (int cz = minChunkZ; cz <= maxChunkZ; cz++)
			{			
				Chunk chunk = w.getChunkAt(cx, cz);
				
				for (Entity e : chunk.getEntities())
				{
					Location eloc = e.getLocation();
					
					if (!(e instanceof Player) && eloc.getBlockX() >= bottom.getBlockX() && eloc.getBlockX() <= top.getBlockX() &&
						  eloc.getBlockZ() >= bottom.getBlockZ() && eloc.getBlockZ() <= top.getBlockZ())
					{
						e.remove();
					}
				}
			}
		}

		for (int x = bottomX; x <= topX; x++)
		{
			for (int z = bottomZ; z <= topZ; z++)
			{
				Block block = new Location(w, x, 0, z).getBlock();
				
				block.setBiome(Biome.PLAINS);
				
				for (int y = w.getMaxHeight(); y >= 0; y--)
				{
					block = new Location(w, x, y, z).getBlock();
					
					BlockState state = block.getState();
					
					if(state instanceof InventoryHolder)
					{
						InventoryHolder holder = (InventoryHolder) state;
						holder.getInventory().clear();
					}
					
					
					if (state instanceof Jukebox)
					{
						Jukebox jukebox = (Jukebox) state;
						//Remove once they fix the NullPointerException
						try
						{
							jukebox.setPlaying(Material.AIR);
						}
						catch(Exception e)
						{
							
						}
					}
					
										
					if (y == 0)
						block.setTypeId(pwi.BottomBlockId);
					else if(y < pwi.RoadHeight)
						block.setTypeId(pwi.PlotFillingBlockId);
					else if(y == pwi.RoadHeight)
						block.setTypeId(pwi.PlotFloorBlockId);
					else
					{
						if(y == (pwi.RoadHeight + 1) && 
								(x == bottomX - 1 || 
								 x == topX + 1 ||
								 z == bottomZ - 1 || 
								 z == topZ + 1))
						{
							//block.setTypeId(plot1.plotpos.w.WallBlockId);
						}
						else
						{
							block.setTypeIdAndData(0, (byte) 0, false); //.setType(Material.AIR);
						}
					}
				}
			}
		}
		
		adjustWall(bottom);
	}
	
	public static void removePlot(Plot plot)
	{
		allPlots.remove(plot);
		removeOwnerSign(plot);
		removeSellSign(plot);
	}
		
	public static void adjustWall(Location l)
	{
		World w = l.getWorld();
		PlotWorld pwi = getPlotWorld(w);
		Plot plot = pwi.getPlotAtBlockPosition(l);
		
		List<String> wallids = new ArrayList<String>();
		
		String auctionwallid = pwi.AuctionWallBlockId;
		String forsalewallid = pwi.ForSaleWallBlockId;
		
		if (plot.isprotected)
		{
			wallids.add(pwi.ProtectedWallBlockId);
		}
		if (plot.isauctionned && !wallids.contains(auctionwallid))
		{
			wallids.add(auctionwallid);
		}
		if (plot.isforsale && !wallids.contains(forsalewallid))
		{
			wallids.add(forsalewallid);
		}
		if (wallids.size() == 0){
			wallids.add(String.valueOf(pwi.WallBlockId + ":" + pwi.WallBlockValue));
		}
		
		int ctr = 0;
			
		Location bottom = getPlotBlockBottom(plot);
		Location top = getPlotBlockTop(plot);
		
		int x;
		int z;
		
		String currentblockid;
		Block block;
		
		for (x = bottom.getBlockX() - 1; x < top.getBlockX() + 1; x++)
		{
			z = bottom.getBlockZ() - 1;
			currentblockid = wallids.get(ctr);
			ctr = (ctr == wallids.size()-1)? 0 : ctr + 1;
			block = w.getBlockAt(x, pwi.RoadHeight + 1, z);
			setWall(block, currentblockid);
		}
		
		for (z = bottom.getBlockZ() - 1; z < top.getBlockZ() + 1; z++)
		{
			x = top.getBlockX() + 1;
			currentblockid = wallids.get(ctr);
			ctr = (ctr == wallids.size()-1)? 0 : ctr + 1;
			block = w.getBlockAt(x, pwi.RoadHeight + 1, z);
			setWall(block, currentblockid);
		}
		
		for (x = top.getBlockX() + 1; x > bottom.getBlockX() - 1; x--)
		{
			z = top.getBlockZ() + 1;
			currentblockid = wallids.get(ctr);
			ctr = (ctr == wallids.size()-1)? 0 : ctr + 1;
			block = w.getBlockAt(x, pwi.RoadHeight + 1, z);
			setWall(block, currentblockid);
		}
		
		for (z = top.getBlockZ() + 1; z > bottom.getBlockZ() - 1; z--)
		{
			x = bottom.getBlockX() - 1;
			currentblockid = wallids.get(ctr);
			ctr = (ctr == wallids.size()-1)? 0 : ctr + 1;
			block = w.getBlockAt(x, pwi.RoadHeight + 1, z);
			setWall(block, currentblockid);
		}
	}
	
	
	private static void setWall(Block block, String currentblockid)
	{
		if (block == null || currentblockid == null)
		{
			return;
		}
		
		int blockId;
		byte blockData = 0;
		PlotWorld pwi = plotWorlds.get(block.getWorld());
		
		if (pwi == null)
		{
			return;
		}
		
		if(currentblockid.contains(":"))
		{
			try
			{
				blockId = Integer.parseInt(currentblockid.substring(0, currentblockid.indexOf(":")));
				blockData = Byte.parseByte(currentblockid.substring(currentblockid.indexOf(":") + 1));
			}
			catch(NumberFormatException e)
			{
				blockId = pwi.WallBlockId;
				blockData = pwi.WallBlockValue;
			}
		}
		else
		{
			try
			{
				blockId = Integer.parseInt(currentblockid);
			}
			catch(NumberFormatException e)
			{
				blockId = pwi.WallBlockId;
			}
		}
		
		block.setTypeIdAndData(blockId, blockData, true);
	}
	
	
	public static boolean isBlockInPlot(Plot plot, Location blocklocation)
	{
		if (!plot.getMinecraftWorld().equals(blocklocation.getWorld()))
		{
			return false;
		}
		
		Location bottom = getPlotBlockBottom(plot);
		Location top = getPlotBlockTop(plot);
		
		return blocklocation.getBlockX() >= bottom.getBlockX() && blocklocation.getBlockX() <= top.getBlockX()
				&& blocklocation.getBlockZ() >= bottom.getBlockZ() && blocklocation.getBlockZ() <= top.getBlockZ();
	}
	
	public static boolean movePlot(Plot plotFrom, Plot plotTo)
	{
		if (plotFrom.getMinecraftWorld() == null || plotTo.getMinecraftWorld() == null || !plotFrom.getMinecraftWorld().equals(plotTo.getMinecraftWorld()))
		{
			return false;
		}
		
		Location plot1Bottom = getPlotBlockBottom(plotFrom);
		Location plot2Bottom = getPlotBlockBottom(plotTo);
		Location plot1Top = getPlotBlockTop(plotFrom);
		
		int distanceX = plot1Bottom.getBlockX() - plot2Bottom.getBlockX();
		int distanceZ = plot1Bottom.getBlockZ() - plot2Bottom.getBlockZ();
		
		for (int x = plot1Bottom.getBlockX(); x <= plot1Top.getBlockX(); x++)
		{
			for (int z = plot1Bottom.getBlockZ(); z <= plot1Top.getBlockZ(); z++)
			{
				Block plot1Block = plotFrom.getMinecraftWorld().getBlockAt(new Location(plotFrom.getMinecraftWorld(), x, 0, z));
				Block plot2Block = plotTo.getMinecraftWorld().getBlockAt(new Location(plotTo.getMinecraftWorld(), x - distanceX, 0, z - distanceZ));
				
				String plot1Biome = plot1Block.getBiome().name();
				String plot2Biome = plot2Block.getBiome().name();
				
				plot1Block.setBiome(Biome.valueOf(plot2Biome));
				plot2Block.setBiome(Biome.valueOf(plot1Biome));
				
				for (int y = 0; y < plotTo.getMinecraftWorld().getMaxHeight() ; y++)
				{
					plot1Block = plotFrom.getMinecraftWorld().getBlockAt(new Location(plotFrom.getMinecraftWorld(), x, y, z));
					int plot1Type = plot1Block.getTypeId();
					byte plot1Data = plot1Block.getData();
					
					plot2Block = plotTo.getMinecraftWorld().getBlockAt(new Location(plotTo.getMinecraftWorld(), x - distanceX, y, z - distanceZ));
					int plot2Type = plot2Block.getTypeId();
					byte plot2Data = plot2Block.getData();
					
					//plot1Block.setTypeId(plot2Type);
					plot1Block.setTypeIdAndData(plot2Type, plot2Data, false);
					plot1Block.setData(plot2Data);
					
					//net.minecraft.server.World world = ((org.bukkit.craftbukkit.CraftWorld) w).getHandle();
					//world.setRawTypeIdAndData(plot1Block.getX(), plot1Block.getY(), plot1Block.getZ(), plot2Type, plot2Data);
					
					
					
					//plot2Block.setTypeId(plot1Type);
					plot2Block.setTypeIdAndData(plot1Type, plot1Data, false);
					plot2Block.setData(plot1Data);
					//world.setRawTypeIdAndData(plot2Block.getX(), plot2Block.getY(), plot2Block.getZ(), plot1Type, plot1Data);
				}
			}
			
		}
		
		removeOwnerSign(plotFrom);
		removeSellSign(plotFrom);
		
		PlotMeSqlManager.updatePlotData(plotFrom, "xpos", plotTo.getPlotX());
		PlotMeSqlManager.updatePlotData(plotFrom, "zpos", plotTo.getPlotZ());

		setOwnerSign(plotFrom);
		setSellSign(plotFrom);
		setOwnerSign(plotTo);
		setSellSign(plotTo);
				
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
	
	public static void deleteNextExpired()
	{
		if (expiredPlots == null || expiredPlots.size()<=0)
		{
			return;
		}
		int expireDiff;
		Plot expirePlot;
		Collections.sort(expiredPlots, new PlotExpiredComparator());
		do
		{
			expirePlot = expiredPlots.get(0);
			expireDiff = Math.round(expirePlot.getExpiration() - (System.currentTimeMillis()/1000));
			if (expireDiff<=0)
			{
				removePlot(expirePlot);
				removeOwnerSign(expirePlot);
				removeSellSign(expirePlot);
				expiredPlots.remove(0);
			}
		} while (expireDiff<=0 && expiredPlots.size()>0);
		if (expiredPlots.size()>0)
		{
			nextExpirationCheck = expiredPlots.get(0).getExpiration();
		}
	}

	private static void removePlot(Plot expirePlot) {
		// TODO Auto-generated method stub
		
	}

	public static void regen(Plot plot, CommandSender sender)
	{
		Location bottom = getPlotBlockBottom(plot);
		Location top    = getPlotBlockTop(plot);
		
		int minChunkX = (int) Math.floor((double)bottom.getBlockX() / 16);
		int minChunkZ = (int) Math.floor((double)bottom.getBlockZ() / 16);
		int maxChunkX = (int) Math.floor((double)top.getBlockX() / 16);
		int maxChunkZ = (int) Math.floor((double)top.getBlockZ() / 16);
		
		HashMap<Location, Biome> biomes = new HashMap<Location, Biome>();
		
		for (int cx = minChunkX; cx <= maxChunkX; cx++)
		{
			int xx = cx << 4;
			for (int cz = minChunkZ; cz <= maxChunkZ; cz++)
			{	
				int zz = cz << 4;
				BlockState[][][] blocks = new BlockState[16][16][plot.getMinecraftWorld().getMaxHeight()];
				//Biome[][] biomes = new Biome[16][16];
				for (int x = 0; x < 16; x++)
				{
					for (int z = 0; z < 16; z++)
					{
						biomes.put(new Location(plot.getMinecraftWorld(), x + xx, 0, z + zz), plot.getMinecraftWorld().getBiome(x + xx, z + zz));
						for (int y = 0; y < plot.getMinecraftWorld().getMaxHeight(); y++)
						{
							Block block = plot.getMinecraftWorld().getBlockAt(x + xx, y, z + zz);
							blocks[x][z][y] = block.getState();
							
							if(PlotMe.usinglwc)
							{
								LWC lwc = com.griefcraft.lwc.LWC.getInstance();
								Material material = block.getType();
								
								boolean ignoreBlockDestruction = Boolean.parseBoolean(lwc.resolveProtectionConfiguration(material, "ignoreBlockDestruction"));
								
								if (!ignoreBlockDestruction)
								{
									Protection protection = lwc.findProtection(block);

									if(protection != null)
									{
										protection.remove();
										
										/*if(sender instanceof Player)
										{
											Player p = (Player) sender;
											boolean canAccess = lwc.canAccessProtection(p, protection);
									        boolean canAdmin = lwc.canAdminProtection(p, protection);
											
											try 
											{
									            LWCProtectionDestroyEvent evt = new LWCProtectionDestroyEvent(p, protection, LWCProtectionDestroyEvent.Method.BLOCK_DESTRUCTION, canAccess, canAdmin);
									            lwc.getModuleLoader().dispatchEvent(evt);
									        } 
											catch (Exception e) 
									        {
									            lwc.sendLocale(p, "protection.internalerror", "id", "BLOCK_BREAK");
									            e.printStackTrace();
									        }
										}*/
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
		            t.printStackTrace();
		        }
				
				for (int x = 0; x < 16; x++)
				{
					for (int z = 0; z < 16; z++)
					{						
						for (int y = 0; y < plot.getMinecraftWorld().getMaxHeight(); y++)
						{
							if ((x + xx) < bottom.getX() || (x + xx) > top.getX() || (z + zz) < bottom.getZ() || (z + zz) > top.getZ())
							{
								Block newblock = plot.getMinecraftWorld().getBlockAt(x + xx, y, z + zz);
								BlockState oldblock = blocks[x][z][y];
								
								newblock.setTypeIdAndData(oldblock.getTypeId(), oldblock.getRawData(), false);
								oldblock.update();
								
								//blocks[x][z][y].update(true);
							}
						}
					}
				}
			}
		}
		
		for(Location loc : biomes.keySet())
		{
			int x = loc.getBlockX();
			int z = loc.getBlockX();
			
			plot.getMinecraftWorld().setBiome(x, z, biomes.get(loc));
		}
		
		//refreshPlotChunks(w, plot);
	}
	
	public static Location getPlotHome(Plot plot)
	{
		if (plot.getMinecraftWorld() != null)
		{
			Location hl = plot.getPlotWorld().getCenterLocation(plot);
			if (hl != null)
			{
				return hl;
			}
		}
		return plot.getMinecraftWorld().getSpawnLocation();
	}
	
	public static void RemoveLWC(final Plot plot)
	{
		if (PlotMe.usinglwc)
		{
			Location bottom = getPlotBlockBottom(plot);
			Location top    = getPlotBlockTop(plot);
			final int x1 = bottom.getBlockX();
			final int y1 = bottom.getBlockY();
	    	final int z1 = bottom.getBlockZ();
	    	final int x2 = top.getBlockX();
	    	final int y2 = top.getBlockY();
	    	final int z2 = top.getBlockZ();
	    	
			Bukkit.getScheduler().runTaskAsynchronously(PlotMe.self, new Runnable() 
			{	
				public void run() 
				{
					LWC lwc = com.griefcraft.lwc.LWC.getInstance();
					List<Protection> protections = lwc.getPhysicalDatabase().loadProtections(plot.plotpos.w.MinecraftWorld.getName(), x1, x2, y1, y2, z1, z2);

					for (Protection protection : protections) {
					    protection.remove();
					}
				}
			});
	    }
	}
}
