package com.projectcitybuild.modules.database

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.entities.migrations.*
import com.projectcitybuild.modules.logger.LoggerProvider
import net.md_5.bungee.api.plugin.Plugin

object Migration {

    private val migrations = arrayOf(
        `20220114_first_time_run`(),
        `20220115_player_configs_warps`(),
    )

    fun executeIfNecessary(
        database: HikariPooledDatabase,
        logger: LoggerProvider,
        plugin: Plugin, // temporary
        currentVersion: Int
    ) {
        var version = currentVersion
        val totalMigrations = migrations.size

        if (version >= totalMigrations) return

        while (version < totalMigrations) {
            val migration = migrations[version]
            logger.info("Running migration ${version + 1}: ${migration.description}")
            migration.execute(database, plugin)
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