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
package com.pcb.pcbridge.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.schema.ForumContract;

public class SyncPlayerRankTask {
	
	/**
	 * Fetches a list of group ids that the given username belongs to on the forums
	 * 
	 * @param alias
	 * @return
	 */
	public static List<String> FetchRank(String alias)
	{
		try(Connection conn = PCBridge.GetConnectionPool().GetConnection(ForumContract.Database))
		{
			try(PreparedStatement stmt = conn.prepareStatement(
					"SELECT " 
							+ ForumContract.TableMembers.COL_PRIMARY_GROUP + "," 
							+ ForumContract.TableMembers.COL_SECONDARY_GROUPS
					+ " FROM " + ForumContract.TableMembers.TABLE_NAME
					+ " WHERE " + ForumContract.TableMembers.COL_ALIAS + " = ? LIMIT 1"))
			{
				stmt.setString(1, alias);
				ResultSet rs = stmt.executeQuery();
				
				List<String> groups = new ArrayList<>();
				if(rs.first())
				{
					do
					{
						groups.add( rs.getString(ForumContract.TableMembers.COL_PRIMARY_GROUP) );
						
						String secondary = rs.getString(ForumContract.TableMembers.COL_SECONDARY_GROUPS);
						groups.addAll( Arrays.asList(secondary.split(",")) );
					}
					while(rs.next());
				}
				
				return groups;
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Synchronises the given player's in-game rank with their forum rank
	 * 
	 * @param permissions
	 * @param player
	 */
	public static void Sync(Player player)
	{	
		String alias = player.getName();
		Permission permissions = PCBridge.GetVaultHook().GetPermission();
		
		// remove the player's current groups first
		String[] allGroups = permissions.getPlayerGroups(player);
		for(String group : allGroups)
		{
			permissions.playerRemoveGroup(null, player, group);
		}
		
		List<String> groups = FetchRank(alias);
		
		// if no forum account, assign the player to Guest
		if(groups.size() == 0)
		{
			permissions.playerAddGroup(null, player, "Guest");
			return;
		}
						
		// assign each forum group with its corresponding in-game PEX group
		// TODO: this shouldn't be hardcoded
		Boolean hasRegisteredGroup = false;
		for(String id : groups)
		{
			switch(id)
			{
				case "1":
					permissions.playerAddGroup(null, player, "Admin");
					hasRegisteredGroup = true;
					break;
				case "13":
					permissions.playerAddGroup(null, player, "SOP");
					hasRegisteredGroup = true;
					break;
				case "2":
					permissions.playerAddGroup(null, player, "OP");
					hasRegisteredGroup = true;
					break;
				case "14":
					permissions.playerAddGroup(null, player, "Moderator");
					hasRegisteredGroup = true;
					break;
				case "21":
					permissions.playerAddGroup(null, player, "Trusted");
					hasRegisteredGroup = true;
					break;
				case "11":
					permissions.playerAddGroup(null, player, "Donator");
					hasRegisteredGroup = true;
					break;
				case "25":
					permissions.playerAddGroup(null, player, "Guide");
					hasRegisteredGroup = true;
					break;
				case "26":
					permissions.playerAddGroup(null, player, "Retired");
					hasRegisteredGroup = true;
					break;
				default:
					continue;
			}
		}
		
		// if the user has an account but no registered group, give them Member
		if(!hasRegisteredGroup)
		{
			permissions.playerAddGroup(null, player, "Member");
		}
		
	}	
	
}
