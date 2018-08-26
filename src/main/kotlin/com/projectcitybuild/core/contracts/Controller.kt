package com.projectcitybuild.core.contracts

interface Controller {
    val commands: Array<Commandable>
        get() = arrayOf()

    val listeners: Array<Listenable>
        get() = arrayOf()
}
