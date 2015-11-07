package com.pcb.pcbridge.bukkit.swearblock.listeners;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.pcb.pcbridge.bukkit.players.PlayerData;
import com.pcb.pcbridge.library.controllers.AbstractListener;

/**
 * SwearBlock v2
 * Modifies any outgoing messages containing swear words
 * 
 * @credit	Hard24get - for his original iteration of SwearBlock (v1)
 */

public final class ListenerOnPlayerChat extends AbstractListener implements Listener
{
	 private final String filter = "(?iu)(FUCK|SHIT|\\bASS\\b|DAMN|ASSHAT|\\bCOCK\\b|NIGGER|NIGGA|FAG|FAGGOT|PUSSY|ASSHOLE|NIGGUH|DAMMIT|ARSE|BASTARD|BITCH|CUNT|DICK|DYKE|DOUCHE|NEGRO|FAGGIT|QUEER|SLUT|TWAT|WANK|WHORE)";
	
	 /**
	  * Replaces swear words with stars for any player with the swear filter on
	  * 
	  * @param e
	  */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerChat(AsyncPlayerChatEvent e)
	{
	    World world = e.getPlayer().getWorld();
        String group = _plugin.GetPermissionsHook().getPrimaryGroup( e.getPlayer() );
        
        String prefix = _plugin.GetChatHook().getGroupPrefix(world, group);
        prefix = prefix.replace("&", "Åò");
	    
        String format = "[" + world + "]" + prefix + ChatColor.WHITE + "<" + e.getPlayer().getDisplayName() + "> ";
        
        Matcher matcher = Pattern.compile(filter).matcher( e.getMessage() );
	    if (matcher.find()) 
	    {
	        String caught = matcher.group();
	        String stars = StringUtils.repeat("*", caught.length());
	        String edited = e.getMessage().replace(caught, stars);
	        
	        for (Player p : e.getRecipients()) 
		    {
	        	// check if message receiver has filter activate
	        	PlayerData settings = _plugin.GetPlayerManager().GetSettings(p.getUniqueId());
	        	if(settings.SwearFilter)
	        		p.sendMessage(format + edited);
	        	else
	        		p.sendMessage(format + e.getMessage());
		    }
	        
	        // log the message since we're cancelling the event
	        _plugin.getLogger().info("Filtered: <" + e.getPlayer().getDisplayName() + "> " + e.getMessage());
	        
		    e.setCancelled(true);
	    }
	}
}
