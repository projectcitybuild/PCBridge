package com.projectcitybuild.pcbridge.core.modules.config

interface ConfigStorage<T> {
    fun get(): T?
}
