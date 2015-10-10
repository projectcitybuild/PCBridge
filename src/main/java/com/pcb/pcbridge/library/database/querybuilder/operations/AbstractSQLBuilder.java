package com.pcb.pcbridge.library.database.querybuilder.operations;

import java.util.LinkedList;

public abstract class AbstractSQLBuilder
{
	public abstract String Build(SQLBuilderParams args);
	
	protected LinkedList<Object> _storedParameters = new LinkedList<Object>();
	public LinkedList<Object> GetStoredParameters()
	{
		return _storedParameters;
	}
}
