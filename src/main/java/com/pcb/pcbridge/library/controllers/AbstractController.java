package com.pcb.pcbridge.library.controllers;

import com.pcb.pcbridge.PCBridge;

/**
 * The base controller which can register commands & listeners
 */

public abstract class AbstractController 
{ 
	/**
	 * Actions to take place when onEnable() is invoked should be called here
	 */
	public void OnBoot(PCBridge plugin) { }
}
