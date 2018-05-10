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

import com.pcb.pcbridge.archived.utils.Actions.Action0;

/**
 * Contains a simple map for transmitting data between multiple commands.
 * See CommandPromptManager for details.
 */
public class CommandPrompt {
	
	private final Map<String, Object> _data = new HashMap<>();
	private final Action0 _callback;
	private boolean _isExpired = false;
	
	public CommandPrompt(Action0 callback)
	{
		this._callback = callback;
	}
	
	/**
	 * Runs the stored action
	 */
	public void Run()
	{
		if(_callback != null)
			_callback.Call();
	}
	
	/**
	 * Returns whether the CommandPrompt has been set as expired
	 * (ie. the prompt timer has ticked down, and the prompt is now unusable)
	 * 
	 * @return
	 */
	public boolean IsExpired()
	{
		return _isExpired;
	}
	
	/**
	 * Expires the CommandPrompt
	 */
	public void Expire()
	{
		this._isExpired = true;
	}
	
	/**
	 * Gets data from this prompt
	 * 
	 * @param key
	 * @return
	 */
	public Object GetData(String key)
	{
		return _data.get(key);
	}
	
	/**
	 * Puts data in this prompt
	 * 
	 * @param key
	 * @param value
	 * @return CommandPrompt
	 */
	public CommandPrompt SetData(String key, Object value)
	{
		_data.put(key, value);
		return this;
	}
}
