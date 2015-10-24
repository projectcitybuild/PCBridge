package com.pcb.pcbridge.library.async;

/**
 * A non-thread-blocking implementation of Java's Future.
 * 
 * @param <T> The value-type expected to be returned from the async task
 */

public class NonBlockingFuture<T> 
{
	private IFutureCallback<T> _callback;
	private boolean _isComplete;
	private T _result;
	private Throwable _error;
	
	/**
	 * Sets the callback to be invoked upon task completion
	 * (or executes it immediately if the task is already complete)
	 * 
	 * @param callback
	 */
	public void SetCallback(IFutureCallback<T> callback)
	{
		this._callback = callback;
		if(_isComplete)
		{
			if(_error != null)
				callback.OnError(_error);
			else
			{
				callback.OnSuccess(_result);
			}
		}
	}
	
	/**
	 * Marks the task as complete and invokes the callback's Success method
	 * 
	 * @param result
	 */
	protected void CompleteTask(T result)
	{
		_result = result;
		_isComplete = true;
		
		if(_callback != null)
			_callback.OnSuccess(result);
	}
	
	/**
	 * Marks the task as failed and invokes the callback's Failure method
	 * 
	 * @param error
	 */
	protected void FailTask(Throwable error)
	{
		_error = error;
		_isComplete = true;
		
		if(_callback != null)
			_callback.OnError(error);
	}
}
