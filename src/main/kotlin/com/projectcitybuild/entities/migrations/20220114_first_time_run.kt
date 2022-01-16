package com.projectcitybuild.entities.migrations

import co.aikar.idb.HikariPooledDatabase
import com.projectcitybuild.modules.database.DatabaseMigration
import net.md_5.bungee.api.plugin.Plugin

class `20220114_first_time_run`: DatabaseMigration {
    override val description = "First-time run"

    override fun execute(database: HikariPooledDatabase, plugin: Plugin) {
        database.executeUpdate("CREATE TABLE IF NOT EXISTS meta(version INT(64));")
        database.executeInsert("INSERT INTO meta VALUES(1);")
    }
}