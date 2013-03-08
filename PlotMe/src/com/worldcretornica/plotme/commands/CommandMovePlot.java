package com.worldcretornica.plotme.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotMe;
import com.worldcretornica.plotme.PlotWorld;

public class CommandMovePlot extends PlotMeCommandBase {
	
	public static Map<String, Plot> plotmovings;
	
	public CommandMovePlot(PlotMeCommands cmd)
	{
		super(cmd);
		plotmovings = new HashMap<String, Plot>();
	}

	@Override
	public boolean run(CommandSender sender, String[] args)
	{
		if (PlotMe.cPerms(sender, "plotme.admin.move"))
		{
			if (args.length == 2)
			{
				if (sender instanceof Player)
				{
					Player player = (Player)sender;
					if (PlotManager.isPlotWorld(player))
					{
						String sub = args[1].toLowerCase();
						if (sub.equals("from"))
						{
							PlotWorld pwi = PlotManager.getPlotWorld(player);
							if (pwi != null)
							{
								Plot pli = pwi.getPlotAtBlockPosition(player.getLocation());
								if (pli != null)
								{
									Plot plotto = plotmovings.get(player.getName());
									if (plotto == null)
									{
										plotmovings.put(player.getName(), pli);
										Send(player, C("MsgMovingSourcePlotMarked"));
									}
									else
									{
										if (PlotManager.movePlots(plotto, pli))
										{
											plotmovings.remove(player.getName());
											Send(player, C("MsgPlotMovedSuccess"));
											if (PlotMe.advancedlogging)
											{
												PlotMe.logger.info("[" + PlotMe.NAME + "] " + player.getName() + " " + C("MsgExchangedPlot") + " " + String.valueOf(plotto.getId()) + " " + C("MsgAndPlot") + " " + String.valueOf(pli.getId()));
											}
											return true;
										}
										else
										{
											Send(player, ChatColor.RED + C("ErrMovingPlot"));
										}
									}
									return true;
								}
							}
							Send(player, ChatColor.RED + C("MsgNoPlotFound"));
							return true;
						}
						else if (sub.equals("to"))
						{
							PlotWorld pwi = PlotManager.getPlotWorld(player);
							if (pwi != null)
							{
								Plot pli = pwi.getPlotAtBlockPosition(player.getLocation());
								if (pli != null)
								{
									Plot plotfrom = plotmovings.get(player.getName());
									if (plotfrom == null)
									{
										plotmovings.put(player.getName(), pli);
										Send(player, C("MsgMovingDestinationPlotMarked"));
									}
									else
									{
										if (PlotManager.movePlots(plotfrom, pli))
										{
											plotmovings.remove(player.getName());
											Send(player, C("MsgPlotMovedSuccess"));
											if (PlotMe.advancedlogging)
											{
												PlotMe.logger.info(PlotMe.PREFIX + player.getName() + " " + C("MsgExchangedPlot") + " " + String.valueOf(plotfrom.getId()) + " " + C("MsgAndPlot") + " " + String.valueOf(pli.getId()));
											}
											return true;
										}
										else
										{
											Send(player, ChatColor.RED + C("ErrMovingPlot"));
											
										}
									}
									return true;
								}
							}
							Send(player, ChatColor.RED + C("MsgNoPlotFound"));
							return true;
						}
						else
						{
							try
							{
								Integer toplotid = Integer.parseInt(args[1]);
								if (toplotid != null && toplotid > 0)
								{
									Plot plotto = PlotManager.getPlot(toplotid);
									if (plotto != null)
									{
										PlotWorld pwi = PlotManager.getPlotWorld(player);
										if (pwi != null)
										{
											Plot pli = pwi.getPlotAtBlockPosition(player.getLocation());
											if (pli != null)
											{
												if (PlotManager.movePlots(pli, plotto))
												{
													Send(sender, C("MsgPlotMovedSuccess"));
													if (PlotMe.advancedlogging)
													{
														PlotMe.logger.info(PlotMe.PREFIX + player.getName() + " " + C("MsgExchangedPlot") + " " + String.valueOf(pli.getId()) + " " + C("MsgAndPlot") + " " + String.valueOf(plotto.getId()));
													}
													return true;
												}
											}
										}
									}
									Send(sender, ChatColor.RED + C("MsgNoPlotFound"));
								}
								else
								{
									Send(sender, ChatColor.RED + C("MsgInvalidNumber"));
								}
								return true;
							}
							catch (NumberFormatException ex)
							{
								Send(sender, ChatColor.RED + C("MsgInvalidNumber"));
								return true;
							}
						}
					}
					else
					{
						Send(sender, ChatColor.RED + C("MsgNotPlotWorld"));
					}
					return true;
				}
				Send(sender, ChatColor.RED + C("ErrMovingPlot"));
				return true;
			}
			else if (args.length == 3)
			{
				try
				{
					Integer fromplotid = Integer.parseInt(args[1]);
					Integer toplotid = Integer.parseInt(args[2]);
					if (fromplotid != null && fromplotid > 0 && toplotid != null && toplotid > 0)
					{
						Plot plotfrom = PlotManager.getPlot(fromplotid);
						Plot plotto = PlotManager.getPlot(toplotid);
						if (plotfrom != null && plotto != null)
						{
							if (PlotManager.movePlots(plotfrom, plotto))
							{
								Send(sender, C("MsgPlotMovedSuccess"));
								if (PlotMe.advancedlogging)
								{
									PlotMe.logger.info(PlotMe.PREFIX + sender.getName() + " " + C("MsgExchangedPlot") + " " + String.valueOf(plotfrom.getId()) + " " + C("MsgAndPlot") + " " + String.valueOf(plotto.getId()));
								}
								return true;
							}
							Send(sender, ChatColor.RED + C("ErrMovingPlot"));
						}
						Send(sender, ChatColor.RED + C("MsgNoPlotFound"));
					}
					else
					{
						Send(sender, ChatColor.RED + C("MsgInvalidNumber"));
					}
					return true;
				}
				catch (NumberFormatException ex)
				{
					Send(sender, ChatColor.RED + C("MsgInvalidNumber"));
					return true;
				}
			}
		}
		else
		{
			Send(sender, ChatColor.RED + C("MsgPermissionDenied"));
			return true;
		}
		return false;
	}

	@Override
	public String getUsage() {
		return  C("WordUsage") + ": " + ChatColor.RED + "/plot " + C("CommandMove") + " " + C("CommandMoveFrom") +
				"  /plot " + C("CommandMove") + " " + C("CommandMoveTo") +
                "  /plot " + C("CommandMove") + " [" + C("WordIdFrom") + "] <" + C("WordIdTo") + ">";
	}
}
