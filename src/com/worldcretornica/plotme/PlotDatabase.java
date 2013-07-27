package com.worldcretornica.plotme;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import com.worldcretornica.plotme.utils.Jakky89Properties;
import com.worldcretornica.plotme.utils.Pair;


public class PlotDatabase {

	private static Connection con = null;

	
    final static String LAYOUT_WORLD_TABLE	=	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "worlds` " +
					"("
						+ "id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,"
						+ "worldname VARCHAR(64) NOT NULL UNIQUE,"
						+ "plotsize SMALLINT NOT NULL DEFAULT 16,"
						+ "roadwidth SMALLINT NOT NULL DEFAULT 8,"
						+ "properties TEXT DEFAULT NULL" +
					")";

	final static String LAYOUT_PLAYER_TABLE	=	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "players` " + 
		 			"("
		 	  			+ "id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,"
		 	  			+ "playername VARCHAR(32) NOT NULL UNIQUE,"
		 	  			+ "displayname VARCHAR(32) DEFAULT NULL,"
		 	  			+ "lastonline UNSIGNED INTEGER DEFAULT NULL,"
		 	  			+ "properties TEXT DEFAULT NULL" +
		 	  		")";

	final static String LAYOUT_PLOT_TABLE	=	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plots` " +
				 	"("
				  		+ "id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,"
		  		  		+ "world UNSIGNED INTEGER,"
		  		  		+ "xpos INTEGER,"
		  		  		+ "zpos INTEGER,"
		  		  		+ "owner INTEGER DEFAULT NULL,"
		  		  		+ "biome VARCHAR(16) DEFAULT NULL,"
		  		  		+ "expiredate UNSIGNED INTEGER DEFAULT NULL,"
				  		+ "finishdate UNSIGNED INTEGER DEFAULT NULL,"
				  		+ "claimprice UNSIGNED FLOAT DEFAULT NULL,"
				  		+ "auction UNSIGNED INTEGER DEFAULT NULL,"
				  		+ "properties TEXT DEFAULT NULL," +
				  		  " UNIQUE (world, xpos, zpos)" +
				  	")";

	final static String LAYOUT_PLOTAUCTION_TABLE = "CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotauctions` " +
					"("
						+ "plot INTEGER NOT NULL,"
						+ "date UNSIGNED INTEGER NOT NULL,"
						+ "auction UNSIGNED INTEGER NOT NULL,"
						+ "player INTEGER NOT NULL,"
						+ "amount UNSIGNED FLOAT NOT NULL" +
					")";
	
