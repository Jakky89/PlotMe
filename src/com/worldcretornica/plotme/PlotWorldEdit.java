package com.worldcretornica.plotme;

import org.bukkit.Location;
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
		
		double ptbbmulti = plot.plotpos.w.getPlotBlockPositionMultiplier();
		
		final int bottomX	= (int)Math.ceil(plot.plotpos.x        * ptbbmulti);
		final int bottomZ	= (int)Math.ceil(plot.plotpos.z        * ptbbmulti);
		final int topX		= (int)Math.floor((plot.plotpos.x - 1) * ptbbmulti);
		final int topZ		= (int)Math.floor((plot.plotpos.z - 1) * ptbbmulti);
		
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
