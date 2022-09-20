package com.projectcitybuild.features.utilities.usecases

import java.util.Properties
import javax.inject.Inject

class GetVersion @Inject constructor() {

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
