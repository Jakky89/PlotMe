package com.worldcretornica.plotme.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.worldcretornica.plotme.PlotMe;


public class JSONutils {
	
	public static JSONObject jsonLoadFromString(String jsonString)
	{
		JSONParser jsonParser = new JSONParser();
		try
		{
			Object obj = jsonParser.parse(jsonString);
			JSONObject jsobj = (JSONObject)obj;
			return jsobj;
		}
		catch (ParseException ex)
		{
			PlotMe.logger.severe(PlotMe.PREFIX + "PARSER EXCEPTION occurred while loading JSON object :");
			PlotMe.logger.severe("  " + ex.getMessage());
		}
		return null;
	}
	
	public static JSONObject jsonLoad(FileReader reader)
	{
		JSONParser jsonParser = new JSONParser();
		try
		{
			Object obj = jsonParser.parse(reader);
			JSONObject jsobj = (JSONObject)obj;
			return jsobj;
		}
		catch (FileNotFoundException ex)
		{
			PlotMe.logger.severe(PlotMe.PREFIX + "JSON object file not found :");
			PlotMe.logger.severe("  " + ex.getMessage());
		}
		catch (IOException ex)
		{
			PlotMe.logger.severe(PlotMe.PREFIX + "IO EXCEPTION occurred while loading JSON object :");
			PlotMe.logger.severe("  " + ex.getMessage());
		}
		catch (ParseException ex)
		{
			PlotMe.logger.severe(PlotMe.PREFIX + "PARSER EXCEPTION occurred while loading JSON object :");
			PlotMe.logger.severe("  " + ex.getMessage());
		}
		return null;
	}
	
	public static JSONObject jsonLoad(String fileName)
	{
		FileReader reader;
		try {
			reader = new FileReader(fileName);
			return jsonLoad(reader);
		} catch (FileNotFoundException ex) {
			PlotMe.logger.severe(PlotMe.PREFIX + "JSON object file not found :");
			PlotMe.logger.severe("  " + ex.getMessage());
		}
		return null;
	}
	
	public static String jsonToString(JSONObject jsonObject)
	{
		if (jsonObject != null)
		{
			return jsonObject.toJSONString();
		}
		return null;
	}
	
	public static boolean jsonSave(JSONObject jsonObject, FileWriter writer)
	{
		try
		{
			if (jsonObject == null)
			{
				return false;
			}
			String jsonString = jsonToString(jsonObject);
			if (jsonString == null)
			{
				return false;
			}
			writer.write(jsonString);
			writer.flush();
			writer.close();
			return true;
		}
		catch (IOException ex)
		{
			PlotMe.logger.severe(PlotMe.PREFIX + "Error while saving json object!");
		}
		return false;
	}

	public static boolean jsonSave(JSONObject jsonObject, String fileName)
	{
		try
		{
			FileWriter writer = new FileWriter(fileName);
			return jsonSave(jsonObject, writer);
		}
		catch (IOException e)
		{
			PlotMe.logger.severe(PlotMe.PREFIX + "Could not create FileWriter for JSON object!");
		}
		return false;
	}

	public static boolean jsonSet(JSONObject jsonObject, Object key, Object value)
	{
		if (key == null)
		{
			return false;
		}
		if (jsonObject == null)
		{
			if (value == null)
			{
				return false;
			}
			jsonObject = new JSONObject();
		}
		else
		{
			if (value == null)
			{
				if (jsonObject.remove(key) != null)
				{
					return true;
				}
			}
			if (jsonObject.get(key).equals(value))
			{
				return false;
			}
		}
		jsonObject.put(key, value);
		return true;
	}
	
	public static void jsonAdd(JSONArray jsonArray, Object value)
	{
		if (jsonArray == null)
		{
			jsonArray = new JSONArray();
		}
		jsonArray.add(value);
	}
	
	public static String getJsonObjectString(JSONObject jsonObject, Object key)
	{
		return (String)jsonObject.get(key);
	}
	
	public static Integer getJsonObjectInteger(JSONObject jsonObject, Object key)
	{
		return (Integer)jsonObject.get(key);
	}
	
	public static List<?> getJsonObjectList(JSONObject jsonObject, Object key)
	{
		return (ArrayList<?>)jsonObject.get(key);
	}
	
}
