package com.worldcretornica.plotme;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.worldcretornica.plotme.utils.PlotPosition;


public class PlotWorld
{
	
	public int id;
	public World MinecraftWorld;

	public int PlotSize;
	public int PlotAutoLimit;
	public int PathWidth;
	
	public short BottomBlockId;
	public byte BottomBlockValue;
	public short WallBlockId;
	public byte WallBlockValue;
	public short PlotFloorBlockId;
	public byte PlotFloorBlockValue;
	public short PlotFillingBlockId;
	public byte PlotFillingBlockValue;
	
	public short RoadMainBlockId;
	public byte RoadMainBlockValue;
	public short RoadStripeBlockId;
	public byte RoadStripeBlockValue;

	public int RoadHeight;
	public int DaysToExpiration;
	
	public String ProtectedWallBlockId;
	public String ForSaleWallBlockId;
	public String AuctionWallBlockId;

	public boolean UseEconomy;
	public boolean CanPutOnSale;
	public boolean CanSellToBank;
	public boolean RefundClaimPriceOnReset;
	public boolean RefundClaimPriceOnSetOwner;
	public double ClaimPrice;
	public double ClearPrice;
	public double AddPlayerPrice;
	public double DenyPlayerPrice;
	public double RemovePlayerPrice;
	public double UndenyPlayerPrice;
	public double PlotHomePrice;
	public boolean CanCustomizeSellPrice;
	public double SellToPlayerPrice;
	public double SellToBankPrice;
	public double BuyFromBankPrice;
	public double AddCommentPrice;
	public double BiomeChangePrice;
	public double ProtectPrice;
	public double DisposePrice;
	
	public boolean AutoLinkPlots;
	public boolean DisableExplosion;
	public boolean DisableIgnition;
	
	public HashSet<Integer> ProtectedBlocks;
	public HashSet<String> PreventedItems;
	
	public Map<PlotPosition, Plot> plotPositions;
	

	public PlotWorld(World w)
	{
		this.MinecraftWorld = w;
		this.plotPositions = new HashMap<PlotPosition, Plot>();
	}
	
	public void resetNeighbours(Plot plot)
	{
		if (plot == null)
		{
			return;
		}
		if (plot.plotneighbours == null)
		{
			plot.plotneighbours = new Plot[8];
		}
		else
		{
			for (byte i=0; i<8; i++)
			{
				if (plot.plotneighbours[i] != null)
				{
					if (plot.plotneighbours[i].plotneighbours != null)
					{
						plot.plotneighbours[i].plotneighbours[(i+4)%8] = null;
					}
					plot.plotneighbours[i] = null;
				}
			}
		}
	}
	
	public void notifyNeighbours(Plot plot)
	{
		if (plot == null)
		{
			return;
		}
		for (byte i=0; i<8; i++)
		{
			if (plot.plotneighbours[i] != null)
			{
				if (plot.plotneighbours[i].plotneighbours == null)
				{
					plot.plotneighbours[i].plotneighbours = new Plot[8];
				}
				plot.plotneighbours[i].plotneighbours[(i+4)%8] = plot;
			}
		}
	}
	
	public void refreshNeighbours(Plot plot)
	{
		resetNeighbours(plot);
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
		
		Integer px = plot.plotpos.getPlotX();
		Integer pz = plot.plotpos.getPlotZ();
		
		plot.plotneighbours[0] = getPlotAtPlotPosition( px    , pz - 1 );
		plot.plotneighbours[1] = getPlotAtPlotPosition( px + 1, pz - 1 );
		plot.plotneighbours[2] = getPlotAtPlotPosition( px + 1, pz     );
		plot.plotneighbours[3] = getPlotAtPlotPosition( px + 1, pz + 1 );
		plot.plotneighbours[4] = getPlotAtPlotPosition( px    , pz + 1 );
		plot.plotneighbours[5] = getPlotAtPlotPosition( px - 1, pz + 1 );
		plot.plotneighbours[6] = getPlotAtPlotPosition( px - 1, pz     );
		plot.plotneighbours[7] = getPlotAtPlotPosition( px - 1, pz - 1 );
		
		notifyNeighbours(plot);
	}
	
	public void registerPlot(Plot plot) {
		if (plot == null || plot.plotpos == null)
		{
			return;
		}
		plotPositions.put(plot.plotpos, plot);
	}
	
	public void unregisterPlot(PlotPosition plotpos)
	{
		if (plotpos == null)
		{
			return;
		}
		plotPositions.remove(plotpos);
	}
	
	public void unregisterPlot(Plot plot)
	{
		if (plot == null)
		{
			return;
		}
		unregisterPlot(plot.plotpos);
	}

