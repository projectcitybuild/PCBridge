package com.projectcitybuild.entities.migrations

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.modules.database.DatabaseMigration
import com.projectcitybuild.old_modules.playerconfig.PlayerConfigFileStorage
import com.projectcitybuild.old_modules.storage.WarpFileStorage
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.plugin.Plugin
import java.sql.Date

class `20220115_player_configs_warps`: DatabaseMigration {
    override val description = "Add player configs and warps"

    override fun execute(database: HikariPooledDatabase, plugin: Plugin) {
        database.executeUpdate(
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

        // TODO: remove all this later
        val configStorage = PlayerConfigFileStorage(plugin.dataFolder.resolve("players"))
        runBlocking {
            configStorage.keys().forEach { fileName ->
                plugin.logger.info("Migrating player/$fileName.json to database")
                val config = configStorage.load(fileName)!!

                database.executeInsert(
                    "INSERT INTO `players` VALUES(NULL, ?, ?, ?, ?);",
                    config.uuid.unwrapped.toString(),
                    config.isMuted,
                    config.isAllowingTPs,
                    Date(System.currentTimeMillis()),
                )
            }
        }

        database.executeUpdate(
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

        val warpStorage = WarpFileStorage(plugin.dataFolder.resolve("warps"))
        runBlocking {
            warpStorage.keys().forEach { fileName ->
                plugin.logger.info("Migrating warps/$fileName.json to database")
                val warp = warpStorage.load(fileName)!!

                database.executeInsert(
                    "INSERT INTO `warps` VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);",
                    fileName,
                    warp.serverName,
                    warp.worldName,
                    warp.x,
                    warp.y,
                    warp.z,
                    warp.pitch,
                    warp.yaw,
                    Date(warp.createdAt.unwrapped.time),
                )
            }
        }

        database.executeUpdate(
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

        database.executeUpdate(
            """
                    |CREATE TABLE chat_ignores (
                    |   `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                    |   `player_id` BIGINT UNSIGNED NOT NULL,
                    |   `ignored_player_id` BIGINT UNSIGNED NOT NULL,
	                |   `created_at` DATETIME NOT NULL,
                    |   PRIMARY KEY (`id`),
                    |   CONSTRAINT `chat_ignores_player_id` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON UPDATE NO ACTION ON DELETE NO ACTION,
                    |   CONSTRAINT `chat_ignores_ignored_player_id` FOREIGN KEY (`ignored_player_id`) REFERENCES `players` (`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
                    |);
                    """
                .trimMargin("|")
                .replace("\n", "")
        )
    }
}