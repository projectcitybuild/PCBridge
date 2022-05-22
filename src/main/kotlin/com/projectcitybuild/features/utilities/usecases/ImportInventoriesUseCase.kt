package com.projectcitybuild.features.utilities.usecases

import br.com.gamemods.nbtmanipulator.NbtCompound
import br.com.gamemods.nbtmanipulator.NbtFile
import br.com.gamemods.nbtmanipulator.NbtIO
import br.com.gamemods.nbtmanipulator.NbtList
import com.dumptruckman.bukkit.configuration.json.JsonConfiguration
import com.projectcitybuild.modules.logger.PlatformLogger
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.Repairable
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.Plugin
import java.io.EOFException
import java.io.File
import javax.inject.Inject

// Warning: There be dragons ahead...
class ImportInventoriesUseCase @Inject constructor(
    private val plugin: Plugin,
    private val logger: PlatformLogger,
) {
    private val inventoriesFolder = File(plugin.dataFolder, "inventories")

    fun execute() {
        val worlds = arrayOf("creative", "survival")

        worlds.forEach { worldFolderName ->
            val existingInventoryFolder = File(inventoriesFolder, worldFolderName)

            val outputFolder = File(inventoriesFolder, "multiverse/$worldFolderName")
            if (! outputFolder.exists()) {
                outputFolder.mkdirs()
            }

            existingInventoryFolder.listFiles { file -> file.extension == "dat" }?.forEach { file ->
                logger.info("Importing [$worldFolderName]: ${file.name}")

                val playerUUID = file.nameWithoutExtension
                val input: NbtFile = try {
                    NbtIO.readNbtFile(file)
                } catch (e: EOFException) {
                    logger.fatal("Encountered unexpected EOF for $playerUUID.json")
                    return@forEach
                }

                val health = input.compound.getFloat("Health")
                val foodExhaustion = input.compound.getFloat("foodExhaustionLevel")
                val foodSaturation = input.compound.getFloat("foodSaturationLevel")
                val foodLevel = input.compound.getInt("foodLevel")
                val air = input.compound.getShort("Air")
                val playerLevel = input.compound.getInt("XpLevel")
                val xpPercentToNextLevel = input.compound.getFloat("XpP")
                val totalXP = input.compound.getInt("XpTotal")
                val fallDistance = input.compound.getFloat("FallDistance")
                val inventory = input.compound.getCompoundList("Inventory")
                val enderItems = input.compound.getCompoundList("EnderItems")

                // Multiverse serializes by player name...
                // Apparently if a player changes their username, Multiverse Inventory will update this to the correct
                // name when they join the server
                val lastKnownName = input.compound.getCompound("bukkit").getString("lastKnownName")
                if (lastKnownName.isEmpty()) {
                    logger.fatal("No last known name for $playerUUID")
                    return@forEach
                }
                val playerWorldFile = File(outputFolder, "$lastKnownName.json").also {
                    if (it.exists()) {
                        it.delete()
                    }
                    it.parentFile.mkdirs()
                    it.createNewFile()
                }

                // TODO: figure out what "Max Air" actually maps to
                if (air.toString() != "300") {
                    logger.fatal("Air was $air. Expected 300")
                }

                playerWorldFile.let { configFile ->
                    JsonConfiguration.loadConfiguration(configFile).apply {
                        val world = "SURVIVAL"
                        inventoryMap(enderItems).entries.forEach { pair ->
                            set("$world.enderChestContents.${pair.key}", pair.value)
                        }
                        set("$world.potions", emptyList<String>()) // Too much work to keep potion effects

                        val armorSlots = arrayOf("100", "101", "102", "103")
                        inventoryMap(inventory).entries.forEach { pair ->
                            if (armorSlots.contains(pair.key)) {
                                // Specific slots are hardcoded for armor slots
                                set("$world.armorContents.${pair.key}", pair.value)
                            } else if (pair.key == "-106") {
                                // -106 is hardcoded for an off-hand item (eg. shield)
                                set("$world.offHandItem", pair.value)
                            } else {
                                set("$world.inventoryContents.${pair.key}", pair.value)
                            }
                        }
                        set("$world.stats.ex", foodExhaustion.toString())
                        set("$world.stats.ma", air.toString())
                        set("$world.stats.fl", foodLevel.toString())
                        set("$world.stats.el", playerLevel.toString())
                        set("$world.stats.hp", health.toString())
                        set("$world.stats.xp", xpPercentToNextLevel.toString())
                        set("$world.stats.txp", totalXP.toString())
                        set("$world.stats.ft", "-20")
                        set("$world.stats.fd", fallDistance.toString())
                        set("$world.stats.sa", foodSaturation.toString())
                        set("$world.stats.ra", "300")

                        save(configFile)
                    }
                }
            }
        }

//        val globalPlayerFile = File(inventoriesFolder, "multiverse/players/$playerUUID.json").also {
//            if (it.exists()) {
//                it.delete()
//            }
//            it.parentFile.mkdirs()
//            it.createNewFile()
//        }
//        globalPlayerFile.let { configFile ->
//            JsonConfiguration.loadConfiguration(configFile).apply {
//                set("playerData.lastWorld", "") // TODO
//                set("playerData.shouldLoad", false) // TODO: should this be true...?
//                set("playerData.lastKnownName", "") // TODO
//
//                save(configFile)
//            }
//        }

        logger.info("Completed import")
    }

    private fun inventoryMap(inventoryCompound: NbtList<NbtCompound>): HashMap<String, ItemStack> {
        val inventoryMap = hashMapOf<String, ItemStack>()

        inventoryCompound.forEach { compound ->
            val material = Material.matchMaterial(compound.getString("id"))
                ?: return@forEach

            val amount = compound.getByte("Count")
            val stack = ItemStack(material, amount.toInt())
            val slot = compound.getByte("Slot")

            if (compound.containsKey("tag")) {
                val tags = compound.getCompound("tag")

                if (tags.containsKey("Enchantments")) {
                    val enchantments = tags.getCompoundList("Enchantments")
                    enchantments.forEach { enchantmentCompound ->
                        val id = enchantmentCompound.getString("id")
                        val level = enchantmentCompound.getShort("lvl")

                        // TODO: why isn't this working?
                        Enchantment.getByName(id)?.let {
                            stack.addEnchantment(it, level.toInt())
                        }
                    }
                }
                if (tags.containsKey("Damage")) {
                    val itemMeta = stack.itemMeta
                    if (itemMeta != null && itemMeta is Damageable) {
                        val damage = tags.getInt("Damage")
                        itemMeta.damage = damage
                        stack.itemMeta = itemMeta
                    }
                }
                if (tags.containsKey("display")) {
                    val display = tags.getCompound("display")
                    if (display.containsKey("Name")) {
                        stack.itemMeta?.setDisplayName(display.getString("Name"))
                    }
                    if (display.containsKey("Lore")) {
                        // TODO
                    }
                    logIfNotExpected(
                        expected = listOf("Name", "Lore"),
                        compound = display,
                        name = "display",
                    )
                }
                if (tags.containsKey("SkullOwner")) {
                    val skullOwner = tags.getCompound("SkullOwner")
                    val properties = skullOwner.getCompound("Properties")

                    val itemMeta = stack.itemMeta
                    if (itemMeta != null && itemMeta is SkullMeta) {
                        if (properties.containsKey("Name")) {
                            itemMeta.setOwner(tags.getCompound("Properties").getString("Name"))
                            stack.itemMeta = itemMeta
                        } else {
                            logger.fatal("No Name found for skull")
                        }
                    }
                }
                if (tags.containsKey("RepairCost")) {
                    val itemMeta = stack.itemMeta
                    if (itemMeta != null && itemMeta is Repairable) {
                        itemMeta.repairCost = tags.getInt("RepairCost")
                        stack.itemMeta = itemMeta
                    }
                }
                if (tags.containsKey("BlockEntityTag")) {
                    // TODO (shulker box, etc)
                    // Use `f9b51cf5-a34e-440c-b4ab-4d497ef1105f` survival
                }

                logIfNotExpected(
                    expected = listOf("Enchantments", "Damage", "display", "SkullOwner", "RepairCost"),
                    compound = tags,
                    name = "tag",
                )
            }
            inventoryMap.put(key = slot.toString(), value = stack)
        }

        return inventoryMap
    }

    private fun logIfNotExpected(expected: List<String>, compound: NbtCompound, name: String) {
        compound.keys
            .filter { !expected.contains(it) }
            .forEach { logger.fatal("Unexpected $name: $it") }
    }

    object Multiverse {
        const val PLAYER_INVENTORY_CONTENTS = "inventoryContents"
        const val PLAYER_OFF_HAND_ITEM = "offHandItem"
        const val ENDER_CHEST_CONTENTS = "enderChestContents"
        const val PLAYER_BED_SPAWN_LOCATION = "bedSpawnLocation"
        const val PLAYER_LAST_LOCATION = "lastLocation"
        const val PLAYER_LAST_WORLD = "lastWorld"
        const val PLAYER_SHOULD_LOAD = "shouldLoad"
        const val PLAYER_LAST_KNOWN_NAME = "lastKnownName"
        const val PLAYER_PROFILE_TYPE = "profileType"
        const val PLAYER_HEALTH = "hp"
        const val PLAYER_EXPERIENCE = "xp"
        const val PLAYER_TOTAL_EXPERIENCE = "txp"
        const val PLAYER_LEVEL = "el"
        const val PLAYER_FOOD_LEVEL = "fl"
        const val PLAYER_EXHAUSTION = "ex"
        const val PLAYER_SATURATION = "sa"
        const val PLAYER_FALL_DISTANCE = "fd"
        const val PLAYER_FIRE_TICKS = "ft"
        const val PLAYER_REMAINING_AIR = "ra"
        const val PLAYER_MAX_AIR = "ma"
        const val LOCATION_X = "x"
        const val LOCATION_Y = "y"
        const val LOCATION_Z = "z"
        const val LOCATION_WORLD = "wo"
        const val LOCATION_PITCH = "pi"
        const val LOCATION_YAW = "ya"
        const val POTION_TYPE = "pt"
        const val POTION_DURATION = "pd"
        const val POTION_AMPLIFIER = "pa"
    }
}