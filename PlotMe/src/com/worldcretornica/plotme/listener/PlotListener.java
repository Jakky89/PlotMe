package com.worldcretornica.plotme.listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotMe;
import com.worldcretornica.plotme.PlotWorld;
import com.worldcretornica.plotme.utils.Pair;

public class PlotListener implements Listener 
{
	
	HashMap<Block, Pair<Integer, Long>> redstoneCurrentHitCounts;
	
	public PlotListener()
	{
		redstoneCurrentHitCounts = new HashMap<Block, Pair<Integer, Long>>();
	}

	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(final BlockBreakEvent event) 
	{	
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block))
		{
			return;
		}
		
		Player player = event.getPlayer();
		if (PlotMe.cPerms(player, "plotme.admin.buildanywhere"))
		{
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block);
		if (pwi != null)
		{
			Plot plot = pwi.getPlotAtBlockPosition(block);
			if (plot != null && plot.isAllowed(player.getName(), true, true))
			{
				plot.resetExpiration(pwi.DaysToExpiration);
				return;
			}
		}
		
		player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		event.setCancelled(true);
	}
	
	
	@EventHandler(priority = EventPriority.HIGH) //, ignoreCancelled = true
	public void onBlockPlace(final BlockPlaceEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block))
		{
			return;
		}
		
		Player player = event.getPlayer();
		if (PlotMe.cPerms(player, "plotme.admin.buildanywhere"))
		{
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block);
		if (pwi != null)
		{
			Plot plot = PlotManager.getPlotAtBlockPosition(block);
			if (plot != null && plot.isAllowed(player.getName(), true, true))
			{
				plot.resetExpiration(pwi.DaysToExpiration);
				return;
			}
		}
		
		player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block))
		{
			return;
		}
		
		Player player = event.getPlayer();
		if (PlotMe.cPerms(player, "plotme.admin.buildanywhere"))
		{
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block);
		if (pwi != null)
		{
			Plot plot = PlotManager.getPlotAtBlockPosition(block);
			if (plot != null && plot.isAllowed(player.getName(), true, true))
			{
				plot.resetExpiration(pwi.DaysToExpiration);
				return;
			}
		}

		player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		event.setCancelled(true);
    }

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockRedstoneChange(BlockRedstoneEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block))
		{
			return;
		}
		PlotWorld pwi = PlotManager.getPlotWorld(block);
		if (pwi != null)
		{
			if (!pwi.PreventHighFrequencyRedstoneCircuits)
			{
				return;
			}
			
			Plot plot = pwi.getPlotAtBlockPosition(block);
			if (plot != null)
			{
				Pair<Integer, Long> newHitCount = null;
				long currentTime = Math.round(System.currentTimeMillis() / 1000);
				Pair<Integer, Long> oldHitCount = redstoneCurrentHitCounts.get(block);
				if (oldHitCount != null)
				{
					if (currentTime > (oldHitCount.getRight() - 1))
					{
						if (oldHitCount.getLeft() < 5)
						{
							newHitCount = new Pair<Integer, Long>(oldHitCount.getLeft() + 1, currentTime);
						}
						else
						{
							event.setNewCurrent(0);
							redstoneCurrentHitCounts.remove(block);
							block.breakNaturally();
							return;
						}
					}
				}
				else
				{
					newHitCount = new Pair<Integer, Long>(0, currentTime);
				}
				if (newHitCount != null)
				{
					redstoneCurrentHitCounts.put(block, newHitCount);
				}
			}
		}
    }
	
	/*@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block))
		{
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block);
		if (pwi != null && !pwi.isOnRoad(block))
		{
			Dispenser disp = (Dispenser)event.getBlock().getState().getData();
			
			Vector velocity = event.getVelocity();
			

		}
    }*/
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block))
		{
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block);
		/**
		 * TODO: maybe send message to player that it burns on his plot
		 */
		/*PlotWorld pwi = PlotManager.getPlotWorld(block);
		if (pwi != null && !pwi.isOnRoad(block))
		{
			Plot plot = PlotManager.getPlotAtBlockPosition(block);
		}*/

		if (pwi != null)
		{
			if (!pwi.DisableIgnition)
			{
				return;
			}
			Plot plot = pwi.getPlotAtBlockPosition(block);
			if (plot != null)
			{
				if ((!pwi.DisableNetherrackIgnition && block.getType().equals(Material.NETHERRACK)) || (!pwi.DisableObsidianIgnition && block.getType().equals(Material.OBSIDIAN)))
				{
					return;
				}
			}
		}

		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event)
	{
		BlockFace bf = event.getBlockFace();
		BlockState block = event.getBlockClicked().getLocation().add(bf.getModX(), bf.getModY(), bf.getModZ()).getBlock().getState();

        if (!PlotManager.isPlotWorld(block))
		{
			return;
		}
        
        Player player = event.getPlayer();

        if (PlotMe.cPerms(player, "plotme.admin.buildanywhere"))
		{
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block);
		if (pwi != null)
		{
			Plot plot = pwi.getPlotAtBlockPosition(block);
			if (plot != null && plot.isAllowed(player.getName(), true, true))
			{
				plot.resetExpiration(pwi.DaysToExpiration);
				return;
			}
		}
		
		player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerBucketFill(final PlayerBucketFillEvent event)
	{
		Block block = event.getBlockClicked();
        if (!PlotManager.isPlotWorld(block))
		{
			return;
		}
        
		Player player = event.getPlayer();
		
        if (PlotMe.cPerms(player, "plotme.admin.buildanywhere"))
		{
			return;
		}

		PlotWorld pwi = PlotManager.getPlotWorld(block);
		if (pwi != null)
		{
			Plot plot = pwi.getPlotAtBlockPosition(block);
			if (plot != null && plot.isAllowed(player.getName(), true, true))
			{
				plot.resetExpiration(plot.getPlotWorld().DaysToExpiration);
				return;
			}
		}
		
		player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		event.setCancelled(true);
	}
	
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerInteract(final PlayerInteractEvent event)
	{
		Block block = event.getClickedBlock();
		if (!PlotManager.isPlotWorld(block))
		{
			return;
		}
		
		Player player = event.getPlayer();
		
		if (block.getType() != Material.SOIL)
		{
			if (PlotMe.cPerms(player, "plotme.admin.buildanywhere"))
			{
				return;
			}
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block);
		if (pwi != null)
		{
			if (event.isBlockInHand() && event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				BlockFace face = event.getBlockFace();
				Block bblock = block.getWorld().getBlockAt(block.getX() + face.getModX(), block.getY() + face.getModY(), block.getZ() + face.getModZ());
				Plot plot = pwi.getPlotAtBlockPosition(bblock);
				if (plot == null || (pwi.isProtectedBlock(bblock) && !plot.isAllowed(player.getName(), true, true)) || (pwi.isPreventedItem(bblock) && !(PlotMe.cPerms(player, "plotme.unblock." + bblock.getTypeId()) || PlotMe.cPerms(player, "plotme.unblock." + bblock.getTypeId() + "." + String.valueOf(bblock.getData())))))
				{
					event.setCancelled(true);
					player.sendMessage(PlotMe.caption("ErrCannotUse"));
					return;
				}
			}
			else
			{
				ItemStack is = event.getItem();
				if (!pwi.isPreventedItem(is.getTypeId(), (byte)is.getData().getData()) || PlotMe.cPerms(player, "plotme.unblock." + block.getTypeId()) || PlotMe.cPerms(player, "plotme.unblock." + is.getTypeId() + "." + String.valueOf(is.getData().getData())))
				{
					Plot plot = pwi.getPlotAtBlockPosition(block);
					if (plot != null)
					{
						if (plot.isAllowed(player.getName(), true, true))
						{
							plot.resetExpiration(plot.getPlotWorld().DaysToExpiration);
						}
					}
					return;
				}
			}
		}

		player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockSpread(final BlockSpreadEvent event)
	{
		Plot plot = PlotManager.getPlotAtBlockPosition(event.getBlock());
		if (plot != null)
		{			
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockForm(final BlockFormEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block))
		{
			return;
		}

		PlotWorld pwi = PlotManager.getPlotWorld(block.getWorld());
		if (pwi != null)
		{
			Plot plot = pwi.getPlotAtBlockPosition(block);
			if (plot != null)
			{
				return;
			}
		}
		
		event.setCancelled(true);
	}
	

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockDamage(final BlockDamageEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block))
		{
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block.getWorld());
		if (pwi != null)
		{
			Plot plot = pwi.getPlotAtBlockPosition(block);
			if (plot != null)
			{
				return;
			}
		}
		
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockFade(final BlockFadeEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block))
		{
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block);
		if (pwi != null)
		{
			Plot plot = pwi.getPlotAtBlockPosition(block);
			if (plot != null)
			{
				return;
			}
		}	
		
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockFromTo(final BlockFromToEvent event)
	{
		Block toblock = event.getToBlock();
		if (!PlotManager.isPlotWorld(toblock))
		{
			return;
		}
		
		Block fromblock = event.getBlock();

		PlotWorld pwi = PlotManager.getPlotWorld(fromblock);
		if (pwi != null && fromblock != null && toblock != null)
		{
			Plot fromplot = pwi.getPlotAtBlockPosition(fromblock);
			Plot toplot = pwi.getPlotAtBlockPosition(toblock);
			if (!pwi.isOnRoad(toblock) && fromplot != null && toplot != null)
			{
				if (fromplot.equals(toplot) || fromplot.getOwner().equals(toplot.getOwner()))
				{
					return;
				}
			}
		}

		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockGrow(final BlockGrowEvent event)
	{
		BlockState newblock = event.getNewState();
		if (!PlotManager.isPlotWorld(newblock))
		{
			return;
		}

		PlotWorld pwi = PlotManager.getPlotWorld(newblock);
		if (pwi != null)
		{
			Block oldblock = event.getBlock();
			if (oldblock != null)
			{
				Plot plot = pwi.getPlotAtBlockPosition(newblock.getX(), newblock.getZ());
				if (plot != null)
				{
					return;
				}
			}	
		}
		
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockPistonExtend(final BlockPistonExtendEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block))
		{
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(block);
		if (pwi == null)
		{
			event.setCancelled(true);
			block.breakNaturally();
			return;
		}
		
		List<Block> blocks = event.getBlocks();
		if (blocks == null || blocks.isEmpty())
		{
			return;
		}
		
		BlockFace face = event.getDirection();
		

		Plot fromplot = pwi.getPlotAtBlockPosition(block);
		
		Iterator<Block> blockIterator = blocks.iterator();
		Block nxtblock;
		Location nxtloc;
		Plot overplot;
		while (blockIterator.hasNext())
		{
			nxtblock = blockIterator.next();
			if (nxtblock != null)
			{
				nxtloc = nxtblock.getLocation().add(face.getModX(), face.getModY(), face.getModZ());
			
				if (pwi.isOnRoad(nxtloc.getX(), nxtloc.getZ()))
				{
					event.setCancelled(true);
					block.breakNaturally();
					return;
				}
			
				overplot = pwi.getPlotAtBlockPosition(nxtloc);
				if (overplot != null)
				{
					if (fromplot == null || overplot == null || (!fromplot.equals(overplot) && !fromplot.getOwner().equals(overplot.getOwner())))
					{
						event.setCancelled(true);
						block.breakNaturally();
						return;
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockPistonRetract(final BlockPistonRetractEvent event)
	{
		Location retractloc = event.getRetractLocation();
		if (!PlotManager.isPlotWorld(retractloc))
		{
			return;
		}
		
		Block pistonblock = event.getBlock();
		PlotWorld pwi = PlotManager.getPlotWorld(retractloc);
		if (pwi != null)
		{
			Plot retractplot = pwi.getPlotAtBlockPosition(retractloc);
			Plot pistonplot = pwi.getPlotAtBlockPosition(pistonblock);
			
			if (retractplot != null && pistonplot != null && (retractplot.equals(pistonplot) || retractplot.getOwner().equals(pistonplot.getOwner())))
			{
				return;
			}
		}
		
		event.setCancelled(true);
		pistonblock.breakNaturally();
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onStructureGrow(final StructureGrowEvent event)
	{
		Location location = event.getLocation();
		if (!PlotManager.isPlotWorld(location))
		{
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(location);
		if (pwi == null)
		{
			event.setCancelled(true);
			return;
		}
		
		Player player = event.getPlayer();
		if (player != null)
		{
			if (PlotMe.cPerms(player, "plotme.admin.buildanywhere"))
			{
				return;
			}
		}

		if (event.isFromBonemeal() && pwi.isPreventedItem(351, (byte)15))
		{
			event.setCancelled(true);
			return;
		}
		
		List<BlockState> blocks = event.getBlocks();
		if (blocks == null || blocks.isEmpty())
		{
			event.setCancelled(true);
			return;
		}
		
		Plot fromplot = pwi.getPlotAtBlockPosition(location);
		if (fromplot == null)
		{
			event.setCancelled(true);
			return;
		}
		
		Iterator<BlockState> bsIterator = blocks.iterator();
		BlockState bs;
		Plot overplot;
		while (bsIterator.hasNext())
		{
			bs = bsIterator.next();
			if (pwi.isOnRoad(bs))
			{
				event.setCancelled(true);
				return;
			}
			overplot = pwi.getPlotAtBlockPosition(bs);
			if (overplot != null)
			{
				if (player != null && player.getName() != null)
				{
					if (!fromplot.isAllowed(player.getName(),  true,  true) || !overplot.isAllowed(player.getName(), true, true))
					{
						event.setCancelled(true);
						return;
					}
				}
				else
				{
					if (!fromplot.equals(overplot) && !fromplot.getOwner().equals(overplot.getOwner()))
					{
						event.setCancelled(true);
						return;
					}
				}
			}
			else
			{
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityExplode(final EntityExplodeEvent event)
	{	
		
		final Location location = event.getLocation();
		if (!PlotManager.isPlotWorld(location))
		{
			return;
		}
		
		
		final Entity entity = event.getEntity();
		List<Block> blockList = event.blockList();
		

		PlotWorld pwi = PlotManager.getPlotWorld(location);
		if (pwi != null)
		{
			
			if (!pwi.DisableExplosion)
			{
				return;
			}
			
			Iterator<Block> blockIterator = blockList.iterator();
			while (blockIterator.hasNext())
			{
				/**
				 * TODO: allow owners to blow up their plots (for building reasons or so)
				 */
			}
		}

		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockIgnite(final BlockIgniteEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block))
		{
			return;
		}

		PlotWorld pwi = PlotManager.getPlotWorld(block);
		if (pwi == null)
		{
			return;
		}
			
		if (!pwi.DisableIgnition)
		{
			return;
		}
			
		Player player = event.getPlayer();
		if (PlotMe.cPerms(player, "plotme.admin.buildanywhere"))
		{
			return;
		}
		
		if (!pwi.isOnRoad(block))
		{
			if ((!pwi.DisableNetherrackIgnition && block.getType().equals(Material.NETHERRACK)) || (!pwi.DisableObsidianIgnition && block.getType().equals(Material.OBSIDIAN)))
			{
				Plot plot = pwi.getPlotAtBlockPosition(block);
				if (plot != null)
				{
					if (plot.getOwnerRealName() == player.getName() || plot.isAllowed(player.getName(), true, true))
					{
						plot.resetExpiration(pwi.DaysToExpiration);
						return;
					}
				}
			}
		}	
		
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onHangingPlace(final HangingPlaceEvent event)
	{
		Block block = event.getBlock();
		if (!PlotManager.isPlotWorld(block))
		{
			return;
		}

		Location location = block.getLocation();
		
		PlotWorld pwi = PlotManager.getPlotWorld(location);
		if (pwi == null)
		{
			return;
		}
		
		Player player = event.getPlayer();
		if (PlotMe.cPerms(player, "plotme.admin.buildanywhere"))
		{
			return;
		}

		if (!pwi.isOnRoad(location))
		{
			Plot plot = pwi.getPlotAtBlockPosition(location);
			if (plot != null)
			{
				if (plot.isAllowed(player.getName(), true, true))
				{
					plot.resetExpiration(pwi.DaysToExpiration);
					return;
				}
			}
		}
		
		player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onHangingBreakByEntity(final HangingBreakByEntityEvent event)
	{
		Entity entity = event.getRemover();
		if (entity == null)
		{
			return;
		}
		
		Location location = event.getEntity().getLocation();
		if (!PlotManager.isPlotWorld(location))
		{
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(location);
		if (pwi == null)
		{
			return;
		}
		
		if (entity instanceof Player)
		{
			Player player = (Player)entity;
						
			if (PlotMe.cPerms(player, "plotme.admin.buildanywhere"))
			{
				return;
			}

			if (!pwi.isOnRoad(location))
			{
				Plot plot = pwi.getPlotAtBlockPosition(location);
				if (plot != null)
				{
					if (plot.isAllowed(player.getName(), true, true))
					{
						plot.resetExpiration(pwi.DaysToExpiration);
						return;
					}
				}
			}
			
			player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		}
		
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerInteractEntity(final PlayerInteractEntityEvent event)
	{
		Location location = event.getRightClicked().getLocation();
		if (!PlotManager.isPlotWorld(location))
		{
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(location);
		if (pwi == null)
		{
			return;
		}
		
		Player player = event.getPlayer();
		if (PlotMe.cPerms(player, "plotme.admin.buildanywhere"))
		{
			return;
		}

		if (!pwi.isOnRoad(location))
		{
			Plot plot = pwi.getPlotAtBlockPosition(location);
			if (plot != null)
			{
				if (plot.isAllowed(player.getName(), true, true))
				{
					plot.resetExpiration(pwi.DaysToExpiration);
					return;
				}
			}
		}
		
		player.sendMessage(PlotMe.caption("ErrCannotBuild"));
		
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerEggThrow(final PlayerEggThrowEvent event)
	{
		Location location = event.getEgg().getLocation();
		if (!PlotManager.isPlotWorld(location))
		{
			return;
		}
		
		PlotWorld pwi = PlotManager.getPlotWorld(location);
		if (pwi == null)
		{
			return;
		}
		
		Player player = event.getPlayer();
		if (PlotMe.cPerms(player, "plotme.admin.buildanywhere"))
		{
			return;
		}

		if (!pwi.isOnRoad(location))
		{
			Plot plot = pwi.getPlotAtBlockPosition(location);
			if (plot != null)
			{
				if (plot.isAllowed(player.getName(), true, true))
				{
					plot.resetExpiration(pwi.DaysToExpiration);
					return;
				}
			}
		}
		
		player.sendMessage(PlotMe.caption("ErrCannotUseEggs"));
		
		event.setHatching(false);
	}
	
/*
	@EventHandler
	public void onWorldInit(WorldInitEvent event) 
	{
		World w = event.getWorld();
		
		if (w.getName().equalsIgnoreCase("TestWorld"))
		{
			for (BlockPopulator pop : w.getPopulators()) 
			{
				if ((pop instanceof PlotRoadPopulator)) 
				{
					return;
				}
			}
			
			PlotMapInfo pmi = PlotManager.getMap(w);
			
			if(pmi == null)
			{
				w.getPopulators().add(new PlotRoadPopulator());
			}else{
				w.getPopulators().add(new PlotRoadPopulator(pmi));
			}
		}
	}*/
}
