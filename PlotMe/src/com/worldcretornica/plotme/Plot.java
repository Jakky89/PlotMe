package com.worldcretornica.plotme;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.worldcretornica.plotme.rooms.PlotRoom;
import com.worldcretornica.plotme.utils.Jakky89Properties;
import com.worldcretornica.plotme.utils.Pair;


public class Plot implements Comparable<Plot>
{
	public int id;
	
	public PlotPosition position;
	public Plot[] neighbourplots;

	public PlotOwner owner;
	public Biome biome;
	
	public Jakky89Properties properties; // Flexible plot properties
	public Set<String> playersinplot; // Names of players that are currently standing in that plot
	public Set<PlotRoom> plotRooms;
	
	public Sign ownersign;
	public Sign sellsign;
	
	public long expireddate;
	public double buyprice;
	public double sellprice;
	public boolean isforsale;
	public long finisheddate;
	public boolean isprotected;
	public boolean isauctionned;
	public List<PlotAuctionBid> auctionbids;
	
	public void setPlotPosition(PlotWorld tW, int tX, int tZ)
	{
		position = new PlotPosition(tW, tX, tZ);
	}
	
	public Plot(int plotId, PlotPosition plotPosition)
	{
		id = 0;
		owner = null;
		properties = new Jakky89Properties();
		playersinplot = new HashSet<String>();
		biome = Biome.PLAINS;
		setDaysUntilExpiration(7);
		sellprice = plotPosition.getPlotWorld().ClaimPrice;
		isforsale = false;
		finisheddate = 0;
		isprotected = false;
		isauctionned = false;
		neighbourplots = new Plot[8];
		auctionbids = null;
		ownersign = null;
		sellsign = null;
	}
	
	public Plot(int plotId, PlotPosition plotPosition, PlotOwner plotOwner, Biome plotBiome, long plotExpiredDate,
				long plotFinishedDate, double plotSellPrice, double plotRentPrice, boolean plotIsForSale, boolean plotIsProtected, boolean plotIsAuctionned)
	{
		id = plotId;
		position = plotPosition;
		owner = plotOwner;
		biome = plotBiome;
		expireddate = plotExpiredDate;
		PlotManager.checkPlotExpiration(this);
		finisheddate = plotFinishedDate;
		sellprice = plotSellPrice;
		isforsale = plotIsForSale;
		isauctionned = plotIsAuctionned;
		isprotected = plotIsProtected;
		neighbourplots = new Plot[8];
		auctionbids = null;
		ownersign = null;
		sellsign = null;
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
			return true;
		}
		return false;
	}
	
	public Plot getNeighbourPlot(int dir)
	{
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
			neighbourplots = new Plot[8];
		}
		if (dir >= 0 && dir <= 7)
		{
			neighbourplots[dir] = plot;
		}
	}

	public void resetNeighbourPlots()
	{
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
				if (neighbourplots[i].neighbourplots != null)
				{
					int j = (i+4)%8;
					if (neighbourplots[i].neighbourplots[j] != null)
					{
						if (neighbourplots[i].neighbourplots[j].id == this.id)
						{
							neighbourplots[i].neighbourplots[j] = null;
						}
					}
				}
				neighbourplots[i] = null;
			}
		}
	}
	
	public void notifyNeighbourPlots()
	{
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
				if (neighbourplots[i].neighbourplots == null)
				{
					neighbourplots[i].neighbourplots = new Plot[8];
				}
				neighbourplots[i].neighbourplots[(i+4)%8] = this;
			}
		}
	}
	
	public boolean addAuctionBid(Player bidderPlayer, Double bidAmount)
	{
		if (!isauctionned)
		{
			return false;
		}
		
		if (auctionbids == null)
		{
			auctionbids = new LinkedList<PlotAuctionBid>();
		}
		
		if (auctionbids.size()>0)
		{
			if (bidAmount > auctionbids.get(0).getBidMoneyAmount())
			{
				auctionbids.add(0, new PlotAuctionBid(bidderPlayer.getName(), bidAmount));
				return true;
			}
		}
		else
		{
			auctionbids.add(new PlotAuctionBid(bidderPlayer.getName(), bidAmount));
			return true;
		}

		return false;
	}
	
	public void enableSelling()
	{
		if (isforsale != true)
		{
			isforsale = true;
			PlotMeSqlManager.updatePlotData(this, "isforsale", 1);
		}
	}
	
	public void disableSelling()
	{
		if (isforsale != false)
		{
			isforsale = false;
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
		PlotMeSqlManager.updatePlotData(this, "expireddate", null);
	}
	
	public long getExpiration()
	{
		return expireddate;
	}
	
	public boolean isExpired()
	{
		if (expireddate <= Math.round(System.currentTimeMillis()/1000))
		{
			return true;
		}
		return false;
	}
	
	public void doExpire()
	{
		expireddate = Math.round(System.currentTimeMillis()/1000);
	}
	
	public void setFinished()
	{
		int newDate = Math.round(System.currentTimeMillis()/1000);
		if (newDate != finisheddate)
		{
			finisheddate = newDate;
			PlotMeSqlManager.updatePlotData(this, "finisheddate", newDate);
		}
	}
	
	public void setUnfinished()
	{
		if (finisheddate != 0)
		{
			finisheddate = -1;
			PlotMeSqlManager.updatePlotData(this, "finisheddate", null);
		}
	}
	
	public long getFinish()
	{
		return finisheddate;
	}
	
	public boolean isFinished()
	{
		if (finisheddate <= Math.round(System.currentTimeMillis()/1000))
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
	
	public Sign getOwnerSign()
	{
		return ownersign;
	}
	
	public void setOwnerSign(Sign sign)
	{
		ownersign = sign;
	}
	
	public Sign getSellSign()
	{
		return sellsign;
	}
	
	public void setSellSign(Sign sign)
	{
		sellsign = sign;
	}
	
	public PlotOwner getOwner()
	{
		return owner;
	}
		
	public void setPrice(double newPrice)
	{
		if (newPrice != sellprice)
		{
			if (newPrice < 0)
			{
				isforsale = false;
				PlotMeSqlManager.updatePlotData(this, "isforsale", 0);
				return;
			}
			sellprice = newPrice;
			
			if (newPrice > 0)
			{
				PlotMeSqlManager.updatePlotData(this, "customprice", newPrice);
				
			}
			else
			{
				PlotMeSqlManager.updatePlotData(this, "customprice", null);
			}
		}
	}
	
	public double getCustomPrice()
	{
		return sellprice;
	}
	
	public boolean isAuctionned()
	{
		return isauctionned;
	}
	
	public void setAuctionned(boolean auction)
	{
		isauctionned = auction;
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
			if (owner != null && !owner.getRealPlayerName().isEmpty())
			{
				if (owner.equals(playerName) || (includeStar && owner.equals("*")))
				{
					return true;
				}
				if (includeGroups && owner.getRealPlayerName().length()>6 && player.hasPermission("plotme.group." + owner.getRealPlayerName().substring(6)))
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
			if (owner != null && !owner.getRealPlayerName().isEmpty())
			{
				if (owner.equals(playerName) || (includeStar && owner.equals("*")))
				{
					return false;
				}
				if (includeGroups && owner.getRealPlayerName().length()>6 && player.hasPermission("plotme.group." + owner.getRealPlayerName().substring(6)))
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
	
	public void playerEntered(String playerName)
	{
		if (playersinplot.add(playerName))
		{
			// action when player entered
		}
	}
	
	public void playerLeft(String playerName)
	{
		if (playersinplot.remove(playerName))
		{
			// action when player left
		}
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
