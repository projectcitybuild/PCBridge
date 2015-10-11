package com.pcb.pcbridge.library.database.querybuilder.operations;

import java.util.ListIterator;


/**
 * Produces a SELECT SQL string
 */

public class SelectSQLBuilder extends AbstractSQLBuilder
{
	public String Build(SQLBuilderParams args) 
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		
		if(args.GetColumns().isEmpty())
		{
			sql.append("*");
		}
		else
		{
			ListIterator<String> i = args.GetColumns().listIterator();
			while(i.hasNext())
			{
				sql.append(i.next());
				
				if(i.hasNext())
					sql.append(",");
			}
		}
		
		sql.append(" FROM ")
			.append(args.GetTable());
		
		return sql.toString();
	}

}
