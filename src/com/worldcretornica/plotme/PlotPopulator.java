package com.worldcretornica.plotme;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;

import com.worldcretornica.plotme.utils.Jakky89ItemIdData;


public class PlotPopulator extends BlockPopulator {

	private double plotsize;
	private double pathsize;
	private int roadheight;
	private Jakky89ItemIdData bottom;
	private Jakky89ItemIdData wall;
	private Jakky89ItemIdData plotfloor;
	private Jakky89ItemIdData filling;
	private Jakky89ItemIdData floor1;
	private Jakky89ItemIdData floor2;

	
	public void setDefaults()
	{
		this.plotsize	= PlotMe.DEFAULT_PLOT_SIZE;
		this.pathsize	= PlotMe.DEFAULT_PATH_WIDTH;
		this.roadheight	= PlotMe.DEFAULT_ROAD_HEIGHT;
		
		this.bottom		= PlotMe.DEFAULT_BOTTOM_BLOCK;
		this.wall		= PlotMe.DEFAULT_WALL_BLOCK;
		this.plotfloor	= PlotMe.DEFAULT_FLOOR_BLOCK;
		this.filling		= PlotMe.DEFAULT_FILL_BLOCK;
		this.floor1		= PlotMe.DEFAULT_FLOOR_1_BLOCK;
		this.floor2		= PlotMe.DEFAULT_FLOOR_2_BLOCK;
	}
	
	public PlotPopulator()
	{
		this.setDefaults();
	}
	
	public PlotPopulator(PlotWorld pwi)
	{
		if (pwi != null)
		{
			this.plotsize	= pwi.PlotSize;
			this.pathsize	= pwi.PathWidth;
			this.roadheight	= pwi.RoadHeight;
		
			this.bottom		= pwi.BottomBlock;
			this.wall		= pwi.WallBlock;
			this.plotfloor	= pwi.PlotFloorBlock;
			this.filling	= pwi.PlotFillingBlock;
			this.floor2		= pwi.RoadMainBlock;
			this.floor1		= pwi.RoadStripeBlock;
		}
		else
		{
			PlotMe.logger.warning(PlotMe.PREFIX + "No PlotWorld given for world generation! Using defaults.");
			this.setDefaults();
		}
	}
	
