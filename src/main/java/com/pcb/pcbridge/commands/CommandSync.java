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
package com.pcb.pcbridge.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.tasks.AbortTask;
import com.pcb.pcbridge.tasks.SyncPlayerRankTask;
import com.pcb.pcbridge.utils.commands.AbstractCommand;
import com.pcb.pcbridge.utils.commands.CommandArgs;

public class CommandSync extends AbstractCommand {

	@Override
	public String GetName() 
	{
		return "sync";
	}
	
	@Override
	public String GetDescription() 
	{
		return "Syncs a player's in-game rank with their PCB forum rank";
	}

	@Override
	public String GetPermission() 
	{
		return "pcbridge.rank.sync";
	}

	@Override
	public String GetUsage()
	{
		return "/sync [name]";
	}
	
	@Override
	public String[] GetAliases()
	{
		return new String[] { "syncrank" };
	}

	@Override
	public boolean OnExecute(CommandArgs args) 
	{
		if(args.GetArgs().length != 1)
			return false;
		
		String alias = args.GetArg(0);
		
		PCBridge.NewChain()
			.syncFirst( () -> {
				for(Player player : GetEnv().GetServer().getOnlinePlayers())
				{
					if(player.getName().equalsIgnoreCase(alias))
						return player;
				}
				return null;
			})
			.abortIfNull( AbortTask.Send(args.GetSender(), ChatColor.RED + "%s is not online", alias) )
			
			.async( player -> {
				SyncPlayerRankTask.Sync(GetEnv(), player);
				return player;
			} )
			.syncLast( player -> {
				player.sendMessage(ChatColor.GRAY + "Your rank has been updated.");
				
				if(!player.getName().equals(args.GetSender()))
					args.GetSender().sendMessage(ChatColor.GRAY + "Updated the rank of " + alias);
			})
			.execute();
		
		return true;
	}

}
