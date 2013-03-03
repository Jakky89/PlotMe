package com.worldcretornica.plotme.rooms;

import org.bukkit.entity.Player;

public class PlotRoomRentee implements Comparable<PlotRoomRentee> {

	private int id;
	private Player player;
	private String playername;
	private String displayname;
	
	
	public PlotRoomRentee(int roomOwnerId, Player minecraftPlayer)
	{
		id = roomOwnerId;
		player = minecraftPlayer;
		playername = minecraftPlayer.getName();
		displayname = minecraftPlayer.getDisplayName();
	}
	
	public PlotRoomRentee(int roomOwnerId, String playerName)
	{
		id = roomOwnerId;
		player = null;
		playername = playerName;
		displayname = playerName;
	}
	
	public PlotRoomRentee(int roomOwnerId, String playerName, String displayName)
	{
		id = roomOwnerId;
		player = null;
		playername = playerName;
		displayname = displayName;
	}
	
	@Override
	public int compareTo(PlotRoomRentee owner2) {		
		return this.id - owner2.id;
	}

}
