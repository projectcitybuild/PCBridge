package com.projectcitybuild.core.contracts

interface ListenerDelegatable {

    fun register(listener: Listenable<*>) { throw NotImplementedError() }
    fun unregisterAll() { throw NotImplementedError() }
}