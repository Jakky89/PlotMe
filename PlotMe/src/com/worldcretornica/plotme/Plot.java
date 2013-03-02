package com.worldcretornica.plotme;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;


public class Plot implements Comparable<Plot>
{
	public int id;
	
	public PlotPosition plotpos;
	public Plot[] neighbourplots;

	public PlotOwner owner;
	public Biome biome;
	
	public PlotProperties properties; // Flexible plot properties
	public Set<String> playersinplot; // Names of players that are currently standing in that plot
	
	public long expireddate;
	public double customprice;
	public boolean isforsale;
	public long finisheddate;
	public boolean isprotected;
	public boolean isauctionned;
	public List<PlotAuctionBid> auctionbids;
	
	public void setPlotPosition(PlotWorld tW, int tX, int tZ)
	{
		plotpos = new PlotPosition(tW, tX, tZ);
	}
	
	public Plot()
	{
		id = 0;
		owner = null;
		properties = new PlotProperties(this);
		playersinplot = new HashSet<String>();
		biome = Biome.PLAINS;
		setExpiration(7);
		customprice = 0;
		isforsale = false;
		finisheddate = 0;
		isprotected = false;
		isauctionned = false;
		neighbourplots = new Plot[8];
	}
	
	public Plot(int mid, PlotWorld mpw, int mx, int mz, PlotOwner mowner, String mbiome, long mexpireddate,
				long mfinisheddate, double mcustomprice, boolean misforsale, boolean misprotected, boolean misauctionned)
	{
		id = mid;
		setPlotPosition(mpw, mx, mz);
		owner = mowner;
		biome = Biome.valueOf(mbiome);
		expireddate = Math.round(System.currentTimeMillis()/1000) + mexpireddate;
		finisheddate = mfinisheddate;
		customprice = mcustomprice;
		isforsale = misforsale;
		isprotected = misprotected;
		isauctionned = misauctionned;
		neighbourplots = new Plot[8];
	}
	
	public int getId()
	{
		return id;
	}
	
	public PlotPosition getPosition()
	{
		return plotpos;
	}
	
	public int getPlotX()
	{
		return plotpos.x;
	}
	
	public int getPlotZ()
	{
		return plotpos.z;
	}
	
	public PlotWorld getPlotWorld()
	{
		return plotpos.w;
	}
	
	public World getMinecraftWorld()
	{
		if (plotpos.w != null)
		{
			return plotpos.w.MinecraftWorld;
		}
		return null;
	}
	
	public boolean hasNeighbourPlots()
	{
		if (neighbourplots != null)
		{
			return true;
		}
		return false;
	}
	
	public Plot getNeighbourPlot(byte dir)
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
			if (bidAmount > auctionbids.get(0).bidamount)
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
	
	public void enableSaling()
	{
		if (isforsale != true)
		{
			isforsale = true;
			PlotMeSqlManager.updatePlotData(this, "isforsale", 1);
		}
	}
	
	public void disableSaling()
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
	
	public boolean isProtected()
	{
		return isprotected;
	}

	public void setExpiration(int days)
	{
		if (days > 0)
		{
			int newDate = Math.round(System.currentTimeMillis()/1000) + (days*86400);
			if (newDate != expireddate)
			{
				expireddate = newDate;
				PlotManager.checkNextExpiration(this);
				PlotMeSqlManager.updatePlotData(this, "expireddate", newDate);
			}
		}
	}
	
	public void resetExpiration(int days)
	{
		if (days <= 0)
		{
			setExpiration(Math.round(System.currentTimeMillis()/1000));
		}
		else
		{
			setExpiration(days);
		}
	}
	
	public void disableExpiration()
	{
		expireddate = 0;
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
			finisheddate = 0;
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
		if (bio != null && bio != biome)
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
	
	public PlotOwner getOwner()
	{
		return owner;
	}
	
	public String getOwnerName()
	{
		return owner.playername;
	}
	
	public void setPrice(double newPrice)
	{
		if (newPrice != customprice)
		{
			if (newPrice < 0)
			{
				isforsale = false;
				PlotMeSqlManager.updatePlotData(this, "isforsale", 0);
				return;
			}
			customprice = newPrice;
			
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
		return customprice;
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
		HashMap<Integer, PlotProperties> comments = (HashMap<Integer, PlotProperties>)properties.getValue("comments");
		if (comments != null)
		{
			return comments.size();
		}
		return 0;
	}
	
	public String[] getComment(int i)
	{
		@SuppressWarnings("unchecked")
		HashMap<Integer, PlotProperties> comments = (HashMap<Integer, PlotProperties>)properties.getValue("comments");
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
			if (owner != null && !owner.playername.isEmpty())
			{
				if (owner.equals(playerName) || (includeStar && owner.equals("*")))
				{
					return true;
				}
				if (includeGroups && owner.playername.length()>6 && player.hasPermission("plotme.group." + owner.playername.substring(6)))
				{
					return true;
				}
			}
		}
		
		PlotProperties rights = properties.getProperties("rights");
		if (rights != null)
		{
			PlotProperties rightsAllowed = rights.getProperties("allowed");
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
			if (owner != null && !owner.playername.isEmpty())
			{
				if (owner.equals(playerName) || (includeStar && owner.equals("*")))
				{
					return false;
				}
				if (includeGroups && owner.playername.length()>6 && player.hasPermission("plotme.group." + owner.playername.substring(6)))
				{
					return false;
				}
			}
		}

		PlotProperties rights = properties.getProperties("rights");
		if (rights != null)
		{
			PlotProperties rightsDenied = rights.getProperties("denied");
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
