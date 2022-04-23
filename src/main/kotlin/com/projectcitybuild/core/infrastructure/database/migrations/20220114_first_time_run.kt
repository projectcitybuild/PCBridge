package com.projectcitybuild.core.infrastructure.database.migrations

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.core.infrastructure.database.DatabaseMigration

class `20220114_first_time_run` : DatabaseMigration {
    override val description = "First-time run"

    override fun execute(database: HikariPooledDatabase) {
        database.executeUpdate("CREATE TABLE IF NOT EXISTS meta(version INT(64));")
        database.executeInsert("INSERT INTO meta VALUES(1);")
    }
}
