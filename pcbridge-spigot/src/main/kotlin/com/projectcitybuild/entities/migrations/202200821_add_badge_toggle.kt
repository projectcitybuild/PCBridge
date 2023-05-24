package com.projectcitybuild.entities.migrations

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.core.database.DatabaseMigration

class `202200821_add_badge_toggle` : DatabaseMigration {
    override val description = "Add a column to toggle the chat badge on/off"

    override fun execute(database: HikariPooledDatabase) {
        database.executeUpdate(
            """
                    |ALTER TABLE players 
                    |   ADD `is_badge_disabled` TINYINT(1) DEFAULT 0 NOT NULL AFTER `is_muted`;
                    """
                .trimMargin("|")
                .replace("\n", "")
        )
    }
}
