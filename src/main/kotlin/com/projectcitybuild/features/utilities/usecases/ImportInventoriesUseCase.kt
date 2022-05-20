package com.projectcitybuild.features.utilities.usecases

import br.com.gamemods.nbtmanipulator.NbtCompound
import br.com.gamemods.nbtmanipulator.NbtIO
import br.com.gamemods.nbtmanipulator.NbtList
import com.google.gson.Gson
import com.projectcitybuild.modules.logger.PlatformLogger
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

class ImportInventoriesUseCase @Inject constructor(
    private val plugin: Plugin,
    private val logger: PlatformLogger,
) {
    private val inventoriesFolder = File(plugin.dataFolder, "inventories")

    private data class MultiverseWorldPlayer(
        val enderChestContents: HashMap<String, ItemStack>,
        val potions: List<String>,
        val armorContents: HashMap<String, ItemStack>,
        val inventoryContents: HashMap<String, ItemStack>,
        val offHandItem: ItemStack?,
        val stats: Stats
    )
    private data class Stats(
        val ex: String,     // food exhaustion
        val ma: String,     // air
        val fl: String,     // food level
        val el: String,     // player level
        val hp: String,     // health
        val xp: String,     // player experience
        val txp: String,    // player total experience
        val ft: String,     // fire ticks
        val fd: String,     // fall distance
        val sa: String,     // food saturation
        val ra: String,     // remaining air
    )

    fun execute() {
        val folder = File(inventoriesFolder, "creative")
        folder.listFiles { file ->file.extension == "dat" }?.forEach { file ->
            logger.info("Importing: ${file.name}")

            val playerUUID = file.nameWithoutExtension
            val input = NbtIO.readNbtFile(file)

            val pos = input.compound.getDoubleList("Pos")
            val x = pos[0]
            val y = pos[1]
            val z = pos[2]
            val health = input.compound.getFloat("Health")
            val foodExhaustion = input.compound.getFloat("foodExhaustionLevel")
            val foodSaturation = input.compound.getFloat("foodSaturationLevel")
            val air = input.compound.getShort("Air")
            val totalXP = input.compound.getInt("XpTotal")
            val fallDistance = input.compound.getFloat("FallDistance")
            val inventory = input.compound.getCompoundList("Inventory")
            val enderItems = input.compound.getCompoundList("EnderItems")

            val playerFile = File(inventoriesFolder, "multiverse/players/$playerUUID.json").also {
                if (it.exists()) {
                    it.delete()
                }
                it.parentFile.mkdirs()
            }

            // TODO: Multiverse serializes by player name...
            val lastKnownName = input.compound.getCompound("bukkit").getString("lastKnownName")
            val outputFile = File(plugin.dataFolder, "inventories/multiverse/creative/$playerUUID.json").also {
                if (it.exists()) {
                    it.delete()
                }
                it.parentFile.mkdirs()
            }

            val player = MultiverseWorldPlayer(
                enderChestContents = inventoryMap(enderItems),
                potions = emptyList(),
                armorContents = hashMapOf(),
                inventoryContents = inventoryMap(inventory),
                offHandItem = null,
                stats = Stats(
                    ex = foodExhaustion.toString(),
                    ma = air.toString(),
                    fl = "",
                    el = "",
                    hp = health.toString(),
                    xp = "",
                    txp = totalXP.toString(),
                    ft = "-20",
                    fd = fallDistance.toString(),
                    sa = foodSaturation.toString(),
                    ra = "300",
                )
            )

            val gson = Gson()
            FileWriter(outputFile).use {
                gson.toJson(hashMapOf(Pair("SURVIVAL", player)), it)
            }
        }
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
                    // TODO
                }

                logIfNotExpected(
                    expected = listOf("Enchantments", "Damage", "display", "SkullOwner"),
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