	public Plot getPlotAtPlotPosition(int pX, int pZ)
	{
		return plotPositions.get(new PlotPosition(Integer.valueOf(pX), Integer.valueOf(pZ)));
	}
	
	public Plot getPlotAtPlotPosition(PlotPosition ppos)
	{
		if (ppos != null)
		{
			return plotPositions.get(ppos);
		}
		return null;		
	}
	
	public PlotPosition blockToPlotPosition(double bX, double bZ)
	{
		int size = PlotSize + PathWidth;
			
		double n3;
		int mod2 = 0;
		int mod1 = 1;
			
		int x = (int) Math.ceil((double)bX / size);
		int z = (int) Math.ceil((double)bZ / size);
			
		//int x2 = (int) Math.ceil((double)bX / size);
		//int z2 = (int) Math.ceil((double)bZ / size);
			
		if (PathWidth % 2 == 1)
		{
			n3 = Math.ceil(((double)PathWidth)/2); //3 7
			mod2 = -1;
		}
		else
		{
			n3 = Math.floor(((double)PathWidth)/2); //3 7
		}
						
		for (double i = n3; i >= 0; i--)
		{
			if ((bX - i + mod1) % size == 0 ||
			    (bX + i + mod2) % size == 0)
			{
				x = (int) Math.ceil((double)(bX - n3) / size);
				//x2 = (int) Math.ceil((double)(bX + n3) / size);
			}
			if ((bZ - i + mod1) % size == 0 ||
				(bZ + i + mod2) % size == 0)
			{
				z = (int) Math.ceil((double)(bZ - n3) / size);
				//z2 = (int) Math.ceil((double)(bZ + n3) / size);
			}
		}
		
		return new PlotPosition(this, x, z);
	}
	
	public Plot getPlotAtBlockPosition(double bX, double bZ)
	{
			
		int size = PlotSize + PathWidth;
		boolean road = false;
			
		double n3;
		int mod2 = 0;
		int mod1 = 1;
			
		int x = (int) Math.ceil((double)bX / size);
		int z = (int) Math.ceil((double)bZ / size);
			
		//int x2 = (int) Math.ceil((double)bX / size);
		//int z2 = (int) Math.ceil((double)bZ / size);
			
		if (PathWidth % 2 == 1)
		{
			n3 = Math.ceil(((double)PathWidth)/2); //3 7
			mod2 = -1;
		}
		else
		{
			n3 = Math.floor(((double)PathWidth)/2); //3 7
		}
						
		for (double i = n3; i >= 0; i--)
		{
			if ((bX - i + mod1) % size == 0 ||
			    (bX + i + mod2) % size == 0)
			{
				road = true;
				x = (int) Math.ceil((double)(bX - n3) / size);
				//x2 = (int) Math.ceil((double)(bX + n3) / size);
			}
			if ((bZ - i + mod1) % size == 0 ||
				(bZ + i + mod2) % size == 0)
			{
				road = true;
				z = (int) Math.ceil((double)(bZ - n3) / size);
				//z2 = (int) Math.ceil((double)(bZ + n3) / size);
			}
		}
			
		if (road)
		{
			/*if(AutoLinkPlots)
			{
				Plot pt1 = getPlotAtPlotPosition(x,  z);
				Plot pt2 = getPlotAtPlotPosition(x2, z2);
				Plot pt3 = getPlotAtPlotPosition(x,  z2);
				Plot pt4 = getPlotAtPlotPosition(x2, z);
				
				if (pt1 == null || pt2 == null || pt3 == null || pt4 == null || 
					!pt1.owner.equalsIgnoreCase(pt2.owner) ||
					!pt2.owner.equalsIgnoreCase(pt3.owner) ||
					!pt3.owner.equalsIgnoreCase(pt4.owner))
				{						
					return null;
				}
				else
				{
					return id1;
				}
			}*/
		}
		else
		{
			return getPlotAtPlotPosition(x, z);
		}
		
		// Fallback
		return null;
		
	}
	
	public Plot getPlotAtBlockPosition(Location loc)
	{
		if (loc.getWorld() == MinecraftWorld)
		{
			return getPlotAtBlockPosition(loc.getBlockX(), loc.getBlockZ());
		}
		
		return null;
	}
	
	public Plot getPlotAtBlockPosition(Block b)
	{
		return getPlotAtBlockPosition(b.getLocation());
	}
	
	@Override
	public int hashCode()
	{
		return MinecraftWorld.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
	    if (o == null) {
	    	return false;
	    }
	    if (!(o instanceof PlotWorld)) {
	    	return false;
	    }
		return this.MinecraftWorld.equals(((PlotWorld)o).MinecraftWorld.getName());
	}
	
}
