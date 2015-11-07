package com.pcb.pcbridge.library.controllers;

/**
 * Interface for controllers that have commands to be registered
 */

public interface ICommandController
{	
	public CommandRoute[] GetCommands();
}
