package com.worldcretornica.plotme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.worldcretornica.plotme.utils.PlotPosition;

public class PlotManager {

    /**
     *  Maps world names to PlotWorld instances 
     */
	public static Map<String, PlotWorld> plotWorlds = null;
	public static Map<Integer, Plot> allPlots = null;
	
	public static long nextPlotExpirationCheck = 0;
	
	
	public PlotManager()
	{
		plotWorlds = new HashMap<String, PlotWorld>();
		allPlots = new HashMap<Integer, Plot>();
	}
	
	public void resetNeighbourPlots(Plot plot)
	{
		if (plot == null)
		{
			return;
		}
		plot.resetNeighbourPlots();
	}
	
	public void notifyNeighbourPlots(Plot plot)
	{
		if (plot == null)
		{
			return;
		}
		plot.notifyNeighbourPlots();
	}
	
	public static void registerPlot(Plot plot)
	{
		if (plot == null)
		{
			return;
		}
		PlotWorld pw = plot.plotpos.w;
		if (pw == null)
		{
			return;
		}
		allPlots.put(plot.id, plot);
		pw.registerPlot(plot);
		plot.notifyNeighbourPlots();
	}
	
	public static PlotWorld getCreatePlotWorld(int id, String worldName)
	{
		if (worldName != null && !worldName.isEmpty())
		{
			PlotWorld pw = plotWorlds.get(worldName);
			if (pw == null)
			{
				World mw = PlotMe.self.getServer().getWorld(worldName);
				pw = new PlotWorld(id, mw);
			}
			plotWorlds.put(worldName, pw);
			return pw;
		}
		return null;
	}

	public static PlotWorld getPlotWorld(String s)
	{
		if (s != null && !s.isEmpty())
		{
			return plotWorlds.get(s);
		}
		return null;
	}
	
	public static PlotWorld getPlotWorld(World w)
	{
		if (w != null)
		{
			return getPlotWorld(w.getName());
		}
		return null;
	}
	
	public static PlotWorld getPlotWorld(Location loc)
	{
		if (loc != null)
		{
			return getPlotWorld(loc.getWorld());
		}
		return null;
	}
	
	public static PlotWorld getPlotWorld(Block b)
	{
		if (b != null)
		{
			return getPlotWorld(b.getLocation());
		}
		return null;
	}
	
	public static Plot getPlotAtBlockPosition(Location loc)
	{
		if (loc == null) {
			return null;
		}
		
		// Get the plotworld instance
		PlotWorld pwi = getPlotWorld(loc);
		if (pwi == null) {
			return null;
		}
		
		// Get the plot position from plotworld instance
		return pwi.getPlotAtBlockPosition(loc);
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

		Plot p0 = plot.plotpos.w.getPlotAtPlotPosition(plot.plotpos.x - 1, plot.plotpos.z - 1);
		Plot p1 = plot.plotpos.w.getPlotAtPlotPosition(plot.plotpos.x - 1, plot.plotpos.z);
		Plot p2 = plot.plotpos.w.getPlotAtPlotPosition(plot.plotpos.x,     plot.plotpos.z - 1);
		Plot p3 = plot.plotpos.w.getPlotAtPlotPosition(plot.plotpos.x,     plot.plotpos.z + 1);
		Plot p4 = plot.plotpos.w.getPlotAtPlotPosition(plot.plotpos.x + 1, plot.plotpos.z);
		Plot p5 = plot.plotpos.w.getPlotAtPlotPosition(plot.plotpos.x - 1, plot.plotpos.z + 1);
		Plot p6 = plot.plotpos.w.getPlotAtPlotPosition(plot.plotpos.x + 1, plot.plotpos.z - 1);
		Plot p7 = plot.plotpos.w.getPlotAtPlotPosition(plot.plotpos.x + 1, plot.plotpos.z + 1);
				
		if (p1 != null && p1.owner.playername.equals(plot.owner.playername))
		{
			fillroad(p1, plot);
		}
				
		if (p2 != null && p2.owner.playername.equals(plot.owner.playername))
		{
			fillroad(p2, plot);
		}

		if (p3 != null && p3.owner.playername.equals(plot.owner.playername))
		{
			fillroad(p3, plot);
		}

		if (p4 != null && p4.owner.playername.equals(plot.owner.playername))
		{
			fillroad(p4, plot);
		}
				
		if (p0 != null && p1 != null  && p2 != null && 
				p0.owner.playername.equals(plot.owner)   &&
				plot.owner.playername.equals(p2.owner) &&
				p2.owner.playername.equals(p1.owner))
		{
			fillmiddleroad(p0, plot);
		}
				
		if (p2 != null && p6 != null && p4 != null &&
				p2.owner.playername.equals(plot.owner)  &&
				plot.owner.playername.equals(p6.owner)  &&
				p6.owner.playername.equals(p4.owner))
		{
			fillmiddleroad(p6, plot);
		}
				
		if (p1 != null && p5 != null && p3 != null &&
				p1.owner.playername.equals(plot.owner)  &&
				plot.owner.playername.equals(p5.owner)  &&
				p5.owner.playername.equals(p3.owner))
		{
			fillmiddleroad(p5, plot);
		}
				
		if (p3 != null && p4 != null && p7 != null &&
				p3.owner.playername.equals(plot.owner)  &&
				plot.owner.playername.equals(p4.owner)  &&
				p4.owner.playername.equals(p7.owner))
		{
			fillmiddleroad(p7, plot);
		}
	}
	
