package com.worldcretornica.plotme.commands;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotAuctionBid;
import com.worldcretornica.plotme.PlotDatabase;
import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotMe;
import com.worldcretornica.plotme.PlotPlayer;
import com.worldcretornica.plotme.PlotWorld;
import com.worldcretornica.plotme.commands.CommandMovePlot;
import com.worldcretornica.plotme.utils.MinecraftFontWidthCalculator;
import com.worldcretornica.plotme.utils.FinishedPlotsComparator;
import com.worldcretornica.plotme.utils.Pair;


import org.bukkit.Bukkit;


public class PlotMeCommands implements CommandExecutor
{
	
	public static CommandHelp helpCommand;

	public static Map<String, PlotMeCommandBase> commands;
	
	public PlotMeCommands(PlotMe pl)
	{
		commands = new HashMap<String, PlotMeCommandBase>();
		
		// USER COMMANDS
		commands.put(C("CommandHelp"), new CommandHelp());
		commands.put(C("CommandAuto"), new CommandAuto());
		commands.put(C("CommandClaim"), new CommandClaim());
		
		// ADMIN COMMANDS
		commands.put(C("CommandMove"), new CommandMovePlot());
		commands.put(C("CommandReload"), new CommandReload());
		commands.put(C("CommandReset"), null); /** TODO **/
		commands.put(C("CommandResetExpired"), new CommandResetExpired());

	}

	public boolean onCommand(CommandSender sender, Command command, String start, String[] args)
	{
		if (sender == null || command == null)
		{
			return true;
		}

		String cmd = command.getName().toLowerCase();
		if (cmd.equals("plot") || cmd.equals("plotme") || cmd.equals("pme"))
		{
			if (args.length == 0)
			{
				helpCommand.doExecute(sender, args);
			}
			else if (args.length >= 1)
			{
				String subcmd = args[0].toString().toLowerCase();
				
				/**
				 * MAIN COMMAND EXECUTOR
				 */
				PlotMeCommandBase cmdClass = commands.get(subcmd);
				if (cmdClass != null)
				{
					cmdClass.doExecute(sender, args);
					return true;
				}

				/**
				 * TODO: see below (convert to command classes)
				 */
				if (sender instanceof Player)
				{
					Player player = (Player)sender;

					if (subcmd.equals(C("CommandClaim")))
					{
						/**
						 * TODO: FIRST CONFIGURABLE X PLOTS ARE FOR FREE (after that it costs something)
						 */
						return claim(player, args);
					}
					if (subcmd.equals(C("CommandInfo")) || subcmd.equals("i"))
					{
						return info(player, args);
					}
					if (subcmd.equals(C("CommandAddtime")))
					{
						return addtime(player, args);
					}
					if (subcmd.equals(C("CommandDone")))
					{
						return done(player, args);
					}
					if (subcmd.equals(C("CommandComment")))
					{
						return comment(player, args);
					}
					if (subcmd.equals(C("CommandTp")))
					{
						return tp(player, args);
					}
					if (subcmd.equals(C("CommandBuy")))
					{
						return buy(player, args);
					}
					if (subcmd.equals(C("CommandBid")))
					{
						return bid(player, args);
					}
					/*if (subcmd.startsWith(C("CommandHome")) || subcmd.startsWith("h"))
					{
						return home(player, args);
					}*/
					if (subcmd.equals(C("CommandSell")))
					{
						return sell(player, args);
					}
					if (subcmd.equals(C("CommandBiome")) || subcmd.equals("b"))
					{
						return biome(player, args);
					}
					/*if (subcmd.equals(C("CommandBiomelist")))
					{
						return biomelist(sender, args);
					}
					if (subcmd.equals(C("CommandClear")))
					{
						return clear(player, args);
					}
					if (subcmd.equals(C("CommandAdd")) || subcmd.equals("+"))
					{
						return add(player, args);
					}
					if (subcmd.equals(C("CommandDeny")))
					{
						return deny(player, args);
					}
					if (subcmd.equals(C("CommandUndeny")))
					{
						return undeny(player, args);
					}
					if (subcmd.equals(C("CommandRemove")) || subcmd.equals("-"))
					{
						return remove(player, args);
					}
					if (subcmd.equals(C("CommandSetowner")) || subcmd.equals("o"))
					{
						return setowner(player, args);
					}*/
					/*if (subcmd.equals(C("CommandMove")) || subcmd.equals("m"))
					{
						return move(sender, args);
					}*/
					/*if (subcmd.equals(C("CommandWEAnywhere")))
					{
						return weanywhere(player, args);
					}
					if (subcmd.equals(C("CommandProtect")))
					{
						return protect(player, args);
					}*/
				}

				/*if (subcmd.equals(C("CommandComments")) || subcmd.equals("c"))
				{
					return comments(sender, args);
				}*/
				if (subcmd.equals(C("CommandId")))
				{
					return id(sender, args);
				}
				/*if (subcmd.equals(C("CommandList")))
				{
					return plotlist(sender, args);
				}*/
				/*if (subcmd.equals(C("CommandExpired")))
				{
					return expired(sender, args);
				}*/
				if (subcmd.equals(C("CommandDoneList")))
				{
					return donelist(sender, args);
				}
				if (subcmd.equals(C("CommandDispose")))
				{
					return dispose(sender, args);
				}
				if (subcmd.equals(C("CommandAuction")))
				{
					return auction(sender, args);
				}
			}
			else
			{
				if (args.length == 0 || args[0].equalsIgnoreCase("1"))
				{
					sender.sendMessage(C("ConsoleHelpMain")); 
					sender.sendMessage(" - /plotme reload");
					sender.sendMessage(C("ConsoleHelpReload"));
					return true;
				}
			}
		}
		return false;
	}


		private boolean bid(Player bidder, String[] args) 
		{	
			if (PlotManager.isEconomyEnabled(bidder)) {
				if (PlotMe.cPerms(bidder, "plotme.use.bid")) {
					Plot plot = PlotManager.getPlotAtLocation(bidder.getLocation());
					if (plot == null) {
						Send(bidder, ChatColor.RED + C("MsgNoPlotFound"));
					} else {
						if (plot.isAuctioned())	{
							if (plot.getOwner().getName().equalsIgnoreCase(bidder.getName())) {
								Send(bidder, ChatColor.RED + C("MsgCannotBidOwnPlot"));
							} else {
								if (args.length == 2) {
									double bid = 0;
									try {  
										bid = Double.parseDouble(args[1]);  
									} catch( Exception ex) {
										return false;
									}
									PlotAuctionBid highestBid = plot.getHighestAuctionBid();
									if (bid <= highestBid.getMoneyAmount() && highestBid != null) {
										Send(bidder, ChatColor.RED + C("MsgInvalidBidMustBeAbove") + " " + ChatColor.RESET + f(highestBid.getMoneyAmount(), false));
									} else {
										double balance = PlotMe.economy.getBalance(bidder.getName());
										if (bid > balance || (highestBid != null && balance < highestBid.getMoneyAmount())) {
											Send(bidder, ChatColor.RED + C("MsgNotEnoughBid"));
										} else {
											EconomyResponse er = PlotMe.economy.withdrawPlayer(bidder.getName(), bid);
											if (er.transactionSuccess()) {
												if (highestBid != null) {
													EconomyResponse er2 = PlotMe.economy.depositPlayer(highestBid.getBidderName(), highestBid.getMoneyAmount());
													if (!er2.transactionSuccess()) {
														Send(bidder, er2.errorMessage);
														warn(er2.errorMessage);
													} else {
														if (plot.getOwner() != null) {
															for (Player player : Bukkit.getServer().getOnlinePlayers()) {
																if (player.getName().equalsIgnoreCase(highestBid.getBidderName())) {
																	if (PlotMe.useDisplayNamesInMessages) {
																		Send(player, C("MsgOutbidOnPlot") + " " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerDisplayName() + " > " + f(bid));
																	} else {
																		Send(player, C("MsgOutbidOnPlot") + " " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerName() + " > " + f(bid));
																	}
																	break;
																}
															}
														} else {
															for (Player player : Bukkit.getServer().getOnlinePlayers()) {
																if (player.getName().equalsIgnoreCase(highestBid.getBidderName())) {
																	Send(player, C("MsgOutbidOnPlot") + " " + String.valueOf(plot.getId()) + " > " + f(bid));
																	break;
																}
															}
														}
													}
												}
												PlotMe.logger.info(bidder.getName() + " bid " + String.valueOf(bid) + " on plot " + String.valueOf(plot.getId()));
												if (plot.addAuctionBid(PlotDatabase.getPlotPlayer(bidder.getName()), bid)) {
													Send(bidder, C("MsgBidAccepted") + " " + f(-bid));
												} else {
													Send(bidder, er.errorMessage);
													warn(er.errorMessage);
												}
											}
										}
									}
								} else {
									Send(bidder, C("WordUsage") + ": " + ChatColor.RED + "/plotme " + 
												 C("CommandBid") + " <" + C("WordAmount") + "> " + 
												 ChatColor.RESET + C("WordExample") + ": " + ChatColor.RED + "/plotme " + C("CommandBid") + " 100");
								}
							}
						} else {
							Send(bidder, ChatColor.RED + C("MsgPlotNotAuctionned"));
						}
					}
				} else {
					Send(bidder, ChatColor.RED + C("MsgPermissionDenied"));
				}
			} else {
				Send(bidder, ChatColor.RED + C("MsgEconomyDisabledWorld"));
			}
			return true;
		}

