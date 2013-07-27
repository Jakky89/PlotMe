package com.worldcretornica.plotme;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import com.worldcretornica.plotme.utils.Jakky89ItemIdData;

public class PlotGen extends ChunkGenerator {
	
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
		
		this.filling	= PlotMe.DEFAULT_FILL_BLOCK;
		this.floor1		= PlotMe.DEFAULT_FLOOR_1_BLOCK;
		this.floor2		= PlotMe.DEFAULT_FLOOR_2_BLOCK;
	}
	
	public PlotGen()
	{
		PlotMe.logger.warning(PlotMe.PREFIX + "No PlotWorld for PlotGen! Using defaults.");
		this.setDefaults();
	}
	
	public PlotGen(PlotWorld pwi)
	{
		if (pwi != null)
		{
			PlotMe.logger.warning(PlotMe.PREFIX + "Using PlotGen for world \"" + pwi.getName() + "\"");
			
			this.plotsize	= pwi.PlotSize;
			this.pathsize	= pwi.PathWidth;
			this.bottom		= pwi.BottomBlock;
			this.wall		= pwi.WallBlock;
			this.plotfloor	= pwi.PlotFloorBlock;
			this.filling	= pwi.PlotFillingBlock;
			this.roadheight	= pwi.RoadHeight;
			this.floor1		= pwi.RoadMainBlock;
			this.floor2		= pwi.RoadStripeBlock;
		}
		else
		{
			PlotMe.logger.warning(PlotMe.PREFIX + "No PlotWorld for PlotGen! Using defaults.");
			this.setDefaults();
		}
	}

	public short[][] generateExtBlockSections(World world, Random random, int cx, int cz, BiomeGrid biomes)
	{
		int maxY = world.getMaxHeight();
		
		short[][] result = new short[maxY / 16][]; 
		
		double size = this.plotsize + this.pathsize;
		int valx;
		int valz;

		double n1;
		double n2;
		double n3;

		int mod2 = 0;
		int mod1 = 1;
		
		if (this.pathsize % 2 == 1)
		{
			n3 = Math.ceil(((double)this.pathsize)/2);
			n2 = n3 - 1;
			n1 = n3 - 2;
		}
		else
		{
			n3 = Math.floor(((double)this.pathsize)/2);
			n2 = n3 - 1;
			n1 = n3 - 2;
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
                for (int y = 0; y < height; y++)
                {
                	valx = (cx * 16 + x);
            		valz = (cz * 16 + z);
                	
            		if (y == 0)
                	{
                		//result[(x * 16 + z) * 128 + y] = bottom;
            			this.setBlock(result, x, y, z, this.bottom);
                		
                	}
            		else if (y == this.roadheight)
                	{
                		if ((valx - n3 + mod1) % size == 0 || (valx + n3 + mod2) % size == 0) //middle+3
                		{
                			boolean found = false;
                			for (double i = n2; i >= 0; i--)
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
                				this.setBlock(result, x, y, z, this.floor1);
                			}
                			else
                			{
                				//result[(x * 16 + z) * 128 + y] = filling; //filling
                				this.setBlock(result, x, y, z, this.filling);
                			}
                		}
                		else if ((valx - n2 + mod1) % size == 0 || (valx + n2 + mod2) % size == 0) //middle+2
                		{
                			if ((valz - n3 + mod1) % size == 0 || (valz + n3 + mod2) % size == 0
                					|| (valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0)
                			{
                				//result[(x * 16 + z) * 128 + y] = floor1; //floor1
                				this.setBlock(result, x, y, z, this.floor1);
                			}
                			else
                			{
                				//result[(x * 16 + z) * 128 + y] = floor2; //floor2
                				this.setBlock(result, x, y, z, this.floor2);
                			}
                		}
                		else if ((valx - n1 + mod1) % size == 0 || (valx + n1 + mod2) % size == 0) //middle+2
                		{
                			if ((valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0 
                			 || (valz - n1 + mod1) % size == 0 || (valz + n1 + mod2) % size == 0)
                			{
                				//result[(x * 16 + z) * 128 + y] = floor2; //floor2
                				this.setBlock(result, x, y, z, this.floor2);
                			}
                			else
                			{
                				//result[(x * 16 + z) * 128 + y] = floor1; //floor1
                				this.setBlock(result, x, y, z, this.floor1);
                			}
                		}
                		else
                		{
                			boolean found = false;
                			for (double i = n1; i >= 0; i--)
                			{
	                			if ((valz - i + mod1) % size == 0
	                			 || (valz + i + mod2) % size == 0)
	                			{
	                				found = true;
	                				break;
	                			}	                			
                			}

                			if (found)
                			{
	                			//result[(x * 16 + z) * 128 + y] = floor1; //floor1
                				this.setBlock(result, x, y, z, this.floor1);
                			}
                			else
                			{
	                			if ((valz - n2 + mod1) % size == 0
	                			 || (valz + n2 + mod2) % size == 0)
	                			{
	                				//result[(x * 16 + z) * 128 + y] = floor2; //floor2
	                				this.setBlock(result, x, y, z, this.floor2);
	                			}
	                			else
	                			{
	                				boolean found2 = false;
	                    			for (double i = n1; i >= 0; i--)
	                    			{
	    	                			if ((valz - i + mod1) % size == 0
	    	                			 || (valz + i + mod2) % size == 0)
	    	                			{
	    	                				found2 = true;
	    	                				break;
	    	                			}	                			
	                    			}
	                				
	                				if (found2)
	                				{
		                				//result[(x * 16 + z) * 128 + y] = floor1; //floor1
	                					this.setBlock(result, x, y, z, this.floor1);
	                				}
		                			else
		                			{
		                				boolean found3 = false;
		                    			for (double i = n3; i >= 0; i--)
		                    			{
		    	                			if ((valx - i + mod1) % size == 0
		    	                			 || (valx + i + mod2) % size == 0)
		    	                			{
		    	                				found3 = true;
		    	                				break;
		    	                			}	                			
		                    			}
		                				
		                				if (found3)
		                				{
		                					//result[(x * 16 + z) * 128 + y] = floor1; //floor1
		                					this.setBlock(result, x, y, z, this.floor1);
		                				}
		                				else
		                				{
		                					//result[(x * 16 + z) * 128 + y] = plotfloor; //plotfloor
		                					this.setBlock(result, x, y, z, this.plotfloor);
		                				}
		                			}
	                			}
	                		}
                		}
                	}
            		else if (y == (this.roadheight + 1))
                	{
                		
                		if ((valx - n3 + mod1) % size == 0
                		 || (valx + n3 + mod2) % size == 0) //middle+3
                		{
                			boolean found = false;
                			for (double i = n2; i >= 0; i--)
                			{
	                			if ((valz - i + mod1) % size == 0
	                			 || (valz + i + mod2) % size == 0)
	                			{
	                				found = true;
	                				break;
	                			}	                			
                			}
                			
                			if (!found)
                			{
                				//result[(x * 16 + z) * 128 + y] = wall;
                				this.setBlock(result, x, y, z, this.wall);
                			}
                		}
                		else
                		{
                			boolean found = false;
                			for (double i = n2; i >= 0; i--)
                			{
	                			if ((valx - i + mod1) % size == 0
	                			 || (valx + i + mod2) % size == 0)
	                			{
	                				found = true;
	                				break;
	                			}	                			
                			}
                			
                			if (!found)
                			{
                				if ((valz - n3 + mod1) % size == 0
                				 || (valz + n3 + mod2) % size == 0)
                				{
	                				//result[(x * 16 + z) * 128 + y] = wall;
                					this.setBlock(result, x, y, z, this.wall);
                				}
                			}
                		}
                	}
            		else
            		{
                		//result[(x * 16 + z) * 128 + y] = filling;
                		this.setBlock(result, x, y, z, this.filling);
                	}
                }
            }
        }
		
		return result;
	}
	
	private void setBlock(short[][] result, int x, int y, int z, Jakky89ItemIdData blk)
	{
        if (result[y >> 4] == null) {
            result[y >> 4] = new short[4096];
        }
        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blk.getTypeId();
    }

	public List<BlockPopulator> getDefaultPopulators(World world)
	{
		if (world==null)
		{
			return Arrays.asList((BlockPopulator)new PlotPopulator());
		}
		else
		{
			return Arrays.asList((BlockPopulator)new PlotPopulator(PlotManager.getPlotWorld(world.getName())));
		}
    }

	public Location getFixedSpawnLocation(World world, Random random)
	{
		if (world == null)
		{
			return null;
		}
		int sx = 0;
		int sy = this.roadheight;
		int sz = 0;
		PlotWorld pwi = PlotManager.getPlotWorld(world.getName());
		if (pwi!=null)
		{
			sx = pwi.spawnX;
			sy = pwi.spawnY;
			sz = pwi.spawnZ;
		}
		return new Location(world, sx, sy, sz);
	}
}
