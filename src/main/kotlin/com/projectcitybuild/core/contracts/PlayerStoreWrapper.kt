package com.projectcitybuild.core.contracts

import com.projectcitybuild.core.services.PlayerStore

interface PlayerStoreWrapper {
    val store: PlayerStore
}