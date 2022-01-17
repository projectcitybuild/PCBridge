package com.projectcitybuild.features.importer

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.features.importer.commands.ImportCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import javax.inject.Inject

class ImporterModule @Inject constructor(
    importCommand: ImportCommand
): BungeecordFeatureModule {

    override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
        importCommand,
    )
}