	private static void fillroad(Plot plot1, Plot plot2)
	{
		
		if (plot1.plotpos.w.id != plot2.plotpos.w.id)
		{
			return;
		}
		
		Location bottomPlot1 = getPlotBottomLoc(plot1);
		Location topPlot1 = getPlotTopLoc(plot1);
		Location bottomPlot2 = getPlotBottomLoc(plot2);
		Location topPlot2 = getPlotTopLoc(plot2);
		
		int minX;
		int maxX;
		int minZ;
		int maxZ;
		boolean isWallX;
		
		int h = plot1.plotpos.w.RoadHeight;
		int wallId = plot1.plotpos.w.WallBlockId;
		byte wallValue = plot1.plotpos.w.WallBlockValue;
		int fillId = plot1.plotpos.w.PlotFloorBlockId;
		byte fillValue = plot1.plotpos.w.PlotFloorBlockValue;
				
		if (bottomPlot1.getBlockX() == bottomPlot2.getBlockX())
		{
			minX = bottomPlot1.getBlockX();
			maxX = topPlot1.getBlockX();
			
			minZ = Math.min(bottomPlot1.getBlockZ(), bottomPlot2.getBlockZ()) + plot1.plotpos.w.PlotSize;
			maxZ = Math.max(topPlot1.getBlockZ(), topPlot2.getBlockZ()) - plot1.plotpos.w.PlotSize;
		}
		else
		{
			minZ = bottomPlot1.getBlockZ();
			maxZ = topPlot1.getBlockZ();
			
			minX = Math.min(bottomPlot1.getBlockX(), bottomPlot2.getBlockX()) + plot1.plotpos.w.PlotSize;
			maxX = Math.max(topPlot1.getBlockX(), topPlot2.getBlockX()) - plot1.plotpos.w.PlotSize;
		}
		
		isWallX = (maxX - minX) > (maxZ - minZ);
		
		if(isWallX)
		{
			minX--;
			maxX++;
		}
		else
		{
			minZ--;
			maxZ++;
		}
		
		for (int x = minX; x <= maxX; x++)
		{
			for (int z = minZ; z <= maxZ; z++)
			{
				for (int y = h; y < plot1.plotpos.w.MinecraftWorld.getMaxHeight(); y++)
				{
					if (y >= (h + 2))
					{
						plot1.pW.MinecraftWorld.getBlockAt(x, y, z).setType(Material.AIR);
					}
					else if(y == (h + 1))
					{
						if (isWallX && (x == minX || x == maxX))
						{
							plot1.pW.MinecraftWorld.getBlockAt(x, y, z).setTypeIdAndData(wallId, wallValue, true);
						}
						else if(!isWallX && (z == minZ || z == maxZ))
						{
							plot1.pW.MinecraftWorld.getBlockAt(x, y, z).setTypeIdAndData(wallId, wallValue, true);
						}
						else
						{
							plot1.pW.MinecraftWorld.getBlockAt(x, y, z).setType(Material.AIR);
						}
					}
					else
					{
						plot1.pW.MinecraftWorld.getBlockAt(x, y, z).setTypeIdAndData(fillId, fillValue, true);
					}
				}
			}
		}
	}
	
	private static void fillmiddleroad(Plot plot1, Plot plot2)
	{
		
		if (!plot1.pW.equals(plot2.pW)) {
			return;
		}
		
		Location bottomPlot1 = getPlotBottomLoc(plot1);
		Location topPlot1 = getPlotTopLoc(plot1);
		Location bottomPlot2 = getPlotBottomLoc(plot2);
		Location topPlot2 = getPlotTopLoc(plot2);
		
		int minX;
		int maxX;
		int minZ;
		int maxZ;

		int h = pmi.RoadHeight;
		int fillId = pmi.PlotFloorBlockId;
				
		
		minX = Math.min(topPlot1.getBlockX(), topPlot2.getBlockX());
		maxX = Math.max(bottomPlot1.getBlockX(), bottomPlot2.getBlockX());
		
		minZ = Math.min(topPlot1.getBlockZ(), topPlot2.getBlockZ());
		maxZ = Math.max(bottomPlot1.getBlockZ(), bottomPlot2.getBlockZ());
				
		for(int x = minX; x <= maxX; x++)
		{
			for(int z = minZ; z <= maxZ; z++)
			{
				for(int y = h; y < w.getMaxHeight(); y++)
				{
					if(y >= (h + 1))
					{
						w.getBlockAt(x, y, z).setType(Material.AIR);
					}
					else
					{
						w.getBlockAt(x, y, z).setTypeId(fillId);
					}
				}
			}
		}
	}
	
	public static boolean isPlotAvailable(String id, World world)
	{
		return isPlotAvailable(id, world.getName().toLowerCase());
	}
	
	public static boolean isPlotAvailable(String id, Player p)
	{
		return isPlotAvailable(id, p.getWorld().getName().toLowerCase());
	}
	
	public static boolean isPlotAvailable(int pX, int pZ, String world)
	{
		if (isPlotWorld(world))
		{
			return !getPlots(world).containsKey(pX + ";" + pZ);
		}

		return false;
	}
	
	public static Plot createPlot(int id, int pX, int pZ, String world, String owner)
	{
		if (isPlotAvailable(pX, pZ, world) && id > 0)
		{
			Plot plot = new Plot(owner, getPlotTopLoc(pX, pZ, world), getPlotBottomLoc(pX, pZ, world), id, getMap(world).DaysToExpiration);
			
			setOwnerSign(plot);
			
			getPlots(world).put(plot);
			
			SqlManager.addPlot(plot);
			return plot;
		}
		
		return null;
	}
	