		private boolean buy(Player player, String[] args) 
		{
			if (PlotManager.isEconomyEnabled(player)) {
				if (PlotMe.cPerms(player, "plotme.use.buy") || PlotMe.cPerms(player, "plotme.admin.buy")) {
					Plot plot = PlotManager.getPlotAtLocation(player.getLocation());
					if (plot == null) {
						Send(player, ChatColor.RED + C("MsgNoPlotFound"));
						return true;
					}

					if (!plot.isAvailable() || !plot.isForSale()) {
						Send(player, ChatColor.RED + C("MsgPlotNotForSale"));
						return true;
					}

					String buyer = player.getName();
					if (buyer.isEmpty()) {
						Send(player, ChatColor.RED + C("MsgPlayerDataError"));
						return true;
					}
					
					PlotPlayer newowner = PlotManager.getPlotPlayer(player);
					if (newowner == null) {
						Send(player, ChatColor.RED + C("MsgPlayerDataError"));
						return true;
					}
					
					PlotPlayer oldowner = plot.getOwner();
					if (oldowner == null) {
						Send(player, ChatColor.RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
						return true;
					}
					
					if (oldowner.equals(newowner) || plot.getOwner().getName().equals(buyer)) {
						Send(player, ChatColor.RED + C("MsgCannotBuyOwnPlot"));
						return true;
					}

					int plotlimit = PlotMe.getPlotLimit(player);

					if (plotlimit != -1 && newowner.getOwnPlotsCount()>=plotlimit) {
						Send(player, C("MsgAlreadyReachedMaxPlots") + " (" + 
										String.valueOf(newowner.getOwnPlotsCount()) + "/" + String.valueOf(plotlimit) + "). " + 
										C("WordUse") + " " + ChatColor.RED + "/plotme " + C("CommandHome") + ChatColor.RESET + " " + C("MsgToGetToIt"));
						return true;
					}

					double cost = plot.getClaimPrice();
					if (cost < 0) {
						Send(player, ChatColor.RED + C("MsgPlotNotForSale"));
						return true;
					}

					if (cost > 0) {
						if (PlotMe.economy.getBalance(buyer) >= cost) {
							EconomyResponse er = PlotMe.economy.withdrawPlayer(buyer, cost);
							if (er.transactionSuccess()) {
								if (oldowner.getId() > 0) {
									EconomyResponse er2 = PlotMe.economy.depositPlayer(oldowner.getName(), cost);
									if (!er2.transactionSuccess()) {
										Send(player, ChatColor.RED + er2.errorMessage);
										warn(er2.errorMessage);
										PlotMe.logger.info(buyer + " " + C("MsgBoughtPlot") + " " + String.valueOf(plot.getId()) + " " + er.errorMessage);
									}
								} else {
									EconomyResponse er2 = PlotMe.economy.bankDeposit("$PlotMeBank$", cost);
								}
							} else {
								Send(player, ChatColor.RED + er.errorMessage);
								warn(er.errorMessage);
								PlotMe.logger.info(buyer + " " + C("MsgBoughtPlot") + " " + String.valueOf(plot.getId()) + " " + er.errorMessage);
								return true;
							}
						} else {
							Send(player, ChatColor.RED + C("MsgNotEnoughBuy"));
							return true;
						}
					}
								
					/*if (oldowner.getPlayer() != null)
					{
						Send(oldowner.getPlayer(), C("WordPlot") + " " + String.valueOf(plot.getId()) + " " + 
												   C("MsgSoldTo") + " " + buyer + ". " + f(cost));
					}*/
					
					for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
						if (pl.getName() != buyer) {
							Send(pl, C("WordPlot") + " " + String.valueOf(plot.getId()) + " " + 
									 C("MsgSoldTo") + " " + buyer + ". " + f(cost));
						}
					}
							
					plot.disableSelling();
					plot.disableAuctioning();
					
					// SET THE NEW OWNER
					plot.setOwner(newowner);
					
					Send(player, C("MsgPlotBought") + " " + f(-cost));

					PlotMe.logger.info(buyer + " " + C("MsgBoughtPlot") + " " + String.valueOf(plot.getId()) + " " + C("WordFor") + " " + f(cost));
					return true;
				} else {
					Send(player, ChatColor.RED + C("MsgPermissionDenied"));
				}
			} else {
				Send(player, ChatColor.RED + C("MsgEconomyDisabledWorld"));
			}
			return true;
		}

		private boolean auction(CommandSender sender, String[] args) 
		{
			if (PlotMe.cPerms(sender, "plotme.use.auction") || PlotMe.cPerms(sender, "plotme.admin.auction")) {
				Plot plot = null;
				Player player = null;
				if (sender instanceof Player) {
					player = (Player)sender;
					if (args.length < 2)
					{
						plot = PlotManager.getPlotAtLocation(player.getLocation());
					}
				} else {
					if (args.length < 2) {
						Send(sender, ChatColor.RED + C("WordMissing"));
						return true;
					}
				}
				
				if (args.length >= 2) {
					int plotid = 0;
					try {
						plotid = Integer.parseInt(args[1]);
						plot = PlotManager.getPlot(plotid);
					} catch (NumberFormatException ex) {
						Send(sender, C("MsgInvalidNumber"));
						return true;
					}
				}
		
				if (plot == null) {
					Send(sender, ChatColor.RED + C("MsgNoPlotFound"));
					return true;
				}
				
				if (!PlotManager.isEconomyEnabled(plot)) {
					Send(sender, ChatColor.RED + C("MsgSellingPlotsIsDisabledWorld"));
					if (plot.isAuctioned()) {
						plot.disableAuctioning();
					}
					return true;
				}
				
				PlotWorld pwi = plot.getPlotWorld();
				if (pwi != null) {
					if (!pwi.CanPutOnSale) {
						Send(sender, ChatColor.RED + C("MsgEconomyDisabledWorld"));
						return true;
					}
					if (plot.getOwner() != null) {
						if ((player != null && plot.getOwnerName().equals(player.getName())) || PlotMe.cPerms(sender, "plotme.admin.auction")) {
							if (plot.isAuctioned()) {
								PlotAuctionBid highestbid = plot.getHighestAuctionBid();
								if (highestbid != null) {
									if (PlotMe.cPerms(sender, "plotme.admin.auction")) {
										EconomyResponse er = PlotMe.economy.depositPlayer(highestbid.getBidderName(), highestbid.getMoneyAmount());
										if (er.transactionSuccess()) {
										    for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
									        	if (PlotMe.useDisplayNamesInMessages) {
									        		Send(pl, C("MsgAuctionCancelledOnPlot") + 
									        				" " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerName() + ". " + f(highestbid.getMoneyAmount()));
									        	} else {
									        		Send(pl, C("MsgAuctionCancelledOnPlot") + 
									        				" " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerDisplayName() + ". " + f(highestbid.getMoneyAmount()));
									        	}
										    }
										} else {
											Send(sender, ChatColor.RED + er.errorMessage);
											warn(er.errorMessage);
										}
										
										plot.disableAuctioning();
										Send(sender, C("MsgAuctionCancelled"));

										PlotMe.logger.info(sender.getName() + " " + C("MsgStoppedTheAuctionOnPlot") + " " + String.valueOf(plot.getId()));
									} else {
										Send(sender, ChatColor.RED + C("MsgPlotHasBidsAskAdmin"));
										return true;
									}
								}
							} else {									
								double bid = 0;
						
								if (args.length == 2) {
									try {  
										bid = Double.parseDouble(args[1]);  
									} catch (Exception ex) {
										return false;
									}
								}
								
								if (bid >= 1) {
									plot.enableAuctioning();
									Send(sender, C("MsgAuctionStarted"));

									PlotMe.logger.info(sender.getName() + " " + C("MsgStartedAuctionOnPlot") + " " + String.valueOf(plot.getId()) + " " + C("WordAt") + " " + String.valueOf(bid));
								} else {
									Send(sender, ChatColor.RED + C("MsgInvalidAmount"));
									return true;
								}
							}
						} else {
							Send(sender, ChatColor.RED + C("MsgDoNotOwnPlot"));
							return true;
						}
					} else {
						Send(sender, ChatColor.RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
						return true;
					}
				} else {
					Send(sender, ChatColor.RED + C("MsgEconomyDisabledWorld"));
				}
			} else {
				Send(sender, ChatColor.RED + C("MsgPermissionDenied"));
				return true;
			}
			return true;
		}

