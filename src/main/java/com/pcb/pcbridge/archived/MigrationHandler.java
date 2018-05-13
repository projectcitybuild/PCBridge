//package com.pcb.pcbridge.archived;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.logging.Logger;
//
//import com.pcb.pcbridge.PCBridge;
//
///**
// * Performs database schema tasks.
// */
//
//public class MigrationHandler {
//
//	private final Logger _logger;
//	public MigrationHandler(Logger logger)
//	{
//		this._logger = logger;
//	}
//
//	/**
//	 * Runs the given Migration on the supplied connection.
//	 * Note: Does NOT close the supplied connection after completion
//	 *
//	 * @param <T>			Class which implements IMigrate
//	 * @param conn			Database connection
//	 * @param migration		Migration to run
//	 */
//	public <T extends IMigrate> void Migrate(Connection conn, Class<T> migration)
//			throws SQLException
//	{
//		try
//		{
//			IMigrate task = migration.newInstance();
//
//			conn.setAutoCommit(!task.IsTransaction());
//
//			_logger.info("Running migration: " + migration.getName());
//			task.OnMigrate(conn, _logger);
//
//			if(task.IsTransaction())
//			{
//				conn.commit();
//				conn.setAutoCommit(true);
//			}
//		}
//		catch (InstantiationException | IllegalAccessException e)
//		{
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * Runs the given Migration on a new connection and then closes it
//	 * after completion
//	 *
//	 * @param migration
//	 * @param database	Database to run the migration on
//	 */
//	public <T extends IMigrate> void Migrate(Class<T> migration, String database)
//	{
//		try(Connection conn = PCBridge.GetConnectionPool().GetConnection(database))
//		{
//			Migrate(conn, migration);
//		}
//		catch (SQLException e)
//		{
//			e.printStackTrace();
//		}
//	}
//
//}
