package com.projectcitybuild.plugin.commands

import com.projectcitybuild.features.utilities.usecases.DataImportUseCase
import com.projectcitybuild.features.utilities.usecases.GetVersionUseCase
import com.projectcitybuild.features.utilities.usecases.ImportInventoriesUseCase
import com.projectcitybuild.features.utilities.usecases.ReloadPluginUseCase
import com.projectcitybuild.modules.scheduler.PlatformScheduler
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import com.projectcitybuild.plugin.exceptions.InvalidCommandArgumentsException
import org.bukkit.command.CommandSender
import javax.inject.Inject

class PCBridgeCommand @Inject constructor(
    private val getVersion: GetVersionUseCase,
    private val dataImport: DataImportUseCase,
    private val importInventories: ImportInventoriesUseCase,
    private val reloadPlugin: ReloadPluginUseCase,
    private val scheduler: PlatformScheduler,
) : SpigotCommand {

    override val label = "pcbridge"
    override val permission = "pcbridge.utilities"
    override val usageHelp = "/pcbridge"

    override suspend fun execute(input: SpigotCommandInput) {
        when {
            input.args.isEmpty() -> showVersion(input.sender)
            input.args.first() == "import" -> dataImport.execute(sender = input.player, args = input.args)
            input.args.first() == "import-inv" -> importInventories(input)
            input.args.first() == "reload" -> reloadPlugin(input)
            else -> throw InvalidCommandArgumentsException()
        }
    }

    private fun showVersion(sender: CommandSender) {
        val version = getVersion.execute()
        sender.send().info("Running PCBridge v${version.version} (${version.commitHash})")
    }

    private fun importInventories(input: SpigotCommandInput) {
        scheduler.async<Unit> {
            importInventories.execute(isDryRun = input.args.contains("--dry-run"))
        }.start()
    }

    private fun reloadPlugin(input: SpigotCommandInput) {
        reloadPlugin.execute()
        input.sender.send().success("Caches flushed")
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> listOf("import", "reload")
            args.size == 1 -> listOf("hub", "banned-ips")
            else -> null
        }
    }
}
