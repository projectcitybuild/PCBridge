package com.pcb.pcbridge.framework.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionPool {

    private final HikariDataSource dataSource;

    // TODO: switch to a builder for config
    public DatabaseConnectionPool(String host,
                                  String port,
                                  String database,
                                  String username,
                                  String password
    ) {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        if(password != null && !password.isEmpty()) {
            config.setPassword(password);
        }

        this.dataSource = new HikariDataSource(config);
    }

    /**
     * Returns a new connection from the pool
     *
     * @return Database connection
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Closes all connections to the data source
     */
    public void close() {
        if(dataSource.isClosed()) {
            return;
        }
        dataSource.close();
    }

}
