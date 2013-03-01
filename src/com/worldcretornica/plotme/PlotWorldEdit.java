package com.worldcretornica.plotme;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.masks.RegionMask;
import com.sk89q.worldedit.regions.CuboidRegion;

public class PlotWorldEdit {
	
	public static void setMask(Player p)
	{
		setMask(p, p.getLocation());
	}
	
	public static void setMask(Player player, Location location)
	{
		Plot plot = PlotManager.getPlotAtBlockPosition(location);
		
		int ptbbmulti = plot.plotpos.w.getBottomPlotToBlockPositionMultiplier();
		int ptbtmulti = plot.plotpos.w.getTopPlotToBlockPositionMultiplier();
		
		int bottomX = plot.plotpos.x * ptbbmulti;
		int bottomZ = plot.plotpos.z * ptbbmulti;
		int topX    = plot.plotpos.x * ptbtmulti;
		int topZ    = plot.plotpos.z * ptbtmulti;
		
		int maxHeight = plot.plotpos.w.MinecraftWorld.getMaxHeight();
		
		LocalSession session = PlotMe.worldedit.getSession(player);

		if (plot != null && plot.isAllowed(player.getName(), true, true))
		{			
			BukkitPlayer bukkitplayer = PlotMe.worldedit.wrapPlayer(player);
			LocalWorld localworld = bukkitplayer.getWorld();

			Vector pos1 = new Vector(bottomX, 1,         bottomZ);
			Vector pos2 = new Vector(topX,    maxHeight, topZ);
						
			CuboidRegion cr = new CuboidRegion(localworld, pos1, pos2);
				
			RegionMask rm = new RegionMask(cr);
				
			session.setMask(rm);
			return;
		}

		if (session.getMask() == null)
		{
			BukkitPlayer bukkitplayer = PlotMe.worldedit.wrapPlayer(player);
			LocalWorld localworld = bukkitplayer.getWorld();
					
			Vector pos1 = new Vector(bottomX, 1,         bottomZ);
			Vector pos2 = new Vector(topX,    maxHeight, topZ);
					
			CuboidRegion cr = new CuboidRegion(localworld, pos1, pos2);
			
			RegionMask rm = new RegionMask(cr);
			
			session.setMask(rm);
		}
	}

	public static void removeMask(Player p)
	{
		LocalSession session = PlotMe.worldedit.getSession(p);
		session.setMask(null);
	}	
}