	public static void setOwnerSign(World world, Plot plot)
	{	
		Location pillar = new Location(world, bottomX(plot.id, world) - 1, getMap(world).RoadHeight + 1, bottomZ(plot.id, world) - 1);
						
		Block bsign = pillar.add(0, 0, -1).getBlock();
		bsign.setType(Material.AIR);
		bsign.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte) 2, false);
		
		String id = getPlotId(new Location(world, bottomX(plot.id, world), 0, bottomZ(plot.id, world)));
		
		Sign sign = (Sign) bsign.getState();
		if((PlotMe.caption("SignId") + id).length() > 16)
		{
			sign.setLine(0, (PlotMe.caption("SignId") + id).substring(0, 16));
			if((PlotMe.caption("SignId") + id).length() > 32)
			{
				sign.setLine(1, (PlotMe.caption("SignId") + id).substring(16, 32));
			}
			else
			{
				sign.setLine(1, (PlotMe.caption("SignId") + id).substring(16));
			}
		}
		else
		{
			sign.setLine(0, PlotMe.caption("SignId") + id);
		}
		if((PlotMe.caption("SignOwner") + plot.owner).length() > 16)
		{
			sign.setLine(2, (PlotMe.caption("SignOwner") + plot.owner).substring(0, 16));
			if((PlotMe.caption("SignOwner") + plot.owner).length() > 32)
			{
				sign.setLine(3, (PlotMe.caption("SignOwner") + plot.owner).substring(16, 32));
			}
			else
			{
				sign.setLine(3, (PlotMe.caption("SignOwner") + plot.owner).substring(16));
			}
		}else{
			sign.setLine(2, PlotMe.caption("SignOwner") + plot.owner);
			sign.setLine(3, "");
		}
		sign.update(true);
	}
	
	public static void setSellSign(World world, Plot plot)
	{
		removeSellSign(plot);
		
		if(plot.forsale || plot.auctionned)
		{
			Location pillar = new Location(world, bottomX(plot.id, world) - 1, getMap(world).RoadHeight + 1, bottomZ(plot.id, world) - 1);
							
			Block bsign = pillar.clone().add(-1, 0, 0).getBlock();
			bsign.setType(Material.AIR);
			bsign.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte) 4, false);
			
			Sign sign = (Sign) bsign.getState();
			
			if(plot.forsale)
			{
				sign.setLine(0, PlotMe.caption("SignForSale"));
				sign.setLine(1, PlotMe.caption("SignPrice"));
				if(plot.customprice % 1 == 0)
					sign.setLine(2, PlotMe.caption("SignPriceColor") + Math.round(plot.customprice));
				else
					sign.setLine(2, PlotMe.caption("SignPriceColor") + plot.customprice);
				sign.setLine(3, "/plotme " + PlotMe.caption("CommandBuy"));
				
				sign.update(true);
			}
			
			if(plot.auctionned)
			{				
				if(plot.forsale)
				{
					bsign = pillar.clone().add(-1, 0, 1).getBlock();
					bsign.setType(Material.AIR);
					bsign.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte) 4, false);
					
					sign = (Sign) bsign.getState();
				}
				
				sign.setLine(0, "" + PlotMe.caption("SignOnAuction"));
				if(plot.highestbidder.equals(""))
					sign.setLine(1, PlotMe.caption("SignMinimumBid"));
				else
					sign.setLine(1, PlotMe.caption("SignCurrentBid"));
				if (plot.highestbid % 1 == 0)
					sign.setLine(2, PlotMe.caption("SignCurrentBidColor") + Math.round(plot.highestbid));
				else
					sign.setLine(2, PlotMe.caption("SignCurrentBidColor") + plot.highestbid);
				sign.setLine(3, "/plotme " + PlotMe.caption("CommandBid") + " <x>");
				
				sign.update(true);
			}
		}
	}
	
	public static void removeOwnerSign(World world, String id)
	{
		Location bottom = getPlotBottomLoc(world, id);
		
		Location pillar = new Location(world, bottom.getX() - 1, getMap(world).RoadHeight + 1, bottom.getZ() - 1);
		
		Block bsign = pillar.add(0, 0, -1).getBlock();
		bsign.setType(Material.AIR);
	}
	
	public static void removeSellSign(World world, String id)
	{
		Location bottom = getPlotBottomLoc(world, id);
		
		Location pillar = new Location(world, bottom.getX() - 1, getMap(world).RoadHeight + 1, bottom.getZ() - 1);
		
		Block bsign = pillar.clone().add(-1, 0, 0).getBlock();
		bsign.setType(Material.AIR);
						
		bsign = pillar.clone().add(-1, 0, 1).getBlock();
		bsign.setType(Material.AIR);
	}
	
	public static Location getPlotBottomLoc(Plot plot)
	{
		int multi = (int)((plot.plotpos.w.PlotSize + plot.plotpos.w.PathWidth) - (plot.plotpos.w.PlotSize) - (Math.floor(plot.plotpos.w.PathWidth/2)));
		
		long x = plot.plotpos.x * multi;
		long z = plot.plotpos.z * multi;
		
		return new Location(plot.plotpos.w.MinecraftWorld, x, 1, z);
	}
	
	public static Location getPlotTopLoc(Plot plot)
	{
		int multi = (int)(plot.plotpos.w.PlotSize + plot.plotpos.w.PathWidth) - (int)(Math.floor(plot.plotpos.w.PathWidth/2)) - 1;
		long x = plot.x * multi;
		long z = plot.z * multi;
		
		return new Location(plot.plotpos.w.MinecraftWorld, x, plot.plotpos.w.MinecraftWorld, z);
	}
	
	public static void setBiome(Plot plot, Biome b)
	{
		int bottomX = PlotManager.bottomX(plot) - 1;
		int topX = PlotManager.topX(plot) + 1;
		int bottomZ = PlotManager.bottomZ(plot) - 1;
		int topZ = PlotManager.topZ(plot) + 1;
		
		for(int x = bottomX; x <= topX; x++)
		{
			for(int z = bottomZ; z <= topZ; z++)
			{
				w.getBlockAt(x, 0, z).setBiome(b);
			}
		}
		
		plot.biome = b;
		
		refreshPlotChunks(w, plot);
		
		SqlManager.updatePlot(getIdX(id), getIdZ(id), plot.world, "biome", b.name());
	}
	
	public static void refreshPlotChunks(Plot plot)
	{
		if (plot.pW.MinecraftWorld == null)
		{
			return;
		}
		
		int bottomX = PlotManager.bottomX(plot);
		int topX = PlotManager.topX(plot);
		int bottomZ = PlotManager.bottomZ(plot);
		int topZ = PlotManager.topZ(plot);
		
		int minChunkX = (int) Math.floor((double) bottomX / 16);
		int maxChunkX = (int) Math.floor((double) topX / 16);
		int minChunkZ = (int) Math.floor((double) bottomZ / 16);
		int maxChunkZ = (int) Math.floor((double) topZ / 16);
		
		for(int x = minChunkX; x <= maxChunkX; x++)
		{
			for(int z = minChunkZ; z <= maxChunkZ; z++)
			{
				plot.pW.MinecraftWorld.refreshChunk(x, z);
			}
		}
	}
	
	public static Location getTop(Plot plot)
	{
		return new Location(plot.pW.MinecraftWorld, PlotManager.topX(plot), plot.pW.MinecraftWorld.getMaxHeight(), PlotManager.topZ(plot));
	}
	
	public static Location getBottom(Plot plot)
	{
		return new Location(plot.pW.MinecraftWorld, PlotManager.bottomX(plot), 0, PlotManager.bottomZ(plot));
	}
	
	public static void clear(World w, Plot plot)
	{
		clear(getBottom(plot), getTop(plot));
		
		RemoveLWC(plot);
		
		//regen(w, plot);
	}
	
	public static void clear(Location bottom, Location top)
	{
		PlotMapInfo pmi = getMap(bottom);
		
		int bottomX = bottom.getBlockX();
		int topX = top.getBlockX();
		int bottomZ = bottom.getBlockZ();
		int topZ = top.getBlockZ();
		
		int minChunkX = (int) Math.floor((double) bottomX / 16);
		int maxChunkX = (int) Math.floor((double) topX / 16);
		int minChunkZ = (int) Math.floor((double) bottomZ / 16);
		int maxChunkZ = (int) Math.floor((double) topZ / 16);
		
		World w = bottom.getWorld();
		
		for(int cx = minChunkX; cx <= maxChunkX; cx++)
		{			
			for(int cz = minChunkZ; cz <= maxChunkZ; cz++)
			{			
				Chunk chunk = w.getChunkAt(cx, cz);
				
				for(Entity e : chunk.getEntities())
				{
					Location eloc = e.getLocation();
					
					if(!(e instanceof Player) && eloc.getBlockX() >= bottom.getBlockX() && eloc.getBlockX() <= top.getBlockX() &&
							eloc.getBlockZ() >= bottom.getBlockZ() && eloc.getBlockZ() <= top.getBlockZ())
					{
						e.remove();
					}
				}
			}
		}

		for(int x = bottomX; x <= topX; x++)
		{
			for(int z = bottomZ; z <= topZ; z++)
			{
				Block block = new Location(w, x, 0, z).getBlock();
				
				block.setBiome(Biome.PLAINS);
				
				for(int y = w.getMaxHeight(); y >= 0; y--)
				{
					block = new Location(w, x, y, z).getBlock();
					
					BlockState state = block.getState();
					
					if(state instanceof InventoryHolder)
					{
						InventoryHolder holder = (InventoryHolder) state;
						holder.getInventory().clear();
					}
					
					
					if(state instanceof Jukebox)
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
						block.setTypeId(pmi.BottomBlockId);
					else if(y < pmi.RoadHeight)
						block.setTypeId(pmi.PlotFillingBlockId);
					else if(y == pmi.RoadHeight)
						block.setTypeId(pmi.PlotFloorBlockId);
					else
					{
						if(y == (pmi.RoadHeight + 1) && 
								(x == bottomX - 1 || 
								 x == topX + 1 ||
								 z == bottomZ - 1 || 
								 z == topZ + 1))
						{
							//block.setTypeId(pmi.WallBlockId);
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
		
	public static void adjustWall(Location l)
	{
		Plot plot = getPlotById(l);
		World w = l.getWorld();
		PlotMapInfo pmi = getMap(w);
		
		List<String> wallids = new ArrayList<String>();
		
		String auctionwallid = pmi.AuctionWallBlockId;
		String forsalewallid = pmi.ForSaleWallBlockId;
		
		if(plot.protect) wallids.add(pmi.ProtectedWallBlockId);
		if(plot.auctionned && !wallids.contains(auctionwallid)) wallids.add(auctionwallid);
		if(plot.forsale && !wallids.contains(forsalewallid)) wallids.add(forsalewallid);
		
		if(wallids.size() == 0) wallids.add("" + pmi.WallBlockId + ":" + pmi.WallBlockValue);
		
		int ctr = 0;
			
		Location bottom = getPlotBottomLoc(plot);
		Location top = getPlotTopLoc(plot);
		
		int x;
		int z;
		
		String currentblockid;
		Block block;
		
		for(x = bottom.getBlockX() - 1; x < top.getBlockX() + 1; x++)
		{
			z = bottom.getBlockZ() - 1;
			currentblockid = wallids.get(ctr);
			ctr = (ctr == wallids.size()-1)? 0 : ctr + 1;
			block = w.getBlockAt(x, pmi.RoadHeight + 1, z);
			setWall(block, currentblockid);
		}
		
		for(z = bottom.getBlockZ() - 1; z < top.getBlockZ() + 1; z++)
		{
			x = top.getBlockX() + 1;
			currentblockid = wallids.get(ctr);
			ctr = (ctr == wallids.size()-1)? 0 : ctr + 1;
			block = w.getBlockAt(x, pmi.RoadHeight + 1, z);
			setWall(block, currentblockid);
		}
		
		for(x = top.getBlockX() + 1; x > bottom.getBlockX() - 1; x--)
		{
			z = top.getBlockZ() + 1;
			currentblockid = wallids.get(ctr);
			ctr = (ctr == wallids.size()-1)? 0 : ctr + 1;
			block = w.getBlockAt(x, pmi.RoadHeight + 1, z);
			setWall(block, currentblockid);
		}
		
		for(z = top.getBlockZ() + 1; z > bottom.getBlockZ() - 1; z--)
		{
			x = bottom.getBlockX() - 1;
			currentblockid = wallids.get(ctr);
			ctr = (ctr == wallids.size()-1)? 0 : ctr + 1;
			block = w.getBlockAt(x, pmi.RoadHeight + 1, z);
			setWall(block, currentblockid);
		}
	}
	
	
	private static void setWall(Block block, String currentblockid)
	{
		
		int blockId;
		byte blockData = 0;
		PlotMapInfo pmi = getMap(block);
		
		if(currentblockid.contains(":"))
		{
			try
			{
				blockId = Integer.parseInt(currentblockid.substring(0, currentblockid.indexOf(":")));
				blockData = Byte.parseByte(currentblockid.substring(currentblockid.indexOf(":") + 1));
			}
			catch(NumberFormatException e)
			{
				blockId = pmi.WallBlockId;
				blockData = pmi.WallBlockValue;
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
				blockId = pmi.WallBlockId;
			}
		}
		
		block.setTypeIdAndData(blockId, blockData, true);
	}
	
	
	public static boolean isBlockInPlot(Plot plot, Location blocklocation)
	{
		World w = blocklocation.getWorld();
		int lowestX = Math.min(PlotManager.bottomX(plot), PlotManager.topX(plot));
		int highestX = Math.max(PlotManager.bottomX(plot), PlotManager.topX(plot));
		int lowestZ = Math.min(PlotManager.bottomZ(plot), PlotManager.topZ(plot));
		int highestZ = Math.max(PlotManager.bottomZ(plot), PlotManager.topZ(plot));
		
		return blocklocation.getBlockX() >= lowestX && blocklocation.getBlockX() <= highestX
				&& blocklocation.getBlockZ() >= lowestZ && blocklocation.getBlockZ() <= highestZ;
	}
	
	public static boolean movePlot(Plot plotFrom, Plot plotTo)
	{
		if (plotFrom.pW != plotTo.pW)
		{
			return false;
		}
		
		Location plot1Bottom = getPlotBottomLoc(plotFrom);
		Location plot2Bottom = getPlotBottomLoc(plotTo);
		Location plot1Top = getPlotTopLoc(plotFrom);
		
		int distanceX = plot1Bottom.getBlockX() - plot2Bottom.getBlockX();
		int distanceZ = plot1Bottom.getBlockZ() - plot2Bottom.getBlockZ();
		
		for (int x = plot1Bottom.getBlockX(); x <= plot1Top.getBlockX(); x++)
		{
			for (int z = plot1Bottom.getBlockZ(); z <= plot1Top.getBlockZ(); z++)
			{
				Block plot1Block = plotFrom.pW.MinecraftWorld.getBlockAt(new Location(plotFrom.pW.MinecraftWorld, x, 0, z));
				Block plot2Block = plotTo.pW.MinecraftWorld.getBlockAt(new Location(plotTo.pW.MinecraftWorld, x - distanceX, 0, z - distanceZ));
				
				String plot1Biome = plot1Block.getBiome().name();
				String plot2Biome = plot2Block.getBiome().name();
				
				plot1Block.setBiome(Biome.valueOf(plot2Biome));
				plot2Block.setBiome(Biome.valueOf(plot1Biome));
				
				for (int y = 0; y < plotTo.pW.MinecraftWorld.getMaxHeight() ; y++)
				{
					plot1Block = plotFrom.pW.MinecraftWorld.getBlockAt(new Location(plotFrom.pW.MinecraftWorld, x, y, z));
					int plot1Type = plot1Block.getTypeId();
					byte plot1Data = plot1Block.getData();
					
					plot2Block = plotTo.pW.MinecraftWorld.getBlockAt(new Location(plotTo.pW.MinecraftWorld, x - distanceX, y, z - distanceZ));
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
		
		HashMap<String, Plot> plots = getPlots(plotTo.pW.MinecraftWorld);
		
		SqlManager.deletePlot(plotTo);
		plots.remove(plotFrom);
		plots.remove(plotTo);
		SqlManager.deletePlot(plotFrom);

		SqlManager.addPlot(plotTo);
		plots.put(plotFrom);
				
		for(int i = 0 ; i < plot2.comments.size() ; i++)
		{
			SqlManager.addPlotComment(plot2.comments.get(i), i, idX, idZ, plot2.world);
		}
				
		for(String player : plot2.allowed())
		{
			SqlManager.addPlotAllowed(player, idX, idZ, plot2.world);
				}
				
				idX = getIdX(idTo);
				idZ = getIdZ(idTo);
				plot1.id = "" + idX + ";" + idZ;
				SqlManager.addPlot(plot1, idX, idZ, w);
				plots.put(idTo, plot1);
				
				for(int i = 0 ; i < plot1.comments.size() ; i++)
				{
					SqlManager.addPlotComment(plot1.comments.get(i), i, idX, idZ, plot1.world);
				}
				
				for(String player : plot1.allowed())
				{
					SqlManager.addPlotAllowed(player, idX, idZ, plot1.world);
				}
				
				setOwnerSign(w, plot1);
				setSellSign(w, plot1);
				setOwnerSign(w, plot2);
				setSellSign(w, plot2);
				
			}
			else
			{
				Plot plot = plots.get(idFrom);
				
				int idX = getIdX(idFrom);
				int idZ = getIdZ(idFrom);
				SqlManager.deletePlot(idX, idZ, plot.world);
				plots.remove(idFrom);
				idX = getIdX(idTo);
				idZ = getIdZ(idTo);
				plot.id = "" + idX + ";" + idZ;
				SqlManager.addPlot(plot, idX, idZ, w);
				plots.put(idTo, plot);
				
				for(int i = 0 ; i < plot.comments.size() ; i++)
				{
					SqlManager.addPlotComment(plot.comments.get(i), i, idX, idZ, plot.world);
				}
				
				for(String player : plot.allowed())
				{
					SqlManager.addPlotAllowed(player, idX, idZ, plot.world);
				}
				
				setOwnerSign(w, plot);
				setSellSign(w, plot);
				removeOwnerSign(w, idFrom);
				removeSellSign(w, idFrom);
				
			}
		}else{
			if(plots.containsKey(idTo))
			{
				Plot plot = plots.get(idTo);
				
				int idX = getIdX(idTo);
				int idZ = getIdZ(idTo);
				SqlManager.deletePlot(idX, idZ, plot.world);
				plots.remove(idTo);
				
				idX = getIdX(idFrom);
				idZ = getIdZ(idFrom);
				plot.id = "" + idX + ";" + idZ;
				SqlManager.addPlot(plot, idX, idZ, w);
				plots.put(idFrom, plot);
				
				for(int i = 0 ; i < plot.comments.size() ; i++)
				{
					SqlManager.addPlotComment(plot.comments.get(i), i, idX, idZ, plot.world);
				}
				
				for(String player : plot.allowed())
				{
					SqlManager.addPlotAllowed(player, idX, idZ, plot.world);
				}
				
				setOwnerSign(w, plot);
				setSellSign(w, plot);
				removeOwnerSign(w, idTo);
				removeSellSign(w, idTo);
			}
		}
		
		return true;
	}
	
	public static int getNbOwnedPlot(Player p)
	{
		return getNbOwnedPlot(p.getName(), p.getWorld());
	}
	
	public static int getNbOwnedPlot(Player p, World w)
	{
		return getNbOwnedPlot(p.getName(), w);
	}

	public static int getNbOwnedPlot(String name, World w)
	{
		int nbfound = 0;
		if(PlotManager.getPlots(w) != null)
		{
			for(Plot plot : PlotManager.getPlots(w).values())
			{
				if(plot.owner.equalsIgnoreCase(name))
				{
					nbfound++;
				}
			}
		}
		return nbfound;
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
	
	public static boolean isEconomyEnabled(String name)
	{
		PlotWorld pw = PlotMe.plotWorlds.get(name);
		if (pw != null)
		{
			if (PlotMe.globalUseEconomy && PlotMe.economy != null && pw.UseEconomy)
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean isEconomyEnabled(World w)
	{
		if (w != null)
		{
			return isEconomyEnabled(w.getName());
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
	
	public static PlotMapInfo getMap(String worldname)
	{
		worldname = worldname.toLowerCase();
		
		if (PlotMe.plotmaps.containsKey(worldname))
		{
			return PlotMe.plotmaps.get(worldname);
		}
		return null;
	}
	
	public static PlotMapInfo getMap(Location l)
	{
		if (l != null)
		{
			String worldname = l.getWorld().getName().toLowerCase();
			if (PlotMe.plotmaps.containsKey(worldname))
			{
					return PlotMe.plotmaps.get(worldname);
			}
		}
		return null;
	}
	
	public static PlotMapInfo getMap(Player p)
	{
		if (p != null) {
			String worldname = p.getWorld().getName().toLowerCase();
			if (PlotMe.plotmaps.containsKey(worldname))
			{
				return PlotMe.plotmaps.get(worldname);
			}
		}
		return null;
	}
	
	public static PlotMapInfo getMap(Block b)
	{
		if (b != null)
		{
			String worldname = b.getWorld().getName().toLowerCase();
			if (PlotMe.plotmaps.containsKey(worldname)) {
				return PlotMe.plotmaps.get(worldname);
			}
		}
		return null;
	}
	
	public static HashMap<String, Plot> getPlots(World w)
	{
		PlotMapInfo pmi = getMap(w);
		if (pmi != null)
		{
			return pmi.plots;
		}
		return null;
	}
	
	public static HashMap<String, Plot> getPlots(String name)
	{		
		PlotMapInfo pmi = getMap(name);
		if(pmi != null)
		{
			return pmi.plots;
		}
		return null;
	}
	
	public static HashMap<String, Plot> getPlots(Player p)
	{		
		PlotMapInfo pmi = getMap(p);
		if (pmi != null)
		{
			return pmi.plots;
		}
		return null;
	}
	
	public static HashMap<String, Plot> getPlots(Block b)
	{	
		PlotMapInfo pmi = getMap(b);
		if (pmi != null)
		{
			return pmi.plots;
		}
		return null;
	}
	
	public static HashMap<String, Plot> getPlots(Location l)
	{
		PlotMapInfo pmi = getMap(l);
		if (pmi != null)
		{
			return pmi.plots;
		}
		return null;
	}
	
	public static Plot getPlotById(World w, String id)
	{
		HashMap<String, Plot> plots = getPlots(w);
		if (plots != null)
		{
			return plots.get(id);
		}
		return null;
	}
	
	public static Plot getPlotById(String name, String id)
	{
		HashMap<String, Plot> plots = getPlots(name);
		if (plots != null)
		{
			return plots.get(id);
		}
		return null;
	}
	
	public static Plot getPlotById(Player p, String id)
	{
		HashMap<String, Plot> plots = getPlots(p);
		if(plots != null)
		{
			return plots.get(id);
		}
		return null;
	}
	
	public static Plot getPlotById(Player p)
	{
		HashMap<String, Plot> plots = getPlots(p);
		if (plots != null)
		{
			int plotid = getPlotId(p.getLocation());
			if (plotid > 0)
			{
				return plots.get(plotid);
			}
		}
		return null;
	}
	
	public static Plot getPlotById(Location l)
	{
		HashMap<String, Plot> plots = getPlots(l);
		if (plots != null)
		{
			int plotid = getPlotId(l);
			if (plotid > 0)
			{
				return plots.get(plotid);
			}
		}
		return null;
	}
	
	public static Plot getPlotById(Block b, int id)
	{
		HashMap<String, Plot> plots = getPlots(b);
		if (plots != null)
		{
			return plots.get(id);
		}
		return null;
	}
	
	public static Plot getPlotById(Block b)
	{
		HashMap<String, Plot> plots = getPlots(b);
		if (plots != null)
		{
			int plotid = getPlotId(b.getLocation());
			if (plotid > 0)
			{
				return plots.get(plotid);
			}
		}
		return null;
	}
	
	public static void deleteNextExpired(World w, CommandSender sender)
	{
		List<Plot> expiredplots = new ArrayList<Plot>();
		HashMap<String, Plot> plots = getPlots(w);
		String date = PlotMe.getDate();
		Plot expiredplot;
		
		for (String id : plots.keySet())
		{
			Plot plot = plots.get(id);
			
			if (!plot.protect && !plot.finished && plot.expireddate > 0 && plot.expireddate < Math.round(System.currentTimeMillis()/1000))
			{
				expiredplots.add(plot);
			}
		}
		
		plots = null;
		
		Collections.sort(expiredplots);
		
		expiredplot = expiredplots.get(0);
		
		expiredplots = null;
		
		clear(w, expiredplot);
		
		getPlots(plot.pW.MinecraftWorld).remove(plot);
			
		removeOwnerSign(plot);
		removeSellSign(plot);
		
		SqlManager.deletePlot(id);
	}

	public static World getFirstWorld()
	{
		if(PlotMe.plotmaps != null)
		{
			if(PlotMe.plotmaps.keySet() != null)
			{
				if(PlotMe.plotmaps.keySet().toArray().length > 0)
				{
					return Bukkit.getWorld((String) PlotMe.plotmaps.keySet().toArray()[0]);
				}
			}
		}
		return null;
	}
	
	public static World getFirstWorld(String player)
	{
		if(PlotMe.plotmaps != null)
		{
			if(PlotMe.plotmaps.keySet() != null)
			{
				if(PlotMe.plotmaps.keySet().toArray().length > 0)
				{
					for(String mapkey : PlotMe.plotmaps.keySet())
					{
						for(String id : PlotMe.plotmaps.get(mapkey).plots.keySet())
						{
							if(PlotMe.plotmaps.get(mapkey).plots.get(id).owner.equalsIgnoreCase(player))
							{
								return Bukkit.getWorld(mapkey);
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	public static Plot getFirstPlot(String player)
	{
		if(PlotMe.plotmaps != null)
		{
			if(PlotMe.plotmaps.keySet() != null)
			{
				if(PlotMe.plotmaps.keySet().toArray().length > 0)
				{
					for(String mapkey : PlotMe.plotmaps.keySet())
					{
						for(String id : PlotMe.plotmaps.get(mapkey).plots.keySet())
						{
							if(PlotMe.plotmaps.get(mapkey).plots.get(id).owner.equalsIgnoreCase(player))
							{
								return PlotMe.plotmaps.get(mapkey).plots.get(id);
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	public static boolean isValidId(String id)
	{
		String[] coords = id.split(";");
		
		if(coords.length != 2)
			return false;
		else
		{
			try
			{
				Integer.parseInt(coords[0]);
				Integer.parseInt(coords[1]);
				return true;
			}catch(Exception e)
			{
				return false;
			}
		}
	}
	
	public static void regen(World w, Plot plot, CommandSender sender)
	{
		int bottomX = PlotManager.bottomX(plot);
		int topX = PlotManager.topX(plot.id, w);
		int bottomZ = PlotManager.bottomZ(plot);
		int topZ = PlotManager.topZ(plot.id, w);
		
		int minChunkX = (int) Math.floor((double) bottomX / 16);
		int maxChunkX = (int) Math.floor((double) topX / 16);
		int minChunkZ = (int) Math.floor((double) bottomZ / 16);
		int maxChunkZ = (int) Math.floor((double) topZ / 16);
		
		HashMap<Location, Biome> biomes = new HashMap<Location, Biome>();
		
		for(int cx = minChunkX; cx <= maxChunkX; cx++)
		{
			int xx = cx << 4;
			
			for(int cz = minChunkZ; cz <= maxChunkZ; cz++)
			{	
				int zz = cz << 4;
				
				BlockState[][][] blocks = new BlockState[16][16][w.getMaxHeight()];
				//Biome[][] biomes = new Biome[16][16];
				
				for(int x = 0; x < 16; x++)
				{
					for(int z = 0; z < 16; z++)
					{
						biomes.put(new Location(w, x + xx, 0, z + zz), w.getBiome(x + xx, z + zz));
						
						for(int y = 0; y < w.getMaxHeight(); y++)
						{
							Block block = w.getBlockAt(x + xx, y, z + zz);
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
		            w.regenerateChunk(cx, cz);
		        } catch (Throwable t) {
		            t.printStackTrace();
		        }
				
				for(int x = 0; x < 16; x++)
				{
					for(int z = 0; z < 16; z++)
					{						
						for(int y = 0; y < w.getMaxHeight(); y++)
						{
							if((x + xx) < bottomX || (x + xx) > topX || (z + zz) < bottomZ || (z + zz) > topZ)
							{
								Block newblock = w.getBlockAt(x + xx, y, z + zz);
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
			
			w.setBiome(x, z, biomes.get(loc));
		}
		
		//refreshPlotChunks(w, plot);
	}
	
	public static Location getPlotHome(Plot plot)
	{
		if (plot.plotpos.w != null)
		{
			return new Location(plot.plotpos.w.MinecraftWorld, plot.plotpos.w.getBottomPlotToBlockPositionMultiplier()*plot.plotpos.x + (topX(plot) - 
					   PlotManager.bottomX(plot))/2, plot.pW.RoadHeight + 2, bottomZ(plot) - 2);
		}
		else
		{
			return w.getSpawnLocation();
		}
	}
	
	public static void RemoveLWC(final Plot plot)
	{
		if(PlotMe.usinglwc)
		{
			
			Location bottom = getBottom(plot);
			Location top = getTop(plot);
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
					List<Protection> protections = lwc.getPhysicalDatabase().loadProtections(plot.pW.MinecraftWorld.getName(), x1, x2, y1, y2, z1, z2);

					for (Protection protection : protections) {
					    protection.remove();
					}
				}
			});
			
			// loadProtections(String world, int x1, int x2, int y1, int y2, int z1, int z2)
			// _1 is min, _2 max
			/*List<Protection> protections = lwc.getPhysicalDatabase().loadProtections(wname, x1, x2, y1, y2, z1, z2);

			for (Protection protection : protections) {
			    protection.remove();
			}*/
			
			
			//plugin.scheduleProtectionRemoval(PlotManager.getBottom(w, plot), PlotManager.getTop(w, plot));
			
			/*Player p = Bukkit.getServer().getPlayerExact(plot.owner);
			
			if(p == null)
			{
				p = (Player) Bukkit.getServer().getOfflinePlayer(plot.owner);
			}
			
			if(p == null)
			{
				PlotMe.logger.info("didnt find player:" + plot.owner);
			}else{
				Location bottom = getBottom(w, plot);
				Location top = getTop(w, plot);
				
				LWC lwc = com.griefcraft.lwc.LWC.getInstance();
				
				int x1 = bottom.getBlockX();
		    	int y1 = bottom.getBlockY();
		    	int z1 = bottom.getBlockZ();
		    	int x2 = top.getBlockX();
		    	int y2 = top.getBlockY();
		    	int z2 = top.getBlockZ();
		    	
		    	for(int x = x1; x <= x2; x++)
		    	{
		    		for(int z = z1; z <= z2; z++)
		    		{
		    			for(int y = y1; y <= y2; y++)
		    			{
		    				Block block = w.getBlockAt(x, y, z);
	
							Material material = block.getType();
							
							boolean ignoreBlockDestruction = Boolean.parseBoolean(lwc.resolveProtectionConfiguration(material, "ignoreBlockDestruction"));
							
							if (!ignoreBlockDestruction)
							{							
								Protection protection = lwc.findProtection(block);
								
								if(protection != null)
								{
									protection.remove();
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
								}
							}
						}
		    		}
		    	}
			}*/
	    }
	}
}
