package com.projectcitybuild.features.utilities.usecases

import com.projectcitybuild.core.Regex
import com.projectcitybuild.core.database.DataSource
import com.projectcitybuild.entities.IPBan
import com.projectcitybuild.support.spigot.logger.PlatformLogger
import com.projectcitybuild.support.textcomponent.send
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.repositories.IPBanRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DataImportUseCase @Inject constructor(
    private val plugin: Plugin,
    private val dataSource: DataSource,
    private val logger: PlatformLogger,
) {
    @Serializable
    data class SpigotBannedIP(
        val ip: String,
        val created: String,
        val source: String,
        val expires: String,
        val reason: String,
    )

    fun execute(sender: Player, args: List<String>) {
        if (args.size <= 1) throw InvalidCommandArgumentsException()

        val name = args[1]
        when (name) {
            "banned-ips" -> {
                val file = File(plugin.dataFolder, "banned-ips.json")
                val json = file.readLines().joinToString(separator = "")
                if (json.isEmpty()) return

                var numberImported = 0
                val repository = IPBanRepository(dataSource)
                Json.decodeFromString<Array<SpigotBannedIP>>(string = json).forEach {
                    val existing = repository.get(it.ip)
                    if (existing != null) return@forEach

                    logger.debug(it.toString())

                    // Filter out malformed or invalid IPs
                    if (!Regex.IP.matcher(it.ip).matches()) {
                        return@forEach
                    }

                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")
                    val createdAt = LocalDateTime.parse(it.created, formatter)

                    // Get rid of the default Spigot message
                    val reason = if (it.reason == "The Ban Hammer has spoken!") "" else it.reason

                    // Strip color codes from names
                    val bannerName = it.source.let {
                        var name = ""
                        var i = 0
                        while (i < it.length) {
                            val char = it[i]
                            if (char.toString() == "§") {
                                // Also skip the character after the symbol
                                i++
                            } else {
                                name += char
                            }
                            i++
                        }
                        name
                    }

                    val ban = IPBan(
                        it.ip,
                        bannerName,
                        reason,
                        createdAt,
                    )
                    repository.put(ban)
                    numberImported++
                }
                sender.send().success("Imported $numberImported IP bans")
            }
            else -> sender.send().error("Invalid import")
        }
    }
}
