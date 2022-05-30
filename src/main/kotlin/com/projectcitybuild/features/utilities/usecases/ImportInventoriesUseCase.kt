package com.projectcitybuild.features.utilities.usecases

import br.com.gamemods.nbtmanipulator.NbtByte
import br.com.gamemods.nbtmanipulator.NbtCompound
import br.com.gamemods.nbtmanipulator.NbtDouble
import br.com.gamemods.nbtmanipulator.NbtFile
import br.com.gamemods.nbtmanipulator.NbtIO
import br.com.gamemods.nbtmanipulator.NbtInt
import br.com.gamemods.nbtmanipulator.NbtList
import br.com.gamemods.nbtmanipulator.NbtShort
import br.com.gamemods.nbtmanipulator.NbtString
import com.dumptruckman.bukkit.configuration.json.JsonConfiguration
import com.projectcitybuild.modules.logger.PlatformLogger
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.block.ShulkerBox
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ItemFrame
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BannerMeta
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.CrossbowMeta
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.Repairable
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.material.Colorable
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.json.JSONObject
import java.io.EOFException
import java.io.File
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.Base64
import java.util.Locale
import java.util.UUID
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
                    logger.fatal("Encountered unexpected EOF for $playerUUID.json: ${e.message}")
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
                            "hub", "big_city_2020", "creative_epsilon" -> dimension // Keep some world names lowercase
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
                        set("$world.stats.ma", "300") // We don't have any plugins that will increase air above the default 300
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
//                    set("playerData.lastWorld", profile.lastWorldName ?: "hub") // Default to `hub` if no world
                    set("playerData.lastWorld", "hub") // Set to `hub` because it will be the "default" world folder that has all player data now
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

                setEnchantments(stack, tags)

                setStoredEnchantments(stack, tags)

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
                // No idea why there's a few here... is this an old NBT?
                if (tags.containsKey("lore")) {
                    stack.itemMeta?.lore?.add(tags.getString("lore"))
                }

                setPotionMeta(stack, tags)

                setBookMeta(stack, tags)

                setSkullOwner(stack, tags)

                if (tags.containsKey("RepairCost")) {
                    val itemMeta = stack.itemMeta
                    if (itemMeta != null && itemMeta is Repairable) {
                        itemMeta.repairCost = tags.getInt("RepairCost")
                        stack.itemMeta = itemMeta
                    }
                }
                if (tags.containsKey("BlockEntityTag")) {
                    val blockEntities = tags.getCompound("BlockEntityTag")

                    setBanner(stack, blockEntities)

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

                    if (blockEntities.containsKey("CustomName")) {
                        val itemMeta = stack.itemMeta
                        itemMeta?.setDisplayName(blockEntities.getString("CustomName"))
                        stack.itemMeta = itemMeta
                    }

                    logIfNotExpected(
                        expected = listOf("Patterns", "Items", "id", "Base", "CustomName"),
                        compound = tags.getCompound("BlockEntityTag"),
                        name = "BlockEntityTag",
                    )
                }
                if (tags.containsKey("EntityTag")) {
                    val entityTags = tags.getCompound("EntityTag")

                    // For invisible item frames
                    if (entityTags.containsKey("Invisible")) {
                        val isInvisible = if (entityTags.get("Invisible") is NbtByte) {
                            entityTags.getByte("Invisible").toInt() == 1
                        } else {
                            entityTags.getInt("Invisible") == 1
                        }
                        if (stack is ItemFrame) {
                            stack.isVisible = isInvisible
                        }
                    }
                    logIfNotExpected(
                        expected = listOf("Invisible"),
                        compound = entityTags,
                        name = "EntityTag",
                    )
                }
                if (tags.containsKey("map")) {
                    val itemMeta = stack.itemMeta
                    if (itemMeta != null && itemMeta is MapMeta) {
                        itemMeta.mapId = tags.getInt("map")
                        stack.itemMeta = itemMeta
                    }
                }

                setProjectiles(stack, tags)

                setFireworks(stack, tags)

                setAttributeModifiers(stack, tags)

                if (tags.containsKey("Unbreakable") && tags.getBooleanByte("Unbreakable")) {
                    stack.itemMeta?.isUnbreakable = true
                }

                if (tags.containsKey("HideFlags")) {
                    // Normally this should be handled, but the majority of inventories I saw with this
                    // are maliciously using it to hide items they shouldn't have
                }

                logIfNotExpected(
                    expected = listOf(
                        "AttributeModifiers",
                        "BlockEntityTag",
                        "ChargedProjectiles",
                        "CustomPotionEffects",
                        "CustomPotionColor",
                        "Enchantments",
                        "EntityTag",
                        "Fireworks",
                        "HideFlags",
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
                        "resolved", // Not needed
                        "lore",
                        "Unbreakable",
                        "StoredEnchantments",
                        "Charged" // Ignore
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

    private fun setPotionMeta(stack: ItemStack, tags: NbtCompound) {
        if (! tags.containsKey("CustomPotionEffects")) {
            return
        }
        val itemMeta = stack.itemMeta
        if (itemMeta == null || itemMeta !is PotionMeta) {
            return
        }
        tags.getCompoundList("CustomPotionEffects").forEach {
            val isAmbient = if (it.containsKey("Ambient")) it.getBooleanByte("Ambient") else false
            val amplifier = if (it.containsKey("Amplifier")) it.getBooleanByte("Amplifier") else false
            val duration = it.getInt("Duration")
            val id = if (it.get("Id") is NbtByte) it.getByte("Id").toInt() else it.getInt("Id")
            val shouldShowIcon = if (it.containsKey("ShowIcon")) it.getBooleanByte("ShowIcon") else false
            val shouldShowParticles = if (it.containsKey("ShowParticles")) it.getBooleanByte("ShowParticles") else false

            val overwrite = true // Overwrite what...?

            val potionType = PotionEffectType.getById(id)
            if (potionType == null) {
                logger.fatal("Could not map PotionEffectType: $id")
                return@forEach
            }
            val effect = PotionEffect(
                potionType,
                duration,
                if (amplifier) 1 else 0,
                isAmbient,
                shouldShowParticles,
                shouldShowIcon,
            )
            itemMeta.addCustomEffect(effect, overwrite)
        }
        if (tags.containsKey("CustomPotionColor")) {
            val rawColor = tags.getInt("CustomPotionColor")
            itemMeta.color = Color.fromRGB(rawColor)
        }
        stack.itemMeta = itemMeta
    }

    private fun setEnchantments(stack: ItemStack, tags: NbtCompound) {
        if (! tags.containsKey("Enchantments")) {
            return
        }
        tags.getCompoundList("Enchantments").forEach { enchantmentCompound ->
            val id = enchantmentCompound.getString("id")

            // Some shulker boxes contain items with an NbtInt instead of an NbtShort...
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

                // Also ignore items that have had hacked-in enchantments applied
                if (e.message != "Specified enchantment cannot be applied to this itemstack") {
                    logger.fatal("Could not map enchantment [$id]: ${e.message}")
                }
            }
        }
    }

    private fun setStoredEnchantments(stack: ItemStack, tags: NbtCompound) {
        if (! tags.containsKey("StoredEnchantments")) {
            return
        }
        val itemMeta = stack.itemMeta
        if (itemMeta == null || itemMeta !is EnchantmentStorageMeta) {
            return
        }
        tags.getCompoundList("StoredEnchantments").forEach {
            val id = it.getString("id")
            val level = it.getShort("lvl").toInt()

            val key = NamespacedKey.minecraft(id.removePrefix("minecraft:"))
            val mappedEnchant = Enchantment.getByKey(key)
            if (mappedEnchant != null) {
                val ignoresLevelRestriction = false
                itemMeta.addStoredEnchant(mappedEnchant, level, ignoresLevelRestriction)
            }
        }
        stack.itemMeta = itemMeta
    }

    private fun setBookMeta(stack: ItemStack, tags: NbtCompound) {
        val itemMeta = stack.itemMeta
        if (itemMeta == null || itemMeta !is BookMeta) {
            return
        }
        if (tags.containsKey("pages")) {
            tags.getStringList("pages").forEach {
                try {
                    // Attempt to parse as JSON
                    itemMeta.spigot().addPage(ComponentSerializer.parse(it.value))
                } catch (e: Exception) {
                    // ... but not all pages are JSON
                    itemMeta.addPage(it.value)
                }
            }
        }
        if (tags.containsKey("author")) {
            itemMeta.author = tags.getString("author")
            stack.itemMeta = itemMeta
        }
        if (tags.containsKey("title")) {
            itemMeta.title = tags.getString("title")
            stack.itemMeta = itemMeta
        }
        if (tags.containsKey("generation")) {
            itemMeta.generation = when (tags.getInt("generation")) {
                0 -> BookMeta.Generation.ORIGINAL
                1 -> BookMeta.Generation.COPY_OF_ORIGINAL
                2 -> BookMeta.Generation.COPY_OF_COPY
                3 -> BookMeta.Generation.TATTERED
                else -> BookMeta.Generation.COPY_OF_ORIGINAL // Just to be safe
            }
        }
        stack.itemMeta = itemMeta
    }

    private fun setBanner(stack: ItemStack, blockEntities: NbtCompound) {
        val itemMeta = stack.itemMeta
        if (itemMeta == null || itemMeta !is BannerMeta) {
            return
        }
        if (blockEntities.containsKey("Base")) {
            val rawColor = blockEntities.getInt("Base")
            itemMeta.baseColor = DyeColor.getByDyeData(rawColor.toByte())
        }
        if (blockEntities.containsKey("Patterns")) {
            blockEntities.getCompoundList("Patterns").forEach {
                val rawColor = it.getInt("Color")
                val rawPattern = it.getString("Pattern")

                val color = DyeColor.getByDyeData(rawColor.toByte())
                val patternType = PatternType.getByIdentifier(rawPattern)

                if (color != null && patternType != null) {
                    itemMeta.addPattern(Pattern(color, patternType))
                } else {
                    logger.fatal("Pattern was missing data")
                    logger.fatal("color=$color")
                    logger.fatal("pattern=$patternType")
                    logger.fatal("rawColor=$rawColor")
                    logger.fatal("rawPattern=$rawPattern")
                }
            }
        }
        stack.itemMeta = itemMeta
    }

    private fun setSkullOwner(stack: ItemStack, tags: NbtCompound) {
        if (! tags.containsKey("SkullOwner")) {
            return
        }
        val itemMeta = stack.itemMeta
        if (itemMeta == null || itemMeta !is SkullMeta) {
            return
        }

        val skullOwner = tags.getCompound("SkullOwner")
        if (! skullOwner.containsKey("Properties")) {
            return
        }
        val properties = skullOwner.getCompound("Properties")

        if (properties.containsKey("Name")) {
            val name = properties.getString("Name")
            val offlinePlayer = plugin.server.getOfflinePlayer(name)
            itemMeta.owningPlayer = offlinePlayer
            stack.itemMeta = itemMeta
            return
        }

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
        // }
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

                if (json.has("profileName")) {
                    val name = json.getString("profileName")
                    itemMeta.setOwner(name)
                    stack.itemMeta = itemMeta
                } else {
                    // We're out of options. Set the damn thing manually.
                    val textureURL = json.getJSONObject("textures").getJSONObject("SKIN").getString("url")
                    val encodedURL = Base64.getEncoder().encodeToString(textureURL.toByteArray())
                    val hashAsId = UUID(encodedURL.hashCode().toLong(), encodedURL.hashCode().toLong())
                    Bukkit.getUnsafe().modifyItemStack(stack, "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + encodedURL + "\"}]}}}")
                }
            } catch (e: Exception) {
                logger.fatal("Failed to decode texture: ${e.message}")
            }
        } else {
            logger.fatal("Could not determine owner of Skull")
        }
    }

    private fun setFireworks(stack: ItemStack, tags: NbtCompound) {
        if (! tags.containsKey("Fireworks")) {
            return
        }
        val itemMeta = stack.itemMeta
        if (itemMeta == null || itemMeta !is FireworkMeta) {
            return
        }
        val fireworks = tags.getCompound("Fireworks")
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

    private fun setProjectiles(stack: ItemStack, tags: NbtCompound) {
        val itemMeta = stack.itemMeta
        if (itemMeta == null || itemMeta !is CrossbowMeta) {
            return
        }
        if (tags.containsKey("ChargedProjectiles")) {
            val items: MutableList<ItemStack> = mutableListOf()
            tags.getCompoundList("ChargedProjectiles").forEach {
                val material = Material.matchMaterial(it.getString("id"))
                    ?: return@forEach

                val amount = it.getByte("Count").toInt()
                val newStack = ItemStack(material, amount)
                items.add(newStack)
            }
            itemMeta.setChargedProjectiles(items)
        }
        stack.itemMeta = itemMeta
    }

    private fun setAttributeModifiers(stack: ItemStack, tags: NbtCompound) {
        if (! tags.containsKey("AttributeModifiers")) {
            return
        }
        tags.getCompoundList("AttributeModifiers").forEach {
            val attribute = when (it.getString("AttributeName")) {
                "generic.max_health" -> Attribute.GENERIC_MAX_HEALTH
                "generic.follow_range" -> Attribute.GENERIC_FOLLOW_RANGE
                "generic.knockback_resistance" -> Attribute.GENERIC_KNOCKBACK_RESISTANCE
                "generic.movement_speed" -> Attribute.GENERIC_MOVEMENT_SPEED
                "generic.flying_speed" -> Attribute.GENERIC_FLYING_SPEED
                "generic.attack_damage" -> Attribute.GENERIC_ATTACK_DAMAGE
                "generic.attack_knockback" -> Attribute.GENERIC_ATTACK_KNOCKBACK
                "generic.attack_speed" -> Attribute.GENERIC_ATTACK_SPEED
                "generic.armor" -> Attribute.GENERIC_ARMOR
                "generic.armor_toughness" -> Attribute.GENERIC_ARMOR_TOUGHNESS
                "generic.luck" -> Attribute.GENERIC_LUCK
                "horse.jump_strength" -> Attribute.HORSE_JUMP_STRENGTH
                "zombie.spawn_reinforcements" -> Attribute.ZOMBIE_SPAWN_REINFORCEMENTS
                else -> return@forEach
            }
            var slot: EquipmentSlot? = null
            if (it.containsKey("Slot")) {
                slot = when (it.getString("Slot")) {
                    "mainhand" -> EquipmentSlot.HAND
                    "offhand" -> EquipmentSlot.OFF_HAND
                    "feet" -> EquipmentSlot.FEET
                    "legs" -> EquipmentSlot.LEGS
                    "chest" -> EquipmentSlot.CHEST
                    "head" -> EquipmentSlot.HEAD
                    else -> null
                }
            }

            val uuid = UUID.randomUUID() // Doesn't need to match
            val name = it.getString("Name")
            val amount = if (it.get("Amount") is NbtDouble) {
                it.getDouble("Amount")
            } else {
                it.getInt("Amount").toDouble()
            }
            val operation = when (it.getInt("Operation")) {
                0 -> AttributeModifier.Operation.ADD_NUMBER
                1 -> AttributeModifier.Operation.ADD_SCALAR
                2 -> AttributeModifier.Operation.MULTIPLY_SCALAR_1
                else -> return@forEach
            }

            val modifier = if (slot != null) {
                AttributeModifier(uuid, name, amount, operation, slot)
            } else {
                AttributeModifier(uuid, name, amount, operation)
            }
            stack.itemMeta = stack.itemMeta?.apply {
                addAttributeModifier(attribute, modifier)
            }
        }
    }
}
