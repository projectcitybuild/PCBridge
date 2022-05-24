package com.projectcitybuild.modules.config

import com.projectcitybuild.core.storage.Storage
import javax.inject.Inject

class Config @Inject constructor(
    private val configKeys: ConfigKeys,
    private val storage: Storage,
) {
    fun cache() {

    }

    fun flush() {

    }
}