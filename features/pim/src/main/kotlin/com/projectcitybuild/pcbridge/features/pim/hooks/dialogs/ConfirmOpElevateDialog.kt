package com.projectcitybuild.pcbridge.features.pim.hooks.dialogs

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

class ConfirmOpElevateDialog {
    companion object {
        const val REASON_KEY = "reason"
        val proceedButtonKey = Key.key("pcbridge:dialogs/confirm_op_elevation/submit")

        fun build() =
            Dialog.create { builder ->
                builder.empty()
                    .base(
                        DialogBase.builder(l10n.pim.opElevationDialogTitle)
                            .afterAction(DialogBase.DialogAfterAction.CLOSE)
                            .body(body())
                            .inputs(
                                listOf(
                                    reasonInput(),
                                ),
                            )
                            .build(),
                    )
                    .type(
                        DialogType.confirmation(
                            proceedButton,
                            cancelButton,
                        ),
                    )
            }

        private fun body(): List<PlainMessageDialogBody> {
            val list = mutableListOf<PlainMessageDialogBody>()
            list.add(
                DialogBody.plainMessage(
                    l10n.pim.opElevationWarning,
                ),
            )
            return list.toList()
        }

        private fun reasonInput() =
            DialogInput.text(
                REASON_KEY,
                l10n.pim.opElevationReasonInputLabel,
            )
                .maxLength(100)
                .build()

        private val proceedButton get() =
            ActionButton.create(
                l10n.pim.opElevationConfirmButton,
                null,
                100,
                DialogAction.customClick(proceedButtonKey, null),
            )

        private val cancelButton get() =
            ActionButton.create(
                l10n.dialogs.dialogCancelButtonGray,
                null,
                100,
                null,
            )
    }
}