	final static String LAYOUT_PLOTCOMMENT_TABLE =	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotcomments` " +
				 	"("
				 		+ "id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,"
				 		+ "plot UNSIGNED INTEGER NOT NULL,"
				 		+ "player UNSIGNED INTEGER NOT NULL,"
				 		+ "type UNSIGNED TINYINT(2) NOT NULL DEFAULT 0,"
				 		+ "message TEXT" +
				 	")";
	
	/*final static String LAYOUT_ROOMS_TABLE =	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "rooms` " +
					"("
						+ "`id` INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,"
						+ "`relx1` INTEGER NOT NULL,"
						+ "`rely1` INTEGER NOT NULL,"
						+ "`relz1` INTEGER NOT NULL,"
						+ "`relx2` INTEGER NOT NULL,"
						+ "`rely2` INTEGER NOT NULL,"
						+ "`relz2` INTEGER NOT NULL,"
						+ "`rentprice` DOUBLE NOT NULL DEFAULT 0,"
						+ "`rentbalance` DOUBLE NOT NULL DEFAULT 0,"
						+ "`rentee` UNSIGNED INTEGER DEFAULT NULL,"
						+ "`type` UNSIGNED TINYINT(1) NOT NULL DEFAULT 0,"
						+ "`comment` TEXT" +
					")";*/

	
    public static Connection initialize()
    {
    	PlotMe.plotMeReady = false;
        try
        {
        	if (PlotMe.useMySQL)
        	{
        		Class.forName("com.mysql.jdbc.Driver");
        		con = DriverManager.getConnection(PlotMe.mySQLconn, PlotMe.mySQLuname, PlotMe.mySQLpass);
        		PlotMe.plotMeReady = true;
        		return con;
        	}
        	else
        	{
        		Class.forName("org.sqlite.JDBC");
        		con = DriverManager.getConnection(PlotMe.sqliteConn);
        		PlotMe.plotMeReady = true;
        		return con;
        	}
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
        return null;
    }
    
    public static boolean batchExecuteCommitOrRollback(Statement st)
    {
        int[] updateCounts = null;
        boolean sqlError = false;
        if (st == null)
        {
        	return false;
        }
		try
		{
			con.setAutoCommit(false);
			
			updateCounts = st.executeBatch();
			if (updateCounts != null)
			{
				if (updateCounts.length == 0)
				{
					return true;
				}
				else if (updateCounts.length > 0)
				{
					for (int i = 0; i < updateCounts.length; i++)
					{
						if (updateCounts[i] == Statement.EXECUTE_FAILED)
						{
							sqlError = true;
							PlotMe.logger.severe(PlotMe.PREFIX + " SQL statement batch execution error : Statement.EXECUTE_FAILED");
							break;
						}
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
           			st.close();
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
           				st.close();
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

    
	public static boolean updateDatabase()
    {
        Connection con = null;
        Statement st = null;
        PreparedStatement ps = null;

		try
		{
	        con = getConnection();
			if (con == null)
			{
				PlotMe.logger.severe(PlotMe.PREFIX + "Could not establish database connection! Not creating or update tables.");
				return false;
			}

			st  = con.createStatement();
			if (st == null)
			{
				PlotMe.logger.severe(PlotMe.PREFIX + "Could not create database statement! Not creating or update tables.");
				return false;
			}
			
	        try
	        {
	        	if (PlotMe.useMySQL)
	        	{
		        	st.addBatch(LAYOUT_WORLD_TABLE);
		        	st.addBatch(LAYOUT_PLAYER_TABLE);
		        	st.addBatch(LAYOUT_PLOT_TABLE);
		        	st.addBatch(LAYOUT_PLOTAUCTION_TABLE);
		        	st.addBatch(LAYOUT_PLOTCOMMENT_TABLE);
	        	}
	        	else
	        	{
		        	st.addBatch(LAYOUT_WORLD_TABLE.replace("AUTO_INCREMENT", "AUTOINCREMENT"));
		        	st.addBatch(LAYOUT_PLAYER_TABLE.replace("AUTO_INCREMENT", "AUTOINCREMENT"));
		        	st.addBatch(LAYOUT_PLOT_TABLE.replace("AUTO_INCREMENT", "AUTOINCREMENT"));
		        	st.addBatch(LAYOUT_PLOTAUCTION_TABLE.replace("AUTO_INCREMENT", "AUTOINCREMENT"));
		        	st.addBatch(LAYOUT_PLOTCOMMENT_TABLE.replace("AUTO_INCREMENT", "AUTOINCREMENT"));
	        	}
	   			if (PlotDatabase.batchExecuteCommitOrRollback(st))
	   			{
	   				return true;
	   			}
	   			else
	   			{
	   				PlotMe.logger.severe(PlotMe.PREFIX + "Could not create needed database tables!");
	   				return false;
	   			}
	        }
	        catch (SQLException ex) 
	        {
	        	PlotMe.logger.severe(PlotMe.PREFIX + "Exception occurred while creating needed database tables :");
	        	PlotMe.logger.severe("  " + ex.getMessage());
	        }
	        finally
	        {
	        	if (st != null)
	        	{
	        		st.close();
	        	}
	        }
		}
		catch (SQLException ex)
		{
        	PlotMe.logger.severe(PlotMe.PREFIX + "SQLEXCEPTION occurred:");
        	PlotMe.logger.severe("  " + ex.getMessage());
		}
		catch (Exception ex)
		{
			PlotMe.logger.warning(PlotMe.PREFIX + "EXCEPTION occurred while updating database tables:");
			PlotMe.logger.severe("  " + ex.getMessage());
		}
		finally
		{
			try 
			{
				if (st != null)
				{
					st.close();
				}
				if (ps != null)
				{
					ps.close();
				}
			} 
			catch (SQLException ex) 
			{
				PlotMe.logger.severe(PlotMe.PREFIX + "Could not close database statement ressource :");
				PlotMe.logger.severe("  " + ex.getMessage());
			}
		}
		return false;
    }

    public static Connection getConnection()
    {
		try
		{
			if (con == null || con.isClosed() || (PlotMe.useMySQL && !con.isValid(3)))
			{
				con = initialize();
			}
			return con;
		} 
		catch (SQLException ex) 
		{
			PlotMe.logger.severe(PlotMe.PREFIX + "Failed establishing SQL database connection :");
			PlotMe.logger.severe("  " + ex.getMessage());
			con = null;
			return null;
		}
	}

    public static void closeConnection() {
		if (con == null)
		{
			return;
		}
		try
		{
			con.close();
			con = null;
		} catch (Exception ex) {}
    }
    
    public static PlotWorld getPlotWorld(String worldName)
    {
    	if (worldName == null || worldName.isEmpty())
    	{
    		return null;
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
            st = con.prepareStatement("SELECT id, worldname FROM `" + PlotMe.databasePrefix + "worlds` WHERE worldname=?");
            st.setString(1, worldName);
            rs = st.executeQuery();
            if (rs.next())
            {
            	worldId = rs.getInt(1);
            	worldName = rs.getString(2);
            	PlotMe.logger.info(PlotMe.PREFIX + "Requested world \"" + worldName + "\" with id " + String.valueOf(worldId));
            }
            else
            {
	            st = con.prepareStatement("INSERT INTO `" + PlotMe.databasePrefix + "worlds` (worldname) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
	            st.setString(1, worldName);
	            if (st.executeUpdate() != 1) {
	                return null;
	            }
	            rs = st.getGeneratedKeys();
	            if (rs.next()) {
	            	worldId = rs.getInt(1);
	            	PlotMe.logger.info(PlotMe.PREFIX + "Created new entry with id " + String.valueOf(worldId) + " for world \"" + worldName + "\" in database.");
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
                	PlotMe.logger.severe(PlotMe.PREFIX + "World with name \"" + worldName + "\" is not unique!");
                	return null;
                }
                return new PlotWorld(worldId, worldName);
            }
            return null;
        }
        catch (SQLException ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "SQLEXCEPTION occurred while getting data for world \"" + worldName + "\" from database :");
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
    
    public static PlotPlayer loadPlotPlayer(int plotPlayerId)
    {
    	con = getConnection();
    	if (con == null)
    	{
    		return null;
    	}

    	Statement st = null;
   	    ResultSet rs = null;
        try {
            st = con.createStatement();
            rs = st.executeQuery("SELECT id, playername, displayname, lastonline FROM `" + PlotMe.databasePrefix + "players` WHERE id=" + String.valueOf(plotPlayerId) + " LIMIT 1");
            rs = st.getResultSet();
            if (rs.next())
            {
               	return new PlotPlayer(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4));
            }
        	PlotMe.logger.severe(PlotMe.PREFIX + "Got no result from database for plot player with id " + String.valueOf(plotPlayerId) + "!");
            return null;
        }
        catch (SQLException ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "Error while getting data for plot player with id \"" + String.valueOf(plotPlayerId) + "\" from database:");
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

    public static PlotPlayer getPlotPlayer(String playerName)
    {
    	if (playerName == null || playerName.isEmpty())
    	{
    		return null;
    	}

    	con = getConnection();
    	if (con == null)
    	{
    		return null;
    	}

    	PreparedStatement st = null;
   	    ResultSet rs = null;
        try {
        	int playerId = -1;
        	String displayName = null;
        	int lastOnline = 0;
            st = con.prepareStatement("SELECT id, playername, displayname, lastonline FROM `" + PlotMe.databasePrefix + "players` WHERE playername=? LIMIT 1");
            st.setString(1, playerName);
            rs = st.executeQuery();
            if (rs.next())
            {
            	playerId = rs.getInt(1);
            	playerName = rs.getString(2);
           		displayName = rs.getString(3);
           		lastOnline = rs.getInt(4);
            	PlotMe.logger.info(PlotMe.PREFIX + "Fetched PlotMe player \"" + playerName + "\" with id " + String.valueOf(playerId));
            }
            else
            {
	            st = con.prepareStatement("INSERT INTO `" + PlotMe.databasePrefix + "players` (playername) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
	            st.setString(1, playerName);
	            if (st.executeUpdate() == 0) {
	                return null;
	            }
	            rs = st.getGeneratedKeys();
	            if (rs.next()) {
	            	playerId = rs.getInt(1);
	            	PlotMe.logger.info(PlotMe.PREFIX + "Created new entry with id " + String.valueOf(playerId) + " for player \"" + playerName + "\" in PlotMe database " + PlotMe.databasePrefix + "players");
	            }
	            else
	            {
	            	return null;
	            }
            }
            return new PlotPlayer(playerId, playerName, displayName, lastOnline);
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
    
    public static PlotPlayer getPlotPlayer(Player bukkitPlayer)
    {
    	return getPlotPlayer(bukkitPlayer.getName());
    }

    public static List<Integer> loadPlayerOwnedPlotIds(int ownerId)
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
    	
    	List<Integer> tmpList = new ArrayList<Integer>();
    	
    	Statement st = null;
    	ResultSet rs = null;
    	
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT id " +
								 "FROM `" + PlotMe.databasePrefix + "plots` " +
								 "WHERE " +
								 		"owner=" + String.valueOf(ownerId)
								);
			while (rs.next()) 
			{
				tmpList.add(rs.getInt(1));
			}
			
            return tmpList;
        }
        catch (SQLException ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "EXCEPTION occurred while getting plots of owner with id " + String.valueOf(ownerId) + " from database:");
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
    
    public static void loadPlotProperties(Plot plot, byte[] byteArray)
    {
    	if (byteArray.length > 0)
        {
			try {
				ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(byteArray));
				plot.setProperties((Jakky89Properties)oin.readObject());
			}
			catch (ClassNotFoundException ex)
			{
				PlotMe.logger.severe(PlotMe.PREFIX + "Error while loading plot properties object:");
				PlotMe.logger.severe("  " + ex.getMessage());
			}
			catch (IOException ex)
			{
	        	PlotMe.logger.severe(PlotMe.PREFIX + "Error while loading plot properties object:");
				PlotMe.logger.severe("  " + ex.getMessage());
			}
        }
    	else
    	{
    		plot.setProperties(new Jakky89Properties());
    	}
    }
    
    
    /**
     * Load a plot and data by its id directly from database
     * 
     * @param plotId
     * @return Plot instance
     */
    public static Plot loadPlot(final int plotId)
    {
    	if (plotId < 1)
    	{
    		return null;
    	}
    	
    	con = getConnection();
    	if (con == null)
    	{
    		return null;
    	}

    	Statement st = null;
    	ResultSet rs = null;
    	
		try
		{
			st = con.createStatement();
			rs = st.executeQuery("SELECT world,xpos,zpos,owner,biome," +
										"claimprice,expiredate,finishdate " +
								 "FROM `" + PlotMe.databasePrefix + "plots` " +
								 "WHERE " +
								 		"id=" + String.valueOf(plotId) +
								 "LIMIT 1"
								);

			if (rs.next())
			{
				PlotPosition plotpos = new PlotPosition(PlotManager.getPlotWorld(rs.getInt(1)), rs.getInt(2), rs.getInt(3));
				
				Biome biome = null;
				String biomestr =  rs.getString(5);
				if (rs.wasNull()) {
					biome = PlotMe.DEFAULT_PLOT_BIOME;
				} else {
					biome = Biome.valueOf(biomestr);
				}
				
					
				Plot plot = new Plot(
										plotId,
										plotpos,
										PlotManager.getPlotPlayer(rs.getInt(4)),
										biome,
										rs.getFloat(6),
										rs.getInt(7),
										rs.getInt(8)
									);
		
				PlotManager.registerPlot(plot);
						
				if (rs.getInt(11) > 0)
				{
					plot.setAuctionNumber(rs.getInt(12));
				}
				
				return plot;
				
			}
			return null;
		}
        catch (SQLException ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "Error while getting data of plot with id " + String.valueOf(plotId) + " from database!");
        	PlotMe.logger.severe("  " + ex.getMessage());
        	return null;
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

    public static void getPlots(PlotWorld plotWorld, int X1, int X2, int Z1, int Z2)
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
    	
    	int minX = Math.min(X1, X2);
    	int minZ = Math.min(Z1, Z2);
    	int maxX = Math.max(X1, X2);
    	int maxZ = Math.max(Z1, Z2);
    	
    	int id;
    	PlotPosition plotpos;
    	Plot plot;
    	Statement st = null;
    	ResultSet rs = null;

		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT id,xpos,zpos,owner,biome," +
										"claimprice,expiredate,finishdate " +
								 "FROM `" + PlotMe.databasePrefix + "plots` " +
								 "WHERE " +
								 		"world=" + String.valueOf(plotWorld.getId()) + " " +
								 	"AND " +
								 		"(xpos BETWEEN "+String.valueOf(minX)+" AND "+String.valueOf(maxX)+") " +
								 	"AND " +
								 		"(zpos BETWEEN "+String.valueOf(minZ)+" AND "+String.valueOf(maxZ)+");");

			while (rs.next()) 
			{
				plotpos = new PlotPosition(plotWorld, rs.getInt(2), rs.getInt(3));
				
				Biome biome = null;
				String biomestr =  rs.getString(5);
				if (!rs.wasNull())
					biome = Biome.valueOf(biomestr);
				
				if (plotWorld.getPlotAtPlotPosition(plotpos) == null)
				{
					plot = new Plot(
						rs.getInt(1),
						plotpos,
		    			PlotManager.getPlotPlayer(rs.getInt(4)),
		    			biome,
		    			rs.getFloat(6),
		    			rs.getInt(7),
		    			rs.getInt(8)
					);

					if (rs.getInt(11) > 0)
					{
						plot.setAuctionNumber(rs.getInt(11));
					}
						
					loadPlotProperties(plot, rs.getBytes(12));
		
					PlotManager.registerPlot(plot);
				}
			}

			PreparedStatement ps;
			ps = con.prepareStatement("INSERT INTO `" + PlotMe.databasePrefix + "plots` (world,xpos,zpos) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
			for (int x=minX; x<maxX; x++)
			{
				for (int z=minZ; z<maxZ; z++)
				{
					plotpos = new PlotPosition(plotWorld, x, z);
					if (plotWorld.getPlotAtPlotPosition(plotpos) == null)
					{
						ps.setInt(1, plotWorld.getId());
						ps.setInt(2, x);
						ps.setInt(3, z);
			            if (ps.executeUpdate() > 0)
			            {
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
        	PlotMe.logger.severe(PlotMe.PREFIX + "Error while getting data for plots at " + String.valueOf(X1) + "," + String.valueOf(Z1) + " from database:");
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
    
    /**
     * @return: ArrayList of FINISHED Plot IDs
     */
    public static List<Integer> getFinishedPlots()
    {
    	con = getConnection();
    	if (con == null)
    	{
    		return null;
    	}

    	Statement st = null;
   	    ResultSet rs = null;
   	    
   	    long currentTime = Math.round(System.currentTimeMillis() / 1000);

        try {
        	st = con.createStatement();
        	
			rs = st.executeQuery("SELECT id " +
								 "FROM `" + PlotMe.databasePrefix + "plots` " +
								 "WHERE " +
									"finishdate IS NOT NULL AND finishdate>0 AND finishdate<=" + String.valueOf(currentTime));

			List<Integer> tmpList = new ArrayList<Integer>();
			
			while (rs.next())
			{
				if (rs.getInt(1) > 0)
				{
					tmpList.add(rs.getInt(1));
				}
			}
			
			return tmpList;
    	}
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "EXCEPTION occurred while fetching list of finished plots:");
        	PlotMe.logger.severe("  " + ex.getMessage());
        	return null;
        } 
        finally 
        {
            try 
            {
                if (st != null) 
                {
                    st.close();
                }
            } catch (SQLException ex) {}
        }
    }
    
    /**
     * @return: ArrayList of EXPIRED Plot IDs
     */
    public static void removeExpiredPlots()
    {
    	con = getConnection();
    	if (con == null)
    	{
    		return;
    	}

    	Statement st = null;
   	    ResultSet rs = null;
   	    
   	    long currentTime = Math.round(System.currentTimeMillis() / 1000);

        try {
        	st = con.createStatement();
        	
			rs = st.executeQuery("SELECT id " +
								 "FROM `" + PlotMe.databasePrefix + "plots` " +
								 "WHERE " +
										"expiredate IS NOT NULL" +
									" AND " +
										"expiredate>0" + 
									" AND " +
										"expiredate<=" + String.valueOf(currentTime) +
									" AND " +
											"(finishdate IS NULL" +
										" OR " +
											"finishdate<=0)" +
									" AND " +
											"(auction IS NULL" +
										" OR " +
											"auction<=0)" +
								" ORDER BY expireddate ASC"
								);
			Plot tmpPlot;
			while (rs.next())
			{
				if (rs.getInt(1) > 0)
				{
					tmpPlot = PlotManager.getPlot(rs.getInt(1));
					if (tmpPlot!=null)
					{
						tmpPlot.unsetOwner();
					}
				}
			}
    	}
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "EXCEPTION occurred while removing expired plots!");
        	PlotMe.logger.severe("  " + ex.getMessage());
        } 
        finally 
        {
            try 
            {
                if (st != null) 
                {
                    st.close();
                }
            } catch (SQLException ex) {}
        }
    }
    
    /**
     * @param: playerId
     * @return: ArrayList of PAIRS of PLOT ID and RIGHTS
     */
    public static List<Pair<Integer, Integer>> getPlotRights(int playerId)
    {
    	if (playerId < 1)
    	{
    		return null;
    	}
    	
    	con = getConnection();
    	if (con == null)
    	{
    		return null;
    	}

    	Statement st = null;
   	    ResultSet rs = null;

        try {
        	st = con.createStatement();
        	
			rs = st.executeQuery("SELECT plot, rights " +
								 "FROM `" + PlotMe.databasePrefix + "rights` " +
								 "WHERE " +
										"player=" + String.valueOf(playerId)
								);

			List<Pair<Integer, Integer>> tmpList = new ArrayList<Pair<Integer, Integer>>();
			
			while (rs.next())
			{
				if (rs.getInt(1) > 0)
				{
					tmpList.add(new Pair<Integer, Integer>(rs.getInt(1), rs.getInt(2)));
				}
			}
			
			return tmpList;
    	}
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "EXCEPTION occurred while fetching list of expired plots!");
        	PlotMe.logger.severe("  " + ex.getMessage());
        	return null;
        } 
        finally 
        {
            try 
            {
                if (st != null) 
                {
                    st.close();
                }
            } catch (SQLException ex) {}
        }
    }

    public static void savePlot(Plot plot)
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
            ps = conn.prepareStatement("INSERT OR REPLACE INTO `" + PlotMe.databasePrefix + "plots` (id, world, xpos, zpos, owner, biome, claimprice, expiredate, finishdate, auction) VALUES (?,?,?,?,?,?,?,?,?,?)");
            
            ps.setInt(1, plot.getId());
            ps.setInt(2, plot.getPlotWorld().getId());
            ps.setInt(3, plot.getPlotX());
            ps.setInt(4, plot.getPlotZ());
            
            // OWNER
            if (plot.getOwner() != null && plot.getOwner().getId() >= 0)
            {
            	ps.setInt(5, plot.getOwner().getId());
            }
            else
            {
            	ps.setNull(5, java.sql.Types.INTEGER);
            }
            
            // BIOME
			if (plot.getBiome() != null)
			{
				String biomestr = plot.getBiome().toString();
				if (biomestr != null)
				{
					ps.setString(6, biomestr);
				}
				else
				{
					ps.setNull(6, java.sql.Types.VARCHAR);
				}
			}
			else
			{
				ps.setNull(6, java.sql.Types.VARCHAR);
			}
            // PRICE
            ps.setDouble(9, plot.getClaimPrice());
            
            // EXPIRATION
            ps.setLong(7, plot.getExpireDate());
            
            // FINISH
            ps.setLong(8, plot.getFinishDate());
            
            // AUCTION
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

            ps = conn.prepareStatement("UPDATE `" + PlotMe.databasePrefix + "plots` SET world=?, xpos=?, zpos=? WHERE id=? LIMIT 1");
            
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
    
    public static void updatePropertiesCell(int rowId, String databaseSuffix, String colName, Jakky89Properties cellValue)
    {
        PreparedStatement ps = null;
        Connection conn;
        try 
        {
            conn = getConnection();

            ps = conn.prepareStatement("UPDATE `" + PlotMe.databasePrefix + databaseSuffix + "` SET " + colName + "=? WHERE id=?");
            
            if (cellValue != null)
            	ps.setObject(1, cellValue);
            else
            	ps.setNull(1, java.sql.Types.BLOB);
            ps.setInt(2, rowId);
            
            ps.executeUpdate();
            conn.commit();
                        
        } 
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "EXCEPTION occurred while updating database row data:");
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
    
    public static void updateStringCell(int rowId, String databaseSuffix, String colName, String cellValue)
    {
        PreparedStatement ps = null;
        Connection conn;
        try 
        {
            conn = getConnection();

            ps = conn.prepareStatement("UPDATE `" + PlotMe.databasePrefix + databaseSuffix + "` SET " + colName + "=? WHERE id=?");
            
            if (cellValue != null)
            	ps.setString(1, cellValue);
            else
            	ps.setNull(1, java.sql.Types.VARCHAR);
            ps.setInt(2, rowId);
            
            ps.executeUpdate();
            conn.commit();
                        
        } 
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "EXCEPTION occurred while updating database row data:");
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
    
    public static void updateFloatCell(int rowId, String databaseSuffix, String colName, Float cellValue)
    {
        PreparedStatement ps = null;
        Connection conn;
        try 
        {
            conn = getConnection();

            ps = conn.prepareStatement("UPDATE `" + PlotMe.databasePrefix + databaseSuffix + "` SET " + colName + "=? WHERE id=?");
            
            if (cellValue != null)
            	ps.setFloat(1, cellValue);
            else
            	ps.setNull(1, java.sql.Types.FLOAT);
            ps.setInt(2, rowId);
            
            ps.executeUpdate();
            conn.commit();
                        
        } 
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "EXCEPTION occurred while updating database row data:");
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
    
    public static void updateIntegerCell(int rowId, String databaseSuffix, String colName, Integer cellValue)
    {
        PreparedStatement ps = null;
        Connection conn;
        try 
        {
            conn = getConnection();

            ps = conn.prepareStatement("UPDATE `" + PlotMe.databasePrefix + databaseSuffix + "` SET " + colName + "=? WHERE id=?");
            
            if (cellValue != null)
            	ps.setInt(1, cellValue);
            else
            	ps.setNull(1, java.sql.Types.INTEGER);
            ps.setInt(2, rowId);
            
            ps.executeUpdate();
            conn.commit();
                        
        } 
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "EXCEPTION occurred while updating database row data:");
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
    
    public static int getNextAuctionNumber()
    {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        
        try
        {
	        con = getConnection();
		    st = con.createStatement();
		    rs = st.executeQuery("SELECT MAX(auction) FROM `" + PlotMe.databasePrefix + "auctions`");
		    if (rs.next())
		    {
		    	return rs.getInt(1)+1;
		    }
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
        return 0;
    }

    private static void addPlotBid(int auctionNumber, long bidDate, int auctionPlotId, int playerId, double amount)
    {
    	PreparedStatement ps = null;
    	//Auctions
        try 
        {
            con = getConnection();
            
            ps = con.prepareStatement("INSERT INTO `auctions` (auction,date,plot,player,amount) VALUES (?,?,?,?,?)");
            
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
    

    private static boolean clearPlotBids(int plotId)
    {
        Statement st = null;
        try 
        {
            con = getConnection();
            st = con.createStatement();
            st.executeUpdate("DELETE FROM `" + PlotMe.databasePrefix + "plotauctions` WHERE plot=" + String.valueOf(plotId));
            con.commit();
            return true;
        } 
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "EXCEPTION occurred while trying to delete auction bids of plot " + String.valueOf(plotId) + ":");
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
            }
            catch (SQLException e) {}
        }
    }
    
    public static void clearPlotBids(Plot plot)
    {
    	clearPlotBids(plot.getId());
    }

    
    
    private static Integer addPlotComment(int plotId, int playerId, int messageType, String messageText)
    {
    	if (plotId<=0 || playerId<=0 || messageType<0 || messageText == null)
    	{
    		return null;
    	}
    	messageText = messageText.trim();
    	if (messageText.isEmpty())
    	{
    		return null;
    	}
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	Integer id = null;
    	//Comments
        try 
        {
            con = getConnection();
            ps = con.prepareStatement("INSERT INTO `" + PlotMe.databasePrefix + "plotcomments` (plot, player, type, message) VALUES (?,?,?,?)");
            ps.setInt(1, plotId);
            ps.setInt(2, playerId);
            ps.setInt(3, messageType);
            ps.setString(4, messageText);
            
            if (ps.executeUpdate() == 1)
            {
            	rs = ps.getGeneratedKeys();
            	if (rs.next())
            	{
            		id = rs.getInt(1);
            		if (id <= 0 || rs.next())
            		{
            			id = null;
            		}
            	}
            	else
            	{
            		id = null;
            	}
            }
            if (id != null)
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
        	return null;
        } 
        finally 
        {
            try 
            {
                if (ps != null) 
                {
                    ps.close();
                }
            }
            catch (SQLException e) {}
        }
    }
    
    public static Integer addPlotComment(Plot plot, PlotPlayer player, String message)
    {
    	return addPlotComment(plot.getId(), player.getId(), 0, message);
    }

    // For security reasons we take both, plotId and commentId
    private static boolean deletePlotComment(int plotId, int commentId)
    {
        Statement st = null;
        try
        {
        	con = getConnection();
        	st = con.createStatement();
        	st.executeUpdate("DELETE FROM `" + PlotMe.databasePrefix + "plotcomments` WHERE id=" + String.valueOf(commentId) + " AND plot=" + String.valueOf(plotId));
            con.commit();
            return true;
        }
        catch (SQLException ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception :");
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
            }
            catch (SQLException ex) {}
        }
    }
    
    public static void deletePlotComment(Plot plot, int commentId)
    {
    	deletePlotComment(plot.getId(), commentId);
    }
    
	private static boolean removePlot(int plotId)
	{
		if (plotId <= 0)
		{
			return false;
		}
        Statement st = null;
        String idStr = String.valueOf(plotId);
        try 
        {
            con = getConnection();
            st = con.createStatement();
            st.addBatch("DELETE FROM `" + PlotMe.databasePrefix + "plots` WHERE id=" + idStr);
            st.addBatch("DELETE FROM `" + PlotMe.databasePrefix + "plotrights` WHERE plot=" + idStr);
            st.addBatch("DELETE FROM `" + PlotMe.databasePrefix + "plotauctions` WHERE plot=" + idStr);
            st.addBatch("DELETE FROM `" + PlotMe.databasePrefix + "plotcomments` WHERE plot=" + idStr);
            return batchExecuteCommitOrRollback(st);
        } 
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "SQLEXCEPTION occurred while plot removal process:");
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
            }
            catch (SQLException e) {}
        }
	}
	
	public static boolean removePlot(Plot plot)
	{
		return removePlot(plot.getId());
	}
 
}
