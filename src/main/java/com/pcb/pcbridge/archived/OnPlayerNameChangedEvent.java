//package com.pcb.pcbridge.archived;
//
//import org.spigot.event.EventHandler;
//import org.spigot.event.EventPriority;
//
//public final class OnPlayerNameChangedEvent extends AbstractListener
//{
//	/**
//	 * Sets the player's Tab list name whenever their name changes
//	 *
//	 * @param event
//	 */
//	@EventHandler(priority = EventPriority.HIGHEST)
//	public void OnPlayerNameChanged(PlayerNameChangedEvent event)
//	{
//		String name = GetPlayerNameTask.GetFormattedName(GetEnv(), event.GetPlayer());
//
//        event.GetPlayer().setPlayerListName(name);
//	}
//}