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

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

import org.apache.commons.lang.StringUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.models.PlayerConfig;
import com.pcb.pcbridge.utils.cache.StaticCache;
import com.pcb.pcbridge.utils.listeners.AbstractListener;

/**
 * @credits Hard24Get - for the original Swearblock plugin and regex pattern
 */
public final class OnPlayerAsyncChatEvent extends AbstractListener
{
	private final Pattern _pattern = Pattern.compile("(?iu)(FUCK|SHIT|\\bASS\\b|DAMN|ASSHAT|\\bCOCK\\b|NIGGER|NIGGA|FAG|FAGGOT|PUSSY|ASSHOLE|NIGGUH|DAMMIT|ARSE|BASTARD|BITCH|CUNT|DICK|DYKE|DOUCHE|NEGRO|FAGGIT|QUEER|SLUT|TWAT|WANK|WHORE)");
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerChat(AsyncPlayerChatEvent event)
	{
		CheckIfMuted(event);
		FilterSwearing(event);
	}
	
	/**
	 * Prevents muted players from chatting
	 * 
	 * @param uuid
	 */
	private void CheckIfMuted(AsyncPlayerChatEvent event)
	{		
		PlayerConfig config = GetEnv().GetPlayerCache( event.getPlayer().getUniqueId() );
		
		if(config.IsMuted)
		{
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You cannot chat while muted.");
		}
	}
	
	/**
	 * Send a filtered message to anyone with swearblock enabled
	 * 
	 * @param event
	 */
	private void FilterSwearing(AsyncPlayerChatEvent event)
	{
		if(event.isCancelled())
			return;
		
		Matcher matcher = _pattern.matcher( event.getMessage() );
	    if (matcher.find()) 
	    {
	    	// only swearblock activated players need to see filtered messages,
	    	// so we have to handle filtering on a per-player basis
	    	
	        String caught = matcher.group();
	        String stars = StringUtils.repeat("*", caught.length());
	        String edited = event.getMessage().replace(caught, stars);
	        
			StaticCache<UUID, PlayerConfig> cache = GetEnv().GetPlayerCache();
	        
	        for (Player player : event.getRecipients()) 
		    {
	        	PlayerConfig config = cache.Get(player.getUniqueId());
				if(config.IsSwearblockEnabled)
					player.sendMessage( FormatMessage(event.getPlayer(), edited) );
				else
					player.sendMessage( FormatMessage(event.getPlayer(), event.getMessage()) );
		    }
	        
		    event.setCancelled(true);
	    
		    // manually log the message or else it won't show up in console anymore
		    GetEnv().GetLogger().info("FILTERED: " + event.getMessage());
	    }
	    else
	    {	    	
	    	event.getRecipients().forEach(player -> {
	    		player.sendMessage( FormatMessage(event.getPlayer(), event.getMessage()) );
	    	});
	    	event.setCancelled(true);
	    }
	}
	
	/**
	 * Formats the final message to be shown to players,
	 * adding their world and groups to the message
	 * 
	 * @param message
	 */
	private String FormatMessage(Player player, String message)
	{
		World world = player.getWorld();
		
		// build the player's group prefix
	    String[] groups = PCBridge.GetVaultHook().GetPermission().getPlayerGroups(player);      
        StringBuilder builder = new StringBuilder();
        for(String group : groups)
        {
        	String prefix = PCBridge.GetVaultHook()
        			.GetChat()
        			.getGroupPrefix(world, group)
        			.trim();
        	
        	// donators have the [$] appear before any other group prefix
        	if(group.equalsIgnoreCase("donator"))
        		builder.insert(0, prefix);
        	else
        		builder.append(prefix);
        }
        String group = builder.toString().replace("&", "Åò");

        // put everything together to form the entire message
        String output = "<" + world.getName() + "><" + group + " " + player.getDisplayName() + ChatColor.WHITE + "> " + message;
        
        return output;
	}
}