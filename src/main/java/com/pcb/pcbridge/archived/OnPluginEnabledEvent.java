//package com.pcb.pcbridge.archived;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.spigot.entity.Player;
//import org.spigot.event.EventHandler;
//import org.spigot.event.EventPriority;
//
//import com.pcb.pcbridge.PCBridge;
//
//public final class OnPluginEnabledEvent extends AbstractListener
//{
//	@EventHandler(priority = EventPriority.HIGHEST)
//	public void OnPluginEnabled(PluginEnabledEvent event)
//	{
//		CheckForBannedPlayers();
//	}
//
//	/**
//	 * Checks if any banned players somehow snuck onto the server
//	 * while it was enabling/reloading
//	 */
//	private void CheckForBannedPlayers()
//	{
//		GetEnv().GetLogger().info("Checking if any online players are banned...");
//
//		PCBridge.NewChain()
//			.syncFirst(() -> GetEnv().GetServer().getOnlinePlayers())
//			.async(onlinePlayers -> {
//				List<Player> playersToBoot = new ArrayList<>();
//				for(Player player : onlinePlayers)
//				{
//					List<PlayerBan> bans = GetBansTask.GetAllByUuid(player.getUniqueId().toString(), true);
//					if(bans.size() > 0)
//						playersToBoot.add(player);
//				}
//
//				return playersToBoot;
//			})
//			.abortIfNull()
//			.syncLast(playersToBoot -> {
//				for(Player player : playersToBoot)
//				{
//					player.kickPlayer("You are currently banned.");
//				}
//
//				GetEnv().GetLogger().info("Player check complete.");
//			})
//			.execute();
//	}
//}
