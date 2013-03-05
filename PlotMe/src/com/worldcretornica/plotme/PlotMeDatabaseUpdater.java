package com.worldcretornica.plotme;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class PlotMeDatabaseUpdater
{
	
    final static String LAYOUT_WORLD_TABLE	=	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotme_worlds` " +
	    										"("
	    											+ "`id` UNSIGNED INTEGER NOT NULL PRIMARY KEY AUTO INCREMENT,"
	    											+ "`worldname` VARCHAR(64) NOT NULL UNIQUE" +
	    										");";
    
    final static String LAYOUT_PLAYER_TABLE	=	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotme_players` " + 
	    							 			"("
	    							 	  			+ "`id` UNSIGNED INTEGER NOT NULL PRIMARY KEY AUTO INCREMENT,"
	    							 	  			+ "`playername` VARCHAR(32) NOT NULL UNIQUE"
	    							 	  			+ "`displayname` VARCHAR(32) DEFAULT NULL" +
	    							 	  		");";
  
    final static String LAYOUT_PLOT_TABLE	=	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotme_plots` " +
	    						  		 		"("
	    						  		  			+ "`id` UNSIGNED INTEGER NOT NULL PRIMARY KEY AUTO INCREMENT,"
		    						  		  		+ "`world` UNSIGNED INTEGER,"
		    						  		  		+ "`xpos` INTEGER,"
		    						  		  		+ "`zpos` INTEGER,"
		    						  		  		+ "`owner` UNSIGNED INTEGER DEFAULT NULL,"
		    						  		  		+ "`biome` VARCHAR(16) DEFAULT NULL,"
		    						  		  		+ "`expireddate` UNSIGNED INTEGER DEFAULT NULL,"
				    						  		+ "`finisheddate` UNSIGNED INTEGER DEFAULT NULL,"
				    						  		+ "`price` DOUBLE DEFAULT 0,"
				    						  		+ "`isforsale` UNSIGNED TINYINT(1) NOT NULL DEFAULT 1,"
				    						  		+ "`isprotected` UNSIGNED TINYINT(1) NOT NULL DEFAULT 0,"
				    						  		+ "`auction` UNSIGNED INTEGER DEFAULT NULL,"
				    						  		+ "`properties` BLOB DEFAULT NULL, "
				    						  		+ "UNIQUE (world, xpos, zpos)" +
				    						  	");";
    
    final static String LAYOUT_AUCTIONS_TABLE = "CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotme_auctions` " +
		 										"("
		 											+ "`id` UNSIGNED INTEGER NOT NULL PRIMARY KEY AUTO INCREMENT,"
		 											+ "`date` UNSIGNED INTEGER NOT NULL,"
		 											+ "`auction` UNSIGNED INTEGER NOT NULL,"
		 											+ "`plot` UNSIGNED INTEGER NOT NULL,"
		 											+ "`player` UNSIGNED INTEGER NOT NULL,"
		 											+ "`amount` UNSIGNED INTEGER NOT NULL,"
		 											+ "`typeid` UNSIGNED SMALLINT DEFAULT NULL,"
		 											+ "`datavalue` UNSIGNED TINYINT DEFAULT NULL," +
		 										");";
    
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
		 										");";*/

    final static String LAYOUT_COMMENT_TABLE =	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotme_comments` " +
	    									 	"("
	    									 		+ "`id` UNSIGNED INTEGER NOT NULL PRIMARY KEY AUTO INCREMENT,"
	    									 		+ "`plot` UNSIGNED INTEGER NOT NULL,"
	    									 		+ "`player` UNSIGNED INTEGER NOT NULL,"
	    									 		+ "`type` UNSIGNED TINYINT(1) NOT NULL DEFAULT 0,"
	    									 		+ "`message` TEXT" +
	    									 	");";
    
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
		 										");";*/

    final static String LAYOUT_INFO_TABLE	=	"CREATE TABLE IF NOT EXISTS `" + PlotMe.databasePrefix + "plotme_info` " + 
    											"("
	    								 			+ "`key` VARCHAR(32) NOT NULL PRIMARY KEY,"
	    								 			+ "`value` TEXT DEFAULT NULL" +
	    								 		");";

	public PlotMeDatabaseUpdater()
	{
		
	}
	
	private static Connection getConnection()
	{
		return PlotMeDatabaseManager.getConnection();
	}
	
	public static Double getTablesVersion()
	{
		Connection con     = null;
		Statement  st  	   = null;
		ResultSet  infoset = null;
		
		try
		{
			con = PlotMeDatabaseManager.getConnection();
			if (con == null)
			{
				return null;
			}
			
			st  = con.createStatement();
			if (st == null)
			{
				return null;
			}
			
	    	infoset = st.executeQuery("SELECT value FROM plotme_info WHERE key='VERSION'");
	    	if (infoset != null && infoset.next())
	    	{
	   			try
	    		{
	    			return Double.valueOf(infoset.getString(1));
	    		}
	    		catch (NumberFormatException ex)
	    		{
	    			PlotMe.logger.warning(PlotMe.PREFIX + "Could not convert plugin version to double! Not autoupdating changes.");
	    		}
	    	}
	    	else
	    	{
				PlotMe.logger.warning(PlotMe.PREFIX + "Could not get previous plugin version from database!");
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
        
        Double fromVersion = getTablesVersion();
        Double toVersion = null;

        try
		{
			toVersion = Double.valueOf(PlotMe.VERSION);
		}
		catch (NumberFormatException ex)
		{
			PlotMe.logger.warning(PlotMe.PREFIX + "Could not convert plugin version to double! Not autoupdating changes.");
			return false;
    	}
        
        if (fromVersion != null && fromVersion == toVersion)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "You are using the newest version. No database updates needed.");
        	return true;
        }
       
		try
		{
	        con = getConnection();
			if (con == null)
			{
				PlotMe.logger.severe(PlotMe.PREFIX + "Could not establish database connection!");
				return false;
			}
			
			st  = con.createStatement();
			if (st == null)
			{
				PlotMe.logger.severe(PlotMe.PREFIX + "Could not create database statement!");
				return false;
			}
			
	        try
	        {
	        	con.setAutoCommit(false);
	        	st.addBatch(LAYOUT_WORLD_TABLE);
	        	st.addBatch(LAYOUT_PLAYER_TABLE);
	        	st.addBatch(LAYOUT_PLOT_TABLE);
	        	st.addBatch(LAYOUT_COMMENT_TABLE);
	        	st.addBatch(LAYOUT_INFO_TABLE);
	        	
	        	st.addBatch("INSERT INTO `plotme_info` VALUES()");
	        	
	   			if (PlotMeDatabaseManager.batchExecuteCommitOrRollback(st))
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
	        
			return true;
		} catch (SQLException ex) {
        	PlotMe.logger.severe(PlotMe.PREFIX + " Update table exception (on close) :");
        	PlotMe.logger.severe("  " + ex.getMessage());
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
			} 
			catch (SQLException ex) 
			{
				PlotMe.logger.severe(PlotMe.PREFIX + "Could not close database statement ressource :");
				PlotMe.logger.severe("  " + ex.getMessage());
			}
		}
		return false;

		/**
         * TODO: Update from older versions
         */
		
        /*try
        {
   			// Save current layout in table
   			st.addBatch("INSERT INTO `plotme_info` VALUES('LAYOUT_WORLDS_TABLE', '" + LAYOUT_WORLD_TABLE + "')");
   			st.addBatch("INSERT INTO `plotme_info` VALUES('LAYOUT_PLAYERS_TABLE', '" + LAYOUT_PLAYER_TABLE + "')");
   			st.addBatch("INSERT INTO `plotme_info` VALUES('LAYOUT_PLOTS_TABLE', '" + LAYOUT_PLOT_TABLE + "')");
   			st.addBatch("INSERT INTO `plotme_info` VALUES('LAYOUT_COMMENTS_TABLE', '" + LAYOUT_COMMENT_TABLE + "')");
   			st.addBatch("INSERT INTO `plotme_info` VALUES('LAYOUT_INFO_TABLE', '" + LAYOUT_INFO_TABLE + "')");
   			
   			if (!PlotMeSqlManager.batchExecuteCommitOrRollback(st))
   			{
   				PlotMe.logger.severe(PlotMe.PREFIX + " Error while trying to create the needed database tables!");
   				return;
   			}
	    	set = statement.executeQuery("SELECT infovalue FROM plotmeInfo WHERE infokey='version'");
	    	if (set.next()) {
	    		set.getInt(0);
	    	} else {
	    		needsUpdate = true;
	    		fromVersion = 8;
	    	}
	       	set.close();
	      	conditionIterator = updateConditions.iterator();
	        while (conditionIterator.hasNext())
	        {
	        	Pair<String, String> conditionPair = conditionIterator.next();
	        	set = statement.executeQuery(conditionPair.getLeft());
	        	if (!set.next())
	        	{
	        		statement.addBatch(conditionPair.getRight());
	        	}
	        }
        }
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + " Update table exception :");
        	PlotMe.logger.severe("  " + ex.getMessage());
        } 
        finally 
        {
            try 
            {
                if (statement != null)
                	statement.close();
                if (set != null)
                	set.close();
            } 
            catch (SQLException ex) 
            {
            	PlotMe.logger.severe(PlotMe.PREFIX + " Update table exception (on close) :");
            	PlotMe.logger.severe("  " + ex.getMessage());
            }
        }*/
    }
}
