package com.pcb.pcbridge.library;

public interface AsyncCallback 
{
	public void OnSuccess(Object results);
	public void OnError(Exception e);
}
