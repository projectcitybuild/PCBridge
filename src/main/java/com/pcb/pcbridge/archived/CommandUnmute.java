//package com.pcb.pcbridge.archived;
//
//import com.pcb.pcbridge.framework.commands.AbstractCommand;
//
//public class CommandUnmute extends AbstractCommand {
//
//	@Override
//	public String GetName()
//	{
//		return "unmute";
//	}
//
//	@Override
//	public String GetDescription()
//	{
//		return "Allows a player to send chat messages again";
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
//		return "/unmute <name>";
//	}
//
//	@Override
//	public boolean OnExecute(CommandArgs args)
//	{
//		// route the command to CommandMute since it's the same logic
//		args.AddData("UNMUTE", true);
//		return GetCommandManager().InvokeCommand("mute", args);
//	}
//
//}
