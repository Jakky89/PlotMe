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
		
		if (PlotMe.cPerms(sender, "plotme.admin.reload"))
			allowed_commands.add("reload");
		int maxplots = 0;
		int ownedplots = 0;
		boolean isInPlotWorld = false;
		PlotWorld pwi = null;
		if (sender instanceof Player)
		{
			Player player = (Player)sender;
			PlotPlayer plotPlayer = PlotManager.getPlotPlayer(player);
			isInPlotWorld = PlotManager.isPlotWorld(player);
			maxplots = PlotMe.getPlotLimit(player);
			if (plotPlayer != null)
			{
				ownedplots = plotPlayer.getOwnPlotsCount();
			}
			if (PlotMe.cPerms(sender, "plotme.use.limit"))
				allowed_commands.add("limit");
			if (PlotMe.cPerms(sender, "plotme.use.claim"))
				allowed_commands.add("claim");
			if (PlotMe.cPerms(sender, "plotme.use.claim.other"))
				allowed_commands.add("claim.other");
			if (PlotMe.cPerms(sender, "plotme.use.auto"))
				allowed_commands.add("auto");
			if (PlotMe.cPerms(sender, "plotme.use.home"))
				allowed_commands.add("home");
			if (PlotMe.cPerms(sender, "plotme.use.home.other"))
				allowed_commands.add("home.other");
			if (PlotMe.cPerms(sender, "plotme.use.info"))
			{
				allowed_commands.add("info");
				allowed_commands.add("biomeinfo");
			}
			if (PlotMe.cPerms(sender, "plotme.use.comment"))
				allowed_commands.add("comment");
			if (PlotMe.cPerms(sender, "plotme.use.comments"))
				allowed_commands.add("comments");
			if (PlotMe.cPerms(sender, "plotme.use.list"))
				allowed_commands.add("list");
			if (PlotMe.cPerms(sender, "plotme.use.biome"))
			{
				allowed_commands.add("biome");
				allowed_commands.add("biomelist");
			}
			if (PlotMe.cPerms(sender, "plotme.use.done") || PlotMe.cPerms(sender, "plotme.admin.done"))
				allowed_commands.add("done");
			if (PlotMe.cPerms(sender, "plotme.admin.done"))
				allowed_commands.add("donelist");
			if (PlotMe.cPerms(sender, "plotme.admin.tp") || (isInPlotWorld && PlotMe.allowWorldTeleport))
				allowed_commands.add("tp");
			if (PlotMe.cPerms(sender, "plotme.admin.id"))
				allowed_commands.add("id");
			if (PlotMe.cPerms(sender, "plotme.use.clear") || PlotMe.cPerms(sender, "plotme.admin.clear"))
				allowed_commands.add("clear");
			if (PlotMe.cPerms(sender, "plotme.use.dispose") || PlotMe.cPerms(sender, "plotme.admin.dispose"))
				allowed_commands.add("dispose");
			if (PlotMe.cPerms(sender, "plotme.use.reset") || PlotMe.cPerms(sender, "plotme.admin.reset"))
				allowed_commands.add("reset");
			if (PlotMe.cPerms(sender, "plotme.use.add") || PlotMe.cPerms(sender, "plotme.admin.add"))
				allowed_commands.add("add");
			if (PlotMe.cPerms(sender, "plotme.use.remove") || PlotMe.cPerms(sender, "plotme.admin.remove"))
				allowed_commands.add("remove");
			if (PlotMe.cPerms(sender, "plotme.use.deny") || PlotMe.cPerms(sender, "plotme.admin.deny"))
				allowed_commands.add("deny");
			if (PlotMe.cPerms(sender, "plotme.use.undeny") || PlotMe.cPerms(sender, "plotme.admin.undeny"))
				allowed_commands.add("undeny");
			if (PlotMe.cPerms(sender, "plotme.use.setowner") || PlotMe.cPerms(sender, "plotme.admin.setowner"))
				allowed_commands.add("setowner");
			if (PlotMe.cPerms(sender, "plotme.user.move") || PlotMe.cPerms(sender, "plotme.admin.move"))
				allowed_commands.add("move");
			if (PlotMe.cPerms(sender, "plotme.admin.weanywhere"))
				allowed_commands.add("weanywhere");
			if (PlotMe.cPerms(sender, "plotme.admin.list"))
				allowed_commands.add("listother");
			if (PlotMe.cPerms(sender, "plotme.admin.expired"))
				allowed_commands.add("expired");
			if (PlotMe.cPerms(sender, "plotme.admin.addtime"))
				allowed_commands.add("addtime");
			if (PlotMe.cPerms(sender, "plotme.admin.resetexpired"))
				allowed_commands.add("resetexpired");
			if (isInPlotWorld && pwi.UseEconomy)
			{
				if (PlotMe.cPerms(sender, "plotme.use.buy"))
					allowed_commands.add("buy");
				if (PlotMe.cPerms(sender, "plotme.use.sell")) 
				{
					allowed_commands.add("sell");
					if (pwi.CanSellToBank)
					{
						allowed_commands.add("sellbank");
					}
				}
				if (PlotMe.cPerms(sender, "plotme.use.auction") || PlotMe.cPerms(sender, "plotme.admin.auction"))
					allowed_commands.add("auction");
				if (PlotMe.cPerms(sender, "plotme.use.bid"))
					allowed_commands.add("bid");
			}
		}
		
		maxpage = (int) Math.ceil((double) allowed_commands.size() / maxEntriesPerPage);
		if (page > maxpage)
			page = maxpage;
		int minEntry = (page - 1) * maxEntriesPerPage;
		int maxEntry = Math.min(minEntry + maxEntriesPerPage, allowed_commands.size());
		
		sender.sendMessage(" " + ChatColor.RED + "---" + ChatColor.GOLD + "== " + ChatColor.BLUE + C("HelpTitle") + " " + page + "/" + maxpage + ChatColor.GOLD + " ==" + ChatColor.RED + "---");
		
		for (int ctr = minEntry; ctr < maxEntry; ctr++)
		{
			String allowedcmd = allowed_commands.get(ctr).toLowerCase();
			if (allowedcmd.equals("limit"))
			{
				if (maxplots == -1)
				{
					sender.sendMessage(ChatColor.GREEN + C("HelpYourPlotLimitWorld") + " : " + ChatColor.AQUA + String.valueOf(ownedplots) + 
							ChatColor.GREEN + " " + C("HelpUsedOf") + " " + ChatColor.AQUA + C("WordInfinite"));
				}
				else
				{
					sender.sendMessage(ChatColor.GREEN + C("HelpYourPlotLimitWorld") + " : " + ChatColor.AQUA + String.valueOf(ownedplots) + 
							ChatColor.GREEN + " " + C("HelpUsedOf") + " " + ChatColor.AQUA + String.valueOf(maxplots));
				}
			}
			else if (allowedcmd.equals("claim"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandClaim"));
				if(PlotManager.isEconomyEnabled(pwi) && pwi != null && pwi.ClaimPrice != 0)
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpClaim") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.ClaimPrice));
				else
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpClaim"));
			}
			else if (allowedcmd.equals("claim.other"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandClaim") + " <" + C("WordPlayer") + ">");
				if(PlotManager.isEconomyEnabled(pwi) && pwi != null && pwi.ClaimPrice != 0)
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpClaimOther") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.ClaimPrice));
				else
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpClaimOther"));
			}
			else if (allowedcmd.equals("auto"))
			{
				if (PlotMe.allowWorldTeleport)
					sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandAuto") + " [" + C("WordWorld") + "]");
				else
					sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandAuto"));
				
				if(PlotManager.isEconomyEnabled(pwi) && pwi != null && pwi.ClaimPrice != 0)
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpAuto") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.ClaimPrice));
				else
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpAuto"));
			}
			else if (allowedcmd.equals("home"))
			{
				if(PlotMe.allowWorldTeleport)
					sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandHome") + "[:#] [" + C("WordWorld") + "]");
				else
					sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandHome") + "[:#]");
				
				if(PlotManager.isEconomyEnabled(pwi) && pwi != null && pwi.PlotHomePrice != 0)
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpHome") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.PlotHomePrice));
				else
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpHome"));
			}
			else if (allowedcmd.equals("home.other"))
			{
				if (PlotMe.allowWorldTeleport)
					sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandHome") + "[:#] <" + C("WordPlayer") + "> [" + C("WordWorld") + "]");
				else
					sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandHome") + "[:#] <" + C("WordPlayer") + ">");
				
				if(PlotManager.isEconomyEnabled(pwi) && pwi != null && pwi.PlotHomePrice != 0)
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpHomeOther") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.PlotHomePrice));
				else
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpHomeOther"));
			}
			else if (allowedcmd.equals("info"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandInfo"));
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpInfo"));
			}
			else if (allowedcmd.equals("comment"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandComment") + " <" + C("WordComment") + ">");
				if(PlotManager.isEconomyEnabled(pwi) && pwi != null && pwi.AddCommentPrice != 0)
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpComment") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.AddCommentPrice));
				else
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpComment"));
			}
			else if (allowedcmd.equals("comments"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandComments"));
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpComments"));
			}
			else if (allowedcmd.equals("list"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandList"));
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpList"));
			}
			else if (allowedcmd.equals("listother"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandList") + " <" + C("WordPlayer") + ">");
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpListOther"));
			}
			else if (allowedcmd.equals("biomeinfo"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandBiome"));
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpBiomeInfo"));
			}
			else if (allowedcmd.equals("biome"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandBiome") + " <" + C("WordBiome") + ">");
				if(PlotManager.isEconomyEnabled(pwi) && pwi != null && pwi.BiomeChangePrice != 0)
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpBiome") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.BiomeChangePrice));
				else
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpBiome"));
			}
			else if (allowedcmd.equals("biomelist"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandBiomelist"));
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpBiomeList"));
			}
			else if (allowedcmd.equals("done"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandDone"));
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpDone"));
			}
			else if (allowedcmd.equals("tp"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandTp") + " <" + C("InfoId") + ">");
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpTp"));
			}
			else if (allowedcmd.equals("id"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandId"));
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpId"));
			}
			else if (allowedcmd.equals("clear"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandClear"));
				if(PlotManager.isEconomyEnabled(pwi) && pwi != null && pwi.ClearPrice != 0)
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpId") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.ClearPrice));
				else
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpClear"));
			}
			else if (allowedcmd.equals("reset"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandReset"));
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpReset"));
			}
			else if (allowedcmd.equals("add"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandAdd") + " <" + C("WordPlayer") + ">");
				if(PlotManager.isEconomyEnabled(pwi) && pwi != null && pwi.AddPlayerPrice != 0)
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpAdd") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.AddPlayerPrice));
				else
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpAdd"));
			}
			else if (allowedcmd.equals("deny"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandDeny") + " <" + C("WordPlayer") + ">");
				if(PlotManager.isEconomyEnabled(pwi) && pwi != null && pwi.DenyPlayerPrice != 0)
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpDeny") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.DenyPlayerPrice));
				else
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpDeny"));
			}
			else if (allowedcmd.equals("remove")){
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandRemove") + " <" + C("WordPlayer") + ">");
				if(PlotManager.isEconomyEnabled(pwi) && pwi != null && pwi.RemovePlayerPrice != 0)
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpRemove") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.RemovePlayerPrice));
				else
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpRemove"));
			}
			else if (allowedcmd.equals("undeny")){
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandUndeny") + " <" + C("WordPlayer") + ">");
				if(PlotManager.isEconomyEnabled(pwi) && pwi != null && pwi.UndenyPlayerPrice != 0)
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpUndeny") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.UndenyPlayerPrice));
				else
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpUndeny"));
			}
			else if (allowedcmd.equals("setowner"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandSetowner") + " <" + C("WordPlayer") + ">");
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpSetowner"));
			}
			else if (allowedcmd.equals("move"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandMove") + " <" + C("WordIdFrom") + "> <" + C("WordIdTo") + ">");
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandMove") + " " + C("WordFrom") + "|" + C("WordTo"));
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpMove"));
			}
			else if (allowedcmd.equals("weanywhere"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandWEAnywhere"));
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpWEAnywhere"));
			}
			else if (allowedcmd.equals("expired"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandExpired") + " [page]");
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpExpired"));
			}
			else if (allowedcmd.equals("donelist"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandDoneList") + " [page]");
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpDoneList"));
			}
			else if (allowedcmd.equals("addtime"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandAddtime"));
				int days = (pwi == null) ? 0 : pwi.DaysToExpiration;
				if(days == 0)
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpAddTime1") + " " + ChatColor.RESET + C("WordNever"));
				else
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpAddTime1") + " " + ChatColor.RESET + days + ChatColor.AQUA + " " + C("HelpAddTime2"));
			}
			else if (allowedcmd.equals("reload"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plotme reload");
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpReload"));
			}
			else if (allowedcmd.equals("dispose"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandDispose"));
				if(PlotManager.isEconomyEnabled(pwi) && pwi != null && pwi.DisposePrice != 0)
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpDispose") + " " + C("WordPrice") + " : " + ChatColor.RESET + f(pwi.DisposePrice));
				else
					sender.sendMessage(ChatColor.AQUA + " " + C("HelpDispose"));
			}
			else if (allowedcmd.equals("buy"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandBuy"));
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpBuy"));
			}
			else if (allowedcmd.equals("sell"))
			{				
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandSell") + " [" + C("WordAmount") + "]");
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpSell") + " " + C("WordDefault") + " : " + ChatColor.RESET + f(pwi.SellToPlayerPrice));
			}
			else if (allowedcmd.equals("sellbank"))
			{				
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandSellBank"));
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpSellBank") + " " + ChatColor.RESET + f(pwi.SellToBankPrice));
			}
			else if (allowedcmd.equals("auction"))
			{				
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandAuction") + " [" + C("WordAmount") + "]");
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpAuction") + " " + C("WordDefault") + " : " + ChatColor.RESET + "1");
			}
			else if (allowedcmd.equals("resetexpired"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plotme " + C("CommandResetExpired") + " <" + C("WordWorld") + ">");
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpResetExpired"));
			}
			else if (allowedcmd.equals("bid"))
			{
				sender.sendMessage(ChatColor.GREEN + " /plot " + C("CommandBid") + " <" + C("WordAmount") + ">");
				sender.sendMessage(ChatColor.AQUA + " " + C("HelpBid"));
			}
		}
		return true;
	}

	@Override
	public String getUsage() {
		return "/plotme help <" + C("HelpTitle") + ">";
	}

}
