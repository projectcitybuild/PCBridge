package com.projectcitybuild.core.database

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.entities.migrations.* // ktlint-disable no-wildcard-imports
import com.projectcitybuild.pcbridge.core.PlatformLogger

object Migration {

    private val migrations = arrayOf(
        `20220114_first_time_run`(),
        `20220115_player_configs_warps`(),
        `20220120_teleport_history`(),
        `20220201_add_hub`(),
        `20220201_add_ip_bans`(),
        `20220207_add_teleport_message_silencing`(),
        `20220320_rename_queued_warps`(),
        `20220505_delete_queues`(),
        `20220530_delete_bungeecord_cols`(),
        `202200821_add_badge_toggle`(),
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
