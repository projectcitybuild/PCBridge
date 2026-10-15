package com.projectcitybuild.pcbridge.paper.core.libs.localconfig

import java.io.File

interface LocalConfigBacking<T> {
    fun readSync(file: File): T?
}

class LocalConfig(
    file: File,
    fileStorage: LocalConfigBacking<LocalConfigKeyValues>,
) {
    // Not so nice, but we need the local config immediately without
    // a suspending function, due to the tricky dependency tree
    private val cached: LocalConfigKeyValues =
        fileStorage.readSync(file)
            ?: LocalConfigKeyValues.default()

    fun get(): LocalConfigKeyValues = cached
}
