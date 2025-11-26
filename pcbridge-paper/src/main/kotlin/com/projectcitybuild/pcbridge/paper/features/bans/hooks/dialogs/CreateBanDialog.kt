package com.projectcitybuild.pcbridge.paper.features.bans.hooks.dialogs

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.TextDialogInput.MultilineOptions
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class CreateBanDialog {
    companion object {
        val playerNameKey = "player_name"
        val reasonKey = "reason"
        val additionalInfoKey = "additional_info"
        val submitBanButtonKey = Key.key("pcbridge:dialogs/create_ban/submit")

        fun build(
            playerName: String?,
            reason: String? = null,
            additionalInfo: String? = null,
            error: String? = null,
        ) = Dialog.create { builder ->
            builder.empty()
                .base(
                    DialogBase.builder(Component.text("Ban Player"))
                        .afterAction(DialogBase.DialogAfterAction.CLOSE)
                        .body(error?.let { listOf(errorText(it)) } ?: listOf())
                        .inputs(listOf(
                            playerNameInput(initial = playerName),
                            reasonInput(initial = reason ?: "Griefing"),
                            additionalInfoInput(initial = additionalInfo),
                        ))
                        .build()
                )
                .type(
                    DialogType.confirmation(
                        createButton,
                        cancelButton
                    )
                )
        }

        private fun errorText(error: String) = DialogBody.plainMessage(
            Component.text(error)
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD)
        )

        private fun playerNameInput(initial: String?) = DialogInput.text(
            playerNameKey,
            Component.text("Player Name")
                .append { Component.text("*").color(NamedTextColor.RED) }
            )
            .initial(initial ?: "")
            .build()

        private fun reasonInput(initial: String?) = DialogInput.text(
            reasonKey,
            Component.text("Reason (shown to player)")
                .append { Component.text("*").color(NamedTextColor.RED) }
            )
            .initial(initial ?: "")
            .maxLength(200)
            .build()

        private fun additionalInfoInput(initial: String?) = DialogInput.text(additionalInfoKey, Component.text("Additional Info/Context"))
            .initial(initial ?: "")
            .multiline(MultilineOptions.create(null, 80))
            .build()

        private val createButton get() = ActionButton.create(
            Component.text("Ban Player").color(NamedTextColor.RED),
            Component.text("Click to ban the player"),
            100,
            DialogAction.customClick(submitBanButtonKey, null),
            )

        private val cancelButton get() = ActionButton.create(
            Component.text("Cancel"),
            Component.text("Click to discard your input"),
            100,
            null,
        )
    }
}