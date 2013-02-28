package com.worldcretornica.plotme;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.worldcretornica.plotme.utils.PlotDatabaseUpdater;
import com.worldcretornica.plotme.utils.PlotPosition;

public class SqlManager {

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
    
    public static void loadPlots(PlotWorld pw, Integer pX, Integer pZ, Integer range)
    {
    	Connection conn = getConnection();
    	Statement st;
		try {
			st = conn.createStatement();
			ResultSet setPlots = st.executeQuery("SELECT * FROM plotme_plots WHERE world=" + String.valueOf(pw.id) + " AND (xpos BETWEEN "+String.valueOf(pX-range)+" AND "+String.valueOf(pX+range)+") AND (zpos BETWEEN "+String.valueOf(pZ-range)+" AND "+String.valueOf(pZ+range)+")");

			int size = 0;

			while (setPlots.next()) 
			{
				size++;
				int id = setPlots.getInt("id");
				PlotWorld pwi = PlotManager.getCreatePlotWorld(setPlots.getInt("world"));
				int tX = setPlots.getInt("xpos");
				int tZ = setPlots.getInt("zpos");
				
				String owner = setPlots.getString("owner");
				String biome = setPlots.getString("biome");
				long expireddate = setPlots.getLong("expireddate");
				HashMap<String, Map<String, Object>> plprops = new HashMap<String, Map<String, Object>>();
				double customprice = setPlots.getDouble("customprice");
				double rentprice = setPlots.getDouble("rentprice");
				long rentlastpaid = setPlots.getLong("rentlastpaid");
				boolean isforsale = setPlots.getBoolean("forsale");
				long finisheddate = setPlots.getLong("finisheddate");
				boolean isprotected = setPlots.getBoolean("isprotected");
				boolean isauctionned = setPlots.getBoolean("isauctionned");
				String properties = setPlots.getString("properties");
	
				Plot plot = new Plot(
	    				id,
	    				pwi,
	    				tX, tZ,
	    				owner,
	    				biome,
	    				expireddate,
	    				customprice,
	    				isforsale,
	    				finisheddate,
	    				isprotected,
	    				isauctionned
	    		);
	
				PlotManager.registerPlot(plot);
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
    
    private static void savePlotProperty(String type, String property, String value)
    {
    	
    }

    private static void createTable()
    {
    	Statement st = null;
    	try
    	{
    		//PlotMe.logger.info(PlotMe.PREFIX + " Creating database tables when needed ...");
    		
   			PlotDatabaseUpdater pdu = new PlotDatabaseUpdater();
   			// UPDATE TABLES
   			pdu.UpdateTables();
   			
    		if (PlotMe.usemySQL)
    		{ 
    			//PlotMe.logger.info(PlotMe.PREFIX + " Modifying database for MySQL support ...");
    			File sqlitefile = new File(PlotMe.configpath + sqlitedb);
    			if (sqlitefile.exists())
    			{
    				PlotMe.logger.info(PlotMe.PREFIX + " Trying to import plots from plots.db ...");
	        		Class.forName("org.sqlite.JDBC");
	        		Connection sqliteconn = DriverManager.getConnection("jdbc:sqlite:" + PlotMe.configpath + sqlitedb);
	        		sqliteconn.setAutoCommit(false);

	        		Statement slstatement = sqliteconn.createStatement();
	        		ResultSet setPlots = slstatement.executeQuery("SELECT * FROM plotmePlots");

	        		PlotMe.logger.info(PlotMe.PREFIX + " Imported " + size + " plots from " + sqlitedb);
	        		PlotMe.logger.info(PlotMe.PREFIX + " Renaming " + sqlitedb + " to " + sqlitedb + ".old");
	        		if (!sqlitefile.renameTo(new File(PlotMe.configpath, sqlitedb + ".old"))) 
	        		{
	        			PlotMe.logger.warning(PlotMe.PREFIX + " Failed to rename " + sqlitedb + "! Please rename this manually!");
	    			}
	        		if (slstatement != null)
        				slstatement.close();
        			if (setPlots != null)
        				setPlots.close();
        			if (setComments != null)
                    	setComments.close();
                    if (setAllowed != null)
                    	setAllowed.close();
    				if (sqliteconn != null)
        				sqliteconn.close();
    			}
    		}
    	} 
    	catch (SQLException ex) 
    	{
    		PlotMe.logger.severe(PlotMe.PREFIX + " Create Table Exception :");
    		PlotMe.logger.severe("  " + ex.getMessage());
    	} 
    	catch (ClassNotFoundException ex) 
    	{
    		PlotMe.logger.severe(PlotMe.PREFIX + " You need the SQLite library :");
    		PlotMe.logger.severe("  " + ex.getMessage());
    	} 
    	finally 
    	{
    		try {
    			if (st != null) 
    			{
    				st.close();
    			}
    		} 
    		catch (SQLException ex) 
    		{
    			PlotMe.logger.severe(PlotMe.PREFIX + " EXCEPTION occurred while closing statement :");
    			PlotMe.logger.severe("  " + ex.getMessage());
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

            ps = conn.prepareStatement("INSERT INTO plotme_plots (id, world, xpos, zpos, owner, biome, customprice, isforsale, isprotected, isauctionned, expireddate, finisheddate) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
            ps.setInt(1, plot.id);
            ps.setInt(2, plot.plotpos.getPlotWorld().WorldId);
            ps.setInt(3, plot.plotpos.getPlotX());
            ps.setInt(4, plot.plotpos.getPlotZ());
            ps.setString(5, plot.biome.toString());
            ps.setDouble(6, plot.customprice);
            ps.setBoolean(7, plot.isforsale);
            ps.setBoolean(8, plot.isprotected);
            ps.setBoolean(9, plot.isauctionned);
            ps.setLong(10, plot.expireddate);
            ps.setLong(11, plot.finisheddate);

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
    
    public static void updatePlot(int id, String field, Object value)
    {
        PreparedStatement ps = null;
        Connection conn;
        
        //Plots
        try 
        {
            conn = getConnection();

            ps = conn.prepareStatement("UPDATE plotme_plots SET " + field + "=? WHERE id=?");
            
            ps.setObject(1, value);
            ps.setInt(2, id);
            
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
		    ps = conn.prepareStatement("UPDATE plotme_plots SET properties=? WHERE id=?");
		         
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
    
    public static PlotProperties loadPlotProperties(Plot plot)
    {
        Connection conn = null;
        PreparedStatement ps = null;
    	
        try
        {
        	// properties blob
	        conn = getConnection();
		    ps = conn.prepareStatement("SELECT FROM plotme_plots (properties) WHERE id=(?)");
		         
    	PreparedStatement ps = connection.prepareStatement(SQL_DESERIALIZE_OBJECT);
    		    pstmt.setLong(1, serialized_id);
    		    ResultSet rs = pstmt.executeQuery();
    		    rs.next();
    		 
    		    // Object object = rs.getObject(1);
    		 
    		    byte[] buf = rs.getBytes(1);
    		    ObjectInputStream objectIn = null;
    		    if (buf != null)
    		      objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
    		 
    		    Object deSerializedObject = objectIn.readObject();
    		 
    		    rs.close();
    		    pstmt.close();
    		 
    		    System.out.println("Java object de-serialized from database. Object: "
    		        + deSerializedObject + " Classname: "
    		        + deSerializedObject.getClass().getName());
    		    return deSerializedObject;
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
    
    public static HashMap<String, String> fetchPlotPlayerProperties(int plotId, String playerName)
    {
    	if (plotId > 0)
    	{
    		Map<String, String> ret = new HashMap<String, String>();
    		try {
	    		Connection conn = getConnection();
	    		PreparedStatement ps = conn.prepareStatement("SELECT properties FROM plotme_plot_player_properties WHERE plotid=? AND player=?");
	    		ps.setInt(0, plotId);
	    		ps.setString(1, playerName);
	    		ResultSet rs = ps.executeQuery();
	    		if (rs.next()) {
	    			JSONObject jsonobj = (JSONObject)new JSONParser().parse(rs.getString(0));
	    			rs.getString(0);
					rgts = EnumSet. setRights.getString("rights");
					ret.put(setRights.getString("player"), setRights.getString("rights"));
				}
				if (ps != null) {
					ps.close();
				}
    		}
    		catch
    		{
    			
    		}
    	}
    	return null;
    }
    
    public static List<Plot> getChunkPlots(Chunk cnk)
    {
    	List<Plot> ret = new ArrayList<Plot>();
    	if (cnk != null && PlotMe.plotWorlds.containsKey(cnk.getWorld().getName()))
    	{

    	}
    	return ret;
    }
    
    public static HashMap<String, Plot> getWorldPlots(String worldName)
    {
    	HashMap<String, Plot> ret = new HashMap<String, Plot>();
    	if (worldName != null && PlotMe.plotWorlds.containsKey(cnk.getWorld().getName())) {
	        Statement statementPlot = null;
	        Statement statementComment = null;
	        ResultSet setPlots = null;
			ResultSet setComments = null;
	        try {
	            Connection conn = getConnection();
	            statementPlot = conn.createStatement();
	            setPlots = statementPlot.executeQuery("SELECT * FROM plotmePlots WHERE LOWER(world)='" + worldName + "'");
	            int size = 0;
	            while (setPlots.next()) {
	            	size++;
	            	int id = setPlots.getInt("id");
	    			int pX = setPlots.getInt("idX");
	    			int pZ = setPlots.getInt("idZ");
	    			String owner = setPlots.getString("owner");
	    			int topX = setPlots.getInt("topX");
	    			int bottomX = setPlots.getInt("bottomX");
	    			int topZ = setPlots.getInt("topZ");
	    			int bottomZ = setPlots.getInt("bottomZ");
	    			String biome = setPlots.getString("biome");
	    			long expireddate = setPlots.getLong("expireddate");
	    			boolean finished = setPlots.getBoolean("finished");
	    			HashMap<String, EnumSet<PlayerRights>> rights;
	    			List<String[]> comments = new ArrayList<String[]>();
	    			double customprice = setPlots.getDouble("customprice");
	    			boolean forsale = setPlots.getBoolean("forsale");
	    			String finisheddate = setPlots.getString("finisheddate");
	    			boolean protect = setPlots.getBoolean("protected");
	    			String currentbidder = setPlots.getString("currentbidder");
	    			double currentbid = setPlots.getDouble("currentbid");
	    			boolean auctionned = setPlots.getBoolean("auctionned");

	    			statementComment = conn.createStatement();
	    			setComments = statementComment.executeQuery("SELECT * FROM plotmeComments WHERE plotid=" + id);
	    			
	    			while (setComments.next())
	    			{
	    				String[] comment = new String[2];
	    				comment[0] = setComments.getString("player");
	    				comment[1] = setComments.getString("comment");
	    				comments.add(comment);
	    			}
	    			
	    			Plot plot = new Plot
	    				(
	    					id,
	    					owner,
	    					world,
	    					topX, bottomX,
	    					topZ, bottomZ,
	    					biome,
	    					expireddate,
	    					finished,
	    					allowed,
	    					comments,
	    					customprice,
	    					forsale,
	    					finisheddate,
	    					protect,
	    					currentbidder, currentbid,
	    					auctionned,
	    					denied
	    				);
	                ret.put("" + idX + ";" + idZ, plot);
	            }
	            PlotMe.logger.info(PlotMe.PREFIX + " " + size + " plots loaded");
	        } 
	        catch (SQLException ex) 
	        {
	        	PlotMe.logger.severe(PlotMe.PREFIX + " Load Exception :");
	        	PlotMe.logger.severe("  " + ex.getMessage());
	        } 
	        finally 
	        {
	            try 
	            {
	                if (statementPlot != null)
	                {
	                	statementPlot.close();
	                }
	                if (statementAllowed != null)
	                {
	                	statementAllowed.close();
	                }
	                if (statementComment != null)
	                {
	                	statementComment.close();
	                }
	                if (setPlots != null)
	                {
	                	setPlots.close();
	                }
	                if (setComments != null)
	                {
	                	setComments.close();
	                }
	                if (setAllowed != null)
	                {
	                	setAllowed.close();
	                }
	            } 
	            catch (SQLException ex) 
	            {
	            	PlotMe.logger.severe(PlotMe.PREFIX + " Exception occurred while closing plot statements :");
	            	PlotMe.logger.severe("  " + ex.getMessage());
	            }
	        }
    	}
        return ret;
    }
 
}
