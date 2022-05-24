package com.projectcitybuild.core.database

import co.aikar.idb.HikariPooledDatabase

interface DatabaseMigration {
    val description: String
    fun execute(database: HikariPooledDatabase)
}
