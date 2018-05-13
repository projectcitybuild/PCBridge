package com.pcb.pcbridge.archived;

import co.aikar.taskchain.TaskChain;
import org.bukkit.command.CommandSender;

import co.aikar.taskchain.TaskChainAbortAction;

public class AbortTask {
		
	public static TaskChainAbortAction<CommandSender, String, Object[]> Send(CommandSender sender, String message, Object... args)
	{
		return new TaskChainAbortAction<CommandSender, String, Object[]>() {
			@Override
			public void onAbort(TaskChain<?> chain, CommandSender __, String ___, Object[] ____)
			{
				String msg = String.format(message, args);				
				sender.sendMessage(msg);
			}
		};
	}		
	
}
