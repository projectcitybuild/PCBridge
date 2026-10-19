package com.projectcitybuild.pcbridge.features.warps.hooks.dialogs

import com.projectcitybuild.pcbridge.core.l10n.l10n
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

class WarpRenameDialog {
    companion object {
        val newNameKey = "new_name"
        val idKey = "id"
        val saveButtonKey = Key.key("pcbridge:dialogs/warp_rename/save")

        fun build(
            warpId: Int,
            prevName: String,
            newName: String? = null,
        ) = Dialog.create { builder ->
            builder.empty()
                .base(
                    DialogBase.builder(l10n.warps.renameWarpDialogTitle)
                        .afterAction(DialogBase.DialogAfterAction.CLOSE)
                        .body(body(prevName))
                        .inputs(
                            listOf(
                                newNameInput(newName),
                                idInput("$warpId"),
                            ),
                        )
                        .build(),
                )
                .type(
                    DialogType.confirmation(
                        verifyButton,
                        cancelButton,
                    ),
                )
        }

        private fun body(prevName: String): List<PlainMessageDialogBody> {
            return listOf(
                DialogBody.plainMessage(
                    l10n.dialogs.currentResourceName(prevName),
                ),
            )
        }

        private fun newNameInput(newName: String?) =
            DialogInput.text(
                newNameKey,
                l10n.dialogs.newNameInputLabel,
            )
                .initial(newName ?: "")
                .build()

        private fun idInput(id: String) =
            DialogInput.singleOption(
                idKey,
                l10n.warps.warpIdInputLabel,
                listOf(
                    SingleOptionDialogInput.OptionEntry.create(
                        id,
                        Component.text(id),
                        true,
                    ),
                ),
            ).build()

        private val verifyButton get() =
            ActionButton.create(
                l10n.dialogs.dialogSaveButton,
                l10n.warps.renameWarpButtonHover,
                100,
                DialogAction.customClick(saveButtonKey, null),
            )

        private val cancelButton get() =
            ActionButton.create(
                l10n.dialogs.dialogCancelButtonGray,
                l10n.dialogs.discardDialogInputHover,
                100,
                null,
            )
    }
}
