package com.worldcretornica.plotme.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotMe;
import com.worldcretornica.plotme.PlotPlayer;
import com.worldcretornica.plotme.PlotWorld;

public class CommandHelp extends PlotMeCommandBase {
	
	static final int maxEntriesPerPage = 4;

	public CommandHelp(PlotMeCommands cmd) {
		super(cmd);
	}

	@Override
	public boolean run(CommandSender sender, String[] args)
	{
		int page = 1;
		int maxpage = 1;
		
		if (args.length >= 2)
		{
			try
			{
				page = Integer.parseInt(args[1]);
				if (page < 1)
					page = 1;
			}
			catch (NumberFormatException ex)
			{
				Send(sender, C("MsgInvalidPageNumber"));
				page = 1;
			}
		}

		List<String> allowed_commands = new ArrayList<String>();
		
		if (sender instanceof Player)
		{
			Player player = (Player)sender;

			boolean ecoon = PlotManager.isEconomyEnabled(player);

			allowed_commands.add("limit");
			if (PlotMe.cPerms(player, "plotme.use.claim")) allowed_commands.add("claim");
			if (PlotMe.cPerms(player, "PlotMe.use.claim.other")) allowed_commands.add("claim.other");
			if (PlotMe.cPerms(player, "PlotMe.use.auto")) allowed_commands.add("auto");
			if (PlotMe.cPerms(player, "PlotMe.use.home")) allowed_commands.add("home");
			if (PlotMe.cPerms(player, "PlotMe.use.home.other")) allowed_commands.add("home.other");
			if (PlotMe.cPerms(player, "PlotMe.use.info"))
			{
				allowed_commands.add("info");
				allowed_commands.add("biomeinfo");
			}
			if (PlotMe.cPerms(player, "PlotMe.use.comment")) allowed_commands.add("comment");
			if (PlotMe.cPerms(player, "PlotMe.use.comments")) allowed_commands.add("comments");
			if (PlotMe.cPerms(player, "PlotMe.use.list")) allowed_commands.add("list");
			if (PlotMe.cPerms(player, "PlotMe.use.biome"))
			{
				allowed_commands.add("biome");
				allowed_commands.add("biomelist");
			}
			if (PlotMe.cPerms(player, "PlotMe.use.done") || 
					PlotMe.cPerms(player, "PlotMe.admin.done")) allowed_commands.add("done");
			if (PlotMe.cPerms(player, "PlotMe.admin.done")) allowed_commands.add("donelist");
			if (PlotMe.cPerms(player, "PlotMe.admin.tp")) allowed_commands.add("tp");
			if (PlotMe.cPerms(player, "PlotMe.admin.id")) allowed_commands.add("id");
			if (PlotMe.cPerms(player, "PlotMe.use.clear") || 
					PlotMe.cPerms(player, "PlotMe.admin.clear")) allowed_commands.add("clear");
			if (PlotMe.cPerms(player, "PlotMe.admin.dispose") || 
					PlotMe.cPerms(player, "PlotMe.use.dispose")) allowed_commands.add("dispose");
			if (PlotMe.cPerms(player, "PlotMe.admin.reset")) allowed_commands.add("reset");
			if (PlotMe.cPerms(player, "PlotMe.use.add") || 
					PlotMe.cPerms(player, "PlotMe.admin.add")) allowed_commands.add("add");
			if (PlotMe.cPerms(player, "PlotMe.use.remove") || 
					PlotMe.cPerms(player, "PlotMe.admin.remove")) allowed_commands.add("remove");
			if (PlotMe.cPerms(player, "PlotMe.use.deny") || 
					PlotMe.cPerms(player, "PlotMe.admin.deny")) allowed_commands.add("deny");
			if (PlotMe.cPerms(player, "PlotMe.use.undeny") || 
					PlotMe.cPerms(player, "PlotMe.admin.undeny")) allowed_commands.add("undeny");
			if (PlotMe.cPerms(player, "PlotMe.admin.setowner")) allowed_commands.add("setowner");
			if (PlotMe.cPerms(player, "PlotMe.admin.move")) allowed_commands.add("move");
			if (PlotMe.cPerms(player, "PlotMe.admin.weanywhere")) allowed_commands.add("weanywhere");
			if (PlotMe.cPerms(player, "PlotMe.admin.reload")) allowed_commands.add("reload");
			if (PlotMe.cPerms(player, "PlotMe.admin.list")) allowed_commands.add("listother");
			if (PlotMe.cPerms(player, "PlotMe.admin.expired")) allowed_commands.add("expired");
			if (PlotMe.cPerms(player, "PlotMe.admin.addtime")) allowed_commands.add("addtime");
			if (PlotMe.cPerms(player, "PlotMe.admin.resetexpired")) allowed_commands.add("resetexpired");
			
			PlotWorld pwi = PlotManager.getPlotWorld(player);
			
			if (PlotManager.isPlotWorld(player) && ecoon)
			{
				if (PlotMe.cPerms(player, "PlotMe.use.buy")) allowed_commands.add("buy");
				if (PlotMe.cPerms(player, "PlotMe.use.sell")) 
				{
					allowed_commands.add("sell");
					if(pwi.CanSellToBank)
					{
						allowed_commands.add("sellbank");
					}
				}
				if (PlotMe.cPerms(player, "PlotMe.use.auction")) allowed_commands.add("auction");
				if (PlotMe.cPerms(player, "PlotMe.use.bid")) allowed_commands.add("bid");
			}
			
			maxpage = (int) Math.ceil((double) allowed_commands.size() / maxEntriesPerPage);
			if (page > maxpage)
				page = maxpage;
			int minEntry = (page - 1) * maxEntriesPerPage;
			int maxEntry = minEntry + maxEntriesPerPage;
			
			player.sendMessage(ChatColor.RED + " ---==" + ChatColor.BLUE + C("HelpTitle") + " " + page + "/" + maxpage + ChatColor.RED + "==--- ");
			
			for (int ctr = minEntry; ctr < maxEntry && ctr < allowed_commands.size(); ctr++)
			{
				String allowedcmd = allowed_commands.get(ctr);
				
				if (allowedcmd.equalsIgnoreCase("limit"))
				{
					if (PlotManager.isPlotWorld(player) || PlotMe.allowWorldTeleport)
					{
						PlotPlayer ppl = PlotManager.getPlotPlayer(player);

						/**
						 * TODO: rewrite plot limit calculations
						 */
						int maxplots = PlotMe.getPlotLimit(player);
						int ownedplots = ppl.getOwnPlotsCount();
						
						if (maxplots == -1)
							player.sendMessage(ChatColor.GREEN + C("HelpYourPlotLimitWorld") + " : " + ChatColor.AQUA + String.valueOf(ownedplots) + 
									ChatColor.GREEN + " " + C("HelpUsedOf") + " " + ChatColor.AQUA + C("WordInfinite"));
						else
							player.sendMessage(ChatColor.GREEN + C("HelpYourPlotLimitWorld") + " : " + ChatColor.AQUA + String.valueOf(ownedplots) + 
									ChatColor.GREEN + " " + C("HelpUsedOf") + " " + ChatColor.AQUA + String.valueOf(maxplots));
					}
					else
					{
						player.sendMessage(ChatColor.GREEN + C("HelpYourPlotLimitWorld") + " : " + ChatColor.AQUA + C("MsgNotPlotWorld"));
					}
				}
				else if(allowedcmd.equalsIgnoreCase("claim"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandClaim"));
					if(ecoon && pwi != null && pwi.ClaimPrice != 0)
						player.sendMessage(ChatColor.AQUA + " " + C("HelpClaim") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.ClaimPrice));
					else
						player.sendMessage(ChatColor.AQUA + " " + C("HelpClaim"));
				}
				else if(allowedcmd.equalsIgnoreCase("claim.other"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandClaim") + " <" + C("WordPlayer") + ">");
					if(ecoon && pwi != null && pwi.ClaimPrice != 0)
						player.sendMessage(ChatColor.AQUA + " " + C("HelpClaimOther") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.ClaimPrice));
					else
						player.sendMessage(ChatColor.AQUA + " " + C("HelpClaimOther"));
				}
				else if(allowedcmd.equalsIgnoreCase("auto"))
				{
					if(PlotMe.allowWorldTeleport)
						player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandAuto") + " [" + C("WordWorld") + "]");
					else
						player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandAuto"));
					
					if(ecoon && pwi != null && pwi.ClaimPrice != 0)
						player.sendMessage(ChatColor.AQUA + " " + C("HelpAuto") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.ClaimPrice));
					else
						player.sendMessage(ChatColor.AQUA + " " + C("HelpAuto"));
				}
				else if(allowedcmd.equalsIgnoreCase("home"))
				{
					if(PlotMe.allowWorldTeleport)
						player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandHome") + "[:#] [" + C("WordWorld") + "]");
					else
						player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandHome") + "[:#]");
					
					if(ecoon && pwi != null && pwi.PlotHomePrice != 0)
						player.sendMessage(ChatColor.AQUA + " " + C("HelpHome") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.PlotHomePrice));
					else
						player.sendMessage(ChatColor.AQUA + " " + C("HelpHome"));
				}
				else if(allowedcmd.equalsIgnoreCase("home.other"))
				{
					if(PlotMe.allowWorldTeleport)
						player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandHome") + "[:#] <" + C("WordPlayer") + "> [" + C("WordWorld") + "]");
					else
						player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandHome") + "[:#] <" + C("WordPlayer") + ">");
					
					if(ecoon && pwi != null && pwi.PlotHomePrice != 0)
						player.sendMessage(ChatColor.AQUA + " " + C("HelpHomeOther") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.PlotHomePrice));
					else
						player.sendMessage(ChatColor.AQUA + " " + C("HelpHomeOther"));
				}
				else if(allowedcmd.equalsIgnoreCase("info"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandInfo"));
					player.sendMessage(ChatColor.AQUA + " " + C("HelpInfo"));
				}
				else if(allowedcmd.equalsIgnoreCase("comment"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandComment") + " <" + C("WordComment") + ">");
					if(ecoon && pwi != null && pwi.AddCommentPrice != 0)
						player.sendMessage(ChatColor.AQUA + " " + C("HelpComment") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.AddCommentPrice));
					else
						player.sendMessage(ChatColor.AQUA + " " + C("HelpComment"));
				}
				else if(allowedcmd.equalsIgnoreCase("comments"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandComments"));
					player.sendMessage(ChatColor.AQUA + " " + C("HelpComments"));
				}
				else if(allowedcmd.equalsIgnoreCase("list"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandList"));
					player.sendMessage(ChatColor.AQUA + " " + C("HelpList"));
				}
				else if(allowedcmd.equalsIgnoreCase("listother"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandList") + " <" + C("WordPlayer") + ">");
					player.sendMessage(ChatColor.AQUA + " " + C("HelpListOther"));
				}
				else if(allowedcmd.equalsIgnoreCase("biomeinfo"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandBiome"));
					player.sendMessage(ChatColor.AQUA + " " + C("HelpBiomeInfo"));
				}
				else if(allowedcmd.equalsIgnoreCase("biome"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandBiome") + " <" + C("WordBiome") + ">");
					if(ecoon && pwi != null && pwi.BiomeChangePrice != 0)
						player.sendMessage(ChatColor.AQUA + " " + C("HelpBiome") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.BiomeChangePrice));
					else
						player.sendMessage(ChatColor.AQUA + " " + C("HelpBiome"));
				}
				else if(allowedcmd.equalsIgnoreCase("biomelist"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandBiomelist"));
					player.sendMessage(ChatColor.AQUA + " " + C("HelpBiomeList"));
				}
				else if(allowedcmd.equalsIgnoreCase("done"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandDone"));
					player.sendMessage(ChatColor.AQUA + " " + C("HelpDone"));
				}
				else if(allowedcmd.equalsIgnoreCase("tp"))
				{
					if(PlotMe.allowWorldTeleport)
						player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandTp") + " <" + C("WordId") + "> [" + C("WordWorld") + "]");
					else
						player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandTp") + " <" + C("WordId") + ">");
					
					player.sendMessage(ChatColor.AQUA + " " + C("HelpTp"));
				}
				else if(allowedcmd.equalsIgnoreCase("id"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandId"));
					player.sendMessage(ChatColor.AQUA + " " + C("HelpId"));
				}
				else if(allowedcmd.equalsIgnoreCase("clear"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandClear"));
					if(ecoon && pwi != null && pwi.ClearPrice != 0)
						player.sendMessage(ChatColor.AQUA + " " + C("HelpId") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.ClearPrice));
					else
						player.sendMessage(ChatColor.AQUA + " " + C("HelpClear"));
				}
				else if(allowedcmd.equalsIgnoreCase("reset"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandReset"));
					player.sendMessage(ChatColor.AQUA + " " + C("HelpReset"));
				}
				else if(allowedcmd.equalsIgnoreCase("add"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandAdd") + " <" + C("WordPlayer") + ">");
					if(ecoon && pwi != null && pwi.AddPlayerPrice != 0)
						player.sendMessage(ChatColor.AQUA + " " + C("HelpAdd") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.AddPlayerPrice));
					else
						player.sendMessage(ChatColor.AQUA + " " + C("HelpAdd"));
				}
				else if(allowedcmd.equalsIgnoreCase("deny"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandDeny") + " <" + C("WordPlayer") + ">");
					if(ecoon && pwi != null && pwi.DenyPlayerPrice != 0)
						player.sendMessage(ChatColor.AQUA + " " + C("HelpDeny") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.DenyPlayerPrice));
					else
						player.sendMessage(ChatColor.AQUA + " " + C("HelpDeny"));
				}
				else if(allowedcmd.equalsIgnoreCase("remove")){
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandRemove") + " <" + C("WordPlayer") + ">");
					if(ecoon && pwi != null && pwi.RemovePlayerPrice != 0)
						player.sendMessage(ChatColor.AQUA + " " + C("HelpRemove") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.RemovePlayerPrice));
					else
						player.sendMessage(ChatColor.AQUA + " " + C("HelpRemove"));
				}
				else if(allowedcmd.equalsIgnoreCase("undeny")){
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandUndeny") + " <" + C("WordPlayer") + ">");
					if(ecoon && pwi != null && pwi.UndenyPlayerPrice != 0)
						player.sendMessage(ChatColor.AQUA + " " + C("HelpUndeny") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.UndenyPlayerPrice));
					else
						player.sendMessage(ChatColor.AQUA + " " + C("HelpUndeny"));
				}
				else if(allowedcmd.equalsIgnoreCase("setowner"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandSetowner") + " <" + C("WordPlayer") + ">");
					player.sendMessage(ChatColor.AQUA + " " + C("HelpSetowner"));
				}
				else if(allowedcmd.equalsIgnoreCase("move"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandMove") + " <" + C("WordIdFrom") + "> <" + C("WordIdTo") + ">");
					player.sendMessage(ChatColor.AQUA + " " + C("HelpMove"));
				}
				else if(allowedcmd.equalsIgnoreCase("weanywhere"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandWEAnywhere"));
					player.sendMessage(ChatColor.AQUA + " " + C("HelpWEAnywhere"));
				}
				else if(allowedcmd.equalsIgnoreCase("expired"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandExpired") + " [page]");
					player.sendMessage(ChatColor.AQUA + " " + C("HelpExpired"));
				}
				else if(allowedcmd.equalsIgnoreCase("donelist"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandDoneList") + " [page]");
					player.sendMessage(ChatColor.AQUA + " " + C("HelpDoneList"));
				}
				else if(allowedcmd.equalsIgnoreCase("addtime"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandAddtime"));
					int days = (pwi == null) ? 0 : pwi.DaysToExpiration;
					if(days == 0)
						player.sendMessage(ChatColor.AQUA + " " + C("HelpAddTime1") + " " + ChatColor.RESET + C("WordNever"));
					else
						player.sendMessage(ChatColor.AQUA + " " + C("HelpAddTime1") + " " + ChatColor.RESET + days + ChatColor.AQUA + " " + C("HelpAddTime2"));
				}
				else if(allowedcmd.equalsIgnoreCase("reload"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme reload");
					player.sendMessage(ChatColor.AQUA + " " + C("HelpReload"));
				}
				else if(allowedcmd.equalsIgnoreCase("dispose"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandDispose"));
					if(ecoon && pwi != null && pwi.DisposePrice != 0)
						player.sendMessage(ChatColor.AQUA + " " + C("HelpDispose") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.DisposePrice));
					else
						player.sendMessage(ChatColor.AQUA + " " + C("HelpDispose"));
				}
				else if(allowedcmd.equalsIgnoreCase("buy"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandBuy"));
					player.sendMessage(ChatColor.AQUA + " " + C("HelpBuy"));
				}
				else if(allowedcmd.equalsIgnoreCase("sell"))
				{				
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandSell") + " [" + C("WordAmount") + "]");
					player.sendMessage(ChatColor.AQUA + " " + C("HelpSell") + " " + C("WordDefault") + " : " + ChatColor.RESET + f(pwi.SellToPlayerPrice));
				}
				else if(allowedcmd.equalsIgnoreCase("sellbank"))
				{				
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandSellBank"));
					player.sendMessage(ChatColor.AQUA + " " + C("HelpSellBank") + " " + ChatColor.RESET + f(pwi.SellToBankPrice));
				}
				else if(allowedcmd.equalsIgnoreCase("auction"))
				{				
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandAuction") + " [" + C("WordAmount") + "]");
					player.sendMessage(ChatColor.AQUA + " " + C("HelpAuction") + " " + C("WordDefault") + " : " + ChatColor.RESET + "1");
				}
				else if(allowedcmd.equalsIgnoreCase("resetexpired"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandResetExpired") + " <" + C("WordWorld") + ">");
					player.sendMessage(ChatColor.AQUA + " " + C("HelpResetExpired"));
				}
				else if(allowedcmd.equalsIgnoreCase("bid"))
				{
					player.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandBid") + " <" + C("WordAmount") + ">");
					player.sendMessage(ChatColor.AQUA + " " + C("HelpBid"));
				}
			}
		}
		return true;
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return null;
	}

}
