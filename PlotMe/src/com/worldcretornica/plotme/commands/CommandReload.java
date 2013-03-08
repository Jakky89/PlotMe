package com.worldcretornica.plotme.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.worldcretornica.plotme.PlotMe;

public class CommandReload extends PlotMeCommandBase {

	public CommandReload(PlotMeCommands cmd) {
		super(cmd);
	}

	@Override
	public boolean run(CommandSender sender, String[] args) {
		if (PlotMe.cPerms(sender, "plotme.admin.reload"))
		{
			Send(sender, ChatColor.GREEN + C("MsgReloadedSuccess"));
			if (PlotMe.advancedlogging)
			{
				PlotMe.logger.info(PlotMe.PREFIX + sender.getName() + " " + C("MsgReloadedConfigurations"));
			}
		}
		else
		{
			Send(sender, ChatColor.RED + C("MsgPermissionDenied"));
		}
		return true;
	}

	@Override
	public String getUsage() {
		return "/plotme reload";
	}

}
