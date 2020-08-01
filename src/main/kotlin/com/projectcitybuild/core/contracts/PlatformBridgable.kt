package com.projectcitybuild.core.contracts

interface PlatformBridgable {

    val environment: EnvironmentProvider

    fun onEnable()
    fun onDisable()
}