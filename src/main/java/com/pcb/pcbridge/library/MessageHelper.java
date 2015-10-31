package com.pcb.pcbridge.library;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Collection of message related helpers
 */

public final class MessageHelper 
{
	/**
	 * Sends a message to a CommandSender based on the MessageType's colour and prefix
	 * 
	 * @param type
	 * @param sender
	 * @param message
	 */
	public static void Send(MessageType type, CommandSender sender, String message)
	{
		StringBuilder msg = new StringBuilder();
		
		switch(type)
		{
			case ERROR:
				msg.append(ChatColor.RED).append("ERROR: ");
				break;
			case FATAL:
				msg.append(ChatColor.RED).append(ChatColor.ITALIC).append("FATAL: ");
				break;
			case INFO:
				msg.append(ChatColor.GRAY);
				break;
			case CONFIRM:
				msg.append(ChatColor.GREEN).append("CONFIRM: ");
				break;
			case WARNING:
				msg.append(ChatColor.YELLOW).append("WARNING: ");
				break;
			default:
				break;
		}
		
		sender.sendMessage( msg.append(message).toString() );
	}
	
	public static void Send(CommandSender sender, String message)
	{
		Send(MessageType.NORMAL, sender, message);
	}

}
