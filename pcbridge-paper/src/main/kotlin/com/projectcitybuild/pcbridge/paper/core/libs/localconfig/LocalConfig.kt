package com.projectcitybuild.pcbridge.paper.core.libs.localconfig

import com.projectcitybuild.pcbridge.paper.core.libs.storage.Storage
import java.io.File

class LocalConfig(
    val file: File,
    val storage: Storage<LocalConfigKeyValues>,
) {
    // Not so nice, but we need the local config immediately without
    // a suspending function, due to the tricky dependency tree
    private val cached: LocalConfigKeyValues = storage.readSync(file)
        ?: LocalConfigKeyValues.default()

    fun get(): LocalConfigKeyValues = cached

    fun bootstrap() {
        if (!file.exists()) {
            storage.writeSync(file, LocalConfigKeyValues.default())
        }
    }
}
