package com.projectcitybuild.core.contracts;

import kotlin.NotImplementedError;

interface CommandDelegatable {
    fun register(command: Commandable) { throw NotImplementedError() }
}
