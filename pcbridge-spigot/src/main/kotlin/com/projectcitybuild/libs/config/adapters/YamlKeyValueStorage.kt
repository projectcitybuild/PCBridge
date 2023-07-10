package com.projectcitybuild.libs.config.adapters

import com.projectcitybuild.libs.config.ConfigStorageKey
import com.projectcitybuild.libs.config.KeyValueStorage
import com.projectcitybuild.libs.storage.StoragePath
import com.projectcitybuild.libs.storage.adapters.YamlStorage

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
