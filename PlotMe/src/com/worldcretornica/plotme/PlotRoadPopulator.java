package com.worldcretornica.plotme;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

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
		plotsize	= PlotMe.DEFAULT_PLOT_SIZE;
		pathsize	= PlotMe.DEFAULT_PATH_WIDTH;
		roadheight	= PlotMe.DEFAULT_ROAD_HEIGHT;
		
		wall 		= PlotMe.DEFAULT_WALL_BLOCK;
		floor1		= PlotMe.DEFAULT_FLOOR_1_BLOCK;
		floor2		= PlotMe.DEFAULT_FLOOR_2_BLOCK;
		
		pillar		= PlotMe.DEFAULT_PILLAR_BLOCK;
		pillarh1	= PlotMe.DEFAULT_PILLAR_H1_BLOCK;
		pillarh2	= PlotMe.DEFAULT_PILLAR_H2_BLOCK;
	}
	
	public PlotRoadPopulator()
	{
		setDefaults();
		PlotMe.logger.warning(PlotMe.PREFIX + "Unable to find configuration for PlotRoadPopulator! Using defaults.");
	}
	
	public PlotRoadPopulator(PlotWorld pwi)
	{
		if (pwi == null)
		{
			setDefaults();
			PlotMe.logger.warning(PlotMe.PREFIX + "Unregistered PlotWorld in PlotRoadPopulator! Using defaults.");
			return;
		}
		
		plotsize	= pwi.PlotSize;
		pathsize	= pwi.PathWidth;
		roadheight	= pwi.RoadHeight;
		
		wall		= pwi.WallBlock;
		floor1		= pwi.RoadStripeBlock;
		floor2		= pwi.RoadMainBlock;
		
		pillar		= PlotMe.DEFAULT_PILLAR_BLOCK;
		pillarh1	= PlotMe.DEFAULT_PILLAR_H1_BLOCK;
		pillarh2	= PlotMe.DEFAULT_PILLAR_H2_BLOCK;
	}

	@Override
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
			valx = (cx * 16 + x);
			
            for (int z = 0; z < 16; z++) 
            {
            	valz = (cz * 16 + z);
                
        		int y = roadheight;
        		            		
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
        				setBlock(w, x + xx, y - 1, z + zz, pillar.getTypeId(), pillar.getDataValue());
        				setBlock(w, x + xx, y,     z + zz, floor1.getTypeId(), floor1.getDataValue());
        				setBlock(w, x + xx, y + 1, z + zz, (short)0, (short)0); // AIR
        				setBlock(w, x + xx, y + 2, z + zz, (short)0, (short)0); // AIR
        			}
        			else
        			{
        				setBlock(w, x + xx, y,   z + zz, pillarh2.getTypeId(), pillarh2.getDataValue());
    					setBlock(w, x + xx, y+1, z + zz, wall.getTypeId(),     wall.getDataValue());
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
        			
        			if(!found5)
        			{
        				if((valz - n3 + mod1) % size == 0 || (valz + n3 + mod2) % size == 0)
        				{
        					setBlock(w, x + xx, y,   z + zz, pillarh1.getTypeId(), pillarh1.getDataValue());
        					setBlock(w, x + xx, y+1, z + zz, wall.getTypeId(),     wall.getDataValue());
        				}
        			}
        			
        			
        			if ((valx - n2 + mod1) % size == 0 || (valx + n2 + mod2) % size == 0) //middle+2
	        		{
	        			if((valz - n3 + mod1) % size == 0 || (valz + n3 + mod2) % size == 0
	        					|| (valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0)
	        			{
	        				setBlock(w, x + xx, y - 1, z + zz, pillar.getTypeId(), pillar.getDataValue());
	        				setBlock(w, x + xx, y,     z + zz, floor1.getTypeId(), floor1.getDataValue());
	        				setBlock(w, x + xx, y + 1, z + zz, (short)0, (short)0); // AIR
	        				setBlock(w, x + xx, y + 2, z + zz, (short)0, (short)0); // AIR
	        			}
	        			else
	        			{
	        				setBlock(w, x + xx, y - 1, z + zz, pillar.getTypeId(), pillar.getDataValue());
	        				setBlock(w, x + xx, y,     z + zz, floor2.getTypeId(), floor2.getDataValue());
	        				setBlock(w, x + xx, y + 1, z + zz, (short)0, (short)0); // AIR
	        				setBlock(w, x + xx, y + 2, z + zz, (short)0, (short)0); // AIR
	        			}
	        		}
        			else if ((valx - n1 + mod1) % size == 0 || (valx + n1 + mod2) % size == 0) //middle+2
	        		{
	        			if((valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0 
	        				|| (valz - n1 + mod1) % size == 0 || (valz + n1 + mod2) % size == 0)
	        			{
	        				setBlock(w, x + xx, y - 1, z + zz, pillar.getTypeId(), pillar.getDataValue());
	        				setBlock(w, x + xx, y,     z + zz, floor2.getTypeId(), floor2.getDataValue());
	        				setBlock(w, x + xx, y + 1, z + zz, (short)0, (short)0); // AIR
	        				setBlock(w, x + xx, y + 2, z + zz, (short)0, (short)0); // AIR
	        			}
	        			else
	        			{
	        				setBlock(w, x + xx, y - 1, z + zz, pillar.getTypeId(), pillar.getDataValue());
	        				setBlock(w, x + xx, y,     z + zz, floor1.getTypeId(), floor1.getDataValue());
	        				setBlock(w, x + xx, y + 1, z + zz, (short)0, (short)0); // AIR
	        				setBlock(w, x + xx, y + 2, z + zz, (short)0, (short)0); // AIR
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
	
	        			if(found)
	        			{
	        				setBlock(w, x + xx, y - 1, z + zz, pillar.getTypeId(), pillar.getDataValue());
	        				setBlock(w, x + xx, y,     z + zz, floor1.getTypeId(), floor1.getDataValue());
	        				setBlock(w, x + xx, y + 1, z + zz, (short)0, (short)0); // AIR
	        				setBlock(w, x + xx, y + 2, z + zz, (short)0, (short)0); // AIR
	        			}else{
	            			if((valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0)
	            			{
	            				setBlock(w, x + xx, y - 1, z + zz, pillar.getTypeId(), pillar.getDataValue());
	            				setBlock(w, x + xx, y,     z + zz, floor2.getTypeId(), floor2.getDataValue());
	            				setBlock(w, x + xx, y + 1, z + zz, (short)0, (short)0); // AIR
	            				setBlock(w, x + xx, y + 2, z + zz, (short)0, (short)0); // AIR
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
	            					setBlock(w, x + xx, y - 1, z + zz, pillar.getTypeId(), pillar.getDataValue());
	            					setBlock(w, x + xx, y,     z + zz, floor1.getTypeId(), floor1.getDataValue());
	            					setBlock(w, x + xx, y + 1, z + zz, (short)0, (short)0); // AIR
	                				setBlock(w, x + xx, y + 2, z + zz, (short)0, (short)0); // AIR
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
	                					setBlock(w, x + xx, y - 1, z + zz,		pillar.getTypeId(),   pillar.getDataValue());
	                					setBlock(w, x + xx, y,     z + zz,		floor1.getTypeId(),   floor1.getDataValue());
	                					setBlock(w, x + xx, y + 1, z + zz,		(short)0, (short)0);  // AIR
	                    				setBlock(w, x + xx, y + 2, z + zz,		(short)0, (short)0);  // AIR
	                				}
	                			}
	            			}
	            		}
	        		}
        		}
            }
        }
	}

	private void setBlock(World w, int x, int y, int z, short typeId, short dataValue)
	{
		w.getBlockAt(x, y, z).setTypeIdAndData(typeId, (byte)dataValue, false);
	}
}
