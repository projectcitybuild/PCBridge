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
package com.pcb.pcbridge.archived.pcbridge.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.archived.pcbridge.models.PlayerBan;
import com.pcb.pcbridge.archived.pcbridge.tasks.GetBansTask;

public final class OnPluginEnabledEvent extends AbstractListener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPluginEnabled(PluginEnabledEvent event)
	{
		CheckForBannedPlayers();
	}

	/**
	 * Checks if any banned players somehow snuck onto the server 
	 * while it was enabling/reloading
	 */
	private void CheckForBannedPlayers()
	{
		GetEnv().GetLogger().info("Checking if any online players are banned...");

		PCBridge.NewChain()
			.syncFirst(() -> GetEnv().GetServer().getOnlinePlayers())
			.async(onlinePlayers -> {
				List<Player> playersToBoot = new ArrayList<>();
				for(Player player : onlinePlayers)
				{
					List<PlayerBan> bans = GetBansTask.GetAllByUuid(player.getUniqueId().toString(), true);
					if(bans.size() > 0)
						playersToBoot.add(player);
				}
				
				return playersToBoot;
			})
			.abortIfNull()
			.syncLast(playersToBoot -> {
				for(Player player : playersToBoot)
				{
					player.kickPlayer("You are currently banned.");
				}
				
				GetEnv().GetLogger().info("Player check complete.");
			})
			.execute();
	}
}
