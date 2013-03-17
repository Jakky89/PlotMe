package com.worldcretornica.plotme;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import com.worldcretornica.plotme.rooms.PlotRoom;
import com.worldcretornica.plotme.utils.Jakky89Properties;
import com.worldcretornica.plotme.utils.Pair;


public class Plot implements Comparable<Plot>
{
	private Integer id;
	
	private PlotPosition position;
	private Plot[] neighbourplots;

	private PlotPlayer owner;
	private Biome biome;
	
	private Jakky89Properties properties; // Flexible plot properties
	private Set<PlotRoom> plotrooms;

	private Float claimprice;
	private Integer expiredate;
	private Integer finishdate;
	
	private Integer auction;
	private Map<PlotGroup, BitSet> grouprights;
	private Map<PlotPlayer, BitSet> playerrights;
	private List<PlotAuctionBid> auctionbids;

	public void setPlotPosition(PlotPosition plotPosition)
	{
		resetNeighbourPlots();
		position = plotPosition;
		PlotDatabase.updatePlotPosition(this);
		refreshNeighbourPlots();
	}
	
	public void setPlotPosition(PlotWorld plotWorld, int plotX, int plotZ)
	{
		if (plotWorld == null)
			return;
		position = new PlotPosition(plotWorld, plotX, plotZ);
		if (neighbourplots == null)
			neighbourplots = new Plot[8];
		plotWorld.refreshNeighbours(this);
		PlotDatabase.updatePlotPosition(this);
	}
	
	public Plot(int plotId, PlotPosition plotPosition)
	{
		id = plotId;
		neighbourplots = null;
		setPlotPosition(plotPosition);
		owner = null;
		biome = Biome.PLAINS;
		properties = null;
		plotrooms = null;
		setDaysLeftUntilExpiration(PlotMe.DEFAULT_DAYS_TO_EXPIRATION);
		claimprice = null;
		expiredate = null;
		finishdate = null;
		auctionbids = null;
		auction = null;
	}
	
	public Plot(int plotId, PlotPosition plotPosition, PlotPlayer plotOwner, Biome plotBiome, Float plotClaimPrice, Integer plotExpireDate, Integer plotFinishDate)
	{
		id = plotId;
		neighbourplots = null;
		setPlotPosition(plotPosition);
		owner = plotOwner;
		biome = plotBiome;
		properties = null;
		plotrooms = null;
		claimprice = plotClaimPrice;
		expiredate = plotExpireDate;
		PlotManager.checkPlotExpiration(this);
		finishdate = plotFinishDate;
		auctionbids = null;
		auction = null;
	}

	public int getId()
	{
		return id;
	}
	
	public PlotPosition getPlotPosition()
	{
		return position;
	}
	
	public int getPlotX()
	{
		return position.x;
	}
	
	public int getPlotZ()
	{
		return position.z;
	}
	
	public PlotWorld getPlotWorld()
	{
		if (this.position != null)
		{
			return position.w;
		}
		return null;
	}
	
	public World getMinecraftWorld()
	{
		if (position != null && position.w != null)
		{
			return position.getMinecraftWorld();
		}
		return null;
	}
	
	public Location getWorldMinBlockLocation()
	{
		if (position != null && position.w != null)
		{
			return position.getPlotWorld().getMinBlockLocation(this);
		}
		return null;
	}
	
	public Location getWorldMaxBlockLocation()
	{
		if (position != null && position.w != null)
		{
			return position.getPlotWorld().getMaxBlockLocation(this);
		}
		return null;
	}
	
	public Pair<Location, Location> getWorldMinMaxBlockLocations()
	{
		if (position != null && position.w != null)
		{
			return position.getPlotWorld().getMinMaxBlockLocation(this);
		}
		return null;
	}
	
	public int getPlotSize()
	{
		if (position != null && position.w != null)
		{
			return position.getPlotWorld().PlotSize;
		}
		return 0;
	}
	
