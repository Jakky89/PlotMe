package com.worldcretornica.plotme;

import org.bukkit.entity.Player;

public class PlotAuctionBid {

	public String bidplayername;
	public String biddisplayname;
	public Double bidamount;

	
	public PlotAuctionBid(String playerName, Double amount)
	{
		this.bidplayername = playerName;
		this.biddisplayname = playerName;
		this.bidamount = amount;
	}
	
	public PlotAuctionBid(Player minecraftPlayer, Double amount)
	{
		this.bidplayername = minecraftPlayer.getName();
		this.biddisplayname = minecraftPlayer.getDisplayName();
		this.bidamount = amount;
	}
	
}