	public void populate(World world, Random random, Chunk chunk) 
	{
		if (world==null || chunk==null)
		{
			return;
		}
		
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
            for (int z = 0; z < 16; z++) 
            {
                int height = this.roadheight + 2;

                world.setBiome(x + xx, z + zz, Biome.PLAINS);

                valx = (cx * 16 + x);
        		valz = (cz * 16 + z);
                
                for (int y = 0; y < height; y++)
                {
                	if (y == 0)
                	{
                		//result[(x * 16 + z) * 128 + y] = bottom;
                		this.setBlock(world, x + xx, y, z + zz, this.bottom.getTypeId(), this.bottom.getDataValue());
                		
                	}
            		else if (y == this.roadheight)
                	{
                		if ((valx - n3 + mod1) % size == 0 || (valx + n3 + mod2) % size == 0) //middle+3
                		{
                			boolean found = false;
                			for(double i = n2; i >= 0; i--)
                			{
	                			if ((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0)
	                			{
	                				found = true;
	                				break;
	                			}	                			
                			}

                			if (found)
                			{
                				//result[(x * 16 + z) * 128 + y] = floor1; //floor1
                				this.setBlock(world, x + xx, y, z + zz, this.floor1.getTypeId(), this.floor1.getDataValue());
                			}
                			else
                			{
                				//result[(x * 16 + z) * 128 + y] = filling; //filling
                				this.setBlock(world, x + xx, y, z + zz, this.filling.getTypeId(), this.filling.getDataValue());
                			}
                		}
                		else if ((valx - n2 + mod1) % size == 0 || (valx + n2 + mod2) % size == 0) //middle+2
                		{
                			if ((valz - n3 + mod1) % size == 0 || (valz + n3 + mod2) % size == 0
                					|| (valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0)
                			{
                				//result[(x * 16 + z) * 128 + y] = floor1; //floor1
                				this.setBlock(world, x + xx, y, z + zz, this.floor1.getTypeId(), this.floor1.getDataValue());
                			}
                			else
                			{
                				//result[(x * 16 + z) * 128 + y] = floor2; //floor2
                				this.setBlock(world, x + xx, y, z + zz, this.floor2.getTypeId(), this.floor2.getDataValue());
                			}
                		}
                		else if ((valx - n1 + mod1) % size == 0 || (valx + n1 + mod2) % size == 0) //middle+2
                		{
                			if ((valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0 
                				|| (valz - n1 + mod1) % size == 0 || (valz + n1 + mod2) % size == 0)
                			{
                				//result[(x * 16 + z) * 128 + y] = floor2; //floor2
                				this.setBlock(world, x + xx, y, z + zz, this.floor2.getTypeId(), this.floor2.getDataValue());
                			}
                			else
                			{
                				//result[(x * 16 + z) * 128 + y] = floor1; //floor1
                				this.setBlock(world, x + xx, y, z + zz, this.floor1.getTypeId(), this.floor1.getDataValue());
                			}
                		}
                		else
                		{
                			boolean found = false;
                			for(double i = n1; i >= 0; i--)
                			{
	                			if ((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0)
	                			{
	                				found = true;
	                				break;
	                			}	                			
                			}

                			if (found)
                			{
	                			//result[(x * 16 + z) * 128 + y] = floor1; //floor1
                				this.setBlock(world, x + xx, y, z + zz, this.floor1.getTypeId(), this.floor1.getDataValue());
                			}
                			else
                			{
	                			if ((valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0)
	                			{
	                				//result[(x * 16 + z) * 128 + y] = floor2; //floor2
	                				this.setBlock(world, x + xx, y, z + zz, this.floor2.getTypeId(), this.floor2.getDataValue());
	                			}
	                			else
	                			{
	                				boolean found2 = false;
	                    			for(double i = n1; i >= 0; i--)
	                    			{
	    	                			if ((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0)
	    	                			{
	    	                				found2 = true;
	    	                				break;
	    	                			}	                			
	                    			}
	                				
	                				if (found2)
	                				{
		                				//result[(x * 16 + z) * 128 + y] = floor1; //floor1
	                					this.setBlock(world, x + xx, y, z + zz, this.floor1.getTypeId(), this.floor1.getDataValue());
	                				}
		                			else
		                			{
		                				boolean found3 = false;
		                    			for(double i = n3; i >= 0; i--)
		                    			{
		    	                			if ((valx - i + mod1) % size == 0 || (valx + i + mod2) % size == 0)
		    	                			{
		    	                				found3 = true;
		    	                				break;
		    	                			}	                			
		                    			}
		                				
		                				if (found3)
		                				{
		                					//result[(x * 16 + z) * 128 + y] = floor1; //floor1
		                					this.setBlock(world, x + xx, y, z + zz, this.floor1.getTypeId(), this.floor1.getDataValue());
		                				}
		                				else
		                				{
		                					//result[(x * 16 + z) * 128 + y] = plotfloor; //plotfloor
		                					this.setBlock(world, x + xx, y, z + zz, this.plotfloor.getTypeId(), this.plotfloor.getDataValue());
		                				}
		                			}
	                			}
	                		}
                		}
                	}
            		else if (y == (this.roadheight + 1))
                	{
                		
                		if ((valx - n3 + mod1) % size == 0 || (valx + n3 + mod2) % size == 0) //middle+3
                		{
                			boolean found = false;
                			for(double i = n2; i >= 0; i--)
                			{
	                			if ((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0)
	                			{
	                				found = true;
	                				break;
	                			}	                			
                			}
                			
                			if (found)
                			{
                				//result[(x * 16 + z) * 128 + y] = air;
                				//setBlock(result, x, y, z, air);
                			}
                			else
                			{
                				//result[(x * 16 + z) * 128 + y] = wall;
                				this.setBlock(world, x + xx, y, z + zz, this.wall.getTypeId(), this.wall.getDataValue());
                			}
                		}
                		else
                		{
                			boolean found = false;
                			for(double i = n2; i >= 0; i--)
                			{
	                			if ((valx - i + mod1) % size == 0 || (valx + i + mod2) % size == 0)
	                			{
	                				found = true;
	                				break;
	                			}	                			
                			}
                			
                			if (found)
                			{
                				//result[(x * 16 + z) * 128 + y] = air;
                				//setBlock(result, x, y, z, air);
                			}else{
                				if ((valz - n3 + mod1) % size == 0 || (valz + n3 + mod2) % size == 0)
                				{
	                				//result[(x * 16 + z) * 128 + y] = wall;
                					this.setBlock(world, x + xx, y, z + zz, this.wall.getTypeId(), this.wall.getDataValue());
                				}
	                			else
	                			{
	                				//result[(x * 16 + z) * 128 + y] = air;
	                				//setBlock(result, x, y, z, air);
	                			}
                			}
                		}
                	}else{
                		//result[(x * 16 + z) * 128 + y] = filling;
                		this.setBlock(world, x + xx, y, z + zz, this.filling.getTypeId(), this.filling.getDataValue());
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
