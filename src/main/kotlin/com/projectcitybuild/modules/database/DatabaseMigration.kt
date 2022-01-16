package com.projectcitybuild.modules.database

import co.aikar.idb.HikariPooledDatabase
import net.md_5.bungee.api.plugin.Plugin

interface DatabaseMigration {
    val description: String
    fun execute(database: HikariPooledDatabase, plugin: Plugin)
}