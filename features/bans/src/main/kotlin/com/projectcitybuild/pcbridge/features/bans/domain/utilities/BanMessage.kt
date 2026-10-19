package com.projectcitybuild.pcbridge.features.bans.domain.utilities

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.IPBan
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PlayerBan
import net.kyori.adventure.text.minimessage.MiniMessage

fun PlayerBan.toMiniMessage() =
    l10n.bans.playerBanMessage(
        reason?.let { MiniMessage.miniMessage().deserialize(it) },
        expiresAt?.toString(),
    )

fun IPBan.toMiniMessage() =
    l10n.bans.ipBanMessage(reason?.let { MiniMessage.miniMessage().deserialize(it) })
