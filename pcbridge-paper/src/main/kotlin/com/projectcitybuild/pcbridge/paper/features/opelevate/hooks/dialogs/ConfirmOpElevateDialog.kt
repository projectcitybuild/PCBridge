package com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.dialogs

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.TextDialogInput.MultilineOptions
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
                    DialogBase.builder(Component.text("Warning"))
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
                        #<yellow><b>You are about to receive temporary Operator (OP) privileges.</b></yellow>
                        #
                        #• <aqua>Full administrative permissions</aqua> will be granted.
                        #• Your OP status will be <aqua>automatically revoked</aqua> after the set duration.
                        #• You may revoke it early at any time with: <aqua><b>/opend</b></aqua>
                        #
                        #<dark_red><b>⚠ IMPORTANT</b></dark_red>
                        #<i>
                        #OP privileges bypass the normal permission system. They may override protections or cause unintended behavior.
                        #
                        #Please revoke your OP status as soon as your task is complete.
                        #</i>
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
            Component.text("I understand."),
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