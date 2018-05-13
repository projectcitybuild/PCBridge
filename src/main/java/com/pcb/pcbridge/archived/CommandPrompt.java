//package com.pcb.pcbridge.archived;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import com.pcb.pcbridge.archived.utils.Actions.Action0;
//
///**
// * Contains a simple map for transmitting data between multiple commands.
// * See CommandPromptManager for details.
// */
//public class CommandPrompt {
//
//	private final Map<String, Object> _data = new HashMap<>();
//	private final Action0 _callback;
//	private boolean _isExpired = false;
//
//	public CommandPrompt(Action0 callback)
//	{
//		this._callback = callback;
//	}
//
//	/**
//	 * Runs the stored action
//	 */
//	public void Run()
//	{
//		if(_callback != null)
//			_callback.Call();
//	}
//
//	/**
//	 * Returns whether the CommandPrompt has been set as expired
//	 * (ie. the prompt timer has ticked down, and the prompt is now unusable)
//	 *
//	 * @return
//	 */
//	public boolean IsExpired()
//	{
//		return _isExpired;
//	}
//
//	/**
//	 * Expires the CommandPrompt
//	 */
//	public void Expire()
//	{
//		this._isExpired = true;
//	}
//
//	/**
//	 * Gets data from this prompt
//	 *
//	 * @param key
//	 * @return
//	 */
//	public Object GetData(String key)
//	{
//		return _data.get(key);
//	}
//
//	/**
//	 * Puts data in this prompt
//	 *
//	 * @param key
//	 * @param value
//	 * @return CommandPrompt
//	 */
//	public CommandPrompt SetData(String key, Object value)
//	{
//		_data.put(key, value);
//		return this;
//	}
//}
