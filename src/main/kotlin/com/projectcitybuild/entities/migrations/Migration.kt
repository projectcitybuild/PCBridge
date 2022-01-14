package com.projectcitybuild.entities.migrations

import com.projectcitybuild.modules.logger.LoggerProvider
import com.projectcitybuild.old_modules.playerconfig.PlayerConfigFileStorage
import com.projectcitybuild.old_modules.storage.WarpFileStorage
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.plugin.Plugin
import java.sql.Date

object Migration {

    fun executeIfNecessary(
        dataSource: HikariDataSource,
        logger: LoggerProvider,
        plugin: Plugin, // temporary
        currentVersion: Int
    ) {
        var version = currentVersion
        val totalMigrations = migrations.size

        if (version >= totalMigrations) return

        while (version < totalMigrations) {
            val migration = migrations[version]
            logger.info("Running migration ${version + 1}: ${migration.first}")
            migration.second(dataSource, plugin)
            version++
        }

        if (currentVersion != totalMigrations) {
            updateVersion(dataSource, version)
        }
    }

    private val migrations: Array<Pair<String, (HikariDataSource, Plugin) -> Unit>> = arrayOf(
        Pair("First-time run") { dataSource, _ ->
            val connection = dataSource.connection

            connection
                .prepareStatement("CREATE TABLE IF NOT EXISTS meta(version INT(64));")
                .executeUpdate()

            connection
                .prepareStatement("INSERT INTO meta VALUES(1);")
                .executeUpdate()
        },

        Pair("Player configs") { dataSource, plugin ->
            val connection = dataSource.connection

            connection
                .prepareStatement(
                    """
                    |CREATE TABLE players (
                    |   `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                    |   `uuid` VARCHAR(50) NOT NULL,
                    |   `is_muted` VARCHAR(50) NOT NULL DEFAULT '0',
                    |   `is_allowing_tp` TINYINT(1) NOT NULL DEFAULT '1',
                    |   `first_seen` DATETIME NOT NULL,
                    |   PRIMARY KEY `id` (`id`),
                    |   INDEX  (`uuid`)
                    |);
                    """
                    .trimMargin("|")
                    .replace("\n", "")
                )
                .executeUpdate()

            // TODO: remove all this later
            val configStorage = PlayerConfigFileStorage(plugin.dataFolder.resolve("players"))
            runBlocking {
                configStorage.keys().forEach { fileName ->
                    plugin.logger.info("Migrating player/$fileName.json to database")
                    val config = configStorage.load(fileName)!!

                    val statement = connection.prepareStatement(
                        "INSERT INTO `players` VALUES(NULL, ?, ?, ?, ?);"
                    ).apply {
                        setString(1, config.uuid.unwrapped.toString())
                        setBoolean(2, config.isMuted)
                        setBoolean(3, config.isAllowingTPs)
                        setDate(4, Date(System.currentTimeMillis()))
                    }
                    statement.executeUpdate()
                }
            }

            connection
                .prepareStatement(
                    """
                    |CREATE TABLE warps (
                    |   `name` VARCHAR(50) NOT NULL,
	                |   `server_name` VARCHAR(50) NOT NULL,
	                |   `world_name` VARCHAR(50) NOT NULL,
	                |   `x` DOUBLE NOT NULL DEFAULT 0,
	                |   `y` DOUBLE NOT NULL DEFAULT 0,
	                |   `z` DOUBLE NOT NULL DEFAULT 0,
	                |   `pitch` FLOAT NOT NULL DEFAULT 0,
	                |   `yaw` FLOAT NOT NULL DEFAULT 0,
	                |   `created_at` DATETIME NOT NULL,
	                |   PRIMARY KEY (`name`) USING BTREE
                    |);
                    """
                    .trimMargin("|")
                    .replace("\n", "")
                )
                .executeUpdate()

            val warpStorage = WarpFileStorage(plugin.dataFolder.resolve("warps"))
            runBlocking {
                warpStorage.keys().forEach { fileName ->
                    plugin.logger.info("Migrating warps/$fileName.json to database")
                    val warp = warpStorage.load(fileName)!!

                    val statement = connection.prepareStatement(
                        "INSERT INTO `warps` VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);"
                    ).apply {
                        setString(1, fileName)
                        setString(2, warp.serverName)
                        setString(3, warp.worldName)
                        setDouble(4, warp.x)
                        setDouble(5, warp.y)
                        setDouble(6, warp.z)
                        setFloat(7, warp.pitch)
                        setFloat(8, warp.yaw)
                        setDate(9, Date(warp.createdAt.unwrapped.time))
                    }
                    statement.executeUpdate()
                }
            }

            connection
                .prepareStatement(
                    """
                    |CREATE TABLE teleport_history (
                    |   `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	                |   `player_id` BIGINT UNSIGNED NOT NULL,
	                |   `tp_reason` VARCHAR(50) NOT NULL,
	                |   `server_name` VARCHAR(50) NOT NULL,
	                |   `world_name` VARCHAR(50) NOT NULL,
	                |   `x` DOUBLE NOT NULL DEFAULT 0,
	                |   `y` DOUBLE NOT NULL DEFAULT 0,
	                |   `z` DOUBLE NOT NULL DEFAULT 0,
	                |   `pitch` FLOAT NOT NULL DEFAULT 0,
	                |   `yaw` FLOAT NOT NULL DEFAULT 0,
	                |   `created_at` DATETIME NOT NULL,
	                |   PRIMARY KEY (`id`),
	                |   CONSTRAINT `teleport_history_player_id` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
                    |);
                    """
                    .trimMargin("|")
                    .replace("\n", "")
                )
                .executeUpdate()

            connection
                .prepareStatement(
                    """
                    |CREATE TABLE chat_ignores (
                    |   `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                    |   `player_id` BIGINT UNSIGNED NOT NULL,
                    |   `ignored_uuid` VARCHAR(50) NOT NULL,
                    |   PRIMARY KEY (`id`),
                    |   CONSTRAINT `chat_ignores_player_id` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
                    |);
                    """
                    .trimMargin("|")
                    .replace("\n", "")
                )
                .executeUpdate()
        },
    )

    private fun updateVersion(dataSource: HikariDataSource, newVersion: Int) {
        val statement = dataSource.connection.prepareStatement(
            "UPDATE meta SET `version`=?"
        ).apply {
            setInt(1, newVersion)
        }
        statement.executeUpdate()
    }
}