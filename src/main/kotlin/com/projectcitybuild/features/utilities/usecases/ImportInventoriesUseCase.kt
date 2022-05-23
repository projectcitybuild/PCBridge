package com.projectcitybuild.features.utilities.usecases

import br.com.gamemods.nbtmanipulator.NbtCompound
import br.com.gamemods.nbtmanipulator.NbtFile
import br.com.gamemods.nbtmanipulator.NbtIO
import br.com.gamemods.nbtmanipulator.NbtInt
import br.com.gamemods.nbtmanipulator.NbtList
import br.com.gamemods.nbtmanipulator.NbtShort
import br.com.gamemods.nbtmanipulator.NbtString
import com.dumptruckman.bukkit.configuration.json.JsonConfiguration
import com.projectcitybuild.modules.logger.PlatformLogger
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.ShulkerBox
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BannerMeta
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.inventory.meta.Repairable
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.material.Colorable
import org.bukkit.plugin.Plugin
import org.json.JSONObject
import java.io.EOFException
import java.io.File
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.Base64
import java.util.Locale
import javax.inject.Inject

//           ,     \    /      ,
//          / \    )\__/(     / \
//         /   \  (_\  /_)   /   \
//    ____/_____\__\@  @/___/_____\____
//   |             |\../|              |
//   |              \VV/               |
//   |                                 |
//   |             WARNING             |
//   |     There be dragons ahead...   |
//   |_________________________________|
//    |    /\ /      \\       \ /\    |
//    |  /   V        ))       V   \  |
//    |/     `       //        '     \|
//    `              V                '
//
class ImportInventoriesUseCase @Inject constructor(
    private val plugin: Plugin,
    private val logger: PlatformLogger,
) {
    private val inventoriesFolder = File(plugin.dataFolder, "inventories")
    private val multiverseInvFolder = File(plugin.dataFolder, "../Multiverse-Inventories")

    data class GlobalProfile(
        var uuid: String,
        var lastSeen: Long,
        var lastKnownName: String,
        var lastWorldName: String?,
    )

    fun execute(isDryRun: Boolean) {
        val profiles: MutableMap<String, GlobalProfile> = mutableMapOf()
        val worlds = arrayOf("creative", "survival")

        worlds.forEach { worldFolderName ->
            val existingInventoryFolder = File(inventoriesFolder, worldFolderName)

            val outputGroupFolder = File(multiverseInvFolder, "groups/$worldFolderName")
            if (! outputGroupFolder.exists()) {
                outputGroupFolder.mkdirs()
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

                val root = input.compound

                val health = root.getFloat("Health")
                val foodExhaustion = root.getFloat("foodExhaustionLevel")
                val foodSaturation = root.getFloat("foodSaturationLevel")
                val foodLevel = root.getInt("foodLevel")
                val air = root.getShort("Air")
                val playerLevel = root.getInt("XpLevel")
                val xpPercentToNextLevel = root.getFloat("XpP")
                val totalXP = root.getInt("XpTotal")
                val fallDistance = root.getFloat("FallDistance")
                val inventory = root.getCompoundList("Inventory")
                val enderItems = root.getCompoundList("EnderItems")

                val bukkit = root.getCompound("bukkit")
                val lastKnownName = bukkit.getString("lastKnownName")
                val lastPlayed = bukkit.getLong("lastPlayed")

                // If a player has a Dimension, they're in a world that we know
                var lastWorldName: String? = null
                if (root.containsKey("Dimension")) {
                    // For some reason, some players have an NbtInt Dimension
                    if (root.get("Dimension") is NbtString) {
                        val dimension = root.getString("Dimension")
                            .removePrefix("minecraft:")

                        lastWorldName = when (dimension) {
                            "overworld" -> "Survival"
                            "the_nether" -> "Survival_nether"
                            "hub", "big_city_2020", "creative_epsilon" -> dimension  // Keep some world names lowercase
                            else -> dimension.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        }
                    }
                }

                // Multiverse serializes by player name...
                // Apparently if a player changes their username, Multiverse Inventory will update this to the correct
                // name when they join the server
                if (lastKnownName.isEmpty()) {
                    logger.fatal("No last known name for $playerUUID")
                    return@forEach
                }
                val playerWorldFile = File(outputGroupFolder, "$lastKnownName.json").also {
                    if (isDryRun) {
                        if (! it.exists()) {
                            it.parentFile.mkdirs()
                            it.createNewFile()
                        }
                    } else {
                        if (it.exists()) {
                            it.delete()
                        }
                        it.parentFile.mkdirs()
                        it.createNewFile()
                    }
                }

                playerWorldFile.let { configFile ->
                    JsonConfiguration.loadConfiguration(configFile).apply {
                        val world = "SURVIVAL"
                        inventoryMap(enderItems).entries.forEach { pair ->
                            set("$world.enderChestContents.${pair.key}", pair.value)
                        }
                        set("$world.potions", emptyList<String>()) // Too much work to keep potion effects

                        val armorSlots = arrayOf("100", "101", "102", "103")
                        val inv = inventoryMap(inventory)
                        inv.entries.forEach { pair ->
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

                        // Multiverse Inventories requires an off-hand item
                        if (! inv.keys.contains("-106")) {
                            set("$world.offHandItem", ItemStack(Material.AIR))
                        }

                        set("$world.stats.ex", foodExhaustion.toString())
                        set("$world.stats.ma", "300")   // We don't have any plugins that will increase air above the default 300
                        set("$world.stats.fl", foodLevel.toString())
                        set("$world.stats.el", playerLevel.toString())
                        set("$world.stats.hp", health.toString())
                        set("$world.stats.xp", xpPercentToNextLevel.toString())
                        set("$world.stats.txp", totalXP.toString())
                        set("$world.stats.ft", "-20")
                        set("$world.stats.fd", fallDistance.toString())
                        set("$world.stats.sa", foodSaturation.toString())
                        set("$world.stats.ra", air.toString())

                        if (!isDryRun) {
                            save(configFile)
                        }
                    }
                }

                val existingProfile = profiles[playerUUID]
                if (existingProfile == null) {
                    profiles[playerUUID] = GlobalProfile(
                        uuid = playerUUID,
                        lastKnownName = lastKnownName,
                        lastWorldName = lastWorldName,
                        lastSeen = lastPlayed,
                    )
                } else {
                    if (existingProfile.lastSeen < lastPlayed) {
                        existingProfile.lastKnownName = lastKnownName
                        existingProfile.lastSeen = lastPlayed
                        existingProfile.lastWorldName = lastWorldName
                    }
                }
            }
        }

        profiles.entries.forEach { pair ->
            val uuid = pair.key
            val profile = pair.value

            val globalPlayerFile = File(multiverseInvFolder, "players/$uuid.json").also {
                if (isDryRun) {
                    if (! it.exists()) {
                        it.parentFile.mkdirs()
                        it.createNewFile()
                    }
                } else {
                    if (it.exists()) {
                        it.delete()
                    }
                    it.parentFile.mkdirs()
                    it.createNewFile()
                }
            }
            globalPlayerFile.let { configFile ->
                JsonConfiguration.loadConfiguration(configFile).apply {
                    set("playerData.lastWorld", profile.lastWorldName ?: "hub")  // Default to `hub` if no world
                    set("playerData.shouldLoad", true) // TODO: should this be false...?
                    set("playerData.lastKnownName", profile.lastKnownName)

                    if (!isDryRun) {
                        save(configFile)
                    }
                }
            }
        }

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

                        // Some shulker boxes have an NbtInt instead of an NbtShort...
                        val unsafeLevel = if (enchantmentCompound.get("lvl") is NbtShort) {
                            enchantmentCompound.getShort("lvl").toInt()
                        } else if (enchantmentCompound.get("lvl") is NbtInt) {
                            enchantmentCompound.getInt("lvl")
                        } else {
                            return@forEach
                        }

                        try {
                            val key = NamespacedKey.minecraft(id.removePrefix("minecraft:"))
                            val mappedEnchant = Enchantment.getByKey(key)
                            if (mappedEnchant != null) {
                                // For some reason some people have enchantments with levels outside the allowed bounds.
                                // It would cause an IllegalArgumentException
                                val level = unsafeLevel
                                    .let { min(it, mappedEnchant.maxLevel) }
                                    .let { max(it, 0) }

                                stack.addEnchantment(mappedEnchant, level)
                            }
                        } catch (e: IllegalArgumentException) {
                            // Just throw out the enchantment if something goes wrong.
                            // There's some enchantments that aren't allowed to be applied to the
                            // given ItemStack, like imageonmap:___gloweffect___
                            logger.fatal("Could not map enchantment [$id]: ${e.message}")
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
                    if (display.containsKey("color")) {
                        val itemMeta = stack.itemMeta
                        if (itemMeta != null && itemMeta is Colorable) {
                            val rgbValue = display.getInt("color")
                            val color = Color.fromRGB(rgbValue)
                            itemMeta.color = DyeColor.getByColor(color)
                            stack.itemMeta = itemMeta
                        }
                    }
                    if (display.containsKey("Lore")) {
                        stack.itemMeta?.lore = display.getStringList("Lore").map { it.value }
                    }
                    if (display.containsKey("MapColor")) {
                        val itemMeta = stack.itemMeta
                        if (itemMeta != null && itemMeta is MapMeta) {
                            itemMeta.color = Color.fromRGB(display.getInt("MapColor"))
                            stack.itemMeta = itemMeta
                        }
                    }
                    logIfNotExpected(
                        expected = listOf("Name", "color", "Lore", "MapColor"),
                        compound = display,
                        name = "display",
                    )
                }
                if (tags.containsKey("pages")) {
                    val pages = tags.getStringList("pages")
                    val itemMeta = stack.itemMeta
                    if (itemMeta != null && itemMeta is BookMeta) {
                        pages.forEach {
                            try {
                                // Attempt to parse as JSON
                                itemMeta.spigot().addPage(ComponentSerializer.parse(it.value))
                            } catch (e: Exception) {
                                // ... but not all pages are JSON
                                itemMeta.addPage(it.value)
                            }
                        }
                        stack.itemMeta = itemMeta
                    }
                }
                if (tags.containsKey("author")) {
                    val itemMeta = stack.itemMeta
                    if (itemMeta != null && itemMeta is BookMeta) {
                        itemMeta.author = tags.getString("author")
                        stack.itemMeta = itemMeta
                    }
                }
                if (tags.containsKey("title")) {
                    val itemMeta = stack.itemMeta
                    if (itemMeta != null && itemMeta is BookMeta) {
                        itemMeta.title = tags.getString("title")
                        stack.itemMeta = itemMeta
                    }
                }
                if (tags.containsKey("generation")) {
                    val itemMeta = stack.itemMeta
                    if (itemMeta != null && itemMeta is BookMeta) {
                        itemMeta.generation = when(tags.getInt("generation")) {
                            0 -> BookMeta.Generation.ORIGINAL
                            1 -> BookMeta.Generation.COPY_OF_ORIGINAL
                            2 -> BookMeta.Generation.COPY_OF_COPY
                            3 -> BookMeta.Generation.TATTERED
                            else -> BookMeta.Generation.COPY_OF_ORIGINAL  // Just to be safe
                        }
                        stack.itemMeta = itemMeta
                    }
                }
                if (tags.containsKey("SkullOwner")) {
                    val skullOwner = tags.getCompound("SkullOwner")

                    if (skullOwner.containsKey("Properties")) {
                        val properties = skullOwner.getCompound("Properties")

                        val itemMeta = stack.itemMeta
                        if (itemMeta != null && itemMeta is SkullMeta) {
                            if (properties.containsKey("Name")) {
                                val name = properties.getString("Name")
                                val offlinePlayer = plugin.server.getOfflinePlayer(name)
                                itemMeta.setOwningPlayer(offlinePlayer)
//                                itemMeta.setOwner(name)
                                stack.itemMeta = itemMeta
                            } else {
                                // We can try base64 decode the `Value`, which will (hopefully) contain the
                                // original URL and profileName, like this:
                                // {
                                //  "timestamp" : 1640581383074,
                                //  "profileId" : "d37fd212aad64aeb888764d615dbaea3",
                                //  "profileName" : "Dicodaspace",
                                //  "signatureRequired" : true,
                                //  "textures" : {
                                //    "SKIN" : {
                                //      "url" : "http://textures.minecraft.net/texture/7ee5fbf3ef15bd5287c556014de1a77f8d082f9ea11aa03d2834175f74e9c2a2"
                                //    }
                                //  }
                                //}
                                val textures = properties.getCompoundList("textures")
                                if (textures.size > 1) {
                                    logger.fatal("More than 1 texture. Is this intended?")
                                }
                                val texture = textures.firstOrNull()
                                if (texture != null) {
                                    try {
                                        val encoded = texture.getString("Value")
                                        val decoded = String(Base64.getUrlDecoder().decode(encoded))
                                        val json = JSONObject(decoded)
                                        val name = json.getString("profileName")

                                        itemMeta.setOwner(name)
                                        stack.itemMeta = itemMeta
                                    } catch (e: Exception) {
                                        logger.fatal("Failed to decode texture: ${e.message}")
                                    }
                                } else {
                                    logger.fatal("Could not determine owner of Skull")
                                }
                            }
                        }
                    } else {
                        logger.fatal("Properties key missing from SkullOwner")
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
                    val blockEntities = tags.getCompound("BlockEntityTag")

                    if (blockEntities.containsKey("Patterns")) {
                        val patterns = blockEntities.getCompoundList("Patterns")
                        val itemMeta = stack.itemMeta

                        if (itemMeta != null && itemMeta is BannerMeta) {
                            patterns.forEach {
                                val rawColor = it.getInt("Color")
                                val rawPattern = it.getString("Pattern")

                                val color = DyeColor.getByDyeData(rawColor.toByte())
                                val patternType = PatternType.getByIdentifier(rawPattern)

                                if (color != null && patternType != null) {
                                    itemMeta.patterns.add(Pattern(color, patternType))
                                }
                            }
                        }
                        stack.itemMeta = itemMeta
                    }

                    if (blockEntities.containsKey("Items")) {
                        val items = blockEntities.getCompoundList("Items")
                        val nestedInventory = inventoryMap(items)

                        val itemMeta = stack.itemMeta
                        if (itemMeta != null && itemMeta is BlockStateMeta) {
                            val blockState = itemMeta.blockState

                            if (blockState is ShulkerBox) {
                                nestedInventory.entries.forEach { pair ->
                                    val slotInShulker = pair.key.toInt()
                                    val item = pair.value
                                    blockState.inventory.setItem(slotInShulker, item)
                                }
                            }
                            itemMeta.blockState = blockState
                        }
                        stack.itemMeta = itemMeta
                    }

                    logIfNotExpected(
                        expected = listOf("Patterns", "Items", "id"),
                        compound = tags.getCompound("BlockEntityTag"),
                        name = "BlockEntityTag",
                    )
                }
                if (tags.containsKey("map")) {
                    val itemMeta = stack.itemMeta
                    if (itemMeta != null && itemMeta is MapMeta) {
                        itemMeta.mapId = tags.getInt("map")
                        stack.itemMeta = itemMeta
                    }
                }
                if (tags.containsKey("Fireworks")) {
                    val fireworks = tags.getCompound("Fireworks")

                    val itemMeta = stack.itemMeta
                    if (itemMeta != null && itemMeta is FireworkMeta) {
                        if (fireworks.containsKey("Flight")) {
                            itemMeta.power = fireworks.getByte("Flight").toInt()
                        }
                        if (fireworks.containsKey("Explosions")) {
                            fireworks.getCompoundList("Explosions").forEach { explosion ->
                                val builder = FireworkEffect.builder()
                                if (explosion.containsKey("Flicker") && explosion.getBooleanByte("Flicker")) {
                                    builder.withFlicker()
                                }
                                if (explosion.containsKey("Trail") && explosion.getBooleanByte("Trail")) {
                                    builder.withTrail()
                                }
                                if (explosion.containsKey("Type")) {
                                    builder.with(
                                        when (explosion.getByte("Type").toInt()) {
                                            0 -> FireworkEffect.Type.BALL
                                            1 -> FireworkEffect.Type.BALL_LARGE
                                            2 -> FireworkEffect.Type.STAR
                                            3 -> FireworkEffect.Type.CREEPER
                                            4 -> FireworkEffect.Type.BURST
                                            else -> FireworkEffect.Type.BALL
                                        }
                                    )
                                }
                                if (explosion.containsKey("Colors")) {
                                    explosion.getIntArray("Colors").forEach {
                                        builder.withColor(Color.fromRGB(it))
                                    }
                                }
                                if (explosion.containsKey("FadeColors")) {
                                    explosion.getIntArray("FadeColors").forEach {
                                        builder.withFade(Color.fromRGB(it))
                                    }
                                }

                                itemMeta.addEffect(builder.build())
                            }
                        }
                        stack.itemMeta = itemMeta
                    }
                }

                logIfNotExpected(
                    expected = listOf(
                        "BlockEntityTag",
                        "Enchantments",
                        "Fireworks",
                        "Damage",
                        "display",
                        "SkullOwner",
                        "RepairCost",
                        "map",
                        "Potion",
                        "pages",
                        "author",
                        "title",
                        "generation",
                        "resolved",  // Not needed
                    ),
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
}