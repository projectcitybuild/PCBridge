package com.pcb.pcbridge.library.database.querybuilder.operations;


/**
 * Produces a SELECT SQL string
 */

public class SelectSQLBuilder extends AbstractSQLBuilder
{
	public String Build(SQLBuilderParams args) 
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		
		return sql.toString();
	}

}
