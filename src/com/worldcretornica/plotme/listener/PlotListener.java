package com.worldcretornica.plotme.listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotDatabase;
import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotMe;
import com.worldcretornica.plotme.PlotPlayer;
import com.worldcretornica.plotme.PlotRoadPopulator;
import com.worldcretornica.plotme.PlotWorld;
import com.worldcretornica.plotme.utils.Pair;

public class PlotListener implements Listener 
{

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) 
	{	
		Player player = event.getPlayer();
		if (player == null) {
			return;
		}
		PlotManager.registerPlotPlayer(player);
		if (player.getWorld() != null && PlotManager.isPlotWorld(player.getWorld()) && !PlotMe.cPerms(player, "plotme.admin.bypassdeny")) {
			Plot plot = PlotManager.getPlotAtLocation(player.getLocation());
			if (plot != null) {
				if (plot.isDenied(player.getName())) {
					player.teleport(player.getWorld().getSpawnLocation());
				}
			}
		}
	}


	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) 
	{
		Player player = event.getPlayer();
		if (player == null) {
			return;
		}

		PlotManager.unregisterPlotPlayer(player);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) 
	{	
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block)) {
			return;
		}
		
		Player player = event.getPlayer();
		if (PlotMe.cPerms(player, "plotme.admin.buildanywhere")) {
			return;
		}

		PlotWorld pwi = PlotManager.getPlotWorld(block.getWorld().getName());
		if (pwi != null) {
			PlotPlayer plotPlayer = PlotManager.getPlotPlayer(player);
			Plot plot = pwi.getPlotAtLocation(block.getLocation());
			if (plot != null && plot.isAllowed(plotPlayer)) {
				plot.setDaysToExpiration(pwi.DaysToExpiration);
				return;
			}
		}
		
		player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		event.setCancelled(true);
	}
	
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block)) {
			return;
		}
		
		Player player = event.getPlayer();
		if (PlotMe.cPerms(player, "plotme.admin.buildanywhere")) {
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block.getWorld().getName());
		if (pwi != null) {
			PlotPlayer plotPlayer = PlotManager.getPlotPlayer(player);
			Plot plot = PlotManager.getPlotAtLocation(block.getLocation());
			if (plot != null && plot.isAllowed(plotPlayer))
			{
				plot.setDaysToExpiration(pwi.DaysToExpiration);
				return;
			}
		}
		
		if (block.getType() == Material.FIRE || block.getType() == Material.SAPLING) {
			event.setCancelled(true);
			return;
		}
		
		player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Location fromloc = event.getFrom();
		Location toloc = event.getTo();
		
		if (fromloc.getBlockX() != toloc.getBlockX() || fromloc.getBlockZ() != toloc.getBlockZ() || fromloc.getWorld() != toloc.getWorld()) {
			if (PlotManager.isPlotWorld(toloc))	{
				Plot plot = PlotManager.getPlotAtLocation(toloc);
				if (plot != null) {
					Player player = event.getPlayer();
					if (player != null)
					{
						PlotWorld pwi = PlotManager.getPlotWorld(player.getWorld().getName());
						if (pwi.isOnRoad(toloc.getBlock()))
						{
							player.sendMessage("ROAD");
						}
						else
						{
							player.sendMessage("PLOT");
						}
						if (plot.isDenied(player.getName()) && !PlotMe.cPerms(player, "plotme.admin.bypassdeny")) {
							event.setCancelled(true);
						}
					}
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block)) {
			return;
		}
		
		Player player = event.getPlayer();
		if (PlotMe.cPerms(player, "plotme.admin.buildanywhere")) {
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block.getWorld().getName());
		if (pwi != null) {
			PlotPlayer plotPlayer = PlotManager.getPlotPlayer(player);
			Plot plot = PlotManager.getPlotAtLocation(block.getLocation());
			if (plot != null && plot.isAllowed(plotPlayer)) {
				plot.setDaysToExpiration(pwi.DaysToExpiration);
				return;
			}
		}

		player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		event.setCancelled(true);
    }
	
	@EventHandler(ignoreCancelled = true)
	public void onPortalCreate(PortalCreateEvent event)
	{
		if (!PlotManager.isPlotWorld(event.getWorld())) {
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(event.getWorld().getName());
		if (pwi == null) {
			event.setCancelled(true);
			return;
		}

		List<Block> blocks = event.getBlocks();
		
		Iterator<Block> blockIterator = blocks.iterator();
		Block block;
		Plot overplot;
		PlotPlayer oldOwner = null;
		PlotPlayer plotOwner;
		
		while (blockIterator.hasNext()) {
			block = blockIterator.next();
			overplot = pwi.getPlotAtLocation(block.getLocation());
			if (overplot != null) {
				plotOwner = overplot.getOwner();
				if (oldOwner == null || !plotOwner.equals(oldOwner)) {
					if (!overplot.isAllowed(oldOwner)) {
						event.setCancelled(true);
						return;
					}
					oldOwner = plotOwner;
				}
			} else {
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onDispense(BlockDispenseEvent event)
	{
		Block fromblock = event.getBlock();
		if (!PlotManager.isPlotWorld(fromblock.getWorld())) {
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(fromblock.getWorld().getName());
		if (pwi == null) {
			event.setCancelled(true);
			return;
		}

		Vector velocity = event.getVelocity();
		int deltax = 0;
		int deltaz = 0;
		if (Math.abs(velocity.getX()) > Math.abs(velocity.getZ())) {
			if (velocity.getX() > 0)
				deltax = 1;
			else
				deltax = -1;	
		} else {
			if (velocity.getZ() > 0) {
				deltaz = 1;
			} else {
				deltaz = -1;
			}
		}

		Block toblock = fromblock.getRelative(deltax, 0, deltaz);

		Plot fromplot = pwi.getPlotAtLocation(fromblock.getLocation());
		Plot toplot = pwi.getPlotAtLocation(toblock.getLocation());

		if (toplot.equals(fromplot) || toplot.isAllowed(fromplot.getOwner())) {
			return;
		}

		event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block)) {
			return;
		}

		PlotWorld pwi = PlotManager.getPlotWorld(block.getWorld().getName());
		if (pwi != null) {
			if (!pwi.DisableIgnition) {
				return;
			}
			Plot plot = pwi.getPlotAtLocation(block.getLocation());
			if (plot != null) {
				if ((!pwi.DisableNetherrackIgnition && block.getType().equals(Material.NETHERRACK)) || (!pwi.DisableObsidianIgnition && block.getType().equals(Material.OBSIDIAN))) {
					return;
				}
			}
		}

		event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
	{
		BlockFace bf = event.getBlockFace();
		BlockState block = event.getBlockClicked().getLocation().add(bf.getModX(), bf.getModY(), bf.getModZ()).getBlock().getState();

		if (!PlotManager.isPlotWorld(block)) {
			return;
		}
        
        Player player = event.getPlayer();
        if (PlotMe.cPerms(player, "plotme.admin.buildanywhere")) {
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block.getWorld().getName());
		if (pwi != null) {
			PlotPlayer plotPlayer = PlotManager.getPlotPlayer(player);
			Plot plot = pwi.getPlotAtLocation(block.getLocation());
			if (plot != null && plot.isAllowed(plotPlayer)) {
				plot.setDaysToExpiration(pwi.DaysToExpiration);
				return;
			}
		}
		
		player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerBucketFill(PlayerBucketFillEvent event)
	{
		Block block = event.getBlockClicked();
        if (!PlotManager.isPlotWorld(block)) {
			return;
		}
        
		Player player = event.getPlayer();
        if (PlotMe.cPerms(player, "plotme.admin.buildanywhere")) {
			return;
		}

		PlotWorld pwi = PlotManager.getPlotWorld(block.getWorld().getName());
		if (pwi != null) {
			PlotPlayer plotPlayer = PlotManager.getPlotPlayer(player);
			Plot plot = pwi.getPlotAtLocation(block.getLocation());
			if (plot != null && plot.isAllowed(plotPlayer)) {
				plot.setDaysToExpiration(plot.getPlotWorld().DaysToExpiration);
				return;
			}
		}
		
		player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		event.setCancelled(true);
	}
	
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Block block = event.getClickedBlock();
		if (!PlotManager.isPlotWorld(block)) {
			return;
		}
		
		Player player = event.getPlayer();
		if (block.getType() != Material.SOIL) {
			if (PlotMe.cPerms(player, "plotme.admin.buildanywhere")) {
				return;
			}
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block.getWorld().getName());
		if (pwi != null) {
			PlotPlayer plotPlayer = PlotManager.getPlotPlayer(player);
			if (event.isBlockInHand() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				BlockFace face = event.getBlockFace();
				Block bblock = block.getWorld().getBlockAt(block.getX() + face.getModX(), block.getY() + face.getModY(), block.getZ() + face.getModZ());
				Plot plot = pwi.getPlotAtLocation(bblock.getLocation());
				if (plot == null || (pwi.isProtectedBlock(bblock) && !plot.isAllowed(plotPlayer)) || (pwi.isPreventedItem(bblock.getType()) && !(PlotMe.cPerms(player, "plotme.unblock." + bblock.getTypeId()) || PlotMe.cPerms(player, "plotme.unblock." + bblock.getTypeId() + "." + String.valueOf(bblock.getData())))))	{
					event.setCancelled(true);
					player.sendMessage(PlotMe.caption("ErrCannotUse"));
					return;
				}
			} else {
				ItemStack is = event.getItem();
				if (!pwi.isPreventedItem((short)is.getTypeId(), is.getData().getData()) || PlotMe.cPerms(player, "plotme.unblock." + block.getTypeId()) || PlotMe.cPerms(player, "plotme.unblock." + is.getTypeId() + "." + String.valueOf(is.getData().getData()))) {
					Plot plot = pwi.getPlotAtLocation(block.getLocation());
					if (plot != null) {
						if (plot.isAllowed(plotPlayer)) {
							plot.setDaysToExpiration(plot.getPlotWorld().DaysToExpiration);
						}
					}
					return;
				}
			}
		}

		player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockSpread(BlockSpreadEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block)) {
			return;
		}
		
		Block sourceblock = event.getSource();
		Plot plot = PlotManager.getPlotAtLocation(block.getLocation());
		Plot sourceplot = PlotManager.getPlotAtLocation(sourceblock.getLocation());
		if (plot==null || sourceplot==null || (plot.equals(sourceplot) && !plot.isAllowed(sourceplot.getOwner()))) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockDamage(BlockDamageEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block))
		{
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block.getWorld().getName());
		if (pwi != null)
		{
			Plot plot = pwi.getPlotAtLocation(block.getLocation());
			if (plot != null)
			{
				return;
			}
		}
		
		event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockFade(BlockFadeEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block))
		{
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block.getWorld().getName());
		if (pwi != null)
		{
			Plot plot = pwi.getPlotAtLocation(block.getLocation());
			if (plot != null)
			{
				return;
			}
		}	
		
		event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockGrow(final BlockGrowEvent event)
	{
		BlockState newblock = event.getNewState();
		if (!PlotManager.isPlotWorld(newblock)) {
			return;
		}

		PlotWorld pwi = PlotManager.getPlotWorld(newblock.getWorld().getName());
		if (pwi != null) {
			Block oldblock = event.getBlock();
			if (oldblock != null) {
				Plot plot = pwi.getPlotAtLocation(newblock.getX(), newblock.getZ());
				if (plot != null) {
					return;
				}
			}	
		}
		
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPistonExtend(final BlockPistonExtendEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block)) {
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block.getWorld().getName());
		if (pwi == null) {
			event.setCancelled(true);
			return;
		}

		Plot fromplot = pwi.getPlotAtLocation(block.getLocation());
		if (fromplot == null) {
			event.setCancelled(true);
			block.breakNaturally();
			block.setType(Material.AIR);
			return;
		}

		BlockFace face = event.getDirection();
		Block overblock = block.getRelative(face);
		Plot overplot = pwi.getPlotAtLocation(overblock.getLocation());
		if (overplot == null) {
			event.setCancelled(true);
			block.breakNaturally();
			block.setType(Material.AIR);
			return;
		}
		
		List<Block> blocks = event.getBlocks();
		if (blocks == null || blocks.isEmpty()) {
			if (!fromplot.equals(overplot) && !overplot.isAllowed(fromplot.getOwner())) {
				event.setCancelled(true);
				block.breakNaturally();
				block.setType(Material.AIR);
				return;
			}
			return;
		}

		Iterator<Block> blockIterator = blocks.iterator();
		while (blockIterator.hasNext())
		{
			overblock = blockIterator.next();
			overplot = pwi.getPlotAtLocation(overblock.getLocation());
			if (overplot==null || (!fromplot.equals(overplot) && !overplot.isAllowed(fromplot.getOwner())))	{
				event.setCancelled(true);
				block.breakNaturally();
				block.setType(Material.AIR);
				return;
			}
		}
		
		overblock = overblock.getRelative(event.getDirection());
		overplot = pwi.getPlotAtLocation(overblock.getLocation());
		if (overplot==null || (!fromplot.equals(overplot) && !overplot.isAllowed(fromplot.getOwner()))) {
			event.setCancelled(true);
			block.breakNaturally();
			block.setType(Material.AIR);
			return;
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPistonRetract(final BlockPistonRetractEvent event)
	{
		if (!event.isSticky()) {
			return;
		}
		
		Location retractloc = event.getRetractLocation();
		if (!PlotManager.isPlotWorld(retractloc)) {
			return;
		}
		
		Block pistonblock = event.getBlock();
		PlotWorld pwi = PlotManager.getPlotWorld(retractloc.getWorld().getName());
		if (pwi != null) {
			Plot retractplot = pwi.getPlotAtLocation(retractloc);
			Plot pistonplot = pwi.getPlotAtLocation(pistonblock.getLocation());

			if (retractplot != null && pistonplot != null && (retractplot.equals(pistonplot) || retractplot.getOwner().equals(pistonplot.getOwner()))) {
				return;
			}
		}
		
		event.setCancelled(true);
		pistonblock.breakNaturally();
	}
	
	
	@EventHandler(ignoreCancelled = true)
	public void onStructureGrow(final StructureGrowEvent event)
	{
		Location location = event.getLocation();
		if (!PlotManager.isPlotWorld(location)) {
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(location.getWorld().getName());
		if (pwi == null) {
			event.setCancelled(true);
			return;
		}
		
		PlotPlayer plotPlayer = null;
		Player player = event.getPlayer();
		Plot fromplot = pwi.getPlotAtLocation(location);
		if (player != null) {
			if (PlotMe.cPerms(player, "plotme.admin.buildanywhere")) {
				return;
			}
			if (event.isFromBonemeal() && pwi.isPreventedItem((short)351, (short)15)) {
				event.setCancelled(true);
				return;
			}
			
			if (fromplot == null) {
				event.setCancelled(true);
				return;
			}

			plotPlayer = PlotManager.getPlotPlayer(player);
			if (!fromplot.isAllowed(plotPlayer)) {
				event.setCancelled(true);
				return;
			}
		}
		
		List<BlockState> blocks = event.getBlocks();
		Iterator<BlockState> bsIterator = blocks.iterator();
		BlockState bs;
		Plot overplot;
		while (bsIterator.hasNext()) {
			bs = bsIterator.next();
			overplot = pwi.getPlotAtLocation(bs.getLocation());
			if (overplot != null) {
				if (!fromplot.equals(overplot) && (!overplot.isAllowed(fromplot.getOwner()))) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityExplode(final EntityExplodeEvent event)
	{	
		
		final Location location = event.getLocation();
		
		if (!PlotManager.isPlotWorld(location))	{
			return;
		}

		PlotWorld pwi = PlotManager.getPlotWorld(location.getWorld().getName());
		if (pwi != null) {
			if (!pwi.DisableExplosion) {
				return;
			}
			List<Block> blockList = event.blockList();
			Iterator<Block> blockIterator = blockList.iterator();
			final Entity entity = event.getEntity();
			Plot entityPlot = pwi.getPlotAtLocation(entity.getLocation());
			while (blockIterator.hasNext()) {
				Block block = blockIterator.next();
				Plot overPlot = pwi.getPlotAtLocation(block.getLocation());
				if (!entityPlot.equals(overPlot) && !overPlot.isAllowed(entityPlot.getOwner())) {
					event.setCancelled(true);
					return;
				}
			}
		}

	}
	
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockIgnite(final BlockIgniteEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block)) {
			return;
		}

		PlotWorld pwi = PlotManager.getPlotWorld(block.getWorld().getName());
		if (pwi == null) {
			return;
		}
			
		if (!pwi.DisableIgnition) {
			return;
		}
			
		Player player = event.getPlayer();
		PlotPlayer plotPlayer = PlotManager.getPlotPlayer(player);
		if (PlotMe.cPerms(player, "plotme.admin.buildanywhere")) {
			return;
		}
		
		Plot plot = pwi.getPlotAtLocation(block.getLocation());
		if (plot != null) {
			if (plot.getOwner().equals(plotPlayer) || plot.isAllowed(plotPlayer)) {
				if ((!pwi.DisableNetherrackIgnition && block.getType().equals(Material.NETHERRACK)) || (!pwi.DisableObsidianIgnition && block.getType().equals(Material.OBSIDIAN))) {
					return;
				}
			}
		}
		
		event.setCancelled(true);
	}
	
	
	@EventHandler(ignoreCancelled = true)
	public void onHangingPlace(final HangingPlaceEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block)) {
			return;
		}
		
		Player player = event.getPlayer();
		if (player == null) {
			event.setCancelled(true);
			return;
		}
		
		if (PlotMe.cPerms(player, "plotme.admin.buildanywhere")) {
			return;
		}

		Location location = block.getLocation();
		PlotWorld pwi = PlotManager.getPlotWorld(location.getWorld().getName());
		if (pwi != null) {
			Plot plot = pwi.getPlotAtLocation(location);
			if (plot != null) {
				if (plot.isAllowed(PlotManager.getPlotPlayer(player))) {
					plot.setDaysToExpiration(pwi.DaysToExpiration);
					return;
				}
			}
		}
		
		player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		
		event.setCancelled(true);
	}
	
	
	@EventHandler(ignoreCancelled = true)
	public void onHangingBreakByEntity(final HangingBreakByEntityEvent event)
	{
		Location location = event.getEntity().getLocation();
		if (!PlotManager.isPlotWorld(location))	{
			return;
		}
		
		Entity entity = event.getRemover();
		if (entity == null || !(entity instanceof Player)) {
			event.setCancelled(true);
			return;
		}
		
		Player player = (Player)entity;

		if (PlotMe.cPerms(player, "plotme.admin.buildanywhere")) {
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(location.getWorld().getName());
		if (pwi != null) {
			Plot plot = pwi.getPlotAtLocation(location);
			if (plot != null) {
				if (plot.isAllowed(PlotManager.getPlotPlayer(player))) {
					plot.setDaysToExpiration(pwi.DaysToExpiration);
					return;
				}
			}
			player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		}
		
		event.setCancelled(true);
	}

	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractEntity(final PlayerInteractEntityEvent event)
	{
		Location location = event.getRightClicked().getLocation();
		if (!PlotManager.isPlotWorld(location)) {
			return;
		}
		
		Player player = event.getPlayer();
		if (PlotMe.cPerms(player, "plotme.admin.buildanywhere")) {
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(location.getWorld().getName());
		if (pwi != null) {
			Plot plot = pwi.getPlotAtLocation(location);
			if (plot != null) {
				if (plot.isAllowed(PlotManager.getPlotPlayer(player))) {
					plot.setDaysToExpiration(pwi.DaysToExpiration);
					return;
				}
			}
		}
		player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		
		event.setCancelled(true);
	}

	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerEggThrow(final PlayerEggThrowEvent event)
	{
		Location location = event.getEgg().getLocation();
		if (!PlotManager.isPlotWorld(location)) {
			return;
		}
		
		Player player = event.getPlayer();
		if (PlotMe.cPerms(player, "plotme.admin.buildanywhere")) {
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(location.getWorld().getName());
		if (pwi != null) {
			Plot plot = pwi.getPlotAtLocation(location);
			if (plot != null) {
				if (plot.isAllowed(PlotManager.getPlotPlayer(player))) {
					plot.setDaysToExpiration(pwi.DaysToExpiration);
					return;
				}
			}
			player.sendMessage(PlotMe.caption("ErrCannotUseEggs"));
		}
		
		event.setHatching(false);
	}


	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerTeleport(final PlayerTeleportEvent event)
	{
		Player player = event.getPlayer();
		if (player == null) {
			event.setCancelled(true);
			return;
		}
		
		Location fromloc = event.getFrom();
		Location toloc = event.getTo();
		
		if (fromloc == null || toloc == null || fromloc.getWorld() != toloc.getWorld() || fromloc.getBlockX() != toloc.getBlockX() || fromloc.getBlockZ() != toloc.getBlockZ()) {
			Plot fromPlot = PlotManager.getPlotAtLocation(fromloc);
			if (PlotManager.isPlotWorld(toloc.getWorld()) && !PlotMe.cPerms(player, "plotme.admin.bypassdeny")) {
				Plot plot = PlotManager.getPlotAtLocation(toloc);
				if (plot != null && plot.isDenied(PlotManager.getPlotPlayer(player))) {
					event.setTo(PlotManager.getPlotHome(fromPlot));
				}
			}
		}
	}


	@EventHandler(priority = EventPriority.LOWEST)
	public void onWorldLoad(WorldLoadEvent event) 
	{
		if (event.getWorld()==null)
		{
			return;
		}

		PlotWorld pwi = PlotManager.getPlotWorld(event.getWorld().getName());
		if (pwi == null)
		{
			return;
		}
		
		for (BlockPopulator pop : event.getWorld().getPopulators())
		{
			if (pop instanceof PlotRoadPopulator)
			{
				return;
			}
		}

		if (pwi != null) {
			event.getWorld().getPopulators().add(new PlotRoadPopulator());
		}
	}

	
}
