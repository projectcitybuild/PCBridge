package com.projectcitybuild.modules.proxyadapter.playerlist

import java.util.UUID

interface OnlinePlayerList {
    fun getUUID(name: String): UUID?
}
