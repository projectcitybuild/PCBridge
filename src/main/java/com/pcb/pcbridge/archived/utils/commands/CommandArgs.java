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
package com.pcb.pcbridge.archived.utils.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A data object that wraps the parameters provided to a command
 * to provide a cleaner method signature and additional functionality
 */
public class CommandArgs {

	private final CommandSender _sender;
	private final Command _command;
	private final String _label;
	private final String[] _args;
	private Map<String, Object> _extraData;
	
	public CommandArgs(CommandSender sender, Command command, String label, String[] args)
	{
		this._sender = sender;
		this._command = command;
		this._label = label;
		this._args = args;
	}
	
	public CommandSender GetSender() 
	{
		return _sender;
	}
	
	public Command GetCommand()
	{
		return _command;
	}
	
	public String GetLabel()
	{
		return _label;
	}
	
	public String[] GetArgs()
	{
		return _args;
	}
	
	public String GetArg(int index)
	{
		return _args[index];
	}
	
	/**
	 * Returns false if the sender is Console
	 * 
	 * @return
	 */
	public boolean IsPlayer()
	{
		return _sender instanceof Player;
	}
	
	/**
	 * Gets a Player casted sender
	 * 
	 * @return
	 */
	public Player GetPlayer()
	{
		return (Player)_sender;
	}
	
	/**
	 * Stores the given KeyValue pair.
	 * 
	 * This is primarily for when we want to invoke a command from another command
	 * and need to transmit extra data with it.
	 * 
	 * @param key
	 * @param value
	 */
	public void AddData(String key, Object value)
	{
		if(_extraData == null)
			_extraData = new HashMap<>();
		
		_extraData.put(key,  value);
	}
	
	/**
	 * Gets the Value for the given Key
	 * 
	 * This is primarily for when we want to invoke a command from another command
	 * and need to transmit extra data with it.
	 * 
	 * @param key
	 * @return
	 */
	public Object GetData(String key)
	{
		if(_extraData == null)
			return null;
		
		return _extraData.get(key);
	}
	
	/**
	 * Returns whether any additional data is stored
	 * 
	 * @return
	 */
	public boolean HasData()
	{
		return _extraData != null && _extraData.size() > 0;
	}
	
}
