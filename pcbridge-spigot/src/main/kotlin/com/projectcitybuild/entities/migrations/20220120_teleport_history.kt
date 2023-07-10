package com.projectcitybuild.entities.migrations

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.libs.database.DatabaseMigration

class `20220120_teleport_history` : DatabaseMigration {
    override val description = "Add last_known_locations"

    override fun execute(database: HikariPooledDatabase) {
        database.executeUpdate(
            """
                    |CREATE TABLE last_known_locations (
	                |   `player_uuid` varchar(50) NOT NULL,
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
    }
}
