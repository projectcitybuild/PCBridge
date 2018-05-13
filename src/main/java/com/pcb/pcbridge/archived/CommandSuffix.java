//package com.pcb.pcbridge.archived;
//
//import org.spigot.ChatColor;
//import org.spigot.entity.Player;
//
//import com.pcb.pcbridge.PCBridge;
//import com.pcb.pcbridge.framework.commands.AbstractCommand;
//
//public class CommandSuffix extends AbstractCommand {
//
//	@Override
//	public String GetName()
//	{
//		return "suffix";
//	}
//
//	@Override
//	public String GetDescription()
//	{
//		return "Manually adds a suffix to the given player.";
//	}
//
//	@Override
//	public String GetPermission()
//	{
//		return "pcbridge.chat.suffix";
//	}
//
//	@Override
//	public String GetUsage()
//	{
//		return "/suffix <name> <prefix|off>";
//	}
//
//	@Override
//	public boolean OnExecute(CommandArgs args)
//	{
//		if(args.GetArgs().length < 2)
//			return false;
//
//		PCBridge.NewChain()
//			.asyncFirst( () -> GetUuidTask.FindOrFetch(GetEnv().GetServer(), args.GetArg(0)) )
//			.abortIfNull()
//			.async(uuid -> GetPlayerCacheTask.Get(GetEnv(), uuid))
//			.syncLast(config -> {
//
//				StringBuilder builder = new StringBuilder();
//				for(int i = 1; i < args.GetArgs().length; i++)
//				{
//					builder.append(args.GetArg(i));
//
//					if(i < (args.GetArgs().length - 1))
//						builder.append(" ");
//				}
//
//				String suffix = builder.toString();
//				config.Suffix = suffix.equalsIgnoreCase("off") ? null : suffix;
//
//				try
//				{
//					config.Save();
//				}
//				catch (Exception e)
//				{
//					e.printStackTrace();
//				}
//
//				if(suffix.equalsIgnoreCase("off"))
//					suffix = "nothing";
//
//				String message = new MessageBuilder()
//					.Colour(ChatColor.GRAY)
//					.String("%s had their suffix set to %s", args.GetArg(0), suffix)
//					.Build();
//
//				args.GetSender().sendMessage(message);
//
//				// broadcast name change event
//				Player player = GetEnv().GetServer().getPlayer(config.Uuid);
//				GetEnv().BroadcastEvent( new PlayerNameChangedEvent(player) );
//			})
//			.execute();
//
//
//		return true;
//	}
//
//}
