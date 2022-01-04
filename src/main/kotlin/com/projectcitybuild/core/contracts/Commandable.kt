package com.projectcitybuild.core.contracts

import com.projectcitybuild.entities.CommandInput
import com.projectcitybuild.entities.CommandResult

/**
 * Represents a Command that a user can use to perform
 * some kind of action.
 *
 * For example, performing a player ban would be handled by
 * BanCommand inheriting from Commandable
 */
interface Commandable {

    // String used to execute the command in-game (eg. "ban", "unban")
    val label: String

    // Alternative Strings to execute the command in-game
    val aliases: Array<String>
        get() = arrayOf()

    // Permission node required to execute the command
    val permission: String

    suspend fun execute(input: CommandInput): CommandResult
}
