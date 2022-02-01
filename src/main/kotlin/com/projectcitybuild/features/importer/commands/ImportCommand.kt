package com.projectcitybuild.features.importer.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.Regex
import com.projectcitybuild.entities.IPBan
import com.projectcitybuild.features.bans.repositories.IPBanRepository
import com.projectcitybuild.features.hub.HubFileStorage
import com.projectcitybuild.modules.database.DataSource
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Plugin
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ImportCommand @Inject constructor(
    private val plugin: Plugin,
    private val dataSource: DataSource,
    private val logger: PlatformLogger,
): BungeecordCommand {

    override val label = "pcbridge"
    override val permission = "pcbridge.utilities"
    override val usageHelp = "/pcbridge"

    override suspend fun execute(input: BungeecordCommandInput) {
        when {
            input.args.isEmpty() -> throw InvalidCommandArgumentsException()
            input.args.first() == "import" -> import(input.sender, input.args)
            else -> throw InvalidCommandArgumentsException()
        }
    }

    @Serializable
    data class SpigotBannedIP(
        val ip: String,
        val created: String,
        val source: String,
        val expires: String,
        val reason: String,
    )

    private fun import(sender: CommandSender, args: List<String>) {
        if (args.size <= 1) throw InvalidCommandArgumentsException()

        val name = args[1]
        when (name) {
            "hub" -> {
                val storage = HubFileStorage(plugin.dataFolder)
                val hub = storage.load()

                if (hub != null) {
                    dataSource.database().executeInsert(
                        "INSERT INTO `hub` VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        hub.serverName,
                        hub.worldName,
                        hub.x,
                        hub.y,
                        hub.z,
                        hub.pitch,
                        hub.yaw,
                        hub.createdAt.unwrapped
                    )
                    sender.send().success("Imported hub")
                } else {
                    sender.send().error("Hub not found")
                }
            }
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

                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
                    val createdAt = LocalDateTime.parse(it.created, formatter);

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

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> listOf("import")
            args.size == 1 -> listOf("hub", "banned-ips")
            else -> null
        }
    }
}
