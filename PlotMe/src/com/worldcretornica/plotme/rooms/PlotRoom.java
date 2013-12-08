package com.worldcretornica.plotme.rooms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.PlotWorld;
import com.worldcretornica.plotme.utils.Jakky89Properties;
import com.worldcretornica.plotme.utils.Pair;


public class PlotRoom {

	private int id;

	private int minx;
	private int miny;
	private int minz;
	
	private int maxx;
	private int maxy;
	private int maxz;
	
	private double rentprice;
	private double rentbalance;
	
	private long nextrentcollection;
	
	private Sign roomsign;
	private PlotRoomRentee rentee;
	private List<Plot> plots;
	private Map<Block, Block> previousblocks;
	
	public Jakky89Properties properties;
	
	
	public PlotRoom(PlotWorld plotWorld, int roomId, int roomX1, int roomY1, int roomZ1, int roomX2, int roomY2, int roomZ2)
	{
		id = roomId;
		rentprice = 0;
		rentbalance = 0;
		roomsign = null;
		rentee = null;
		nextrentcollection = 0;
		previousblocks = new HashMap<Block, Block>();
		setLocations(plotWorld, roomX1, roomY1, roomZ1, roomX2, roomY2, roomZ2);
	}
	
	public PlotRoom(PlotWorld plotWorld, int roomId, int roomX1, int roomY1, int roomZ1, int roomX2, int roomY2, int roomZ2, PlotRoomRentee roomRentee, double roomRentPrice, int roomRentBalance)
	{
		id = roomId;
		rentprice = roomRentPrice;
		rentbalance = roomRentBalance;
		roomsign = null;
		rentee = roomRentee;
		nextrentcollection = 0;
		previousblocks = new HashMap<Block, Block>();
		setLocations(plotWorld, roomX1, roomY1, roomZ1, roomX2, roomY2, roomZ2);
	}
	
	public int getId()
	{
		return id;
	}
	
	public double getBalance()
	{
		return rentbalance;
	}
	
	public PlotRoomRentee getRentee()
	{
		return rentee;
	}
	
	public double getDaysRemain()
	{
		if (rentprice > 0)
		{
			return (rentbalance / rentprice);
		}
		return 0;
	}
	
	public void setRentPrice(double newPrice)
	{
		if (newPrice != rentprice)
		{
			rentprice = newPrice;
			/**
			 * TODO: handle database updating etc.
			 */
		}
	}
	
	public double depositMoneyAmount(double moneyAmount)
	{
		if (moneyAmount > 0)
		{
			rentbalance += moneyAmount;
			/**
			 * TODO: handle database updating etc.
			 */
		}
		return rentbalance;
	}
	
	public void setRentee(PlotRoomRentee newRentee)
	{
		if (newRentee != rentee)
		{
			rentee = newRentee;
			/**
			 * TODO: drawback remaining rent balance to previous owner, handle database updating etc.
			 */
		}
	}
	
	public List<Plot> getPlots()
	{
		return plots;
	}
	
	public int getMinX()
	{
		return minx;
	}
	
	public int getMinY()
	{
		return miny;
	}
	
	public int getMinZ()
	{
		return minz;
	}
	
	public int getMaxX()
	{
		return maxx;
	}
	
	public int getMaxY()
	{
		return maxy;
	}
	
	public int getMaxZ()
	{
		return maxz;
	}
	
	public Pair<Location, Location> getLocations()
	{
		return new Pair<Location, Location>(new Location(plots.get(0).getMinecraftWorld(), minx, miny, minz), new Location(plots.get(0).getMinecraftWorld(), maxx, maxy, maxz));
	}
	
	public boolean setLocations(PlotWorld plotWorld, int x1, int y1, int z1, int x2, int y2, int z2)
	{
		if (plotWorld == null)
		{
			return false;
		}
		
		plots = new ArrayList<Plot>();
		
		minx = Math.min(x1, x2);
		miny = Math.min(y1, y2);
		minz = Math.min(z1, z2);
		
		maxx = Math.max(x1, x2);
		maxy = Math.max(y1, y2);
		maxz = Math.max(z1, z2);
		
		double multi = plotWorld.getPlotBlockPositionMultiplier();
		
		int pminx = (int)Math.floor(minx / multi);
		int pminz = (int)Math.floor(minz / multi);
		
		int pmaxx = (int)Math.ceil(maxx / multi);
		int pmaxz = (int)Math.ceil(maxx / multi);
		
		Plot tmpPlot;
		
		for (int x=pminx; x<=pmaxx; x++)
		{
			for (int z=pminz; z<=pmaxz; z++)
			{
				tmpPlot = plotWorld.getPlotAtPlotPosition(x, z);
				if (tmpPlot != null)
				{
					plots.add(tmpPlot);
				}
				else
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	public boolean setLocations(Location loc1, Location loc2)
	{
		if (!loc1.getWorld().equals(loc2.getWorld()))
		{
			return false;
		}

		return setLocations(PlotManager.getPlotWorld(loc1.getWorld()), loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ(), loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());
	}
	
	public boolean setLocations(Pair<Location, Location> locations)
	{
		return setLocations(locations.getLeft(), locations.getRight());
	}
	
}
