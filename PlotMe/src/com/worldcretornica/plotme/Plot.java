package com.worldcretornica.plotme;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	private String publicflags;
	private Map<PlotGroup, String> groupflags;
	private Map<PlotPlayer, String> playerflags;
	private List<PlotAuctionBid> auctionbids;

	public void setPlotPosition(PlotPosition plotPosition)
	{
		resetNeighbourPlots();
		position = plotPosition;
		PlotDatabase.updatePlotPosition(this);
		notifyNeighbourPlots();
	}
	
	public void setPlotPosition(PlotWorld plotWorld, int plotX, int plotZ)
	{
		if (plotWorld == null)
			return;
		position = new PlotPosition(plotWorld, plotX, plotZ);
		if (neighbourplots == null)
			neighbourplots = new Plot[8];
		plotWorld.notifyNeighbours(this);
	}
	
	public Plot(int plotId, PlotPosition plotPosition)
	{
		id = plotId;
		neighbourplots = null;
		position = plotPosition;
		owner = null;
		biome = Biome.PLAINS;
		properties = null;
		plotrooms = null;
		claimprice = null;
		expiredate = null;
		finishdate = null;
		auctionbids = null;
		auction = null;
		publicflags = "n";
		groupflags = new HashMap<PlotGroup, String>();
		playerflags = new HashMap<PlotPlayer, String>();
	}
	
	public Plot(int plotId, PlotPosition plotPosition, PlotPlayer plotOwner, Biome plotBiome, Float plotClaimPrice, Integer plotExpireDate, Integer plotFinishDate)
	{
		id = plotId;
		neighbourplots = null;
		position = plotPosition;
		owner = plotOwner;
		biome = plotBiome;
		properties = null;
		plotrooms = null;
		claimprice = plotClaimPrice;
		expiredate = plotExpireDate;
		finishdate = plotFinishDate;
		auctionbids = null;
		auction = null;
		publicflags = "n";
		groupflags = new HashMap<PlotGroup, String>();
		playerflags = new HashMap<PlotPlayer, String>();
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
		if (this.position != null)
		{
			return position.x;
		}
		return 0;
	}
	
	public int getPlotZ()
	{
		if (this.position != null)
		{
			return position.z;
		}
		return 0;
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
		if (position != null && position.getPlotWorld() != null)
		{
			return position.getPlotWorld().PlotSize;
		}
		return -1;
	}
	
	public boolean hasNeighbourPlots()
	{
		if (neighbourplots != null)
		{
			for (Plot p : neighbourplots)
			{
				if (p != null)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public Plot getNeighbourPlot(byte dir)
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
	
		for (byte i = 0; i < 8; i++)
		{
			if (neighbourplots[i] != null)
			{
				byte j = (byte)((i+4)%8);
				if (neighbourplots[i].getNeighbourPlot(j) != null)
				{
					if (neighbourplots[i].getNeighbourPlot(j).equals(this))
					{
						neighbourplots[i].setNeighbourPlot((byte)j, null);
					}
				}
			}
		}
		neighbourplots = null;
	}
	
	public void notifyNeighbourPlots()
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

	public void setDaysToExpiration(int days)
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
	
	public boolean isFinished()
	{
		if (finishdate > 0 && finishdate <= Math.round(System.currentTimeMillis()/1000))
		{
			return true;
		}
		
		return false;
	}

	public boolean isForSale()
	{
		if (claimprice != null && claimprice >= 0)
		{
			return true;
		}
		return false;
	}
	
	public boolean isAuctioned()
	{
		if (auction >= 1)
		{
			return true;
		}
		return false;
	}
	
	public boolean isProtected()
	{
		if (expiredate == null || expiredate <= 0 || isForSale() || isFinished() || isAuctioned())
		{
			return true;
		}
		return false;
	}
	
	public Biome getBiome()
	{
		return biome;
	}
	
	public void setBiome(Biome newBiome)
	{
		if (newBiome != null)
		{
			if (biome != newBiome)
			{
				biome = newBiome;
				PlotDatabase.updateStringCell(id, "plots", "biome", newBiome.toString());
			}
		}
		else if (biome != null)
		{
			biome = null;
			PlotDatabase.updateStringCell(id, "plots", "biome", null);
		}
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
	
	public boolean hasPublicFlag(char flag)
	{
		if (publicflags.indexOf(flag)>=0)
		{
			return true;
		}
		return false;
	}
	
	public boolean hasFlag(PlotGroup plotGroup, char flag)
	{
		if (plotGroup == null)
			return false;

		String grpRights = groupflags.get(plotGroup);
		if (grpRights != null && !grpRights.isEmpty())
		{
			if (grpRights.indexOf('n')==-1 && grpRights.indexOf('d')==-1)
			{
				if (grpRights.indexOf('a')>=0 || grpRights.indexOf(flag)>=0)
				{
					return true;
				}
			}
		}

		return true;
	}
	
	public boolean hasFlag(PlotPlayer plotPlayer, char flag)
	{
		if (plotPlayer == null || plotPlayer.getPlayer() == null)
			return false;
		
		if (owner != null && owner.equals(plotPlayer))
			return true;

		String prights = playerflags.get(plotPlayer);
		if (prights != null && prights.indexOf('n') == -1 && prights.indexOf('d') == -1 && prights.indexOf(flag) >= 0)
			return true;

		if (plotPlayer.getPlayer().isOnline())
		{
			Entry<PlotGroup, String> grpright;
			Iterator<Entry<PlotGroup, String>> grprightsIterator = groupflags.entrySet().iterator();
			while (grprightsIterator.hasNext())
			{
				grpright = grprightsIterator.next();
				if (grpright.getValue().indexOf(flag)>=0)
				{
					if (plotPlayer.getPlayer().hasPermission("plotme.group." + grpright.getKey().getName()))
						return true;
				}
			}
		}

		return false;
	}

	public void setFlag(PlotPlayer plotPlayer, char flag)
	{
		if (plotPlayer == null || plotPlayer.getPlayer() == null)
			return;
		String prights = playerflags.get(plotPlayer);
		if (prights.indexOf(flag) == -1)
		{
			prights += flag;
		}
	}
	
	public void removeFlag(PlotPlayer plotPlayer, char flag)
	{
		if (plotPlayer == null || plotPlayer.getPlayer() == null)
			return;
		String prights = playerflags.get(plotPlayer);
		if (prights.indexOf(flag) >= 0)
		{
			prights.replace(String.valueOf(flag), "");
		}
	}

	public boolean isAllowed(PlotPlayer plotPlayer)
	{
		if (hasFlag(plotPlayer, 'o') || hasFlag(plotPlayer, 'a'))
		{
			return true;
		}
		return false;
	}
	
	public boolean isDenied(PlotPlayer plotPlayer)
	{
		if (hasFlag(plotPlayer, 'd'))
		{
			return true;
		}
		return false;
	}
	
	public boolean canUseWorldEdit(PlotPlayer plotPlayer)
	{
		if (hasFlag(plotPlayer, 'w'))
		{
			return true;
		}
		return false;
	}
	
	public void setOwner(PlotPlayer newOwner)
	{
		if ((newOwner != null && owner == null) || (owner == null && newOwner != null) || !newOwner.equals(owner))
		{
			owner = newOwner;
			setFlag(newOwner, 'o');
			owner.addPlotWithRights(this);
			PlotManager.actualizePlotSigns(this);
			PlotDatabase.updateIntegerCell(id, "plots", "owner", newOwner.getId());
		}
	}
	
	public void unsetOwner()
	{
		if (owner != null)
		{
			String prights = playerflags.get(owner);
			if (prights != null && !prights.isEmpty())
			{
				prights.replace("o", "");
			}
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
