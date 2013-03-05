package com.worldcretornica.plotme;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.masks.RegionMask;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.worldcretornica.plotme.utils.Pair;

public class PlotWorldEdit {
	
	public static void setMask(Player player)
	{
		setMask(player, player.getLocation());
	}
	
	public static void setMask(Player player, Location location)
	{
		LocalSession session = PlotMe.worldedit.getSession(player);

		Plot plot = PlotManager.getPlotAtBlockPosition(location);
		if (plot == null)
		{
			session.setMask(null);
		}
		
		if (!plot.getMinecraftWorld().equals(location.getWorld()) || !player.getWorld().equals(plot.getMinecraftWorld()))
		{
			session.setMask(null);
			return;
		}
		
		Pair<Location, Location> locations = plot.getPlotWorld().getMinMaxBlockLocation(plot);
		if (locations == null)
		{
			session.setMask(null);
			return;
		}
	
		if (plot.isAllowed(player.getName()))
		{
			Vector pos1 = new Vector(locations.getLeft().getBlockX(), locations.getLeft().getBlockY(), locations.getLeft().getBlockZ());
			Vector pos2 = new Vector(locations.getRight().getBlockX(), locations.getRight().getBlockY(), locations.getRight().getBlockZ());
		
			BukkitPlayer bukkitplayer = PlotMe.worldedit.wrapPlayer(player);
			LocalWorld localworld = bukkitplayer.getWorld();
			
			CuboidRegion cr = new CuboidRegion(localworld, pos1, pos2);
			RegionMask rm = new RegionMask(cr);
			
			session.setMask(rm);
		}
		else
		{
			session.setMask(null);
		}
	}

	public static void removeMask(Player player)
	{
		LocalSession session = PlotMe.worldedit.getSession(player);
		session.setMask(null);
	}	
}
