package com.projectcitybuild.pcbridge.paper.features.opelevate.dialogs

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage

class ConfirmOpElevateDialog {
    companion object {
        val reasonKey = "reason"
        val proceedButtonKey = Key.key("pcbridge:dialogs/confirm_op_elevation/submit")

        fun build() = Dialog.create { builder ->
            builder.empty()
                .base(
                    DialogBase.builder(Component.text("Temporary OP Elevation"))
                        .afterAction(DialogBase.DialogAfterAction.CLOSE)
                        .body(body())
                        .inputs(listOf(
                            reasonInput(),
                        ))
                        .build()
                )
                .type(
                    DialogType.confirmation(
                        proceedButton,
                        cancelButton
                    )
                )
        }

        private fun body(): List<PlainMessageDialogBody> {
            val list = mutableListOf<PlainMessageDialogBody>()
            list.add(
                DialogBody.plainMessage(
                    MiniMessage.miniMessage().deserialize("""
                        #<bold>You are about to be granted temporary Operator (OP) privileges on the server.</bold>
                        #
                        #* This grants full administrative permissions.
                        #* Your OP status will be automatically revoked once the set duration expires.
                        #* You may revoke it early at any time using: <aqua>/opend</aqua>
                        #
                        #⚠ Important: OP privileges bypass the normal permission system and can unintentionally override protections or cause unexpected behavior.
                        #Please revoke your OP status as soon as your task is complete.
                    """.trimMargin("#"))
                )
            )
            return list.toList()
        }

        private fun reasonInput() = DialogInput.text(
            reasonKey,
            Component.text("Reason for Elevation")
                .append { Component.text("*").color(NamedTextColor.RED) }
            )
            .maxLength(100)
            .build()

        private val proceedButton get() = ActionButton.create(
            Component.text("I understand.").color(NamedTextColor.RED),
            null,
            100,
            DialogAction.customClick(proceedButtonKey, null),
            )

        private val cancelButton get() = ActionButton.create(
            Component.text("Cancel").color(NamedTextColor.GRAY),
            null,
            100,
            null,
        )
    }
}