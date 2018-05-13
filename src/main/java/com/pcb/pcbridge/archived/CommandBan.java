//package com.pcb.pcbridge.archived;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import org.spigot.ChatColor;
//import org.spigot.OfflinePlayer;
//import org.spigot.Server;
//import org.spigot.entity.Player;
//
//import com.pcb.pcbridge.PCBridge;
//import com.pcb.pcbridge.archived.MessageBuilder.RawColours;
//import com.pcb.pcbridge.archived.MessageBuilder.RawFormats;
//import com.pcb.pcbridge.framework.commands.AbstractCommand;
//
//public class CommandBan extends AbstractCommand {
//
//	@Override
//	public String GetName()
//	{
//		return "ban";
//	}
//
//	@Override
//	public String GetDescription()
//	{
//		return "Bans a player permanently from PCB";
//	}
//
//	@Override
//	public String GetPermission()
//	{
//		return "pcbridge.ban.ban";
//	}
//
//	@Override
//	public String GetUsage()
//	{
//		return "/ban <name> [reason]";
//	}
//
//	@Override
//	public String[] GetAliases()
//	{
//		return new String[] { "permban" };
//	}
//
//	@Override
//	public List<String> OnTabComplete(CommandArgs args)
//	{
//		if(args.GetArgs().length > 0)
//			return null;
//
//		List<String> players = new ArrayList<>();
//		GetEnv().GetServer().getOnlinePlayers().forEach( player -> players.add(player.getName()) );
//
//		return players;
//	}
//
//	@Override
//	public boolean OnExecute(CommandArgs args)
//	{
//		if(args.GetArgs().length < 1)
//			return false;
//
//		final String alias = args.GetArg(0);
//		final Server server = GetEnv().GetServer();
//
//		PCBridge.NewChain()
//			.asyncFirst( () -> GetUuidTask.FindOrFetch(server, alias) )
//			.abortIfNull( AbortTask.Send(args.GetSender(), ChatColor.GRAY + "Cannot find UUID for %s. Does that player even exist?", alias) )
//
//			.async( uuid -> CheckIfBanned(uuid) )
//			.abortIfNull( AbortTask.Send(args.GetSender(), ChatColor.GRAY + "%s is already banned", alias) )
//
//			.sync( uuid -> CreateBan(uuid, server, alias, args) )
//			.asyncLast( ban -> StoreBan(ban) )
//			.execute();
//
//		return true;
//	}
//
//	/**
//	 * Checks if the given UUID is already banned.
//	 * Returns null if banned, or passes the UUID back if not banned
//	 *
//	 * @param uuid
//	 * @return UUID
//	 * @throws SQLException
//	 */
//	private UUID CheckIfBanned(UUID uuid)
//	{
//		List<PlayerBan> bans = GetBansTask.GetAllByUuid(uuid.toString(), true);
//		if(bans.size() == 1)
//			return null;
//
//		return uuid;
//	}
//
//	/**
//	 * Gathers details such as banned player, staff who banned them, etc
//	 * and returns a new PlayerBan data model
//	 *
//	 * @param uuid
//	 * @param server
//	 * @param username
//	 * @param args
//	 * @return
//	 */
//	private PlayerBan CreateBan(UUID uuid, Server server, String username, CommandArgs args)
//	{
//		Player player = server.getPlayer(uuid);
//		String alias;
//
//		boolean isOnline = true;
//		if(player != null)
//		{
//			alias = player.getName();
//		}
//		else
//		{
//			OfflinePlayer offPlayer = server.getOfflinePlayer(uuid);
//			isOnline = false;
//
//			if(offPlayer.hasPlayedBefore())
//				alias = offPlayer.getName();
//			else
//				// player has never joined the server before, so use the supplied username
//				alias = username;
//		}
//
//		// get the 'ban reason' by gluing-together every argument after the name
//		int startIndex = args.GetData("BAN_LENGTH") == null ? 1 : 2;
//		String banReason = null;
//		if(args.GetArgs().length == startIndex)
//		{
//			banReason = null;
//		}
//		else
//		{
//			StringBuilder builder = new StringBuilder();
//			for(int i = startIndex; i < args.GetArgs().length; i++)
//			{
//				builder.append(args.GetArg(i));
//
//				if(i < args.GetArgs().length)
//					builder.append(" ");
//			}
//			banReason = builder.toString();
//		}
//
//		// determine who banned the player
//		String staffAlias = "CONSOLE";
//		String staffUuid = "CONSOLE";
//		if(args.IsPlayer())
//		{
//			Player staff = (Player)args.GetSender();
//			staffAlias = staff.getName();
//			staffUuid = staff.getUniqueId().toString();
//		}
//
//		PlayerBan ban = new PlayerBan();
//		ban.PlayerAlias = alias;
//		ban.PlayerUUID = uuid.toString();
//		ban.StaffAlias = staffAlias;
//		ban.StaffUUID = staffUuid;
//		ban.Reason = banReason;
//		ban.Timestamp = System.currentTimeMillis() / 1000L;
//		ban.ExpiryTimestamp = (Long) args.GetData("BAN_LENGTH");
//
//
//		// if player is online, boot them
//		if(isOnline)
//		{
//			String kickMessage = new MessageBuilder()
//				.Colour(RawColours.RED, RawFormats.BOLD)
//				.String("You have been banned.").Linebreak(2)
//				.RawReset()
//				.Colour(RawColours.GRAY)
//				.String("Reason: ")
//				.Colour(RawColours.WHITE)
//				.String(banReason).Linebreak()
//				.Colour(RawColours.GRAY)
//				.String("Expires: ")
//				.Colour(RawColours.WHITE)
//				.String(ban.GetTimeUntilExpiry()).Linebreak(2)
//				.Colour(MessageBuilder.RawColours.AQUA)
//				.String("Appeal @ www.projectcitybuild.com")
//				.Build();
//
//			player.kickPlayer(kickMessage);
//		}
//
//		String banMessage = new MessageBuilder()
//			.Colour(ChatColor.DARK_GRAY, ChatColor.BOLD)
//			.String(alias)
//			.Colour(ChatColor.RESET, ChatColor.GRAY)
//			.String(" has been banned by %s: %s", staffAlias, banReason)
//			.Build();
//
//		server.broadcastMessage(banMessage);
//
//		return ban;
//	}
//
//	/**
//	 * Takes in a PlayerBan data-object and creates an actual
//	 * ban entry in the database
//	 *
//	 * @param ban
//	 * @return
//	 * @throws SQLException
//	 */
//	private void StoreBan(PlayerBan ban)
//	{
//		try(Connection conn = PCBridge.GetConnectionPool().GetConnection(BanListContract.DATABASE))
//		{
//			// get the ids of player and staff
//			int playerId = GetPlayerIdTask.Run(conn, ban.PlayerUUID, ban.PlayerAlias);
//			int staffId = GetPlayerIdTask.Run(conn, ban.StaffUUID, ban.StaffAlias);
//
//			// insert the ban into storage
//			try(PreparedStatement stmt = conn.prepareStatement(
//					"INSERT INTO " + BanListContract.TableBans.TABLE_NAME + " ("
//					+ BanListContract.TableBans.COL_PLAYER_ID + ","
//					+ BanListContract.TableBans.COL_STAFF_ID + ","
//					+ BanListContract.TableBans.COL_REASON + ","
//					+ BanListContract.TableBans.COL_SERVER + ","
//					+ BanListContract.TableBans.COL_TIMESTAMP + ","
//					+ BanListContract.TableBans.COL_UNBAN_ON + ","
//					+ BanListContract.TableBans.COL_IS_BANNED + ") "
//					+ "VALUES (?,?,?,?,?,?,?)"))
//			{
//				stmt.setInt(1, playerId);
//				stmt.setInt(2, staffId);
//				stmt.setObject(3, ban.Reason);
//				stmt.setString(4, "Minecraft");
//				stmt.setLong(5, ban.Timestamp);
//				stmt.setObject(6, ban.ExpiryTimestamp);
//				stmt.setBoolean(7, true);
//				stmt.executeUpdate();
//			}
//		}
//		catch(SQLException e)
//		{
//			e.printStackTrace();
//		}
//
//	}
//
//}
