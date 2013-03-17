package com.worldcretornica.plotme;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import com.worldcretornica.plotme.utils.Jakky89Properties;
import com.worldcretornica.plotme.utils.Pair;


public class PlotDatabase {

	private static Connection con = null;
	private static int nextAuctionNumber;
	
	public final static String sqlitedb = "/plots.db";
	
    final static String LAYOUT_WORLD_TABLE	=	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotme_worlds` " +
					"("
						+ "`id` UNSIGNED INTEGER NOT NULL PRIMARY KEY AUTO INCREMENT,"
						+ "`worldname` VARCHAR(64) NOT NULL UNIQUE" +
					")";

	final static String LAYOUT_PLAYER_TABLE	=	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotme_players` " + 
		 			"("
		 	  			+ "`id` UNSIGNED INTEGER NOT NULL PRIMARY KEY AUTO INCREMENT,"
		 	  			+ "`playername` VARCHAR(32) NOT NULL UNIQUE,"
		 	  			+ "`displayname` VARCHAR(32) DEFAULT NULL,"
		 	  			+ "`lastonline` UNSIGNED INTEGER DEFAULT NULL" +
		 	  		")";
	
	final static String LAYOUT_PLOT_TABLE	=	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotme_plots` " +
				 	"("
				  		+ "`id` UNSIGNED INTEGER NOT NULL PRIMARY KEY AUTO INCREMENT,"
		  		  		+ "`world` UNSIGNED INTEGER,"
		  		  		+ "`xpos` INTEGER,"
		  		  		+ "`zpos` INTEGER,"
		  		  		+ "`owner` UNSIGNED INTEGER DEFAULT NULL INDEX,"
		  		  		+ "`biome` VARCHAR(16) DEFAULT NULL,"
		  		  		+ "`expiredate` UNSIGNED INTEGER DEFAULT NULL,"
				  		+ "`finishdate` UNSIGNED INTEGER DEFAULT NULL,"
				  		+ "`claimprice` UNSIGNED FLOAT DEFAULT NULL,"
				  		+ "`auction` UNSIGNED INTEGER DEFAULT NULL "
				  		+ "UNIQUE (world, xpos, zpos)" +
				  	")";
	
	final static String LAYOUT_PLOTRIGHTS_TABLE	=	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotme_plotrights` " +
		 	"("
		  		+ "`plot` UNSIGNED INTEGER NOT NULL PRIMARY KEY AUTO INCREMENT,"
  		  		+ "`type` UNSIGNED TINYINT(1) NOT NULL," // player/group
  		  		+ "`name` VARCHAR(16) NOT NULL,"
  		  		+ "`perms` VARCHAR(16) NOT NULL" +
		  	")";
	
	final static String LAYOUT_PLOTAUCTION_TABLE = "CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotme_plotauctions` " +
					"("
						+ "`date` UNSIGNED INTEGER NOT NULL,"
						+ "`auction` UNSIGNED INTEGER NOT NULL,"
						+ "`plot` UNSIGNED INTEGER NOT NULL,"
						+ "`player` UNSIGNED INTEGER NOT NULL,"
						+ "`amount` UNSIGNED FLOAT NOT NULL,"
						+ "INDEX(plot, auction)" +
					")";
	
	/*final static String LAYOUT_CHANGES_TABLE  = "CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotme_changes` " +
					"("
						+ "`id` UNSIGNED INTEGER NOT NULL PRIMARY KEY AUTO INCREMENT,"
						+ "`plot` UNSIGNED INTEGER NOT NULL,"
						+ "`relx` INTEGER NOT NULL,"
						+ "`rely` INTEGER NOT NULL,"
						+ "`relz` INTEGER NOT NULL,"
						+ "`player` UNSIGNED INTEGER NOT NULL,"
						+ "`fromblockid` UNSIGNED SMALLINT NOT NULL,"
						+ "`fromblockvalue` UNSIGNED TINYINT NOT NULL DEFAULT 0,"
						+ "`toblockid` UNSIGNED SMALLINT NOT NULL,"
						+ "`toblockvalue` UNSIGNED TINYINT NOT NULL DEFAULT 0,"
						+ "`binarydata` BLOB DEFAULT NULL" +
					")";*/
	
