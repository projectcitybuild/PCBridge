package com.projectcitybuild.entities.migrations

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.core.database.DatabaseMigration

class `20220207_add_teleport_message_silencing` : DatabaseMigration {
    override val description = "Add a column to silence teleport messages"

    override fun execute(database: HikariPooledDatabase) {
        database.executeUpdate(
            """
                    |ALTER TABLE queued_teleports 
                    |   ADD `is_silent_tp` TINYINT(1) DEFAULT 0 NOT NULL AFTER `teleport_type`;
                    """
                .trimMargin("|")
                .replace("\n", "")
        )
    }
}
