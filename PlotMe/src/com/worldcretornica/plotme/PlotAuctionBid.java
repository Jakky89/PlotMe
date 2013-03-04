package com.worldcretornica.plotme;

import java.util.Comparator;

import org.bukkit.entity.Player;

public class PlotAuctionBid implements Comparator<PlotAuctionBid> {

	private String realplayername;
	private String playerdisplayname;
	private int auctionnumber;
	private Double moneyamount;
	private long date;

	public PlotAuctionBid(int auctionNumber, Player minecraftPlayer, Double moneyAmount)
	{
		auctionnumber = auctionNumber;
		realplayername = minecraftPlayer.getName();
		playerdisplayname = minecraftPlayer.getDisplayName();
		moneyamount = moneyAmount;
		date = Math.round(System.currentTimeMillis() / 1000);
	}
	
	public PlotAuctionBid(int auctionNumber, String playerName, Double moneyAmount)
	{
		auctionnumber = auctionNumber;
		realplayername = playerName;
		playerdisplayname = playerName;
		moneyamount = moneyAmount;
		date = Math.round(System.currentTimeMillis() / 1000);
	}
	
	public PlotAuctionBid(int auctionNumber, String playerName, String displayName, Double moneyAmount)
	{
		auctionnumber = auctionNumber;
		realplayername = playerName;
		playerdisplayname = displayName;
		moneyamount = moneyAmount;
		date = Math.round(System.currentTimeMillis() / 1000);
	}
	
	public int getAuctionNumber()
	{
		return auctionnumber;
	}
	
	public String getBidderRealPlayerName()
	{
		return realplayername;
	}
	
	public String getBidderDisplayName()
	{
		return playerdisplayname;
	}

	public double getMoneyAmount()
	{
		return moneyamount;
	}
	
	public long getDate()
	{
		return date;
	}
	
	
	@Override
	public int compare(PlotAuctionBid bid1, PlotAuctionBid bid2) {
		return (int)Math.round((bid2.moneyamount - bid1.moneyamount) * 1000);
	}

}
