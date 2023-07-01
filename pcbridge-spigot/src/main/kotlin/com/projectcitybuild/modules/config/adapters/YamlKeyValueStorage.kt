package com.projectcitybuild.modules.config.adapters

import com.projectcitybuild.modules.config.ConfigStorageKey
import com.projectcitybuild.modules.config.KeyValueStorage
import com.projectcitybuild.modules.storage.StoragePath
import com.projectcitybuild.modules.storage.adapters.YamlStorage

class YamlKeyValueStorage(
    private val storage: YamlStorage,
) : KeyValueStorage {

    override fun <T> get(key: ConfigStorageKey<T>): T {
        return storage.get(
            StoragePath(
                key = key.path,
                defaultValue = key.defaultValue,
            )
        )
    }

    override fun <T> set(key: ConfigStorageKey<T>, value: T) {
        storage.set(
            path = StoragePath(
                key = key.path,
                defaultValue = key.defaultValue,
            ),
            value = value,
        )
    }
}
