package com.pcb.pcbridge.library.database.querybuilder.operations;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Produces an INSERT SQL string
 */

public class InsertSQLBuilder extends AbstractSQLBuilder
{
	public String Build(SQLBuilderParams args) 
	{
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ")
			.append(args.GetTable())
			.append("(");
	
		Iterator<Entry<String, Object>> i = args.GetParameters().entrySet().iterator();
		while(i.hasNext())
		{
			sql.append(i.next().getKey());
			
			if(i.hasNext())
				sql.append(",");
		}
		
		sql.append(") VALUES (");
		
		i = args.GetParameters().entrySet().iterator();
		while(i.hasNext())
		{
			Object value = i.next().getValue();
			
			_storedParameters.add(value);
			value = (String)"?";
			
			
			sql.append(value);
			
			if(i.hasNext())
				sql.append(",");
		}
		
		sql.append(")");
		
		return sql.toString();
	}

}
