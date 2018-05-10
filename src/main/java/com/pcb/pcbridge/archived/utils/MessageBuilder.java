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
package com.pcb.pcbridge.archived.utils;

import org.bukkit.ChatColor;

/**
 * Assists with the creation of chat and kick screen messages
 */
public class MessageBuilder {
	
	/**
	 * List of colour codes and styles for use with kick screens, etc,
	 * as normal ChatColour objects cannot be used there
	 */
	public final static class RawColours
	{
		public final static String BLACK 		= "§0";
		public final static String DARK_BLUE 	= "§1";
		public final static String DARK_GREEN 	= "§2";
		public final static String DARK_AQUA 	= "§3";
		public final static String DARK_RED 	= "§4";
		public final static String DARK_PURPLE 	= "§5";
		public final static String GOLD 		= "§6";
		public final static String GRAY 		= "§7";
		public final static String DARK_GRAY 	= "§8";
		public final static String BLUE 		= "§9";
		public final static String GREEN 		= "§a";
		public final static String AQUA 		= "§b";
		public final static String RED 			= "§c";
		public final static String LIGHT_PURPLE = "§d";
		public final static String YELLOW 		= "§e";
		public final static String WHITE 		= "§f";
	}
	
	public final static class RawFormats
	{
		public final static String OBFUSCATED 	= "§k";
		public final static String BOLD 		= "§l";
		public final static String STRIKETHROUGH = "§m";
		public final static String UNDERLINE 	= "§n";
		public final static String ITALIC 		= "§o";
		public final static String RESET 		= "§r";
	}
	
	public interface MessageAction
	{
		public MessageBuilder Do(MessageBuilder builder);
	}
	
	private StringBuilder _builder = new StringBuilder();
	
	public MessageBuilder() { }
	
	/**
	 * Appends the given String
	 * 
	 * @param string
	 * @param args	Arguments to insert into the string
	 * @return
	 */
	public MessageBuilder String(String string, Object... args)
	{		
		if(args.length > 0)
			_builder.append( String.format(string, args) );
		else
			_builder.append(string);
		
		return this;
	}
	
	/**
	 * Appends the given String and then a linebreak after it
	 * 
	 * @param string
	 * @param args	Arguments to insert into the string
	 * @return
	 */
	public MessageBuilder Stringln(String string, Object... args)
	{
		String(string, args);
		Linebreak();
		
		return this;
	}
	
	/**
	 * Appends a colour to the following text
	 * 
	 * @param colour
	 * @return
	 */
	public MessageBuilder Colour(ChatColor... colour)
	{
		for(ChatColor c : colour)
			_builder.append(c);
		
		return this;
	}
	
	public MessageBuilder Colour(String... colour)
	{
		for(String c : colour)
			_builder.append(c);
		
		return this;
	}
	
	/**
	 * Appends a format reset to the following text
	 * 
	 * @return
	 */
	public MessageBuilder Reset()
	{
		_builder.append(ChatColor.RESET);
		return this;
	}
	
	public MessageBuilder RawReset()
	{
		_builder.append(RawFormats.RESET);
		return this;
	}
	
	/**
	 * Appends a line break
	 * 
	 * @param number	Number of breaks to append
	 * @return
	 */
	public MessageBuilder Linebreak(int number)
	{
		for(int i = 0; i < number; i++)
		{
			_builder.append("\n");
		}
		return this;
	}
	
	public MessageBuilder Linebreak()
	{
		return Linebreak(1);
	}
	
	
	/**
	 * Performs an action if the condition is met, or the elseAction if the condition is not
	 * 
	 * @param condition
	 * @param action		Action to perform if condition == true
	 * @param elseAction	Action to perform if condition == false
	 * @return
	 */
	public MessageBuilder When(boolean condition, MessageAction action, MessageAction elseAction)
	{
		if(!condition)
			return elseAction == null ? this : elseAction.Do(this);
		
		return action.Do(this);
	}
	
	public MessageBuilder When(boolean condition, MessageAction action)
	{
		return When(condition, action, null);
	}
		
	
	/**
	 * Builds the final string and returns it
	 * 
	 * @return
	 */
	public String Build()
	{
		return _builder.toString();
	}
	
	/**
	 * Builds the final string and returns it,
	 * replacing any %number occurances with the given arguments
	 * 
	 * @param args	Args to insert into the final text
	 * @return
	 */
	public String Build(Object... args)
	{
		String text = _builder.toString();
		return String.format(text, args);
	}
	
}
