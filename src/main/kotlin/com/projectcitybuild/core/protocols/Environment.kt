package com.projectcitybuild.core.protocols

import com.projectcitybuild.entities.models.LogLevel
import com.projectcitybuild.entities.models.Player
import com.projectcitybuild.entities.models.PluginConfigPair
import java.util.*

interface Environment {
    fun get(key: PluginConfigPair) : Any { throw NotImplementedError() }
    fun set(key: PluginConfigPair, value: Any) { throw NotImplementedError() }

    fun log(level: LogLevel, message: String) { System.out.println(message) }

    fun get(player: UUID) : Player? { throw NotImplementedError() }
    fun set(player: Player) { throw NotImplementedError() }
}