package com.pcb.pcbridge.bukkit.players;

public final class PlayerData 
{
	public long UserId;
	public boolean SwearFilter;
	
	public PlayerData() { }
	public PlayerData(long userId, boolean swearFilter)
	{
		this.UserId = userId;
		this.SwearFilter = swearFilter;
	}
}
