package com.pcb.pcbridge.library;

public enum MessageType 
{
	NORMAL,
	
	// extra information to supply to the user
	INFO,
	
	// something succeeded or executed entirely
	SUCCESS,
	
	// something bad happened, execution cancelled (eg. bad command input formatting, invalid arguement, etc)
	ERROR,
	
	// something bad happened, but continue anyway
	WARNING,	
	
	// an exception was raised, cannot continue at all
	FATAL,
	
	// prompt the user for further input
	CONFIRM
}
