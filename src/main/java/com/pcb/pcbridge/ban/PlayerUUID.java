package com.pcb.pcbridge.ban;

import java.util.UUID;
import org.bukkit.entity.Player;

public final class PlayerUUID 
{
	public String Username;
	public String IP;
	public UUID UUID;
	public boolean IsOnline;
	public boolean HasJoinedBefore;
	public Player Player;
	
	public String GetUUID()
	{
		if(UUID == null)
			return "";
		
		return UUID.toString();
	}
	
	public PlayerUUID(String username, String ip, UUID uuid, boolean isOnline, boolean hasJoinedBefore, Player player)
	{
		this.Username 			= username;
		this.IP					= ip;
		this.UUID 				= uuid;
		this.IsOnline 			= isOnline;
		this.HasJoinedBefore 	= hasJoinedBefore;
		this.Player				= player;
	}
}
