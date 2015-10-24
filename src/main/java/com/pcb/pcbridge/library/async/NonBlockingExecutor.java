package com.pcb.pcbridge.library.async;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A wrapper for running a single Callable task on a new thread
 */

public class NonBlockingExecutor 
{
	private ExecutorService _executor;
	
	public NonBlockingExecutor(ExecutorService executor)
	{
		this._executor = executor;
	}
	
	public NonBlockingExecutor()
	{
		this._executor = Executors.newSingleThreadExecutor();
	}
	
	/**
	 * Runs the given task concurrently on a new thread
	 * 
	 * @param task
	 * @return NonBlockingFuture
	 */
	public <T> NonBlockingFuture<T> Submit(final Callable<T> task)
	{
		final NonBlockingFuture<T> future = new NonBlockingFuture<>();
		_executor.submit(new Callable<T>()
		{
			@Override
			public T call() throws Exception 
			{
				try
				{
					T result = task.call();
					future.CompleteTask(result);
					return result;
				}
				catch(Exception e)
				{
					future.FailTask(e);
					throw e;
				}
			}
		});
		
		return future;
	}
}
