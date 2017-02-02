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
package com.pcb.pcbridge.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.models.PlayerBan;
import com.pcb.pcbridge.schema.BanListContract;
import com.pcb.pcbridge.tasks.AbortTask;
import com.pcb.pcbridge.tasks.GetBansTask;
import com.pcb.pcbridge.tasks.GetPlayerIdTask;
import com.pcb.pcbridge.tasks.GetUuidTask;
import com.pcb.pcbridge.utils.MessageBuilder;
import com.pcb.pcbridge.utils.commands.AbstractCommand;
import com.pcb.pcbridge.utils.commands.CommandArgs;

public class CommandUnban extends AbstractCommand {

	@Override
	public String GetName() 
	{
		return "unban";
	}
	
	@Override
	public String GetDescription() 
	{
		return "Unbans a player from PCB";
	}

	@Override
	public String GetPermission() 
	{
		return "pcbridge.ban.unban";
	}

	@Override
	public String GetUsage() 
	{
		return "/unban <name>";
	}

	@Override
	public boolean OnExecute(CommandArgs args) 
	{
		if(args.GetArgs().length != 1)
			return false;
		
		final String username = args.GetArg(0);
		final Server server = GetEnv().GetServer();
		
		PCBridge.NewChain()
			.asyncFirst( () -> GetUuidTask.FindOrFetch(server, username) )
			.abortIfNull( AbortTask.Send(args.GetSender(), ChatColor.GRAY + "Cannot find UUID for %s. Does that player even exist?", username) )
			
			.async( uuid -> GetBans(uuid) )
			.abortIfNull( AbortTask.Send(args.GetSender(), ChatColor.GRAY + "%s is not currently banned", username) )
			
			.async( bans -> StoreUnban(bans, args) )
			.abortIfNull( AbortTask.Send(args.GetSender(), ChatColor.RED + "Failed to unban %s", username) )
			
			.syncLast(alias -> {
				String staffName = args.IsPlayer() ? args.GetSender().getName() : "CONSOLE";
				
				String message = new MessageBuilder()
					.Colour(ChatColor.DARK_GRAY, ChatColor.BOLD)
					.String(alias)
					.Colour(ChatColor.RESET, ChatColor.GRAY)
					.String(" has been unbanned by %s", staffName)
					.Build();
				
				GetEnv().GetServer().broadcastMessage(message);
			})
			.execute();
		
		return true;
	}
	
	private List<PlayerBan> GetBans(UUID uuid)
	{
		List<PlayerBan> bans = GetBansTask.GetAllByUuid(uuid.toString(), true);
		if(bans.size() == 0)
			return null;
		
		return bans;
	}
	
	/**
	 * Unbans the user from storage and logs the action
	 * 
	 * @param bans
	 * @param conn
	 * @param args
	 * @return String	Alias of the unbanned player
	 */
	private String StoreUnban(List<PlayerBan> bans, CommandArgs args)
	{
		try(Connection conn = PCBridge.GetConnectionPool().GetConnection(BanListContract.DATABASE))
		{
			String staffAlias = "CONSOLE";
			String staffUuid = "CONSOLE";
			if(args.IsPlayer())
			{
				Player staff = (Player)args.GetSender();
				staffAlias = staff.getName();
				staffUuid = staff.getUniqueId().toString();
			}
			
			int staffId = GetPlayerIdTask.Run(conn, staffUuid, staffAlias);					
			
			String alias = null;
			for(PlayerBan ban : bans)
			{	
				int id = ban.Id;
				alias = ban.PlayerAlias;
				try(PreparedStatement stmt = conn.prepareStatement(
						"UPDATE " + BanListContract.TableBans.TABLE_NAME
						+ " SET " + BanListContract.TableBans.COL_IS_BANNED + " = false"
						+ " WHERE " + BanListContract.TableBans._ID + " = ?"))
				{
					stmt.setInt(1, id);
					stmt.executeUpdate();
				}		
				
				try(PreparedStatement stmt = conn.prepareStatement(
						"INSERT INTO " + BanListContract.TableUnbans.TABLE_NAME + " (" 
						+ BanListContract.TableUnbans.COL_BAN_ID + ","
						+ BanListContract.TableUnbans.COL_STAFF_ID + ","
						+ BanListContract.TableUnbans.COL_TIMESTAMP + ") "
						+ "VALUES (?,?,?)"))
				{	
					stmt.setInt(1, id);
					stmt.setInt(2, staffId);
					stmt.setLong(3, System.currentTimeMillis() / 1000L);
					stmt.executeUpdate();
				}
			}
			
			return alias;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return null;
		}
		
	}

}
