package com.worldcretornica.plotme;


import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

import com.worldcretornica.plotme.utils.Jakky89ItemIdData;


public class PlotRoadPopulator extends BlockPopulator
{
	private double plotsize;
	private double pathsize;

	private Jakky89ItemIdData wall;
	private Jakky89ItemIdData floor1;
	private Jakky89ItemIdData floor2;
	private Jakky89ItemIdData pillar;
	private Jakky89ItemIdData pillarh1;
	private Jakky89ItemIdData pillarh2;
	
	private int roadheight;
	
	
	public void setDefaults()
	{
		this.plotsize	= PlotMe.DEFAULT_PLOT_SIZE;
		this.pathsize	= PlotMe.DEFAULT_PATH_WIDTH;
		this.roadheight	= PlotMe.DEFAULT_ROAD_HEIGHT;
		
		this.wall 		= PlotMe.DEFAULT_WALL_BLOCK;
		this.floor1		= PlotMe.DEFAULT_FLOOR_1_BLOCK;
		this.floor2		= PlotMe.DEFAULT_FLOOR_2_BLOCK;
		
		this.pillar		= PlotMe.DEFAULT_PILLAR_BLOCK;
		this.pillarh1	= PlotMe.DEFAULT_PILLAR_H1_BLOCK;
		this.pillarh2	= PlotMe.DEFAULT_PILLAR_H2_BLOCK;
	}
	
	public PlotRoadPopulator()
	{
		this.setDefaults();
	}
	
	public PlotRoadPopulator(PlotWorld pwi)
	{
		if (pwi != null)
		{
			this.plotsize	= pwi.PlotSize;
			this.pathsize	= pwi.PathWidth;
			this.roadheight	= pwi.RoadHeight;
		
			this.wall		= pwi.WallBlock;
			this.floor1		= pwi.RoadStripeBlock;
			this.floor2		= pwi.RoadMainBlock;
		
			this.pillar		= PlotMe.DEFAULT_PILLAR_BLOCK;
			this.pillarh1	= PlotMe.DEFAULT_PILLAR_H1_BLOCK;
			this.pillarh2	= PlotMe.DEFAULT_PILLAR_H2_BLOCK;
		}
		else
		{
			PlotMe.logger.warning(PlotMe.PREFIX + "Unregistered PlotWorld in PlotRoadPopulator! Using defaults.");
			this.setDefaults();
		}
	}

