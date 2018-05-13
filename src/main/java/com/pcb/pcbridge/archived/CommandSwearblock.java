//package com.pcb.pcbridge.archived;
//
//import java.io.IOException;
//
//import org.spigot.ChatColor;
//
//import com.pcb.pcbridge.PCBridge;
//import com.pcb.pcbridge.framework.commands.AbstractCommand;
//
//public class CommandSwearblock extends AbstractCommand {
//
//	@Override
//	public String GetName()
//	{
//		return "swearblock";
//	}
//
//	@Override
//	public String GetDescription()
//	{
//		return "Toggles on/off your chat swear filter";
//	}
//
//	@Override
//	public String GetPermission()
//	{
//		return "pcbridge.chat.swearblock";
//	}
//
//	@Override
//	public String GetUsage()
//	{
//		return "/swearblock <on|off>";
//	}
//
//	@Override
//	public String[] GetAliases()
//	{
//		return new String[] { "swearfilter" };
//	}
//
//	@Override
//	public boolean OnExecute(CommandArgs args)
//	{
//		if(!args.IsPlayer())
//		{
//			args.GetSender().sendMessage(ChatColor.RED + "Only players can use this command");
//			return true;
//		}
//
//		if(args.GetArgs().length > 1)
//			return false;
//
//		if(args.GetArgs().length == 1)
//		{
//			if(!args.GetArg(0).equalsIgnoreCase("on") && !args.GetArg(0).equalsIgnoreCase("off"))
//				return false;
//		}
//
//		PCBridge.NewChain()
//			.asyncFirst( () -> {
//				// fetch player config from cache
//				PlayerConfig config = GetEnv().GetPlayerCache( args.GetPlayer().getUniqueId() );
//
//				// 'on' or 'off'. if no value given then show the current value
//				boolean setEnabled;
//				if(args.GetArgs().length == 1)
//				{
//					if(args.GetArg(0).equalsIgnoreCase("on"))
//						setEnabled = true;
//					else
//						setEnabled = false;
//				}
//				else
//				{
//					setEnabled = !config.IsSwearblockEnabled;
//				}
//
//				config.IsSwearblockEnabled = setEnabled;
//
//				try
//				{
//					config.Save();
//				}
//				catch (IOException e)
//				{
//					e.printStackTrace();
//				}
//
//				return config.IsSwearblockEnabled;
//			})
//			.syncLast( isEnabled -> {
//
//				String message;
//				if(isEnabled)
//					message = new MessageBuilder()
//						.Colour(ChatColor.GRAY)
//						.String("Swear filter is now enabled")
//						.Build();
//				else
//					message = new MessageBuilder()
//						.Colour(ChatColor.GRAY)
//						.String("Swear filter is now disabled")
//						.Build();
//
//				args.GetSender().sendMessage(message);
//			})
//			.execute();
//
//		return true;
//	}
//
//}
