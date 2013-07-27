package com.worldcretornica.plotme.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.worldcretornica.plotme.PlotDatabase;
import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotMe;
import com.worldcretornica.plotme.PlotWorld;

public class CommandResetExpired extends PlotMeCommandBase
{

	@Override
	public boolean doExecute(CommandSender sender, String[] args)
	{
		if (PlotMe.cPerms(sender, "plotme.admin.resetexpired"))
		{
				PlotDatabase.removeExpiredPlots();
				Send(sender, ChatColor.GREEN + C("CommandReset"));
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
