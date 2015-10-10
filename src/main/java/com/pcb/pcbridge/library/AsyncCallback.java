package com.pcb.pcbridge.library;

public interface AsyncCallback<T>
{
	public void OnSuccess(T results);
	public void OnError(Exception e);
}
