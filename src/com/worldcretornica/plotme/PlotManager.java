package com.worldcretornica.plotme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.worldcretornica.plotme.utils.ExpiredPlotsComparator;
import com.worldcretornica.plotme.utils.Jakky89ItemIdData;
import com.worldcretornica.plotme.utils.Pair;


public class PlotManager {

    // Maps bukkits world names to PlotWorld instances
	public static Map<String, PlotWorld> plotWorlds;
	public static Map<String, PlotPlayer> plotPlayers;
	public static PlotPlayer npcBank;
	public static PlotPlayer npcServer;
	// Maps player names to PlotMePlayer instances 
	public static List<Plot> allPlots;

	
	static {
		plotWorlds = new HashMap<String, PlotWorld>();
		plotPlayers = new HashMap<String, PlotPlayer>();
		
		npcServer = new PlotPlayer(-1, "SERVER");
		npcBank = new PlotPlayer(-2, "BANK");
	
		allPlots = new ArrayList<Plot>();
	}

	
	public static void registerPlotWorld(PlotWorld plotWorld)
	{
		if (plotWorld == null || plotWorld.getName().isEmpty()) {
			PlotMe.logger.warning(PlotMe.PREFIX + "Could not register PlotWorld with empty name!");
			return;
		}
		if (plotWorlds.containsKey(plotWorld.getName()))
		{
			PlotMe.logger.warning(PlotMe.PREFIX + " PlotWorld " + plotWorld.getName() + " already is registered!");
			return;
		}
		plotWorlds.put(plotWorld.getName(), plotWorld);
		if (PlotMe.advancedLogging)
		{
			PlotMe.logger.info(PlotMe.PREFIX + "Registered PlotWorld \"" + plotWorld.getName() + "\" with id " + String.valueOf(plotWorld.getId()) + ".");
		}
	}
	
	public static PlotPlayer registerPlotPlayer(String playerName)
	{
		if (playerName == null || playerName.isEmpty()) {
			PlotMe.logger.warning(PlotMe.PREFIX + "Could not register PlotPlayer with empty name!");
			return null;
		}
		if (plotPlayers.containsKey(playerName))
		{
			PlotMe.logger.warning(PlotMe.PREFIX + " PlotPlayer already is registered!");
			return null;
		}
		PlotPlayer ppres = PlotDatabase.getPlotPlayer(playerName);
		if (ppres != null)
		{
			plotPlayers.put(playerName, ppres);
			if (PlotMe.advancedLogging)
			{
				PlotMe.logger.info(PlotMe.PREFIX + "Registered PlotPlayer \"" + ppres.getName() + "\" with id " + String.valueOf(ppres.getId()) + ".");
			}
			return ppres;
		}
		else
		{
			PlotMe.logger.warning(PlotMe.PREFIX + "Could not load PlotPlayer \"" + playerName + "\" from database!");
		}
		return null;
	}
	
	public static void registerPlotPlayer(Player player)
	{
		if (player == null || player.getName().isEmpty())
		{
			return;
		}
		
		if (!plotPlayers.containsKey(player.getName()))
		{
			PlotPlayer ppi = registerPlotPlayer(player.getName());
			ppi.setDisplayName(player.getDisplayName());
			ppi.setMinecraftPlayer(player);
		}
	}
	
	public static void unregisterPlotPlayer(PlotPlayer plotPlayer)
	{
		if (plotPlayer == null || plotPlayer.getName().isEmpty())
		{
			PlotMe.logger.warning(PlotMe.PREFIX + "Could not unregister PlotPlayer with empty name!");
			return;
		}
		plotPlayers.remove(plotPlayer.getName());
		if (PlotMe.advancedLogging)
		{
			PlotMe.logger.info(PlotMe.PREFIX + "Unregistered PlotPlayer \"" + plotPlayer.getName() + "\" with id " + String.valueOf(plotPlayer.getId()) + ".");
		}
	}
	
	public static void unregisterPlotPlayer(String playerName)
	{
		if (playerName == null)
		{
			return;
		}
		plotPlayers.remove(playerName);
	}
	
	public static void unregisterPlotPlayer(Player bukkitPlayer)
	{
		if (bukkitPlayer == null)
		{
			return;
		}
		plotPlayers.remove(bukkitPlayer.getName());
	}

