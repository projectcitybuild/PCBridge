package com.projectcitybuild.pcbridge.paper.core.libs.localconfig

import com.projectcitybuild.pcbridge.paper.core.libs.storage.Storage
import java.io.File

class LocalConfig(
    file: File,
    storage: Storage<LocalConfigKeyValues>,
) {
    // Not so nice, but we need the local config immediately without
    // a suspending function, due to the tricky dependency tree
    private val cached: LocalConfigKeyValues = storage.readSync(file)
        ?: LocalConfigKeyValues.default()

    fun get(): LocalConfigKeyValues = cached
}
