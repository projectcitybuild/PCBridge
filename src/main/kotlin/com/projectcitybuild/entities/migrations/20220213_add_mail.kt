package com.projectcitybuild.entities.migrations

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.core.infrastructure.database.DatabaseMigration

class `20220213_add_mail`: DatabaseMigration {
    override val description = "Add a table to hold mail"

    override fun execute(database: HikariPooledDatabase) {
        database.executeUpdate(
            """
                    |CREATE TABLE mail (
                    |   `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                    |   `sender_uuid` VARCHAR(50) NOT NULL,
                    |   `sender_name` VARCHAR(50) NOT NULL,
                    |   `receiver_uuid` VARCHAR(50) NOT NULL,
                    |   `receiver_name` VARCHAR(50) NOT NULL,
	                |   `message` TEXT NULL,
                    |   `is_read` TINYINT(1) DEFAULT 0 NOT NULL,
                    |   `is_cleared` TINYINT(1) DEFAULT 0 NOT NULL,
                    |   `read_at` DATETIME NULL,
                    |   `cleared_at DATETIME NULL,
	                |   `created_at` DATETIME NOT NULL,
	                |   PRIMARY KEY (`id`)
                    |);
                    """
                .trimMargin("|")
                .replace("\n", "")
        )
    }
}