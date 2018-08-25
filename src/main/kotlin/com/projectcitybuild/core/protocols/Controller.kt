package com.projectcitybuild.core.protocols

interface Controller {
    val commands: Array<Commandable>
        get() = arrayOf()

    val listeners: Array<Listenable>
        get() = arrayOf()
}
