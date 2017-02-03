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

public final class BanListContract {
	
	public static final String DATABASE = "pcbridge";
	
	// prevent this class from being instantiated
	protected BanListContract() { }
	
	/**
	 * Table where every ban is recorded
	 */
	public static final class TableBans extends AbstractSchemaTable
	{
		public static final String TABLE_NAME = "ban_records_bans";
		
		public static final String COL_PLAYER_ID 	= "player_id";
		public static final String COL_TIMESTAMP 	= "timestamp";
		public static final String COL_STAFF_ID 	= "staff_id";
		public static final String COL_REASON 		= "reason";
		public static final String COL_UNBAN_ON		= "unban_on";
		public static final String COL_IS_BANNED	= "is_banned";
		public static final String COL_SERVER		= "server";
		
		public static final String SQL_CREATE = 
				"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( "
							+ _ID + " INT UNSIGNED NOT NULL AUTO_INCREMENT ,"
							+ COL_PLAYER_ID + " INT UNSIGNED NOT NULL ,"
							+ COL_TIMESTAMP + " INT NOT NULL ,"
							+ COL_STAFF_ID + " INT UNSIGNED NOT NULL ,"
							+ COL_REASON + " TEXT NULL ,"
							+ COL_UNBAN_ON + " INT NULL ,"
							+ COL_IS_BANNED + " BOOLEAN NOT NULL ,"
							+ COL_SERVER + " VARCHAR(30) NOT NULL DEFAULT 'minecraft' ,"
							+ "PRIMARY KEY (" + _ID + ") ,"
							+ "FOREIGN KEY (" + COL_PLAYER_ID + ") REFERENCES " + PlayerContract.TablePlayers.TABLE_NAME + "(" + PlayerContract.TablePlayers._ID + ") ,"
							+ "FOREIGN KEY (" + COL_STAFF_ID + ") REFERENCES " + PlayerContract.TablePlayers.TABLE_NAME + "(" + PlayerContract.TablePlayers._ID + ")"
						+ ") ENGINE = InnoDB;";
		
		public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}
	
	/**
	 * Table for any manual unban records
	 */
	public static final class TableUnbans extends AbstractSchemaTable
	{
		public static final String TABLE_NAME = "ban_records_unbans";

		public static final String COL_BAN_ID	 	= "ban_id";
		public static final String COL_STAFF_ID 	= "staff_id";
		public static final String COL_TIMESTAMP 	= "timestamp";
		
		public static final String SQL_CREATE = 
				"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( "
							+ _ID + " INT UNSIGNED NOT NULL AUTO_INCREMENT ,"
							+ COL_BAN_ID + " INT UNSIGNED NOT NULL ,"
							+ COL_STAFF_ID + " INT UNSIGNED NOT NULL ,"
							+ COL_TIMESTAMP + " INT NOT NULL ,"
							+ "PRIMARY KEY (" + _ID + ") ,"
							+ "FOREIGN KEY (" + COL_BAN_ID + ") REFERENCES " + TableBans.TABLE_NAME + "(" + TableBans._ID + ") ,"
							+ "FOREIGN KEY (" + COL_STAFF_ID + ") REFERENCES " + PlayerContract.TablePlayers.TABLE_NAME + "(" + PlayerContract.TablePlayers._ID + ")"
						+ ") ENGINE = InnoDB;";
		
		public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}
	
}
