package com.worldcretornica.plotme.commands;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import com.worldcretornica.plotme.PlotMe;
import com.worldcretornica.plotme.commands.CommandMovePlot;
import com.worldcretornica.plotme.utils.MinecraftFontWidthCalculator;
import com.worldcretornica.plotme.utils.FinishedPlotsComparator;
import com.worldcretornica.plotme.utils.Pair;
import com.worldcretornica.plotme.utils.RunnableExpiredPlotsRemover;

import org.bukkit.Bukkit;

public class PlotMeCommands implements CommandExecutor {
	
	public static PlotMe plugin;
	public static CommandHelp helpCommandRunner;

	public static Map<String, PlotMeCommandBase> commands;
	
	public PlotMeCommands(PlotMe pl)
	{
		plugin = pl;
		
		commands.clear();
		
		// USER COMMANDS
		helpCommandRunner = new CommandHelp(this);
		commands.put(C("CommandHelp"), helpCommandRunner);
		commands.put(C("CommandAuto"), new CommandAuto(this));
		commands.put(C("CommandClaim"), new CommandClaim(this));
		
		// ADMIN COMMANDS
		commands.put(C("CommandMove"), new CommandMovePlot(this));
		commands.put(C("CommandReload"), new CommandReload(this));
		commands.put(C("CommandReset"), null); /** TODO **/
		commands.put(C("CommandResetExpired"), new CommandResetExpired(this));

	}
	
	public PlotMe getPlugin()
	{
		return plugin;
	}
		
