package com.projectcitybuild.modules.database

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.modules.database.migrations.*
import com.projectcitybuild.modules.logger.PlatformLogger

object Migration {

    private val migrations = arrayOf(
        `20220114_first_time_run`(),
        `20220115_player_configs_warps`(),
        `20220120_teleport_history`(),
    )

    fun executeIfNecessary(
        database: HikariPooledDatabase,
        logger: PlatformLogger,
        currentVersion: Int
    ) {
        var version = currentVersion
        val totalMigrations = migrations.size

        if (version >= totalMigrations) return

        while (version < totalMigrations) {
            val migration = migrations[version]
            logger.info("Running migration ${version + 1}: ${migration.description}")
            migration.execute(database)
            version++
        }

        if (currentVersion != totalMigrations) {
            updateVersion(database, version)
        }
    }

    private fun updateVersion(database: HikariPooledDatabase, newVersion: Int) {
        database.executeUpdate("UPDATE meta SET `version`=?", newVersion)
    }
}