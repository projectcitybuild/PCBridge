package com.projectcitybuild.core.contracts

import org.bukkit.event.Event

interface Controller {
    val commands: Array<Commandable>
        get() = arrayOf()

    val listeners: Array<Listenable<Event>>
        get() = arrayOf()
}
