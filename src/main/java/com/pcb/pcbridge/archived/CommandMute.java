//package com.pcb.pcbridge.archived;
//
//import java.io.IOException;
//
//import org.spigot.ChatColor;
//
//import com.pcb.pcbridge.PCBridge;
//import com.pcb.pcbridge.framework.commands.AbstractCommand;
//
//public class CommandMute extends AbstractCommand {
//
//	@Override
//	public String GetName()
//	{
//		return "mute";
//	}
//
//	@Override
//	public String GetDescription()
//	{
//		return "Prevents a player from sending chat messages (does not prevent commands)";
//	}
//
//	@Override
//	public String GetPermission()
//	{
//		return "pcbridge.ban.mute";
//	}
//
//	@Override
//	public String GetUsage()
//	{
//		return "/mute <name>";
//	}
//
//	@Override
//	public boolean OnExecute(CommandArgs args)
//	{
//		if(args.GetArgs().length != 1)
//			return false;
//
//		String alias = args.GetArg(0);
//
//		PCBridge.NewChain()
//			.asyncFirst( () -> GetUuidTask.FindOrFetch(GetEnv().GetServer(), alias) )
//			.async(uuid -> GetPlayerCacheTask.Get(GetEnv(), uuid))
//			.async(config -> {
//				// check if this command was routed from /unmute
//				boolean setToMute = args.GetData("UNMUTE") == null;
//
//				// don't set a value if the value has not changed
//				if(config.IsMuted == setToMute)
//					return setToMute;
//
//				System.out.println(config.IsMuted);
//				config.IsMuted = setToMute;
//				try
//				{
//					config.Save();
//				}
//				catch(IOException e)
//				{
//					e.printStackTrace();
//				}
//
//				args.AddData("IS_DIRTY", true);
//				return setToMute;
//			})
//			.syncLast(isMuted -> {
//
//				// if no change occured, let the user know
//				if(args.GetData("IS_DIRTY") == null)
//				{
//					String message = isMuted
//							? ChatColor.RED + String.format("%s is already muted", alias)
//							: ChatColor.RED + String.format("%s is not muted", alias);
//
//					args.GetSender().sendMessage(message);
//					return;
//				}
//
//				// otherwise broadcast their un/mute
//				String message = isMuted
//						? ChatColor.YELLOW + String.format("%s has been muted", alias)
//						: ChatColor.YELLOW + String.format("%s has been unmuted", alias);
//
//				GetEnv().GetServer().broadcastMessage(message);
//
//			})
//			.execute();
//
//		return true;
//	}
//
//}
