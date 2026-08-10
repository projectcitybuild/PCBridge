package com.projectcitybuild.pcbridge.core.playerlookup

import java.util.UUID

interface OnlinePlayerFinder {
    fun findByName(name: String): UUID?
}