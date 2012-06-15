package com.worldcretornica.plotme;

import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class PlotManager {
				
	public static String getPlotId(Location loc)
	{
		if(isPlotWorld(loc))
		{
			int valx = loc.getBlockX();
			int valz = loc.getBlockZ();
			PlotMapInfo pmi = getMap(loc);
			int size = pmi.PlotSize + 7;
			int pathsize = pmi.PathWidth;
			
			double n3;
			int mod2 = 0;
			int mod1 = 1;
			
			if(pathsize % 2 == 1)
			{
				n3 = Math.ceil(((double)pathsize)/2); //3 7
			}else{
				n3 = Math.floor(((double)pathsize)/2); //3 7
			}
			
			if(pathsize % 2 == 1)
			{
				mod2 = -1;
			}
			
			
			boolean found = false;
			for(double i = n3; i >= 0; i--)
			{
				if((valx - i + mod1) % size == 0 || (valx + i + mod2) % size == 0)
				{
					found = true;
					break;
				}	                			
			}
	
			if(found)
			{
				return "";
			}else{
				
				boolean found2 = false;
				for(double i = n3; i >= 0; i--)
				{
					if((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0)
					{
						found2 = true;
						break;
					}	                			
				}
	
				if(found2)
				{
					return "";
				}else{
					int x = (int) Math.ceil((double)valx / size);
					int z = (int) Math.ceil((double)valz / size);
					
					return "" + x + ";" + z;
				}
			}
		}else{
			return "";
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
	
	public static boolean isPlotAvailable(String id, String world)
	{
		if(isPlotWorld(world))
		{
			return !getPlots(world).containsKey(id);
		}else{
			return false;
		}
	}
	
	public static Plot createPlot(World world, String id, String owner)
	{
		if(isPlotAvailable(id, world))
		{
			Plot plot = new Plot(owner, getPlotTopLoc(world, id), getPlotBottomLoc(world, id));
			
			setSign(world, plot);
			
			getPlots(world).put(id, plot);
			SqlManager.addPlot(plot, getIdX(id), getIdZ(id));
			return plot;
		}else{
			return null;
		}
	}
	
	public static void setSign(World world, Plot plot)
	{
		Location pillar = new Location(world, plot.bottomX - 1, 65, plot.bottomZ - 1);
				
		Block bsign = pillar.add(0, 0, -1).getBlock();
		bsign.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte) 2, false);
		
		String id = getPlotId(new Location(world, plot.bottomX, 0, plot.bottomZ));
		
		
		Sign sign = (Sign) bsign.getState();
		if(("ID:" + id).length() > 15)
		{
			sign.setLine(0, ("ID:" + id).substring(0, 15));
			if(("ID:" + id).length() > 30)
			{
				sign.setLine(1, ("ID:" + id).substring(15, 30));
			}else{
				sign.setLine(1, ("ID:" + id).substring(15));
			}
		}
		else
		{
			sign.setLine(0, "ID:" + id);
		}
		if(("Owner:" + plot.owner).length() > 15)
		{
			sign.setLine(2, ("Owner:" + plot.owner).substring(0, 15));
			if(("Owner: " + plot.owner).length() > 30)
			{
				sign.setLine(3, ("Owner:" + plot.owner).substring(15, 30));
			}else{
				sign.setLine(3, ("Owner:" + plot.owner).substring(15));
			}
		}else{
			sign.setLine(2, "Owner:" + plot.owner);
		}
		sign.update(true);
	}
	
	public static void removeSign(World world, String id)
	{
		Location bottom = getPlotBottomLoc(world, id);
		
		Location pillar = new Location(world, bottom.getX() - 1, 65, bottom.getZ() - 1);
		
		Block bsign = pillar.add(0, 0, -1).getBlock();
		bsign.setType(Material.AIR);
	}
	
	public static int getIdX(String id)
	{
		return Integer.parseInt(id.substring(0, id.indexOf(";")));
	}
	
	public static int getIdZ(String id)
	{
		return Integer.parseInt(id.substring(id.indexOf(";") + 1));
	}
	
	public static Location getPlotBottomLoc(World world, String id)
	{
		int px = getIdX(id);
		int pz = getIdZ(id);
		
		int plotsize = getMap(world).PlotSize;
		
		int x = px * (plotsize + 7) - (plotsize - 1) - 4;
		int z = pz * (plotsize + 7) - (plotsize - 1) - 4;
		
		return new Location(world, x, 1, z);
	}
	
	public static Location getPlotTopLoc(World world, String id)
	{
		int px = getIdX(id);
		int pz = getIdZ(id);
		
		int plotsize = getMap(world).PlotSize;
		
		int x = px * (plotsize + 7) - 4;
		int z = pz * (plotsize + 7) - 4;
		
		return new Location(world, x, 255, z);
	}
	
	public static void setBiome(World w, String id, Plot plot, Biome b)
	{
		for(int x = plot.bottomX; x <= plot.topX; x++)
		{
			for(int z = plot.bottomZ; z <= plot.topZ; z++)
			{
				w.getBlockAt(x, 0, z).setBiome(b);
			}
		}
		
		plot.biome = b;
		SqlManager.updatePlot(getIdX(id), getIdZ(id), plot.world, "biome", b.name());
	}
	
	public static Location getTop(World w, Plot plot)
	{
		return new Location(w, plot.topX, 256, plot.topZ);
	}
	
	public static void clear(World w, Plot plot)
	{
		clear(new Location(w, plot.bottomX, 0, plot.bottomZ), new Location(w, plot.topX, 256, plot.topZ));
	}
	
	public static void clear(Location bottom, Location top)
	{
		PlotMapInfo pmi = PlotManager.getMap(bottom);
		
		for(int x = bottom.getBlockX() - 1; x <= top.getBlockX() + 1; x++)
		{
			for(int z = bottom.getBlockZ() - 1; z <= top.getBlockZ() + 1; z++)
			{
				Block block = new Location(bottom.getWorld(), x, 0, z).getBlock();
				
				block.setBiome(Biome.PLAINS);
				
				for(int y = 0; y < 256; y++)
				{
					block = new Location(bottom.getWorld(), x, y, z).getBlock();
					
					if(y == 0)
						block.setTypeId(pmi.BottomBlockId);
					else if(y < 64)
						block.setTypeId(pmi.PlotFillingBlockId);
					else if(y == 64)
						block.setTypeId(pmi.PlotFloorBlockId);
					else
					{
						if(y == 65 && (x == bottom.getBlockX() - 1 || x == top.getBlockX() + 1 ||
								z == bottom.getBlockZ() - 1 || z == top.getBlockZ() + 1))
						{
							block.setTypeId(pmi.WallBlockId);
						}else{
							block.setType(Material.AIR);
						}
					}
				}
			}
		}
	} 
	
	public static boolean isBlockInPlot(Plot plot, Location blocklocation)
	{
		int lowestX = plot.bottomX < plot.topX ? plot.bottomX : plot.topX;
		int highestX = plot.bottomX > plot.topX ? plot.bottomX : plot.topX;
		int lowestZ = plot.bottomZ < plot.topZ ? plot.bottomZ : plot.topZ;
		int highestZ = plot.bottomZ > plot.topZ ? plot.bottomZ : plot.topZ;
		
		return blocklocation.getBlockX() >= lowestX && blocklocation.getBlockX() <= highestX
				&& blocklocation.getBlockZ() >= lowestZ && blocklocation.getBlockZ() <= highestZ;
	}
	
	public static boolean movePlot(World w, String idFrom, String idTo)
	{
		Location plot1Bottom = getPlotBottomLoc(w, idFrom);
		Location plot2Bottom = getPlotBottomLoc(w, idTo);
		Location plot1Top = getPlotTopLoc(w, idFrom);
		
		int distanceX = plot1Bottom.getBlockX() - plot2Bottom.getBlockX();
		int distanceZ = plot1Bottom.getBlockZ() - plot2Bottom.getBlockZ();
		
		for(int x = plot1Bottom.getBlockX(); x <= plot1Top.getBlockX(); x++)
		{
			for(int z = plot1Bottom.getBlockZ(); z <= plot1Top.getBlockZ(); z++)
			{
				Block plot1Block = w.getBlockAt(new Location(w, x, 0, z));
				Block plot2Block = w.getBlockAt(new Location(w, x - distanceX, 0, z - distanceZ));
				
				String plot1Biome = plot1Block.getBiome().name();
				String plot2Biome = plot2Block.getBiome().name();
				
				plot1Block.setBiome(Biome.valueOf(plot2Biome));
				plot2Block.setBiome(Biome.valueOf(plot1Biome));
				
				for(int y = 0; y < 256 ; y++)
				{
					plot1Block = w.getBlockAt(new Location(w, x, y, z));
					int plot1Type = plot1Block.getTypeId();
					byte plot1Data = plot1Block.getData();
					
					plot2Block = w.getBlockAt(new Location(w, x - distanceX, y, z - distanceZ));
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
		
		HashMap<String, Plot> plots = getPlots(w);
		
		if(plots.containsKey(idFrom))
		{
			if(plots.containsKey(idTo))
			{
				Plot plot1 = plots.get(idFrom);
				Plot plot2 = plots.get(idTo);
				
				Plot plottemp = new Plot();
				plottemp.bottomX = plot1.bottomX;
				plottemp.bottomZ = plot1.bottomZ;
				plottemp.topX = plot1.topX;
				plottemp.topZ = plot1.topZ;
				
				plot1.bottomX = plot2.bottomX;
				plot1.bottomZ = plot2.bottomZ;
				plot1.topX = plot2.topX;
				plot1.topZ = plot2.topZ;
				
				plot2.bottomX = plottemp.bottomX;
				plot2.bottomZ = plottemp.bottomZ;
				plot2.topX = plottemp.topX;
				plot2.topZ = plottemp.topZ;
				
				int idX = getIdX(idTo);
				int idZ = getIdZ(idTo);
				SqlManager.deletePlot(idX, idZ, plot2.world);
				plots.remove(idFrom);
				plots.remove(idTo);
				idX = getIdX(idFrom);
				idZ = getIdZ(idFrom);
				SqlManager.deletePlot(idX, idZ, plot1.world);
								
				SqlManager.addPlot(plot2, idX, idZ);
				plots.put(idFrom, plot2);
				
				for(int i = 0 ; i < plot2.comments.size() ; i++)
				{
					SqlManager.addPlotComment(plot2.comments.get(i), i, idX, idZ, plot2.world);
				}
				
				for(String player : plot2.allowed)
				{
					SqlManager.addPlotAllowed(player, idX, idZ, plot2.world);
				}
				
				idX = getIdX(idTo);
				idZ = getIdZ(idTo);
				SqlManager.addPlot(plot1, idX, idZ);
				plots.put(idTo, plot1);
				
				for(int i = 0 ; i < plot1.comments.size() ; i++)
				{
					SqlManager.addPlotComment(plot1.comments.get(i), i, idX, idZ, plot1.world);
				}
				
				for(String player : plot1.allowed)
				{
					SqlManager.addPlotAllowed(player, idX, idZ, plot1.world);
				}
				
				setSign(w, plot1);
				setSign(w, plot2);
				
			}else{
				Plot plot = plots.get(idFrom);
				Location bottom = getPlotBottomLoc(w, idTo);
				Location top = getPlotTopLoc(w, idTo);
								
				plot.bottomX = bottom.getBlockX();
				plot.bottomZ = bottom.getBlockZ();
				plot.topX = top.getBlockX();
				plot.topZ = top.getBlockZ();
				
				int idX = getIdX(idFrom);
				int idZ = getIdZ(idFrom);
				SqlManager.deletePlot(idX, idZ, plot.world);
				plots.remove(idFrom);
				idX = getIdX(idTo);
				idZ = getIdZ(idTo);
				SqlManager.addPlot(plot, idX, idZ);
				plots.put(idTo, plot);
				
				for(int i = 0 ; i < plot.comments.size() ; i++)
				{
					SqlManager.addPlotComment(plot.comments.get(i), i, idX, idZ, plot.world);
				}
				
				for(String player : plot.allowed)
				{
					SqlManager.addPlotAllowed(player, idX, idZ, plot.world);
				}
				
				setSign(w, plot);
				removeSign(w, idFrom);
				
			}
		}else{
			if(plots.containsKey(idTo))
			{
				Plot plot = plots.get(idTo);
				Location bottom = getPlotBottomLoc(w, idFrom);
				Location top = getPlotTopLoc(w, idFrom);
								
				plot.bottomX = bottom.getBlockX();
				plot.bottomZ = bottom.getBlockZ();
				plot.topX = top.getBlockX();
				plot.topZ = top.getBlockZ();
				
				int idX = getIdX(idTo);
				int idZ = getIdZ(idTo);
				SqlManager.deletePlot(idX, idZ, plot.world);
				plots.remove(idTo);
				
				idX = getIdX(idFrom);
				idZ = getIdZ(idFrom);
				SqlManager.addPlot(plot, idX, idZ);
				plots.put(idFrom, plot);
				
				for(int i = 0 ; i < plot.comments.size() ; i++)
				{
					SqlManager.addPlotComment(plot.comments.get(i), i, idX, idZ, plot.world);
				}
				
				for(String player : plot.allowed)
				{
					SqlManager.addPlotAllowed(player, idX, idZ, plot.world);
				}
				
				setSign(w, plot);
				removeSign(w, idTo);
			}
		}
		
		return true;
	}
	
	public static boolean isPlotWorld(World w)
	{
		return PlotMe.plotmaps.containsKey(w.getName().toLowerCase());
	}
	
	public static boolean isPlotWorld(String name)
	{
		return PlotMe.plotmaps.containsKey(name.toLowerCase());
	}
	
	public static boolean isPlotWorld(Location l)
	{
		return PlotMe.plotmaps.containsKey(l.getWorld().getName().toLowerCase());
	}
	
	public static boolean isPlotWorld(Player p)
	{
		return PlotMe.plotmaps.containsKey(p.getWorld().getName().toLowerCase());
	}
	
	public static boolean isPlotWorld(Block b)
	{
		if(b == null)
			return false;
		else
			return PlotMe.plotmaps.containsKey(b.getWorld().getName().toLowerCase());
	}
	
	public static boolean isPlotWorld(BlockState b) {
		return PlotMe.plotmaps.containsKey(b.getWorld().getName().toLowerCase());
	}
	
	public static PlotMapInfo getMap(World w)
	{
		return PlotMe.plotmaps.get(w.getName().toLowerCase());
	}
	
	public static PlotMapInfo getMap(String name)
	{
		return PlotMe.plotmaps.get(name.toLowerCase());
	}
	
	public static PlotMapInfo getMap(Location l)
	{
		return PlotMe.plotmaps.get(l.getWorld().getName().toLowerCase());
	}
	
	public static PlotMapInfo getMap(Player p)
	{
		return PlotMe.plotmaps.get(p.getWorld().getName().toLowerCase());
	}
	
	public static PlotMapInfo getMap(Block b)
	{
		if(b == null)
			return null;
		else
			return PlotMe.plotmaps.get(b.getWorld().getName().toLowerCase());
	}
	
	public static HashMap<String, Plot> getPlots(World w)
	{
		return PlotMe.plotmaps.get(w.getName().toLowerCase()).plots;
	}
	
	public static HashMap<String, Plot> getPlots(String name)
	{
		return PlotMe.plotmaps.get(name.toLowerCase()).plots;
	}
	
	public static HashMap<String, Plot> getPlots(Player p)
	{
		return PlotMe.plotmaps.get(p.getWorld().getName().toLowerCase()).plots;
	}
	
	public static HashMap<String, Plot> getPlots(Block b)
	{
		if(b == null)
			return null;
		else
			return PlotMe.plotmaps.get(b.getWorld().getName().toLowerCase()).plots;
	}
	
	public static Plot getPlotById(World w, String id)
	{
		return PlotMe.plotmaps.get(w.getName().toLowerCase()).plots.get(id);
	}
	
	public static Plot getPlotById(String name, String id)
	{
		return PlotMe.plotmaps.get(name.toLowerCase()).plots.get(id);
	}
	
	public static Plot getPlotById(Player p, String id)
	{
		return PlotMe.plotmaps.get(p.getWorld().getName().toLowerCase()).plots.get(id);
	}
	
	public static Plot getPlotById(Player p)
	{
		return PlotMe.plotmaps.get(p.getWorld().getName().toLowerCase()).plots.get(getPlotId(p.getLocation()));
	}
	
	public static Plot getPlotById(Block b, String id)
	{
		if(b == null)
			return null;
		else
			return PlotMe.plotmaps.get(b.getWorld().getName().toLowerCase()).plots.get(id);
	}
	
	public static Plot getPlotById(Block b)
	{
		if(b == null)
			return null;
		else
		{
			return PlotMe.plotmaps.get(b.getWorld().getName().toLowerCase()).plots.get(getPlotId(b.getLocation()));
		}
	}
}