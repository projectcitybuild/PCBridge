package com.projectcitybuild.entities.migrations

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.libs.database.DatabaseMigration

class `20220530_delete_bungeecord_cols` : DatabaseMigration {
    override val description = "Delete columns that were previously needed for Bungeecord"

    override fun execute(database: HikariPooledDatabase) {
        database.executeUpdate(
            """
                    |DROP TABLE chat_ignores;
                    """
                .trimMargin("|")
                .replace("\n", "")
        )

        database.executeUpdate(
            """
                    |DROP TABLE last_known_locations;
                    """
                .trimMargin("|")
                .replace("\n", "")
        )

        database.executeUpdate(
            """
                    |DROP TABLE hub;
                    """
                .trimMargin("|")
                .replace("\n", "")
        )

        database.executeUpdate(
            """
                    |ALTER TABLE players DROP COLUMN is_allowing_tp;
                    """
                .trimMargin("|")
                .replace("\n", "")
        )

        database.executeUpdate(
            """
                    |ALTER TABLE warps DROP COLUMN server_name;
                    """
                .trimMargin("|")
                .replace("\n", "")
        )
    }
}
