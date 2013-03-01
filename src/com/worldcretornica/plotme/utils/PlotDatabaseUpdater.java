package com.worldcretornica.plotme.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.worldcretornica.plotme.PlotMe;
import com.worldcretornica.plotme.SqlManager;

public class PlotDatabaseUpdater
{

	public PlotDatabaseUpdater()
	{
		
	}
	
	public void createTables()
	{
        final String WORLD_TABLE = "CREATE TABLE IF NOT EXISTS `plotme_worlds` ("
    		    + "`id` UNSIGNED INTEGER NOT NULL PRIMARY KEY AUTO INCREMENT,"
    		    + "`worldname` VARCHAR(64) NOT NULL UNIQUE"
    	        + ");";
        final String PLAYERS_TABLE = "CREATE TABLE IF NOT EXISTS `plotme_players` ("
    		    + "`id` UNSIGNED INTEGER NOT NULL PRIMARY KEY AUTO INCREMENT,"
    		    + "`playername` VARCHAR(32) NOT NULL UNIQUE"
    	        + ");";
        final String PLOT_TABLE = "CREATE TABLE IF NOT EXISTS `plotme_plots` ("
    		    + "`id` UNSIGNED INTEGER NOT NULL PRIMARY KEY AUTO INCREMENT,"
    		    + "`world` UNSIGNED INTEGER,"
        		+ "`xpos` INTEGER,"
    		    + "`zpos` INTEGER,"
    		    + "`owner` VARCHAR(32) DEFAULT NULL,"
    	        + "`biome` VARCHAR(16) NOT NULL DEFAULT 'PLAINS',"
    	        + "`expireddate` UNSIGNED INTEGER DEFAULT NULL,"
    	        + "`finisheddate` UNSIGNED INTEGER DEFAULT NULL,"
    	        + "`customprice` UNSIGNED DOUBLE DEFAULT NULL,"
    	        + "`isforsale` UNSIGNED TINYINT(1) NOT NULL DEFAULT 0,"
    	        + "`isprotected` UNSIGNED TINYINT(1) NOT NULL DEFAULT 1,"
    	        + "`isauctionned` UNSIGNED TINYINT(1) NOT NULL DEFAULT 0,"
    	        + "UNIQUE (world, xpos, zpos)"
    	        + ");";
        final String COMMENT_TABLE = "CREATE TABLE IF NOT EXISTS `plotme_plot_comments` ("
    	    	+ "`id` UNSIGNED INTEGER NOT NULL PRIMARY KEY AUTO INCREMENT,"
        		+ "`plot` UNSIGNED INTEGER NOT NULL INDEX,"
    		    + "`player` UNSIGNED INTEGER NOT NULL,"
        		+ "`type` UNSIGNED TINYINT(1) NOT NULL DEFAULT 0,"
    		    + "`comment` TEXT"
    	    	+ ");";
        final String INFO_TABLE = "CREATE TABLE IF NOT EXISTS `plotme_info` ("
        		+ "`key` VARCHAR(32) NOT NULL PRIMARY KEY,"
        		+ "`value`"
        		+ ");";
        
        Connection con = null;
        Statement st = null;
        ResultSet infoset = null;
        
        try
        {
        	
	        con = SqlManager.getConnection();
	        if (con == null)
	        {
   				PlotMe.logger.severe(PlotMe.PREFIX + "Could not get database connection! Needed database tables could not be created!");
   				return;
	        }
	        con.setAutoCommit(false);

	        st = con.createStatement();
	        if (st == null)
	        {
   				PlotMe.logger.severe(PlotMe.PREFIX + "Could not create database statement! Needed database tables could not be created!");
   				return;
	        }
    		st.addBatch(WORLD_TABLE);
    		st.addBatch(PLAYERS_TABLE);
    		st.addBatch(PLOT_TABLE);
   			st.addBatch(COMMENT_TABLE);
   			st.addBatch(INFO_TABLE);
   			if (!SqlManager.batchExecuteCommitOrRollback(st))
   			{
   				PlotMe.logger.severe(PlotMe.PREFIX + "Could not create needed database tables!");
   				return;
   			}
   			if (!st.isClosed())
   			{
   				st.close();
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
                if (st != null && !st.isClosed())
                {
                	st.close();
                }
                if (infoset != null && !infoset.isClosed())
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
	}
	
    public void UpdateTables()
    {
        Connection con = null;
        Statement st = null;
        ResultSet infoset = null;
    	
		int fromVersion = 8;
		int toVersion = PlotMe.VERSION_NUMBER;
		
   		
		st = con.createStatement();
    	infoset = st.executeQuery("SELECT value FROM plotme_info WHERE key='VERSION'");
    	if (infoset != null)
    	{
    		if (infoset.next()) {
    			infoset.getInt(1);
    		}
    	}
       	st.close();
    	
    	
    	String schema = SqlManager.getSchema();
        Statement statement = null;
        ResultSet set = null;
        String queryPrefix;
        final String PLOT_TABLE = "CREATE TABLE IF NOT EXISTS `plotme_plots` ("
    		    + "`id` INTEGER PRIMARY KEY AUTO INCREMENT,"
        		+ "`pX` INTEGER,"
    		    + "`pZ` INTEGER,"
    		    + "`world` VARCHAR(32) NOT NULL DEFAULT '0',"
    		    + "`owner` VARCHAR(32) DEFAULT NULL,"
    	        + "`topX` INTEGER NOT NULL DEFAULT '0',"
    	        + "`bottomX` INTEGER NOT NULL DEFAULT '0',"
    	        + "`topZ` INTEGER NOT NULL DEFAULT '0',"
    	        + "`bottomZ` INTEGER NOT NULL DEFAULT '0',"
    	        + "`biome` VARCHAR(32) NOT NULL DEFAULT '0',"
    	        + "`expireddate` DATETIME NULL,"
    	        + "`finished` BOOLEAN NOT NULL DEFAULT '0',"
    	        + "`customprice` UNSIGNED DOUBLE NOT NULL DEFAULT '0',"
    	        + "`forsale` BOOLEAN NOT NULL DEFAULT '0',"
    	        + "`finisheddate` VARCHAR(16) NULL,"
    	        + "`protected` BOOLEAN NOT NULL DEFAULT '0',"
    	        + "`auctionned` BOOLEAN NOT NULL DEFAULT '0',"
    	        + "`auctionenddate` VARCHAR(16) NULL,"
    	        + "`currentbid` DOUBLE NOT NULL DEFAULT '0',"
    	        + "`currentbidder` VARCHAR(32) NULL,"
    	        + "PRIMARY KEY (idX, idZ, world) "
    	        + ");";
        final String COMMENT_TABLE = "CREATE TABLE IF NOT EXISTS `plotmeComments` ("
    	    	+ "`idX` INTEGER,"
    		    + "`idZ` INTEGER,"
    	    	+ "`world` VARCHAR(32) NOT NULL,"
    		    + "`commentid` INTEGER,"
    		    + "`player` VARCHAR(32) NOT NULL,"
    		    + "`comment` TEXT,"
    	        + "PRIMARY KEY (idX, idZ, world, commentid) "
    	    	+ ");";
        final String ALLOWED_TABLE = "CREATE TABLE IF NOT EXISTS `plotmeAllowed` ("
        		+ "`idX` INTEGER,"
        	    + "`idZ` INTEGER,"
        	    + "`world` VARCHAR(32) NOT NULL,"
        	    + "`player` VARCHAR(32) NOT NULL,"
    	        + "PRIMARY KEY (idX, idZ, world, player) "
        	    + ");";
        final String DENIED_TABLE = "CREATE TABLE IF NOT EXISTS `plotmeDenied` ("
        		+ "`idX` INTEGER,"
        	    + "`idZ` INTEGER,"
        	    + "`world` VARCHAR(32) NOT NULL,"
        	    + "`player` VARCHAR(32) NOT NULL,"
    	        + "PRIMARY KEY (idX, idZ, world, player)"
        	    + ");";
        final String INFO_TABLE = "CREATE TABLE IF NOT EXISTS `plotmeInfo` ("
        		+ "`infokey`,"
        		+ "`infovalue`"
        		+ "PRIMARY KEY (infokey)"
        		+ ");";
        try
        {
	        Connection conn = SqlManager.getConnection();
	        Statement st = conn.createStatement();
	        boolean needsUpdate = false;
	        int fromVersion;
	        int toVersion;
	        conn.setAutoCommit(false);
	        //
    		st = conn.createStatement();
    		st.addBatch(PLOT_TABLE);
   			st.addBatch(COMMENT_TABLE);
   			st.addBatch(ALLOWED_TABLE);
   			st.addBatch(DENIED_TABLE);
   			if (!SqlManager.batchExecuteCommitOrRollback(st))
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
        }
    }
}
