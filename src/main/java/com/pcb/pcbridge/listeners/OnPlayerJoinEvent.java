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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.models.PlayerConfig;
import com.pcb.pcbridge.schema.PlayerContract;
import com.pcb.pcbridge.schema.WarningContract;
import com.pcb.pcbridge.tasks.SyncPlayerRankTask;
import com.pcb.pcbridge.utils.listeners.AbstractListener;
import com.pcb.pcbridge.utils.listeners.events.PlayerNameChangedEvent;
import com.pcb.pcbridge.utils.listeners.events.PluginEnabledEvent;
import com.pcb.pcbridge.utils.MessageBuilder;

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
		
		// set the player's Tab list name, but we need to delay it by a few 
		// ticks, or else Bukkit will immediately overwrite it
		GetEnv().GetServer().getScheduler().scheduleSyncDelayedTask(GetEnv().GetPlugin(), () -> {
			GetEnv().BroadcastEvent( new PlayerNameChangedEvent(event.getPlayer()) );
		}, 5);
		
		FetchWarnings(event.getPlayer());
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
			.async( () -> SyncPlayerRankTask.Sync(GetEnv(), player) )
			.execute();
	}
	
	private void FetchWarnings(Player player)
	{
		PCBridge.NewChain()
			.asyncFirst( () -> {
				int warnings = 0;
				
				String database = GetEnv().GetConfig().getString("database.warnings.database");
				try(Connection conn = PCBridge.GetConnectionPool().GetConnection(database))
				{
					String selectQuery = "SELECT COUNT(*) " 
							+ " FROM " + WarningContract.TableWarnings.TABLE_NAME + " AS s"
							+ " LEFT JOIN " + PlayerContract.TablePlayers.TABLE_NAME + " AS t1"
							+ " ON s." + WarningContract.TableWarnings.COL_PLAYER_ID + " = t1." + PlayerContract.TablePlayers._ID
							+ " WHERE t1." + PlayerContract.TablePlayers.COL_UUID + " = ?";
					
					try(PreparedStatement stmt = conn.prepareStatement(selectQuery))
					{
						stmt.setString(1, player.getUniqueId().toString());
						ResultSet results = stmt.executeQuery();
						
						if(results.first())
							warnings = results.getInt(1);
					}
				}
				catch(SQLException e)
				{
					e.printStackTrace();
				}
				
				return warnings;
			})
			.syncLast(warnings -> {
				
				if(warnings == 0)
					return;
				
				GetEnv().GetServer().getOnlinePlayers().forEach(onlinePlayer -> 
				{						
					if(onlinePlayer.hasPermission("pcbridge.warn.notify"))
					{
						String msg = new MessageBuilder()
							.Colour(ChatColor.RED)
							.When(warnings == 1, b -> {
								return b.String("@staff: %s has 1 warning on record", player.getName());
							}, b -> {
								return b.String("@staff: %s has %s warnings on record", player.getName(), warnings);
							})
							.Build();
						
						onlinePlayer.sendMessage(msg);
					}
						
				});
				
				
			})
			.execute();
	}
}