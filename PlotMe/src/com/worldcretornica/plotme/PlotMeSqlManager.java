package com.worldcretornica.plotme;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Location;

import com.worldcretornica.plotme.utils.Jakky89Properties;



public class PlotMeSqlManager {

	private static Connection conn = null;
	
	public final static String sqlitedb = "/plots.db";
	
	
    public static Connection initialize()
    {
        try
        {
        	if (PlotMe.usemySQL)
        	{
        		Class.forName("com.mysql.jdbc.Driver");
        		conn = DriverManager.getConnection(PlotMe.mySQLconn, PlotMe.mySQLuname, PlotMe.mySQLpass);
        	}
        	else
        	{
        		Class.forName("org.sqlite.JDBC");
        		conn = DriverManager.getConnection("jdbc:sqlite:" + PlotMe.configpath + "/plots.db");
        	}
        	conn.setAutoCommit(false);
        }
        catch (SQLException ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + " SQL exception on initialize:");
        	PlotMe.logger.severe("  " + ex.getMessage());
        }
        catch (ClassNotFoundException ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + " You need the SQLite/MySQL library!");
        	PlotMe.logger.severe("  " + ex.getMessage());
        }
        catch (Exception ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + " Exception occurred while initializing database connection :");
        	PlotMe.logger.severe("  " + ex.getMessage());
        }
        createTable();
        return conn;
    }
    
    public static String getSchema()
    {
    	String conn = PlotMe.mySQLconn;
    	if (conn.lastIndexOf("/") > 0)
    	{
    		return conn.substring(conn.lastIndexOf("/") + 1);
    	}
    	return "";
    }
    
    public static Connection getConnection()
    {
		if (conn == null)
		{
			conn = initialize();
		}
		if (PlotMe.usemySQL)
		{
			try
			{
				if (!conn.isValid(10))
				{
					conn = initialize();
				}
			} 
			catch (SQLException ex) 
			{
				PlotMe.logger.severe(PlotMe.PREFIX + " Failed establishing SQL database connection :");
				PlotMe.logger.severe("  " + ex.getMessage());
			}
		}
		return conn;
	}

    public static void closeConnection() {
		if (conn != null)
		{
			try
			{
				if (PlotMe.usemySQL)
				{
					if (conn.isValid(10))
					{
						conn.close();
					}
				}
				else
				{
					conn.close();
				}
				conn = null;
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
            		conn.commit();
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
	        		conn.rollback();
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
            		if (!st.isClosed())
            		{
            			st.close();
            		}
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
    
    public static void loadPlots(PlotWorld plotWorld, Integer centerX, Integer centerZ, Integer range)
    {
    	Connection conn = getConnection();
    	Statement st;
		try {
			st = conn.createStatement();
			ResultSet setPlots = st.executeQuery("SELECT id, xpos, zpos, owner, playername, biome," +
												 		"expireddate, finisheddate, customprice," +
												 		"isforsale, isprotected, isauctionned, properties " +
												 "FROM plotme_plots " +
												 "INNER JOIN plotme_players " +
												 "ON plotme_players.id=plotme_plots.owner " +
												 "INNER JOIN plotme_worlds " +
												 "ON plotme_worlds.id=plotme_plots.world " +
												 "WHERE world=" + String.valueOf(plotWorld.getId()) + " " +
												 		"AND (xpos BETWEEN "+String.valueOf(centerX-range)+" AND "+String.valueOf(centerX+range)+") " +
												 		"AND (zpos BETWEEN "+String.valueOf(centerZ-range)+" AND "+String.valueOf(centerZ+range)+")");
			while (setPlots.next()) 
			{
				int id = setPlots.getInt(1);
				int tX = setPlots.getInt(2);
				int tZ = setPlots.getInt(3);
				PlotOwner owner = new PlotOwner(setPlots.getInt(4), setPlots.getString(5));
				String biome = setPlots.getString(6);
				long expireddate = setPlots.getLong(7);
				long finisheddate = setPlots.getLong(8);
				double customprice = setPlots.getDouble(9);
				boolean isforsale = setPlots.getBoolean(10);
				boolean isprotected = setPlots.getBoolean(11);
				boolean isauctionned = setPlots.getBoolean(12);
				
				Plot plot = new Plot(
	    				id,
	    				plotWorld,
	    				tX, tZ,
	    				owner,
	    				biome,
	    				expireddate,
	    				finisheddate,
	    				customprice,
	    				isforsale,
	    				isprotected,
	    				isauctionned
	    		);
	
				PlotManager.registerPlot(plot);
				
				PlotMeSqlManager.loadPlotProperties(plot);
			}
		} catch (SQLException ex) {
			PlotMe.logger.severe(PlotMe.PREFIX + "ERROR while loading plots from database :");
			PlotMe.logger.severe("  " + ex.getMessage());
		}
    }
    
    public static void loadPlots(PlotWorld pw, Location loc, Integer bRange)
    {
    	loadPlots(pw, loc.getBlockX(), loc.getBlockZ(), bRange);
    }

    private static void createTable()
    {
		//PlotMe.logger.info(PlotMe.PREFIX + " Creating database tables when needed ...");
    		
   		PlotDatabaseUpdater.updateDatabase();
		
		if (PlotMe.usemySQL)
		{
			//PlotMe.logger.info(PlotMe.PREFIX + "Modifying database for MySQL support ...");
			File sqliteFile = new File(PlotMe.configpath + sqlitedb);
			if (sqliteFile.exists())
			{
				PlotMe.logger.info(PlotMe.PREFIX + "Trying to import plots from " + sqlitedb + " ...");
				
        		Connection sqliteCon = null;
        		Statement stSqlite = null;
            	try
            	{
            		sqliteCon = DriverManager.getConnection("jdbc:sqlite:" + PlotMe.configpath + sqlitedb);
	        		sqliteCon.setAutoCommit(false);

	        		stSqlite = sqliteCon.createStatement();

	        		PlotMe.logger.info(PlotMe.PREFIX + " Imported plots from " + sqlitedb);
	        		PlotMe.logger.info(PlotMe.PREFIX + " Renaming " + sqlitedb + " to " + sqlitedb + ".old");
	        		if (!sqliteFile.renameTo(new File(PlotMe.configpath, sqlitedb + ".old"))) 
	        		{
	        			PlotMe.logger.warning(PlotMe.PREFIX + " Failed to rename " + sqlitedb + "! Please rename this manually!");
	    			}
            	}
            	catch (SQLException ex) 
            	{
            		PlotMe.logger.severe(PlotMe.PREFIX + " Create Table Exception :");
            		PlotMe.logger.severe("  " + ex.getMessage());
            	} 
            	finally
            	{
            		try {
	                    if (stSqlite != null)
	                    {
	                    	stSqlite.close();
	                    }
	    				if (sqliteCon != null)
	    				{
	        				sqliteCon.close();
	    				}
            		}
	    	    	catch (SQLException ex) 
	    	    	{
	    	    		PlotMe.logger.severe(PlotMe.PREFIX + " EXCEPTION occurred while closing statement :");
	    	    		PlotMe.logger.severe("  " + ex.getMessage());
	    	    	}
            	}
			}
		}
    }
    
    public static void insertPlot(Plot plot)
    {
        PreparedStatement ps = null;
        Connection conn;
        //Plots
        try 
        {
            conn = getConnection();

            ps = conn.prepareStatement("INSERT INTO plotme_plots (id, world, xpos, zpos, owner, biome, expireddate, finisheddate, customprice, isforsale, isprotected, isauctionned) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
            
            ps.setInt(1, plot.id);
            ps.setInt(2, plot.plotpos.w.id);
            ps.setInt(3, plot.plotpos.x);
            ps.setInt(4, plot.plotpos.z);
            ps.setInt(5, plot.owner.id);
            ps.setString(6, plot.biome.toString());
            ps.setLong(7, plot.expireddate);
            ps.setLong(8, plot.finisheddate);
            ps.setDouble(9, plot.customprice);
            ps.setBoolean(10, plot.isforsale);
            ps.setBoolean(11, plot.isprotected);
            ps.setBoolean(12, plot.isauctionned);
            
            ps.executeUpdate();
            conn.commit();
        } 
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + " EXCEPTION occurred while inserting plot data :");
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
            } 
            catch (SQLException ex) 
            {
            	PlotMe.logger.severe(PlotMe.PREFIX + " EXCEPTION occurred while closing prepared statement :");
            	PlotMe.logger.severe("  " + ex.getMessage());
            }
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

            ps = conn.prepareStatement("UPDATE plotme_plots SET " + colName + "=? WHERE id=? LIMIT 1");
            
            ps.setObject(1, cellValue);
            ps.setInt(2, plot.id);
            
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
            } 
            catch (SQLException ex) 
            {
            	PlotMe.logger.severe(PlotMe.PREFIX + " Insert Exception (on close) :");
            	PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }
    
    public static void savePlotProperties(Plot plot)
    {
        Connection conn = null;
        PreparedStatement ps = null;

        try
        {
        	// properties blob
	        conn = getConnection();
		    ps = conn.prepareStatement("UPDATE plotme_plots SET properties=? WHERE id=? LIMIT 1");
		         
	        // just setting the class name
		    ps.setInt(1, plot.id);
	        ps.setObject(2, plot.properties);
	        ps.executeUpdate();
	        conn.commit();
        }
	    catch (SQLException ex) 
	    {
	      	PlotMe.logger.severe(PlotMe.PREFIX + "Database plot properties insert exception :");
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
	        } 
	        catch (SQLException ex) 
	        {
	        	PlotMe.logger.severe(PlotMe.PREFIX + "Exception while closing prepared statement for plot properties :");
	            PlotMe.logger.severe("  " + ex.getMessage());
	        }
        }
    }
    
    public static void loadPlotProperties(Plot plot)
    {
        Connection con = null;
        PreparedStatement ps = null;
        Jakky89Properties desero = null;
    	
        try
        {
        	// properties blob
	        con = getConnection();
	        
		    ps = con.prepareStatement("SELECT properties FROM plotme_plots WHERE id=(?) LIMIT 1");
		    
		    ps.setInt(1, plot.id);
		    
		    ResultSet rs = ps.executeQuery();
		    if (rs.next())
		    {
    		    byte[] buf = rs.getBytes(1);
    		    ObjectInputStream oin = null;
    		    if (buf != null)
    		    {
    		    	oin = new ObjectInputStream(new ByteArrayInputStream(buf));
    		    }
				try {
					desero = (Jakky89Properties)oin.readObject();
				} catch (ClassNotFoundException ex) {
		        	PlotMe.logger.severe(PlotMe.PREFIX + "Error while loading plot properties object!");
					PlotMe.logger.severe("  " + ex.getMessage());
				}
    		    rs.close();
    		    ps.close();
    		    plot.properties = desero;
		    }
        }
        catch (Exception ex)
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "Error while loading plot properties object!");
			PlotMe.logger.severe("  " + ex.getMessage());
		}
        finally
        {
        	if (ps != null)
        	{
        		try {
					ps.close();
				} catch (SQLException ex) {
		        	PlotMe.logger.severe(PlotMe.PREFIX + "Error while closing prepared statement in properties loader!");
					PlotMe.logger.severe("  " + ex.getMessage());
				}
        	}
        }
    }

    public static void addPlotBid(String player, double bid, int idX, int idZ, String world)
    {
    	PreparedStatement ps = null;
        Connection conn;
        
    	//Auctions
        try 
        {
            conn = getConnection();
            
            ps = conn.prepareStatement("INSERT INTO plotmeAuctions (idX, idZ, player, world, bid) " +
					   "VALUES (?,?,?,?,?)");
            
            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setString(3, player);
            ps.setString(4, world.toLowerCase());
            ps.setDouble(5, bid);
            
            ps.executeUpdate();
            conn.commit();
            
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
            } 
            catch (SQLException ex) 
            {
            	PlotMe.logger.severe(PlotMe.PREFIX + " Insert Exception (on close) :");
            	PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }
    
    public static void addPlotComment(String[] comment, int commentid, int idX, int idZ, String world)
    {
    	PreparedStatement ps = null;
        Connection conn;
        
    	//Comments
        try 
        {
            conn = getConnection();
            
            ps = conn.prepareStatement("INSERT INTO plotmeComments (idX, idZ, commentid, player, comment, world) " +
					   "VALUES (?,?,?,?,?,?)");
            
            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setInt(3, commentid);
            ps.setString(4, comment[0]);
            ps.setString(5, comment[1]);
            ps.setString(6, world.toLowerCase());
            
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
            } catch (SQLException ex) 
            {
            	PlotMe.logger.severe(PlotMe.PREFIX + " Insert Exception (on close) :");
            	PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }
    
    public static void deletePlot(int idX, int idZ, String world) {
    	if (world != null) {
	        Statement st = null;
	        boolean sqlError = false;
	        try {
	        	world = world.replaceAll("'", "''");
	        	String sqlCmdSuffix = "WHERE idX="+String.valueOf(idX)+" and idZ="+String.valueOf(idZ)+" and LOWER(world)="+world;
	            Connection conn = getConnection();
	            conn.setAutoCommit(false);
	            st = conn.createStatement();
	            st.addBatch("DELETE FROM plotmeComments "+sqlCmdSuffix);
	            st.addBatch("DELETE FROM plotmeAllowed "+sqlCmdSuffix);
	            st.addBatch("DELETE FROM plotmePlots "+sqlCmdSuffix);
	            int[] updateCounts = st.executeBatch();
	            for (int i=0; i<updateCounts.length; i++) {
	                if (updateCounts[i] == Statement.EXECUTE_FAILED) {
	                	sqlError = true;
	                	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception (on executing SQL statement) : updateCount=Statement.EXECUTE_FAILED");
	                    break;
	                }
	            }
	            if (sqlError) {
	            	try {
	            		conn.rollback();
	            	} catch (SQLException ex) {
	            		PlotMe.logger.severe(PlotMe.PREFIX + " Rollback exception : ");
	            		PlotMe.logger.severe("  " + ex.getMessage());
	            	} finally {
	            		PlotMe.logger.info(PlotMe.PREFIX + " Rolled back database record deletion.");
	            	}
	            } else {
	            	try {
	            		conn.commit();
	            	} catch (SQLException ex) {
	            		PlotMe.logger.severe(PlotMe.PREFIX + " Commit Exception : ");
	            		PlotMe.logger.severe("  " + ex.getMessage());
	            	}
	            }
	        } catch (SQLException ex) {
	        	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception :");
	        	PlotMe.logger.severe("  " + ex.getMessage());
	        } finally {
	            try {
	                if (st != null) {
	                    st.close();
	                }
	            } catch (SQLException ex) {
	            	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception (on close) :");
	            	PlotMe.logger.severe("  " + ex.getMessage());
	            }
	        }
    	}
    }
    
    public static void deletePlotComment(int plotId, int commentId) {
        PreparedStatement ps = null;
        ResultSet set = null;
        try {
            Connection conn = getConnection();
            ps = conn.prepareStatement("DELETE FROM plotmeComments WHERE plotId=? AND commentId=?");
            ps.setInt(1, plotId);
            ps.setInt(2, commentId);
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException ex) {
        	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception :");
        	PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException ex) {
            	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception (on close) :");
            	PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }
    
    public static void deletePlotAllowed(int id, String playerName) {
        PreparedStatement ps = null;
        try {
            Connection conn = getConnection();
            ps = conn.prepareStatement("DELETE FROM plotmeAllowed WHERE id=? AND player=?");
            ps.setInt(1, id);
            ps.setString(2, playerName);
            ps.executeUpdate();
            conn.commit();
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

    public static void deletePlotAllAllowed(int id) {
        PreparedStatement ps = null;
        ResultSet set = null;
        try {
            Connection conn = getConnection();
            ps = conn.prepareStatement("DELETE FROM plotmeAllowed WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException ex) {
        	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception :");
        	PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (set != null) {
                	set.close();
                }
            } catch (SQLException ex) {
            	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception (on close) :");
            	PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }
    
    public static void deletePlotDenied(int idX, int idZ, String player, String world) {
        PreparedStatement ps = null;
        ResultSet set = null;
        try {
            Connection conn = getConnection();
            ps = conn.prepareStatement("DELETE FROM plotmeDenied WHERE idX = ? and idZ = ? and player = ? and LOWER(world) = ?");
            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setString(3, player);
            ps.setString(4, world);
            ps.executeUpdate();
            conn.commit();
            
        } catch (SQLException ex) {
        	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception :");
        	PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException ex) {
            	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception (on close) :");
            	PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }
   
    public static void deletePlotAllDenied(int idX, int idZ, String world) {
        PreparedStatement ps = null;
        ResultSet set = null;
        try {
            Connection conn = getConnection();
            ps = conn.prepareStatement("DELETE FROM plotmeDenied WHERE idX = ? and idZ = ? and LOWER(world) = ?");
            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setString(4, world);
            ps.executeUpdate();
            conn.commit();
            
        } catch (SQLException ex) {
        	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception :");
        	PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException ex) {
            	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception (on close) :");
            	PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }
    
    public static void deletePlotBid(int idX, int idZ, String player, String world) {
        PreparedStatement ps = null;
        ResultSet set = null;
        try {
            Connection conn = getConnection();
            ps = conn.prepareStatement("DELETE FROM plotmeAuctions WHERE idX = ? and idZ = ? and player = ? and LOWER(world) = ?");
            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setString(3, player);
            ps.setString(4, world);
            ps.executeUpdate();
            conn.commit();
            
        } catch (SQLException ex) {
        	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception :");
        	PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (ps != null) 
                {
                    ps.close();
                }
                if (set != null) 
                {
                    set.close();
                }
            } 
            catch (SQLException ex) 
            {
            	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception (on close) :");
            	PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }
    
    public static void deleteAllPlotBids(int idX, int idZ, String world)
    {
        PreparedStatement ps = null;
        
        try 
        {
            Connection conn = getConnection();

            ps = conn.prepareStatement("DELETE FROM plotmeAuctions WHERE idX = ? and idZ = ? and LOWER(world) = ?");
            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setString(3, world);
            ps.executeUpdate();
            conn.commit();
            
        } 
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception :");
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
            } 
            catch (SQLException ex) 
            {
            	PlotMe.logger.severe(PlotMe.PREFIX + " Delete Exception (on close) :");
            	PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }

	public static void removePlot(Plot plot) {
        PreparedStatement ps = null;
        try 
        {
            Connection conn = getConnection();

            ps = conn.prepareStatement("DELETE FROM plotme_plots WHERE id=?");
            ps.setInt(1, plot.getId());
            ps.executeUpdate();
            conn.commit();
            
        } 
        catch (SQLException ex) 
        {
        	PlotMe.logger.severe(PlotMe.PREFIX + "Plot removal database exception :");
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
            } 
            catch (SQLException ex) 
            {
            	PlotMe.logger.severe(PlotMe.PREFIX + "Plot removal database exception (on close) :");
            	PlotMe.logger.severe("  " + ex.getMessage());
            }
        }	
	}
 
}
