package com.worldcretornica.plotme.listener;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotMe;
import com.worldcretornica.plotme.utils.Pair;

public class PlotDenyListener implements Listener
{
	
	public static HashMap<String, Location> previousPlayerLocations;
	
	
	public PlotDenyListener()
	{
		previousPlayerLocations = new HashMap<String, Location>();
	}
	

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerMove(final PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		if (player == null)
		{
			event.setCancelled(true);
			return;
		}
		
		Location fromloc = event.getFrom();
		Location toloc = event.getTo();
		
		if (fromloc == null || toloc == null || fromloc.getWorld() != toloc.getWorld() || fromloc.getBlockX() != toloc.getBlockX() || fromloc.getBlockZ() != toloc.getBlockZ())
		{
			if (PlotManager.isPlotWorld(toloc.getWorld()) && !PlotMe.cPerms(player, "plotme.admin.bypassdeny"))
			{
				Plot plot = PlotManager.getPlotAtBlockPosition(toloc);
				if (plot != null)
				{
					if (plot.isDenied(player.getName(), true, true))
					{
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerTeleport(final PlayerTeleportEvent event)
	{
		Player player = event.getPlayer();
		if (player == null)
		{
			event.setCancelled(true);
			return;
		}
		
		Location fromloc = event.getFrom();
		Location toloc = event.getTo();
		
		if (fromloc == null || toloc == null || fromloc.getWorld() != toloc.getWorld() || fromloc.getBlockX() != toloc.getBlockX() || fromloc.getBlockZ() != toloc.getBlockZ())
		{
			if (PlotManager.isPlotWorld(toloc.getWorld()) && !PlotMe.cPerms(player, "plotme.admin.bypassdeny"))
			{
				Plot plot = PlotManager.getPlotAtBlockPosition(toloc);
				if (plot != null && plot.isDenied(player.getName(), true, true))
				{
					event.setTo(PlotManager.getPlotHome(plot));
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(final PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		if (PlotManager.isPlotWorld(player.getWorld()) && !PlotMe.cPerms(player, "plotme.admin.bypassdeny"))
		{
			Plot plot = PlotManager.getPlotAtBlockPosition(player);
			if (plot != null)
			{
				if (plot.isDenied(player.getName(), false, false))
				{
					player.teleport(PlotManager.getPlotHome(plot));
				}
			}
		}
	}
}
