package com.projectcitybuild.core.contracts

import com.projectcitybuild.api.client.PCBClient
import com.projectcitybuild.entities.LogLevel
import com.projectcitybuild.entities.models.Player
import com.projectcitybuild.entities.models.PluginConfigPair
import net.milkbowl.vault.permission.Permission
import java.util.*

interface Environment {
    fun get(key: PluginConfigPair) : Any { throw NotImplementedError() }
    fun set(key: PluginConfigPair, value: Any) { throw NotImplementedError() }
    fun log(level: LogLevel, message: String) { System.out.println(message) }
    fun get(player: UUID) : Player? { throw NotImplementedError() }
    fun set(player: Player) { throw NotImplementedError() }
    fun permissions() : Permission? { throw NotImplementedError() }
    fun apiClient() : PCBClient { throw NotImplementedError() }
}