		private boolean dispose(CommandSender sender, String[] args) 
		{
			if (PlotMe.cPerms(sender, "plotme.admin.dispose") || PlotMe.cPerms(sender, "plotme.use.dispose")) {
				Plot plot = null;
				if (sender instanceof Player) {
					if (args.length < 2) {
						plot = PlotManager.getPlotAtLocation(((Player)sender).getLocation());
					}
				} else {
					if (args.length < 2) {
						Send(sender, ChatColor.RED + C("WordMissing"));
						return true;
					}
				}
				
				if (args.length >= 2)
				{
					int plotid = 0;
					try {
						plotid = Integer.parseInt(args[1]);
						plot = PlotManager.getPlot(plotid);
					} catch (NumberFormatException ex) {
						Send(sender, C("MsgInvalidNumber"));
						return false;
					}
				}
				
				if (plot == null) {
					Send(sender, ChatColor.RED + C("MsgNoPlotFound"));
					return true;
				}
				
				if (!plot.isAvailable()) {
					Send(sender, C("MsgPlotDisposedAnyoneClaim"));
					return true;
				}

				if (!PlotManager.isPlotWorld(plot.getPlotWorld())) {
					Send(sender, ChatColor.RED + C("MsgNotPlotWorld"));
					return true;
				}

				if (plot.isProtected()) {
					Send(sender, ChatColor.RED + C("MsgPlotProtectedNotDisposed"));
					return true;
				}
				
				if (plot.getOwner() == null) {
					Send(sender, ChatColor.RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
					return true;
				}
				
				double cost = plot.getPlotWorld().DisposePrice;
				
				if (sender instanceof Player) {
					Player player = (Player)sender;
					if (plot.getOwnerName().equals(player.getName()) || PlotMe.cPerms(player, "plotme.admin.dispose")) {
						if (PlotManager.isEconomyEnabled(plot)) {
							if (cost != 0 && PlotMe.economy.getBalance(player.getName()) < cost) {
								Send(player, ChatColor.RED + C("MsgNotEnoughDispose"));
								return true;
							}
						}
					} else {
						Send(player, ChatColor.RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgNotYoursCannotDispose"));
						return true;
					}
					EconomyResponse er = PlotMe.economy.withdrawPlayer(player.getName(), cost);
					if (!er.transactionSuccess()) {	
						Send(player, ChatColor.RED + er.errorMessage);
						warn(er.errorMessage);
						return true;
					}
				}
				
				if (plot.isAuctioned()) {
					PlotAuctionBid highestbid = plot.getHighestAuctionBid();
					if (highestbid != null) {
						if (PlotMe.cPerms(sender, "plotme.admin.dispose")) {
							EconomyResponse er2 = PlotMe.economy.depositPlayer(highestbid.getBidderName(), highestbid.getMoneyAmount());
							if (!er2.transactionSuccess()) {
								Send(sender, ChatColor.RED + er2.errorMessage);
								warn(er2.errorMessage);
							}
						} else {
							Send(sender, ChatColor.RED + C("MsgPlotHasBidsAskAdmin"));
							return true;
						}
					}
				}
				
				PlotManager.removePlot(plot);
				PlotMe.logger.info(sender.getName() + " " + C("MsgDisposedPlot") + " " + String.valueOf(plot.getId()));
				
				for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
			    	if (PlotMe.useDisplayNamesInMessages) {
			    		Send(pl, C("WordPlot") + 
							     " " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerDisplayName() + " " + C("MsgWasDisposed") + " " + f(cost));
					} else {
						Send(pl, C("WordPlot") + 
						   		 " " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerName() + " " + C("MsgWasDisposed") + " " + f(cost));
					}
				}
			} else {
				Send(sender, ChatColor.RED + C("MsgPermissionDenied"));
			}
				
			return true;
		}

		private boolean sell(Player player, String[] args) 
		{
			if (PlotManager.isEconomyEnabled(player)) {
				PlotWorld pwi = PlotManager.getPlotWorld(player.getWorld().getName());
				if (pwi != null) {
					if (pwi.CanSellToBank || pwi.CanPutOnSale) {
						if (PlotMe.cPerms(player, "plotme.use.sell") || PlotMe.cPerms(player, "plotme.admin.sell")) {
							Plot plot = PlotManager.getPlotAtLocation(player.getLocation());
							if (plot == null) {
								Send(player, ChatColor.RED + C("MsgNoPlotFound"));
								return true;
							}
							if (plot.getOwner() != null) {
								if (plot.getOwnerName().equals(player.getName()) || PlotMe.cPerms(player, "plotme.admin.sell")) {
									if (plot.isForSale()) {
										plot.disableSelling();
										Send(player, C("MsgPlotNoLongerSale"));
										PlotMe.logger.info(player.getName() + " " + C("MsgRemovedPlot") + " " + String.valueOf(plot.getId()) + " " + C("MsgFromBeingSold"));
									} else {
										float price = pwi.SellToPlayerPrice;
										boolean bank = false;
											
										if (args.length == 2) {
											if (args[1].equalsIgnoreCase("bank")) {
												bank = true;
											} else {
												if (pwi.CanCustomizeSellPrice) {
													try {  
														price = Float.parseFloat(args[1]);  
													} catch (Exception e) {
														if (pwi.CanSellToBank) {
															Send(player, C("WordUsage") + ": " + ChatColor.RED + " /plot " + C("CommandSellBank") + "|<" + C("WordAmount") + ">");
															player.sendMessage("  " + C("WordExample") + ": " + ChatColor.RED + "/plot " + C("CommandSellBank") + " " + ChatColor.RESET + " or " + ChatColor.RED + " /plot " + C("CommandSell") + " 200");
														} else {
															Send(player, C("WordUsage") + ": " + ChatColor.RED + 
																	" /plot " + C("CommandSell") + " <" + C("WordAmount") + ">" + ChatColor.RESET + 
																	" " + C("WordExample") + ": " + ChatColor.RED + "/plot " + C("CommandSell") + " 200");
														}
														return false;
													}
												} else {
													Send(player, ChatColor.RED + C("MsgCannotCustomPriceDefault") + " " + f(price));
													return true;
												}
											}
										}
										
										if (bank) {
											if (!pwi.CanSellToBank)	{
												Send(player, ChatColor.RED + C("MsgCannotSellToBank"));
											} else {
												PlotAuctionBid highestBid = plot.getAuctionBid(0);
												
												if (highestBid != null) {
													double bid = highestBid.getMoneyAmount();
													
													EconomyResponse er = PlotMe.economy.depositPlayer(highestBid.getBidderName(), bid);
													
													if (!er.transactionSuccess()) {
														Send(player, ChatColor.RED + er.errorMessage);
														warn(er.errorMessage);
													} else {
														for(Player pl : Bukkit.getServer().getOnlinePlayers()) {
															if (pl.getName().equals(highestBid.getBidderName())) {
																if (PlotMe.useDisplayNamesInMessages) {
																	Send(player, C("WordPlot") + " " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerDisplayName() + " " + C("MsgSoldToBank") + " " + f(bid));
																} else {
																	Send(player, C("WordPlot") + " " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerName() + " " + C("MsgSoldToBank") + " " + f(bid));
																}
																break;
															}
														}
													}
												}
													
												EconomyResponse er = PlotMe.economy.depositPlayer(player.getName(), pwi.SellToBankPrice);
												if (er.transactionSuccess()) {
													plot.setOwner(PlotManager.getPlotPlayer("$Bank$"));
													plot.enableSelling(pwi.BuyFromBankPrice);
													plot.disableExpiration();
																									
													Send(player, C("MsgPlotSold") + " " + f(pwi.BuyFromBankPrice));
													PlotMe.logger.info(player.getName() + " " + C("MsgSoldToBankPlot") + " " + String.valueOf(plot.getId()) + " " + C("WordFor") + " " + f(pwi.BuyFromBankPrice));
												} else {
													Send(player, " " + er.errorMessage);
													warn(er.errorMessage);
												}
											}
										} else {
											if (price < 0) {
												Send(player, ChatColor.RED + C("MsgInvalidAmount"));
											} else {
												plot.enableSelling(price);
		
												Send(player, C("MsgPlotForSale"));
												PlotMe.logger.info(player.getName() + " " + C("MsgPutOnSalePlot") + " " + String.valueOf(plot.getId()) + " " + C("WordFor") + " " + f(price));
											}
										}
									}
								} else {
									Send(player, ChatColor.RED + C("MsgDoNotOwnPlot"));
								}
							} else {
								Send(player, ChatColor.RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
							}
						} else {
							Send(player, ChatColor.RED + C("MsgPermissionDenied"));
						}
					} else {
						Send(player, ChatColor.RED + C("MsgSellingPlotsIsDisabledWorld"));
					}
				} else {
					Send(player, ChatColor.RED + C("MsgSellingPlotsIsDisabledWorld"));
				}
			} else {
				Send(player, ChatColor.RED + C("MsgEconomyDisabledWorld"));
			}
			return true;
		}

		private boolean protect(Player player, String[] args) 
		{
			if (PlotMe.cPerms(player, "plotme.admin.protect") || PlotMe.cPerms(player, "plotme.use.protect"))
			{
				PlotWorld pwi = PlotManager.getPlotWorld(player.getWorld().getName());
				if (pwi == null) {
					Send(player, ChatColor.RED + C("MsgNotPlotWorld"));
					return true;
				}
				Plot plot = PlotManager.getPlotAtLocation(player.getLocation());
				if (plot == null) {
					Send(player, ChatColor.RED + C("MsgNoPlotFound"));
					return true;
				}
				if (plot.getOwner() == null) {
					Send(player, ChatColor.RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
				}
				if ((plot.getOwner() != null && plot.getOwnerName().equalsIgnoreCase(player.getName())) || PlotMe.cPerms(player, "plotme.admin.protect")) {
					if (plot.isProtected()) {
						plot.setDaysToExpiration(PlotMe.DEFAULT_DAYS_TO_EXPIRATION);
						Send(player, C("MsgPlotNoLongerProtected"));
						PlotMe.logger.info(player.getName() + " " + C("MsgUnprotectedPlot") + " " + String.valueOf(plot.getId()));
					} else {
						double cost = 0;
						if (PlotManager.isEconomyEnabled(player)) {
							cost = pwi.ProtectPrice;
							if (PlotMe.economy.getBalance(player.getName()) < cost) {
								Send(player, ChatColor.RED + C("MsgNotEnoughProtectPlot"));
								return true;
							} else {
								EconomyResponse er = PlotMe.economy.withdrawPlayer(player.getName(), cost);
								if (!er.transactionSuccess()) {
									Send(player, ChatColor.RED + er.errorMessage);
									warn(er.errorMessage);
									return true;
								}
							}
							plot.disableExpiration();
							Send(player, C("MsgPlotNowProtected") + " " + f(-cost));
							PlotMe.logger.info(player.getName() + " " + C("MsgProtectedPlot") + " " + String.valueOf(plot.getId()));
						}
					}
				} else {
					Send(player, ChatColor.RED + C("MsgDoNotOwnPlot"));
				}
			} else {
				Send(player, ChatColor.RED + C("MsgPermissionDenied"));
			}
			return true;
		}

		private boolean donelist(CommandSender sender, String[] args) 
		{
			if (PlotMe.cPerms(sender, "plotme.admin.done")) {
				List<Integer> finishedplots = PlotDatabase.getFinishedPlots();
				if (finishedplots == null || finishedplots.size() == 0) {
					Send(sender, C("MsgNoPlotsFinished"));
					return true;
				}
				int nbfinished = 0;
				int page = 1;
				if (args.length >= 2) {
					try {
						page = Integer.parseInt(args[1]);
					} catch(NumberFormatException ex) {
						Send(sender, ChatColor.RED + C("MsgInvalidPageNumber"));
						page = 1;
					}
				}
				
				if (page < 1) {
					page = 1;
				}
				
				int minIndex = (page-1) * 8;
				int maxIndex = Math.min(minIndex + 8, finishedplots.size());
				
				int maxPage = (int)Math.ceil((double)finishedplots.size() / (double)8);
				if (page > maxPage) {
					page = maxPage;
				}
				
				Send(sender, C("MsgFinishedPlotsPage") + " " + page + " / " + maxPage);
				
				int finId;

				Iterator<Integer> fini = finishedplots.iterator();
				int textLength;
				int daysAgo;
				long currentTime = Math.round(System.currentTimeMillis() / 1000);
				while (fini.hasNext() && nbfinished < maxIndex) {
					finId = fini.next();
					if (finId > 0 && nbfinished >= minIndex) {
						Plot plot = PlotManager.getPlot(finId);
						if (plot != null) {
							daysAgo = (int)Math.floor((currentTime - plot.getFinishDate()) / 86400);
							sender.sendMessage(" " + ChatColor.BLUE + String.valueOf(nbfinished) + String.valueOf(plot.getId()) + ChatColor.RESET + " -> " + plot.getOwnerName() + " (" + String.valueOf(daysAgo) + " days ago)");
						} else {
							sender.sendMessage(" " + ChatColor.RED + String.valueOf(nbfinished) + ". NULL");
						}
					} else {
						sender.sendMessage(" " + ChatColor.RED + String.valueOf(nbfinished) + ". INVALID");
					}
					nbfinished++;
				}
			} else {
				Send(sender, ChatColor.RED + C("MsgPermissionDenied"));
			}
			return true;
		}

		private boolean done(Player player, String[] args)
		{
			if(PlotMe.cPerms(player, "PlotMe.use.done") || PlotMe.cPerms(player, "PlotMe.admin.done"))
			{
				if(!PlotManager.isPlotWorld(player))
				{
					Send(player, ChatColor.RED + C("MsgNotPlotWorld"));
					return true;
				}
				else
				{
					Plot plot = PlotManager.getPlotAtLocation(player.getLocation());
					
					if (plot == null)
					{
						Send(player, ChatColor.RED + C("MsgNoPlotFound"));
					}
					else
					{
						if(!PlotManager.isPlotAvailable(player))
						{
							
							String name = player.getName();
							
							if (plot.getOwner().getName().equalsIgnoreCase(name) || PlotMe.cPerms(player, "PlotMe.admin.done"))
							{							
								if(plot.isFinished())
								{
									plot.setUnfinished();
									Send(player, C("MsgUnmarkFinished"));
									PlotMe.logger.info(name + " " + C("WordMarked") + " " + String.valueOf(plot.getId()) + " " + C("WordFinished"));
								}
								else
								{
									plot.setFinished();
									Send(player, C("MsgMarkFinished"));
									PlotMe.logger.info(name + " " + C("WordMarked") + " " + String.valueOf(plot.getId()) + " " + C("WordUnfinished"));
								}
							}
						}
						else
						{
							Send(player, ChatColor.RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
						}
					}
				}
			}
			else
			{
				Send(player, ChatColor.RED + C("MsgPermissionDenied"));
			}
			return true;
		}
		
		private boolean addtime(Player player, String[] args)
		{
			if(PlotMe.cPerms(player, "PlotMe.admin.addtime"))
			{
				if(!PlotManager.isPlotWorld(player))
				{
					Send(player, ChatColor.RED + C("MsgNotPlotWorld"));
					return true;
				}
				else
				{
					Plot plot = PlotManager.getPlotAtLocation(player.getLocation());
					
					if (plot == null)
					{
						Send(player, ChatColor.RED + C("MsgNoPlotFound"));
					}
					else
					{
						if (!plot.isAvailable())
						{
							if(plot != null)
							{
								String name = player.getName();
								
								plot.setDaysToExpiration(plot.getPlotWorld().DaysToExpiration);
								Send(player, C("MsgPlotExpirationReset"));
								
								PlotMe.logger.info(name + " reset expiration on plot " + plot.getId());
							}
						}
						else
						{
							Send(player, ChatColor.RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
						}
					}
				}
			}
			else
			{
				Send(player, ChatColor.RED + C("MsgPermissionDenied"));
			}
			return true;
		}

		/*private boolean expired(CommandSender sender, String[] args)
		{
			if (((sender instanceof Player) && PlotMe.cPerms((Player)sender, "plotme.admin.expired")))
			{
				List<Integer> expiredPlots = PlotDatabase.getExpiredPlots();
				if (expiredPlots == null || expiredPlots.isEmpty())
				{
					Send(sender, ChatColor.GREEN + C("MsgNoPlotExpired"));
					return true;
				}
				
				Integer plotId;
				StringBuilder commaSeparatedList = new StringBuilder();
				Iterator<Integer> expiredPlotsIterator = expiredPlots.iterator();
				while (expiredPlotsIterator.hasNext())
				{
					plotId = expiredPlotsIterator.next();
					if (plotId != null)
					{
						commaSeparatedList.append(plotId);
						if (expiredPlotsIterator.hasNext())
							commaSeparatedList.append(", ");
					}
						
				}
				sender.sendMessage(ChatColor.BLUE + commaSeparatedList.toString() + ChatColor.RESET);

			}
			else
			{
				Send(sender, ChatColor.RED + C("MsgPermissionDenied"));
			}
			return true;
		}*/

/*		private boolean plotlist(CommandSender sender, String[] args)
		{
			Player player = null;
			if (sender instanceof Player) {
				player = (Player)sender;
			} else {
				return true;
			}
			if ((player != null && PlotMe.cPerms(player, "plotme.use.list")) || PlotMe.cPerms(player, "plotme.admin.list")) {
				if (!PlotManager.isPlotWorld(player)) {
					Send(player, ChatColor.RED + C("MsgNotPlotWorld"));
					return true;
				} else {
					String name;
					
					if (PlotMe.cPerms(player, "plotme.admin.list") && args.length == 2)	{
						name = args[1];
						Send(player, C("MsgListOfPlotsWhere") + " " + ChatColor.BLUE + name + ChatColor.RESET + " " + C("MsgCanBuild"));
					} else {
						name = player.getName();
						Send(player, C("MsgListOfPlotsWhereYou"));
					}
									
					for (Plot plot : PlotManager.get) {
						StringBuilder addition = new StringBuilder();
							
						if(plot.getExpiration() != null)
						{
							java.util.Date tempdate = plot.getExpiration(); 
							
							if(tempdate.compareTo(Calendar.getInstance().getTime()) < 0)
							{
								addition.append(RED + " @" + plot.getExpiration().toString() + RESET);
							}else{
								addition.append(" @" + plot.getExpiration().toString());
							}
						}
						
						if(plot.isAuctioned())
						{
							addition.append(" " + C("WordAuction") + ": " + GREEN + round(plot.currentbid) + RESET + ((!plot.currentbidder.isEmpty()) ? " " + plot.currentbidder : "") );
						}
						
						if(plot.forsale)
						{
							addition.append(" " + C("WordSell") + ": " + GREEN + round(plot.customprice) + RESET);
						}
							
						if(plot.getOwner().getName().equalsIgnoreCase(name))
						{
							if(plot.allowedcount() == 0)
							{
								if(name.equalsIgnoreCase(player.getName()))
									player.sendMessage("  " + plot.getId() + " -> " + BLUE + ITALIC + C("WordYours") + RESET + addition);
								else
									player.sendMessage("  " + plot.getId() + " -> " + BLUE + ITALIC + plot.getOwnerName() + RESET + addition);
							}
							else
							{
								StringBuilder helpers = new StringBuilder();
								for(int i = 0 ; i < plot.allowedcount(); i++)
								{
									helpers.append(BLUE).append(plot.allowed().toArray()[i]).append(RESET).append(", ");
								}
								if(helpers.length() > 2)
									helpers.delete(helpers.length() - 2, helpers.length());
								
								if(name.equalsIgnoreCase(player.getName()))
									player.sendMessage("  " + plot.getId() + " -> " + BLUE + ITALIC + C("WordYours") + RESET + addition + ", " + C("WordHelpers") + ": " + helpers);
								else
									player.sendMessage("  " + plot.getId() + " -> " + BLUE + ITALIC + plot.getOwnerName() + RESET + addition + ", " + C("WordHelpers") + ": " + helpers);
							}
						}
						else if(plot.isAllowed(name))
						{
							StringBuilder helpers = new StringBuilder();
							for(int i = 0 ; i < plot.allowedcount(); i++)
							{
								if(player.getName().equalsIgnoreCase((String) plot.allowed().toArray()[i]))
									if(name.equalsIgnoreCase(player.getName()))
										helpers.append(BLUE).append(ITALIC).append("You").append(RESET).append(", ");
									else
										helpers.append(BLUE).append(ITALIC).append(name).append(RESET).append(", ");
								else
									helpers.append(BLUE).append(plot.allowed().toArray()[i]).append(RESET).append(", ");
							}
							if(helpers.length() > 2)
								helpers.delete(helpers.length() - 2, helpers.length());
							
							if(plot.getOwner().getName().equalsIgnoreCase(player.getName()))
								player.sendMessage("  " + plot.getId() + " -> " + BLUE + C("WordYours") + RESET + addition + ", " + C("WordHelpers") + ": " + helpers);
							else
								player.sendMessage("  " + plot.getId() + " -> " + BLUE + plot.getOwnerName() + C("WordPossessive") + RESET + addition + ", " + C("WordHelpers") + ": " + helpers);
						}
					}
				}
			}
			else
			{
				Send(player, RED + C("MsgPermissionDenied"));
			}
			return true;
		}

		private boolean weanywhere(Player player, String[] args)
		{
			if(PlotMe.cPerms(player, "PlotMe.admin.weanywhere"))
			{
				String name = player.getName();
				
				if(PlotMe.isIgnoringWELimit(player) && !PlotMe.defaultWEAnywhere || !PlotMe.isIgnoringWELimit(player) && PlotMe.defaultWEAnywhere)
				{
					PlotMe.removeIgnoreWELimit(player);
				}
				else
				{
					PlotMe.addIgnoreWELimit(player);					
				}
				
				if(PlotMe.isIgnoringWELimit(player))
				{
					Send(player, C("MsgWorldEditAnywhere"));
					
					if(isAdv)
						PlotMe.logger.info(LOG + name + " enabled WorldEdit anywhere");
				}
				else
				{
					Send(player, C("MsgWorldEditInYourPlots"));
					
					if(isAdv)
						PlotMe.logger.info(LOG + name + " disabled WorldEdit anywhere");
				}
			}
			else
			{
				Send(player, RED + C("MsgPermissionDenied"));
			}
			return true;
		}*/
		
		private boolean tp(CommandSender sender, String[] args)
		{
			Player player = null;
			if (sender instanceof Player) {
				player = (Player)sender;
			} else {
				return true;
			}
			if (PlotMe.cPerms(player, "plotme.tp") || PlotMe.cPerms(player, "plotme.admin.tp")) {
				if (args.length >= 2) {
					int plotId = 0;
					try {
						plotId = Integer.parseInt(args[1]);
					} catch (Exception e) {
						return false;
					}
					Plot plot = PlotManager.getPlot(plotId);
					Location bottom = plot.getWorldMinBlockLocation();
					Location top = plot.getWorldMaxBlockLocation();
					int centerX = (int)Math.round((bottom.getX() + top.getX()) / 2);
					int centerZ = (int)Math.round((bottom.getZ() + top.getZ()) / 2);
					player.teleport(new Location(plot.getMinecraftWorld(), centerX, plot.getMinecraftWorld().getHighestBlockYAt(centerX, centerZ), centerZ));
				} else {
					Send(player, C("WordUsage") + ": " + ChatColor.RED + "/plotme " + C("CommandTp") + " <" + C("WordId") + "> " + ChatColor.RESET + C("WordExample") + ": " + ChatColor.RED + "/plotme " + C("CommandTp") + " 5 ");
					return true;
				}
			} else {
				Send(player, ChatColor.RED + C("MsgPermissionDenied"));
			}
			return true;
		}

		private boolean claim(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "PlotMe.use.claim") || PlotMe.cPerms(player, "PlotMe.admin.claim.other"))
			{
				if (!PlotManager.isPlotWorld(player))
				{
					Send(player, ChatColor.RED + C("MsgNotPlotWorld"));
				}
				else
				{		
					Plot ppi = PlotManager.getPlotAtLocation(player.getLocation());
					
					if (ppi == null)
					{
						Send(player, ChatColor.RED + C("MsgCannotClaimRoad"));
					}
					else if(!PlotManager.isPlotAvailable(player))
					{
						Send(player, ChatColor.RED + C("MsgThisPlotOwned"));
					}
					else
					{
						String playername = player.getName();
						
						if (args.length == 2)
						{
							if (PlotMe.cPerms(player, "PlotMe.admin.claim.other"))
							{
								playername = args[1];
							}
						}
						
						int plotlimit = PlotMe.getPlotLimit(player);
						
						PlotPlayer plotPlayer = PlotManager.getPlotPlayer(player);
						
						int plotCount = PlotManager.getPlayerPlotCount(plotPlayer, ppi.getPlotWorld());
						
						if (playername == player.getName() && plotlimit != -1 && plotCount >= plotlimit) {
							Send(player, ChatColor.RED + C("MsgAlreadyReachedMaxPlots") + " (" + 
									String.valueOf(plotCount) + "/" + PlotMe.getPlotLimit(player) + "). " + C("WordUse") + " " + ChatColor.RED + "/plotme " + C("CommandHome") + ChatColor.RESET + " " + C("MsgToGetToIt"));
						}
						else
						{
							PlotWorld pwi = PlotManager.getPlotWorld(player.getWorld().getName());
							
							double price = 0;
							
							if(PlotManager.isEconomyEnabled(pwi))
							{
								price = pwi.ClaimPrice;
								double balance = PlotMe.economy.getBalance(playername);
								
								if(balance >= price)
								{
									EconomyResponse er = PlotMe.economy.withdrawPlayer(playername, price);
									
									if(!er.transactionSuccess())
									{
										Send(player, ChatColor.RED + er.errorMessage);
										warn(er.errorMessage);
										return true;
									}
								}
								else
								{
									Send(player, ChatColor.RED + C("MsgNotEnoughBuy") + " " + C("WordMissing") + " " + ChatColor.RESET + (price - balance) + ChatColor.RED + " " + PlotMe.economy.currencyNamePlural());
									return true;
								}
							}
							
							PlotManager.adjustLinkedPlots(ppi);
			
							if (ppi == null) {
								Send(player, ChatColor.RED + C("ErrCreatingPlotAt") + " " + player.getLocation().toString());
							} else {
								if(playername.equalsIgnoreCase(player.getName())) {
									Send(player, C("MsgThisPlotYours") + " " + C("WordUse") + " " + ChatColor.RED + "/plotme " + C("CommandHome") + ChatColor.RESET + " " + C("MsgToGetToIt") + " " + f(-price));
								} else {
									Send(player, C("MsgThisPlotIsNow") + " " + playername + C("WordPossessive") + ". " + C("WordUse") + " " + ChatColor.RED + "/plotme " + C("CommandHome") + ChatColor.RESET + " " + C("MsgToGetToIt") + " " + f(-price));
								}

								PlotMe.logger.info(playername + " " + C("MsgClaimedPlot") + " " + String.valueOf(ppi.getId()) + ((price != 0) ? " " + C("WordFor") + " " + price : ""));
							}
						}
					}
				}
			}
			else
			{
				Send(player, ChatColor.RED + C("MsgPermissionDenied"));
			}
			return true;
		}
	
		/*
		 * TODO
		 */
/*		private boolean home(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "PlotMe.use.home") || PlotMe.cPerms(player, "PlotMe.admin.home.other"))
			{
				if(!PlotManager.isPlotWorld(player) && !PlotMe.allowWorldTeleport)
				{
					Send(player, ChatColor.RED + C("MsgNotPlotWorld"));
				}
				else
				{
					boolean found = false;
					String playername = player.getName();
					int nb = 1;
					World w;
					
					if(!PlotManager.isPlotWorld(player) && PlotMe.allowWorldTeleport)
					{
						w = PlotManager.getFirstWorld();
					}
					else
					{
						w = p.getWorld();
					}
					
					if(args[0].contains(":"))
					{
						try{
							if(args[0].split(":").length == 1 || args[0].split(":")[1].isEmpty())
							{
								Send(player, C("WordUsage") + ": " + RED + "/plotme " + C("CommandHome") + ":# " + 
										RESET + C("WordExample") + ": " + RED + "/plotme " + C("CommandHome") + ":1");
								return true;
							}
							else
							{
								nb = Integer.parseInt(args[0].split(":")[1]);
							}
						}catch(Exception ex)
						{
							Send(player, C("WordUsage") + ": " + RED + "/plotme " + C("CommandHome") + ":# " + 
									RESET + C("WordExample") + ": " + RED + "/plotme " + C("CommandHome") + ":1");
							return true;
						}
					}
					
					if(args.length == 2)
					{
						if(Bukkit.getWorld(args[1]) == null)
						{
							if(PlotMe.cPerms(player, "PlotMe.admin.home.other"))
							{
								playername = args[1];
							}
						}
						else
						{
							w = Bukkit.getWorld(args[1]);
						}
					}
					
					if(args.length == 3)
					{
						if(Bukkit.getWorld(args[2]) == null)
						{
							Send(player, RED + args[2] + C("MsgWorldNotPlot"));
							return true;
						}
						else
						{
							w = Bukkit.getWorld(args[2]);
						}
					}
					
					if(!PlotManager.isPlotWorld(w))
					{
						Send(player, RED + args[2] + C("MsgWorldNotPlot"));
					}
					else
					{
						int i = nb - 1;
								
						for(Plot plot : PlotManager.getPlots(w).values())
						{
							if(plot.getOwner().getName().equalsIgnoreCase(playername))
							{
								if(i == 0)
								{							
									PlotWorld pwi = PlotManager.getMap(w);
									
									double price = 0;
															
									if(PlotManager.isEconomyEnabled(w))
									{
										price = pwi.PlotHomePrice;
										double balance = PlotMe.economy.getBalance(playername);
										
										if(balance >= price)
										{
											EconomyResponse er = PlotMe.economy.withdrawPlayer(playername, price);
											
											if(!er.transactionSuccess())
											{
												Send(player, RED + er.errorMessage);
												return true;
											}
										}
										else
										{
											Send(player, RED + C("MsgNotEnoughTp") + " " + C("WordMissing") + " " + RESET + f(price - balance, false));
											return true;
										}
									}
									
									p.teleport(PlotManager.getPlotHome(w, plot));
									
									if(price != 0)
										Send(player, f(-price));
									
									return true;
								}else{
									i--;
								}
							}
						}
						
						if(!found)
						{
							if(nb > 0)
							{
								if(!playername.equalsIgnoreCase(player.getName()))
								{
									Send(player, RED + playername + " " + C("MsgDoesNotHavePlot") + " #" + nb);
								}else{
									Send(player, RED + C("MsgPlotNotFound") + " #" + nb);
								}
							}
							else if(!playername.equalsIgnoreCase(player.getName()))
							{
								Send(player, RED + playername + " " + C("MsgDoesNotHavePlot"));
							}
							else
							{
								Send(player, RED + C("MsgYouHaveNoPlot"));
							}
						}
					}
				}
			}
			else
			{
				Send(player, RED + C("MsgPermissionDenied"));
			}
			return true;
		}*/
		
		private boolean info(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "PlotMe.use.info"))
			{
				if(!PlotManager.isPlotWorld(player))
				{
					Send(player, ChatColor.RED + C("MsgNotPlotWorld"));
				}
				else
				{
					Plot plot = PlotManager.getPlotAtLocation(player.getLocation());
					
					if (plot == null)
					{
						Send(player, ChatColor.RED + C("MsgNoPlotFound"));
					}
					else
					{
						if (!PlotManager.isPlotAvailable(player))
						{
							if (plot != null) {
								player.sendMessage(ChatColor.GREEN + C("InfoId") + ": " + ChatColor.AQUA + String.valueOf(plot.getId()) + 
										ChatColor.GREEN + " " + C("InfoOwner") + ": " + ChatColor.AQUA + plot.getOwnerName() + 
										ChatColor.GREEN + " " + C("InfoBiome") + ": " + ChatColor.AQUA + FormatBiome(plot.getBiome().name()));
								
								player.sendMessage(ChatColor.GREEN + C("InfoExpire") + ": " + ChatColor.AQUA + ((plot.getExpireDate() == null) ? C("WordNever") : plot.getExpireDate().toString()) +
										ChatColor.GREEN + " " + C("InfoFinished") + ": " + ChatColor.AQUA + ((plot.isFinished()) ? C("WordYes") : C("WordNo")) +
										ChatColor.GREEN + " " + C("InfoProtected") + ": " + ChatColor.AQUA + ((plot.isProtected()) ? C("WordYes") : C("WordNo")));
								
								/*if(plot.allowedcount() > 0) { TODO
									player.sendMessage(GREEN + C("InfoHelpers") + ": " + ChatColor.AQUA + plot.isA);
								}
								
								if(PlotMe.allowToDeny && plot.deniedcount() > 0)
								{
									player.sendMessage(GREEN + C("InfoDenied") + ": " + ChatColor.AQUA + plot.getDenied());
								}*/
								
								if (PlotManager.isEconomyEnabled(plot)) {
									player.sendMessage(ChatColor.GREEN + "Auctionned: " + ChatColor.AQUA + ((plot.isAuctioned()) ? C("WordYes") + 
												ChatColor.GREEN + " Minimum bid: " + ChatColor.AQUA + String.valueOf(plot.getHighestAuctionBid().getMoneyAmount()) : C("WordNo")) +
												ChatColor.GREEN + " For sale: " + ChatColor.AQUA + ((plot.isForSale()) ? ChatColor.AQUA + String.valueOf(plot.getClaimPrice()) : C("WordNo")));
								} else {
									player.sendMessage(ChatColor.GREEN + C("InfoAuctionned") + ": " + ChatColor.AQUA + ((plot.isAuctioned()) ? C("WordYes") + 
												ChatColor.GREEN + " " + C("InfoBidder") + ": " + ChatColor.AQUA + plot.getHighestAuctionBid().getBidderDisplayName() + 
												ChatColor.GREEN + " " + C("InfoBid") + ": " + ChatColor.AQUA + String.valueOf(plot.getHighestAuctionBid().getMoneyAmount()) : C("WordNo")) +
												ChatColor.GREEN + " " + C("InfoForSale") + ": " + ChatColor.AQUA + ((plot.isForSale()) ? ChatColor.AQUA + String.valueOf(plot.getClaimPrice()) : C("WordNo")));
								}
							}
						}
						else
						{
							Send(player, ChatColor.RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
						}
					}
				}
			}
			else
			{
				Send(player, ChatColor.RED + C("MsgPermissionDenied"));
			}
			return true;
		}
		
		private boolean comment(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "plotme.use.comment"))
			{
				
				if(!PlotManager.isPlotWorld(player))
				{
					Send(player, ChatColor.RED + C("MsgNotPlotWorld"));
				}
				else
				{
					if(args.length < 2)
					{
						Send(player, C("WordUsage") + ": " + ChatColor.RED + "/plot " + C("CommandComment") + " <" + C("WordText") + ">");
					}
					else
					{
						Plot plot = PlotManager.getPlotAtLocation(player.getLocation());
						
						if (plot == null)
						{
							Send(player, ChatColor.RED + C("MsgNoPlotFound"));
						}
						else
						{
							if (!PlotManager.isPlotAvailable(player))
							{
								PlotWorld pwi = PlotManager.getPlotWorld(player.getWorld().getName());
								String playername = player.getName();
								
								double price = 0;
								
								if(PlotManager.isEconomyEnabled(pwi))
								{
									price = pwi.AddCommentPrice;
									double balance = PlotMe.economy.getBalance(playername);
									
									if(balance >= price)
									{
										EconomyResponse er = PlotMe.economy.withdrawPlayer(playername, price);
										
										if(!er.transactionSuccess())
										{
											Send(player, ChatColor.RED + er.errorMessage);
											warn(er.errorMessage);
											return true;
										}
									}
									else
									{
										Send(player, ChatColor.RED + C("MsgNotEnoughComment") + " " + C("WordMissing") + " " + ChatColor.RESET + f(price - balance, false));
										return true;
									}
								}
								
								// TODO
								/*String[] comment = new String[2];
								args[0] = playername;
								args[1] = ;
								
								plot.comments.add(comment);
								PlotMeSqlManager.addPlotComment(comment, plot.comments.size(), PlotManager.getIdX(id), PlotManager.getIdZ(id), plot.world);
								
								Send(player, C("MsgCommentAdded") + " " + f(-price));

								PlotMe.logger.info(playername + " " + C("MsgCommentedPlot") + " " + String.valueOf(plot.getId()) + ((price != 0) ? " " + C("WordFor") + " " + price : ""));*/
							}
							else
							{
								Send(player, ChatColor.RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
							}
						}
					}
				}
			}
			else
			{
				Send(player, ChatColor.RED + C("MsgPermissionDenied"));
			}
			return true;
		}
		
		private boolean comments(CommandSender sender, String[] args)
		{
			if (PlotMe.cPerms(sender, "plotme.use.comments") || PlotMe.cPerms(sender, "plotme.view.allcomments"))
			{
				Plot plot = null;
				int ipage = 0;
				Player player = null;
				if (args.length < 3)
				{
					if (sender instanceof Player)
					{
						player = (Player)sender;
						if (!PlotManager.isPlotWorld(player))
						{
							Send(player, ChatColor.RED + C("MsgNotPlotWorld"));
							return true;
						}
						plot = PlotManager.getPlotAtLocation(player.getLocation());
						PlotPlayer plotPlayer = PlotManager.getPlotPlayer(player);
						if (plot != null)
						{
							if (!(plot.getOwner().getName().equals(player.getName()) || plot.isAllowed(plotPlayer) || PlotMe.cPerms(sender, "plotme.view.allcomments")))
							{
								player.sendMessage(ChatColor.RED + C("MsgPermissionDenied"));
								return true;
							}
						}
						if (args.length >= 2)
						{
							try
							{
								ipage = Integer.parseInt(args[1]);
							}
							catch (NumberFormatException ex)
							{
								sender.sendMessage(C("MsgInvalidPageNumber"));
								return false;
							}
						}
					}
					else
					{
						return false;
					}
				}
				else
				{
					try
					{
						int plotid = Integer.parseInt(args[1]);
						plot = PlotManager.getPlot(plotid);
					}
					catch (NumberFormatException ex)
					{
						sender.sendMessage(C("MsgInvalidNumber"));
						return false;
					}
					try
					{
						ipage = Integer.parseInt(args[2]);
					}
					catch (NumberFormatException ex)
					{
						sender.sendMessage(C("MsgInvalidPageNumber"));
						return false;
					}
				}
				if (plot == null)
				{
					Send(sender, ChatColor.RED + C("MsgNoPlotFound"));
					return true;
				}
				
				if (ipage < 0)
				{
					ipage = 0;
				}

/*				LinkedList<Pair<String, String>> plotcomments = plot.getComments();
				
				if (plotcomments != null && plotcomments.size() > 0)
				{
					int maxpage = (int)Math.ceil(plotcomments.size() / 10);
					
					if (ipage > maxpage)
					{
						sender.sendMessage(C("MsgInvalidPageNumber"));
					}
					
					if (player != null)
					{
						Send(player, C("MsgYouHave") + " " +
									 BLUE + plotcomments.size() +
									 RESET + " " + C("MsgComments")
						);
					}
					else
					{
						Send(sender, BLUE + plotcomments.size() +
								 	 RESET + " " + C("MsgComments")
						);
					}
						
					int curIndex = 0;
					int minIndex = ipage * 10;
					int maxIndex = min(minIndex + 10, plotcomments.size());
					Pair<String, String> curComment;

					Iterator<Pair<String, String>> commentsIterator = plotcomments.iterator();
					while (commentsIterator.hasNext() && curIndex < maxIndex)
					{
						curComment = commentsIterator.next();
						if (curIndex >= minIndex)
						{
							sender.sendMessage(ChatColor.BLUE + C("WordFrom") + " : " + RED + curComment.getLeft());
							sender.sendMessage("" + RESET + ChatColor.ITALIC + curComment.getRight());
						}
						curIndex ++;
					}
				}
				else
				{
					Send(player, C("MsgNoComments"));
				}
								}
								else
								{
									Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgNotYoursNotAllowedViewComments"));
								}
							}
							else
							{
								Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
							}
						}
					}
				}*/
			}
			else
			{
				sender.sendMessage(ChatColor.RED + C("MsgPermissionDenied"));
			}
			return true;
		}
		
		private boolean biome(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "PlotMe.use.biome"))
			{
				if(!PlotManager.isPlotWorld(player))
				{
					Send(player, ChatColor.RED + C("MsgNotPlotWorld"));
				}
				else
				{
					Plot plot = PlotManager.getPlotAtLocation(player.getLocation());
					if (plot == null)
					{
						player.sendMessage(ChatColor.RED + C("MsgNoPlotFound"));
					}
					else
					{
						if(!PlotManager.isPlotAvailable(player))
						{
							PlotWorld pwi = PlotManager.getPlotWorld(player.getWorld().getName());
							
							if(args.length == 2)
							{
								Biome biome = null;
								
								for(Biome bio : Biome.values())
								{
									if(bio.name().equalsIgnoreCase(args[1]))
									{
										biome = bio;
									}
								}
								
								if(biome == null)
								{
									Send(player, ChatColor.RED + args[1] + ChatColor.RESET + " " + C("MsgIsInvalidBiome"));
								}
								else
								{
									String playername = player.getName();
									
									if(plot.getOwner().getName().equalsIgnoreCase(playername) || PlotMe.cPerms(player, "PlotMe.admin"))
									{
										double price = 0;
										
										if(PlotManager.isEconomyEnabled(plot.getPlotWorld()))
										{
											price = pwi.BiomeChangePrice;
											double balance = PlotMe.economy.getBalance(playername);
											
											if(balance >= price)
											{
												EconomyResponse er = PlotMe.economy.withdrawPlayer(playername, price);
												
												if(!er.transactionSuccess())
												{
													Send(player, ChatColor.RED + er.errorMessage);
													warn(er.errorMessage);
													return true;
												}
											}
											else
											{
												Send(player, ChatColor.RED + C("MsgNotEnoughBiome") + " " + C("WordMissing") + " " + ChatColor.RESET + f(price - balance, false));
												return true;
											}
										}
										
										PlotManager.setBiome(plot, biome);
									
										Send(player, C("MsgBiomeSet") + " " + ChatColor.BLUE + FormatBiome(biome.name()) + " " + f(-price));
		
										PlotMe.logger.info(playername + " " + C("MsgChangedBiome") + " " + String.valueOf(plot.getId()) + " " + C("WordTo") + " " + 
													FormatBiome(biome.name()) + ((price != 0) ? " " + C("WordFor") + " " + price : ""));
									}
									else
									{
										Send(player, ChatColor.RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgNotYoursNotAllowedBiome"));
									}
								}
							}
							else
							{
								Send(player, C("MsgPlotUsingBiome") + " " + ChatColor.BLUE + FormatBiome(plot.getBiome().name()));
							}
						}
						else
						{
							Send(player, ChatColor.RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
						}
					}
				}
			}
			else
			{
				Send(player, ChatColor.RED + C("MsgPermissionDenied"));
			}
			return true;
		}
		
		/*private boolean biomelist(CommandSender sender, String[] args)
		{
			if (!(sender instanceof Player) || PlotMe.cPerms((Player)sender, "PlotMe.use.biome"))
			{
				Send(sender, C("WordBiomes") + " : ");
				
				//int i = 0;
				StringBuilder line = new StringBuilder();
				List<String> biomes = new ArrayList<String>();
				
				for(Biome b : Biome.values())
				{
					biomes.add(b.name());
				}
				
				Collections.sort(biomes);
				
				List<String> column1 = new ArrayList<String>();
				List<String> column2 = new ArrayList<String>();
				List<String> column3 = new ArrayList<String>();
				
				for(int ctr = 0; ctr < biomes.size(); ctr++)
				{
					if(ctr < biomes.size() / 3)
					{
						column1.add(biomes.get(ctr));
					}else if(ctr < biomes.size() * 2 / 3)
					{
						column2.add(biomes.get(ctr));
					}else{
						column3.add(biomes.get(ctr));
					}
				}
				
				
				for(int ctr = 0; ctr < column1.size(); ctr++)
				{
					String b;
					int nameLength;
					
					b = FormatBiome(column1.get(ctr));
					nameLength = MinecraftFontWidthCalculator.getStringWidth(b);
					line.append(b).append(whitespace(432 - nameLength));
					
					if(ctr < column2.size())
					{
						b = FormatBiome(column2.get(ctr));
						nameLength = MinecraftFontWidthCalculator.getStringWidth(b);
						line.append(b).append(whitespace(432 - nameLength));
					}
					
					if(ctr < column3.size())
					{
						b = FormatBiome(column3.get(ctr));
						line.append(b);
					}
					
					s.sendMessage("" + BLUE + line);
					//i = 0;
					line = new StringBuilder();
									
					/*int nameLength = MinecraftFontWidthCalculator.getStringWidth(b);
					
					i += 1;
					if(i == 3)
					{
						line.append(b);
						s.sendMessage("" + BLUE + line);
						i = 0;
						line = new StringBuilder();
					}
					else
					{
						line.append(b).append(whitespace(318 - nameLength));
					}
				}
			}
			else
			{
				Send(sender, RED + C("MsgPermissionDenied"));
			}
			return true;
		}
		
		private boolean reset(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "PlotMe.admin.reset"))
			{
				if(!PlotManager.isPlotWorld(player))
				{
					Send(player, RED + C("MsgNotPlotWorld"));
				}
				else
				{
					Plot plot = PlotManager.getPlotById(p.getLocation());
					
					if(plot == null)
					{
						Send(player, RED + C("MsgNoPlotFound"));
					}
					else
					{
						if(plot.protect)
						{
							Send(player, RED + C("MsgPlotProtectedCannotReset"));
						}
						else
						{
							String id = plot.getId();
							PlotWorld pwi = PlotManager.getPlotWorld(player);
							
							PlotManager.setBiome(w, id, plot, Biome.PLAINS);
							PlotManager.clear(w, plot);
							//RemoveLWC(w, plot);
							
							if(PlotManager.isEconomyEnabled(p))
							{
								if(plot.isAuctioned())
								{
									String currentbidder = plot.currentbidder;
									
									if(!currentbidder.isEmpty())
									{
										EconomyResponse er = PlotMe.economy.depositPlayer(currentbidder, plot.currentbid);
										
										if(!er.transactionSuccess())
										{
											Send(player, er.errorMessage);
											warn(er.errorMessage);
										}
										else
										{
										    for(Player player : Bukkit.getServer().getOnlinePlayers())
										    {
										        if(player.getName().equalsIgnoreCase(currentbidder))
										        {
										            Send(player, C("WordPlot") + " " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerName() + " " + C("MsgWasReset") + " " + f(plot.currentbid));
										            break;
										        }
										    }
										}
									}
								}
								
								PlotWorld pwi = PlotManager.getPlotWorld(player);
								
								if(pwi.RefundClaimPriceOnReset)
								{
									EconomyResponse er = PlotMe.economy.depositPlayer(plot.owner, pwi.ClaimPrice);
									
									if(!er.transactionSuccess())
									{
										Send(player, RED + er.errorMessage);
										warn(er.errorMessage);
										return true;
									}
									else
									{
									    for(Player player : Bukkit.getServer().getOnlinePlayers())
									    {
									        if(player.getName().equalsIgnoreCase(plot.owner))
									        {
									            Send(player, C("WordPlot") + " " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerName() + " " + C("MsgWasReset") + " " + f(pwi.ClaimPrice));
									            break;
									        }
									    }
									}
								}
							}
							
							if(!PlotManager.isPlotAvailable(player))
							{
								PlotManager.getPlots(p).remove(id);
							}
							
							String name = player.getName();
							
							PlotManager.removeOwnerSign(w, id);
							PlotManager.removeSellSign(w, id);
							
							PlotMeSqlManager.deletePlot(PlotManager.getIdX(id), PlotManager.getIdZ(id), w.getName().toLowerCase());
							
							Send(player, C("MsgPlotReset"));
							
							if(isAdv)
								PlotMe.logger.info(LOG + name + " " + C("MsgResetPlot") + " " + plot.getId());
						}
					}
				}
			}
			else
			{
				Send(player, RED + C("MsgPermissionDenied"));
			}
			return true;
		}
		
		private boolean clear(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "PlotMe.admin.clear") || PlotMe.cPerms(player, "PlotMe.use.clear"))
			{
				if(!PlotManager.isPlotWorld(player))
				{
					Send(player, RED + C("MsgNotPlotWorld"));
				}
				else
				{
					Plot ppi = PlotManager.getPlotAtBlockPosition(player.getLocation());
					if (ppi == null)
					{
						Send(player, RED + C("MsgNoPlotFound"));
					}
					else
					{
						if(!PlotManager.isPlotAvailable(player))
						{
							Plot plot = PlotManager.getPlotById(p,id);
							
							if(plot.protect)
							{
								Send(player, RED + C("MsgPlotProtectedCannotClear"));
							}
							else
							{
								String playername = player.getName();
								
								if(plot.getOwner().getName().equalsIgnoreCase(playername) || PlotMe.cPerms(player, "PlotMe.admin.clear"))
								{
									PlotWorld pwi = PlotManager.getPlotWorld(player);
									
									PlotWorld pwi = PlotManager.getMap(w);
									
									double price = 0;
									
									if(PlotManager.isEconomyEnabled(w))
									{
										price = pwi.ClearPrice;
										double balance = PlotMe.economy.getBalance(playername);
										
										if(balance >= price)
										{
											EconomyResponse er = PlotMe.economy.withdrawPlayer(playername, price);
											
											if(!er.transactionSuccess())
											{
												Send(player, RED + er.errorMessage);
												warn(er.errorMessage);
												return true;
											}
										}
										else
										{
											Send(player, RED + C("MsgNotEnoughClear") + " " + C("WordMissing") + " " + RESET + (price - balance) + RED + " " + PlotMe.economy.currencyNamePlural());
											return true;
										}
									}						
									
									PlotManager.clear(w, plot);
									//RemoveLWC(w, plot, p);
									//PlotManager.regen(w, plot);
									
									Send(player, C("MsgPlotCleared") + " " + f(-price));
									
									if(isAdv)
										PlotMe.logger.info(LOG + playername + " " + C("MsgClearedPlot") + " " + String.valueOf(plot.getId()) + ((price != 0) ? " " + C("WordFor") + " " + price : ""));
								}
								else
								{
									Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgNotYoursNotAllowedClear"));
								}
							}
						}
						else
						{
							Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
						}
					}
				}
			}
			else
			{
				Send(player, RED + C("MsgPermissionDenied"));
			}
			return true;
		}
		
		private boolean add(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "PlotMe.admin.add") || PlotMe.cPerms(player, "PlotMe.use.add"))
			{
				if(!PlotManager.isPlotWorld(player))
				{
					Send(player, RED + C("MsgNotPlotWorld"));
				}
				else
				{
					Plot ppi = PlotManager.getPlotAtBlockPosition(player.getLocation());
					if (ppi == null)
					{
						Send(player, RED + C("MsgNoPlotFound"));
					}
					else
					{
						if(!PlotManager.isPlotAvailable(player))
						{
							if(args.length < 2 || args[1].isEmpty())
							{
								Send(player, C("WordUsage") + " " + RED + "/plotme " + C("CommandAdd") + " <" + C("WordPlayer") + ">");
							}
							else
							{
							
								Plot plot = PlotManager.getPlotById(p,id);
								String playername = player.getName();
								String allowed = args[1];
								
								if(plot.getOwner().getName().equalsIgnoreCase(playername) || PlotMe.cPerms(player, "PlotMe.admin.add"))
								{
									if(plot.isAllowed(allowed))
									{
										Send(player, C("WordPlayer") + " " + RED + args[1] + RESET + " " + C("MsgAlreadyAllowed"));
									}
									else
									{
										PlotWorld pwi = PlotManager.getPlotWorld(player);

										double price = 0;
										
										if(PlotManager.isEconomyEnabled(w))
										{
											price = pwi.AddPlayerPrice;
											double balance = PlotMe.economy.getBalance(playername);
											
											if(balance >= price)
											{
												EconomyResponse er = PlotMe.economy.withdrawPlayer(playername, price);
												
												if(!er.transactionSuccess())
												{
													Send(player, RED + er.errorMessage);
													warn(er.errorMessage);
													return true;
												}
											}
											else
											{
												Send(player, RED + C("MsgNotEnoughAdd") + " " + C("WordMissing") + " " + RESET + f(price - balance, false));
												return true;
											}
										}
										
										plot.addAllowed(args[1]);
										
										Send(player, C("WordPlayer") + " " + RED + allowed + RESET + " " + C("MsgNowAllowed") + " " + f(-price));
										
										if(isAdv)
											PlotMe.logger.info(LOG + playername + " " + C("MsgAddedPlayer") + " " + allowed + " " + C("MsgToPlot") + " " + String.valueOf(plot.getId()) + ((price != 0) ? " " + C("WordFor") + " " + price : ""));
									}
								}
								else
								{
									Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgNotYoursNotAllowedAdd"));
								}
							}
						}
						else
						{
							Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
						}
					}
				}
			}
			else
			{
				Send(player, RED + C("MsgPermissionDenied"));
			}
			return true;
		}
		
		private boolean deny(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "PlotMe.admin.deny") || PlotMe.cPerms(player, "PlotMe.use.deny"))
			{
				if(!PlotManager.isPlotWorld(player))
				{
					Send(player, RED + C("MsgNotPlotWorld"));
				}
				else
				{
					Plot ppi = PlotManager.getPlotAtBlockPosition(player.getLocation());
					if (ppi == null)
					{
						Send(player, RED + C("MsgNoPlotFound"));
					}
					else
					{
						if(!PlotManager.isPlotAvailable(player))
						{
							if(args.length < 2 || args[1].isEmpty())
							{
								Send(player, C("WordUsage") + " " + RED + "/plotme " + C("CommandDeny") + " <" + C("WordPlayer") + ">");
							}
							else
							{
							
								Plot plot = PlotManager.getPlotAtBlockPosition(player);
								String playername = player.getName();
								String denied = args[1];
								
								if(plot.getOwner().getName().equalsIgnoreCase(playername) || PlotMe.cPerms(player, "PlotMe.admin.deny"))
								{
									if(plot.isDenied(denied))
									{
										Send(player, C("WordPlayer") + " " + RED + args[1] + RESET + " " + C("MsgAlreadyDenied"));
									}
									else
									{
										PlotWorld pwi = plot.getPlotWorld();
										
										double price = 0;
										
										if(PlotManager.isEconomyEnabled(pwi.getMinecraftWorld()))
										{
											price = pwi.DenyPlayerPrice;
											double balance = PlotMe.economy.getBalance(playername);
											
											if(balance >= price)
											{
												EconomyResponse er = PlotMe.economy.withdrawPlayer(playername, price);
												
												if(!er.transactionSuccess())
												{
													Send(player, RED + er.errorMessage);
													warn(er.errorMessage);
													return true;
												}
											}
											else
											{
												Send(player, RED + C("MsgNotEnoughDeny") + " " + C("WordMissing") + " " + RESET + f(price - balance, false));
												return true;
											}
										}
										
										/**
										 * TODO: add to denied
										 */
										
										/*if (denied.equals("*"))
										{
											List<Player> deniedplayers = plot.getPlayersInPlot();
											
											for(Player deniedplayer : deniedplayers)
											{
												if(!plot.isAllowed(deniedplayer.getName()))
													deniedplayer.teleport(PlotManager.getPlotHome(w, plot));
											}
										}
										else
										{
											Player deniedplayer = Bukkit.getServer().getPlayer(denied);
											
											if(deniedplayer != null)
											{
												if(deniedplayer.getWorld().equals(w))
												{
													String deniedid = PlotManager.getPlotId(deniedplayer);
													
													if(deniedid.equalsIgnoreCase(id))
													{
														deniedplayer.teleport(PlotManager.getPlotHome(w, plot));
													}
												}
											}
										}
										
										Send(player, C("WordPlayer") + " " + RED + denied + RESET + " " + C("MsgNowDenied") + " " + f(-price));
										
										if(isAdv)
											PlotMe.logger.info(LOG + playername + " " + C("MsgDeniedPlayer") + " " + denied + " " + C("MsgToPlot") + " " + String.valueOf(plot.getId()) + ((price != 0) ? " " + C("WordFor") + " " + price : ""));
									}
								}
								else
								{
									Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgNotYoursNotAllowedDeny"));
								}
							}
						}
						else
						{
							Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
						}
					}
				}
			}
			else
			{
				Send(player, RED + C("MsgPermissionDenied"));
			}
			return true;
		}
		
		private boolean remove(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "PlotMe.admin.remove") || PlotMe.cPerms(player, "PlotMe.use.remove") || canExecuteAdminCommands(player))
			{
				if(!PlotManager.isPlotWorld(player))
				{
					Send(player, RED + C("MsgNotPlotWorld"));
				}
				else
				{
					Plot plot = PlotManager.getPlotAtBlockPosition(player);
					if (plot == null)
					{
						Send(player, RED + C("MsgNoPlotFound"));
					}
					else
					{
						if(!PlotManager.isPlotAvailable(player))
						{
							if(args.length < 2 || args[1].isEmpty())
							{
								Send(player, C("WordUsage") + ": " + RED + "/plotme " + C("CommandRemove") + " <" + C("WordPlayer") + ">");
							}
							else
							{
								
								String playername = player.getName();
								String allowed = args[1];
								
								if(plot.getOwner().getName().equalsIgnoreCase(playername) || PlotMe.cPerms(player, "PlotMe.admin.remove") || canExecuteAdminCommands(player))
								{
									if(plot.isAllowed(allowed))
									{
										PlotWorld pwi = plot.getPlotWorld();
										
										double price = 0;
										
										if(PlotManager.isEconomyEnabled(pwi.getMinecraftWorld()))
										{
											price = pwi.RemovePlayerPrice;
											double balance = PlotMe.economy.getBalance(playername);
											
											if(balance >= price)
											{
												EconomyResponse er = PlotMe.economy.withdrawPlayer(playername, price);
												
												if(!er.transactionSuccess())
												{
													Send(player, RED + er.errorMessage);
													warn(er.errorMessage);
													return true;
												}
											}
											else
											{
												Send(player, RED + C("MsgNotEnoughRemove") + " " + C("WordMissing") + " " + RESET + f(price - balance, false));
												return true;
											}
										}
										
										/**
										 * TODO: remove allowed
										 */
																		
										/*Send(player, C("WordPlayer") + " " + RED + allowed + RESET + " " + C("WorldRemoved") + ". " + f(-price));
										
										if (isAdv)
										{
											PlotMe.logger.info(LOG + playername + " " + C("MsgRemovedPlayer") + " " + allowed + " " + C("MsgFromPlot") + " " + String.valueOf(plot.getId()));
										}
									}
									else
									{
										Send(player, C("WordPlayer") + " " + RED + args[1] + RESET + " " + C("MsgWasNotAllowed"));
									}
								}
								else
								{
									Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgNotYoursNotAllowedRemove"));
								}
							}
						}
						else
						{
							Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
						}
					}
				}
			}
			else
			{
				Send(player, RED + C("MsgPermissionDenied"));
			}
			return true;
		}
		
		private boolean undeny(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "plotme.admin.undeny") || PlotMe.cPerms(player, "plotme.use.undeny") || canExecuteAdminCommands(player))
			{
				if (!PlotManager.isPlotWorld(player))
				{
					Send(player, RED + C("MsgNotPlotWorld"));
					return true;
				}
				
				Plot plot = PlotManager.getPlotAtBlockPosition(player.getLocation());
				if (plot == null)
				{
					Send(player, RED + C("MsgNoPlotFound"));
					return true;
				}

				if (args.length < 2 || args[1].isEmpty())
				{
					Send(player, C("WordUsage") + ": " + RED + "/plot " + C("CommandUndeny") + " <" + C("WordPlayer") + ">");
					return true;
				}
				
				if (plot.getOwner() == null)
				{
					Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
					return true;
				}

				String playername = player.getName();
				String denied = args[1];
								
				if (plot.getOwner().getName().equalsIgnoreCase(playername) || PlotMe.cPerms(player, "plotme.admin.undeny") || canExecuteAdminCommands(player))
				{
					if (plot.isDenied(denied))
					{
						PlotWorld pwi = plot.getPlotWorld();
						
						if (PlotManager.isEconomyEnabled(plot.getMinecraftWorld()))
						{
							double price = pwi.UndenyPlayerPrice;

							if (PlotMe.economy.getBalance(playername) >= price)
							{
								EconomyResponse er = PlotMe.economy.withdrawPlayer(playername, price);
									
								if (!er.transactionSuccess())
								{
		
									Send(player, RED + er.errorMessage);
												  warn(er.errorMessage);
									return true;
								}
								
								Send(player, C("WordPlayer") + " " + RED + denied + RESET + " " + C("MsgNowUndenied") + " " + f(-price));
								return true;
							}
							else
							{
								Send(player, RED + C("MsgNotEnoughUndeny") + " " + C("WordMissing") + " " + RESET + f(price - PlotMe.economy.getBalance(playername), false));
								return true;
							}
						}
						
						Send(player, C("WordPlayer") + " " + RED + denied + RESET + " " + C("MsgNowUndenied"));

						/**
						 * TODO
						 */
						/*if (isAdv)
						{
							PlotMe.logger.info(LOG + playername + " " + C("MsgUndeniedPlayer") + " " + denied + " " + C("MsgFromPlot") + " " + String.valueOf(plot.getId()));
						}
						return true;
					}
					else
					{
						Send(player, C("WordPlayer") + " " + RED + args[1] + RESET + " " + C("MsgWasNotDenied"));
					}
				}
				else
				{
					Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgNotYoursNotAllowedUndeny"));
				}
			}
			else
			{
				Send(player, RED + C("MsgPermissionDenied"));
			}
			return true;
		}
		
		private boolean setowner(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "plotme.admin.setowner") || canExecuteAdminCommands(player))
			{
				if (!PlotManager.isPlotWorld(player))
				{
					Send(player, RED + C("MsgNotPlotWorld"));
					return true;
				}
				
				PlotWorld pwi = PlotManager.getPlotWorld(player);
				if (pwi == null)
				{
					Send(player, RED + C("MsgNotPlotWorld"));
					return true;
				}

				Plot plot = pwi.getPlotAtBlockPosition(player.getLocation());
				if (plot == null)
				{
					Send(player, RED + C("MsgNoPlotFound"));
					return true;
				}

				if (args.length < 2 || args[1].isEmpty())
				{
					Send(player, C("WordUsage") + ": " + RED + "/plot " + C("CommandSetowner") + " <" + C("WordPlayer") + ">");
					return true;
				}
				
				PlotPlayer newowner = PlotManager.getPlotPlayer(args[1]);
				PlotPlayer oldowner = plot.getOwner();

				if (PlotManager.isEconomyEnabled(player))
				{
					EconomyResponse er;
					
					if (pwi.RefundClaimPriceOnSetOwner && newowner != oldowner)
					{
						er = PlotMe.economy.depositPlayer(oldowner.getName(), pwi.ClaimPrice);
						
						if (!er.transactionSuccess())
						{
							Send(player, RED + er.errorMessage);
										  warn(er.errorMessage);
							return true;
						}
						
						if (oldowner.getPlayer() != null)
						{
							if (PlotMe.useDisplayNamesInMessages)
							{
								Send(oldowner.getPlayer(), C("MsgYourPlot") + " " + String.valueOf(plot.getId()) + " " + C("MsgNowOwnedBy") + " " + newowner.getDisplayName() + ". " + f(pwi.ClaimPrice));
							}
							else
							{
								Send(oldowner.getPlayer(), C("MsgYourPlot") + " " + String.valueOf(plot.getId()) + " " + C("MsgNowOwnedBy") + " " + newowner.getName() + ". " + f(pwi.ClaimPrice));
							}
						}

						if (plot.isAuctioned())
						{
							PlotAuctionBid highestbid = plot.getAuctionBid(0);
							if (highestbid != null && highestbid.getMoneyAmount()>0)
							{
								er = PlotMe.economy.depositPlayer(highestbid.getBidderName(), highestbid.getMoneyAmount());	
								
								if (!er.transactionSuccess())
								{
									Send(player, er.errorMessage);
										    warn(er.errorMessage);
								}
								else
								{
									Send(Bukkit.getPlayerExact(highestbid.getBidderName()), C("WordPlot") + " " + String.valueOf(plot.getId()) + " " + C("MsgChangedOwnerFrom") + " " + oldowner.getName() + " " + C("WordTo") + " " + newowner.getName() + ". " + f(highestbid.getMoneyAmount()));
								}
							}
						}
					}
				}

				plot.setOwner(newowner);
				Send(player, C("MsgOwnerChangedTo") + " " + RED + newowner.getName());
							
				if (isAdv)
				{
					PlotMe.logger.info(LOG + player.getName() + " " + C("MsgChangedOwnerOf") + " " + String.valueOf(plot.getId()) + " " + C("WordFrom") + " " + oldowner.getName() + " " + C("WordTo") + " " + newowner.getName());
				}
			}
			else
			{
				Send(player, RED + C("MsgPermissionDenied"));
			}
			return true;
		}*/
		
		private boolean id(CommandSender sender, String[] args)
		{
			Player player = null;
			if (sender instanceof Player) {
				player = (Player)sender;
			} else {
				return true;
			}
			if (PlotMe.cPerms(sender, "plotme.admin.id"))
			{
				if (!PlotManager.isPlotWorld(player))
				{
					Send(sender, ChatColor.RED + C("MsgNotPlotWorld"));
				}
				else
				{
					Plot plot = PlotManager.getPlotAtLocation(player.getLocation());
					if (plot == null)
					{
						Send(sender, ChatColor.RED + C("MsgNoPlotFound"));
					}
					else
					{
						sender.sendMessage(ChatColor.BLUE + C("WordPlot") + " " + C("WordId") + ": " + ChatColor.RESET + String.valueOf(plot.getId()));
						
						Pair<Location, Location> locations = plot.getWorldMinMaxBlockLocations();
						
						sender.sendMessage(ChatColor.BLUE + C("WordBottom") + ": " + ChatColor.RESET + locations.getLeft().getBlockX() + ChatColor.BLUE + "," + ChatColor.RESET + locations.getLeft().getBlockZ());
						sender.sendMessage(ChatColor.BLUE + C("WordTop") + ": " + ChatColor.RESET + locations.getRight().getBlockX() + ChatColor.BLUE + "," + ChatColor.RESET + locations.getRight().getBlockZ());
					}
				}
			}
			else
			{
				Send(sender, ChatColor.RED + C("MsgPermissionDenied"));
			}
			return true;
		}
	
		public static String C(String caption)
		{
			if (caption != null && !caption.isEmpty())
			{
				return PlotMe.caption(caption);
			}
			return "(none)";
		}
	
	public static void Send(CommandSender sender, String text)
	{
		if (sender != null && !text.isEmpty())
		{
			sender.sendMessage(PlotMe.PREFIX + text);
		}
	}
	
	public static String FormatBiome(String biome)
	{
		biome = biome.toLowerCase();
		String[] tokens = biome.split("_");
		biome = "";
		for (String token : tokens)
		{
			token = token.substring(0, 1).toUpperCase() + token.substring(1);
			if (biome.isEmpty())
			{
				biome = token;
			}
			else
			{
				biome = biome + "_" + token;
			}
		}
		return biome;
	}

	public static void warn(String msg)
	{
		PlotMe.logger.warning("[" + PlotMe.NAME + "] " + msg);
	}
	
	public static String f(double price)
	{
		return f(price, true);
	}
	
	public static String f(double price, boolean showsign)
	{
		if (price == 0)
		{
			return "";
		}
		String format = String.valueOf(Math.round(Math.abs(price) * 100) / 100);
		if (PlotMe.economy != null)
		{
			format = (price <= 1 && price >= -1) ? format + " " + PlotMe.economy.currencyNameSingular() : format + " " + PlotMe.economy.currencyNamePlural();
		}
		if (showsign)
		{
			return ChatColor.GREEN + ((price > 0) ? "+" + format : "-" + format);
		}
		else
		{
			return ChatColor.GREEN + format;
		}
	}

	
}
