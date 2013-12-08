package com.worldcretornica.plotme.commands;

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

public class CommandAuto extends PlotMeCommandBase {

	public CommandAuto(PlotMeCommands cmd) {
		super(cmd);
	}

	@Override
	public boolean run(CommandSender sender, String[] args) {
		if (sender instanceof Player)
		{
			Player player = (Player)sender;
			if (PlotMe.cPerms(player, "plotme.use.auto"))
			{
				if (!PlotManager.isPlotWorld(player))
				{
					Send(player, ChatColor.RED + C("MsgNotPlotWorld"));
				}
				else
				{
					World w;
					
					if (args.length == 2)
					{
						w = Bukkit.getWorld(args[1]);
					}
					
					if (w == null || !PlotManager.isPlotWorld(w))
					{
						Send(player, ChatColor.RED + args[1] + " " + C("MsgWorldNotPlot"));
						return true;
					}
					else
					{
						w = player.getWorld();
					}
					if (w == null)
					{
						Send(player, ChatColor.RED + C("MsgNoPlotworldFound"));
					}
					else
					{
						PlotWorld pwi = PlotManager.getPlotWorld(w);
						PlotPlayer ppl = PlotManager.getPlotPlayer(player);
						int plc = ppl.getPlotCount(pwi);
						if (plc >= PlotMe.getPlotLimit(player) && !PlotMe.cPerms(player, "plotme.admin"))
						{
							Send(player, ChatColor.RED + C("MsgAlreadyReachedMaxPlots") + " (" + 
								 String.valueOf(plc) + "/" + PlotMe.getPlotLimit(player) + "). " + C("WordUse") + " " + ChatColor.RED + "/plot " + C("CommandHome") + ChatColor.RESET + " " + C("MsgToGetToIt"));
						}
						else
						{
							int limit = pwi.PlotAutoLimit;

							
							for (int i = 0; i < limit; i++)
							{
								for (int x = -i; x <= i; x++)
								{
									for (int z = -i; z <= i; z++)
									{
										String id = "" + x + ";" + z;
										
										if (PlotManager.isPlotAvailable(id, w))
										{									
											String name = player.getName();
											
											double price = 0;
											
											if(PlotManager.isEconomyEnabled(w))
											{
												price = pwi.ClaimPrice;
												double balance = PlotMe.economy.getBalance(name);
												
												if(balance >= price)
												{
													EconomyResponse er = PlotMe.economy.withdrawPlayer(name, price);
													
													if(!er.transactionSuccess())
													{
														Send(player, RED + er.errorMessage);
														warn(er.errorMessage);
														return true;
													}
												}
												else
												{
													Send(player, RED + C("MsgNotEnoughAuto") + " " + C("WordMissing") + " " + RESET + f(price - balance, false));
													return true;
												}
											}
											
											Plot plot = PlotManager.createPlot(w, id, name);
											
											//PlotManager.adjustLinkedPlots(id, w);
											
											p.teleport(new Location(w, PlotManager.bottomX(plot.getId(), w) + (PlotManager.topX(plot.getId(), w) - 
													PlotManager.bottomX(plot.getId(), w))/2, pwi.RoadHeight + 2, PlotManager.bottomZ(plot.getId(), w) - 2));
				
											Send(player, C("MsgThisPlotYours") + " " + C("WordUse") + " " + RED + "/plotme " + C("CommandHome") + RESET + " " + C("MsgToGetToIt") + " " + f(-price));
											
											if(isAdv)
												PlotMe.logger.info(LOG + name + " " + C("MsgClaimedPlot") + " " + String.valueOf(plot.getId()) + ((price != 0) ? " " + C("WordFor") + " " + price : ""));
											
											return true;
										}
									}
								}
							}
						
							Send(player, RED + C("MsgNoPlotFound1") + " " + (limit^2) + " " + C("MsgNoPlotFound2"));
						}
					}
				}
			}
			else
			{
				Send(sender, RED + C("MsgPermissionDenied"));
			}
		}
		else
		{
			Send(sender, RED + C("MsgPermissionDenied"));
		}
		return true;
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return null;
	}

}
