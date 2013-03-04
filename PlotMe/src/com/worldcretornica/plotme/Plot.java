package com.worldcretornica.plotme;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
	private int id;
	
	private PlotPosition position;
	private Plot[] neighbourplots;

	private PlotOwner owner;
	private Biome biome;
	
	private Jakky89Properties properties; // Flexible plot properties
	private Set<PlotRoom> plotRooms;

	private long expireddate;
	private long finisheddate;
	private double price;
	private boolean isforsale;
	private boolean isprotected;
	private int auction;
	private List<PlotAuctionBid> auctionbids;
	
	public void setPlotPosition(PlotWorld plotWorld, int plotX, int plotZ)
	{
		resetNeighbourPlots();
		position = new PlotPosition(plotWorld, plotX, plotZ);
		refreshNeighbourPlots();
	}
	
	public Plot(int plotId, PlotPosition plotPosition)
	{
		id = 0;
		owner = null;
		properties = new Jakky89Properties();
		biome = Biome.PLAINS;
		setDaysUntilExpiration(7);
		price = plotPosition.getPlotWorld().ClaimPrice;
		isforsale = false;
		finisheddate = 0;
		isprotected = false;
		neighbourplots = new Plot[8];
		auctionbids = null;
		auction = -1;
	}
	
	public Plot(int plotId, PlotPosition plotPosition, PlotOwner plotOwner, Biome plotBiome, long plotExpiredDate,
				long plotFinishedDate, double plotPrice, boolean plotIsForSale, boolean plotIsProtected, boolean plotIsAuctionned)
	{
		id = plotId;
		position = plotPosition;
		owner = plotOwner;
		biome = plotBiome;
		expireddate = plotExpiredDate;
		PlotManager.checkPlotExpiration(this);
		finisheddate = plotFinishedDate;
		price = plotPrice;
		isforsale = plotIsForSale;
		isprotected = plotIsProtected;
		neighbourplots = new Plot[8];
		auctionbids = null;
		auction = -1;
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
		return -1;
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
			return -1;
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
	
	public boolean addAuctionBid(Player bidderPlayer, Double bidAmount)
	{
		if (auction < 1)
		{
			return false;
		}
		
		if (auctionbids == null)
		{
			auctionbids = new LinkedList<PlotAuctionBid>();
		}

		if (auctionbids == null || bidAmount > auctionbids.get(0).getMoneyAmount())
		{
			auctionbids.add(0, new PlotAuctionBid(auction, bidderPlayer.getName(), bidAmount));
			PlotManager.actualizePlotSigns(this);
			PlotMeSqlManager.addPlotBid(this, bidderPlayer, bidAmount);
			return true;
		}
		return false;
	}
	
	public void enableSelling()
	{
		if (isforsale != true)
		{
			isforsale = true;
			PlotManager.actualizePlotSigns(this);
			PlotManager.adjustWall(this);
			PlotMeSqlManager.updatePlotData(this, "isforsale", 1);
		}
	}
	
	public void disableSelling()
	{
		if (isforsale != false)
		{
			isforsale = false;
			PlotManager.actualizePlotSigns(this);
			PlotManager.adjustWall(this);
			PlotMeSqlManager.updatePlotData(this, "isforsale", 0);
		}
	}
	
	public boolean isForSale()
	{
		return isforsale;
	}
	
	public void setProtected(boolean protect)
	{
		if (protect != isprotected)
		{
			isprotected = protect;
			PlotManager.actualizePlotSigns(this);
			PlotManager.adjustWall(this);
			if (isprotected)
			{
				PlotMeSqlManager.updatePlotData(this, "isprotected", 1);
			}
			else
			{
				PlotMeSqlManager.updatePlotData(this, "isprotected", 0);
			}
		}
	}
	
	public boolean isProtected()
	{
		return isprotected;
	}

	public void setDaysUntilExpiration(int days)
	{
		if (days > 0)
		{
			int newDate = Math.round(System.currentTimeMillis()/1000) + (days*86400);
			if (newDate != expireddate)
			{
				expireddate = newDate;
				PlotManager.checkPlotExpiration(this);
				PlotManager.actualizePlotSigns(this);
				PlotMeSqlManager.updatePlotData(this, "expireddate", newDate);
			}
		}
	}
	
	public void resetExpiration(int days)
	{
		if (days <= 0)
		{
			doExpire();
		}
		else
		{
			setDaysUntilExpiration(days);
		}
	}
	
	public void disableExpiration()
	{
		expireddate = -1;
		PlotManager.actualizePlotSigns(this);
		PlotManager.adjustWall(this);
		PlotMeSqlManager.updatePlotData(this, "expireddate", null);
	}
	
	public long getExpiration()
	{
		if (finisheddate > 0 || isprotected || isforsale)
		{
			return -1;
		}
		return expireddate;
	}
	
	public boolean isExpired()
	{
		if (expireddate > 0 && expireddate <= Math.round(System.currentTimeMillis()/1000))
		{
			return true;
		}
		return false;
	}
	
	public void doExpire()
	{
		expireddate = Math.round(System.currentTimeMillis()/1000);
		PlotManager.actualizePlotSigns(this);
		PlotManager.adjustWall(this);
	}
	
	public void setFinished()
	{
		int currentTime = Math.round(System.currentTimeMillis()/1000);
		if (currentTime != finisheddate)
		{
			finisheddate = currentTime;
			PlotManager.actualizePlotSigns(this);
			PlotManager.adjustWall(this);
			PlotMeSqlManager.updatePlotData(this, "finisheddate", currentTime);
		}
	}
	
	public void setUnfinished()
	{
		if (finisheddate != -1)
		{
			finisheddate = -1;
			PlotManager.actualizePlotSigns(this);
			PlotManager.adjustWall(this);
			PlotMeSqlManager.updatePlotData(this, "finisheddate", null);
		}
	}
	
	public long getFinish()
	{
		return finisheddate;
	}
	
	public boolean isFinished()
	{
		if (finisheddate > 0 && finisheddate <= Math.round(System.currentTimeMillis()/1000))
		{
			return true;
		}
		return false;
	}
	
	public Biome getBiome()
	{
		return biome;
	}
	
	public boolean setBiome(Biome bio)
	{
		if (bio != null && biome != bio)
		{
			biome = bio;
			PlotMeSqlManager.updatePlotData(this, "biome", bio.toString());
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
	
	public String getOwnerRealName()
	{
		if (owner != null)
		{
			return owner.getRealName();
		}
		return null;
	}

	public PlotOwner getOwner()
	{
		return owner;
	}
	
	public void setOwner(PlotOwner newOwner)
	{
		if (!newOwner.equals(owner))
		{
			owner = newOwner;
			PlotManager.actualizePlotSigns(this);
			PlotMeSqlManager.updatePlotData(this, "owner", owner.getId());
		}
	}
	
	public void setPrice(double newPrice)
	{
		if (newPrice != price && newPrice >= 0)
		{
			price = newPrice;
			PlotManager.actualizePlotSigns(this);
			PlotMeSqlManager.updatePlotData(this, "isforsale", price);
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

	
	public double getPrice()
	{
		return price;
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
		if (auction != -1)
		{
			auction = -1;
			PlotManager.actualizePlotSigns(this);
			PlotManager.adjustWall(this);
			PlotMeSqlManager.updatePlotData(this, "auction", null);
			return true;
		}
		return false;
	}
	
	public boolean enableAuctioning()
	{
		if (auction == -1)
		{
			auctionbids.clear();
			auction = PlotMeSqlManager.getNextAuctionNumber(this);
			if (auction > 0)
			{
				PlotManager.actualizePlotSigns(this);
				PlotManager.adjustWall(this);
				PlotMeSqlManager.updatePlotData(this, "isauctionned", 1);
				return true;
			}
		}
		return false;
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
	
	public String[] getComment(int i)
	{
		@SuppressWarnings("unchecked")
		HashMap<Integer, Jakky89Properties> comments = (HashMap<Integer, Jakky89Properties>)properties.getValue("comments");
		if (comments != null)
		{
			return (String[])comments.get(i).getValue("texts");
		}
		return null;
	}
	
	public void setProperty(String property, Object value)
	{
		property = property.toLowerCase();
		if (properties.setValue(property, value))
		{
			PlotMeSqlManager.savePlotProperties(this);
		}
	}
	
	public boolean isAllowed(String playerName, boolean includeStar, boolean includeGroups)
	{
		if (playerName == null || playerName.isEmpty())
		{
			return false;
		}

		Player player = Bukkit.getServer().getPlayerExact(playerName);
		if (player != null)
		{
			playerName = player.getName();
			if (owner != null && !owner.getRealName().isEmpty())
			{
				if (owner.equals(playerName) || (includeStar && owner.equals("*")))
				{
					return true;
				}
				if (includeGroups && owner.getRealName().length()>6 && player.hasPermission("plotme.group." + owner.getRealName().substring(6)))
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
				if (includeStar && rightsAllowed.getBoolean("*")==true)
				{
					return true;
				}
				if (player != null)
				{
					if (rightsAllowed.isStringInHashSet("players", playerName))
					{
						return true;
					}
					if (includeStar && rightsAllowed.isStringInHashSet("players", "*"))
					{
						return true;
					}
					if (includeGroups)
					{
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
		}
		return false;
	}
	
	public boolean isDenied(String playerName, boolean includeStar, boolean includeGroups)
	{
		if (playerName == null || playerName.isEmpty())
		{
			return true;
		}
		
		Player player = Bukkit.getServer().getPlayerExact(playerName);
		if (player != null)
		{
			playerName = player.getName();
			if (owner != null && !owner.getRealName().isEmpty())
			{
				if (owner.equals(playerName) || (includeStar && owner.equals("*")))
				{
					return false;
				}
				if (includeGroups && owner.getRealName().length()>6 && player.hasPermission("plotme.group." + owner.getRealName().substring(6)))
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
				if (includeStar && rightsDenied.getBoolean("*")==true)
				{
					return true;
				}
				if (player != null)
				{
					if (rightsDenied.isStringInHashSet("players", playerName))
					{
						return true;
					}
					if (includeStar && rightsDenied.isStringInHashSet("players", "*"))
					{
						return true;
					}
					if (includeGroups)
					{
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
		}

		return true;
	}

	public void updatePlotData(String field, Object value)
	{
		PlotMeSqlManager.updatePlotData(this, field, value);
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
