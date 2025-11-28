package com.projectcitybuild.pcbridge.paper.features.homes.hooks.listeners

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.homes.domain.repositories.HomeRepository
import com.projectcitybuild.pcbridge.paper.features.homes.homesTracer
import com.projectcitybuild.pcbridge.paper.features.homes.hooks.dialogs.HomeRenameDialog
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.event.player.PlayerCustomClickEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class HomeRenameDialogListener(
    private val homeRepository: HomeRepository,
) : Listener {
    @EventHandler
    suspend fun onPlayerCustomClickEvent(
        event: PlayerCustomClickEvent,
    ) = event.scoped(homesTracer, this::class.java) {
        if (! event.identifier.equals(HomeRenameDialog.saveButtonKey)) return@scoped

        val view = event.dialogResponseView
        if (view == null) {
            log.error { "DialogResponseView was null for ${event.identifier}" }
            return@scoped
        }

        val homeId = view.getText(HomeRenameDialog.idKey)
        val newName = view.getText(HomeRenameDialog.newNameKey)?.trim()

        logSync.info("Dialog response received", mapOf(
            "new_name" to newName,
            "home_id" to homeId,
        ))

        val connection = event.commonConnection as? PlayerGameConnection
        val player = connection?.player ?: return@scoped

        if (newName.isNullOrEmpty()) {
            player.sendRichMessage("<red>Error: New name cannot be empty</red>")
            return@scoped
        }
        if (homeId.isNullOrEmpty()) {
            player.sendRichMessage("<red>Error: Home not found</red>")
            throw Exception("Rename failed: home id was null")
        }
        try {
            homeRepository.rename(
                id = homeId.toInt(),
                newName = newName,
                player = player,
            )
            player.sendRichMessage(l10n.homeRenamed(newName))
        } catch (e: Exception) {
            player.sendRichMessage("<red>Error: ${e.message}</red>")
            throw e
        }
    }
}