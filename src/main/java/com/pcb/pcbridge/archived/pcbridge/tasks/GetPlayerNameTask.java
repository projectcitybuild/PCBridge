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
package com.pcb.pcbridge.archived.pcbridge.tasks;

import java.util.UUID;

import org.bukkit.entity.Player;

import net.milkbowl.vault.chat.Chat;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.archived.pcbridge.models.PlayerConfig;
import com.pcb.pcbridge.archived.utils.cache.StaticCache;


public class GetPlayerNameTask {
	
	public static String GetFormattedName(Environment env, Player player)
	{
		StaticCache<UUID, PlayerConfig> cache = env.GetPlayerCache();
		PlayerConfig config = cache.Get(player.getUniqueId());
		
		return GetFormattedName(env, player, config);
	}
	
	public static String GetFormattedName(Environment env, Player player, PlayerConfig config)
	{
		// build the player's group prefix
	    String[] groups = PCBridge.GetVaultHook().GetPermission().getPlayerGroups(player);      
        StringBuilder prefixBuilder = new StringBuilder();
        StringBuilder suffixBuilder = new StringBuilder();
        
        // add any manual pre/suffixes to their prefix list
        if(config.Prefix != null)
        	prefixBuilder.insert(0, config.Prefix);
        
        for(String group : groups)
        {
        	Chat chat = PCBridge.GetVaultHook().GetChat();
        	
        	String prefix = chat.getGroupPrefix(player.getWorld(), group).trim();
        	String suffix = chat.getGroupSuffix(player.getWorld(), group).trim();
        	
        	// donators have the [$] appear before any other group prefix or the manual prefix
        	if(group.equalsIgnoreCase("donator"))
        		prefixBuilder.insert(0, prefix);
        	else
        		prefixBuilder.append(prefix);
        	
        	suffixBuilder.append(suffix);
        }
        
        if(config.Suffix != null)
        	suffixBuilder.append(config.Suffix);
        
        String prefix = prefixBuilder.toString().replace("&", "§");
        String suffix = suffixBuilder.toString().replace("&", "§");
        
        String name = prefix + " " + player.getDisplayName() + suffix;
        
        return name;
	}	
	
}
