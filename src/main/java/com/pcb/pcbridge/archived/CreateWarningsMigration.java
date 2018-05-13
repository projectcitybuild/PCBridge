//package com.pcb.pcbridge.archived;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.logging.Logger;
//
//import com.pcb.pcbridge.spigot.warnings.schema.WarningContract;
//import com.pcb.pcbridge.archived.IMigrate;
//
///**
// * Migration to execute when the plugin is run for the first time
// */
//
//public class CreateWarningsMigration implements IMigrate {
//
//	@Override
//	public boolean IsTransaction()
//	{
//		return true;
//	}
//
//	@Override
//	public void OnMigrate(Connection connection, Logger logger) throws SQLException
//	{
//		Statement stmt;
//
//		stmt = connection.createStatement();
//		stmt.executeUpdate(WarningContract.TableWarnings.SQL_CREATE);
//		stmt.close();
//	}
//
//}
