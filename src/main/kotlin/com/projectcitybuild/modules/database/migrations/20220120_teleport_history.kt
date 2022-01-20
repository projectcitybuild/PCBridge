package com.projectcitybuild.modules.database.migrations

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.modules.database.DatabaseMigration

class `20220120_teleport_history`: DatabaseMigration {
    override val description = "Add player configs and warps"

    override fun execute(database: HikariPooledDatabase) {
        database.executeUpdate(
            """
                    |CREATE TABLE teleport_history (
                    |   `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	                |   `player_uuid` varchar(50) NOT NULL,
	                |   `tp_reason` VARCHAR(50) NOT NULL,
	                |   `server_name` VARCHAR(50) NOT NULL,
	                |   `world_name` VARCHAR(50) NOT NULL,
	                |   `x` DOUBLE NOT NULL DEFAULT 0,
	                |   `y` DOUBLE NOT NULL DEFAULT 0,
	                |   `z` DOUBLE NOT NULL DEFAULT 0,
	                |   `pitch` FLOAT NOT NULL DEFAULT 0,
	                |   `yaw` FLOAT NOT NULL DEFAULT 0,
                    |   `can_go_back` TINYINT(1) DEFAULT 1,
	                |   `created_at` DATETIME NOT NULL,
	                |   PRIMARY KEY (`id`),
                    |   INDEX  (`player_uuid`)
                    |);
                    """
                .trimMargin("|")
                .replace("\n", "")
        )
    }
}