	public boolean hasNeighbourPlots()
	{
		if (neighbourplots != null)
		{
			for (byte i=0; i<8; i++)
			{
				if (neighbourplots[i] != null)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public Plot getNeighbourPlot(int dir)
	{
		if (neighbourplots == null)
		{
			return null;
		}
		if (dir >= 0 && dir <= 7)
		{
			return neighbourplots[dir];
		}
		return null;
	}
	
	public void setNeighbourPlot(byte dir, Plot plot)
	{
		if (neighbourplots == null)
		{
			if (plot == null)
			{
				return;
			}
			neighbourplots = new Plot[8];
		}
		if (dir >= 0 && dir <= 7)
		{
			neighbourplots[dir] = plot;
		}
		if (plot == null)
		{
			if (!hasNeighbourPlots())
			{
				neighbourplots = null;
			}
		}
	}

	public void resetNeighbourPlots()
	{
		if (neighbourplots == null)
		{
			return;
		}
		
		/**
		 * +++++++++++++++++++++++++
		 * +  #7   +  #0   +  #1   +
		 * +(-1,-1)+( 0,-1)+( 1,-1)+
		 * +++++++++++++++++++++++++
		 * +  #6   +       +  #2   +
		 * +(-1,0 )+       +( 1,0 )+
		 * +++++++++++++++++++++++++
		 * +  #5   +  #4   +  #3   +
		 * +(-1,1 )+( 0,1 )+( 1,1 )+
		 * +++++++++++++++++++++++++
		 */
	
		for (byte i=0; i<8; i++)
		{
			if (neighbourplots[i] != null)
			{
				int j = (i+4)%8;
				if (neighbourplots[i].getNeighbourPlot(j) != null)
				{
					if (neighbourplots[i].getNeighbourPlot(j).getId() == this.id)
					{
						neighbourplots[i].setNeighbourPlot((byte)j, null);
					}
				}
			}
		}
		neighbourplots = null;
	}
	
	public void refreshNeighbourPlots()
	{
		if (neighbourplots == null || neighbourplots.length < 8)
		{
			return;
		}
		
		/**
		 * +++++++++++++++++++++++++
		 * +  #7   +  #0   +  #1   +
		 * +(-1,-1)+( 0,-1)+( 1,-1)+
		 * +++++++++++++++++++++++++
		 * +  #6   +       +  #2   +
		 * +(-1,0 )+       +( 1,0 )+
		 * +++++++++++++++++++++++++
		 * +  #5   +  #4   +  #3   +
		 * +(-1,1 )+( 0,1 )+( 1,1 )+
		 * +++++++++++++++++++++++++
		 */

		for (byte i=0; i<8; i++)
		{
			if (neighbourplots[i] != null)
			{
				neighbourplots[i].setNeighbourPlot((byte)((i+4)%8), this);
			}
		}
	}
	
	public int getAuctionNumber()
	{
		return auction;
	}
	
	public int getAuctionBidsCount()
	{
		if (isAuctioned())
		{
			return 0;
		}
		return auctionbids.size();
	}
	
	public PlotAuctionBid getAuctionBid(int bidIndex)
	{
		if (bidIndex >= 0 && bidIndex < auctionbids.size())
		{
			return auctionbids.get(bidIndex);
		}
		return null;
	}
	
	public PlotAuctionBid getHighestAuctionBid()
	{
		if (auctionbids == null || auctionbids.isEmpty())
		{
			return null;
		}
		
		return (PlotAuctionBid)((LinkedList<PlotAuctionBid>)auctionbids).getFirst();
	}
	
	public boolean addAuctionBid(PlotPlayer bidder, Double bidAmount)
	{
		if (auction == null || auction < 1)
		{
			return false;
		}
		
		if (auctionbids == null)
		{
			auctionbids = new LinkedList<PlotAuctionBid>();
		}

		PlotAuctionBid highestbid = getHighestAuctionBid();
		if (highestbid == null || bidAmount > highestbid.getMoneyAmount())
		{
			((LinkedList<PlotAuctionBid>)auctionbids).addFirst(new PlotAuctionBid(auction, bidder.getName(), bidAmount));
			PlotManager.actualizePlotSigns(this);
			return true;
		}
		
		return false;
	}
	
	public void enableSelling(float claimPrice)
	{
		if ((claimprice == null || claimprice != claimPrice) && claimPrice >= 0)
		{
			claimprice = claimPrice;
			PlotManager.actualizePlotSigns(this);
			PlotManager.adjustWall(this);
			PlotDatabase.updateFloatCell(id, "plots", "claimprice", claimPrice);
		}
	}
	
	public void disableSelling()
	{
		if (claimprice != null)
		{
			claimprice = null;
			PlotManager.actualizePlotSigns(this);
			PlotManager.adjustWall(this);
			PlotDatabase.updateFloatCell(id, "plots", "claimprice", null);
		}
	}
	
	public boolean isForSale()
	{
		if (claimprice != null && claimprice >= 0)
		{
			return true;
		}
		return false;
	}

	public void setDaysLeftUntilExpiration(int days)
	{
		if (days > 0)
		{
			int newDate = Math.round(System.currentTimeMillis()/1000) + (days*86400);
			if (newDate != expiredate)
			{
				expiredate = newDate;
				PlotManager.checkPlotExpiration(this);
				PlotManager.actualizePlotSigns(this);
				PlotDatabase.updateIntegerCell(id, "plots", "expiredate", newDate);
			}
		}
	}
	
	public void disableExpiration()
	{
		if (expiredate != null)
		{
			expiredate = null;
			PlotManager.actualizePlotSigns(this);
			PlotManager.adjustWall(this);
			PlotDatabase.updateIntegerCell(id, "plots", "expiredate", null);
		}
	}
	
	public Integer getExpireDate()
	{
		if (finishdate != null || finishdate > 0)
		{
			return null;
		}
		return expiredate;
	}
	
	public boolean isExpired()
	{
		if (finishdate > 0 || finishdate >= 0)
		{
			return false;
		}
		
		if (expiredate != null && expiredate > 0 && expiredate < Math.round(System.currentTimeMillis() / 1000))
		{
			return true;
		}
		
		return false;
	}
	
	public void doExpire()
	{
		int currentTime = Math.round(System.currentTimeMillis()/1000);
		if (expiredate == null || expiredate != currentTime)
		{
			expiredate = currentTime;
			PlotManager.actualizePlotSigns(this);
			PlotManager.adjustWall(this);
			PlotDatabase.updateIntegerCell(id, "plots", "expiredate", currentTime);
		}
	}
	
	public void setFinished()
	{
		int currentTime = Math.round(System.currentTimeMillis()/1000);
		if (finishdate == null || finishdate != currentTime)
		{
			finishdate = currentTime;
			PlotManager.actualizePlotSigns(this);
			PlotManager.adjustWall(this);
			PlotDatabase.updateIntegerCell(id, "plots", "finishdate", currentTime);
		}
	}
	
	public void setUnfinished()
	{
		if (finishdate != null)
		{
			finishdate = null;
			PlotManager.actualizePlotSigns(this);
			PlotManager.adjustWall(this);
			PlotDatabase.updateIntegerCell(id, "plots", "finishdate", null);
		}
	}
	
	public long getFinishDate()
	{
		return finishdate;
	}
	
	public boolean isFinished()
	{
		if (finishdate > 0 && finishdate <= Math.round(System.currentTimeMillis()/1000))
		{
			return true;
		}
		
		return false;
	}
	
	public Biome getBiome()
	{
		return biome;
	}
	
	public boolean setBiome(Biome newBiome)
	{
		if (newBiome != null)
		{
			if (biome != newBiome)
			{
				biome = newBiome;
				PlotDatabase.updateStringCell(id, "plots", "biome", newBiome.toString());
				return true;
			}
		}
		else if (biome != null)
		{
			biome = null;
			PlotDatabase.updateStringCell(id, "plots", "biome", null);
			return true;
		}
		return false;
	}
	
	public void setBiome(String newBiome)
	{
		setBiome(Biome.valueOf(newBiome));
	}
	
	public String getOwnerDisplayName()
	{
		if (owner != null)
		{
			return owner.getDisplayName();
		}
		return null;
	}
	
	public String getOwnerName()
	{
		if (owner != null)
		{
			return owner.getName();
		}
		return null;
	}

	public PlotPlayer getOwner()
	{
		return owner;
	}
	
	public void setOwner(PlotPlayer newOwner)
	{
		if (!newOwner.equals(owner))
		{
			owner = newOwner;
			PlotManager.actualizePlotSigns(this);
			PlotDatabase.updateIntegerCell(id, "plots", "owner", owner.getId());
		}
	}
	
	public void unsetOwner()
	{
		if (owner != null)
		{
			owner.removeOwnPlot(this);
			owner = null;
			PlotDatabase.updateIntegerCell(id, "plots", "owner", null);
		}
	}
	
	public void setPrice(float newPrice)
	{
		if (newPrice != claimprice && newPrice >= 0)
		{
			claimprice = newPrice;
			PlotManager.actualizePlotSigns(this);
			PlotDatabase.updateFloatCell(id, "plots", "claimprice", claimprice);
		}
	}
	
	public void resetPriceToWorldsDefault()
	{
		if (position == null || position.getPlotWorld() == null)
		{
			return;
		}
		setPrice(position.getPlotWorld().ClaimPrice);
	}

	
	public double getClaimPrice()
	{
		return claimprice;
	}
	
	public boolean isAuctioned()
	{
		if (auction >= 1)
		{
			return true;
		}
		return false;
	}
	
	public boolean disableAuctioning()
	{
		if (auction != 0)
		{
			auction = 0;
			PlotManager.actualizePlotSigns(this);
			PlotManager.adjustWall(this);
			PlotDatabase.updateIntegerCell(id, "plots", "auction", null);
			return true;
		}
		return false;
	}
	
	public boolean enableAuctioning()
	{
		if (auction == null || auction < 1)
		{
			auctionbids.clear();
			auction = PlotDatabase.getNextAuctionNumber();
			if (auction >= 1)
			{
				PlotManager.actualizePlotSigns(this);
				PlotManager.adjustWall(this);
				PlotDatabase.updateIntegerCell(id, "plots", "auction", auction);
				return true;
			}
		}
		return false;
	}
	
	public void setAuctionNumber(int number)
	{
		if (auction == null || number != auction)
		{
			auction = number;
			PlotManager.actualizePlotSigns(this);
			PlotManager.adjustWall(this);
		}
	}
	
	public int getCommentsCount()
	{
		@SuppressWarnings("unchecked")
		HashMap<Integer, Jakky89Properties> comments = (HashMap<Integer, Jakky89Properties>)properties.getValue("comments");
		if (comments != null)
		{
			return comments.size();
		}
		return 0;
	}

	public Jakky89Properties getProperties()
	{
		return properties;
	}
	
	public void setProperties(Jakky89Properties newProperties)
	{
		properties = newProperties;
	}
	
	public boolean isAllowed(String playerName)
	{
		if (playerName == null || playerName.isEmpty())
		{
			return false;
		}

		Player player = Bukkit.getServer().getPlayerExact(playerName);
		if (player != null)
		{
			playerName = player.getName();
			if (owner != null && !owner.getName().isEmpty())
			{
				if (owner.equals(playerName) || owner.equals("*"))
				{
					return true;
				}
				if (owner.getName().length()>6 && owner.getName().startsWith("group:") && player.hasPermission("plotme.group." + owner.getName().substring(6)))
				{
					return true;
				}
			}
		}
		
		Jakky89Properties rights = properties.getProperties("rights");
		if (rights != null)
		{
			Jakky89Properties rightsAllowed = rights.getProperties("allowed");
			if (rightsAllowed != null)
			{
				if (rightsAllowed.getBoolean("*")==true)
				{
					return true;
				}
				if (player != null)
				{
					if (rightsAllowed.isStringInHashSet("players", playerName))
					{
						return true;
					}
					if (rightsAllowed.isStringInHashSet("players", "*"))
					{
						return true;
					}
					Set<String> allowedGroups = rightsAllowed.getStringHashSet("groups");
					if (allowedGroups != null)
					{
						Iterator<String> agi = allowedGroups.iterator();
						while (agi.hasNext())
						{
							String groupName = agi.next();
							if (player.hasPermission("plotme.group." + groupName))
							{
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	public boolean isDenied(String playerName)
	{
		if (playerName == null || playerName.isEmpty())
		{
			return true;
		}
		
		Player player = Bukkit.getServer().getPlayerExact(playerName);
		if (player != null)
		{
			playerName = player.getName();
			if (owner != null && !owner.getName().isEmpty())
			{
				if (owner.equals(playerName) || owner.equals("*"))
				{
					return false;
				}
				if (owner.getName().length()>6 && owner.getName().startsWith("group:") && player.hasPermission("plotme.group." + owner.getName().substring(6)))
				{
					return false;
				}
			}
		}

		Jakky89Properties rights = properties.getProperties("rights");
		if (rights != null)
		{
			Jakky89Properties rightsDenied = rights.getProperties("denied");
			if (rightsDenied != null)
			{
				if (rightsDenied.getBoolean("*")==true)
				{
					return true;
				}
				if (player != null)
				{
					if (rightsDenied.isStringInHashSet("players", playerName))
					{
						return true;
					}
					if (rightsDenied.isStringInHashSet("players", "*"))
					{
						return true;
					}
					Set<String> deniedGroups = rightsDenied.getStringHashSet("groups");
					if (deniedGroups != null)
					{
						Iterator<String> dgi = deniedGroups.iterator();
						while (dgi.hasNext())
						{
							String groupName = dgi.next();
							if (player.hasPermission("plotme.group." + groupName))
							{
								return false;
							}
						}
					}
				}
			}
		}

		return true;
	}
	
	public boolean isAvailable()
	{
		if (owner == null)
		{
			if (id > 0 && !isFinished() && (isForSale() || auction > 0) && !isExpired())
			{
				return true;
			}
		}
		
		return false;
	}
	
	public List<Pair<String, String>> getComments()
	{
		return properties.getLinkedStringPairList("comments");
	}
	
	public Pair<String, String> getComment(int commentIndex)
	{
		List<Pair<String, String>> allcomments = getComments();
		if (allcomments != null && allcomments.size() > 0 && commentIndex >= 0 && commentIndex < allcomments.size())
		{
			return allcomments.get(commentIndex);
		}
		return null;
	}
	
	public void addComment(String commentAuthor, String commentText)
	{
		List<Pair<String, String>> allcomments = properties.getLinkedStringPairList("comments");

		Pair<String, String> newcomment = new Pair<String, String>(commentAuthor, commentText);
		
		// check if it is no spam
		if (allcomments != null && allcomments.size() > 0)
		{
			Pair<String, String> oldcomment;
			Iterator<Pair<String, String>> commentIterator = allcomments.iterator();
			while (commentIterator.hasNext())
			{
				oldcomment = commentIterator.next();
				if (oldcomment.equals(newcomment))
				{
					commentIterator.remove();
					break;
				}
			}
		}
		
		((LinkedList<Pair<String, String>>)allcomments).addFirst(newcomment);
	}
	
	@Override
	public int hashCode()
	{
		return id;
	}

	@Override
	public boolean equals(Object o)
	{
	    if (o == null)
	    {
	    	return false;
	    }
	    if (o instanceof Plot)
	    {
			if (this.id == ((Plot)o).id)
			{
				return true;
			}
	    }
		return false;
	}

	@Override
	public int compareTo(Plot plot2)
	{
		return this.id-plot2.id;
	}
	
	@Override
	public String toString()
	{
		return "PLOT"+String.valueOf(id);
	}
	
	/*private static Map<String, Double> sortByValues(final Map<String, Double> map) 
	{
		Comparator<String> valueComparator = new Comparator<String>() 
		{
		    public int compare(String k1, String k2) 
		    {
		        int compare = map.get(k2).compareTo(map.get(k1));
		        if (compare == 0) 
		        	return 1;
		        else 
		        	return compare;
		    }
		};
		
		Map<String, Double> sortedByValues = new TreeMap<String, Double>(valueComparator);
		sortedByValues.putAll(map);
		return sortedByValues;
	}*/
}
