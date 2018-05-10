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

import java.util.List;

/**
 * Any command logic should inherit and override this class
 */
public abstract class AbstractCommand {

	/**
	 * Returns the string used to invoke the command (eg. 'ban')
	 * 
	 * @return
	 */
	public abstract String GetName();
	
	public abstract String GetDescription();
	
	public abstract String GetPermission();
	
	public abstract String GetUsage();
	
	public String[] GetAliases()
	{
		return null;
	}
	
	/**
	 * The logic to run when invoked
	 * 
	 * @param args
	 * @return
	 */
	public abstract boolean OnExecute(CommandArgs args);
	
	/**
	 * The logic to run when the [Tab] key is pressed
	 * 
	 * @param args
	 * @return
	 */
	public List<String> OnTabComplete(CommandArgs args)
	{
		return null;
	}
	
	
	private Environment _environment;
	
	protected Environment GetEnv()
	{
		return _environment;
	}
	
	public void SetEnv(Environment environment)
	{
		this._environment = environment;
	}
	
	
	protected CommandPromptManager GetPromptManager()
	{
		return _commandManager.GetPromptManager();
	}
	
	
	private CommandManager _commandManager;
	protected CommandManager GetCommandManager()
	{
		return _commandManager;
	}
	
	public void SetCommandManager(CommandManager cm)
	{
		this._commandManager = cm;
	}
	
}
