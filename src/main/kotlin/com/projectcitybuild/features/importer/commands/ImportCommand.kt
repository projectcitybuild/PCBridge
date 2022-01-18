package com.projectcitybuild.features.importer.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.modules.database.DataSource
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.old_modules.playerconfig.PlayerConfigFileStorage
import com.projectcitybuild.old_modules.storage.WarpFileStorage
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Plugin
import java.sql.Date
import javax.inject.Inject

class ImportCommand @Inject constructor(
    private val plugin: Plugin,
    private val dataSource: DataSource,
): BungeecordCommand {

    override val label = "importpcb"
    override val permission = "pcbridge.import"
    override val usageHelp = "/importpcb"

    override suspend fun execute(input: BungeecordCommandInput) {
        when {
            input.args.isEmpty() -> throw InvalidCommandArgumentsException()
            input.args.first() == "playerconfigs" -> importPlayerConfigFiles()
            input.args.first() == "warps" -> importWarpFiles()
            else -> throw InvalidCommandArgumentsException()
        }
        input.sender.send().success("Migration complete")
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> listOf("playerconfigs", "warps")
            else -> null
        }
    }

    private fun importPlayerConfigFiles() {
        val configStorage = PlayerConfigFileStorage(plugin.dataFolder.resolve("players"))
        runBlocking {
            configStorage.keys().forEach { fileName ->
                plugin.logger.info("Migrating player/$fileName.json to database")
                val config = configStorage.load(fileName)!!

                dataSource.database().executeInsert(
                    "INSERT INTO `players` VALUES(NULL, ?, ?, ?, ?);",
                    config.uuid.unwrapped.toString(),
                    config.isMuted,
                    config.isAllowingTPs,
                    Date(System.currentTimeMillis()),
                )
            }
        }
    }

    private fun importWarpFiles() {
        val warpStorage = WarpFileStorage(plugin.dataFolder.resolve("warps"))
        runBlocking {
            warpStorage.keys().forEach { fileName ->
                plugin.logger.info("Migrating warps/$fileName.json to database")
                val warp = warpStorage.load(fileName)!!

                dataSource.database().executeInsert(
                    "INSERT INTO `warps` VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);",
                    fileName,
                    warp.serverName,
                    warp.worldName,
                    warp.x,
                    warp.y,
                    warp.z,
                    warp.pitch,
                    warp.yaw,
                    Date(warp.createdAt.unwrapped.time),
                )
            }
        }
    }
}
