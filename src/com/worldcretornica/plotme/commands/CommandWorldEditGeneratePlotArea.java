package com.worldcretornica.plotme.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.worldcretornica.plotme.PlotMe;

public class CommandWorldEditGeneratePlotArea extends PlotMeCommandBase {
	
	WorldEditPlugin worldEdit;

	
	@Override
	public boolean doExecute(CommandSender sender, String[] args) {
		if (sender instanceof Player)
		{
			Player player = (Player)sender;
			Selection selection = PlotMe.worldedit.getSelection(player);
			
			if (selection != null)
			{
				World world = selection.getWorld();
				Location min = selection.getMinimumPoint();
				Location max = selection.getMaximumPoint();
				/**
				 * TODO
				 */
			}
			else
			{
				/**
				 * TODO
				 */
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
		return null;
	}

}
