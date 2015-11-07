package com.pcb.pcbridge.bukkit.ban.commands;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.pcb.pcbridge.bukkit.ban.Ban;
import com.pcb.pcbridge.library.MessageHelper;
import com.pcb.pcbridge.library.MessageType;
import com.pcb.pcbridge.library.PlayerUUID;
import com.pcb.pcbridge.library.TimestampHelper;
import com.pcb.pcbridge.library.async.IFutureCallback;
import com.pcb.pcbridge.library.async.NonBlockingExecutor;
import com.pcb.pcbridge.library.async.NonBlockingFuture;
import com.pcb.pcbridge.library.controllers.AbstractCommand;
import com.pcb.pcbridge.library.controllers.CommandArgs;
import com.pcb.pcbridge.library.database.DbConn;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;

/**
 * Command: Ban the specified user (via username) from the server
 * 
 * - Checks if the user is already banned
 * - Kicks the player
 * - Adds their username and UUID to the ban list in storage
 */

public final class CommandBan extends AbstractCommand 
{	
	private NonBlockingExecutor _executor;
	private CommandArgs _e;
	private PlayerUUID _uuid;
	
	public boolean Execute(CommandArgs e) 
	{
		if(e.Args.length == 0)
			return false;
		
		_executor = new NonBlockingExecutor(Executors.newCachedThreadPool());
		_e = e;
		_uuid = null;
		
		// if invoked via text input, it's a permanent ban
		if(e.RouteArgs == null)
			return BanPlayer(false);
		
		// if invoked via code, it's a temporary ban
		return BanPlayer(true);
	}
	
	/**
	 * Bans the player
	 * 
	 * @param e
	 * @param isTempBan
	 */
	private boolean BanPlayer(boolean isTempBan)
	{
		NonBlockingFuture<PlayerUUID> uuidTask = GetUUIDTask();

		uuidTask.SetCallback(new IFutureCallback<PlayerUUID>()
		{
			@Override
			public void OnSuccess(PlayerUUID uuid) 
			{
				// exit if no UUID found at all - the player doesn't exist
				if(uuid.GetUUID() == "")
				{
					// TODO: prompt if the user still wants to proceed
					MessageHelper.Send(MessageType.ERROR, _e.Sender, "No UUID found. This player most likely does not exist");
					return;
				}
				
				_uuid = uuid;
				GetBanRecordTask(uuid.GetUUID());
			}

			@Override
			public void OnError(Throwable e)
			{
				MessageHelper.Send(MessageType.FATAL, _e.Sender, "Could not determine UUID of given player. Please contact an admin if this persists");
				_plugin.getLogger().severe("UUID Lookup: " + e.getMessage());
				e.printStackTrace();
			}			
		});
			
		return true;
	}
	
	
	/**
	 * Async Task: Retrieves the given username (arg 0) either internally or from Mojang
	 * 
	 * @return NonBlockingFuture
	 */
	private NonBlockingFuture<PlayerUUID> GetUUIDTask()
	{
		NonBlockingFuture<PlayerUUID> future = _executor.Submit(new Callable<PlayerUUID>()
		{
			@Override
			public PlayerUUID call() throws Exception
			{
				UUID uuid = null;
				String ip = "";
				
				// check if player is currently online
				@SuppressWarnings("deprecation")
				Player player = _plugin.getServer().getPlayer(_e.Args[0]);
				
				if(player != null)
				{
					uuid 	= player.getUniqueId();
					ip 		= player.getAddress().getHostString();
					
					return new PlayerUUID(_e.Args[0], ip, uuid, true, true, player);
				}
				
				// otherwise check if player has played before
				@SuppressWarnings("deprecation")
				OfflinePlayer offlinePlayer	= _plugin.getServer().getOfflinePlayer(_e.Args[0]);
				
				boolean hasPlayedBefore = offlinePlayer.hasPlayedBefore();			
				if(hasPlayedBefore)
				{
					uuid = offlinePlayer.getUniqueId();
				}
				else
				{
					// retrieve the player's UUID from Mojang because they've never joined the server before
					uuid = _plugin.GetUUIDFetcher().GetCurrentUUID(_e.Args[0]);
				}
				
				return new PlayerUUID(_e.Args[0], ip, uuid, false, hasPlayedBefore, null);	
			}
		});
		
		return future;
	}
	
