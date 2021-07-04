package com.projectcitybuild.core.entities

data class CommandInput<Player>(
        val sender: Player,
        val args: List<String>,
        val isConsole: Boolean
)