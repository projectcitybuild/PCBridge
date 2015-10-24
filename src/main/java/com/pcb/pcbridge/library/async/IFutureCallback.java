package com.pcb.pcbridge.library.async;

/**
 * Interface for working with the result/exception of an asynchronous task
 * 
 * @param <T> The value type to be passed to OnSuccess()
 */

public interface IFutureCallback<T>
{
	public void OnSuccess(T results);
	public void OnError(Throwable e);
}
