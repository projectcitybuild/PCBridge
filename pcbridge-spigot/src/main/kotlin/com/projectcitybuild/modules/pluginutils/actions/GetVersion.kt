package com.projectcitybuild.modules.pluginutils.actions

import java.util.Properties

class GetVersion {

    data class Version(
        val version: String,
        val commitHash: String,
    )

    fun execute(): Version {
        val properties = Properties().apply {
            load(object {}.javaClass.getResourceAsStream("/version.properties"))
        }
        return Version(
            version = properties.getProperty("version"),
            commitHash = properties.getProperty("commit"),
        )
    }
}
