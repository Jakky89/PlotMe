package com.worldcretornica.plotme;

import java.util.Comparator;

import org.bukkit.entity.Player;

public class PlotAuctionBid implements Comparator<PlotAuctionBid> {

	public String realplayername;
	public String playerdisplayname;
	public Double moneyamount;

	public PlotAuctionBid(Player minecraftPlayer, Double moneyAmount)
	{
		realplayername = minecraftPlayer.getName();
		playerdisplayname = minecraftPlayer.getDisplayName();
		this.moneyamount = moneyAmount;
	}
	
	public PlotAuctionBid(String playerName, Double moneyAmount)
	{
		realplayername = playerName;
		playerdisplayname = playerName;
		moneyamount = moneyAmount;
	}
	
	public PlotAuctionBid(String playerName, String displayName, Double moneyAmount)
	{
		realplayername = playerName;
		playerdisplayname = displayName;
		moneyamount = moneyAmount;
	}
	
	public String getBidderRealPlayerName()
	{
		return realplayername;
	}
	
	public String getBidderDisplayName()
	{
		return playerdisplayname;
	}

	public double getBidMoneyAmount()
	{
		return moneyamount;
	}
	
	
	@Override
	public int compare(PlotAuctionBid bid1, PlotAuctionBid bid2) {
		return (int)Math.round((bid2.moneyamount - bid1.moneyamount) * 1000);
	}

}
