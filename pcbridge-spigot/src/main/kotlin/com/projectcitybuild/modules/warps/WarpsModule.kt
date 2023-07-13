package com.projectcitybuild.modules.warps

import com.projectcitybuild.modules.warps.actions.CreateWarp
import com.projectcitybuild.modules.warps.actions.DeleteWarp
import com.projectcitybuild.modules.warps.actions.GetWarpList
import com.projectcitybuild.modules.warps.actions.TeleportToWarp
import com.projectcitybuild.modules.warps.commands.DelWarpCommand
import com.projectcitybuild.modules.warps.commands.SetWarpCommand
import com.projectcitybuild.modules.warps.commands.WarpCommand
import com.projectcitybuild.modules.warps.commands.WarpsCommand
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule

class WarpsModule: PluginModule {
    override fun register(module: ModuleDeclaration) {
        module {
            command(
                DelWarpCommand(
                    DeleteWarp(
                        container.warpRepository,
                        container.localEventBroadcaster,
                    ),
                    container.warpRepository,
                ),
            )
            command(
                SetWarpCommand(
                    CreateWarp(
                        container.warpRepository,
                        container.localEventBroadcaster,
                        container.time,
                    )
                ),
            )
            command(
                WarpsCommand(
                    GetWarpList(
                        container.warpRepository,
                        container.config,
                    )
                ),
            )
            command(
                WarpCommand(
                    TeleportToWarp(
                        container.warpRepository,
                        container.nameGuesser,
                        container.logger,
                        container.localEventBroadcaster,
                        container.server,
                    ),
                    container.warpRepository,
                ),
            )
        }
    }
}