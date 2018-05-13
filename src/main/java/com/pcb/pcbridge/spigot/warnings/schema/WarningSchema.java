package com.pcb.pcbridge.spigot.warnings.schema;

import com.pcb.pcbridge.archived.PlayerContract;
import com.pcb.pcbridge.archived.AbstractSchemaTable;

public final class WarningSchema {
	
	// prevent this class from being instantiated
	protected WarningSchema() { }
	
	/**
	 * Table where every warning is recorded
	 */
	public static final class TableWarnings extends AbstractSchemaTable
	{
		public static final String TABLE_NAME = "warnings";
		
		public static final String COL_PLAYER_ID 	= "player_id";
		public static final String COL_TIMESTAMP 	= "timestamp";
		public static final String COL_STAFF_ID 	= "staff_id";
		public static final String COL_REASON 		= "reason";
		
		public static final String SQL_CREATE = 
				"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( "
							+ _ID + " INT UNSIGNED NOT NULL AUTO_INCREMENT ,"
							+ COL_PLAYER_ID + " INT UNSIGNED NOT NULL ,"
							+ COL_STAFF_ID + " INT UNSIGNED NOT NULL ,"
							+ COL_REASON + " TEXT NULL ,"
							+ COL_TIMESTAMP + " INT NOT NULL ,"
							+ "PRIMARY KEY (" + _ID + ") ,"
							+ "FOREIGN KEY (" + COL_PLAYER_ID + ") REFERENCES " + PlayerContract.TablePlayers.TABLE_NAME + "(" + PlayerContract.TablePlayers._ID + ") ,"
							+ "FOREIGN KEY (" + COL_STAFF_ID + ") REFERENCES " + PlayerContract.TablePlayers.TABLE_NAME + "(" + PlayerContract.TablePlayers._ID + ")"
						+ ") ENGINE = InnoDB;";
		
		public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}
	
}
