package com.projectcitybuild.entities.migrations

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.core.database.DatabaseMigration

class `20220201_add_ip_bans` : DatabaseMigration {
    override val description = "Add IP bans table"

    override fun execute(database: HikariPooledDatabase) {
        database.executeUpdate(
            """
                    |CREATE TABLE ip_bans (
                    |   `ip` VARCHAR(50) NOT NULL,
                    |   `banner_name` VARCHAR(50) NULL,
	                |   `reason` TEXT NULL,
	                |   `created_at` DATETIME NOT NULL,
	                |   PRIMARY KEY (`ip`)
                    |);
                    """
                .trimMargin("|")
                .replace("\n", "")
        )
    }
}
