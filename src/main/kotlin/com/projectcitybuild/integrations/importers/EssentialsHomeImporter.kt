package com.projectcitybuild.integrations.importers

import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.repositories.HomeRepository
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class EssentialsHomeImporter @Inject constructor(
    private val plugin: Plugin,
    private val homeRepository: HomeRepository,
): PluginImporter {

    override fun run(): Boolean {
        val folder = File(plugin.dataFolder, "PCBridge")

        val files = folder.listFiles()
            ?.filter { it.extension == "yml" }
            ?.map { File(plugin.dataFolder, "PCBridge/${it.name}") }
            ?: emptyList()

        // TODO: get this from importer input
        var serverName = ""

        val now = LocalDateTime.now()

        files.forEach { file ->
            // Each player has a YML file named as their player UUID (with hyphens)
            val playerUUID = UUID.fromString(file.nameWithoutExtension)

            val config = ConfigurationProvider
                .getProvider(YamlConfiguration::class.java)
                .load(file)

            config
                .getList("homes") ?: emptyList<String>()
                .forEach { homeName ->
                    val _worldName = config.get("homes.$homeName.world-name")?.let { it as String }
                    val _world = config.get("homes.$homeName.world")?.let { it as String }

                    // `world` normally contains the world's name, however in the case where
                    // it's a UUID instead, `world-name` will have the real name
                    val worldName = if (_worldName.isNullOrEmpty()) _world else _worldName

                    if (worldName == null) {
                        // TODO: output warning
                        return@forEach
                    }

                    val x = config.get("homes.$homeName.x") as Double
                    val y = config.get("homes.$homeName.y") as Double
                    val z = config.get("homes.$homeName.z") as Double
                    val yaw = config.get("homes.$homeName.yaw") as Float
                    val pitch = config.get("homes.$homeName.pitch") as Float

                    val existingHome = homeRepository.exists(
                        name = homeName,
                        playerUUID = playerUUID
                    )
                    if (existingHome != null) {
                        // TODO: output warning
                        return@forEach
                    }
                    homeRepository.add(
                        playerUUID = playerUUID,
                        homeName = homeName,
                        location = CrossServerLocation(
                            serverName = serverName,
                            worldName = worldName,
                            x = x,
                            y = y,
                            z = z,
                            yaw = yaw,
                            pitch = pitch,
                        ),
                        createdAt = now,
                    )

                    // TODO: output success
                }
        }

        return true
    }
}