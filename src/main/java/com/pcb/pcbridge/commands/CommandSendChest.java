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

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.tasks.GetPlayerCacheTask;
import com.pcb.pcbridge.tasks.GetUuidTask;
import com.pcb.pcbridge.utils.cache.StaticCache;
import com.pcb.pcbridge.utils.commands.AbstractCommand;
import com.pcb.pcbridge.utils.commands.CommandArgs;
import com.pcb.pcbridge.models.PlayerConfig;

public class CommandSendChest extends AbstractCommand {

	@Override
	public String GetName() 
	{
		return "sendchest";
	}
	
	@Override
	public String GetDescription() 
	{
		return "Gives items to a player that they can access at anytime using /receivechest";
	}

	@Override
	public String GetPermission() 
	{
		return "pcbridge.admin.sendchest.give";
	}

	@Override
	public String GetUsage()
	{
		return "/sendchest <name>";
	}

	@Override
	public boolean OnExecute(CommandArgs args) 
	{
		/*if(!args.IsPlayer())
		{
			args.GetSender().sendMessage("ERROR: Only players can use this command - you need to be in-game to access the GUI");
			return true;
		}
	
		if(args.GetArgs().length != 1)
			return false;
		
		PCBridge.NewChain()
			.asyncFirst( () -> GetUuidTask.FindOrFetch(GetEnv().GetServer(), args.GetArg(0)) )
			.abortIfNull()
			.async(uuid -> GetPlayerCacheTask.Get(GetEnv(), uuid))
			.syncLast(config -> {
				ItemStack[] serialisedInv = config.Chest;
				
				String invName = args.GetArg(0) + " - PCB Chest";
				Inventory chest = Bukkit.createInventory(args.GetPlayer(), 36, invName);
				if(serialisedInv != null)
				{
					chest.setContents(serialisedInv);
				}
				
				CacheManager cacheManager = GetEnv().GetCache();
				StaticCache<String, PlayerConfig> cache = cacheManager.Get("SENDCHEST");
				if(cache == null)
				{
					cache = new StaticCache<String, PlayerConfig>();
					cacheManager.Add("SENDCHEST", cache);
				}
				cache.Remember(invName, config);
				
				args.GetPlayer().openInventory(chest);
			})
			.execute();*/
		
		return true;
	}

}
