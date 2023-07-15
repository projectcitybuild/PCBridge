package com.projectcitybuild.modules.pluginutils.actions

import com.projectcitybuild.entities.PluginVersion
import java.util.Properties

class GetVersion {

    fun execute(): PluginVersion {
        val properties = Properties().apply {
            load(object {}.javaClass.getResourceAsStream("/version.properties"))
        }
        return PluginVersion(
            version = properties.getProperty("version"),
            commitHash = properties.getProperty("commit"),
        )
    }
}
