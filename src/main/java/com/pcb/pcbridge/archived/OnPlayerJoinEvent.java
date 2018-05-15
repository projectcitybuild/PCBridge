//package com.pcb.pcbridge.archived;
//
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.UUID;
//
//import org.spigot.ChatColor;
//import org.spigot.entity.Player;
//import org.spigot.event.EventHandler;
//import org.spigot.event.EventPriority;
//import org.spigot.event.player.PlayerJoinEvent;
//
//import com.pcb.pcbridge.PCBridge;
//import com.pcb.pcbridge.spigot.warnings.schema.WarningContract;
//
//public final class OnPlayerJoinEvent extends AbstractListener
//{
//	/**
//	 * Called after a player has logged in to the server
//	 *
//	 * @param event
//	 */
//	@EventHandler(priority = EventPriority.MONITOR)
//	public void OnPlayerJoin(PlayerJoinEvent event)
//	{
//		GetPlayerConfig(event.getPlayer());
//
//		// set the player's Tab list name, but we need to delay it by a few
//		// ticks, or else Bukkit will immediately overwrite it
//		GetEnv().GetServer().getScheduler().scheduleSyncDelayedTask(GetEnv().GetPlugin(), () -> {
//			GetEnv().BroadcastEvent( new PlayerNameChangedEvent(event.getPlayer()) );
//		}, 5);
//
//		FetchWarnings(event.getPlayer());
//	}
//
//	@EventHandler(priority = EventPriority.HIGHEST)
//	public void OnPluginEnabled(PluginEnabledEvent event)
//	{
//		GetEnv().GetServer().getOnlinePlayers().forEach( player -> GetPlayerConfig(player) );
//	}
//
//	/**
//	 * Loads (or creates) the player's file from storage
//	 */
//	private void GetPlayerConfig(Player player)
//	{
//		PCBridge.NewChain()
//			.asyncFirst( () -> {
//				// get or create PlayerConfig from storage
//				UUID uuid = player.getUniqueId();
//				PlayerConfig config = new PlayerConfig(GetEnv().GetPlayerFolder(), uuid);
//				GetEnv().GetPlayerCache().Remember(uuid, config);
//
//				return config;
//			})
//			.syncLast( config -> {
//				RescuePlayer(player, config);
//			})
//			.execute();
//	}
//
//							}, b -> {
//								return b.String("@staff: %s has %s warnings on record", player.getName(), warnings);
//							})
//							.Build();
//
//						onlinePlayer.sendMessage(msg);
//					}
//
//				});
//
//
//			})
//			.execute();
//	}
//}