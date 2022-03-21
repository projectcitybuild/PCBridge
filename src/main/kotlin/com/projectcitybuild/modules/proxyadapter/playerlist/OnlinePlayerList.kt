package com.projectcitybuild.modules.proxyadapter.playerlist

import java.util.*

interface OnlinePlayerList {
    fun getUUID(name: String): UUID?
}
