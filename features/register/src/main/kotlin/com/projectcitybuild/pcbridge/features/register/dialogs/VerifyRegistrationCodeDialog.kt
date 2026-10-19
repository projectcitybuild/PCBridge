package com.projectcitybuild.pcbridge.features.register.dialogs

import com.projectcitybuild.pcbridge.core.l10n.l10n
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
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage

class VerifyRegistrationCodeDialog {
    companion object {
        val codeKey = "code"
        val verifyButtonKey = Key.key("pcbridge:dialogs/verify_registration_code/submit")

        fun build(email: String?, error: Component? = null) = Dialog.create { builder ->
            builder.empty()
                .base(
                    DialogBase.builder(l10n.registration.verifyRegistrationDialogTitle)
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

        private fun body(email: String?, error: Component?): List<PlainMessageDialogBody> {
            val list = mutableListOf<PlainMessageDialogBody>()
            if (error != null) {
                list.add(errorText(error))
            }
            list.add(
                DialogBody.plainMessage(
                    if (email != null) l10n.registration.codeHasBeenEmailedTo(email)
                    else l10n.registration.codeHasBeenEmailed
                )
            )
            return list.toList()
        }

        private fun errorText(error: Component) = DialogBody.plainMessage(
            error
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD)
        )

        private fun codeInput() = DialogInput.text(
            codeKey,
            l10n.registration.registrationCodeInputLabel
            )
            .maxLength(6)
            .build()

        private val verifyButton get() = ActionButton.create(
            l10n.registration.verifyRegistrationButton,
            null,
            100,
            DialogAction.customClick(verifyButtonKey, null),
            )

        private val cancelButton get() = ActionButton.create(
            l10n.dialogs.dialogCancelButtonGray,
            null,
            100,
            null,
        )
    }
}