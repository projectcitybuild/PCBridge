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
package com.pcb.pcbridge.utils.commands;

import java.io.InvalidClassException;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * A stand-in command used for plugin.yml command registration which
 * simply routes any command execution back to a CommandExecutor
 *
 */
public class ProxyCommand extends Command {
	
	private CommandManager _executor = null;
	
    protected ProxyCommand(String command)
    {
    	super(command);
    }
    
    public void SetExecutor(CommandManager executor) throws InvalidClassException
    {
    	if(executor instanceof CommandExecutor == false)
    		throw new InvalidClassException("Command handler must extend 'CommandExecutor'");
    	
    	if(executor instanceof TabCompleter == false)
    		throw new InvalidClassException("Command handler must extend 'TabCompleter'");
    	
    	this._executor = executor; 
    }
    
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) 
    {
        if (_executor != null)
        	return _executor.onCommand(sender, this, commandLabel, args);
        
        System.out.println("no executor...");
        return false;
    }
    
    @Override  public List<String> tabComplete(CommandSender sender, String alais, String[] args) 
    {
        if (_executor != null) 
        	return _executor.onTabComplete(sender, this, alais, args); 
        
        return null;
    }

}
