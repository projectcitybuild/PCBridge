package com.projectcitybuild.pcbridge.paper.features.homes.hooks.dialogs

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
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage

class HomeRenameDialog {
    companion object {
        val newNameKey = "new_name"
        val idKey = "id"
        val saveButtonKey = Key.key("pcbridge:dialogs/home_rename/save")

        fun build(
            homeId: Int,
            prevName: String,
            newName: String? = null,
        ) = Dialog.create { builder ->
            builder.empty()
                .base(
                    DialogBase.builder(Component.text("Rename Home"))
                        .afterAction(DialogBase.DialogAfterAction.CLOSE)
                        .body(body(prevName))
                        .inputs(listOf(
                            newNameInput(newName),
                            idInput("$homeId"),
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

        private fun body(prevName: String): List<PlainMessageDialogBody> {
            return listOf(
                DialogBody.plainMessage(
                    MiniMessage.miniMessage().deserialize(
                        "Current name: <aqua>$prevName</aqua>"
                    )
                )
            )
        }

        private fun newNameInput(newName: String?) = DialogInput.text(
            newNameKey,
            Component.text("New Name")
                .append { Component.text("*").color(NamedTextColor.RED) }
            )
            .initial(newName ?: "")
            .build()

        private fun idInput(id: String) = DialogInput.singleOption(
            idKey,
            Component.text("Home Id"),
            listOf(
                SingleOptionDialogInput.OptionEntry.create(
                    id,
                    Component.text(id),
                    true,
                ),
            ),
        ).build()

        private val verifyButton get() = ActionButton.create(
            Component.text("Save"),
            Component.text("Click to rename the home"),
            100,
            DialogAction.customClick(saveButtonKey, null),
            )

        private val cancelButton get() = ActionButton.create(
            Component.text("Cancel").color(NamedTextColor.GRAY),
            Component.text("Click to discard your input"),
            100,
            null,
        )
    }
}