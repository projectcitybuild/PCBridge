package com.projectcitybuild.core.contracts;

import kotlin.NotImplementedError;

/**
 * Represents a command event handler.
 *
 * A CommandDelegatable is responsible for registering and instantializing
 * all available commands, then delegating to them if the user uses one.
 *
 */
interface CommandDelegatable {
    fun register(command: Commandable) { throw NotImplementedError() }
}
