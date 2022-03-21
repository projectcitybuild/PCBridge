package com.projectcitybuild.core.infrastructure.database.migrations

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.core.infrastructure.database.DatabaseMigration

class `20220321_add_homes`: DatabaseMigration {
    override val description = "Adds a table to store player homes"

    override fun execute(database: HikariPooledDatabase) {

        database.executeUpdate(
            """
                    |CREATE TABLE homes (
                    |   `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                    |   `name` VARCHAR(50) NOT NULL,
                    |   `player_uuid` VARCHAR(50) NOT NULL,
	                |   `server_name` VARCHAR(50) NOT NULL,
	                |   `world_name` VARCHAR(50) NOT NULL,
	                |   `x` DOUBLE NOT NULL DEFAULT 0,
	                |   `y` DOUBLE NOT NULL DEFAULT 0,
	                |   `z` DOUBLE NOT NULL DEFAULT 0,
	                |   `pitch` FLOAT NOT NULL DEFAULT 0,
	                |   `yaw` FLOAT NOT NULL DEFAULT 0,
	                |   `created_at` DATETIME NOT NULL,
	                |   PRIMARY KEY (`id`),
                    |   INDEX (`name`, `player_uuid`)
                    |);
                    """
                .trimMargin("|")
                .replace("\n", "")
        )
    }
}