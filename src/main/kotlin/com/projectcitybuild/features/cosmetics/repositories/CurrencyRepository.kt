package com.projectcitybuild.features.cosmetics.repositories

import java.util.*
import javax.inject.Inject

class CurrencyRepository @Inject constructor() {

    fun getBalance(playerUUID: UUID): Int {
        // TODO: GadgetsMenu hits this API a LOT so we'll need to cache this
        return 1000
    }

    fun setBalance(playerUUID: UUID, newBalance: Int) {

    }

    fun add(playerUUID: UUID, amount: Int) {

    }

    fun deduct(playerUUID: UUID, amount: Int) {

    }
}