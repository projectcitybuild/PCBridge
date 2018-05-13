//package com.pcb.pcbridge.archived;
//
//import java.util.UUID;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import net.md_5.bungee.api.ChatColor;
//
//import org.apache.commons.lang.StringUtils;
//import org.spigot.World;
//import org.spigot.entity.Player;
//import org.spigot.event.EventHandler;
//import org.spigot.event.EventPriority;
//import org.spigot.event.player.AsyncPlayerChatEvent;
//
//import com.pcb.pcbridge.archived.utils.cache.StaticCache;
//
///**
// * @credits Hard24Get - for the original Swearblock plugin and regex pattern
// */
//public final class OnPlayerAsyncChatEvent extends AbstractListener
//{
//	private final Pattern _pattern = Pattern.compile("(?iu)(FUCK|SHIT|\\bASS\\b|DAMN|ASSHAT|\\bCOCK\\b|NIGGER|NIGGA|FAG|FAGGOT|PUSSY|ASSHOLE|NIGGUH|DAMMIT|ARSE|BASTARD|BITCH|CUNT|DICK|DYKE|DOUCHE|NEGRO|FAGGIT|QUEER|SLUT|TWAT|WANK|WHORE)");
//
//	@EventHandler(priority = EventPriority.HIGHEST)
//	public void OnPlayerChat(AsyncPlayerChatEvent event)
//	{
//		CheckIfMuted(event);
//		FilterSwearing(event);
//	}
//
//	/**
//	 * Prevents muted players from chatting
//	 *
//	 * @param uuid
//	 */
//	private void CheckIfMuted(AsyncPlayerChatEvent event)
//	{
//		PlayerConfig config = GetEnv().GetPlayerCache( event.getPlayer().getUniqueId() );
//
//		if(config.IsMuted)
//		{
//			event.setCancelled(true);
//			event.getPlayer().sendMessage(ChatColor.RED + "You cannot chat while muted.");
//		}
//	}
//
//	/**
//	 * Send a filtered message to anyone with swearblock enabled
//	 *
//	 * @param event
//	 */
//	private void FilterSwearing(AsyncPlayerChatEvent event)
//	{
//		if(event.isCancelled())
//			return;
//
//		StaticCache<UUID, PlayerConfig> cache = GetEnv().GetPlayerCache();
//		PlayerConfig senderConfig = cache.Get(event.getPlayer().getUniqueId());
//
//		Matcher matcher = _pattern.matcher( event.getMessage() );
//	    if (matcher.find())
//	    {
//	    	// only swearblock activated players need to see filtered messages,
//	    	// so we have to handle filtering on a per-player basis
//
//	        String caught = matcher.group();
//	        String stars = StringUtils.repeat("*", caught.length());
//	        String edited = event.getMessage().replace(caught, stars);
//
//	        for (Player player : event.getRecipients())
//		    {
//	        	PlayerConfig config = cache.Get(player.getUniqueId());
//				if(config.IsSwearblockEnabled)
//					player.sendMessage( FormatMessage(event.getPlayer(), senderConfig, edited) );
//				else
//					player.sendMessage( FormatMessage(event.getPlayer(), senderConfig, event.getMessage()) );
//		    }
//
//		    event.setCancelled(true);
//
//		    // manually log the message or else it won't show up in console anymore
//		    GetEnv().GetLogger().info("FILTERED: " + event.getMessage());
//	    }
//	    else
//	    {
//	    	event.getRecipients().forEach(player -> {
//	    		player.sendMessage( FormatMessage(event.getPlayer(), senderConfig, event.getMessage()) );
//	    	});
//	    	event.setCancelled(true);
//	    }
//
//	    // a player's Tab list name changes everytime they chat, so we need to
//	    // rebuild their name again
//		GetEnv().BroadcastEvent( new PlayerNameChangedEvent(event.getPlayer()) );
//	}
//
//	/**
//	 * Formats the final message to be shown to players,
//	 * adding their world and groups to the message
//	 *
//	 * @param message
//	 */
//	private String FormatMessage(Player player, PlayerConfig senderConfig, String message)
//	{
//		World world = player.getWorld();
//		String name = GetPlayerNameTask.GetFormattedName(GetEnv(), player, senderConfig);
//
//        // put everything together to form the entire message
//        String output = "<" + world.getName() + "><" + name + ChatColor.WHITE + "> " + message;
//
//        return output;
//	}
//}