	/**
	 * Async Task: Retrieves any ban records for the given UUID/username
	 * 
	 * @return NonBlockingFuture
	 */
	private NonBlockingFuture<Ban> GetBanRecordTask(final String uuid)
	{
		NonBlockingFuture<Ban> future = _executor.Submit(new Callable<Ban>()
		{
			@Override
			public Ban call() throws Exception
			{
				AbstractAdapter adapter = _plugin.GetAdapter(DbConn.REMOTE);
				List<HashMap<String, Object>> result = adapter.Query("SELECT * FROM pcban_active_bans WHERE is_active=1 and (banned_name=? or banned_uuid=?) LIMIT 0,1",
					_e.Args[0], uuid
				);
				
				if(result != null && result.size() > 0)
				{
					Ban ban = new Ban(result.get(0));
					
					// check if the ban has expired
					if(ban.IsTempBan() && ban.ExpiryDate <= TimestampHelper.GetNowTimestamp())
					{
						UpdateExpiredBanTask(ban);
						return null;
					}
					
					return ban;
				}
				
				return null;
			}
		});
		
		future.SetCallback(new IFutureCallback<Ban>()
		{
			@Override
			public void OnSuccess(Ban ban) 
			{
				if(ban != null)
				{
					MessageHelper.Send(MessageType.INFO, _e.Sender, _e.Args[0] + " is already banned.");
					return;				
				}
				
				CreateBanTask();
			}

			@Override
			public void OnError(Throwable e)
			{
				MessageHelper.Send(MessageType.FATAL, _e.Sender, "Failed to retrieve ban records. Please contact an admin if this persists");
				_plugin.getLogger().severe("Failed to retrieve ban records: " + e.getMessage());
				e.printStackTrace();
			}			
		});
				
		return future;
	}
	
	/**
	 * Async Task: Deactivates the given expired ban and logs it
	 * 
	 * @return NonBlockingFuture
	 */
	private NonBlockingFuture<Boolean> UpdateExpiredBanTask(final Ban ban)
	{
		NonBlockingFuture<Boolean> future = _executor.Submit(new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				AbstractAdapter adapter = _plugin.GetAdapter(DbConn.REMOTE);
				adapter.Execute("UPDATE pcban_active_bans SET is_active=0 WHERE id=?", ban.Id);
				
				adapter.Execute("INSERT INTO pcban_unbans (ban_id, date, auto_expired) VALUES (?, ?, true)",
							ban.Id, ban.ExpiryDate
						);
				
				return true;
			}
		});
		
		future.SetCallback(new IFutureCallback<Boolean>()
		{
			@Override
			public void OnSuccess(Boolean ban) { }

			@Override
			public void OnError(Throwable e)
			{
				MessageHelper.Send(MessageType.WARNING, _e.Sender, "The player's ban has expired but failed to update in the database. Please contact an admin if this persists");
				_plugin.getLogger().severe("Failed to update expired ban: " + e.getMessage());
				e.printStackTrace();
			}			
		});
				
		return future;
	}

	/**
	 * Async Task: Save the new ban in storage
	 * 
	 * @return NonBlockingFuture
	 */
	private NonBlockingFuture<Boolean> CreateBanTask()
	{
		// if given, stitch together the 'ban reason' which spans multiple args
		String banReason = "Griefing";
		int startIndex = 1;
		if(_e.Args.length > startIndex)
		{
			StringBuilder builder = new StringBuilder();
			for(int x = startIndex; x < _e.Args.length; x++)
			{
				if(x > startIndex)
					builder.append(" ");
						
				builder.append(_e.Args[x]);
			}
			banReason = builder.toString();
		}
		final String banReasonStr = banReason;
		
		// get the banner's name and uuid
		final String staffName = _e.Sender.getName();
		@SuppressWarnings("deprecation")
		final String staffUUID = _e.IsPlayer ? _plugin.getServer().getPlayer(staffName).getUniqueId().toString() : "";
		
		// create the ban in storage
		NonBlockingFuture<Boolean> future = _executor.Submit(new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				AbstractAdapter adapter = _plugin.GetAdapter(DbConn.REMOTE);
				adapter.Execute("INSERT INTO pcban_active_bans(banned_name, banned_uuid, date_ban, date_expire, staff_uuid, staff_name, reason, ip) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
						_e.Args[0], 
						_uuid.GetUUID(), 
						TimestampHelper.GetNowTimestamp(),
						null,
						staffUUID,
						staffName,
						banReasonStr, 
						_uuid.IP
					);
				
				return true;
			}
		});
		
		future.SetCallback(new IFutureCallback<Boolean>()
		{
			@Override
			public void OnSuccess(Boolean ban) 
			{ 
				_plugin.getServer().broadcastMessage(ChatColor.GRAY + _e.Args[0] + " has been banned.");
				
				// kick the player if they're online
				//if(_uuid.IsOnline && _uuid.Player != null)
				//{
					String expiry = "Never";
					String message = "Åòc" + "You have been banned.\n\n" +
									
								 	"Åò8" + "Reason: Åòf" + banReasonStr + "\n" +
									"Åò8" + "Expires: Åòf" + expiry + "\n\n" + 
												 
									"Åòb" + "Appeal @ www.projectcitybuild.com";
							
					_uuid.Player.kickPlayer(message);
				//}
			}

			@Override
			public void OnError(Throwable e)
			{
				MessageHelper.Send(MessageType.FATAL, _e.Sender, "Failed to create the new ban in storage. Please contact an admin if this persists");
				_plugin.getLogger().severe("Failed to create new ban: " + e.getMessage());
				e.printStackTrace();
			}			
		});
		
		return future;
	}
}
