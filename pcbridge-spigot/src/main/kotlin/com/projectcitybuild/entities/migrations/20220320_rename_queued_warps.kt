package com.projectcitybuild.entities.migrations

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.libs.database.DatabaseMigration

class `20220320_rename_queued_warps` : DatabaseMigration {
    override val description = "Add a column to silence teleport messages"

    override fun execute(database: HikariPooledDatabase) {
        database.executeUpdate(
            """
                    |ALTER TABLE queued_warps 
                    |   RENAME TO queued_location_teleports;
                    """
                .trimMargin("|")
                .replace("\n", "")
        )
    }
}
