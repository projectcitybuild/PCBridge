/*
 * The MIT License
 *
 * Copyright 2016 Andy Saw
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.pcb.pcbridge.schema;

import com.pcb.pcbridge.utils.database.AbstractSchemaTable;

public final class WarningContract {
	
	// prevent this class from being instantiated
	protected WarningContract() { }
	
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
