package com.worldcretornica.plotme.utils;

import java.util.Comparator;

import com.worldcretornica.plotme.PlotAuctionBid;


public class PlotBidComparator implements Comparator<PlotAuctionBid> {
	
	@Override
	public int compare(PlotAuctionBid bid1, PlotAuctionBid bid2) {
		return (int)Math.round((bid2.bidamount - bid1.bidamount) * 1000);
	}
	
}
