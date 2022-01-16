package com.projectcitybuild.modules.database

import com.zaxxer.hikari.HikariDataSource
import net.md_5.bungee.api.plugin.Plugin

interface DatabaseMigration {
    val description: String
    fun execute(dataSource: HikariDataSource, plugin: Plugin)
}