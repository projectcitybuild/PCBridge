package com.projectcitybuild.modules.config.adapters

import com.projectcitybuild.core.storage.StoragePath
import com.projectcitybuild.core.storage.adapters.YamlStorage
import com.projectcitybuild.modules.config.ConfigStorageKey
import com.projectcitybuild.modules.config.KeyValueStorage
import javax.inject.Inject
import kotlin.reflect.KClass

class YamlKeyValueStorage @Inject constructor(
    private val storage: YamlStorage,
): KeyValueStorage {

    override fun <T> get(key: ConfigStorageKey<T>): T {
        return storage.get(
            StoragePath(
                key = key.path,
                defaultValue = key.defaultValue,
            )
        )
    }

    override fun <T : Any> get(type: KClass<out T>, path: String): T? {
        return storage.get(path) as T
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