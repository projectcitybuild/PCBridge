/*
 * The MIT License
 *
 * Copyright 2016 Andy Saw
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.pcb.pcbridge.pcbridge.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.pcb.pcbridge.Environment;
import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.pcbridge.models.PlayerWarning;
import com.pcb.pcbridge.pcbridge.schema.PlayerContract;
import com.pcb.pcbridge.pcbridge.schema.WarningContract;
import com.pcb.pcbridge.pcbridge.tasks.AbortTask;
import com.pcb.pcbridge.pcbridge.tasks.GetPlayerIdTask;
import com.pcb.pcbridge.pcbridge.tasks.GetUuidTask;
import com.pcb.pcbridge.utils.MessageBuilder;
import com.pcb.pcbridge.utils.commands.AbstractCommand;
import com.pcb.pcbridge.utils.commands.CommandArgs;

public class CommandWarn extends AbstractCommand {

	@Override
	public String GetName() 
	{
		return "warn";
	}
	
	@Override
	public String GetDescription() 
	{
		return "Stores a warning about a player for misconduct (and notifies them)";
	}

	@Override
	public String GetPermission() 
	{
		return "pcbridge.warn";
	}

	@Override
	public String GetUsage()
	{
		return "/warn <name> new|silent [reason]\n/warn list <name>";
	}

	@Override
	public boolean OnExecute(CommandArgs args) 
	{
		if(args.GetArgs().length == 0)
			return false;
		
		if(args.GetArg(0).equalsIgnoreCase("list"))
			return ListWarnings(args);
		
		return StoreWarning(args);
	}
	
	/**
	 * Lists all the warnings for the given player
	 * 
	 * @param args
	 * @return
	 */
	private boolean ListWarnings(CommandArgs args)
	{
		if(args.GetArgs().length != 2)
			return false;
		
		String alias = args.GetArg(1);
		
		PCBridge.NewChain()
			.asyncFirst( () -> GetUuidTask.FindOrFetch(GetEnv().GetServer(), alias) )
			.abortIfNull( AbortTask.Send(args.GetSender(), ChatColor.GRAY + "Cannot find UUID for %s. Does that player even exist?", alias) )
			
			.async(uuid -> {				
				List<PlayerWarning> warnings = new ArrayList<>();
				
				String database = GetEnv().GetConfig().getString("database.warnings.database");
				try(Connection conn = PCBridge.GetConnectionPool().GetConnection(database))
				{
					String selectQuery = "SELECT *,"
							+ " t1." + PlayerContract.TablePlayers.COL_ALIAS + " AS playerAlias,"
							+ " t1." + PlayerContract.TablePlayers.COL_UUID + " AS playerUuid," 
							+ " t2." + PlayerContract.TablePlayers.COL_ALIAS + " AS staffAlias,"
							+ " t2." + PlayerContract.TablePlayers.COL_UUID + " AS staffUuid"
							+ " FROM " + WarningContract.TableWarnings.TABLE_NAME + " AS s"
							+ " LEFT JOIN " + PlayerContract.TablePlayers.TABLE_NAME + " AS t1"
							+ " ON s." + WarningContract.TableWarnings.COL_PLAYER_ID + " = t1." + PlayerContract.TablePlayers._ID
							+ " LEFT JOIN " + PlayerContract.TablePlayers.TABLE_NAME + " AS t2"
							+ " ON s." + WarningContract.TableWarnings.COL_STAFF_ID + " = t2." + PlayerContract.TablePlayers._ID
							+ " WHERE t1." + PlayerContract.TablePlayers.COL_UUID + " = ?"
							+ " ORDER BY s." + WarningContract.TableWarnings.COL_TIMESTAMP + " DESC";
					
					try(PreparedStatement stmt = conn.prepareStatement(selectQuery))
					{
						stmt.setString(1, uuid.toString());
						ResultSet results = stmt.executeQuery();
						
						if(results.first())
						{
							do
							{
								PlayerWarning warning = new PlayerWarning();
								warning.PlayerName 	= results.getString("playerAlias");
								warning.PlayerUUID 	= results.getString("playerUuid");
								warning.StaffName 	= results.getString("staffAlias");
								warning.StaffUUID 	= results.getString("staffUuid");
								warning.Reason 		= results.getString(WarningContract.TableWarnings.COL_REASON);
								warning.Timestamp 	= results.getLong(WarningContract.TableWarnings.COL_TIMESTAMP);
								
								warnings.add(warning);
							}
							while(results.next());
						}						
					}
				}
				catch(SQLException e)
				{
					e.printStackTrace();
				}
				
				return warnings;				
			})
			
			.syncLast( warnings -> {
				
				if(warnings.size() == 0)
				{
					String msg = new MessageBuilder()
						.String(ChatColor.GRAY + "%s has no warnings on record.", alias)
						.Build();
					
					args.GetSender().sendMessage(msg);
				}
				else
				{
					MessageBuilder msg = new MessageBuilder()
						.Colour(ChatColor.BOLD)
						.Stringln("Warnings for %s (%s)", alias, warnings.size())
						.Reset();
					
					for(PlayerWarning warning : warnings)
					{
						SimpleDateFormat formatter = Environment.DateFormat.Short();
						
						Date dtWarn = new Date();
						dtWarn.setTime((long)warning.Timestamp * 1000);
						String date = formatter.format(dtWarn);
						
						msg.Stringln("Warned by %s on %s: %s", warning.StaffName, date, warning.Reason);
					}
					
					args.GetSender().sendMessage(msg.Build());
				}
				
			})
			.execute();
		
		return true;
	}
	
	/**
	 * Warns a player and stores it in the database
	 * 
	 * @param args
	 * @return
	 */
	private boolean StoreWarning(CommandArgs args)
	{
		if(args.GetArgs().length < 3)
			return false;
		
		if(!args.GetArg(1).equalsIgnoreCase("new") && !args.GetArg(1).equalsIgnoreCase("silent"))
			return false;
		
		boolean isSilent = args.GetArg(1).equalsIgnoreCase("silent");
		
		
		StringBuilder builder = new StringBuilder();
		for(int i = 2; i < args.GetArgs().length; i++)
		{
			if(i > 2) builder.append(" ");
			builder.append(args.GetArg(i));
		}
		
		PlayerWarning warning = new PlayerWarning();
		
		warning.Reason = builder.toString();
		warning.PlayerName = args.GetArg(0);
		warning.Timestamp = System.currentTimeMillis() / 1000L;
		warning.StaffName = "CONSOLE";
		warning.StaffUUID = "CONSOLE";
		
		if(args.IsPlayer())
		{
			warning.StaffName = args.GetPlayer().getName();
			warning.StaffUUID = args.GetPlayer().getUniqueId().toString();
		}
		
		PCBridge.NewChain()
			.asyncFirst( () -> GetUuidTask.FindOrFetch(GetEnv().GetServer(), warning.PlayerName) )
			.abortIfNull( AbortTask.Send(args.GetSender(), ChatColor.GRAY + "Cannot find UUID for %s. Does that player even exist?", warning.PlayerName) )
			
			.async(playerUUID -> {
				
				String database = GetEnv().GetConfig().getString("database.warnings.database");
				try(Connection conn = PCBridge.GetConnectionPool().GetConnection(database))
				{
					// get the ids of player and staff
					int playerId = GetPlayerIdTask.Run(conn, playerUUID.toString(), warning.PlayerName);
					int staffId = GetPlayerIdTask.Run(conn, warning.StaffUUID, warning.StaffName);
					
					// insert the warning into storage
					try(PreparedStatement stmt = conn.prepareStatement(
							"INSERT INTO " + WarningContract.TableWarnings.TABLE_NAME + " (" 
							+ WarningContract.TableWarnings.COL_PLAYER_ID + ","
							+ WarningContract.TableWarnings.COL_STAFF_ID + ","
							+ WarningContract.TableWarnings.COL_REASON + ","
							+ WarningContract.TableWarnings.COL_TIMESTAMP + ") "
							+ "VALUES (?,?,?,?)"))
					{
						stmt.setInt(1, playerId);
						stmt.setInt(2, staffId);
						stmt.setObject(3, warning.Reason);
						stmt.setLong(4, warning.Timestamp);
						stmt.executeUpdate();
					}
					
					return playerUUID;
				}
				catch(SQLException e)
				{
					e.printStackTrace();
					return null;
				}
				
			})
			.abortIfNull( AbortTask.Send(args.GetSender(), ChatColor.RED + "Failed to create warning for %s", warning.PlayerName) )
			
			.syncLast( playerUUID -> {
				
				// notify the receiver if necessary
				if(!isSilent)
				{
					Player player = GetEnv().GetServer().getPlayer(playerUUID);
					if(player.isOnline())
					{
						String playerMsg = new MessageBuilder()
							.Colour(ChatColor.RED, ChatColor.BOLD)
							.Stringln("You received a warning from %s", warning.StaffName).Reset()
							.String(warning.Reason)
							.Build();
						
						player.sendMessage(playerMsg);
					}					
				}
				
				// notify the command sender
				String msg = new MessageBuilder()
						.When(isSilent, b -> {
							return b.Colour(ChatColor.GRAY)
									.String("Warning silently registered for %s", warning.PlayerName);
						}, b -> {
							return b.Colour(ChatColor.GRAY)
									.String("Warning stored and sent to %s", warning.PlayerName);
						})
						.Build();
				
				args.GetSender().sendMessage(msg);
			})
			.execute();
		
		return true;
	}

}
