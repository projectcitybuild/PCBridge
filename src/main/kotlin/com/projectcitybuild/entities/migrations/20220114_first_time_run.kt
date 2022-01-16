package com.projectcitybuild.entities.migrations

import com.projectcitybuild.modules.database.DatabaseMigration
import com.zaxxer.hikari.HikariDataSource
import net.md_5.bungee.api.plugin.Plugin

class `20220114_first_time_run`: DatabaseMigration {
    override val description = "First-time run"

    override fun execute(dataSource: HikariDataSource, plugin: Plugin) {
        val connection = dataSource.connection

        connection
            .prepareStatement("CREATE TABLE IF NOT EXISTS meta(version INT(64));")
            .executeUpdate()

        connection
            .prepareStatement("INSERT INTO meta VALUES(1);")
            .executeUpdate()
    }
}