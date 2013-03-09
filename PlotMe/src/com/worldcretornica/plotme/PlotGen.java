package com.worldcretornica.plotme;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import com.worldcretornica.plotme.utils.Jakky89ItemIdData;
import com.worldcretornica.plotme.utils.Pair;

public class PlotGen extends ChunkGenerator {
	
	private double plotsize;
	private double pathsize;
	private Jakky89ItemIdData bottom;
	private Jakky89ItemIdData wall;
	private Jakky89ItemIdData plotfloor;
	private Jakky89ItemIdData filling;
	private Jakky89ItemIdData floor1;
	private Jakky89ItemIdData floor2;
	private int roadheight;
	
	
	public void setDefaults()
	{
		plotsize	= PlotMe.DEFAULT_PLOT_SIZE;
		pathsize	= PlotMe.DEFAULT_PATH_WIDTH;
		roadheight	= PlotMe.DEFAULT_ROAD_HEIGHT;
		
		bottom		= PlotMe.DEFAULT_BOTTOM_BLOCK;
		wall		= PlotMe.DEFAULT_WALL_BLOCK;
		plotfloor	= PlotMe.DEFAULT_FLOOR_BLOCK;
		
		filling		= PlotMe.DEFAULT_FILL_BLOCK;
		floor1		= PlotMe.DEFAULT_FLOOR_1_BLOCK;
		floor2		= PlotMe.DEFAULT_FLOOR_2_BLOCK;
	}
	
	public PlotGen()
	{
		setDefaults();
		PlotMe.logger.warning(PlotMe.PREFIX + "Unable to find configuration for PlotGen! Using defaults.");
	}
	
	private void fetchPlotWorldProperties(PlotWorld pwi)
	{
		if (pwi == null)
		{
			setDefaults();
			PlotMe.logger.warning(PlotMe.PREFIX + "Unregistered PlotWorld in PlotGen! Using defaults.");
			return;
		}
		
		plotsize	= pwi.PlotSize;
		pathsize	= pwi.PathWidth;
		bottom		= pwi.BottomBlock;
		wall		= pwi.WallBlock;
		plotfloor	= pwi.PlotFloorBlock;
		filling		= pwi.PlotFillingBlock;
		roadheight	= pwi.RoadHeight;
		floor1		= pwi.RoadMainBlock;
		floor2		= pwi.RoadStripeBlock;
	}

	public PlotGen(World w)
	{
		fetchPlotWorldProperties(PlotManager.getPlotWorld(w));
	}

	@Override
	public short[][] generateExtBlockSections(World world, Random random, int cx, int cz, BiomeGrid biomes)
	{
		int maxY = world.getMaxHeight();
		
		short[][] result = new short[maxY / 16][]; 
		
		double size = plotsize + pathsize;
		int valx;
		int valz;

		double n1;
		double n2;
		double n3;

		int mod2 = 0;
		int mod1 = 1;
		
		if (pathsize % 2 == 1)
		{
			n1 = Math.ceil(((double)pathsize)/2) - 2;
			n2 = Math.ceil(((double)pathsize)/2) - 1;
			n3 = Math.ceil(((double)pathsize)/2);
		}
		else
		{
			n1 = Math.floor(((double)pathsize)/2) - 2;
			n2 = Math.floor(((double)pathsize)/2) - 1;
			n3 = Math.floor(((double)pathsize)/2);
		}

		if(pathsize % 2 == 1)
		{
			mod2 = -1;
		}

        for (int x = 0; x < 16; x++)
        {
            for (int z = 0; z < 16; z++)
            {
                int height = roadheight + 2;
                for (int y = 0; y < height; y++)
                {
                	valx = (cx * 16 + x);
            		valz = (cz * 16 + z);
                	
            		if (y == 0)
                	{
                		//result[(x * 16 + z) * 128 + y] = bottom;
            			setBlock(result, x, y, z, bottom);
                		
                	}
            		else if(y == roadheight)
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
                				setBlock(result, x, y, z, floor1);
                			}
                			else
                			{
                				//result[(x * 16 + z) * 128 + y] = filling; //filling
                				setBlock(result, x, y, z, filling);
                			}
                		}
                		else if ((valx - n2 + mod1) % size == 0 || (valx + n2 + mod2) % size == 0) //middle+2
                		{
                			if((valz - n3 + mod1) % size == 0 || (valz + n3 + mod2) % size == 0
                					|| (valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0)
                			{
                				//result[(x * 16 + z) * 128 + y] = floor1; //floor1
                				setBlock(result, x, y, z, floor1);
                			}
                			else
                			{
                				//result[(x * 16 + z) * 128 + y] = floor2; //floor2
                				setBlock(result, x, y, z, floor2);
                			}
                		}
                		else if ((valx - n1 + mod1) % size == 0 || (valx + n1 + mod2) % size == 0) //middle+2
                		{
                			if ((valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0 
                			 || (valz - n1 + mod1) % size == 0 || (valz + n1 + mod2) % size == 0)
                			{
                				//result[(x * 16 + z) * 128 + y] = floor2; //floor2
                				setBlock(result, x, y, z, floor2);
                			}
                			else
                			{
                				//result[(x * 16 + z) * 128 + y] = floor1; //floor1
                				setBlock(result, x, y, z, floor1);
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
	                			setBlock(result, x, y, z, floor1);
                			}
                			else
                			{
	                			if ((valz - n2 + mod1) % size == 0
	                			 || (valz + n2 + mod2) % size == 0)
	                			{
	                				//result[(x * 16 + z) * 128 + y] = floor2; //floor2
	                				setBlock(result, x, y, z, floor2);
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
		                				setBlock(result, x, y, z, floor1);
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
		                					setBlock(result, x, y, z, floor1);
		                				}
		                				else
		                				{
		                					//result[(x * 16 + z) * 128 + y] = plotfloor; //plotfloor
		                					setBlock(result, x, y, z, plotfloor);
		                				}
		                			}
	                			}
	                		}
                		}
                	}
            		else if (y == (roadheight + 1))
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
                				setBlock(result, x, y, z, wall);
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
	                				setBlock(result, x, y, z, wall);
                				}
                			}
                		}
                	}
            		else
            		{
                		//result[(x * 16 + z) * 128 + y] = filling;
                		setBlock(result, x, y, z, filling);
                	}
                }
            }
        }
		
		return result;
	}
	
	private void setBlock(short[][] result, int x, int y, int z, short blkid) {
        if (result[y >> 4] == null) {
            result[y >> 4] = new short[4096];
        }
        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
    }
	
	public List<BlockPopulator> getDefaultPopulators(World world)
	{
		if (world == null)
		{
			return Arrays.asList((BlockPopulator)new PlotPopulator());
		}
		else
		{
			return Arrays.asList((BlockPopulator)new PlotPopulator(world));
		}
    }

	public Location getFixedSpawnLocation(World world, Random random)
	{
		return new Location(world, 0, roadheight + 2, 0);
	}
}
