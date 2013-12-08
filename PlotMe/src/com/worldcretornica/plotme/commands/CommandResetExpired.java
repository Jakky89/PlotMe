package com.worldcretornica.plotme.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotMe;
import com.worldcretornica.plotme.PlotWorld;

public class CommandResetExpired extends PlotMeCommandBase {

	
	public CommandResetExpired(PlotMeCommands cmd)
	{
		super(cmd);
	}

	@Override
	public boolean run(CommandSender sender, String[] args) {
		if (PlotMe.cPerms(sender, "plotme.admin.resetexpired"))
		{
			World world = null;
			if (args.length < 2)
			{
				if (sender instanceof Player)
				{
					world = ((Player)sender).getWorld();
				}
				else
				{
					return true;
				}
			}
			else if (args.length == 1)
			{
				world = Bukkit.getWorld(args[1]);
			}
			else
			{
				return true;
			}
			if (world != null)
			{
				PlotWorld pwi = PlotManager.getPlotWorld(world);
				if (pwi != null)
				{
					PlotManager.checkPlotExpirationsManually(sender, pwi);
					Send(sender, ChatColor.GREEN + C("WordWorld") + " '" + world.getName() + "' " + C("CommandReset"));
				}
				else
				{
					Send(sender, ChatColor.RED + C("WordWorld") + " '" + world.getName() + "' " + C("MsgNotPlotWorld"));
				}
			}
			else
			{
				Send(sender, ChatColor.RED + C("WordWorld") + " '" + args[1] + "' " + C("MsgDoesNotExistOrNotLoaded"));
			}
		}
		else
		{
			Send(sender, ChatColor.RED + C("MsgPermissionDenied"));
		}
		return false;
	}

	@Override
	public String getUsage() {
		return "/plot " + C("CommandResetExpired") + " <" + C("WordWorld") + "> " + ChatColor.RESET + "Example: " + ChatColor.RED + "/plotme " + C("CommandResetExpired") + " plotworld ";
	}

}
