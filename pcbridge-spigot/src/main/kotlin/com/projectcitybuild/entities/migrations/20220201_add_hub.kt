package com.projectcitybuild.entities.migrations

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.core.database.DatabaseMigration

class `20220201_add_hub` : DatabaseMigration {
    override val description = "Add hub location"

    override fun execute(database: HikariPooledDatabase) {
        database.executeUpdate(
            """
                    |CREATE TABLE hub (
                    |   `server_name` VARCHAR(50) NOT NULL,
	                |   `world_name` VARCHAR(50) NOT NULL,
	                |   `x` DOUBLE NOT NULL DEFAULT 0,
	                |   `y` DOUBLE NOT NULL DEFAULT 0,
	                |   `z` DOUBLE NOT NULL DEFAULT 0,
	                |   `pitch` FLOAT NOT NULL DEFAULT 0,
	                |   `yaw` FLOAT NOT NULL DEFAULT 0,
	                |   `created_at` DATETIME NOT NULL
                    |);
                    """
                .trimMargin("|")
                .replace("\n", "")
        )
    }
}
