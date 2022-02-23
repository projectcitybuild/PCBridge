package com.projectcitybuild.features.mail

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.features.mail.commands.MailCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import javax.inject.Inject

class MailModule @Inject constructor(
    mailCommand: MailCommand,
): BungeecordFeatureModule {

    override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
        mailCommand,
    )
}
