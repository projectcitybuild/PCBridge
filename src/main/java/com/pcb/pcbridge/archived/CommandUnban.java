//package com.pcb.pcbridge.archived;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.util.List;
//import java.util.UUID;
//
//import org.spigot.ChatColor;
//import org.spigot.Server;
//import org.spigot.entity.Player;
//
//import com.pcb.pcbridge.PCBridge;
//import com.pcb.pcbridge.framework.commands.AbstractCommand;
//
//public class CommandUnban extends AbstractCommand {
//
//	@Override
//	public String GetName()
//	{
//		return "unban";
//	}
//
//	@Override
//	public String GetDescription()
//	{
//		return "Unbans a player from PCB";
//	}
//
//	@Override
//	public String GetPermission()
//	{
//		return "pcbridge.ban.unban";
//	}
//
//	@Override
//	public String GetUsage()
//	{
//		return "/unban <name>";
//	}
//
//	@Override
//	public boolean OnExecute(CommandArgs args)
//	{
//		if(args.GetArgs().length != 1)
//			return false;
//
//		final String username = args.GetArg(0);
//		final Server server = GetEnv().GetServer();
//
//		PCBridge.NewChain()
//			.asyncFirst( () -> GetUuidTask.FindOrFetch(server, username) )
//			.abortIfNull( AbortTask.Send(args.GetSender(), ChatColor.GRAY + "Cannot find UUID for %s. Does that player even exist?", username) )
//
//			.async( uuid -> GetBans(uuid) )
//			.abortIfNull( AbortTask.Send(args.GetSender(), ChatColor.GRAY + "%s is not currently banned", username) )
//
//			.async( bans -> StoreUnban(bans, args) )
//			.abortIfNull( AbortTask.Send(args.GetSender(), ChatColor.RED + "Failed to unban %s", username) )
//
//			.syncLast(alias -> {
//				String staffName = args.IsPlayer() ? args.GetSender().getName() : "CONSOLE";
//
//				String message = new MessageBuilder()
//					.Colour(ChatColor.DARK_GRAY, ChatColor.BOLD)
//					.String(alias)
//					.Colour(ChatColor.RESET, ChatColor.GRAY)
//					.String(" has been unbanned by %s", staffName)
//					.Build();
//
//				GetEnv().GetServer().broadcastMessage(message);
//			})
//			.execute();
//
//		return true;
//	}
//
//	private List<PlayerBan> GetBans(UUID uuid)
//	{
//		List<PlayerBan> bans = GetBansTask.GetAllByUuid(uuid.toString(), true);
//		if(bans.size() == 0)
//			return null;
//
//		return bans;
//	}
//
//	/**
//	 * Unbans the user from storage and logs the action
//	 *
//	 * @param bans
//	 * @param conn
//	 * @param args
//	 * @return String	Alias of the unbanned player
//	 */
//	private String StoreUnban(List<PlayerBan> bans, CommandArgs args)
//	{
//		try(Connection conn = PCBridge.GetConnectionPool().GetConnection(BanListContract.DATABASE))
//		{
//			String staffAlias = "CONSOLE";
//			String staffUuid = "CONSOLE";
//			if(args.IsPlayer())
//			{
//				Player staff = (Player)args.GetSender();
//				staffAlias = staff.getName();
//				staffUuid = staff.getUniqueId().toString();
//			}
//
//			int staffId = GetPlayerIdTask.Run(conn, staffUuid, staffAlias);
//
//			String alias = null;
//			for(PlayerBan ban : bans)
//			{
//				int id = ban.Id;
//				alias = ban.PlayerAlias;
//				try(PreparedStatement stmt = conn.prepareStatement(
//						"UPDATE " + BanListContract.TableBans.TABLE_NAME
//						+ " SET " + BanListContract.TableBans.COL_IS_BANNED + " = false"
//						+ " WHERE " + BanListContract.TableBans._ID + " = ?"))
//				{
//					stmt.setInt(1, id);
//					stmt.executeUpdate();
//				}
//
//				try(PreparedStatement stmt = conn.prepareStatement(
//						"INSERT INTO " + BanListContract.TableUnbans.TABLE_NAME + " ("
//						+ BanListContract.TableUnbans.COL_BAN_ID + ","
//						+ BanListContract.TableUnbans.COL_STAFF_ID + ","
//						+ BanListContract.TableUnbans.COL_TIMESTAMP + ") "
//						+ "VALUES (?,?,?)"))
//				{
//					stmt.setInt(1, id);
//					stmt.setInt(2, staffId);
//					stmt.setLong(3, System.currentTimeMillis() / 1000L);
//					stmt.executeUpdate();
//				}
//			}
//
//			return alias;
//		}
//		catch(SQLException e)
//		{
//			e.printStackTrace();
//			return null;
//		}
//
//	}
//
//}
