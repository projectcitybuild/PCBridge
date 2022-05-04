package com.projectcitybuild.core.infrastructure.database.migrations

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.core.infrastructure.database.DatabaseMigration

class `20220505_delete_queues` : DatabaseMigration {
    override val description = "Delete queued teleport tables"

    override fun execute(database: HikariPooledDatabase) {
        database.executeUpdate(
            """
                    |DROP TABLE queued_location_teleports;
                    """
                .trimMargin("|")
                .replace("\n", "")
        )

        database.executeUpdate(
            """
                    |DROP TABLE queued_teleports;
                    """
                .trimMargin("|")
                .replace("\n", "")
        )
    }
}
