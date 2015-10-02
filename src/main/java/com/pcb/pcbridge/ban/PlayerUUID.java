package com.pcb.pcbridge.ban;

import java.util.UUID;

public final class PlayerUUID 
{
	public String Username;
	public String IP;
	public UUID UUID;
	public boolean IsOnline;
	public boolean HasJoinedBefore;
	
	public String GetUUID()
	{
		if(UUID == null)
			return "";
		
		return UUID.toString();
	}
	
	public PlayerUUID(String username, String ip, UUID uuid, boolean isOnline, boolean hasJoinedBefore)
	{
		this.Username 			= username;
		this.IP					= ip;
		this.UUID 				= uuid;
		this.IsOnline 			= isOnline;
		this.HasJoinedBefore 	= hasJoinedBefore;
	}
}
