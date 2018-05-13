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
//	/**
//	 * If a player needs to be rescued on join, teleport them to the
//	 * given coordinate
//	 *
//	 * @param coordinate
//	 */
//	private void RescuePlayer(Player player, PlayerConfig config)
//	{
//		if(!config.NeedsRescue || config.RescueCoords == null)
//			return;
//
//		player.teleport(config.RescueCoords);
//
//		config.NeedsRescue = false;
//		config.RescueCoords = null;
//
//		try
//		{
//			config.Save();
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//	}
//
//	private void FetchWarnings(Player player)
//	{
//		PCBridge.NewChain()
//			.asyncFirst( () -> {
//				int warnings = 0;
//
//				String database = GetEnv().GetConfig().getString("database.warnings.database");
//				try(Connection conn = PCBridge.GetConnectionPool().GetConnection(database))
//				{
//					String selectQuery = "SELECT COUNT(*) "
//							+ " FROM " + WarningContract.TableWarnings.TABLE_NAME + " AS s"
//							+ " LEFT JOIN " + PlayerContract.TablePlayers.TABLE_NAME + " AS t1"
//							+ " ON s." + WarningContract.TableWarnings.COL_PLAYER_ID + " = t1." + PlayerContract.TablePlayers._ID
//							+ " WHERE t1." + PlayerContract.TablePlayers.COL_UUID + " = ?";
//
//					try(PreparedStatement stmt = conn.prepareStatement(selectQuery))
//					{
//						stmt.setString(1, player.getUniqueId().toString());
//						ResultSet results = stmt.executeQuery();
//
//						if(results.first())
//							warnings = results.getInt(1);
//					}
//				}
//				catch(SQLException e)
//				{
//					e.printStackTrace();
//				}
//
//				return warnings;
//			})
//			.syncLast(warnings -> {
//
//				if(warnings == 0)
//					return;
//
//				GetEnv().GetServer().getOnlinePlayers().forEach(onlinePlayer ->
//				{
//					if(onlinePlayer.hasPermission("pcbridge.warn.notify"))
//					{
//						String msg = new MessageBuilder()
//							.Colour(ChatColor.RED)
//							.When(warnings == 1, b -> {
//								return b.String("@staff: %s has 1 warning on record", player.getName());
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