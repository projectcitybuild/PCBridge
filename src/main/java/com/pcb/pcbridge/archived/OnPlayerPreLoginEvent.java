
//package com.pcb.pcbridge.archived;
//
//import java.util.List;
//
//import org.spigot.event.EventHandler;
//import org.spigot.event.EventPriority;
//import org.spigot.event.player.AsyncPlayerPreLoginEvent;
//import org.spigot.event.player.AsyncPlayerPreLoginEvent.Result;
//
//import com.pcb.pcbridge.archived.MessageBuilder.RawColours;
//
//public final class OnPlayerPreLoginEvent extends AbstractListener
//{
//	/**
//	 * Called when a player first attempts to connect to the server.
//	 * Nothing here will run on the main thread
//	 *
//	 * @param event
//	 */
//	@EventHandler(priority = EventPriority.HIGHEST)
//	public void OnPlayerPreLogin(AsyncPlayerPreLoginEvent event)
//	{
//		CheckIfBanned(event);
//	}
//
//	/**
//	 * Prevents banned UUIDs from connecting to the server
//	 *
//	 * @param event
//	 */
//	private void CheckIfBanned(AsyncPlayerPreLoginEvent event)
//	{
//		List<PlayerBan> bans = GetBansTask.GetAllByUuid(event.getUniqueId().toString(), true);
//
//		for(PlayerBan ban : bans)
//		{
//			if(!ban.IsActive)
//				continue;
//
//			String message = new MessageBuilder()
//				.Colour(MessageBuilder.RawColours.RED, MessageBuilder.RawFormats.BOLD)
//				.String("You are currently banned.").Linebreak(2)
//				.RawReset()
//				.Colour(RawColours.GRAY)
//				.String("Reason: ")
//				.Colour(RawColours.WHITE)
//				.Stringln(ban.Reason)
//				.Colour(RawColours.GRAY)
//				.String("Expires: ")
//				.Colour(RawColours.WHITE)
//				.String(ban.GetTimeUntilExpiry()).Linebreak(2)
//				.Colour(RawColours.AQUA)
//				.String("Appeal @ www.projectcitybuild.com")
//				.Build();
//
//			event.disallow(Result.KICK_BANNED, message);
//			break;
//		}
//	}
//}
