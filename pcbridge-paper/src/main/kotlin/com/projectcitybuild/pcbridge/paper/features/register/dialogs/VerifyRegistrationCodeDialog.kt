package com.projectcitybuild.pcbridge.paper.features.register.dialogs

import com.projectcitybuild.pcbridge.paper.l10n.l10n
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
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage

class VerifyRegistrationCodeDialog {
    companion object {
        val codeKey = "code"
        val submitBanButtonKey = Key.key("pcbridge:dialogs/verify_registration_code/submit")

        fun build(email: String?, error: String? = null) = Dialog.create { builder ->
            builder.empty()
                .base(
                    DialogBase.builder(Component.text("Enter Code"))
                        .afterAction(DialogBase.DialogAfterAction.CLOSE)
                        .body(body(email, error))
                        .inputs(listOf(
                            codeInput(),
                        ))
                        .build()
                )
                .type(
                    DialogType.confirmation(
                        verifyButton,
                        cancelButton
                    )
                )
        }

        private fun body(email: String?, error: String?): List<PlainMessageDialogBody> {
            val list = mutableListOf<PlainMessageDialogBody>()
            if (error != null) {
                list.add(errorText(error))
            }
            list.add(
                DialogBody.plainMessage(
                    MiniMessage.miniMessage().deserialize(
                        if (email != null) l10n.codeHasBeenEmailedTo(email)
                        else l10n.codeHasBeenEmailed
                    )
                )
            )
            return list.toList()
        }

        private fun errorText(error: String) = DialogBody.plainMessage(
            MiniMessage.miniMessage().deserialize(error)
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD)
        )

        private fun codeInput() = DialogInput.text(
            codeKey,
            Component.text("Code")
                .append { Component.text("*").color(NamedTextColor.RED) }
            )
            .build()

        private val verifyButton get() = ActionButton.create(
            Component.text("Verify"),
            null,
            100,
            DialogAction.customClick(submitBanButtonKey, null),
            )

        private val cancelButton get() = ActionButton.create(
            Component.text("Cancel").color(NamedTextColor.GRAY),
            null,
            100,
            null,
        )
    }
}