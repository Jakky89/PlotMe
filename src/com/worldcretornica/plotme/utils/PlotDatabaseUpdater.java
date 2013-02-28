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
	
	public static ArrayList<Pair<String, String>> updateConditions;


	public static void PlotDatabaseUpdater()
	{
		updateConditions = new ArrayList<Pair<String, String>>();
	}
	
	public static void buildMySQLConditions()
	{
		updateConditions.clear();

    	/*** START Version 0.8 changes ***/
        updateConditions.add(
			new Pair
			(
				"TABLE_NAME='plotmePlots' AND column_name='customprice'",
				"ALTER TABLE plotmePlots ADD customprice DOUBLE NOT NULL DEFAULT '0';"
			)
		);
		updateConditions.add(
				new Pair
				(
	       			"TABLE_NAME='plotmePlots' AND column_name='forsale'",
	       			"ALTER TABLE plotmePlots ADD forsale BOOLEAN NOT NULL DEFAULT '0';"
				)
		);
		updateConditions.add(
				new Pair
				(
					"TABLE_NAME='plotmePlots' AND column_name='finisheddate'",
					"ALTER TABLE plotmePlots ADD finisheddate VARCHAR(16) NULL;"
				)
		);
        updateConditions.add(
			new Pair
			(
				"TABLE_NAME='plotmePlots' AND column_name='protected'",
				"ALTER TABLE plotmePlots ADD protected BOOLEAN NOT NULL DEFAULT '0';"
			)
		);
        updateConditions.add(
			new Pair
			(
				"TABLE_NAME='plotmePlots' AND column_name='auctionned'",
				"ALTER TABLE plotmePlots ADD auctionned BOOLEAN NOT NULL DEFAULT '0';"
			)
		);

	}
	
	public static void buildSQLiteConditions()
	{
		updateConditions.clear();
		
		/*** START Version 0.8 changes ***/
		
	}
	
    public void UpdateTables()
    {
    	Iterator<Pair<String, String>> conditionIterator;
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
