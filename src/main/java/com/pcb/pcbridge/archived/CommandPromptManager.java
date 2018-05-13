//package com.pcb.pcbridge.archived;
//
//import net.md_5.bungee.api.ChatColor;
//
//import org.spigot.command.CommandSender;
//
//import com.pcb.pcbridge.archived.utils.Actions.Action0;
//import com.pcb.pcbridge.archived.utils.cache.TimedCache;
//
///**
// * Handles the creation of 'Command Prompts' - basically a timer and a KeySet.
// *
// * A command can register a 'Prompt' that will expire after X seconds. Another
// * command can then pull data from this Prompt when it's invoked.
// */
//
//public class CommandPromptManager {
//
//	private final int DEFAULT_EXPIRY_TIME = 15;	// secs until a prompt becomes unusable
//	private final int DEFAULT_DELETE_TIME = 45; // secs until a prompt no longer informs the sender it is expired
//
//	private final TimedCache<Integer, CommandPrompt> _cache;
//	private final TimedCache<Integer, CommandPrompt> _expiredCache;
//
//	public CommandPromptManager(Environment env)
//	{
//		this._cache = new TimedCache<>(env.GetPlugin());
//		this._expiredCache = new TimedCache<>(env.GetPlugin());
//	}
//
//	/**
//	 * Creates a prompt for the given sender and starts its timer
//	 *
//	 * @param id				String id for the timer
//	 * @param sender			Recepient of the prompt
//	 * @param expiryInSeconds	How long the prompt is available for use
//	 * @return
//	 */
//	public CommandPrompt Register(String id, CommandSender sender, int expiryInSeconds, Action0 callback)
//	{
//		int key = GetKey(id, sender);
//
//		CommandPrompt prompt = new CommandPrompt(callback);
//		_cache.Remember(key, prompt, expiryInSeconds, () -> {
//			prompt.Expire();
//			_expiredCache.Remember(key, prompt, DEFAULT_DELETE_TIME);
//		});
//
//		return prompt;
//	}
//
//	public CommandPrompt Register(String id, CommandSender sender, int expiryInSeconds)
//	{
//		return Register(id, sender, expiryInSeconds, null);
//	}
//
//	public CommandPrompt Register(String id, CommandSender sender)
//	{
//		return Register(id, sender, DEFAULT_EXPIRY_TIME, null);
//	}
//
//	public CommandPrompt Register(String id, CommandSender sender, Action0 callback)
//	{
//		return Register(id, sender, DEFAULT_EXPIRY_TIME, callback);
//	}
//
//	/**
//	 * Gets the CommandPrompt for the given sender and id.
//	 * Returns null if not found. If an expired prompt is found,
//	 * informs the users it has expired
//	 *
//	 * @param id
//	 * @param sender
//	 * @return
//	 */
//	public CommandPrompt GetPrompt(String id, CommandSender sender)
//	{
//		int key = GetKey(id, sender);
//
//		CommandPrompt prompt = _cache.Get(key);
//		if(prompt != null)
//			return prompt;
//
//		// if no prompt found, check the expired prompt map and let the user know
//		if(_expiredCache.Get(key) != null)
//			sender.sendMessage(ChatColor.GRAY + "This command has already expired");
//
//		return null;
//	}
//
//	/**
//	 * Destroys the given prompt and stops its timer without
//	 * expiring the prompt
//	 *
//	 * @param id
//	 * @param sender
//	 * @return
//	 */
//	public void DestroyPrompt(String id, CommandSender sender)
//	{
//		int key = GetKey(id, sender);
//		_cache.Forget(key);
//	}
//
//	/**
//	 * Returns the hash of the id and the sender
//	 *
//	 * @param id
//	 * @return
//	 */
//	private int GetKey(String id, CommandSender sender)
//	{
//		String key = id + sender.getName();
//		return key.hashCode();
//	}
//
//}
