package com.worldcretornica.plotme;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.worldcretornica.plotme.Metrics.Graph;
import com.worldcretornica.plotme.listener.PlotDenyListener;
import com.worldcretornica.plotme.listener.PlotListener;
import com.worldcretornica.plotme.listener.PlotWorldEditListener;

public class PlotMe extends JavaPlugin
{

	public static String NAME;
	public static String PREFIX;
	public static String VERSION;
	public static String WEBSITE;
	
	public static Logger logger = Logger.getLogger("Minecraft");
		
	public static Boolean usemySQL;
    public static String mySQLuname;
    public static String mySQLpass;
    public static String mySQLconn;
    public static String databasePrefix;
    public static String configpath;
    public static Boolean globalUseEconomy;
    public static Boolean advancedlogging;
    public static Boolean opPermissions;
    public static Boolean useDisplayNamesOnSigns;
    public static Boolean useDisplayNamesInMessages;
    public static String language;
    public static Boolean allowWorldTeleport;
    public static Boolean autoUpdate;
    public static Boolean allowToDeny;
    
    public static final int DEFAULT_PLOT_AUTO_LIMIT = 100;
    public static final int DEFAULT_PLOT_SIZE = 32;
    public static final int DEFAULT_PATH_WIDTH = 7;
    public static final int DEFAULT_BOTTOM_BLOCK = 7;
    public static final int DEFAULT_FLOOR_BLOCK = 2;
    public static final int DEFAULT_WALL_BLOCK = 44;
    public static final int DEFAULT_FILL_BLOCK = 3;
    public static final int DEFAULT_ROAD_HEIGHT = 64;
    public static final int DEFAULT_FLOOR_1 = 5;
    public static final int DEFAULT_FLOOR_2 = 5;
    
    public static final int DEFAULT_DAYS_EXPIRATION = 7;
    
    public static WorldEditPlugin worldedit = null;
    public static Economy economy = null;
    public static Boolean usinglwc = false;
    public static Boolean usingvoxelsniper = false;
    
    private static HashSet<String> playersignoringwelimit = null;
    private static HashMap<String, String> captions;
    
    private static Boolean update = false;
    
    public static World worldcurrentlyprocessingexpired;
    public static CommandSender cscurrentlyprocessingexpired;
    public static Integer counterexpired;
    public static Integer nbperdeletionprocessingexpired;
    public static Boolean defaultWEAnywhere;
    
    protected static PlotMe self = null;
	
	public void onDisable()
	{	
		PlotMeSqlManager.closeConnection();
		PlotProtectionRemover.forceRun();
		NAME = null;
		PREFIX = null;
		VERSION = null;
		WEBSITE = null;
		
		logger = null;
		
		usemySQL = null;
		mySQLuname = null;
		mySQLpass = null;
		mySQLconn = null;
		databasePrefix = null;
		globalUseEconomy = null;
		advancedlogging = null;
		opPermissions = null;
	    useDisplayNamesOnSigns = null;
	    useDisplayNamesInMessages = null;
		language = null;
		allowWorldTeleport = null;
		autoUpdate = null;
		configpath = null;
		worldedit = null;
		economy = null;
		usinglwc = null;
		playersignoringwelimit = null;
		captions = null;
		update = null;
		worldcurrentlyprocessingexpired = null;
		cscurrentlyprocessingexpired = null;
		counterexpired = null;
		nbperdeletionprocessingexpired = null;
		defaultWEAnywhere = null;
		self = null;
		allowToDeny = null;
		
		
	}
	
	public void onEnable()
	{
		initialize();
		
		doMetric();
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlotListener(), this);
		
		if (pm.getPlugin("Vault") != null)
		{
			setupEconomy();
		}
		
		if (pm.getPlugin("WorldEdit") != null)
		{
			worldedit = (WorldEditPlugin)pm.getPlugin("WorldEdit");
			pm.registerEvents(new PlotWorldEditListener(), this);			
		}
		
		if (pm.getPlugin("LWC") != null)
		{
			usinglwc = true;
		}
		
		if (pm.getPlugin("VoxelSniper") != null)
		{
			usingvoxelsniper = true;
		}
		
		if (allowToDeny)
		{
			pm.registerEvents(new PlotDenyListener(), this);
		}
				
		getCommand("plotme").setExecutor(new PlotMeCommands(this));
				
		setupUpdater();
				