	public boolean onCommand(CommandSender sender, Command command, String start, String[] args)
	{
		String cmd = command.getName().toLowerCase();
		if (cmd.equals("plot") || cmd.equals("plotme") || cmd.equals("p"))
		{
			if (args.length == 0)
			{
				helpCommandRunner.run(sender, args);
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
					cmdClass.run(sender, args);
					return true;
				}

				/**
				 * TODO: see below (convert to command classes)
				 */
				if (sender instanceof Player)
						{
							Player player = (Player)sender;
		
							if (arg0.equals(C("CommandClaim")))
							{
								/**
								 * TODO: FIRST CONFIGURABLE X PLOTS ARE FOR FREE (after that it costs something)
								 */
								return claim(player, args);
							}
							if (arg0.equals(C("CommandInfo")) || arg0.equals("i"))
							{
								return info(player, args);
							}
							if (arg0.equals(C("CommandComment")))
							{
								return comment(player, args);
							}
							if (arg0.equals(C("CommandTp")))
							{
								return tp(player, args);
							}
							if (arg0.equals(C("CommandBuy")))
							{
								return buy(player, args);
							}
							if (arg0.equals(C("CommandBid")))
							{
								return bid(player, args);
							}
							if (arg0.startsWith(C("CommandHome")) || arg0.startsWith("h"))
							{
								return home(player, args);
							}
							if (arg0.equals(C("CommandSell")))
							{
								return sell(player, args);
							}
							if (arg0.equals(C("CommandBiome")) || arg0.equals("b"))
							{
								return biome(player, args);
							}
							if (arg0.equals(C("CommandBiomelist")))
							{
								return biomelist(sender, args);
							}
							if (arg0.equals(C("CommandClear")))
							{
								return clear(player, args);
							}
							if (arg0.equals(C("CommandAdd")) || arg0.equals("+"))
							{
								return add(player, args);
							}
							if (arg0.equals(C("CommandDeny")))
							{
								return deny(player, args);
							}
							if (arg0.equals(C("CommandUndeny")))
							{
								return undeny(player, args);
							}
							if (arg0.equals(C("CommandRemove")) || arg0.equals("-"))
							{
								return remove(player, args);
							}
							if (arg0.equals(C("CommandSetowner")) || arg0.equals("o"))
							{
								return setowner(player, args);
							}
							if (arg0.equals(C("CommandMove")) || arg0.equals("m"))
							{
								return move(sender, args);
							}
							if (arg0.equals(C("CommandWEAnywhere")))
							{
								return weanywhere(player, args);
							}
							if (arg0.equals(C("CommandProtect")))
							{
								return protect(player, args);
							}
						}
					}

					if (arg0.equals(C("CommandComments")) || arg0.equals("c"))
					{
						return comments(sender, args);
					}
					if (arg0.equals(C("CommandId")))
					{
						return id(sender, args);
					}
					if (arg0.equals("reload"))
					{
						return reload(sender, args);
					}
					if (arg0.equals(C("CommandList")))
					{
						return plotlist(sender, args);
					}
					if (arg0.equals(C("CommandExpired")))
					{
						return expired(sender, args);
					}
					if (arg0.equals(C("CommandAddtime")))
					{
						return addtime(sender, args);
					}
					if (arg0.equals(C("CommandDone")))
					{
						return done(sender, args);
					}
					if (arg0.equals(C("CommandDoneList")))
					{
						return donelist(sender, args);
					}
					if (arg0.equals(C("CommandDispose")))
					{
						return dispose(sender, args);
					}
					if (arg0.equals(C("CommandAuction")))
					{
						return auction(sender, args);
					}
					if (arg0.equals(C("CommandResetExpired")))
					{
						return resetexpired(player, args);
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
					else
					{
						String arg0 = args[0].toString().toLowerCase();
						if (arg0.equals("reload"))
						{
							return reload(sender, args);
						}
						if (arg0.equals(C("CommandResetExpired")))
						{
							return resetexpired(sender, args);
						}
					}
				}
			}
			return false;
		}


		private boolean bid(Player bidder, String[] args) 
		{	
			if (PlotManager.isEconomyEnabled(bidder))
			{
				if (PlotMe.cPerms(bidder, "plotme.use.bid") || canExecuteAdminCommands(bidder))
				{
					Plot plot = PlotManager.getPlotAtBlockPosition(bidder);
					
					if (plot == null)
					{
						Send(bidder, RED + C("MsgNoPlotFound"));
					}
					else
					{
						if (plot.isAuctioned())
						{
							if (plot.getOwner().getName().equals(bidder.getName()))
							{
								Send(bidder, RED + C("MsgCannotBidOwnPlot"));
							}
							else
							{
								if (args.length == 2)
								{
									double bid = 0;

									try  
									{  
										bid = Double.parseDouble(args[1]);  
									}  
									catch( Exception ex) {}
									
									PlotAuctionBid highestBid = plot.getHighestAuctionBid();
									
									if (bid <= highestBid.getMoneyAmount() && highestBid != null)
									{
										Send(bidder, RED + C("MsgInvalidBidMustBeAbove") + " " + RESET + f(highestBid.getMoneyAmount(), false));
									}
									else
									{
										double balance = PlotMe.economy.getBalance(bidder.getName());
										
										if (bid > balance || (highestBid != null && balance < highestBid.getMoneyAmount()))
										{
											Send(bidder, RED + C("MsgNotEnoughBid"));
										}
										else
										{
											EconomyResponse er = PlotMe.economy.withdrawPlayer(bidder.getName(), bid);
											if (er.transactionSuccess())
											{
												if (highestBid != null)
												{
													EconomyResponse er2 = PlotMe.economy.depositPlayer(highestBid.getBidderName(), highestBid.getMoneyAmount());
													if (!er2.transactionSuccess())
													{
														Send(bidder, er2.errorMessage);
														warn(er2.errorMessage);
													}
													else
													{
														if (plot.getOwner() != null)
														{
															for (Player player : Bukkit.getServer().getOnlinePlayers())
															{
																if (player.getName().equalsIgnoreCase(highestBid.getBidderName()))
																{
																	if (PlotMe.useDisplayNamesInMessages)
																	{
																		Send(player, C("MsgOutbidOnPlot") + " " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerDisplayName() + " > " + f(bid));
																	}
																	else
																	{
																		Send(player, C("MsgOutbidOnPlot") + " " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerName() + " > " + f(bid));
																	}
																	break;
																}
															}
														}
														else
														{
															for (Player player : Bukkit.getServer().getOnlinePlayers())
															{
																if (player.getName().equalsIgnoreCase(highestBid.getBidderName()))
																{
																	Send(player, C("MsgOutbidOnPlot") + " " + String.valueOf(plot.getId()) + " > " + f(bid));
																	break;
																}
															}
														}
													}
												}
												
												if (isAdv)
												{
													PlotMe.logger.info(LOG + bidder.getName() + " bid " + String.valueOf(bid) + " on plot " + String.valueOf(plot.getId()));
												}
												
												if (plot.addAuctionBid(PlotDatabase.getPlotPlayer(bidder.getName(), bidder.getDisplayName()), bid))
												{
													Send(bidder, C("MsgBidAccepted") + " " + f(-bid));
												}
												else
												{
													Send(bidder, er.errorMessage);
													warn(er.errorMessage);
												}
											}
										}
									}
								}
								else
								{
									Send(bidder, C("WordUsage") + ": " + RED + "/plotme " + 
												 C("CommandBid") + " <" + C("WordAmount") + "> " + 
												 RESET + C("WordExample") + ": " + RED + "/plotme " + C("CommandBid") + " 100");
								}
							}
						}
						else
						{
							Send(bidder, RED + C("MsgPlotNotAuctionned"));
						}
					}
				}
				else
				{
					Send(bidder, RED + C("MsgPermissionDenied"));
				}
			}
			else
			{
				Send(bidder, RED + C("MsgEconomyDisabledWorld"));
			}
			return true;
		}

		private boolean buy(Player player, String[] args) 
		{
			if (PlotManager.isEconomyEnabled(player))
			{
				if (PlotMe.cPerms(player, "plotme.use.buy") || PlotMe.cPerms(player, "plotme.admin.buy") || canExecuteAdminCommands(player))
				{
					Plot plot = PlotManager.getPlotAtBlockPosition(player);
					if (plot == null)
					{
						Send(player, RED + C("MsgNoPlotFound"));
						return true;
					}

					if (!plot.isAvailable() || !plot.isForSale())
					{
						Send(player, RED + C("MsgPlotNotForSale"));
						return true;
					}

					String buyer = player.getName();
					if (buyer.isEmpty())
					{
						Send(player, RED + C("MsgPlayerDataError"));
						return true;
					}
					
					PlotPlayer newowner = PlotManager.getPlotPlayer(player);
					if (newowner == null)
					{
						Send(player, RED + C("MsgPlayerDataError"));
						return true;
					}
					
					PlotPlayer oldowner = plot.getOwner();
					if (oldowner == null)
					{
						Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
						return true;
					}
					
					if (oldowner.equals(newowner) || plot.getOwner().getName().equals(buyer))
					{
						Send(player, RED + C("MsgCannotBuyOwnPlot"));
						return true;
					}

					int plotlimit = PlotMe.getPlotLimit(player);

					if (plotlimit != -1 && newowner.getOwnPlotsCount()>=plotlimit && !canExecuteAdminCommands(player))
					{
						Send(player, C("MsgAlreadyReachedMaxPlots") + " (" + 
										String.valueOf(newowner.getOwnPlotsCount()) + "/" + String.valueOf(plotlimit) + "). " + 
										C("WordUse") + " " + RED + "/plotme " + C("CommandHome") + RESET + " " + C("MsgToGetToIt"));
						return true;
					}

					double cost = plot.getPrice();
					if (cost < 0)
					{
						Send(player, RED + C("MsgPlotNotForSale"));
						return true;
					}

					if (cost > 0)
					{
						if (PlotMe.economy.getBalance(buyer) >= cost)
						{
							EconomyResponse er = PlotMe.economy.withdrawPlayer(buyer, cost);
							if (er.transactionSuccess())
							{
								if (oldowner.getId() > 0) // owner with id 0 is the bank
								{
									EconomyResponse er2 = PlotMe.economy.depositPlayer(oldowner.getName(), cost);
									if (!er2.transactionSuccess())
									{
										Send(player, RED + er2.errorMessage);
										warn(er2.errorMessage);
										if (isAdv)
										{
											PlotMe.logger.info(LOG + buyer + " " + C("MsgBoughtPlot") + " " + String.valueOf(plot.getId()) + " " + er.errorMessage);
										}
									}
								}
								else
								{
									EconomyResponse er2 = PlotMe.economy.bankDeposit("$PlotMeBank$", cost);
								}
							}
							else
							{
								Send(player, RED + er.errorMessage);
								warn(er.errorMessage);
								if (isAdv)
								{
									PlotMe.logger.info(LOG + buyer + " " + C("MsgBoughtPlot") + " " + String.valueOf(plot.getId()) + " " + er.errorMessage);
								}
								return true;
							}
						}
						else
						{
							Send(player, RED + C("MsgNotEnoughBuy"));
							return true;
						}
					}
								
					/*if (oldowner.getPlayer() != null)
					{
						Send(oldowner.getPlayer(), C("WordPlot") + " " + String.valueOf(plot.getId()) + " " + 
												   C("MsgSoldTo") + " " + buyer + ". " + f(cost));
					}*/
					
					for (Player pl : Bukkit.getServer().getOnlinePlayers())
					{
						if (pl.getName() != buyer)
						{
							Send(pl, C("WordPlot") + " " + String.valueOf(plot.getId()) + " " + 
									 C("MsgSoldTo") + " " + buyer + ". " + f(cost));
						}
					}
							
					plot.disableSelling();
					plot.disableAuctioning();
					
					// SET THE NEW OWNER
					plot.setOwner(newowner);
					
					Send(player, C("MsgPlotBought") + " " + f(-cost));

					if (isAdv)
					{
						PlotMe.logger.info(LOG + buyer + " " + C("MsgBoughtPlot") + " " + String.valueOf(plot.getId()) + " " + C("WordFor") + " " + f(cost));
					}
					return true;
				}
				else
				{
					Send(player, RED + C("MsgPermissionDenied"));
				}
			}
			else
			{
				Send(player, RED + C("MsgEconomyDisabledWorld"));
			}
			return true;
		}

		private boolean auction(CommandSender sender, String[] args) 
		{
			if (PlotMe.cPerms(sender, "plotme.use.auction") || PlotMe.cPerms(sender, "plotme.admin.auction") || canExecuteAdminCommands(sender))
			{
				Plot plot = null;
				Player player = null;
				if (sender instanceof Player)
				{
					player = (Player)sender;
					if (args.length < 2)
					{
						plot = PlotManager.getPlotAtBlockPosition(player);
					}
				}
				else
				{
					if (args.length < 2)
					{
						Send(sender, RED + C("WordMissing"));
						return true;
					}
				}
				
				if (args.length >= 2)
				{
					int plotid = 0;
					try
					{
						plotid = Integer.parseInt(args[1]);
						plot = PlotManager.getPlot(plotid);
					}
					catch (NumberFormatException ex)
					{
						Send(sender, C("MsgInvalidNumber"));
						return true;
					}
				}
		
				if (plot == null)
				{
					Send(sender, RED + C("MsgNoPlotFound"));
					return true;
				}
				
				if (!PlotManager.isEconomyEnabled(plot))
				{
					Send(sender, RED + C("MsgSellingPlotsIsDisabledWorld"));
					if (plot.isAuctioned())
					{
						plot.disableAuctioning();
					}
					return true;
				}
				
				PlotWorld pwi = plot.getPlotWorld();
				if (pwi != null)
				{
					if (!pwi.CanPutOnSale)
					{
						Send(sender, RED + C("MsgEconomyDisabledWorld"));
						return true;
					}
					if (plot.getOwner() != null)
					{
						if ((player != null && plot.getOwnerName().equals(player.getName())) || PlotMe.cPerms(sender, "plotme.admin.auction") || canExecuteAdminCommands(sender))
						{
							if (plot.isAuctioned())
							{
								PlotAuctionBid highestbid = plot.getHighestAuctionBid();
								if (highestbid != null)
								{
									if (PlotMe.cPerms(sender, "plotme.admin.auction") || canExecuteAdminCommands(sender))
									{
										EconomyResponse er = PlotMe.economy.depositPlayer(highestbid.getBidderName(), highestbid.getMoneyAmount());
										if (!er.transactionSuccess())
										{
											Send(sender, RED + er.errorMessage);
											warn(er.errorMessage);
										}
										else
										{
										    for (Player pl : Bukkit.getServer().getOnlinePlayers())
										    {
									        	if (PlotMe.useDisplayNamesInMessages)
									        	{
									        		Send(pl, C("MsgAuctionCancelledOnPlot") + 
									        				" " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerName() + ". " + f(highestbid.getMoneyAmount()));
									        	}
									        	else
									        	{
									        		Send(pl, C("MsgAuctionCancelledOnPlot") + 
									        				" " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerDisplayName() + ". " + f(highestbid.getMoneyAmount()));
									        	}
										    }
										}			
										
										plot.disableAuctioning();
										Send(sender, C("MsgAuctionCancelled"));
										
										if (isAdv)
										{
											PlotMe.logger.info(LOG + sender.getName() + " " + C("MsgStoppedTheAuctionOnPlot") + " " + String.valueOf(plot.getId()));
										}
									}
									else
									{
										Send(sender, RED + C("MsgPlotHasBidsAskAdmin"));
										return true;
									}
								}
							}
							else
							{									
								double bid = 0;
						
								if (args.length == 2)
								{
									try  
									{  
										bid = Double.parseDouble(args[1]);  
									}  
									catch (Exception ex) {}
								}
								
								if (bid >= 1)
								{
									plot.enableAuctioning();
									Send(sender, C("MsgAuctionStarted"));
									
									if (isAdv)
									{
										PlotMe.logger.info(LOG + sender.getName() + " " + C("MsgStartedAuctionOnPlot") + " " + String.valueOf(plot.getId()) + " " + C("WordAt") + " " + String.valueOf(bid));
									}
								}
								else
								{
									Send(sender, RED + C("MsgInvalidAmount"));
									return true;
								}
							}
						}
						else
						{
							Send(sender, RED + C("MsgDoNotOwnPlot"));
							return true;
						}
					}
					else
					{
						Send(sender, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
						return true;
					}
				}
				else
				{
					Send(sender, RED + C("MsgEconomyDisabledWorld"));
				}
			}
			else
			{
				Send(sender, RED + C("MsgPermissionDenied"));
				return true;
			}
			return true;
		}

		private boolean dispose(CommandSender sender, String[] args) 
		{
			if (PlotMe.cPerms(sender, "plotme.admin.dispose") || PlotMe.cPerms(sender, "plotme.use.dispose") || canExecuteAdminCommands(sender))
			{
				Plot plot = null;
				if (sender instanceof Player)
				{
					if (args.length < 2)
					{
						plot = PlotManager.getPlotAtBlockPosition((Player)sender);
					}
				}
				else
				{
					if (args.length < 2)
					{
						Send(sender, RED + C("WordMissing"));
						return true;
					}
				}
				
				if (args.length >= 2)
				{
					int plotid = 0;
					try
					{
						plotid = Integer.parseInt(args[1]);
						plot = PlotManager.getPlot(plotid);
					}
					catch (NumberFormatException ex)
					{
						Send(sender, C("MsgInvalidNumber"));
						return true;
					}
				}
				
				if (plot == null)
				{
					Send(sender, RED + C("MsgNoPlotFound"));
					return true;
				}
				
				if (!plot.isAvailable())
				{
					Send(sender, C("MsgPlotDisposedAnyoneClaim"));
				}

				if (!PlotManager.isPlotWorld(plot.getPlotWorld()))
				{
					Send(sender, RED + C("MsgNotPlotWorld"));
					return true;
				}

				if (plot.isProtected())
				{
					Send(sender, RED + C("MsgPlotProtectedNotDisposed"));
					return true;
				}
				
				if (plot.getOwner() == null)
				{
					Send(sender, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
					return true;
				}
				
				double cost = plot.getPlotWorld().DisposePrice;
				
				if (sender instanceof Player)
				{
					Player player = (Player)sender;
					if (plot.getOwnerName().equals(player.getName()) || PlotMe.cPerms(player, "plotme.admin.dispose") || canExecuteAdminCommands(player))
					{
						if (PlotManager.isEconomyEnabled(plot))
						{
							if (cost != 0 && PlotMe.economy.getBalance(player.getName()) < cost)
							{
								Send(player, RED + C("MsgNotEnoughDispose"));
								return true;
							}
						}
					}
					else
					{
						Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgNotYoursCannotDispose"));
						return true;
					}
					EconomyResponse er = PlotMe.economy.withdrawPlayer(player.getName(), cost);
					if (!er.transactionSuccess())
					{	
						Send(player, RED + er.errorMessage);
						warn(er.errorMessage);
						return true;
					}
				}
				
				if (plot.isAuctioned())
				{
					PlotAuctionBid highestbid = plot.getHighestAuctionBid();
					if (highestbid != null)
					{
						if (PlotMe.cPerms(sender, "plotme.admin.dispose") || canExecuteAdminCommands(sender))
						{
							EconomyResponse er2 = PlotMe.economy.depositPlayer(highestbid.getBidderName(), highestbid.getMoneyAmount());
							if (!er2.transactionSuccess())
							{
								Send(sender, RED + er2.errorMessage);
								warn(er2.errorMessage);
							}
						}
						else
						{
							Send(sender, RED + C("MsgPlotHasBidsAskAdmin"));
							return true;
						}
					}
				}
				
				PlotManager.removePlot(plot);
				if (isAdv)
				{
					PlotMe.logger.info(LOG + sender.getName() + " " + C("MsgDisposedPlot") + " " + String.valueOf(plot.getId()));
				}
				
				for (Player pl : Bukkit.getServer().getOnlinePlayers())
				{
			    	if (PlotMe.useDisplayNamesInMessages)
					{
			    		Send(pl, C("WordPlot") + 
							     " " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerDisplayName() + " " + C("MsgWasDisposed") + " " + f(cost));
					}
					else
					{
						Send(pl, C("WordPlot") + 
						   		 " " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerName() + " " + C("MsgWasDisposed") + " " + f(cost));
					}
					break;
				}
			}
			else
			{
				Send(sender, RED + C("MsgPermissionDenied"));
			}
				
			return true;
		}

		private boolean sell(Player player, String[] args) 
		{
			if (PlotManager.isEconomyEnabled(player))
			{
				PlotWorld pwi = PlotManager.getPlotWorld(player.getWorld());
				
				if (pwi != null)
				{
					if (pwi.CanSellToBank || pwi.CanPutOnSale)
					{
						if (PlotMe.cPerms(player, "plotme.use.sell") || PlotMe.cPerms(player, "plotme.admin.sell") || canExecuteAdminCommands(player))
						{
							Plot plot = PlotManager.getPlotAtBlockPosition(player);
							
							if (plot == null)
							{
								Send(player, RED + C("MsgNoPlotFound"));
							}
							else
							{
								if (plot.getOwner() != null)
								{
									if (plot.getOwnerName().equals(player.getName()) || PlotMe.cPerms(player, "plotme.admin.sell") || canExecuteAdminCommands(player))
									{
										if (plot.isForSale())
										{
											plot.disableSelling();
											
											Send(player, C("MsgPlotNoLongerSale"));
												
											if (isAdv)
											{
												PlotMe.logger.info(LOG + player.getName() + " " + C("MsgRemovedPlot") + " " + String.valueOf(plot.getId()) + " " + C("MsgFromBeingSold"));
											}
										}
										else
										{
											double price = pwi.SellToPlayerPrice;
											boolean bank = false;
												
											if (args.length == 2)
											{
												if (args[1].equalsIgnoreCase("bank"))
												{
													bank = true;
												}
												else
												{
													if (pwi.CanCustomizeSellPrice)
													{
														try  
														{  
															price = Double.parseDouble(args[1]);  
														}  
														catch (Exception e)
														{
															if (pwi.CanSellToBank)
															{
																Send(player, C("WordUsage") + ": " + RED + " /plot " + C("CommandSellBank") + "|<" + C("WordAmount") + ">");
																player.sendMessage("  " + C("WordExample") + ": " + RED + "/plot " + C("CommandSellBank") + " " + RESET + " or " + RED + " /plot " + C("CommandSell") + " 200");
															}
															else
															{
																Send(player, C("WordUsage") + ": " + RED + 
																		" /plot " + C("CommandSell") + " <" + C("WordAmount") + ">" + RESET + 
																		" " + C("WordExample") + ": " + RED + "/plot " + C("CommandSell") + " 200");
															}
														}
													}
													else
													{
														Send(player, RED + C("MsgCannotCustomPriceDefault") + " " + f(price));
														return true;
													}
												}
											}
											
											if (bank)
											{
												if (!pwi.CanSellToBank)
												{
													Send(player, RED + C("MsgCannotSellToBank"));
												}
												else
												{
													PlotAuctionBid highestBid = plot.getAuctionBid(0);
													
													if (highestBid != null)
													{
														double bid = highestBid.getMoneyAmount();
														
														EconomyResponse er = PlotMe.economy.depositPlayer(highestBid.getBidderName(), bid);
														
														if (!er.transactionSuccess())
														{
															Send(player, RED + er.errorMessage);
															warn(er.errorMessage);
														}
														else
														{
															for(Player pl : Bukkit.getServer().getOnlinePlayers())
															{
																if (pl.getName().equals(highestBid.getBidderName()))
																{
																	if (PlotMe.useDisplayNamesInMessages)
																	{
																		Send(player, C("WordPlot") + " " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerDisplayName() + " " + C("MsgSoldToBank") + " " + f(bid));
																	}
																	else
																	{
																		Send(player, C("WordPlot") + " " + String.valueOf(plot.getId()) + " " + C("MsgOwnedBy") + " " + plot.getOwnerName() + " " + C("MsgSoldToBank") + " " + f(bid));
																	}
																	break;
																}
															}
														}
													}
														
													EconomyResponse er = PlotMe.economy.depositPlayer(player.getName(), pwi.SellToBankPrice);
														
													if (er.transactionSuccess())
													{
														plot.setOwner(PlotManager.getPlotPlayer("$Bank$"));
														plot.setPrice(pwi.BuyFromBankPrice);
														plot.enableSelling();
							
														plot.setProtected(true);
																										
														Send(player, C("MsgPlotSold") + " " + f(pwi.BuyFromBankPrice));
														
														if (isAdv)
														{
															PlotMe.logger.info(LOG + player.getName() + " " + C("MsgSoldToBankPlot") + " " + String.valueOf(plot.getId()) + " " + C("WordFor") + " " + f(pwi.BuyFromBankPrice));
														}
													}
													else
													{
														Send(player, " " + er.errorMessage);
														warn(er.errorMessage);
													}
												}
											}
											else
											{
												if (price < 0)
												{
													Send(player, RED + C("MsgInvalidAmount"));
												}
												else
												{
													plot.setPrice(price);
													plot.enableSelling();
			
													Send(player, C("MsgPlotForSale"));
													
													if (isAdv)
													{
														PlotMe.logger.info(LOG + player.getName() + " " + C("MsgPutOnSalePlot") + " " + String.valueOf(plot.getId()) + " " + C("WordFor") + " " + f(price));
													}
												}
											}
										}
									}
									else
									{
										Send(player, RED + C("MsgDoNotOwnPlot"));
									}
								}
								else
								{
									Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
								}
							}
						}
						else
						{
							Send(player, RED + C("MsgPermissionDenied"));
						}
					}
					else
					{
						Send(player, RED + C("MsgSellingPlotsIsDisabledWorld"));
					}
				}
				else
				{
					Send(player, RED + C("MsgEconomyDisabledWorld"));
				}
			}
			return true;
		}

		private boolean protect(Player player, String[] args) 
		{
			if (PlotMe.cPerms(player, "plotme.admin.protect") || PlotMe.cPerms(player, "plotme.use.protect") || canExecuteAdminCommands(player))
			{
				PlotWorld pwi = PlotManager.getPlotWorld(player.getWorld());
				if (pwi == null)
				{
					Send(player, RED + C("MsgNotPlotWorld"));
					return true;
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
						if (plot.getOwner() != null)
						{
							if (plot.getOwnerName().equals(player.getName()) || PlotMe.cPerms(player, "plotme.admin.protect") || canExecuteAdminCommands(player))
							{
								if (plot.isProtected())
								{
									plot.setProtected(false);
									
									Send(player, C("MsgPlotNoLongerProtected"));
										
									if (isAdv)
									{
										PlotMe.logger.info(LOG + player.getName() + " " + C("MsgUnprotectedPlot") + " " + String.valueOf(plot.getId()));
									}
								}
								else
								{
									double cost = 0;
										
									if (PlotManager.isEconomyEnabled(player))
									{
										cost = pwi.ProtectPrice;
											
										if (PlotMe.economy.getBalance(player.getName()) < cost)
										{
											Send(player, RED + C("MsgNotEnoughProtectPlot"));
											return true;
										}
										else
										{
											EconomyResponse er = PlotMe.economy.withdrawPlayer(player.getName(), cost);
											
											if (!er.transactionSuccess())
											{
												Send(player, RED + er.errorMessage);
												warn(er.errorMessage);
												return true;
											}
										}
									}
										
									plot.setProtected(true);
		
									Send(player, C("MsgPlotNowProtected") + " " + f(-cost));
										
									if (isAdv)
									{
										PlotMe.logger.info(LOG + player.getName() + " " + C("MsgProtectedPlot") + " " + String.valueOf(plot.getId()));
									}	
								}
							}
							else
							{
								Send(player, RED + C("MsgDoNotOwnPlot"));
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

		private boolean donelist(CommandSender sender, String[] args) 
		{
			if (PlotMe.cPerms(sender, "plotme.admin.done") || canExecuteAdminCommands(sender))
			{
				List<Integer> finishedplots = PlotDatabase.getFinishedPlots();
				
				if (finishedplots == null || finishedplots.size() == 0)
				{
					Send(sender, C("MsgNoPlotsFinished"));
					return true;
				}
				
				int nbfinished = 0;
				int page = 1;
				
				if (args.length >= 2)
				{
					try
					{
						page = Integer.parseInt(args[1]);
					}
					catch(NumberFormatException ex)
					{
						Send(sender, RED + C("MsgInvalidPageNumber"));
						page = 1;
					}
				}
				
				if (page < 1)
				{
					page = 1;
				}
				
				int minIndex = (page-1) * 8;
				int maxIndex = Math.min(minIndex + 8, finishedplots.size());
				
				int maxPage = (int)Math.ceil((double)finishedplots.size() / (double)8);
				if (page > maxPage)
				{
					page = maxPage;
				}
				
				Send(sender, C("MsgFinishedPlotsPage") + " " + page + " / " + maxPage);
				
				int finId;

				Iterator<Integer> fini = finishedplots.iterator();
				int textLength;
				int daysAgo;
				long currentTime = Math.round(System.currentTimeMillis() / 1000);
				while (fini.hasNext() && nbfinished < maxIndex)
				{
					finId = fini.next();
					if (finId > 0 && nbfinished >= minIndex)
					{
						Plot plot = PlotManager.getPlot(finId);
						if (plot != null)
						{
							daysAgo = (int)Math.floor((currentTime - plot.getFinish()) / 86400);
							sender.sendMessage(" " + BLUE + String.valueOf(nbfinished) + String.valueOf(plot.getId()) + RESET + " -> " + plot.getOwnerName() + " (" + String.valueOf(daysAgo) + " days ago)");
						}
						else
						{
							sender.sendMessage(" " + RED + String.valueOf(nbfinished) + ". NULL");
						}
					}
					else
					{
						sender.sendMessage(" " + RED + String.valueOf(nbfinished) + ". INVALID");
					}
					nbfinished++;
				}
			}
			else
			{
				Send(sender, RED + C("MsgPermissionDenied"));
			}
			return true;
		}

		private boolean done(Player player, String[] args)
		{
			if(PlotMe.cPerms(player, "PlotMe.use.done") || PlotMe.cPerms(player, "PlotMe.admin.done"))
			{
				if(!PlotManager.isPlotWorld(player))
				{
					Send(player, RED + C("MsgNotPlotWorld"));
					return true;
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
							
							String name = player.getName();
							
							if (plot.getOwner().getName().equalsIgnoreCase(name) || PlotMe.cPerms(player, "PlotMe.admin.done"))
							{							
								if(plot.isFinished())
								{
									plot.setUnfinished();
									Send(player, C("MsgUnmarkFinished"));
									
									if(isAdv)
										PlotMe.logger.info(LOG + name + " " + C("WordMarked") + " " + String.valueOf(plot.getId()) + " " + C("WordFinished"));
								}
								else
								{
									plot.setFinished();
									Send(player, C("MsgMarkFinished"));
									
									if(isAdv)
										PlotMe.logger.info(LOG + name + " " + C("WordMarked") + " " + String.valueOf(plot.getId()) + " " + C("WordUnfinished"));
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
		
		private boolean addtime(Player player, String[] args)
		{
			if(PlotMe.cPerms(player, "PlotMe.admin.addtime"))
			{
				if(!PlotManager.isPlotWorld(player))
				{
					Send(player, RED + C("MsgNotPlotWorld"));
					return true;
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
						if (!plot.isAvailable())
						{
							if(plot != null)
							{
								String name = player.getName();
								
								plot.resetExpiration(plot.getPlotWorld().DaysToExpiration);
								Send(player, C("MsgPlotExpirationReset"));
								
								if(isAdv)
									PlotMe.logger.info(LOG + name + " reset expiration on plot " + plot.getId());
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

		private boolean expired(CommandSender sender, String[] args)
		{
			if (((sender instanceof Player) && PlotMe.cPerms((Player)sender, "plotme.admin.expired")) || canExecuteAdminCommands(sender))
			{
				List<Integer> expiredPlots = PlotDatabase.getExpiredPlots();
				if (expiredPlots == null || expiredPlots.isEmpty())
				{
					Send(sender, GREEN + C("MsgNoPlotExpired"));
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
				sender.sendMessage(BLUE + commaSeparatedList.toString() + RESET);

			}
			else
			{
				Send(sender, RED + C("MsgPermissionDenied"));
			}
			return true;
		}

		private boolean plotlist(CommandSender sender, String[] args)
		{
			if (((sender instanceof Player) && PlotMe.cPerms((Player)sender, "plotme.use.list") || PlotMe.cPerms((Player)sender), "plotme.admin.list")) || canExecuteAdminCommands(sender))
			{
				if (!PlotManager.isPlotWorld(player))
				{
					Send(player, RED + C("MsgNotPlotWorld"));
					return true;
				}
				else
				{
					String name;
					
					if (PlotMe.cPerms(player, "plotme.admin.list") && args.length == 2)
					{
						name = args[1];
						Send(player, C("MsgListOfPlotsWhere") + " " + BLUE + name + RESET + " " + C("MsgCanBuild"));
					}
					else
					{
						name = player.getName();
						Send(player, C("MsgListOfPlotsWhereYou"));
					}
									
					for (Plot plot : PlotManager.getPlots(p).values())
					{
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
		}
		
		private boolean showhelp(CommandSender sender, int page)
		{

			
			return true;
		}
		
		private boolean tp(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "PlotMe.admin.tp"))
			{
				if(!PlotManager.isPlotWorld(player) && !PlotMe.allowWorldTeleport)
				{
					Send(player, RED + C("MsgNotPlotWorld"));
				}
				else
				{
					if(args.length == 2 || (args.length == 3 && PlotMe.allowWorldTeleport))
					{
						String id = args[1];
						
						if(!PlotManager.isValidId(id))
						{
							if(PlotMe.allowWorldTeleport)
								Send(player, C("WordUsage") + ": " + RED + "/plotme " + C("CommandTp") + " <" + C("WordId") + "> [" + C("WordWorld") + "] " + RESET + C("WordExample") + ": " + RED + "/plotme " + C("CommandTp") + " 5;-1 ");
							else
								Send(player, C("WordUsage") + ": " + RED + "/plotme " + C("CommandTp") + " <" + C("WordId") + "> " + RESET + C("WordExample") + ": " + RED + "/plotme " + C("CommandTp") + " 5;-1 ");
							return true;
						}
						else
						{
							World w;
							
							if(args.length == 3)
							{
								String world = args[2];
								
								w = Bukkit.getWorld(world);
							}
							else
							{
								if(!PlotManager.isPlotWorld(player))
								{
									w = PlotManager.getFirstWorld();
								}
								else
								{
									w = p.getWorld();
								}
							}
							
							if(w == null || !PlotManager.isPlotWorld(w))
							{
								Send(player, RED + C("MsgNoPlotworldFound"));
							}
							else
							{
								Location bottom = PlotManager.getPlotBottomLoc(w, id);
								Location top = PlotManager.getPlotTopLoc(w, id);
								
								p.teleport(new Location(w, bottom.getX() + (top.getBlockX() - bottom.getBlockX())/2, PlotManager.getMap(w).RoadHeight + 2, bottom.getZ() - 2));
							}
						}
					}
					else
					{
						if(PlotMe.allowWorldTeleport)
							Send(player, C("WordUsage") + ": " + RED + "/plotme " + C("CommandTp") + " <" + C("WordId") + "> [" + C("WordWorld") + "] " + RESET + C("WordExample") + ": " + RED + "/plotme " + C("CommandTp") + " 5;-1 ");
						else
							Send(player, C("WordUsage") + ": " + RED + "/plotme " + C("CommandTp") + " <" + C("WordId") + "> " + RESET + C("WordExample") + ": " + RED + "/plotme " + C("CommandTp") + " 5;-1 ");
					}
				}
			}
			else
			{
				Send(player, RED + C("MsgPermissionDenied"));
			}
			return true;
		}

		private boolean claim(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "PlotMe.use.claim") || PlotMe.cPerms(player, "PlotMe.admin.claim.other"))
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
						Send(player, RED + C("MsgCannotClaimRoad"));
					}
					else if(!PlotManager.isPlotAvailable(player))
					{
						Send(player, RED + C("MsgThisPlotOwned"));
					}
					else
					{
						String playername = player.getName();
						
						if(args.length == 2)
						{
							if(PlotMe.cPerms(player, "PlotMe.admin.claim.other"))
							{
								playername = args[1];
							}
						}
						
						int plotlimit = PlotMe.getPlotLimit(player);
						
						if(playername == player.getName() && plotlimit != -1 && PlotManager.getNbOwnedPlot(p) >= plotlimit)
						{
							Send(player, RED + C("MsgAlreadyReachedMaxPlots") + " (" + 
									PlotManager.getNbOwnedPlot(p) + "/" + PlotMe.getPlotLimit(player) + "). " + C("WordUse") + " " + RED + "/plotme " + C("CommandHome") + RESET + " " + C("MsgToGetToIt"));
						}
						else
						{
							PlotWorld pwi = PlotManager.getPlotWorld(player);
							PlotWorld pwi = PlotManager.getMap(w);
							
							double price = 0;
							
							if(PlotManager.isEconomyEnabled(w))
							{
								price = pwi.ClaimPrice;
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
									Send(player, RED + C("MsgNotEnoughBuy") + " " + C("WordMissing") + " " + RESET + (price - balance) + RED + " " + PlotMe.economy.currencyNamePlural());
									return true;
								}
							}
							
							Plot plot = PlotManager.createPlot(w, id, playername);
							
							//PlotManager.adjustLinkedPlots(id, w);
			
							if(plot == null)
								Send(player, RED + C("ErrCreatingPlotAt") + " " + plot.getId());
							else
							{
								if(playername.equalsIgnoreCase(player.getName()))
								{
									Send(player, C("MsgThisPlotYours") + " " + C("WordUse") + " " + RED + "/plotme " + C("CommandHome") + RESET + " " + C("MsgToGetToIt") + " " + f(-price));
								}else{
									Send(player, C("MsgThisPlotIsNow") + " " + playername + C("WordPossessive") + ". " + C("WordUse") + " " + RED + "/plotme " + C("CommandHome") + RESET + " " + C("MsgToGetToIt") + " " + f(-price));
								}
								
								if(isAdv)
									PlotMe.logger.info(LOG + playername + " " + C("MsgClaimedPlot") + " " + String.valueOf(plot.getId()) + ((price != 0) ? " " + C("WordFor") + " " + price : ""));
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
		}
		
		private boolean home(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "PlotMe.use.home") || PlotMe.cPerms(player, "PlotMe.admin.home.other"))
			{
				if(!PlotManager.isPlotWorld(player) && !PlotMe.allowWorldTeleport)
				{
					Send(player, RED + C("MsgNotPlotWorld"));
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
		}
		
		private boolean info(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "PlotMe.use.info"))
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
							
							player.sendMessage(GREEN + C("InfoId") + ": " + AQUA + String.valueOf(plot.getId()) + 
									GREEN + " " + C("InfoOwner") + ": " + AQUA + plot.getOwnerName() + 
									GREEN + " " + C("InfoBiome") + ": " + AQUA + FormatBiome(plot.biome.name()));
							
							player.sendMessage(GREEN + C("InfoExpire") + ": " + AQUA + ((plot.getExpiration() == null) ? C("WordNever") : plot.getExpiration().toString()) +
									GREEN + " " + C("InfoFinished") + ": " + AQUA + ((plot.isFinished()) ? C("WordYes") : C("WordNo")) +
									GREEN + " " + C("InfoProtected") + ": " + AQUA + ((plot.protect) ? C("WordYes") : C("WordNo")));
							
							if(plot.allowedcount() > 0)
							{
								player.sendMessage(GREEN + C("InfoHelpers") + ": " + AQUA + plot.getAllowed());
							}
							
							if(PlotMe.allowToDeny && plot.deniedcount() > 0)
							{
								player.sendMessage(GREEN + C("InfoDenied") + ": " + AQUA + plot.getDenied());
							}
							
							if(PlotManager.isEconomyEnabled(p))
							{
								if(plot.currentbidder.isEmpty())
								{
									player.sendMessage(GREEN + "Auctionned: " + AQUA + ((plot.isAuctioned()) ? C("WordYes") + 
											GREEN + " Minimum bid: " + AQUA + round(plot.currentbid) : C("WordNo")) +
											GREEN + " For sale: " + AQUA + ((plot.forsale) ? AQUA + round(plot.customprice) : C("WordNo")));
								}
								else
								{
									player.sendMessage(GREEN + C("InfoAuctionned") + ": " + AQUA + ((plot.isAuctioned()) ? C("WordYes") + 
											GREEN + " " + C("InfoBidder") + ": " + AQUA + plot.currentbidder + 
											GREEN + " " + C("InfoBid") + ": " + AQUA + round(plot.currentbid) : C("WordNo")) +
											GREEN + " " + C("InfoForSale") + ": " + AQUA + ((plot.forsale) ? AQUA + round(plot.customprice) : C("WordNo")));
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
		
		private boolean comment(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "plotme.use.comment"))
			{
				
				if(!PlotManager.isPlotWorld(player))
				{
					Send(player, RED + C("MsgNotPlotWorld"));
				}
				else
				{
					if(args.length < 2)
					{
						Send(player, C("WordUsage") + ": " + RED + "/plot " + C("CommandComment") + " <" + C("WordText") + ">");
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
								PlotWorld pwi = PlotManager.getPlotWorld(player);
								PlotWorld pwi = PlotManager.getMap(w);
								String playername = player.getName();
								
								double price = 0;
								
								if(PlotManager.isEconomyEnabled(w))
								{
									price = pwi.AddCommentPrice;
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
										Send(player, RED + C("MsgNotEnoughComment") + " " + C("WordMissing") + " " + RESET + f(price - balance, false));
										return true;
									}
								}
								
								Plot plot = PlotManager.getPlotById(p, id);
								
								String text = StringUtils.join(args," ");
								text = text.substring(text.indexOf(" "));
								
								String[] comment = new String[2];
								comment[0] = playername;
								comment[1] = text;
								
								plot.comments.add(comment);
								PlotMeSqlManager.addPlotComment(comment, plot.comments.size(), PlotManager.getIdX(id), PlotManager.getIdZ(id), plot.world);
								
								Send(player, C("MsgCommentAdded") + " " + f(-price));
								
								if(isAdv)
									PlotMe.logger.info(LOG + playername + " " + C("MsgCommentedPlot") + " " + String.valueOf(plot.getId()) + ((price != 0) ? " " + C("WordFor") + " " + price : ""));
							}
							else
							{
								Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgHasNoOwner"));
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
		}
		
		private boolean comments(CommandSender sender, String[] args)
		{
			if (PlotMe.cPerms(sender, "plotme.use.comments") || PlotMe.cPerms(sender, "plotme.view.allcomments") || canExecuteAdminCommands(sender))
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
							Send(player, RED + C("MsgNotPlotWorld"));
							return true;
						}
						plot = PlotManager.getPlotAtBlockPosition(player.getLocation());
						if (plot != null)
						{
							if (!(plot.getOwner().getName().equals(player.getName()) || plot.isAllowed(player.getName()) || PlotMe.cPerms(sender, "plotme.view.allcomments") || canExecuteAdminCommands(player)))
							{
								player.sendMessage(BLUE + PREFIX + RED + C("MsgPermissionDenied"));
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
					Send(sender, RED + C("MsgNoPlotFound"));
					return true;
				}
				
				if (ipage < 0)
				{
					ipage = 0;
				}

				LinkedList<Pair<String, String>> plotcomments = plot.getComments();
				
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
				}
			}
			else
			{
				player.sendMessage(BLUE + PREFIX + RED + C("MsgPermissionDenied"));
			}
			return true;
		}
		
		private boolean biome(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "PlotMe.use.biome"))
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
						player.sendMessage(BLUE + PREFIX + RED + C("MsgNoPlotFound"));
					}
					else
					{
						if(!PlotManager.isPlotAvailable(player))
						{
							PlotWorld pwi = PlotManager.getPlotWorld(player);
							
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
									Send(player, RED + args[1] + RESET + " " + C("MsgIsInvalidBiome"));
								}
								else
								{
									Plot plot = PlotManager.getPlotById(p,id);
									String playername = player.getName();
									
									if(plot.getOwner().getName().equalsIgnoreCase(playername) || PlotMe.cPerms(player, "PlotMe.admin"))
									{
										PlotWorld pwi = PlotManager.getMap(w);
										
										double price = 0;
										
										if(PlotManager.isEconomyEnabled(w))
										{
											price = pwi.BiomeChangePrice;
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
												Send(player, RED + C("MsgNotEnoughBiome") + " " + C("WordMissing") + " " + RESET + f(price - balance, false));
												return true;
											}
										}
										
										PlotManager.setBiome(w, id, plot, biome);
									
										Send(player, C("MsgBiomeSet") + " " + ChatColor.BLUE + FormatBiome(biome.name()) + " " + f(-price));
										
										if(isAdv)
										{
											PlotMe.logger.info(LOG + playername + " " + C("MsgChangedBiome") + " " + String.valueOf(plot.getId()) + " " + C("WordTo") + " " + 
													FormatBiome(biome.name()) + ((price != 0) ? " " + C("WordFor") + " " + price : ""));
										}
									}
									else
									{
										Send(player, RED + C("MsgThisPlot") + "(" + String.valueOf(plot.getId()) + ") " + C("MsgNotYoursNotAllowedBiome"));
									}
								}
							}
							else
							{
								Plot plot = PlotMe.plotmaps.get(w.getName().toLowerCase()).plots.get(id);
								
								Send(player, C("MsgPlotUsingBiome") + " " + ChatColor.BLUE + FormatBiome(plot.biome.name()));
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
		
		private boolean biomelist(CommandSender sender, String[] args)
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
					}*/
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
										
										if (denied.equals("*"))
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
																		
										Send(player, C("WordPlayer") + " " + RED + allowed + RESET + " " + C("WorldRemoved") + ". " + f(-price));
										
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
						if (isAdv)
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
		}
		
		private boolean id(Player player, String[] args)
		{
			if (PlotMe.cPerms(player, "plotme.admin.id") || canExecuteAdminCommands(player))
			{
				if (!PlotManager.isPlotWorld(player))
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
						player.sendMessage(BLUE + C("WordPlot") + " " + C("WordId") + ": " + RESET + String.valueOf(plot.getId()));
						
						Pair<Location, Location> locations = plot.getWorldMinMaxBlockLocations();
						
						player.sendMessage(BLUE + C("WordBottom") + ": " + RESET + locations.getLeft().getBlockX() + ChatColor.BLUE + "," + RESET + locations.getLeft().getBlockZ());
						player.sendMessage(BLUE + C("WordTop") + ": " + RESET + locations.getRight().getBlockX() + ChatColor.BLUE + "," + RESET + locations.getRight().getBlockZ());
					}
				}
			}
			else
			{
				Send(player, RED + C("MsgPermissionDenied"));
			}
			return true;
		}

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