	public void populate(World world, Random random, Chunk chunk) 
	{
		int cx = chunk.getX();
		int cz = chunk.getZ();
		
		int xx = cx << 4;
		int zz = cz << 4;
		
		double size = this.plotsize + this.pathsize;
		double psh  = (double)(this.pathsize/2);
		int valx;
		int valz;
		
		double n1;
		double n2;
		double n3;
		int mod2 = 0;
		int mod1 = 1;
		
		if (this.pathsize % 2 == 1)
		{
			n3 = Math.ceil(psh);
			n1 = n3 - 2;
			n2 = n3 - 1;
		}
		else
		{
			n3 = Math.floor(psh);
			n1 = n3 - 2;
			n2 = n3 - 1;
		}
		
		if (this.pathsize % 2 == 1)
		{
			mod2 = -1;
		}
		
		for (int x = 0; x < 16; x++) 
        {
			valx = (cx * 16 + x);
			
            for (int z = 0; z < 16; z++) 
            {
            	valz = (cz * 16 + z);
                
        		int y = this.roadheight;
        		            		
        		if ((valx - n3 + mod1) % size == 0 || (valx + n3 + mod2) % size == 0) //middle+3
        		{            			
        			boolean found = false;
        			for(double i = n2; i >= 0; i--)
        			{
            			if((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0)
            			{
            				found = true;
            				break;
            			}	                			
        			}

        			if (found)
        			{
        				this.setBlock(world, x + xx, y - 1, z + zz, this.pillar.getTypeId(), this.pillar.getDataValue());
        				this.setBlock(world, x + xx, y,     z + zz, this.floor1.getTypeId(), this.floor1.getDataValue());
        				this.setBlock(world, x + xx, y + 1, z + zz, (short)0, (short)0); // AIR
        				this.setBlock(world, x + xx, y + 2, z + zz, (short)0, (short)0); // AIR
        			}
        			else
        			{
        				this.setBlock(world, x + xx, y,   z + zz, this.pillarh2.getTypeId(), this.pillarh2.getDataValue());
        				this.setBlock(world, x + xx, y+1, z + zz, this.wall.getTypeId(),     this.wall.getDataValue());
        			}
        		}
        		else
        		{
        			boolean found5 = false;
        			for(double i = n2; i >= 0; i--)
        			{
            			if((valx - i + mod1) % size == 0 || (valx + i + mod2) % size == 0)
            			{
            				found5 = true;
            				break;
            			}	                			
        			}
        			
        			if (!found5)
        			{
        				if ((valz - n3 + mod1) % size == 0 || (valz + n3 + mod2) % size == 0)
        				{
        					this.setBlock(world, x + xx, y,   z + zz, this.pillarh1.getTypeId(), this.pillarh1.getDataValue());
        					this.setBlock(world, x + xx, y+1, z + zz, this.wall.getTypeId(),     this.wall.getDataValue());
        				}
        			}
        			
        			
        			if ((valx - n2 + mod1) % size == 0 || (valx + n2 + mod2) % size == 0) //middle+2
	        		{
	        			if((valz - n3 + mod1) % size == 0 || (valz + n3 + mod2) % size == 0
	        					|| (valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0)
	        			{
	        				this.setBlock(world, x + xx, y - 1, z + zz, this.pillar.getTypeId(), this.pillar.getDataValue());
	        				this.setBlock(world, x + xx, y,     z + zz, this.floor1.getTypeId(), this.floor1.getDataValue());
	        				this.setBlock(world, x + xx, y + 1, z + zz, (short)0, (short)0); // AIR
	        				this.setBlock(world, x + xx, y + 2, z + zz, (short)0, (short)0); // AIR
	        			}
	        			else
	        			{
	        				this.setBlock(world, x + xx, y - 1, z + zz, this.pillar.getTypeId(), this.pillar.getDataValue());
	        				this.setBlock(world, x + xx, y,     z + zz, this.floor2.getTypeId(), this.floor2.getDataValue());
	        				this.setBlock(world, x + xx, y + 1, z + zz, (short)0, (short)0); // AIR
	        				this.setBlock(world, x + xx, y + 2, z + zz, (short)0, (short)0); // AIR
	        			}
	        		}
        			else if ((valx - n1 + mod1) % size == 0 || (valx + n1 + mod2) % size == 0) //middle+2
	        		{
	        			if((valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0 
	        				|| (valz - n1 + mod1) % size == 0 || (valz + n1 + mod2) % size == 0)
	        			{
	        				this.setBlock(world, x + xx, y - 1, z + zz, this.pillar.getTypeId(), this.pillar.getDataValue());
	        				this.setBlock(world, x + xx, y,     z + zz, this.floor2.getTypeId(), this.floor2.getDataValue());
	        				this.setBlock(world, x + xx, y + 1, z + zz, (short)0, (short)0); // AIR
	        				this.setBlock(world, x + xx, y + 2, z + zz, (short)0, (short)0); // AIR
	        			}
	        			else
	        			{
	        				this.setBlock(world, x + xx, y - 1, z + zz, this.pillar.getTypeId(), this.pillar.getDataValue());
	        				this.setBlock(world, x + xx, y,     z + zz, this.floor1.getTypeId(), this.floor1.getDataValue());
	        				this.setBlock(world, x + xx, y + 1, z + zz, (short)0, (short)0); // AIR
	        				this.setBlock(world, x + xx, y + 2, z + zz, (short)0, (short)0); // AIR
	        			}
	        		}
	        		else
	        		{
	        			boolean found = false;
	        			for(double i = n1; i >= 0; i--)
	        			{
	            			if((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0)
	            			{
	            				found = true;
	            				break;
	            			}	                			
	        			}
	
	        			if (found)
	        			{
	        				this.setBlock(world, x + xx, y - 1, z + zz, this.pillar.getTypeId(), this.pillar.getDataValue());
	        				this.setBlock(world, x + xx, y,     z + zz, this.floor1.getTypeId(), this.floor1.getDataValue());
	        				this.setBlock(world, x + xx, y + 1, z + zz, (short)0, (short)0); // AIR
	        				this.setBlock(world, x + xx, y + 2, z + zz, (short)0, (short)0); // AIR
	        			} else {
	            			if ((valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0)
	            			{
	            				this.setBlock(world, x + xx, y - 1, z + zz, this.pillar.getTypeId(), this.pillar.getDataValue());
	            				this.setBlock(world, x + xx, y,     z + zz, this.floor2.getTypeId(), this.floor2.getDataValue());
	            				this.setBlock(world, x + xx, y + 1, z + zz, (short)0, (short)0); // AIR
	            				this.setBlock(world, x + xx, y + 2, z + zz, (short)0, (short)0); // AIR
	            			}
	            			else
	            			{
	            				boolean found2 = false;
	                			for (double i = n1; i >= 0; i--)
	                			{
		                			if((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0)
		                			{
		                				found2 = true;
		                				break;
		                			}	                			
	                			}
	            				if (found2)
	            				{
	            					this.setBlock(world, x + xx, y - 1, z + zz, this.pillar.getTypeId(), this.pillar.getDataValue());
	            					this.setBlock(world, x + xx, y,     z + zz, this.floor1.getTypeId(), this.floor1.getDataValue());
	            					this.setBlock(world, x + xx, y + 1, z + zz, (short)0, (short)0); // AIR
	            					this.setBlock(world, x + xx, y + 2, z + zz, (short)0, (short)0); // AIR
	            				}
	                			else
	                			{
	                				boolean found3 = false;
	                    			for (double i = n3; i >= 0; i--)
	                    			{
	    	                			if ((valx - i + mod1) % size == 0 || (valx + i + mod2) % size == 0)
	    	                			{
	    	                				found3 = true;
	    	                				break;
	    	                			}	                			
	                    			}
	                				if (found3)
	                				{
	                					this.setBlock(world, x + xx, y - 1, z + zz,		this.pillar.getTypeId(),   this.pillar.getDataValue());
	                					this.setBlock(world, x + xx, y,     z + zz,		this.floor1.getTypeId(),   this.floor1.getDataValue());
	                					this.setBlock(world, x + xx, y + 1, z + zz,		(short)0, (short)0);  // AIR
	                					this.setBlock(world, x + xx, y + 2, z + zz,		(short)0, (short)0);  // AIR
	                				}
	                			}
	            			}
	            		}
	        		}
        		}
            }
        }
	}

	private void setBlock(World world, int x, int y, int z, short typeId, short dataValue)
	{
		if (world==null)
		{
			return;
		}
		world.getBlockAt(x, y, z).setTypeIdAndData(typeId, (byte)dataValue, false);
	}

}
