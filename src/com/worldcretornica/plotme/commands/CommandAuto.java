package com.worldcretornica.plotme.commands;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotMe;
import com.worldcretornica.plotme.PlotPlayer;
import com.worldcretornica.plotme.PlotWorld;

public class CommandAuto extends PlotMeCommandBase
{

	@Override
	public boolean doExecute(CommandSender sender, String[] args)
	{
		if (sender instanceof Player)
		{
			Player player = (Player)sender;
			if (!PlotMe.cPerms(player, "plotme.use.auto"))
			{
				Send(sender, ChatColor.RED + C("MsgPermissionDenied"));
				return true;
			}
			if (!PlotManager.isPlotWorld(player))
			{
				Send(player, ChatColor.RED + C("MsgNotPlotWorld"));
				return true;
			}
			World w = null;
			if (args.length == 2)
			{
				w = Bukkit.getServer().getWorld(args[1]);
			}
			if (w == null || !PlotManager.isPlotWorld(w))
			{
				Send(player, ChatColor.RED + args[1] + " " + C("MsgWorldNotPlot"));
				return true;
			}
			w = player.getWorld();
			if (w == null)
			{
				Send(player, ChatColor.RED + C("MsgNoPlotworldFound"));
				return true;
			}
			PlotWorld pwi = PlotManager.getPlotWorld(w.getName());
			PlotPlayer ppl = PlotManager.getPlotPlayer(player);
			int plc = ppl.getOwnPlotsCount();
			if (plc >= PlotMe.getPlotLimit(player) && !PlotMe.cPerms(player, "plotme.admin"))
			{
				Send(player, ChatColor.RED + C("MsgAlreadyReachedMaxPlots") + " (" + 
				String.valueOf(plc) + "/" + PlotMe.getPlotLimit(player) + "). " + C("WordUse") + " " + ChatColor.RED + "/plot " + C("CommandHome") + ChatColor.RESET + " " + C("MsgToGetToIt"));
				return true;
			}
			int limit = pwi.PlotAutoLimit;
			if (limit <= 0)
				return false;
			Location spawnLoc = pwi.getMinecraftWorld().getSpawnLocation();
			int offX = spawnLoc.getBlockX();
			int offZ = spawnLoc.getBlockZ();
			Plot plot = null;
			List<Plot> availablePlots = new LinkedList<Plot>();
			for (int i = 1; i < limit; i++) {
				for (int x = -i; x < i; x++) {
					plot = pwi.getPlotAtPlotPosition(offX + x, offZ - i);
					if (plot != null && plot.isAvailable()) {
						availablePlots.add(plot);
					}
					plot = pwi.getPlotAtPlotPosition(offX + x, offZ + i);
					if (plot != null && plot.isAvailable()) {
						availablePlots.add(plot);
					}
				}
				for (int z = -i; z < i; z++) {
					plot = pwi.getPlotAtPlotPosition(offX + i, offZ + z);
					if (plot != null && plot.isAvailable()) {
						availablePlots.add(plot);
					}
					plot = pwi.getPlotAtPlotPosition(offX - i, offZ + z);
					if (plot != null && plot.isAvailable()) {
						availablePlots.add(plot);
					}
				}
			}
			
			if (availablePlots.size() > 0) {
				Plot assigned = null;
				boolean again = false;
				double price = availablePlots.get(0).getClaimPrice();
				double balance = PlotMe.economy.getBalance(player.getName());
				do {
					Iterator<Plot> plotIterator = availablePlots.iterator();
					if (PlotManager.isEconomyEnabled(w)) {
						while (plotIterator.hasNext()) {
							plot = plotIterator.next();
							price = plot.getClaimPrice();
							if (price <= balance)
							{
								assigned = plot;
								break;
							}
						}
					} else {
						assigned = plotIterator.next();
					}
					if (assigned == null && price > balance) {
						Send(player, ChatColor.RED + C("MsgNotEnoughAuto") + " " + C("WordMissing") + " " + ChatColor.RESET + f(price - balance, false));
						return true;
					} else if (assigned != null) {
							break;
					} else {
						EconomyResponse er = PlotMe.economy.withdrawPlayer(player.getName(), price);
						if (er.transactionSuccess()) {
							assigned = plot;
							int centerX = (int)Math.round((plot.getPlotX() * pwi.getPlotBlockPositionMultiplier()) + (plot.getPlotSize() / 2));
							int centerZ = (int)Math.round((plot.getPlotZ() * pwi.getPlotBlockPositionMultiplier()) + (plot.getPlotSize() / 2));
							if (player.teleport(new Location(w, centerX, w.getHighestBlockAt(centerX, centerZ).getY(), centerZ))) {
								Send(player, C("MsgThisPlotYours") + " " + C("WordUse") + " " + ChatColor.RED + "/plotme " + C("CommandHome") + ChatColor.RESET + " " + C("MsgToGetToIt") + " " + f(-price));
								PlotMe.logger.info(player.getName() + " " + C("MsgClaimedPlot") + " " + String.valueOf(plot.getId()) + ((price != 0) ? " " + C("WordFor") + " " + price : ""));
								again = false;
							} else {
								again = true;
							}
						}
					}
				} while (again);
				Send(player, ChatColor.RED + C("MsgNoPlotFound1") + " " + (limit^2) + " " + C("MsgNoPlotFound2"));
			}
		}
		return true;
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return null;
	}

}
