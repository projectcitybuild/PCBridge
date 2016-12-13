/*
 * The MIT License
 *
 * Copyright 2016 Andy Saw
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.pcb.pcbridge.listeners;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.models.PlayerConfig;
import com.pcb.pcbridge.tasks.SyncPlayerRankTask;
import com.pcb.pcbridge.utils.listeners.AbstractListener;
import com.pcb.pcbridge.utils.listeners.PluginEnabledEvent;

public final class OnPlayerJoinEvent extends AbstractListener
{
	/**
	 * Called after a player has logged in to the server
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void OnPlayerJoin(PlayerJoinEvent event)
	{
		GetPlayerConfig(event.getPlayer());
		SyncPlayerRank(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPluginEnabled(PluginEnabledEvent event)
	{
		GetEnv().GetServer().getOnlinePlayers().forEach( player -> GetPlayerConfig(player) );
	}
	
	/**
	 * Loads (or creates) the player's file from storage
	 */
	private void GetPlayerConfig(Player player)
	{
		PCBridge.NewChain()
			.asyncFirst( () -> {
				// get or create PlayerConfig from storage
				UUID uuid = player.getUniqueId();
				PlayerConfig config = new PlayerConfig(GetEnv().GetPlayerFolder(), uuid);	
				GetEnv().GetPlayerCache().Remember(uuid, config);
				
				return config;
			})
			.syncLast( config -> {
				RescuePlayer(player, config);
			})
			.execute();
	}
	
	/**
	 * If a player needs to be rescued on join, teleport them to the
	 * given coordinate
	 * 
	 * @param coordinate
	 */
	private void RescuePlayer(Player player, PlayerConfig config)
	{
		if(!config.NeedsRescue || config.RescueCoords == null)
			return;
		
		player.teleport(config.RescueCoords);
		
		config.NeedsRescue = false;
		config.RescueCoords = null;
		
		try 
		{
			config.Save();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void SyncPlayerRank(Player player)
	{
		PCBridge.NewChain()
			.async( () -> SyncPlayerRankTask.Sync(player) )
			.execute();
	}
}