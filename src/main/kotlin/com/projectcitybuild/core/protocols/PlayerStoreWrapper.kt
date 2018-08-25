package com.projectcitybuild.core.protocols

import com.projectcitybuild.core.services.PlayerStore

interface PlayerStoreWrapper {
    val store: PlayerStore
}