		self = this;
	}
	
	private void setupUpdater()
	{
		if (autoUpdate)
		{
			if (advancedlogging)
			{
				logger.info("Checking for a new update ...");
			}
			
			Updater updater = new Updater(this, NAME, this.getFile(), Updater.UpdateType.DEFAULT, false);
			update = updater.getResult() != Updater.UpdateResult.NO_UPDATE;
			
			if (advancedlogging)
			{
				logger.info("Update available: " + update);
			}
		}
	}
	
	private void doMetric()
	{
		try
		{
		    Metrics metrics = new Metrics(this);
		    
		    Graph graphNbWorlds = metrics.createGraph("Plot worlds");
		    
		    graphNbWorlds.addPlotter(new Metrics.Plotter("Number of plot worlds")
		    {
				@Override
				public int getValue() 
				{
					return PlotManager.plotWorlds.size();
				}
			});
		    	    
		    graphNbWorlds.addPlotter(new Metrics.Plotter("Average Plot size")
		    {
				@Override
				public int getValue() 
				{
					int avgplotsize = 0;
					if (PlotManager.plotWorlds != null)
					{
						if (!PlotManager.plotWorlds.isEmpty())
						{
							for (PlotWorld pw : PlotManager.plotWorlds.values())
							{
								avgplotsize += pw.PlotSize;
							}
							avgplotsize /= PlotManager.plotWorlds.size();
						}
					}
					return avgplotsize;
				}
		    });
		    
		    graphNbWorlds.addPlotter(new Metrics.Plotter("Number of plots")
		    {
				@Override
				public int getValue() 
				{
					return PlotManager.allPlots.size();
				}
			});
		    		    
		    metrics.start();
		} 
		catch (IOException e) 
		{
		    // Failed to submit the stats :-(
		}
	}
	
	public ChunkGenerator getDefaultWorldGenerator(Chunk chunk)
	{
		if (chunk == null)
		{
			return null;
		}

		if (PlotManager.isPlotWorld(chunk.getWorld().getName()))
		{
			return new PlotGen(PlotManager.plotWorlds.get(chunk.getWorld()).MinecraftWorld);
		}
		
		else
		{
			logger.warning(PREFIX + "Configuration not found for PlotMe world \"" + chunk.getWorld().getName() + "\"! Using defaults.");
			return new PlotGen();
		}
	}
	
	public static boolean cPerms(CommandSender sender, String node)
	{
		return sender.hasPermission(node);
	}
	
	public void initialize()
	{
		PluginDescriptionFile pdfFile = this.getDescription();
		NAME = pdfFile.getName();
		PREFIX = ChatColor.BLUE + "[" + NAME + "] " + ChatColor.RESET;
		VERSION = pdfFile.getVersion();
		WEBSITE = pdfFile.getWebsite();
		configpath = getDataFolder().getAbsolutePath();
		playersignoringwelimit = new HashSet<String>();

		if (!this.getDataFolder().exists()) 
		{
        	this.getDataFolder().mkdirs();
        }
				
		File configfile = new File(configpath, "config.yml");
		FileConfiguration config = new YamlConfiguration();
		
		try 
		{
			config.load(configfile);
		} 
		catch (FileNotFoundException e) {
			logger.severe(PREFIX + " Configuration file will be created when the rest has successfully been loaded.");
		}
		catch (IOException e) 
		{
			logger.severe(PREFIX + "can't read configuration file");
			e.printStackTrace();
		} 
		catch (InvalidConfigurationException e) 
		{
			logger.severe(PREFIX + "invalid configuration format");
			e.printStackTrace();
		}
        
        usemySQL = config.getBoolean("usemySQL", false);
		mySQLconn = config.getString("mySQLconn", "jdbc:mysql://localhost:3306/minecraft");
		mySQLuname = config.getString("mySQLuname", "root");
		mySQLpass = config.getString("mySQLpass", "password");
		databasePrefix = config.getString("databasePrefix", "");
		globalUseEconomy = config.getBoolean("globalUseEconomy", false);
		advancedlogging = config.getBoolean("AdvancedLogging", false);
		language = config.getString("Language", "english");
		opPermissions = config.getBoolean("OpPermissions", true);
		useDisplayNamesOnSigns = config.getBoolean("UseDisplayNamesOnSigns", true);
		useDisplayNamesInMessages = config.getBoolean("UseDisplayNamesInMessages", false);
		allowWorldTeleport = config.getBoolean("allowWorldTeleport", true);
		defaultWEAnywhere = config.getBoolean("defaultWEAnywhere", false);
		autoUpdate = config.getBoolean("auto-update", false);
		allowToDeny = config.getBoolean("allowToDeny", true);

		ConfigurationSection cfgWorlds;
		ConfigurationSection cfgWorld;
		String cfgWorldName;
		
		if (config.contains("worlds"))
		{
			cfgWorlds = config.getConfigurationSection("worlds");
		}
		else
		{
			cfgWorlds = config.createSection("worlds");
			
			cfgWorld = cfgWorlds.createSection("Global");

			cfgWorld.set("InheritWorld", "");
			cfgWorld.set("PlotAutoLimit", DEFAULT_PLOT_AUTO_LIMIT);
			cfgWorld.set("PathWidth", DEFAULT_PATH_WIDTH);
			cfgWorld.set("PlotSize", DEFAULT_PLOT_SIZE);
			
			cfgWorld.set("BottomBlockId", String.valueOf(DEFAULT_BOTTOM_BLOCK));
			cfgWorld.set("WallBlockId", String.valueOf(DEFAULT_WALL_BLOCK));
			cfgWorld.set("PlotFloorBlockId", String.valueOf(DEFAULT_FLOOR_BLOCK));
			cfgWorld.set("PlotFillingBlockId", String.valueOf(DEFAULT_FILL_BLOCK));
			cfgWorld.set("RoadMainBlockId", "5");
			cfgWorld.set("RoadStripeBlockId", "5:2");
			
			cfgWorld.set("RoadHeight", DEFAULT_ROAD_HEIGHT);
			cfgWorld.set("DaysToExpiration", DEFAULT_DAYS_EXPIRATION);
			cfgWorld.set("ProtectedBlocks", getDefaultProtectedBlocks());
			cfgWorld.set("PreventedItems", getDefaultPreventedItems());
			cfgWorld.set("ProtectedWallBlockId", "44:4");
			cfgWorld.set("ForSaleWallBlockId", "44:1");
			cfgWorld.set("AuctionWallBlockId", "44:1");
			cfgWorld.set("AutoLinkPlots", true);
			cfgWorld.set("DisableExplosion", true);
			
			cfgWorld.set("DisableIgnition", true);
			
			ConfigurationSection cfgWorldEconomy = cfgWorld.createSection("economy");
			
			cfgWorldEconomy.set("UseEconomy", false);
			cfgWorldEconomy.set("CanPutOnSale", false);
			cfgWorldEconomy.set("CanSellToBank", false);
			cfgWorldEconomy.set("RefundClaimPriceOnReset", false);
			cfgWorldEconomy.set("RefundClaimPriceOnSetOwner", false);
			cfgWorldEconomy.set("ClaimPrice", 0);
			cfgWorldEconomy.set("ClearPrice", 0);
			cfgWorldEconomy.set("AddPlayerPrice", 0);
			cfgWorldEconomy.set("DenyPlayerPrice", 0);
			cfgWorldEconomy.set("RemovePlayerPrice", 0);
			cfgWorldEconomy.set("UndenyPlayerPrice", 0);
			cfgWorldEconomy.set("PlotHomePrice", 0);
			cfgWorldEconomy.set("CanCustomizeSellPrice", false);
			cfgWorldEconomy.set("SellToPlayerPrice", 0);
			cfgWorldEconomy.set("SellToBankPrice", 0);
			cfgWorldEconomy.set("BuyFromBankPrice", 0);
			cfgWorldEconomy.set("AddCommentPrice", 0);
			cfgWorldEconomy.set("BiomeChangePrice", 0);
			cfgWorldEconomy.set("ProtectPrice", 0);
			cfgWorldEconomy.set("DisposePrice", 0);
			
			cfgWorld.set("economy", cfgWorldEconomy);
			
			cfgWorld = cfgWorlds.createSection("ExampleWorld");
			cfgWorld.set("InheritWorld", "Global");
			
		}
		
		Iterator<String> cfgWorldsIterator = cfgWorlds.getKeys(false).iterator();
		while (cfgWorldsIterator.hasNext())
		{
			cfgWorldName = cfgWorldsIterator.next();
			if (cfgWorldName == null || cfgWorldName.isEmpty())
			{
				logger.warning(PREFIX + "Empty world name found in config file!");
				continue;
			}
			ConfigurationSection cfgCurrWorld = cfgWorlds.getConfigurationSection(cfgWorldName);
			if (cfgCurrWorld == null)
			{
				logger.warning(PREFIX + "No configuration section for world \"" + cfgWorldName + "\" found!");
				continue;
			}
			World bukkitWorld = getServer().getWorld(cfgWorldName);
			if (bukkitWorld == null)
			{
				logger.warning(PREFIX + "World \"" + cfgWorldName + "\" is configured but doesn't exist.");
				continue;
			}
			String inhWorldName = cfgWorlds.getString(cfgWorldName + ".InheritWorld");
			if (inhWorldName != null && !inhWorldName.isEmpty())
			{
				ConfigurationSection cfgInhWorld = cfgWorlds.getConfigurationSection(inhWorldName);
				if (cfgInhWorld != null)
				{
					for (Entry<String, Object> inhVal : cfgInhWorld.getValues(true).entrySet())
					{
						cfgCurrWorld.set(inhVal.getKey(), inhVal.getValue());
					}
				}
			}
			PlotWorld tmpPlotWorld = new PlotWorld(0, bukkitWorld);
			tmpPlotWorld.PlotAutoLimit			= cfgCurrWorld.getInt("PlotAutoLimit",	DEFAULT_PLOT_AUTO_LIMIT);
			tmpPlotWorld.PlotAutoLimit 			= cfgCurrWorld.getInt("PathWidth",		DEFAULT_PATH_WIDTH);
			tmpPlotWorld.PlotAutoLimit 			= cfgCurrWorld.getInt("PlotSize",		DEFAULT_PLOT_SIZE);
			
			tmpPlotWorld.BottomBlockId 			= getBlockId(cfgCurrWorld,				"BottomBlockId",		"7:0");
			tmpPlotWorld.BottomBlockValue 		= getBlockValue(cfgCurrWorld,			"BottomBlockId",		"7:0");
			tmpPlotWorld.WallBlockId 			= getBlockId(cfgCurrWorld,				"WallBlockId",			"44:0");
			tmpPlotWorld.WallBlockValue			= getBlockValue(cfgCurrWorld,			"WallBlockId",			"44:0");
			tmpPlotWorld.PlotFloorBlockId 		= getBlockId(cfgCurrWorld,				"PlotFloorBlockId",		"2:0");
			tmpPlotWorld.PlotFloorBlockValue 	= getBlockValue(cfgCurrWorld,			"PlotFloorBlockId",		"2:0");
			tmpPlotWorld.PlotFillingBlockId		= getBlockId(cfgCurrWorld,				"PlotFillingBlockId", 	"3:0");
			tmpPlotWorld.PlotFillingBlockValue 	= getBlockValue(cfgCurrWorld,			"PlotFillingBlockId",	"3:0");
			tmpPlotWorld.RoadMainBlockId		= getBlockId(cfgCurrWorld,				"RoadMainBlockId",		"5:0");
			tmpPlotWorld.RoadMainBlockValue		= getBlockValue(cfgCurrWorld,			"RoadMainBlockId",		"5:0");
			tmpPlotWorld.RoadStripeBlockId		= getBlockId(cfgCurrWorld,				"RoadStripeBlockId",	"5:2");
			tmpPlotWorld.RoadStripeBlockValue	= getBlockValue(cfgCurrWorld,			"RoadStripeBlockId",	"5:2");
			
			tmpPlotWorld.RoadHeight				= cfgCurrWorld.getInt("RoadHeight",		cfgCurrWorld.getInt("WorldHeight", 64));
			if (tmpPlotWorld.RoadHeight > 250)
			{
				logger.severe(PREFIX + "RoadHeight above 250 is unsafe. This is the height at which your road is located. Set down to 64.");
				tmpPlotWorld.RoadHeight = 64;
			}
			
			tmpPlotWorld.DaysToExpiration			= cfgCurrWorld.getInt("DaysToExpiration",	DEFAULT_DAYS_EXPIRATION);
			
			if (cfgCurrWorld.contains("ProtectedBlocks"))
			{
				tmpPlotWorld.ProtectedBlocks = new HashSet<Integer>(cfgCurrWorld.getIntegerList("ProtectedBlocks"));
			}
			else
			{
				tmpPlotWorld.ProtectedBlocks = getDefaultProtectedBlocks();
			}
			
			if (cfgCurrWorld.contains("PreventedItems"))
			{
				tmpPlotWorld.PreventedItems = new HashSet<String>(cfgCurrWorld.getStringList("PreventedItems"));
			}
			else
			{
				tmpPlotWorld.PreventedItems = getDefaultPreventedItems();
			}
			
			tmpPlotWorld.ProtectedWallBlockId 	= cfgCurrWorld.getString("ProtectedWallBlockId",	"44:4");
			tmpPlotWorld.ForSaleWallBlockId 	= cfgCurrWorld.getString("ForSaleWallBlockId",		"44:1");
			tmpPlotWorld.AuctionWallBlockId		= cfgCurrWorld.getString("AuctionWallBlockId",		"44:1");
			tmpPlotWorld.AutoLinkPlots			= cfgCurrWorld.getBoolean("AutoLinkPlots",			true);
			tmpPlotWorld.DisableExplosion 		= cfgCurrWorld.getBoolean("DisableExplosion",		true);
			tmpPlotWorld.DisableIgnition 		= cfgCurrWorld.getBoolean("DisableIgnition",		true);
			
			ConfigurationSection economysection;
			
			if (cfgCurrWorld.getConfigurationSection("economy") == null)
			{
				economysection = cfgCurrWorld.createSection("economy");
			}
			else
			{
				economysection = cfgCurrWorld.getConfigurationSection("economy");
			}

			tmpPlotWorld.UseEconomy = economysection.getBoolean("UseEconomy", false);
			tmpPlotWorld.CanPutOnSale = economysection.getBoolean("CanPutOnSale", false);
			tmpPlotWorld.CanSellToBank = economysection.getBoolean("CanSellToBank", false);
			tmpPlotWorld.RefundClaimPriceOnReset = economysection.getBoolean("RefundClaimPriceOnReset", false);
			tmpPlotWorld.RefundClaimPriceOnSetOwner = economysection.getBoolean("RefundClaimPriceOnSetOwner", false);
			tmpPlotWorld.ClaimPrice = economysection.getDouble("ClaimPrice", 0);
			tmpPlotWorld.ClearPrice = economysection.getDouble("ClearPrice", 0);
			tmpPlotWorld.AddPlayerPrice = economysection.getDouble("AddPlayerPrice", 0);
			tmpPlotWorld.DenyPlayerPrice = economysection.getDouble("DenyPlayerPrice", 0);
			tmpPlotWorld.RemovePlayerPrice = economysection.getDouble("RemovePlayerPrice", 0);
			tmpPlotWorld.UndenyPlayerPrice = economysection.getDouble("UndenyPlayerPrice", 0);
			tmpPlotWorld.PlotHomePrice = economysection.getDouble("PlotHomePrice", 0);
			tmpPlotWorld.CanCustomizeSellPrice = economysection.getBoolean("CanCustomizeSellPrice", false);
			tmpPlotWorld.SellToPlayerPrice = economysection.getDouble("SellToPlayerPrice", 0);
			tmpPlotWorld.SellToBankPrice = economysection.getDouble("SellToBankPrice", 0);
			tmpPlotWorld.BuyFromBankPrice = economysection.getDouble("BuyFromBankPrice", 0);
			tmpPlotWorld.AddCommentPrice = economysection.getDouble("AddCommentPrice", 0);
			tmpPlotWorld.BiomeChangePrice = economysection.getDouble("BiomeChangePrice", 0);
			tmpPlotWorld.ProtectPrice = economysection.getDouble("ProtectPrice", 0);
			tmpPlotWorld.DisposePrice = economysection.getDouble("DisposePrice", 0);
			
			
			
			cfgCurrWorld.set("PlotAutoLimit", tmpPlotWorld.PlotAutoLimit);
			cfgCurrWorld.set("PathWidth", tmpPlotWorld.PathWidth);
			cfgCurrWorld.set("PlotSize", tmpPlotWorld.PlotSize);
			
			cfgCurrWorld.set("BottomBlockId", getBlockValueId(tmpPlotWorld.BottomBlockId, tmpPlotWorld.BottomBlockValue));
			cfgCurrWorld.set("WallBlockId", getBlockValueId(tmpPlotWorld.WallBlockId, tmpPlotWorld.WallBlockValue));
			cfgCurrWorld.set("PlotFloorBlockId", getBlockValueId(tmpPlotWorld.PlotFloorBlockId, tmpPlotWorld.PlotFloorBlockValue));
			cfgCurrWorld.set("PlotFillingBlockId", getBlockValueId(tmpPlotWorld.PlotFillingBlockId, tmpPlotWorld.PlotFillingBlockValue));
			cfgCurrWorld.set("RoadMainBlockId", getBlockValueId(tmpPlotWorld.RoadMainBlockId, tmpPlotWorld.RoadMainBlockValue));
			cfgCurrWorld.set("RoadStripeBlockId", getBlockValueId(tmpPlotWorld.RoadStripeBlockId, tmpPlotWorld.RoadStripeBlockValue));
			
			cfgCurrWorld.set("RoadHeight", tmpPlotWorld.RoadHeight);
			cfgCurrWorld.set("WorldHeight", null);
			cfgCurrWorld.set("DaysToExpiration", tmpPlotWorld.DaysToExpiration);
			cfgCurrWorld.set("ProtectedBlocks", tmpPlotWorld.ProtectedBlocks);
			cfgCurrWorld.set("PreventedItems", tmpPlotWorld.PreventedItems);
			cfgCurrWorld.set("ProtectedWallBlockId", tmpPlotWorld.ProtectedWallBlockId);
			cfgCurrWorld.set("ForSaleWallBlockId", tmpPlotWorld.ForSaleWallBlockId);
			cfgCurrWorld.set("AuctionWallBlockId", tmpPlotWorld.AuctionWallBlockId);
			cfgCurrWorld.set("AutoLinkPlots", tmpPlotWorld.AutoLinkPlots);
			cfgCurrWorld.set("DisableExplosion", tmpPlotWorld.DisableExplosion);
			cfgCurrWorld.set("DisableIgnition", tmpPlotWorld.DisableIgnition);
			
			economysection = cfgCurrWorld.createSection("economy");
			
			economysection.set("UseEconomy", tmpPlotWorld.UseEconomy);
			economysection.set("CanPutOnSale", tmpPlotWorld.CanPutOnSale);
			economysection.set("CanSellToBank", tmpPlotWorld.CanSellToBank);
			economysection.set("RefundClaimPriceOnReset", tmpPlotWorld.RefundClaimPriceOnReset);
			economysection.set("RefundClaimPriceOnSetOwner", tmpPlotWorld.RefundClaimPriceOnSetOwner);
			economysection.set("ClaimPrice", tmpPlotWorld.ClaimPrice);
			economysection.set("ClearPrice", tmpPlotWorld.ClearPrice);
			economysection.set("AddPlayerPrice", tmpPlotWorld.AddPlayerPrice);
			economysection.set("DenyPlayerPrice", tmpPlotWorld.DenyPlayerPrice);
			economysection.set("RemovePlayerPrice", tmpPlotWorld.RemovePlayerPrice);
			economysection.set("UndenyPlayerPrice", tmpPlotWorld.UndenyPlayerPrice);
			economysection.set("PlotHomePrice", tmpPlotWorld.PlotHomePrice);
			economysection.set("CanCustomizeSellPrice", tmpPlotWorld.CanCustomizeSellPrice);
			economysection.set("SellToPlayerPrice", tmpPlotWorld.SellToPlayerPrice);
			economysection.set("SellToBankPrice", tmpPlotWorld.SellToBankPrice);
			economysection.set("BuyFromBankPrice", tmpPlotWorld.BuyFromBankPrice);
			economysection.set("AddCommentPrice", tmpPlotWorld.AddCommentPrice);
			economysection.set("BiomeChangePrice", tmpPlotWorld.BiomeChangePrice);
			economysection.set("ProtectPrice", tmpPlotWorld.ProtectPrice);
			economysection.set("DisposePrice", tmpPlotWorld.DisposePrice);
			
			cfgCurrWorld.set("economy", economysection);
			
			cfgWorlds.set(cfgWorldName, cfgCurrWorld);

			
			PlotMeSqlManager.loadPlots(tmpPlotWorld, tmpPlotWorld.MinecraftWorld.getSpawnLocation(), 16);
			
			PlotManager.registerPlotWorld(tmpPlotWorld);
		}
		
		config.set("usemySQL", usemySQL);
		config.set("mySQLconn", mySQLconn);
		config.set("mySQLuname", mySQLuname);
		config.set("mySQLpass", mySQLpass);
		config.set("globalUseEconomy", globalUseEconomy);
		config.set("AdvancedLogging", advancedlogging);
		config.set("Language", language);
		config.set("allowWorldTeleport", allowWorldTeleport);
		config.set("defaultWEAnywhere", defaultWEAnywhere);
		config.set("auto-update", autoUpdate);
		config.set("allowToDeny", allowToDeny);
		
		try 
		{
			config.save(configfile);
		} 
		catch (IOException e) 
		{
			logger.severe(PREFIX + "error writting configurations");
			e.printStackTrace();
		}
		
		loadCaptions();
    }
	
	private void setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) 
        {
            economy = economyProvider.getProvider();
        }
    }
	
	public static void addIgnoreWELimit(Player p)
	{
		if (!playersignoringwelimit.contains(p.getName()))
		{
			playersignoringwelimit.add(p.getName());
			if (worldedit != null)
			{
				PlotWorldEdit.removeMask(p);
			}
		}
	}
	
	public static void removeIgnoreWELimit(Player p)
	{
		if (playersignoringwelimit.contains(p.getName()))
		{
			playersignoringwelimit.remove(p.getName());
			if (worldedit != null)
			{
				PlotWorldEdit.setMask(p);
			}
		}
	}
	
	public static boolean isIgnoringWELimit(Player p)
	{
		if(defaultWEAnywhere && cPerms(p, "PlotMe.admin.weanywhere"))
			return !playersignoringwelimit.contains(p.getName());
		else
			return playersignoringwelimit.contains(p.getName());
	}
		
	public static int getPlotLimit(Player p)
	{
		int max = -2;
		
		int maxlimit = 255;
		
		if(p.hasPermission("plotme.limit.*"))
		{
			return -1;
		}
		else
		{
			for(int ctr = 0; ctr < maxlimit; ctr++)
			{
				if(p.hasPermission("plotme.limit." + ctr))
				{
					max = ctr;
				}
			}
		
		}
		
		if(max == -2)
		{
			if(cPerms(p, "plotme.admin"))
				return -1;
			else if(cPerms(p, "plotme.use"))
				return 1;
			else
				return 0;
		}
		
		return max;
	}
	
	private HashSet<Integer> getDefaultProtectedBlocks()
	{
		HashSet<Integer> protections = new HashSet<Integer>();
		
		protections.add(Material.CHEST.getId());
		protections.add(Material.FURNACE.getId());
		protections.add(Material.BURNING_FURNACE.getId());
		protections.add(Material.ENDER_PORTAL_FRAME.getId());
		protections.add(Material.DIODE_BLOCK_ON.getId());
		protections.add(Material.DIODE_BLOCK_OFF.getId());
		protections.add(Material.JUKEBOX.getId());
		protections.add(Material.NOTE_BLOCK.getId());
		protections.add(Material.BED.getId());
		protections.add(Material.CAULDRON.getId());
		protections.add(Material.BREWING_STAND.getId());
		protections.add(Material.BEACON.getId());
		protections.add(Material.FLOWER_POT.getId());
		protections.add(Material.ANVIL.getId());
		
		return protections;
	}
	
	private HashSet<String> getDefaultPreventedItems()
	{
		HashSet<String> preventeditems = new HashSet<String>();

		preventeditems.add(String.valueOf(Material.INK_SACK.getId()) + ":15");
		preventeditems.add(String.valueOf(Material.FLINT_AND_STEEL.getId()));
		preventeditems.add(String.valueOf(Material.MINECART.getId()));
		preventeditems.add(String.valueOf(Material.POWERED_MINECART.getId()));
		preventeditems.add(String.valueOf(Material.STORAGE_MINECART.getId()));
		preventeditems.add(String.valueOf(Material.BOAT.getId()));
		
		return preventeditems;
	}
	
	public void scheduleTask(Runnable task, int eachseconds, int howmanytimes)
	{		 		 
		PlotMe.cscurrentlyprocessingexpired.sendMessage("" + PlotMe.PREFIX + ChatColor.RESET + caption("MsgStartDeleteSession"));
		
		for(int ctr = 0; ctr < (howmanytimes / nbperdeletionprocessingexpired); ctr++)
		{
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, task, ctr * eachseconds * 20);
		}
	}
	
	private void loadCaptions()
	{
		File filelang = new File(this.getDataFolder(), "caption-english.yml");
		
		TreeMap<String, String> properties = new TreeMap<String, String>();
		properties.put("MsgStartDeleteSession","Starting delete session");
		properties.put("MsgDeletedExpiredPlots", "Deleted expired plot");
		properties.put("MsgDeleteSessionFinished","Deletion session finished, rerun to reset more plots");
		properties.put("MsgAlreadyProcessingPlots", "is already processing expired plots");
		properties.put("MsgDoesNotExistOrNotLoaded","does not exist or is not loaded.");
		properties.put("MsgNotPlotWorld", "This is not a plot world.");
		properties.put("MsgInvalidPageNumber", "Invalid page number!");
		properties.put("MsgPermissionDenied", "Permission denied");
		properties.put("MsgNoPlotFound", "No plot found");
		properties.put("MsgCannotBidOwnPlot", "You cannot bid on your own plot.");
		properties.put("MsgCannotBuyOwnPlot", "You cannot buy your own plot.");
		properties.put("MsgCannotClaimRoad", "You cannot claim the road.");
		properties.put("MsgInvalidBidMustBeAbove", "Invalid bid. Must be above");
		properties.put("MsgOutbidOnPlot", "Outbid on plot");
		properties.put("MsgOwnedBy", "owned by");
		properties.put("MsgBidAccepted", "Bid accepted.");
		properties.put("MsgPlotNotAuctionned", "This plot isn't being auctionned.");
		properties.put("MsgThisPlot", "This plot");
		properties.put("MsgThisPlotYours", "This plot is now yours.");
		properties.put("MsgThisPlotIsNow", "This plot is now ");
		properties.put("MsgThisPlotOwned", "This plot is already owned.");
		properties.put("MsgHasNoOwner", "has no owners.");
		properties.put("MsgEconomyDisabledWorld", "Economy is disabled for this world.");
		properties.put("MsgPlotNotForSale", "Plot isn't for sale.");
		properties.put("MsgAlreadyReachedMaxPlots", "You have already reached your maximum amount of plots");
		properties.put("MsgToGetToIt", "to get to it");
		properties.put("MsgNotEnoughBid", "You do not have enough to bid this much.");
		properties.put("MsgNotEnoughBuy", "You do not have enough to buy this plot.");
		properties.put("MsgNotEnoughAuto", "You do not have enough to buy a plot.");
		properties.put("MsgNotEnoughComment", "You do not have enough to comment on a plot.");
		properties.put("MsgNotEnoughBiome", "You do not have enough to change the biome.");
		properties.put("MsgNotEnoughClear", "You do not have enough to clear the plot.");
		properties.put("MsgNotEnoughDispose", "You do not have enough to dispose of this plot.");
		properties.put("MsgNotEnoughProtectPlot", "You do not have enough to protect this plot.");
		properties.put("MsgNotEnoughTp","You do not have enough to teleport home.");
		properties.put("MsgNotEnoughAdd","You do not have enough to add a player.");
		properties.put("MsgNotEnoughDeny","You do not have enough to deny a player.");
		properties.put("MsgNotEnoughRemove","You do not have enough to remove a player.");
		properties.put("MsgNotEnoughUndeny","You do not have enough to undeny a player.");
		properties.put("MsgSoldTo", "sold to");
		properties.put("MsgPlotBought", "Plot bought.");
		properties.put("MsgBoughtPlot", "bought plot");
		properties.put("MsgClaimedPlot", "claimed plot");
		properties.put("MsgPlotHasBidsAskAdmin", "Plot is being auctionned and has bids. Ask an admin to cancel it.");
		properties.put("MsgAuctionCancelledOnPlot", "Auction cancelled on plot");
		properties.put("MsgAuctionCancelled", "Auction cancelled.");
		properties.put("MsgStoppedTheAuctionOnPlot", "stopped the auction on plot");
		properties.put("MsgInvalidAmount", "Invalid amount. Must be above or equal to 0.");
		properties.put("MsgAuctionStarted", "Auction started.");
		properties.put("MsgStartedAuctionOnPlot", "started an auction on plot");
		properties.put("MsgDoNotOwnPlot", "You do not own this plot.");
		properties.put("MsgSellingPlotsIsDisabledWorld", "Selling plots is disabled in this world.");
		properties.put("MsgPlotProtectedNotDisposed", "Plot is protected and cannot be disposed.");
		properties.put("MsgWasDisposed", "was disposed.");
		properties.put("MsgPlotDisposedAnyoneClaim", "Plot disposed. Anyone can claim it.");
		properties.put("MsgDisposedPlot", "disposed of plot");
		properties.put("MsgNotYoursCannotDispose","is not yours. You are not allowed to dispose it.");
		properties.put("MsgPlotNoLongerSale","Plot no longer for sale.");
		properties.put("MsgRemovedPlot","removed the plot");
		properties.put("MsgFromBeingSold","from being sold");
		properties.put("MsgCannotCustomPriceDefault","You cannot customize the price. Default price is :");
		properties.put("MsgCannotSellToBank", "Plots cannot be sold to the bank in this world.");
		properties.put("MsgSoldToBank", "sold to bank.");
		properties.put("MsgPlotSold", "Plot sold.");
		properties.put("MsgSoldToBankPlot", "sold to bank plot");
		properties.put("MsgPlotForSale", "Plot now for sale.");
		properties.put("MsgPutOnSalePlot", "put on sale plot");
		properties.put("MsgPlotNoLongerProtected", "Plot is no longer protected. It is now possible to Clear or Reset it.");
		properties.put("MsgUnprotectedPlot", "unprotected plot");
		properties.put("MsgPlotNowProtected", "Plot is now protected. It won't be possible to Clear or Reset it.");
		properties.put("MsgProtectedPlot", "protected plot");
		properties.put("MsgNoPlotsFinished", "No plots are finished");
		properties.put("MsgFinishedPlotsPage","Finished plots page");
		properties.put("MsgUnmarkFinished","Plot is no longer marked finished.");
		properties.put("MsgMarkFinished","Plot is now marked finished.");
		properties.put("MsgPlotExpirationReset","Plot expiration reset");
		properties.put("MsgNoPlotExpired","No plots are expired");
		properties.put("MsgExpiredPlotsPage","Expired plots page");
		properties.put("MsgListOfPlotsWhere","List of plots where");
		properties.put("MsgCanBuild","can build:");
		properties.put("MsgListOfPlotsWhereYou","List of plots where you can build:");
		properties.put("MsgWorldEditInYourPlots","You can now only WorldEdit in your plots");
		properties.put("MsgWorldEditAnywhere","You can now WorldEdit anywhere");
		properties.put("MsgNoPlotFound1","No plot found within");
		properties.put("MsgNoPlotFound2","plots. Contact an admin.");
		properties.put("MsgDoesNotHavePlot","does not have a plot");
		properties.put("MsgPlotNotFound","Could not find plot");
		properties.put("MsgYouHaveNoPlot","You don't have a plot.");
		properties.put("MsgCommentAdded","Comment added.");
		properties.put("MsgCommentedPlot","commented on plot");
		properties.put("MsgNoComments","No comments");
		properties.put("MsgYouHave","You have");
		properties.put("MsgComments","comments.");
		properties.put("MsgNotYoursNotAllowedViewComments","is not yours. You are not allowed to view the comments.");
		properties.put("MsgIsInvalidBiome","is not a valid biome.");
		properties.put("MsgBiomeSet","Biome set to");
		properties.put("MsgChangedBiome","changed the biome of plot");
		properties.put("MsgNotYoursNotAllowedBiome","is not yours. You are not allowed to change it's biome.");
		properties.put("MsgPlotUsingBiome","This plot is using the biome");
		properties.put("MsgPlotProtectedCannotReset","Plot is protected and cannot be reset.");
		properties.put("MsgPlotProtectedCannotClear","Plot is protected and cannot be cleared.");
		properties.put("MsgOwnedBy","owned by");
		properties.put("MsgWasReset","was reset.");
		properties.put("MsgPlotReset","Plot has been reset.");
		properties.put("MsgResetPlot","reset plot");
		properties.put("MsgPlotCleared","Plot cleared.");
		properties.put("MsgClearedPlot","cleared plot");
		properties.put("MsgNotYoursNotAllowedClear","is not yours. You are not allowed to clear it.");
		properties.put("MsgAlreadyAllowed","was already allowed");
		properties.put("MsgAlreadyDenied","was already denied");
		properties.put("MsgWasNotAllowed","was not allowed");
		properties.put("MsgWasNotDenied","was not denied");
		properties.put("MsgNowUndenied","now undenied.");
		properties.put("MsgNowDenied","now denied.");
		properties.put("MsgAddedPlayer","added player");
		properties.put("MsgDeniedPlayer","denied player");
		properties.put("MsgRemovedPlayer","removed player");
		properties.put("MsgUndeniedPlayer","undenied player");
		properties.put("MsgToPlot","to plot");
		properties.put("MsgFromPlot","from plot");
		properties.put("MsgNotYoursNotAllowedAdd","is not yours. You are not allowed to add someone to it.");
		properties.put("MsgNotYoursNotAllowedDeny","is not yours. You are not allowed to deny someone from it.");
		properties.put("MsgNotYoursNotAllowedRemove","is not yours. You are not allowed to remove someone from it.");
		properties.put("MsgNotYoursNotAllowedUndeny","is not yours. You are not allowed to undeny someone to it.");
		properties.put("MsgNowOwnedBy","is now owned by");
		properties.put("MsgChangedOwnerFrom","changed owner from");
		properties.put("MsgChangedOwnerOf","changed owner of");
		properties.put("MsgOwnerChangedTo","Plot Owner has been set to");
		properties.put("MsgPlotMovedSuccess","Plot moved successfully");
		properties.put("MsgExchangedPlot","exchanged plot");
		properties.put("MsgAndPlot","and plot");
		properties.put("MsgReloadedSuccess","reloaded successfully");
		properties.put("MsgReloadedConfigurations","reloaded configurations");
		properties.put("MsgNoPlotworldFound","No Plot world found.");
		properties.put("MsgWorldNotPlot","does not exist or is not a plot world.");
		
		properties.put("ConsoleHelpMain", " ---==PlotMe Console Help Page==---");
		properties.put("ConsoleHelpReload", " - Reloads the plugin and its configuration files");
		
		properties.put("HelpTitle", "PlotMe Help Page");
		properties.put("HelpYourPlotLimitWorld", "Your plot limit in this world");
		properties.put("HelpUsedOf", "used of");
		properties.put("HelpClaim", "Claims the current plot you are standing on.");
		properties.put("HelpClaimOther", "Claims the current plot you are standing on for another player.");
		properties.put("HelpAuto", "Claims the next available free plot.");
		properties.put("HelpHome", "Teleports you to your plot, :# if you own multiple plots.");
		properties.put("HelpHomeOther", "Teleports you to other plots, :# if other people own multiple plots.");
		properties.put("HelpInfo", "Displays information about the plot you're standing on.");
		properties.put("HelpComment", "Leave comment on the current plot.");
		properties.put("HelpComments", "Lists all comments users have said about your plot.");
		properties.put("HelpList", "Lists every plot you can build on.");
		properties.put("HelpListOther", "Lists every plot <player> can build on.");
		properties.put("HelpBiomeInfo", "Shows the current biome in the plot.");
		properties.put("HelpBiome", "Changes the plots biome to the one specified.");
		properties.put("HelpBiomeList", "Lists all possible biomes.");
		properties.put("HelpDone", "Toggles a plot done or not done.");
		properties.put("HelpTp", "Teleports to a plot in the current world.");
		properties.put("HelpId", "Gets plot id and coordinates of the current plot your standing on.");
		properties.put("HelpClear", "Clears the plot to its original flat state.");
		properties.put("HelpReset", "Resets the plot to its original flat state AND remove its owner.");
		properties.put("HelpAdd", "Allows a player to have full access to the plot(This is your responsibility!)");
		properties.put("HelpDeny", "Prevents a player from moving onto your plot.");
		properties.put("HelpRemove", "Revokes a players access to the plot.");
		properties.put("HelpUndeny", "Allows a previously denied player to move onto your plot.");
		properties.put("HelpSetowner", "Sets the player provided as the owner of the plot your currently on.");
		properties.put("HelpMove", "Swaps the plots blocks(highly experimental for now, use at your own risk).");
		properties.put("HelpWEAnywhere", "Toggles using worldedit anywhere.");
		properties.put("HelpExpired", "Lists expired plots.");
		properties.put("HelpDoneList", "Lists finished plots.");
		properties.put("HelpAddTime1", "Resets the expiration date to");
		properties.put("HelpAddTime2", "days from now.");
		properties.put("HelpReload", "Reloads the plugin and its configuration files.");
		properties.put("HelpDispose", "You will no longer own the plot but it will not get cleared.");
		properties.put("HelpBuy", "Buys a plot at the price listed.");
		properties.put("HelpSell", "Puts your plot for sale.");
		properties.put("HelpSellBank", "Sells your plot to the bank for");
		properties.put("HelpAuction", "Puts your plot for auction.");
		properties.put("HelpResetExpired", "Resets the 50 oldest plots on that world.");
		properties.put("HelpBid", "Places a bid on the current plot.");
		
		
		properties.put("WordWorld", "World");
		properties.put("WordUsage", "Usage");
		properties.put("WordExample", "Example");
		properties.put("WordAmount", "amount");
		properties.put("WordUse", "Use");
		properties.put("WordPlot", "Plot");
		properties.put("WordFor", "for");
		properties.put("WordAt", "at");
		properties.put("WordMarked","marked");
		properties.put("WordFinished", "finished");
		properties.put("WordUnfinished", "unfinished");
		properties.put("WordAuction", "Auction");
		properties.put("WordSell", "Sell");
		properties.put("WordYours", "Yours");
		properties.put("WordHelpers", "Helpers");
		properties.put("WordInfinite", "Infinite");
		properties.put("WordPrice", "Price");
		properties.put("WordPlayer", "Player");
		properties.put("WordComment", "comment");
		properties.put("WordBiome", "biome");
		properties.put("WordId", "id");
		properties.put("WordIdFrom", "id-from");
		properties.put("WordIdTo", "id-to");
		properties.put("WordNever", "Never");
		properties.put("WordDefault", "Default");
		properties.put("WordMissing", "Missing");
		properties.put("WordYes", "Yes");
		properties.put("WordNo", "No");
		properties.put("WordText", "text");
		properties.put("WordFrom", "From");
		properties.put("WordTo", "to");
		properties.put("WordBiomes", "Biomes");
		properties.put("WordNotApplicable", "N/A");
		properties.put("WordBottom", "Bottom");
		properties.put("WordTop", "Top");
		properties.put("WordPossessive", "'s");
		properties.put("WordRemoved", "removed");
		
		properties.put("SignOwner", "Owner:");
		properties.put("SignId", "ID:");
		properties.put("SignForSale", "&9&lFOR SALE");
		properties.put("SignPrice", "Price :");
		properties.put("SignPriceColor", "&9");
		properties.put("SignOnAuction", "&9&lON AUCTION");
		properties.put("SignMinimumBid", "Minimum bid :");
		properties.put("SignCurrentBid","Current bid :");
		properties.put("SignCurrentBidColor", "&9");
		
		properties.put("InfoId", "ID");
		properties.put("InfoOwner", "Owner");
		properties.put("InfoBiome", "Biome");
		properties.put("InfoExpire", "Expire date");
		properties.put("InfoFinished", "Finished");
		properties.put("InfoProtected", "Protected");
		properties.put("InfoHelpers", "Helpers");
		properties.put("InfoDenied", "Denied");
		properties.put("InfoAuctionned", "Auctionned");
		properties.put("InfoBidder", "Bidder");
		properties.put("InfoBid", "Bid");
		properties.put("InfoForSale", "For sale");
		
		properties.put("CommandBuy", "buy");
		properties.put("CommandBid", "bid");
		properties.put("CommandResetExpired", "resetexpired");
		properties.put("CommandHelp", "help");
		properties.put("CommandClaim", "claim");
		properties.put("CommandAuto", "auto");
		properties.put("CommandInfo", "info");
		properties.put("CommandComment", "comment");
		properties.put("CommandComments", "comments");
		properties.put("CommandBiome", "biome");
		properties.put("CommandBiomelist", "biomelist");
		properties.put("CommandId", "id");
		properties.put("CommandTp", "tp");
		properties.put("CommandClear", "clear");
		properties.put("CommandReset", "reset");
		properties.put("CommandAdd", "add");
		properties.put("CommandDeny", "deny");
		properties.put("CommandRemove", "remove");
		properties.put("CommandUndeny", "undeny");
		properties.put("CommandSetowner", "setowner");
		properties.put("CommandMove", "move");
		properties.put("CommandWEAnywhere", "weanywhere");
		properties.put("CommandList", "list");
		properties.put("CommandExpired", "expired");
		properties.put("CommandAddtime", "addtime");
		properties.put("CommandDone", "done");
		properties.put("CommandDoneList", "donelist");
		properties.put("CommandProtect", "protect");
		properties.put("CommandSell", "sell");
		properties.put("CommandSellBank", "sell bank");
		properties.put("CommandDispose", "dispose");
		properties.put("CommandAuction", "auction");
		properties.put("CommandHome", "home");
		
		properties.put("ErrCannotBuild","You cannot build here.");
		properties.put("ErrCannotUseEggs", "You cannot use eggs here.");
		properties.put("ErrCannotUse", "You cannot use that.");
		properties.put("ErrCreatingPlotAt", "An error occured while creating the plot at");
		properties.put("ErrMovingPlot", "Error moving plot");
		
		CreateConfig(filelang, properties, "PlotMe Caption configuration αω");
		
		if (language != "english")
		{
			filelang = new File(this.getDataFolder(), "caption-" + language + ".yml");
			CreateConfig(filelang, properties, "PlotMe Caption configuration");
		}
		
		InputStream input = null;
		
		try
		{				
			input = new FileInputStream(filelang);
		    Yaml yaml = new Yaml();
		    Object obj = yaml.load(input);
		
		    if(obj instanceof LinkedHashMap<?, ?>)
		    {
				@SuppressWarnings("unchecked")
				LinkedHashMap<String, String> data = (LinkedHashMap<String, String>) obj;
							    
			    captions = new HashMap<String, String>();
				for(String key : data.keySet())
				{
					captions.put(key, data.get(key));
				}
		    }
		} catch (FileNotFoundException e) {
			logger.severe("[" + NAME + "] File not found: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.severe("[" + NAME + "] Error with configuration: " + e.getMessage());
			e.printStackTrace();
		} finally {                      
			if (input != null) try {
				input.close();
			} catch (IOException e) {}
		}
	}
	
	private void CreateConfig(File file, TreeMap<String, String> properties, String Title)
	{
		if(!file.exists())
		{
			BufferedWriter writer = null;
			
			try{
				File dir = new File(this.getDataFolder(), "");
				dir.mkdirs();			
				
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
				writer.write("# " + Title + "\n");
				
				for(Entry<String, String> e : properties.entrySet())
				{
					writer.write(e.getKey() + ": '" + e.getValue().replace("'", "''") + "'\n");
				}
				
				writer.close();
			}catch (IOException e){
				logger.severe("[" + NAME + "] Unable to create config file : " + Title + "!");
				logger.severe(e.getMessage());
			} finally {                      
				if (writer != null) try {
					writer.close();
				} catch (IOException e2) {}
			}
		}
		else
		{
			OutputStreamWriter writer = null;
			InputStream input = null;
			
			try
			{				
				input = new FileInputStream(file);
			    Yaml yaml = new Yaml();
			    Object obj = yaml.load(input);
			    
			    if(obj instanceof LinkedHashMap<?, ?>)
			    {
					@SuppressWarnings("unchecked")
					LinkedHashMap<String, String> data = (LinkedHashMap<String, String>) obj;
					
				    writer = new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8");
					
					for(Entry<String, String> e : properties.entrySet())
					{						
						if (!data.containsKey(e.getKey()))
							writer.write("\n" + e.getKey() + ": '" + e.getValue().replace("'", "''") + "'");
					}
					
					writer.close();
					input.close();
			    }
			} catch (FileNotFoundException e) {
				logger.severe("[" + NAME + "] File not found: " + e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				logger.severe("[" + NAME + "] Error with configuration: " + e.getMessage());
				e.printStackTrace();
			} finally {                      
				if (writer != null) try {
					writer.close();
				} catch (IOException e2) {}
				if (input != null) try {
					input.close();
				} catch (IOException e) {}
			}
		}
	}
	
	public static String caption(String s)
	{
		if (captions.containsKey(s))
		{
			return addColor(captions.get(s));
		}
		else
		{
			logger.warning("[" + NAME + "] Missing caption: " + s);
			return "ERROR:Missing caption '" + s + "'";
		}
	}
	
	public static String addColor(String string) 
	{
		return ChatColor.translateAlternateColorCodes('&', string);
    }
	
	public void scheduleProtectionRemoval(Plot plot)
	{
		PlotProtectionRemover.addPlotToQueue(plot);
	}
	
	private short getBlockId(ConfigurationSection cs, String section, String def)
	{
		String idvalue = cs.getString(section, def.toString());
		if(idvalue.indexOf(":") > 0)
		{
			return Short.parseShort(idvalue.split(":")[0]);
		}
		else
		{
			return Short.parseShort(idvalue);
		}
	}
	
	private byte getBlockValue(ConfigurationSection cs, String section, String def)
	{
		String idvalue = cs.getString(section, def.toString());
		if(idvalue.indexOf(":") > 0)
		{
			return Byte.parseByte(idvalue.split(":")[1]);
		}
		else
		{
			return 0;
		}
	}
	
	private String getBlockValueId(Short id, Byte value)
	{
		return (value == 0) ? id.toString() : id.toString() + ":" + value.toString();
	}
}