	public static PlotWorld getPlotWorld(String worldName)
	{
		if (worldName == null || worldName.isEmpty())
		{
			return null;
		}

		PlotWorld tmppwi = plotWorlds.get(worldName);
		if (tmppwi == null)
		{
			tmppwi = PlotDatabase.getPlotWorld(worldName);
			if (tmppwi != null)
			{
				registerPlotWorld(tmppwi);
			}
		}

		return tmppwi;
	}
	
	public static PlotWorld getPlotWorld(int plotWorldId)
	{
		if (plotWorldId < 1)
		{
			return null;
		}

		Iterator<PlotWorld> plotWorldsIterator = plotWorlds.values().iterator();
		PlotWorld testpwi;
		
		while (plotWorldsIterator.hasNext())
		{
			testpwi = plotWorldsIterator.next();
			if (testpwi!=null && testpwi.getId()==plotWorldId)
			{
				return testpwi;
			}
		}
		
		return null;
	}
	
	public static void loadChunkPlots(Chunk chunk)
	{
		if (chunk.getWorld() == null)
		{
			return;
		}
		
		PlotWorld pwi = getPlotWorld(chunk.getWorld().getName());
		if (pwi != null)
		{
			PlotDatabase.getPlots(pwi, chunk.getX()-8, chunk.getZ()-8, chunk.getX()+8, chunk.getZ()+8);
		}
	}
	
	public static void loadActivePlots(World minecraftWorld)
	{
		Chunk[] chunks = minecraftWorld.getLoadedChunks();
		if (chunks != null && chunks.length > 0)
		{
			int minCX = chunks[0].getX();
			int maxCX = minCX + 1;
			int minCZ = chunks[0].getZ();
			int maxCZ = minCZ + 1;
			for (Chunk chunk : chunks)
			{
				if (chunk.getWorld().getName().equals(minecraftWorld.getName()))
				{
					if (chunk.getX() < minCX) {
						minCX = chunk.getX();
					} else if (chunk.getX() > maxCX) {
						maxCX = chunk.getX();
					}
					if (chunk.getZ() < minCZ) {
						minCZ = chunk.getZ();
					} else if (chunk.getZ() > maxCZ) {
						maxCZ = chunk.getZ();
					}
				}
			}
			PlotDatabase.getPlots(getPlotWorld(minecraftWorld.getName()), minCX, maxCX, minCZ, maxCZ);
		}
	}
	
	public static PlotPlayer getPlotPlayer(Player player)
	{
		if (player != null)
		{
			PlotPlayer ppi = plotPlayers.get(player.getName());
			if (ppi == null)
			{
				ppi = PlotDatabase.getPlotPlayer(player);
				plotPlayers.put(player.getName(), ppi);
			}
			if (ppi != null)
			{
				ppi.setDisplayName(player.getDisplayName());
				return ppi;
			}
		}
		return null;
	}
	
	public static PlotPlayer getPlotPlayer(String playerName)
	{
		if (playerName != null && !playerName.isEmpty())
		{
			PlotPlayer tmpppi = plotPlayers.get(playerName);
			if (tmpppi != null)
			{
				Player bukkitPlayer = Bukkit.getPlayerExact(tmpppi.getName());
				if (bukkitPlayer != null)
				{
					tmpppi.setDisplayName(bukkitPlayer.getDisplayName());
					plotPlayers.put(playerName, tmpppi);
				}
				return tmpppi;
			}
		}
		return null;
	}
	
	public static PlotPlayer getPlotPlayer(int plotPlayerId)
	{
		if (plotPlayerId < 0)
		{
			return null;
		}
		Iterator<PlotPlayer> plotPlayersIterator = plotPlayers.values().iterator();
		PlotPlayer testppi;
		while (plotPlayersIterator.hasNext())
		{
			testppi = plotPlayersIterator.next();
			if (testppi.getId() == plotPlayerId)
			{
				return testppi;
			}
		}
		
		return null;
	}

	public static List<Player> getPlayersInPlot(Plot plot)
	{
		if (plot == null)
		{
			return null;
		}
		
		List<Player> tmplist = new ArrayList<Player>();
		
		Plot testplot;
		Player[] onlineplayers = Bukkit.getOnlinePlayers();
		if (onlineplayers.length > 0)
		{
			for (Player pl : onlineplayers)
			{
				testplot = getPlotAtLocation(pl.getLocation());
				if (testplot != null && testplot.equals(plot))
				{
					tmplist.add(pl);
				}
			}
			
			return tmplist;
		}
		
		return null;
	}
	
