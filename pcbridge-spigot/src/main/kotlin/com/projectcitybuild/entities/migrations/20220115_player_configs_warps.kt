package com.projectcitybuild.entities.migrations

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.modules.database.DatabaseMigration

class `20220115_player_configs_warps` : DatabaseMigration {
    override val description = "Add player configs and warps"

    override fun execute(database: HikariPooledDatabase) {
        database.executeUpdate(
            """
                    |CREATE TABLE players (
                    |   `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                    |   `uuid` VARCHAR(50) NOT NULL,
                    |   `is_muted` TINYINT(1) NOT NULL DEFAULT '0',
                    |   `is_allowing_tp` TINYINT(1) NOT NULL DEFAULT '1',
                    |   `first_seen` DATETIME NOT NULL,
                    |   PRIMARY KEY `id` (`id`),
                    |   INDEX  (`uuid`)
                    |);
                    """
                .trimMargin("|")
                .replace("\n", "")
        )

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

        database.executeUpdate(
            """
                    |CREATE TABLE queued_warps (
	                |   `player_uuid` VARCHAR(50) NOT NULL,
                    |   `warp_name` VARCHAR(50) NOT NULL,   
	                |   `server_name` VARCHAR(50) NOT NULL,
	                |   `world_name` VARCHAR(50) NOT NULL,
	                |   `x` DOUBLE NOT NULL DEFAULT 0,
	                |   `y` DOUBLE NOT NULL DEFAULT 0,
	                |   `z` DOUBLE NOT NULL DEFAULT 0,
	                |   `pitch` FLOAT NOT NULL DEFAULT 0,
	                |   `yaw` FLOAT NOT NULL DEFAULT 0,
	                |   `created_at` DATETIME NOT NULL,
	                |   PRIMARY KEY (`player_uuid`)
                    |);
                    """
                .trimMargin("|")
                .replace("\n", "")
        )

        database.executeUpdate(
            """
                    |CREATE TABLE queued_teleports (
	                |   `player_uuid` VARCHAR(50) NOT NULL,
	                |   `target_player_uuid` VARCHAR(50) NOT NULL,
	                |   `target_server_name` VARCHAR(50) NOT NULL,
                    |   `teleport_type` VARCHAR(10) NOT NULL,   
	                |   `created_at` DATETIME NOT NULL,
	                |   PRIMARY KEY (`player_uuid`)
                    |);
                    """
                .trimMargin("|")
                .replace("\n", "")
        )
    }
}
