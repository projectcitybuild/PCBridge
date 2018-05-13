//package com.pcb.pcbridge.archived;
//
//import org.spigot.event.EventHandler;
//import org.spigot.event.EventPriority;
//import org.spigot.event.player.PlayerLoginEvent;
//import org.spigot.event.player.PlayerLoginEvent.Result;
//
//import com.pcb.pcbridge.archived.MessageBuilder.RawColours;
//
//public final class OnPlayerLoginEvent extends AbstractListener
//{
//	/**
//	 * Called after a player has established a connection to the server
//	 *
//	 * @param event
//	 */
//	@EventHandler(priority = EventPriority.HIGHEST)
//	public void OnPlayerPreLogin(PlayerLoginEvent event)
//	{
//		CheckIfMaintenance(event);
//	}
//
//	/**
//	 * Prevents users from joining if the server is in maintenance mode
//	 *
//	 * @param event
//	 */
//	private void CheckIfMaintenance(PlayerLoginEvent event)
//	{
//		if(GetEnv().GetConfig().getBoolean("server.maintenance"))
//		{
//			if(!event.getPlayer().hasPermission("pcbridge.maintenance.access"))
//			{
//				String message = new MessageBuilder()
//					.Colour(RawColours.AQUA)
//					.String("Server maintenance in progress.")
//					.Linebreak(2)
//					.Colour(RawColours.WHITE)
//					.String("Visit www.projectcitybuild.com for details.")
//					.Build();
//
//				event.disallow(Result.KICK_WHITELIST, message);
//			}
//		}
//	}
//}
