package com.pcb.pcbridge.library;

/**
 * Just a container to neaten up Async adapter method signatures
 * due to the unlimited 'Object... args' paramater
 */
public class AsyncAdapterParams 
{
	public String SQL;
	public Object[] Args;
	
	public AsyncAdapterParams(String sql, Object... args)
	{
		this.SQL 	= sql;
		this.Args 	= args;
	}
}
