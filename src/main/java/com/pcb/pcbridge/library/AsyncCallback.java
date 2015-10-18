package com.pcb.pcbridge.library;

/**
 * Interface for working with the result of an asynchronous task
 * 
 * @param <T> The value type to be passed to OnSuccess()
 */

public interface AsyncCallback<T>
{
	public void OnSuccess(T results);
	public void OnError(Exception e);
}