	final static String LAYOUT_PLOTCOMMENT_TABLE =	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotme_plotcomments` " +
				 	"("
				 		+ "`id` UNSIGNED INTEGER NOT NULL PRIMARY KEY AUTO INCREMENT,"
				 		+ "`plot` UNSIGNED INTEGER NOT NULL,"
				 		+ "`player` UNSIGNED INTEGER NOT NULL,"
				 		+ "`type` UNSIGNED TINYINT(2) NOT NULL DEFAULT 0,"
				 		+ "`message` TEXT" +
				 	")";
	
	/*final static String LAYOUT_ROOMS_TABLE =	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotme_rooms` " +
					"("
						+ "`id` UNSIGNED INTEGER NOT NULL PRIMARY KEY AUTO INCREMENT,"
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
	
	final static String LAYOUT_INFO_TABLE	=	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotme_info` " + 
				"("
		 			+ "`key` VARCHAR(32) NOT NULL PRIMARY KEY,"
		 			+ "`value` TEXT DEFAULT NULL" +
		 		");";
	
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
	
	
	public static String getTablesVersion()
	{
		Connection con     = null;
		Statement  st  	   = null;
		ResultSet  infoset = null;
		
		try
		{
			con = PlotDatabase.getConnection();
			if (con == null)
			{
				return null;
			}
			
			st  = con.createStatement();
			if (st == null)
			{
				return null;
			}
			
	    	infoset = st.executeQuery("SELECT value FROM plotme_info WHERE key='PLOTME_VERSION'");
	    	if (infoset != null && infoset.next())
	    	{
	   			try
	    		{
	    			return infoset.getString(1);
	    		}
	    		catch (NumberFormatException ex)
	    		{
	    			PlotMe.logger.warning(PlotMe.PREFIX + "Could not convert plugin version to double! Not autoupdating changes.");
	    		}
	    	}
	    	else
	    	{
				PlotMe.logger.warning(PlotMe.PREFIX + "Could not get previous plugin version from database! Assuming previous version was 0.13");
				return null;
	    	}
		}
		catch (Exception ex)
		{
			PlotMe.logger.warning(PlotMe.PREFIX + "Could not get previous plugin version from database!");
		}
		finally
		{
            try 
            {
                if (st != null)
                {
                	st.close();
                }
                if (infoset != null)
                {
                	infoset.close();
                }
            } 
            catch (SQLException ex) 
            {
            	PlotMe.logger.severe(PlotMe.PREFIX + " Update table exception (on close) :");
            	PlotMe.logger.severe("  " + ex.getMessage());
            }
		}
		return null;
	}

	public static boolean updateDatabase()
    {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        
        String fromVersion = getTablesVersion();
        
        if (fromVersion != null && fromVersion == PlotMe.VERSION)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "You are using the newest version. No database updates needed.");
        	return true;
        }
       
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
	        	con.setAutoCommit(false);
	        	st.addBatch(LAYOUT_WORLD_TABLE);
	        	st.addBatch(LAYOUT_PLAYER_TABLE);
	        	st.addBatch(LAYOUT_PLOT_TABLE);
	        	st.addBatch(LAYOUT_PLOTRIGHTS_TABLE);
	        	st.addBatch(LAYOUT_PLOTAUCTION_TABLE);
	        	st.addBatch(LAYOUT_PLOTCOMMENT_TABLE);
	        	st.addBatch(LAYOUT_INFO_TABLE);
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
	        
	        try
	        {
	        	ps = con.prepareStatement("INSERT OR REPLACE INTO `" + PlotMe.databasePrefix + "plotme_info` (key, value) VALUES(?,?)");
	        	ps.setString(1, "LAYOUT_WORLD_TABLE");
	        	ps.setString(2, LAYOUT_WORLD_TABLE);
	        	ps.execute();
	        	ps.setString(1, "LAYOUT_PLAYER_TABLE");
	        	ps.setString(2, LAYOUT_PLAYER_TABLE);
	        	ps.execute();
	        	ps.setString(1, "LAYOUT_PLOT_TABLE");
	        	ps.setString(2, LAYOUT_PLOT_TABLE);
	        	ps.execute();
	        	ps.setString(1, "LAYOUT_PLOTRIGHTS_TABLE");
	        	ps.setString(2, LAYOUT_PLOTRIGHTS_TABLE);
	        	ps.execute();
	        	ps.setString(1, "LAYOUT_PLOTAUCTION_TABLE");
	        	ps.setString(2, LAYOUT_PLOTAUCTION_TABLE);
	        	ps.execute();
	        	ps.setString(1, "LAYOUT_PLOTCOMMENT_TABLE");
	        	ps.setString(2, LAYOUT_PLOTCOMMENT_TABLE);
	        	ps.execute();
	        }
	        catch (SQLException ex)
	        {
	        	PlotMe.logger.severe(PlotMe.PREFIX + "SQLEXCEPTION occurred while inserting current table layout:");
	        	PlotMe.logger.severe("  " + ex.getMessage());
	        }
	        finally
	        {
				if (ps != null)
				{
					ps.close();
				}
	        }
        	
	        try
	        {
	        	if (fromVersion == null)
	        	{
	        		st = con.createStatement();
	        		try
	        		{
	        			rs = st.executeQuery("SELECT * FROM plotmePlots LIMIT 5");
		        		if (rs.next())
		        		{
		        			fromVersion = "0.13";
		        		}
	        		}
	        		catch (SQLException ex) {}
	        	}
	        	
	        	if (fromVersion.equals("0.13"))
	        	{
	        		PlotMe.logger.info(PlotMe.PREFIX + "Converting database from version 0.13 to the new table layout ...");
	        		st.addBatch("INSERT INTO `" + PlotMe.databasePrefix + "plotme_worlds` (worldname) SELECT DISTINCT world FROM plotmePlots");
	        		st.addBatch("INSERT INTO `" + PlotMe.databasePrefix + "plotme_players` (playername) SELECT DISTINCT owner FROM plotmePlots");
	        		st.addBatch("INSERT INTO `" + PlotMe.databasePrefix + "plotme_players` (playername) SELECT DISTINCT player FROM `plotmeAllowed`");
	        		st.addBatch("INSERT INTO `" + PlotMe.databasePrefix + "plotme_players` (playername) SELECT DISTINCT player FROM `plotmeDenied`");
	        		st.addBatch("INSERT INTO `" + PlotMe.databasePrefix + "plotme_players` (playername) SELECT DISTINCT player FROM `plotmeComments`");
	        		st.addBatch("INSERT INTO `" + PlotMe.databasePrefix + "plotme_plots` (world, xpos, zpos, owner, biome, expiredate, finishdate, claimprice, protected) SELECT (SELECT id FROM `" + PlotMe.databasePrefix + "plotme_worlds` WHERE `" + PlotMe.databasePrefix + "plotme_worlds`.worldname LIKE plotmePlots.world), bottomX, bottomZ, (SELECT id FROM plotme_players WHERE `" + PlotMe.databasePrefix + "plotme_players`.playername LIKE plotmePlots.owner), biome, DATEDIFF(plotmePlots.expireddate, '19700101', GETDATE()), DATEDIFF(plotmePlots.finished, '19700101', GETDATE()), customprice, forsale, protected FROM plotmePlots, `" + PlotMe.databasePrefix + "plotme_plots`");
	        		st.addBatch("INSERT INTO `" + PlotMe.databasePrefix + "plotme_plotcomments` (plot, from, message) SELECT (SELECT id FROM `" + PlotMe.databasePrefix + "plotme_plots`, `plotmeComments` WHERE `" + PlotMe.databasePrefix + "plotme_plots`.xpos=plotmeComments.idX AND `" + PlotMe.databasePrefix + "plotme_plots`.zpos=plotmeComments.idZ AND `" + PlotMe.databasePrefix + "plotme_plots`.world=(SELECT id FROM `" + PlotMe.databasePrefix + "plotme_worlds` WHERE worldname LIKE `plotmeComments`.world), (SELECT id` FROM `" + PlotMe.databasePrefix + "plotme_players` WHERE `" + PlotMe.databasePrefix + "`.playername LIKE `plotmeComments`.player), comment");
	        		if (PlotDatabase.batchExecuteCommitOrRollback(st))
	        		{
	        			PlotMe.logger.info(PlotMe.PREFIX + "Success!");
	        			return true;
	        		}
	        		else
	        		{
	        			PlotMe.logger.severe(PlotMe.PREFIX + "Could not convert old table contents to new table layout!");
	        			return false;
	        		}
	        	}
	        }
	        catch (SQLException ex)
	        {
	        	PlotMe.logger.severe(PlotMe.PREFIX + "SQLEXCEPTION occurred while updating database tables:");
	        	PlotMe.logger.severe("  " + ex.getMessage());
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
			} catch (SQLException ex) {}
		}
    }
    
    public static PlotWorld getPlotWorld(World bukkitWorld)
    {
    	if (bukkitWorld == null)
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
            st = con.prepareStatement("SELECT id, worldname FROM `" + PlotMe.databasePrefix + "plotme_worlds` WHERE worldname=?");
            st.setString(1, bukkitWorld.getName());
            st.executeQuery();
            rs = st.getResultSet();
            if (rs.next())
            {
            	worldId = rs.getInt(1);
            	PlotMe.logger.info(PlotMe.PREFIX + "Requested world \"" + bukkitWorld.getName() + "\" with id " + String.valueOf(worldId));
            }
            else
            {
	            st = con.prepareStatement("INSERT INTO `" + PlotMe.databasePrefix + "plotme_worlds` (worldname) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
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
                	PlotMe.logger.severe(PlotMe.PREFIX + "World with name \"" + bukkitWorld.getName() + "\" is not unique!");
                	return null;
                }
                return new PlotWorld(worldId, bukkitWorld);
            }
            return null;
        }
        catch (SQLException ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "SQLEXCEPTION occurred while getting data for world \"" + bukkitWorld.getName() + "\" from database :");
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
    	if (plotPlayerId < 0)
    	{
    		return null;
    	}
    	
    	PlotPlayer tmpPlayer;
    	
    	tmpPlayer = PlotManager.getPlotPlayer(plotPlayerId);
    	if (tmpPlayer != null)
    	{
    		return tmpPlayer;
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
            rs = st.executeQuery("SELECT id, playername, displayname, lastonline FROM `" + PlotMe.databasePrefix + "plotme_players` WHERE id=" + String.valueOf(plotPlayerId) + " LIMIT 1");
            rs = st.getResultSet();
            if (rs.next())
            {
               	return new PlotPlayer(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4));
            }
        	PlotMe.logger.severe(PlotMe.PREFIX + "Got no result from database while loading plot player with id " + String.valueOf(plotPlayerId) + "!");
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
    
    public static PlotPlayer getPlotPlayer(int plotPlayerId)
    {
    	if (plotPlayerId < 0)
    	{
    		return null;
    	}
    	
    	PlotPlayer tmpPlayer;
    	
    	tmpPlayer = PlotManager.getPlotPlayer(plotPlayerId);
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
            st = con.prepareStatement("SELECT playername, displayname, lastonline FROM `" + PlotMe.databasePrefix + "plotme_players` WHERE id=? LIMIT 1");
            st.setInt(1, plotPlayerId);
            
            st.executeQuery();
            
            rs = st.getResultSet();
            
            if (rs.next())
            {
                // error when we found more than one world with that name (should normally never happen)
                if ( rs.getString(2) != null && ! rs.getString(2).isEmpty())
                {
                	return new PlotPlayer(playerId, rs.getString(1), rs.getString(2), rs.getInt(3));
                }
                else
                {
                	return new PlotPlayer(playerId, rs.getString(1), rs.getInt(2));
                }
            }
            return null;
        }
        catch (SQLException ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "EXCEPTION occurred while getting data for player with id \"" + plotPlayerId + "\" from database!");
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
    	
    	tmpPlayer = PlotManager.getPlotPlayer(playerName);
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
            st = con.prepareStatement("SELECT id, playername, displayname, lastonline FROM `" + PlotMe.databasePrefix + "plotme_players` WHERE playername=? LIMIT 1");
            st.setString(1, playerName);
            
            st.executeQuery();
            rs = st.getResultSet();
            if (rs.next())
            {
            	playerId = rs.getInt(1);
            	if (displayName != null)
            	{
            		updateStringCell(playerId, "players", "displayname", displayName);
            	}
            	else
            	{
            		displayName = rs.getString(2);
            	}
            	PlotMe.logger.info(PlotMe.PREFIX + "PlotMe player \"" + playerName + "\" has id " + String.valueOf(playerId));
            }
            else
            {
	            st = con.prepareStatement("INSERT INTO `" + PlotMe.databasePrefix + "plotme_players` (playername) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
	            st.setString(1, playerName);
	            if (st.executeUpdate() != 1) {
	                return null;
	            }
	            rs = st.getGeneratedKeys();
	            if (rs.next()) {
	            	playerId = rs.getInt(1);
	            	PlotMe.logger.info(PlotMe.PREFIX + "Created new entry with id " + String.valueOf(playerId) + " for player \"" + playerName + "\" in PlotMe database " + PlotMe.databasePrefix + "plotme_players");
	            }
	            else
	            {
	            	return null;
	            }
            }
            if (playerId != null && playerId >= 0)
            {
                // error when we found more than one world with that name (should normally never happen)
                if (rs.next())
                {
                	PlotMe.logger.severe(PlotMe.PREFIX + "PlotMe player \"" + playerName + "\" is not unique in database!");
                	return null;
                }
                if (displayName != null && !displayName.isEmpty())
                {
                	updateStringCell(playerId, "players", "displayname", displayName);
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
    
    public static PlotPlayer getPlotPlayer(Player bukkitPlayer)
    {
    	return getPlotPlayer(bukkitPlayer.getName(), bukkitPlayer.getDisplayName());
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
								 "FROM `" + PlotMe.databasePrefix + "plotme_plots` " +
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
								 "FROM `" + PlotMe.databasePrefix + "plotme_plots` " +
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
    
    
    public static void loadPlots(PlotWorld plotWorld, final int centerPlotX, final int centerPlotZ, final int plotRange)
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

    	int minX = centerPlotX - plotRange;
    	int minZ = centerPlotZ - plotRange;
    	int maxX = centerPlotX + plotRange;
    	int maxZ = centerPlotZ + plotRange;
    	
    	int id;
    	PlotPosition plotpos;
    	Plot plot;
    	Statement st = null;
    	ResultSet rs = null;
    	byte[] buf;

		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT id,xpos,zpos,owner,biome," +
										"claimprice,expiredate,finishdate " +
								 "FROM `" + PlotMe.databasePrefix + "plotme_plots` " +
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
				if (rs.wasNull()) {
					biome = PlotMe.DEFAULT_PLOT_BIOME;
				} else {
					biome = Biome.valueOf(biomestr);
				}
				
				if (plotWorld.getPlotAtPlotPosition(plotpos) == null)
				{
					plot = new Plot(
						rs.getInt(1),
						plotpos,
		    			getPlotPlayer(rs.getInt(4)),
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
			ps = con.prepareStatement("INSERT INTO `" + PlotMe.databasePrefix + "plotme_plots` (world,xpos,zpos) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
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
        	PlotMe.logger.severe(PlotMe.PREFIX + "Error while getting data for plots at " + String.valueOf(centerPlotX) + "," + String.valueOf(centerPlotZ) + " (range " + String.valueOf(plotRange) + ") from database:");
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
    
    public static void loadPlots(PlotWorld plotWorld, final Location centerBlockLocation, final int range)
    {
    	loadPlots(plotWorld, centerBlockLocation.getBlockX(), centerBlockLocation.getBlockZ(), range);
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
								 "FROM `" + PlotMe.databasePrefix + "plotme_plots` " +
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
    public static List<Integer> getExpiredPlots()
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
								 "FROM `" + PlotMe.databasePrefix + "plotme_plots` " +
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
   	    
   	    long currentTime = Math.round(System.currentTimeMillis() / 1000);

        try {
        	st = con.createStatement();
        	
			rs = st.executeQuery("SELECT plot, rights " +
								 "FROM `" + PlotMe.databasePrefix + "plotme_rights` " +
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
            ps = conn.prepareStatement("INSERT OR REPLACE INTO `" + PlotMe.databasePrefix + "plotme_plots` (id, world, xpos, zpos, owner, biome, claimprice, expiredate, finishdate, auction) VALUES (?,?,?,?,?,?,?,?,?,?)");
            
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
            ps.setLong(7, plot.getExpiration());
            
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
    
    public static void updatePropertiesCell(int rowId, String databaseSuffix, String colName, Jakky89Properties cellValue)
    {
        PreparedStatement ps = null;
        Connection conn;
        try 
        {
            conn = getConnection();

            ps = conn.prepareStatement("UPDATE `" + PlotMe.databasePrefix + "plotme_" + databaseSuffix + "` SET " + colName + "=? WHERE id=?");
            
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

            ps = conn.prepareStatement("UPDATE `" + PlotMe.databasePrefix + "plotme_" + databaseSuffix + "` SET " + colName + "=? WHERE id=?");
            
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

            ps = conn.prepareStatement("UPDATE `" + PlotMe.databasePrefix + "plotme_" + databaseSuffix + "` SET " + colName + "=? WHERE id=?");
            
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

            ps = conn.prepareStatement("UPDATE `" + PlotMe.databasePrefix + "plotme_" + databaseSuffix + "` SET " + colName + "=? WHERE id=?");
            
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
		    rs = st.executeQuery("SELECT MAX(auction) FROM `" + PlotMe.databasePrefix + "plotme_auctions`");
		    while (rs.next())
		    {
		    	if (rs.getInt(1) >= nextAuctionNumber)
		    	{
		    		nextAuctionNumber = rs.getInt(1);
		    		nextAuctionNumber ++;
		    	}
		    }
		    return nextAuctionNumber;
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
    

    private static boolean clearPlotBids(int plotId)
    {
        Statement st = null;
        try 
        {
            con = getConnection();
            st = con.createStatement();
            st.executeUpdate("DELETE FROM `" + PlotMe.databasePrefix + "plotme_plotauctions` WHERE plot=" + String.valueOf(plotId));
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
            ps = con.prepareStatement("INSERT INTO `" + PlotMe.databasePrefix + "plotme_plotcomments` (plot, player, type, message) VALUES (?,?,?,?)");
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
        	st.executeUpdate("DELETE FROM `" + PlotMe.databasePrefix + "plotme_plotcomments` WHERE id=" + String.valueOf(commentId) + " AND plot=" + String.valueOf(plotId));
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
            st.addBatch("DELETE FROM `" + PlotMe.databasePrefix + "plotme_plots` WHERE id=" + idStr);
            st.addBatch("DELETE FROM `" + PlotMe.databasePrefix + "plotme_plotrights` WHERE plot=" + idStr);
            st.addBatch("DELETE FROM `" + PlotMe.databasePrefix + "plotme_plotauctions` WHERE plot=" + idStr);
            st.addBatch("DELETE FROM `" + PlotMe.databasePrefix + "plotme_plotcomments` WHERE plot=" + idStr);
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