	/*
	 * TODO
	 */
	public static int getPlayerPlotCount(PlotPlayer plotPlayer, PlotWorld plotWorld) {
		return 0;
	}
	
	public static void resetNeighbours(Plot plot)
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
		plot.getPlotWorld().notifyNeighbours(plot);
	}

	public static int getPlotIndex(int plotId)
	{
		if (plotId < 1)
		{
			return -1;
		}
		
        int low = 0;
        int high = allPlots.size() - 1;
        int mid;

        while ( low <= high )
        {
            mid = ( low + high ) / 2;
            if ( allPlots.get( mid ).getId() < plotId )
            {
                low = mid + 1;
            }
            else if( allPlots.get( mid ).getId() > plotId )
            {
                high = mid - 1;
            }
            else
            {
                return mid;
            }
        }
        
        return -1;
	}

	public static Plot getPlot(int plotId)
	{
		int plotIndex = getPlotIndex(plotId);
		
		if (plotIndex >= 0)
		{
			return allPlots.get(plotIndex);
		}

        return null;
	}
	
	public static void registerPlot(Plot plot)
	{
		if (plot == null || plot.getId() <= 0 || plot.getPlotPosition() == null || plot.getPlotWorld() == null || (plot.getExpireDate() != null && plot.getExpireDate() > 0 && plot.getExpireDate() < (System.currentTimeMillis()/1000)))
		{
			return;
		}
		
		PlotWorld pwi = plot.getPlotWorld();
		if (pwi == null)
		{
			return;
		}

		if (getPlotIndex(plot.getId()) >= 0)
		{
			return;
		}
		
		if (pwi.registerPlot(plot))
		{
			allPlots.add(plot);
			Collections.sort(allPlots);
			actualizePlotSigns(plot);
		}

	}
	
	public static Plot getPlotAtPlotPosition(PlotPosition plotPosition)
	{
		if (plotPosition == null) {
			return null;
		}
		return plotPosition.getPlotWorld().getPlotAtPlotPosition(plotPosition);
	}

	public static Plot getPlotAtLocation(Location loc)
	{
		if (loc == null || loc.getWorld() == null)
		{
			return null;
		}

		PlotWorld plotWorld = plotWorlds.get(loc.getWorld());
		if (plotWorld != null) {
			return plotWorld.getPlotAtLocation(loc);
		}
		
		return null;
	}

	public static void adjustLinkedPlots(Plot plot)
	{
		if (plot == null)
		{
			return;
		}

		plot.getPlotWorld().notifyNeighbours(plot);
		
		for (byte i=0; i<8; i++)
		{
			if (plot.getNeighbourPlot(i) != null)
			{
				if (plot.getNeighbourPlot(i).getOwner().equals(plot.getOwner()))
				{
					fillroad(plot.getNeighbourPlot(i), plot);
				}
			}
		}
		
		if (plot.getNeighbourPlot((byte)1) != null && plot.getNeighbourPlot((byte)1).getOwner().equals(plot.getOwner()))
		{
			if (plot.getNeighbourPlot((byte)7) != null &&
				plot.getNeighbourPlot((byte)0) != null)
			{
				if (plot.getNeighbourPlot((byte)7).getOwner().equals(plot.getOwner()) &&
					plot.getNeighbourPlot((byte)0).getOwner().equals(plot.getOwner()))
				{
					fillmiddleroad(plot.getNeighbourPlot((byte)0), plot);
				}
			}
			
			if (plot.getNeighbourPlot((byte)2) != null &&
				plot.getNeighbourPlot((byte)3) != null)
			{
				if (plot.getNeighbourPlot((byte)2).getOwner().equals(plot.getOwner()) &&
					plot.getNeighbourPlot((byte)3).getOwner().equals(plot.getOwner()))
				{
					fillmiddleroad(plot.getNeighbourPlot((byte)2), plot);
				}
			}
		}
		if (plot.getNeighbourPlot((byte)5) != null && plot.getNeighbourPlot((byte)5).getOwner().equals(plot.getOwner()))
		{
			if (plot.getNeighbourPlot((byte)3) != null &&
				plot.getNeighbourPlot((byte)4) != null)
			{
				if (plot.getNeighbourPlot((byte)3).getOwner().equals(plot.getOwner()) &&
					plot.getNeighbourPlot((byte)4).getOwner().equals(plot.getOwner()))
				{
					fillmiddleroad(plot.getNeighbourPlot((byte)4), plot);
				}
			}
			
			if (plot.getNeighbourPlot((byte)6) != null &&
				plot.getNeighbourPlot((byte)7) != null)
			{
				if (plot.getNeighbourPlot((byte)6).getOwner().equals(plot.getOwner()) &&
					plot.getNeighbourPlot((byte)7).getOwner().equals(plot.getOwner()))
				{
					fillmiddleroad(plot.getNeighbourPlot((byte)6), plot);
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
							plot1.getMinecraftWorld().getBlockAt(x, y, z).setTypeIdAndData(plot1.getPlotWorld().WallBlock.getTypeId(), (byte)plot1.getPlotWorld().WallBlock.getDataValue(), true);
						}
						else if(!isWallX && (z == minZ || z == maxZ))
						{
							plot1.getMinecraftWorld().getBlockAt(x, y, z).setTypeIdAndData(plot1.getPlotWorld().WallBlock.getTypeId(), (byte)plot1.getPlotWorld().WallBlock.getDataValue(), true);
						}
						else
						{
							plot1.getMinecraftWorld().getBlockAt(x, y, z).setType(Material.AIR);
						}
					}
					else
					{
						plot1.getMinecraftWorld().getBlockAt(x, y, z).setTypeIdAndData(plot1.getPlotWorld().PlotFloorBlock.getTypeId(), (byte)plot1.getPlotWorld().PlotFloorBlock.getDataValue(), true);
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
						plot1.getMinecraftWorld().getBlockAt(x, y, z).setTypeId(plot1.getPlotWorld().PlotFloorBlock.getTypeId());
					}
				}
			}
		}
	}
	
	public static boolean isPlotAvailable(PlotWorld plotWorld, int X, int Z)
	{
		if (plotWorld == null)
		{
			return false;
		}
		
		Plot plp = plotWorld.getPlotAtPlotPosition(X, Z);
		if (plp != null && plp.isAvailable())
		{
			return true;
		}

		return false;
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
		if (plp != null && plp.isAvailable())
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
		if (plp != null && plp.isAvailable())
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
		
		return (pwi.getPlotAtLocation(player.getLocation()) == null);
	}
	
	public static void actualizePlotSigns(Plot plot)
	{
		if (plot == null || plot.getMinecraftWorld() == null)
		{
			return;
		}
		
		Sign infosign = null;
		Sign auctionsign = null;
		Sign sellsign = null;

		Location baseLocation = plot.getWorldMinBlockLocation();
		
		Location pillar = new Location(plot.getMinecraftWorld(), baseLocation.getBlockX() - 1, plot.getPlotWorld().RoadHeight + 1, baseLocation.getBlockZ() - 1);
		
		Block binfo = pillar.clone().add(-1, 0, 0).getBlock();
		if (binfo != null && (binfo instanceof Sign))
		{
			infosign = (Sign)binfo.getState();
		}
		
		Block bauction = pillar.clone().add(-1, 0, -1).getBlock();
		if (bauction != null && (bauction instanceof Sign))
		{
			auctionsign = (Sign)bauction.getState();
		}
		
		Block bsell = pillar.clone().add(0, 0, -1).getBlock();
		if (bsell != null && (bsell instanceof Sign))
		{
			sellsign = (Sign)bsell.getState();
		}
		
		long currentTime = Math.round(System.currentTimeMillis()/1000);


		if (binfo != null)
		{
			if (infosign == null)
			{
				binfo.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte)2, false);
				infosign = (Sign)binfo.getState();
			}
			if (infosign != null)
			{
				// Always show plot id info in the first line
				infosign.setLine(0, PlotMe.caption("SignId") + String.valueOf(plot.getId()));
	
				// Check if someone owns this plot
				if (plot.getOwner() != null)
				{
					String tmpOwnerCaption;
					if (PlotMe.useDisplayNamesOnSigns)
					{
						tmpOwnerCaption = PlotMe.caption("SignOwner") + plot.getOwner().getDisplayName();
					}
					else
					{
						tmpOwnerCaption = PlotMe.caption("SignOwner") + plot.getOwner().getName();
					}
					if (tmpOwnerCaption.length() > 16)
					{
						infosign.setLine(1, tmpOwnerCaption.substring(0, 16));
						if (tmpOwnerCaption.length() > 32)
						{
							infosign.setLine(2, tmpOwnerCaption.substring(16, 32));
						}
						else
						{
							infosign.setLine(2, tmpOwnerCaption.substring(16));
						}
					}
					else
					{
						infosign.setLine(2, tmpOwnerCaption);
					}
				}
				if (plot.isFinished())
				{
					infosign.setLine(3, PlotMe.caption("InfoFinished"));
				}
				else
				{
					if (plot.getExpireDate() > 0)
					{
						int secsRemain = Math.round(plot.getExpireDate() - currentTime);
						if (secsRemain > 0)
						{
							if (secsRemain < 2592000)
							{
								if (secsRemain > 604800)
								{
									infosign.setLine(3, PlotMe.caption("InfoExpire") + " +" + String.valueOf(Math.floor(secsRemain / 604800)) + "w");
								}
								else if (secsRemain > 86400)
								{
									infosign.setLine(3, PlotMe.caption("InfoExpire") + " +" + String.valueOf(Math.floor(secsRemain / 86400)) + "d");
								}
								else if (secsRemain > 3600)
								{
									infosign.setLine(3, PlotMe.caption("InfoExpire") + " +" + String.valueOf(Math.floor(secsRemain / 3600)) + "h");
								}
							}
						}
					}
				}
				infosign.update(true);
			}
		}
		
		if (bsell != null)
		{
			if (plot.isForSale())
			{
				if (sellsign == null)
				{
					bsell.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte)2, false);
					sellsign = (Sign)bsell.getState();
				}
				if (sellsign != null)
				{
					sellsign.setLine(0, PlotMe.caption("SignForSale"));
					int tmpPrice = (int)Math.round(plot.getClaimPrice() * 100);
					sellsign.setLine(1, PlotMe.caption("SignPrice"));
					sellsign.setLine(2, PlotMe.caption("SignPriceColor") + String.valueOf(tmpPrice / 100));
					sellsign.setLine(3, "/plot " + PlotMe.caption("CommandBuy"));
					sellsign.update(true);
				}
			}
			else if (bsell.getType().equals(Material.WALL_SIGN))
			{
				bsell.setType(Material.AIR);
			}
		}

		if (bauction != null)
		{
			if (plot.isAuctioned())
			{
				if (auctionsign == null)
				{
					bauction.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte)4, false);
					auctionsign = (Sign)bauction.getState();
				}
				if (auctionsign != null)
				{
					auctionsign.setLine(0, PlotMe.caption("SignOnAuction"));
					PlotAuctionBid highestBid = plot.getAuctionBid(0);
					if (highestBid != null)
					{
						auctionsign.setLine(1, PlotMe.caption("SignCurrentBid"));
						int tmpAmount = (int)Math.round(highestBid.getMoneyAmount() * 100);
						String tmpAuctionLine = PlotMe.caption("SignCurrentBidColor") + Math.round(tmpAmount / 100);
						if (tmpAuctionLine.length() < 16)
						{
							auctionsign.setLine(2, tmpAuctionLine);
						}
						else
						{
							auctionsign.setLine(2, ">" + tmpAuctionLine.substring(0, 14));
						}
					}
					else
					{
						auctionsign.setLine(1, PlotMe.caption("SignMinimumBid"));
					}
					auctionsign.setLine(3, "/plot " + PlotMe.caption("CommandBid") + " <x>");
				}
			}
			else if (bsell.getType().equals(Material.WALL_SIGN))
			{
				bauction.setType(Material.AIR);
			}
		}
	}
	
	public static PlotPlayer getOnlinePlotOwner(Player bukkitPlayer)
	{
		if (bukkitPlayer == null)
		{
			return null;
		}
		return plotPlayers.get(bukkitPlayer.getName());
	}

	public static void removePlotSigns(Plot plot)
	{
		if (plot == null || plot.getMinecraftWorld() == null)
		{
			return;
		}
		
		Location baseLocation = plot.getWorldMinBlockLocation();
		
		Location pillar = new Location(plot.getMinecraftWorld(), baseLocation.getBlockX() - 1, plot.getPlotWorld().RoadHeight + 1, baseLocation.getBlockZ() - 1);
		
		Block binfo = pillar.clone().add(-1, 0, 0).getBlock();
		if (binfo != null && (binfo instanceof Sign))
		{
			binfo.setType(Material.AIR);
		}
		
		Block bauction = pillar.clone().add(-1, 0, -1).getBlock();
		if (bauction != null && (bauction instanceof Sign))
		{
			bauction.setType(Material.AIR);
		}
		
		Block bsell = pillar.clone().add(0, 0, -1).getBlock();
		if (bsell != null && (bsell instanceof Sign))
		{
			bsell.setType(Material.AIR);
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
		plot.setBiome(bio);
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

	public static void clear(Location loc1, Location loc2)
	{
		if (loc1 == null || loc2 == null || loc1.getWorld() == null || loc2.getWorld() == null || !loc1.getWorld().equals(loc2.getWorld()))
		{
			return;
		}
		
		World mw = loc1.getWorld();
		PlotWorld pwi = getPlotWorld(mw.getName());
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
						block.setTypeIdAndData(pwi.BottomBlock.getTypeId(), (byte)pwi.BottomBlock.getDataValue(), false);
					}
					else if (y < pwi.RoadHeight)
					{
						block.setTypeIdAndData(pwi.PlotFillingBlock.getTypeId(), (byte)pwi.PlotFillingBlock.getDataValue(), false);
					}
					else if (y == pwi.RoadHeight)
					{
						block.setTypeIdAndData(pwi.PlotFloorBlock.getTypeId(), (byte)pwi.PlotFloorBlock.getDataValue(), false);
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
		removePlotSigns(plot);
		clear(locations.getLeft(), locations.getRight());
		
		removeLWCProtections(plot);

		//regen(plot);
	}
	
	public static void removePlot(Plot plot)
	{
		clearPlot(plot);
		PlotDatabase.removePlot(plot);

		int plotIndex = getPlotIndex(plot.getId());
		if (plotIndex >= 0)
		{
			allPlots.remove( plotIndex );
		}
	}
	
	public static void adjustWall(Plot plot)
	{
		if (plot == null)
		{
			return;
		}
		
		List<Jakky89ItemIdData> wallids = new ArrayList<Jakky89ItemIdData>();

		if (plot.isProtected())
		{
			wallids.add(plot.getPlotWorld().ProtectedWallBlock);
		}
		if (plot.isAuctioned())
		{
			wallids.add(plot.getPlotWorld().AuctionWallBlock);
		}
		if (plot.isForSale())
		{
			wallids.add(plot.getPlotWorld().ForSaleWallBlock);
		}

		if (wallids.size() == 0){
			wallids.add(plot.getPlotWorld().WallBlock);
		}
		
		int ctr = 0;
			
		Pair<Location, Location> locations = plot.getWorldMinMaxBlockLocations();
		
		int x;
		int z;
		
		Jakky89ItemIdData currentblockid;
		Block block;
		
		for (x = locations.getLeft().getBlockX() - 1; x < locations.getRight().getBlockX() + 1; x++)
		{
			z = locations.getLeft().getBlockZ() - 1;
			currentblockid = wallids.get(ctr);
			ctr = (ctr == wallids.size()-1)? 0 : ctr + 1;
			block = plot.getPlotWorld().getMinecraftWorld().getBlockAt(x, plot.getPlotWorld().RoadHeight + 1, z);
			setWall(block, currentblockid);
		}
		
		for (z = locations.getLeft().getBlockZ() - 1; z < locations.getRight().getBlockZ() + 1; z++)
		{
			x = locations.getRight().getBlockX() + 1;
			currentblockid = wallids.get(ctr);
			ctr = (ctr == wallids.size()-1)? 0 : ctr + 1;
			block = plot.getPlotWorld().getMinecraftWorld().getBlockAt(x, plot.getPlotWorld().RoadHeight + 1, z);
			setWall(block, currentblockid);
		}
		
		for (x = locations.getRight().getBlockX() + 1; x > locations.getLeft().getBlockX() - 1; x--)
		{
			z = locations.getRight().getBlockZ() + 1;
			currentblockid = wallids.get(ctr);
			ctr = (ctr == wallids.size()-1)? 0 : ctr + 1;
			block = plot.getPlotWorld().getMinecraftWorld().getBlockAt(x, plot.getPlotWorld().RoadHeight + 1, z);
			setWall(block, currentblockid);
		}
		
		for (z = locations.getRight().getBlockZ() + 1; z > locations.getLeft().getBlockZ() - 1; z--)
		{
			x = locations.getLeft().getBlockX() - 1;
			currentblockid = wallids.get(ctr);
			ctr = (ctr == wallids.size()-1)? 0 : ctr + 1;
			block = plot.getPlotWorld().getMinecraftWorld().getBlockAt(x, plot.getPlotWorld().RoadHeight + 1, z);
			setWall(block, currentblockid);
		}
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

		Plot plot = pwi.getPlotAtLocation(loc);
		
		if (plot != null)
		{
			adjustWall(plot);
		}
	}
	
	
	private static void setWall(Block block, Jakky89ItemIdData blockIdData)
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
			block.setTypeIdAndData(blockIdData.getTypeId(), (byte)blockIdData.getDataValue(), true);
			return;
		}
		block.setTypeIdAndData(pwi.WallBlock.getTypeId(), (byte)pwi.WallBlock.getDataValue(), true);
	}
	
	
	public static boolean isBlockInPlot(Plot plot, Location blockLocation)
	{
		if (!plot.getMinecraftWorld().equals(blockLocation.getWorld()))
		{
			return false;
		}
		
		Location baseLocation = plot.getPlotWorld().getMinBlockLocation(plot);
		
		if (blockLocation.getBlockX() >= baseLocation.getBlockX() && blockLocation.getBlockX() <= baseLocation.getBlockX() + plot.getPlotSize()
		 && blockLocation.getBlockZ() >= baseLocation.getBlockZ() && blockLocation.getBlockZ() <= baseLocation.getBlockZ() + plot.getPlotSize())
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
	
	/**
	 * TODO: move lwc protections
	 */
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
		
		removePlotSigns(plot);
		
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
		
		Location baseTo   = new Location(mwiTo, Math.floor(targetPlotPosition.getPlotX() * multiTo + (pwiTo.PathWidth / 2)), 0, Math.floor(targetPlotPosition.getPlotZ() * multiTo + (pwiTo.PathWidth / 2)));
		
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
						sourceBlock.setTypeIdAndData(pwiFrom.BottomBlock.getTypeId(), (byte)pwiFrom.BottomBlock.getDataValue(), false);
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
	
	/**
	 * TODO: move lwc protections
	 */
	public static boolean movePlots(Plot plot1, Plot plot2)
	{
		if (plot1 == null || plot2 == null || plot1.getMinecraftWorld() == null || plot2.getMinecraftWorld() == null)
		{
			return false;
		}
		
		PlotWorld plotWorld1 = plot1.getPlotWorld();
		PlotWorld plotWorld2 = plot2.getPlotWorld();
				
		// Remove signs
		removePlotSigns(plot1);
		removePlotSigns(plot2);
		
		LWC lwc = com.griefcraft.lwc.LWC.getInstance();
		
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
		
		Protection lwcprotection1;
		Protection lwcprotection2;
		
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

		for (x = 0; x < plotSize; x++)
		{
			for (z = 0; z < plotSize; z++)
			{
				inner2 : for (y = 0; y < maxY; y++)
				{
					if (rhd != 0)
					{
						if (y + Math.abs(rhd) >= maxY1 || y + Math.abs(rhd) >= maxY2 || y - Math.abs(rhd) < 0)
						{
							break inner2;
						}
					}

					// Get block data copies
					
					// Plot 1
					block1 = minecraftWorld1.getBlockAt(baseLocation1.getBlockX() + x, y, baseLocation1.getBlockZ() + z);
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
						block1.setTypeIdAndData(plotWorld1.BottomBlock.getTypeId(), (byte)plotWorld1.BottomBlock.getDataValue(), false);
					}
					lwcprotection1 = lwc.findProtection(block1);
					
					// Plot 2
					block2 = minecraftWorld2.getBlockAt(baseLocation2.getBlockX() + x, y, baseLocation2.getBlockZ() + z);
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
						block2.setTypeIdAndData(plotWorld2.BottomBlock.getTypeId(), (byte)plotWorld2.BottomBlock.getDataValue(), false);
					}
					lwcprotection2 = lwc.findProtection(block2);

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

		PlotDatabase.updatePlotPosition(plot1);
		PlotDatabase.updatePlotPosition(plot2);

		return true;
	}

	public static boolean isPlotWorld(String worldName)
	{
		if (worldName != null)
		{
			if (plotWorlds.containsKey(worldName))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean isPlotWorld(World bukkitWorld)
	{
		if (bukkitWorld != null)
		{
			return isPlotWorld(bukkitWorld.getName());
		}
		return false;
	}
	
	public static boolean isPlotWorld(PlotWorld plotWorld)
	{
		if (plotWorld != null)
		{
			return isPlotWorld(plotWorld.getName());
		}
		return false;
	}
	
	public static boolean isPlotWorld(Location location)
	{
		if (location != null)
		{
			return isPlotWorld(location.getWorld());
		}
		return false;
	}
	
	public static boolean isPlotWorld(Player player)
	{
		if (player != null)
		{
			return isPlotWorld(player.getWorld());
		}
		return false;
	}
	
	public static boolean isPlotWorld(Block block)
	{
		if (block != null)
		{
			return isPlotWorld(block.getWorld());
		}
		return false;
	}
	
	public static boolean isPlotWorld(BlockState blockState)
	{
		if  (blockState != null)
		{
			return isPlotWorld(blockState.getWorld());
		}
		return false;
	}
	
	public static boolean isEconomyEnabled(PlotWorld plotWorld)
	{
		if (plotWorld != null)
		{
			if (isPlotWorld(plotWorld.getMinecraftWorld()))
			{
				PlotWorld pwi = plotWorlds.get(plotWorld.getMinecraftWorld());
				if (pwi != null)
				{
					return pwi.UseEconomy;
				}
			}
		}
		return false;
	}
	
	public static boolean isEconomyEnabled(Plot plot)
	{
		if (plot != null)
		{
			if (isPlotWorld(plot.getMinecraftWorld()))
			{
				PlotWorld pwi = plotWorlds.get(plot.getMinecraftWorld());
				if (pwi != null)
				{
					return pwi.UseEconomy;
				}
			}
		}
		return false;
	}

	public static boolean isEconomyEnabled(World world)
	{
		if (world != null)
		{
			if (isPlotWorld(world))
			{
				PlotWorld pwi = plotWorlds.get(world);
				if (pwi != null)
				{
					return pwi.UseEconomy;
				}
			}
		}
		return false;
	}


	public static boolean isEconomyEnabled(Player player)
	{
		if (player != null)
		{
			return isEconomyEnabled(player.getWorld());
		}
		return false;
	}
	
	public static boolean isEconomyEnabled(Block block)
	{
		if (block != null)
		{
			return isEconomyEnabled(block.getWorld());
		}
		return false;
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
							
							if (PlotMe.lwc!=null)
							{
								boolean ignoreBlockDestruction = Boolean.parseBoolean(PlotMe.lwc.resolveProtectionConfiguration(block, "ignoreBlockDestruction"));
								
								if (!ignoreBlockDestruction)
								{
									Protection protection = PlotMe.lwc.findProtection(block);

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
			hl = plot.getPlotWorld().getCenterBlockLocation(plot).clone().add(0, 3, 0);
		}
		else
		{
			hl = plot.getMinecraftWorld().getSpawnLocation().clone();
		}

		return PlotMe.getAirSpawnPosition(hl);
	}
	
	public static List<Protection> getLWCProtections(final Plot plot)
	{
		if (PlotMe.lwc==null || plot == null || plot.getMinecraftWorld() == null)
		{
			return null;
		}
		
		final Pair<Location, Location> locations = plot.getWorldMinMaxBlockLocations();
		List<Protection> protections = PlotMe.lwc.getPhysicalDatabase().loadProtections(plot.getMinecraftWorld().getName(), locations.getLeft().getBlockX(), locations.getRight().getBlockX(), locations.getLeft().getBlockY(), locations.getRight().getBlockY(), locations.getLeft().getBlockZ(), locations.getRight().getBlockZ());
		return protections;
	}
	
	public static void removeLWCProtections(final Plot plot)
	{
		if (PlotMe.lwc==null || plot == null || plot.getMinecraftWorld() == null)
		{
			return;
		}

		Bukkit.getScheduler().runTaskAsynchronously(PlotMe.self, new Runnable() 
		{	
			@Override
			public void run() 
			{
				List<Protection> protections = getLWCProtections(plot);
				if (protections!=null && protections.size()>0)
				{
					Iterator<Protection> protectionIterator = protections.iterator();
					while (protectionIterator.hasNext())
					{
						protectionIterator.next().remove();
					}
				}
			}
		});
	}
}
