package com.worldcretornica.plotme;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

import com.worldcretornica.plotme.utils.Jakky89Properties;


public class PlotMeDatabaseManager {

	private static Connection con = null;
	
	public final static String sqlitedb = "/plots.db";
	
	
    public static Connection initialize()
    {
        try
        {
        	if (PlotMe.usemySQL)
        	{
        		Class.forName("com.mysql.jdbc.Driver");
        		con = DriverManager.getConnection(PlotMe.mySQLconn, PlotMe.mySQLuname, PlotMe.mySQLpass);
        	}
        	else
        	{
        		Class.forName("org.sqlite.JDBC");
        		con = DriverManager.getConnection("jdbc:sqlite:" + PlotMe.configpath + "/plots.db");
        	}
        	con.setAutoCommit(false);
        }
        catch (SQLException ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "SQL exception on initialize:");
        	PlotMe.logger.severe("  " + ex.getMessage());
        }
        catch (ClassNotFoundException ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "You need the SQLite/MySQL library!");
        	PlotMe.logger.severe("  " + ex.getMessage());
        }
        catch (Exception ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "Exception occurred while initializing database connection :");
        	PlotMe.logger.severe("  " + ex.getMessage());
        }
        return con;
    }
    
    public static String getSchema()
    {
    	String constr = PlotMe.mySQLconn;
    	if (constr.lastIndexOf("/") > 0)
    	{
    		return constr.substring(constr.lastIndexOf("/") + 1);
    	}
    	return "";
    }
    
    public static Connection getConnection()
    {
		if (con == null)
		{
			con = initialize();
		}
		if (PlotMe.usemySQL)
		{
			try
			{
				if (!con.isValid(10))
				{
					con = initialize();
				}
			} 
			catch (SQLException ex) 
			{
				PlotMe.logger.severe(PlotMe.PREFIX + "Failed establishing SQL database connection :");
				PlotMe.logger.severe("  " + ex.getMessage());
			}
		}
		return con;
	}

    public static void closeConnection() {
		if (con != null)
		{
			try
			{
				if (PlotMe.usemySQL)
				{
					if (con.isValid(10))
					{
						con.close();
					}
				}
				else
				{
					con.close();
				}
				con = null;
			}
			catch (SQLException ex)
			{
				PlotMe.logger.severe(PlotMe.PREFIX + " Error while closing Database Connection :");
				PlotMe.logger.severe("  " + ex.getMessage());
			}
		}
    }
    
    public static boolean batchExecuteCommitOrRollback(Statement st) {
        int[] updateCounts = null;
        boolean sqlError = false;
        if (st == null)
        {
        	return false;
        }
		try
		{
	        if (st.isClosed())
	        {
	        	return false;
	        }
			updateCounts = st.executeBatch();
			if (updateCounts != null)
			{
				for (int i=0; i<updateCounts.length; i++)
				{
					if (updateCounts[i] == Statement.EXECUTE_FAILED)
					{
						sqlError = true;
						PlotMe.logger.severe(PlotMe.PREFIX + " SQL statement batch execution error : Statement.EXECUTE_FAILED");
						break;
					}
				}
	        }
			else
			{
	        	sqlError = true;
	        }
	        if (!sqlError)
	        {
            	try
            	{
            		con.commit();
            		return true;
            	}
            	catch (SQLException ex)
            	{
            		PlotMe.logger.severe(PlotMe.PREFIX + " SQL statement batch COMMIT exception :");
            		PlotMe.logger.severe("  " + ex.getMessage());
            		return false;
            	}
            	finally
            	{
            		if (!st.isClosed())
            		{
            			st.close();
            		}
            	}
	        }
	        else
	        {
	        	try
	        	{
	        		con.rollback();
            	}
	        	catch (SQLException ex)
	        	{
            		PlotMe.logger.severe(PlotMe.PREFIX + " SQL statement batch ROLLBACK exception :");
            		PlotMe.logger.severe("  " + ex.getMessage());
            		return false;
            	}
	        	finally
	        	{
            		PlotMe.logger.info(PlotMe.PREFIX + " Rolled back SQL statement batch.");
            		try
            		{
            			if (!st.isClosed())
            			{
            				st.close();
            			}
            		} catch (SQLException ex) {}
            	}
            }
		}
		catch (SQLException ex)
		{
			PlotMe.logger.severe(PlotMe.PREFIX + " SQL statement batch exception :");
			PlotMe.logger.severe("  " + ex.getMessage());
			return false;
		}
		return false;
    }
    
    public static PlotWorld getPlotWorld(World bukkitWorld)
    {
    	if (bukkitWorld == null)
    	{
    		return null;
    	}
    	
    	PlotWorld tmpWorld;
    	
    	tmpWorld = PlotManager.getPlotWorld(bukkitWorld);
    	if (tmpWorld != null)
    	{
    		return tmpWorld;
    	}
    	
    	con = getConnection();
    	if (con == null)
    	{
    		return null;
    	}

    	PreparedStatement st = null;
   	    ResultSet rs = null;
   	    
        try {
            Integer worldId = -1;
            st = con.prepareStatement("SELECT id, worldname FROM `" + PlotMe.databasePrefix + "plotme_worlds` WHERE worldname='?'");
            st.setString(1, bukkitWorld.getName());
            st.executeQuery();
            rs = st.getResultSet();
            if (rs.next())
            {
            	worldId = rs.getInt(1);
            	PlotMe.logger.info(PlotMe.PREFIX + "World \"" + bukkitWorld.getName() + "\" has id " + String.valueOf(worldId));
            }
            else
            {
	            st = con.prepareStatement("INSERT INTO `" + PlotMe.databasePrefix + "plotme_worlds` (worldname) VALUES ('?')", Statement.RETURN_GENERATED_KEYS);
	            st.setString(1, bukkitWorld.getName());
	            if (st.executeUpdate() != 1) {
	                return null;
	            }
	            rs = st.getGeneratedKeys();
	            if (rs.next()) {
	            	worldId = rs.getInt(1);
	            	PlotMe.logger.info(PlotMe.PREFIX + "Created new entry with id " + String.valueOf(worldId) + " for world \"" + bukkitWorld.getName() + "\" in database.");
	            }
	            else
	            {
	            	return null;
	            }
            }
            if (worldId != null && worldId > 0)
            {
                // error when we found more than one world with that name (should normally never happen)
                if (rs.next())
                {
                	PlotMe.logger.severe(PlotMe.PREFIX + "World with name \"" + bukkitWorld.getName() + "\" is not unique in database!");
                	return null;
                }
                return new PlotWorld(worldId, bukkitWorld);
            }
            return null;
        }
        catch (SQLException ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "Error while getting data for world \"" + bukkitWorld.getName() + "\" from database :");
        	PlotMe.logger.severe("  " + ex.getMessage());
        }
        finally
        {
            if (rs != null)
            {
            	try
            	{
            		rs.close();
            	} catch (SQLException ex) {}
            }
            if (st != null)
            {
            	try
            	{
            		st.close();
            	} catch (SQLException ex) {}
            }
        }
        return null;
    }
    
    public static PlotPlayer getPlotPlayer(String playerName, String displayName)
    {
    	if (playerName == null || playerName.isEmpty())
    	{
    		return null;
    	}
    	
    	PlotPlayer tmpPlayer;
    	
    	tmpPlayer = PlotManager.getPlotOwner(playerName);
    	if (tmpPlayer != null)
    	{
    		return tmpPlayer;
    	}
    	
    	con = getConnection();
    	if (con == null)
    	{
    		return null;
    	}

    	PreparedStatement st = null;
   	    ResultSet rs = null;
   	    
        try {
            Integer playerId = -1;
            st = con.prepareStatement("SELECT id, playername, displayname FROM `" + PlotMe.databasePrefix + "plotme_players` WHERE playername='?'");
            st.setString(1, playerName);
            st.executeQuery();
            rs = st.getResultSet();
            if (rs.next())
            {
            	playerId = rs.getInt(1);
            	if (displayName == null || displayName.isEmpty())
            	{
            		displayName = rs.getString(2);
            	}
            	PlotMe.logger.info(PlotMe.PREFIX + "PlotMe player \"" + playerName + "\" has id " + String.valueOf(playerId));
            }
            else
            {
	            st = con.prepareStatement("INSERT INTO `" + PlotMe.databasePrefix + "plotme_players` (playername) VALUES ('?')", Statement.RETURN_GENERATED_KEYS);
	            st.setString(1, playerName);
	            if (st.executeUpdate() != 1) {
	                return null;
	            }
	            rs = st.getGeneratedKeys();
	            if (rs.next()) {
	            	playerId = rs.getInt(1);
	            	PlotMe.logger.info(PlotMe.PREFIX + "Created new entry with id " + String.valueOf(playerId) + " for plot owner \"" + playerName + "\" in database.");
	            }
	            else
	            {
	            	return null;
	            }
            }
            if (playerId != null && playerId > 0)
            {
                // error when we found more than one world with that name (should normally never happen)
                if (rs.next())
                {
                	PlotMe.logger.severe(PlotMe.PREFIX + "PlotMe player \"" + playerName + "\" is not unique in database!");
                	return null;
                }
                if (displayName != null)
                {
                	return new PlotPlayer(playerId, playerName, displayName);
                }
                else
                {
                	return new PlotPlayer(playerId, playerName);
                }
            }
            return null;
        }
        catch (SQLException ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "Error while getting data for player \"" + playerName + "\" from database:");
        	PlotMe.logger.severe("  " + ex.getMessage());
        }
        finally
        {
            if (rs != null)
            {
            	try
            	{
            		rs.close();
            	} catch (SQLException ex) {}
            }
            if (st != null)
            {
            	try
            	{
            		st.close();
            	} catch (SQLException ex) {}
            }
        }
        return null;
    }
    
    public static List<Plot> loadPlayerOwnedPlots(int ownerId)
    {
    	if (ownerId < 0)
    	{
    		return null;
    	}
    	
    	con = getConnection();
    	if (con == null)
    	{
    		return null;
    	}
    	
    	int id;
    	Statement st = null;
    	ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT id,owner " +
								 "FROM `" + PlotMe.databasePrefix + "plotme_plots` " +
								 "WHERE " +
								 		"owner=" + String.valueOf(ownerId) +
								 ";");
			while (rs.next()) 
			{
				if (rs.getInt(2) == ownerId)
				{
					id = rs.getInt(1);

				}
			}
            return null;
        }
        catch (SQLException ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "Error while getting plots of owner with id " + String.valueOf(ownerId) + " from database:");
        	PlotMe.logger.severe("  " + ex.getMessage());
        }
        finally
        {
            if (st != null)
            {
            	try
            	{
            		st.close();
            	} catch (SQLException ex) {}
            }
        }
        return null;
    }
    
    public static void loadPlots(PlotWorld plotWorld, final int centerX, final int centerZ, final int range)
    {
    	if (plotWorld == null)
    	{
    		return;
    	}
    	
    	con = getConnection();
    	if (con == null)
    	{
    		return;
    	}
    	
    	final int minX = (int)Math.ceil(centerX - (range / 2));
    	final int minZ = (int)Math.ceil(centerZ - (range / 2));
    	final int maxX = minX + range;
    	final int maxZ = minZ + range;
    	
    	int id;
    	PlotPosition plotpos;
    	Plot plot;
    	Statement st = null;
    	ResultSet rs = null;
    	byte[] buf;
    	
    	ObjectInputStream oin = null;
    	Jakky89Properties properties = null;
    	
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT id,world,xpos,zpos,playername,biome," +
										"expireddate,finisheddate,price," +
										"isforsale,isprotected,auction,properties " +
								 "FROM `" + PlotMe.databasePrefix + "plotme_plots` " +
								 "INNER JOIN `" + PlotMe.databasePrefix + "plotme_players` " +
								 		"ON `" + PlotMe.databasePrefix + "plotme_players`.id=`" + PlotMe.databasePrefix + "plotme_plots`.owner " +
								 "WHERE " +
								 		"world=" + String.valueOf(plotWorld.getId()) + " " +
								 	"AND " +
								 		"(xpos BETWEEN "+String.valueOf(minX)+" AND "+String.valueOf(maxX)+") " +
								 	"AND " +
								 		"(zpos BETWEEN "+String.valueOf(minZ)+" AND "+String.valueOf(maxZ)+");");

			while (rs.next()) 
			{
				if (rs.getInt(2) == plotWorld.getId())
				{
					id = rs.getInt(1);
					int xpos = rs.getInt(3);
					int zpos = rs.getInt(4);
					plotpos = new PlotPosition(plotWorld, xpos, zpos);
					if (plotWorld.getPlotAtPlotPosition(plotpos) == null)
					{
						boolean isforsale = false;
						if (rs.getInt(10) == 1)
						{
							isforsale = true;
						}
						boolean isprotected = true;
						if (rs.getInt(11) == 0)
						{
							isprotected = false;
						}
						// load other properties
						buf = rs.getBytes(13);
						oin = null;
		    		    properties = null;
		    		    if (buf != null)
		    		    {
							try {
								oin = new ObjectInputStream(new ByteArrayInputStream(buf));
								properties = (Jakky89Properties)oin.readObject();
							} catch (ClassNotFoundException ex) {
								PlotMe.logger.severe(PlotMe.PREFIX + "Error while loading plot properties object:");
								PlotMe.logger.severe("  " + ex.getMessage());
							} catch (IOException ex) {
					        	PlotMe.logger.severe(PlotMe.PREFIX + "Error while loading plot properties object:");
								PlotMe.logger.severe("  " + ex.getMessage());
							}
		    		    }
						plot = new Plot(
							id,
							plotpos,
		    				getPlotPlayer(rs.getString(5), ""),
		    				Biome.valueOf(rs.getString(6)),
		    				rs.getLong(7),
		    				rs.getLong(8),
		    				rs.getDouble(9),
		    				isforsale,
		    				isprotected,
		    				properties
						);
		
						PlotManager.registerPlot(plot);
						
						if (rs.getInt(12) > 0)
						{
							plot.setAuctionNumber(rs.getInt(12));
						}
					
						//PlotMeSqlManager.loadPlotProperties(plot);
					}
				}
			}

			PreparedStatement ps;
			for (int x=minX; x<maxX; x++)
			{
				for (int z=minZ; z<maxZ; z++)
				{
					plotpos = new PlotPosition(plotWorld, x, z);
					if (plotWorld.getPlotAtPlotPosition(plotpos) == null)
					{
						ps = con.prepareStatement("INSERT INTO `" + PlotMe.databasePrefix + "plotme_plots` (world,xpos,zpos) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
						ps.setInt(1, plotWorld.getId());
						ps.setInt(2, x);
						ps.setInt(3, z);
			            if (ps.executeUpdate() > 0) {
			            	rs = ps.getGeneratedKeys();
			            	if (rs.next())
			            	{
			            		id = rs.getInt(1);
			            		plot = new Plot(id, plotpos);
			            		
			            		PlotManager.registerPlot(plot);
			            		
			            		PlotMe.logger.info(PlotMe.PREFIX + "Created new id " + String.valueOf(id) + " for plot at ( " + String.valueOf(x) + " | " + String.valueOf(z) + " ) in database.");
			            	}
			            }
					}
				}
			}
        }
        catch (SQLException ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "Error while getting data for plots at " + String.valueOf(centerX) + "," + String.valueOf(centerZ) + " (range " + String.valueOf(range) + ") from database:");
        	PlotMe.logger.severe("  " + ex.getMessage());
        }
        finally
        {
            if (rs != null)
            {
            	try
            	{
            		rs.close();
            	} catch (SQLException ex) {}
            }
            if (st != null)
            {
            	try
            	{
            		st.close();
            	} catch (SQLException ex) {}
            }
        }
    }
    
    public static void loadPlots(PlotWorld plotWorld, final Location centerLocation, final int range)
    {
    	loadPlots(plotWorld, centerLocation.getBlockX(), centerLocation.getBlockZ(), range);
    }

    public static void insertPlot(Plot plot)
    {
    	if (plot == null || plot.getPlotWorld() == null)
    	{
    		return;
    	}
    	
        PreparedStatement ps = null;
        Connection conn;
        //Plots
        try 
        {
            conn = getConnection();
            ps = conn.prepareStatement("INSERT INTO `" + PlotMe.databasePrefix + "plotme_plots` (id, world, xpos, zpos, owner, biome, expireddate, finisheddate, price, isforsale, isprotected, auction, properties) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
            
            ps.setInt(1, plot.getId());
            ps.setInt(2, plot.getPlotWorld().getId());
            ps.setInt(3, plot.getPlotX());
            ps.setInt(4, plot.getPlotZ());
            if (plot.getOwner() != null)
            {
            	ps.setInt(5, plot.getOwner().getId());
            }
            else
            {
            	ps.setInt(5, 0);
            }
            ps.setString(6, plot.getBiome().toString());
            
            if (plot.getExpiration() > 0)
            {
            	ps.setLong(7, plot.getExpiration());
            }
            else
            {
            	ps.setLong(7, (Long)null);
            }
            
            if (plot.getFinish() > 0)
            {
            	ps.setLong(8, plot.getFinish());
            }
            else
            {
            	ps.setLong(8, (Long)null);
            }
            
            ps.setDouble(9, plot.getPrice());
            
            if (plot.isForSale())
            {
            	ps.setByte(10, (byte)1);
            }
            else
            {
            	ps.setByte(10, (byte)0);
            }
            
            if (plot.isProtected())
            {
            	ps.setByte(11, (byte)1);
            }
            else
            {
            	ps.setByte(11, (byte)0);
            }
            
            ps.setInt(12, plot.getAuctionNumber());
            
            ps.executeUpdate();
            conn.commit();
        } 
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "EXCEPTION occurred while inserting plot data:");
        	PlotMe.logger.severe("  " + ex.getMessage());
        } 
        finally 
        {
            try 
            {
                if (ps != null) 
                {
                    ps.close();
                }
            } catch (SQLException ex) {}
        }
    }
    
    public static void updatePlotPosition(Plot plot)
    {
    	if (plot == null || plot.getPlotWorld() == null)
    	{
    		return;
    	}
    	
        PreparedStatement ps = null;
        Connection conn;
        
        //Plots
        try 
        {
            conn = getConnection();

            ps = conn.prepareStatement("UPDATE `" + PlotMe.databasePrefix + "plotme_plots` SET world=?, xpos=?, zpos=? WHERE id=? LIMIT 1");
            
            ps.setInt(1, plot.getPlotWorld().getId());
            ps.setInt(2, plot.getPlotX());
            ps.setInt(3, plot.getPlotZ());
            ps.setInt(4, plot.getId());
            
            ps.executeUpdate();
            conn.commit();
                        
        } 
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "EXCEPTION occurred while updating plot coordinates in database:");
        	PlotMe.logger.severe("  " + ex.getMessage());
        } 
        finally 
        {
            try 
            {
                if (ps != null) 
                {
                    ps.close();
                }
            } catch (SQLException ex) {}
        }
    }
    
    public static void updatePlotData(Plot plot, String colName, Object cellValue)
    {
        PreparedStatement ps = null;
        Connection conn;
        
        //Plots
        try 
        {
            conn = getConnection();

            ps = conn.prepareStatement("UPDATE `" + PlotMe.databasePrefix + "plotme_plots` SET " + colName + "=? WHERE id=? LIMIT 1");
            
            ps.setObject(1, cellValue);
            ps.setInt(2, plot.getId());
            
            ps.executeUpdate();
            conn.commit();
                        
        } 
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + " Insert Exception :");
        	PlotMe.logger.severe("  " + ex.getMessage());
        } 
        finally 
        {
            try 
            {
                if (ps != null) 
                {
                    ps.close();
                }
            } catch (SQLException ex) {}
        }
    }
  
    public static int getNextAuctionNumber(Plot plot)
    {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        int tmpan = -1;
        
        try
        {
	        con = getConnection();
		    st = con.createStatement();
		    rs = st.executeQuery("SELECT MAX(auction) FROM `" + PlotMe.databasePrefix + "plotme_auctions`");
		    while (rs.next())
		    {
		    	if (rs.getInt(1) >= tmpan)
		    	{
		    		tmpan = rs.getInt(1) + 1;
		    	}
		    }
		    return tmpan;
        }
        catch (Exception ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "Error while getting next auction number from database!");
			PlotMe.logger.severe("  " + ex.getMessage());
		}
        finally
        {
        	if (st != null)
        	{
        		try {
					st.close();
				} catch (SQLException ex) {}
        	}
        }
        return -1;
    }

    public static void addPlotBid(int auctionNumber, long bidDate, int auctionPlotId, int playerId, double amount)
    {
    	PreparedStatement ps = null;

    	//Auctions
        try 
        {
            con = getConnection();
            
            ps = con.prepareStatement("INSERT INTO `plotme_auctions` (auction,date,plot,player,amount) VALUES (?,?,?,?,?)");
            
            ps.setInt(1, auctionNumber);
            ps.setLong(2, bidDate);
            ps.setInt(3, auctionPlotId);
            ps.setInt(4, playerId);
            ps.setDouble(5, amount);
            
            ps.executeUpdate();
            con.commit();
            
        } catch (SQLException ex) {
        	PlotMe.logger.severe(PlotMe.PREFIX + " Insert Exception :");
        	PlotMe.logger.severe("  " + ex.getMessage());
        } 
        finally 
        {
            try 
            {
                if (ps != null) 
                {
                    ps.close();
                }
            } catch (SQLException ex) {}
        }
    }
    
    public static void addPlotBid(Plot auctionPlot, PlotPlayer auctionBidder, double amount)
    {
    	addPlotBid(auctionPlot.getAuctionNumber(), Math.round(System.currentTimeMillis() / 1000), auctionPlot.getId(), auctionBidder.getId(), amount);
    }
    
    public static int addPlotComment(int plotId, int playerId, String message)
    {
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	int id = -1;
        
    	//Comments
        try 
        {
            con = getConnection();
            
            ps = con.prepareStatement("INSERT INTO `" + PlotMe.databasePrefix + "plotme_comments` (plot, player, type, message) VALUES (?,?,?,'?')");
            
            ps.setInt(1, plotId);
            ps.setInt(2, playerId);
            ps.setInt(3, 0);
            ps.setString(4, message);
            
            if (ps.executeUpdate() > 0) {
            	rs = ps.getGeneratedKeys();
            	if (rs.next())
            	{
            		id = rs.getInt(1);
            	}
            }
            if (id > 0)
            {
            	con.commit();
            }
            else
            {
            	con.rollback();
            }
   
            return id;
        }
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "EXCEPTION occurred while inserting comment:");
        	PlotMe.logger.severe("  " + ex.getMessage());
        	return -1;
        } 
        finally 
        {
            try 
            {
                if (ps != null) 
                {
                    ps.close();
                }
            } catch (SQLException e) {}
        }
    }

    public static void deletePlotComment(Plot plot, int commentId) {
        PreparedStatement ps = null;
        try {
        	con = getConnection();
            ps = con.prepareStatement("DELETE FROM `" + PlotMe.databasePrefix + "plotme_comments` WHERE id=? AND plot=?");
            ps.setInt(1, commentId);
            ps.setInt(2, plot.getId());
            ps.executeUpdate();
            con.commit();
        } catch (SQLException ex) {
        	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception :");
        	PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception (on close) :");
            	PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }

    public static void deleteAllPlotBids(Plot plot)
    {
        PreparedStatement ps = null;
        try 
        {
            con = getConnection();

            ps = con.prepareStatement("DELETE FROM `" + PlotMe.databasePrefix + "plotme_auctions` WHERE plot=?");
            ps.setInt(1, plot.getId());
            
            ps.executeUpdate();
            
            con.commit();
        } 
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "EXCEPTION occurred while trying to delete auctions of plot " + String.valueOf(plot.getId()) + ":");
        	PlotMe.logger.severe("  " + ex.getMessage());
        } 
        finally 
        {
            try 
            {
                if (ps != null) 
                {
                    ps.close();
                }
            } catch (SQLException e) {}
        }
    }

	public static boolean removePlot(Plot plot) {
        Statement st = null;
        try 
        {
            con = getConnection();

            st = con.createStatement();
            st.addBatch("DELETE FROM `" + PlotMe.databasePrefix + "plotme_plots` WHERE id=" + String.valueOf(plot.getId()));
            st.addBatch("DELETE FROM `" + PlotMe.databasePrefix + "plotme_auctions` WHERE plot=" + String.valueOf(plot.getId()));
            st.addBatch("DELETE FROM `" + PlotMe.databasePrefix + "plotme_comments` WHERE plot=" + String.valueOf(plot.getId()));
            
            return batchExecuteCommitOrRollback(st);
        } 
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "Plot removal database exception :");
        	PlotMe.logger.severe("  " + ex.getMessage());
        	return false;
        } 
        finally 
        {
            try 
            {
                if (st != null) 
                {
                    st.close();
                }
            } catch (SQLException e) {}
        }
	}
 
}
