package com.worldcretornica.plotme.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.worldcretornica.plotme.PlotMe;


public abstract class PlotMeCommandBase
{
	
	/**
	 * ABSTRACT METHODS
	 */
	public abstract boolean doExecute(CommandSender sender, String[] args); // should return false when e.g. command arguments were missing
	public abstract String getUsage();
	
	
	public static String C(String text)
	{
		return PlotMeCommands.C(text);
	}
	
	public static void Send(CommandSender sender, String text)
	{
		PlotMeCommands.Send(sender, text);
	}
	
	public static String FormatBiome(String biome)
	{
		return PlotMeCommands.FormatBiome(biome);
	}

	public static String f(double price)
	{
		return PlotMeCommands.f(price);
	}
	
	public static String f(double price, boolean showsign)
	{
		return PlotMeCommands.f(price, showsign);
	}
	
}
