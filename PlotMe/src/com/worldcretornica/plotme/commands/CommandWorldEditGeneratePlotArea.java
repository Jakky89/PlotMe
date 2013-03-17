package com.worldcretornica.plotme.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class CommandWorldEditGeneratePlotArea extends PlotMeCommandBase {
	
	WorldEditPlugin worldEdit;
	
	
	public CommandWorldEditGeneratePlotArea(PlotMeCommands cmd) {
		super(cmd);
		worldEdit = (WorldEditPlugin)cmd.getPlugin().getServer().getPluginManager().getPlugin("WorldEdit");
	}

	@Override
	public boolean run(CommandSender sender, String[] args) {
		if (sender instanceof Player)
		{
			Player player = (Player)sender;
			Selection selection = worldEdit.getSelection(player);
			
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
