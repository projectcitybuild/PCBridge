package com.pcb.pcbridge.swearblock.listeners;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.pcb.pcbridge.library.controllers.listeners.AbstractListener;
import com.pcb.pcbridge.players.PlayerData;

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
	        		p.sendMessage(String.format(e.getFormat(), new Object[] { e.getPlayer().getDisplayName(), edited }));
	        	else
	        		p.sendMessage(String.format(e.getFormat(), new Object[] { e.getPlayer().getDisplayName(), e.getMessage() }));
		    }
	        
	        // log the message since we're cancelling the event
	        _plugin.getLogger().info("Filtered: <" + e.getPlayer().getDisplayName() + "> " + e.getMessage());
	        
		    e.setCancelled(true);
	    }
	}
}
