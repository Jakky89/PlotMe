package com.worldcretornica.plotme;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;

import com.worldcretornica.plotme.utils.Jakky89ItemIdData;

public class PlotPopulator extends BlockPopulator {

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
	
	public PlotPopulator()
	{
		setDefaults();
		PlotMe.logger.warning(PlotMe.PREFIX + "Unable to find configuration for PlotPopulator! Using defaults.");
	}
	
	public PlotPopulator(PlotWorld pwi)
	{
		if (pwi == null)
		{
			setDefaults();
			PlotMe.logger.warning(PlotMe.PREFIX + "Unregistered PlotWorld in PlotPopulator! Using defaults.");
			return;
		}
		
		plotsize	= pwi.PlotSize;
		pathsize	= pwi.PathWidth;
		roadheight	= pwi.RoadHeight;
		
		bottom		= pwi.BottomBlock;
		wall		= pwi.WallBlock;
		plotfloor	= pwi.PlotFloorBlock;
		filling		= pwi.PlotFillingBlock;
		floor2		= pwi.RoadMainBlock;
		floor1		= pwi.RoadStripeBlock;
	}
	
	public void populate(World w, Random rand, Chunk chunk) 
	{
		int cx = chunk.getX();
		int cz = chunk.getZ();
		
		int xx = cx << 4;
		int zz = cz << 4;

		double size = plotsize + pathsize;
		double psh  = (double)(pathsize/2);
		
		int valx;
		int valz;

		double n1;
		double n2;
		double n3;

		int mod2 = 0;
		int mod1 = 1;
		
		if (pathsize % 2 == 1)
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
		
		if (pathsize % 2 == 1)
		{
			mod2 = -1;
		}
		
		
        for (int x = 0; x < 16; x++) 
        {
            for (int z = 0; z < 16; z++) 
            {
                int height = roadheight + 2;

                w.setBiome(x + xx, z + zz, Biome.PLAINS);

                valx = (cx * 16 + x);
        		valz = (cz * 16 + z);
                
                for (int y = 0; y < height; y++)
                {
                	if (y == 0)
                	{
                		//result[(x * 16 + z) * 128 + y] = bottom;
            			setBlock(w, x + xx, y, z + zz, bottom.getTypeId(), bottom.getDataValue());
                		
                	}
            		else if (y == roadheight)
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
                				setBlock(w, x + xx, y, z + zz, floor1.getTypeId(), floor1.getDataValue());
                			}
                			else
                			{
                				//result[(x * 16 + z) * 128 + y] = filling; //filling
                				setBlock(w, x + xx, y, z + zz, filling.getTypeId(), filling.getDataValue());
                			}
                		}
                		else if ((valx - n2 + mod1) % size == 0 || (valx + n2 + mod2) % size == 0) //middle+2
                		{
                			if ((valz - n3 + mod1) % size == 0 || (valz + n3 + mod2) % size == 0
                					|| (valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0)
                			{
                				//result[(x * 16 + z) * 128 + y] = floor1; //floor1
                				setBlock(w, x + xx, y, z + zz, floor1.getTypeId(), floor1.getDataValue());
                			}
                			else
                			{
                				//result[(x * 16 + z) * 128 + y] = floor2; //floor2
                				setBlock(w, x + xx, y, z + zz, floor2.getTypeId(), floor2.getDataValue());
                			}
                		}
                		else if ((valx - n1 + mod1) % size == 0 || (valx + n1 + mod2) % size == 0) //middle+2
                		{
                			if ((valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0 
                				|| (valz - n1 + mod1) % size == 0 || (valz + n1 + mod2) % size == 0)
                			{
                				//result[(x * 16 + z) * 128 + y] = floor2; //floor2
                				setBlock(w, x + xx, y, z + zz, floor2.getTypeId(), floor2.getDataValue());
                			}
                			else
                			{
                				//result[(x * 16 + z) * 128 + y] = floor1; //floor1
                				setBlock(w, x + xx, y, z + zz, floor1.getTypeId(), floor1.getDataValue());
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
                				setBlock(w, x + xx, y, z + zz, floor1.getTypeId(), floor1.getDataValue());
                			}
                			else
                			{
	                			if ((valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0)
	                			{
	                				//result[(x * 16 + z) * 128 + y] = floor2; //floor2
	                				setBlock(w, x + xx, y, z + zz, floor2.getTypeId(), floor2.getDataValue());
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
	                					setBlock(w, x + xx, y, z + zz, floor1.getTypeId(), floor1.getDataValue());
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
		                					setBlock(w, x + xx, y, z + zz, floor1.getTypeId(), floor1.getDataValue());
		                				}
		                				else
		                				{
		                					//result[(x * 16 + z) * 128 + y] = plotfloor; //plotfloor
		                					setBlock(w, x + xx, y, z + zz, plotfloor.getTypeId(), plotfloor.getDataValue());
		                				}
		                			}
	                			}
	                		}
                		}
                	}
            		else if (y == (roadheight + 1))
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
                				setBlock(w, x + xx, y, z + zz, wall.getTypeId(), wall.getDataValue());
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
                					setBlock(w, x + xx, y, z + zz, wall.getTypeId(), wall.getDataValue());
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
                		setBlock(w, x + xx, y, z + zz, filling.getTypeId(), filling.getDataValue());
                	}
                }
            }
        }
	}

	
	private void setBlock(World w, int x, int y, int z, short typeId, short dataValue)
	{
		if (typeId >= 0)
		{
			w.getBlockAt(x, y, z).setTypeIdAndData(typeId, (byte)dataValue, false);
		}
	}
	
}
