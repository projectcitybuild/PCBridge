package com.projectcitybuild.pcbridge.features.inventories.domain.services

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.projectcitybuild.pcbridge.features.inventories.InventoriesPermissions
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryCompatibilityMode
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryProfileLocation
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryProfileType
import com.projectcitybuild.pcbridge.features.inventories.domain.data.MultiverseInventoryConfig
import com.projectcitybuild.pcbridge.features.inventories.domain.data.MultiverseInventoryShares
import com.projectcitybuild.pcbridge.features.inventories.domain.data.ResolvedInventoryGroup
import com.projectcitybuild.pcbridge.features.inventories.domain.repositories.MultiverseInventoryRepository
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Statistic
import org.bukkit.attribute.Attribute
import org.bukkit.block.data.type.Bed
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import java.util.Base64
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MultiverseInventoryTransitionService(
    private val repository: MultiverseInventoryRepository,
    private val config: MultiverseInventoryConfig,
    private val mode: InventoryCompatibilityMode,
    private val multiverseInventoriesEnabled: () -> Boolean,
) {
    private val applyingStoredLocation = ConcurrentHashMap.newKeySet<UUID>()

    fun onWorldChanged(
        player: Player,
        fromWorld: String,
        toWorld: String,
    ) {
        if (player.uniqueId in applyingStoredLocation) return
        if (!requireActive()) return
        requirePrimaryThread()
        if (fromWorld == toWorld) return

        val profileType = profileType(player.gameMode)
        val sourceGroups = groupsFor(fromWorld)
        val destinationGroups = groupsFor(toWorld)
        val playerName = repository.profileName(player.uniqueId, player.name)
        val bypassWorld = bypassesWorld(player, fromWorld)
        val writes = mutableListOf<ProfileWrite>()
        val reads = mutableListOf<ProfileRead>()

        if (bypassWorld) {
            if (config.alwaysWriteWorldProfile) {
                writes += ProfileWrite(
                    worldProfile(fromWorld, playerName),
                    profileType,
                    worldProfileShares(sourceGroups),
                )
            }
        } else {
            val handledSource = linkedSetOf<String>()
            sourceGroups.forEach { group ->
                if (destinationGroups.none { it.name == group.name }) {
                    writes += ProfileWrite(
                        groupProfile(group, playerName),
                        profileType,
                        group.shares,
                    )
                }
                handledSource += group.shares
                handledSource += group.disabledShares
            }
            val sourceWorldShares = activeShares()
            val worldWrites =
                if (config.alwaysWriteWorldProfile) {
                    sourceWorldShares
                } else {
                    sourceWorldShares - handledSource
                }
            if (worldWrites.isNotEmpty()) {
                writes += ProfileWrite(worldProfile(fromWorld, playerName), profileType, worldWrites)
            }

            val handledDestination = linkedSetOf<String>()
            destinationGroups.forEach { group ->
                if (!bypassesGroup(player, group) && sourceGroups.none { it.name == group.name }) {
                    reads += ProfileRead(
                        groupProfile(group, playerName),
                        profileType,
                        sharesForRead(group.shares),
                    )
                }
                if (!bypassesGroup(player, group)) {
                    handledDestination += group.shares
                    handledDestination += group.disabledShares
                }
            }
            val destinationWorldShares = worldReadShares(destinationGroups) - handledDestination
            val worldReads = sharesForRead(destinationWorldShares)
            if (worldReads.isNotEmpty()) {
                reads += ProfileRead(worldProfile(toWorld, playerName), profileType, worldReads)
            }
        }

        rejectUnsupported(writes.flatMap { it.shares } + reads.flatMap { it.shares })
        persistWrites(player, writes)
        if (!bypassWorld) applyReads(player, reads)
        repository.updateGlobalPlayerData(
            player.uniqueId,
            mapOf("lastWorld" to JsonPrimitive(toWorld)),
        )
    }

    fun onGameModeChanged(
        player: Player,
        fromGameMode: GameMode,
        toGameMode: GameMode,
    ) {
        if (!requireActive() || !config.enableGamemodeShareHandling) return
        requirePrimaryThread()
        val fromType = profileType(fromGameMode)
        val toType = profileType(toGameMode)
        if (fromType == toType) return

        val world = player.world.name
        val groups = groupsFor(world)
        val playerName = repository.profileName(player.uniqueId, player.name)
        val worldShares = worldReadShares(groups)
        val reads = mutableListOf<ProfileRead>()
        val writes = mutableListOf<ProfileWrite>()
        val bypassWorld = bypassesWorld(player, world) || bypassesGameMode(player, toGameMode)
        val handled = linkedSetOf<String>()

        if (!bypassWorld) {
            groups.forEach { group ->
                writes += ProfileWrite(groupProfile(group, playerName), fromType, group.shares)
                reads += ProfileRead(groupProfile(group, playerName), toType, group.shares)
                handled += group.shares
                handled += group.disabledShares
            }
            val unhandled = worldShares - handled
            if (unhandled.isNotEmpty()) {
                reads += ProfileRead(worldProfile(world, playerName), toType, sharesForRead(unhandled))
            }
            val worldWrites =
                if (config.alwaysWriteWorldProfile) {
                    if (config.useOptionalsForUngroupedWorlds) {
                        activeShares()
                    } else {
                        activeShares().intersect(MultiverseInventoryShares.standard)
                    }
                } else {
                    unhandled
                }
            if (worldWrites.isNotEmpty()) {
                writes += ProfileWrite(worldProfile(world, playerName), fromType, worldWrites)
            }
        } else if (config.alwaysWriteWorldProfile) {
            writes += ProfileWrite(worldProfile(world, playerName), fromType, worldShares)
        }

        rejectUnsupported(writes.flatMap { it.shares } + reads.flatMap { it.shares })
        persistWrites(player, writes)
        if (!bypassWorld) applyReads(player, reads)
    }

    fun onPlayerJoined(player: Player) {
        if (!requireActive() || !config.applyPlayerDataOnJoin) return
        requirePrimaryThread()

        val world = player.world.name
        val groups = groupsFor(world)
        val profileType = profileType(player.gameMode)
        val playerName = repository.profileName(player.uniqueId, player.name)
        val handled = linkedSetOf<String>()
        val reads = mutableListOf<ProfileRead>()
        groups.forEach { group ->
            if (!bypassesGroup(player, group)) {
                reads += ProfileRead(
                    groupProfile(group, playerName),
                    profileType,
                    sharesForRead(group.shares),
                )
                handled += group.shares
                handled += group.disabledShares
            }
        }
        val worldShares = worldReadShares(groups) - handled
        val worldReads = sharesForRead(worldShares)
        if (worldReads.isNotEmpty()) {
            reads += ProfileRead(worldProfile(world, playerName), profileType, worldReads)
        }

        rejectUnsupported(reads.flatMap { it.shares })
        applyReads(player, reads)
    }

    fun onPlayerQuit(player: Player) {
        if (!requireActive()) return
        requirePrimaryThread()
        saveCurrentWorld(player)
    }

    fun saveOnlinePlayers(players: Collection<Player>) {
        if (!requireActive()) return
        requirePrimaryThread()
        val failures = mutableListOf<Exception>()
        players.forEach { player ->
            try {
                saveCurrentWorld(player)
            } catch (failure: Exception) {
                failures += failure
            }
        }
        val firstFailure = failures.firstOrNull() ?: return
        failures.drop(1).forEach(firstFailure::addSuppressed)
        throw firstFailure
    }

    fun recordLastLocation(
        player: Player,
        location: org.bukkit.Location,
    ) {
        if (player.uniqueId in applyingStoredLocation) return
        if (!requireActive()) return
        requirePrimaryThread()
        if (!config.applyLastLocationForAllTeleports &&
            "last_location" !in MultiverseInventoryShares.active(config)
        ) {
            return
        }

        val world = location.world?.name ?: return
        val groups = groupsFor(world)
        val profileType = profileType(player.gameMode)
        val playerName = repository.profileName(player.uniqueId, player.name)
        val writes = mutableListOf<ProfileWrite>()
        groups.forEach { group ->
            if ("last_location" in group.shares) {
                writes += ProfileWrite(
                    groupProfile(group, playerName),
                    profileType,
                    setOf("last_location"),
                    lastLocation = location,
                )
            }
        }
        val handled = groups.flatMap { it.shares + it.disabledShares }.toSet()
        val worldShares =
            activeShares().let {
                if (config.alwaysWriteWorldProfile) it else it - handled
            }
        if ("last_location" in worldShares) {
            writes += ProfileWrite(
                worldProfile(world, playerName),
                profileType,
                setOf("last_location"),
                lastLocation = location,
            )
        }
        rejectUnsupported(writes.flatMap { it.shares })
        persistWrites(player, writes)
    }

    private fun saveCurrentWorld(player: Player) {
        val world = player.world.name
        val groups = groupsFor(world)
        val profileType = profileType(player.gameMode)
        val playerName = repository.profileName(player.uniqueId, player.name)
        val writes = mutableListOf<ProfileWrite>()
        val handled = linkedSetOf<String>()
        groups.forEach { group ->
            writes += ProfileWrite(groupProfile(group, playerName), profileType, group.shares)
            handled += group.shares
            handled += group.disabledShares
        }
        val worldShares = activeShares().let { if (config.alwaysWriteWorldProfile) it else it - handled }
        if (worldShares.isNotEmpty()) {
            writes += ProfileWrite(worldProfile(world, playerName), profileType, worldShares)
        }
        rejectUnsupported(writes.flatMap { it.shares })
        persistWrites(player, writes)
        repository.updateGlobalPlayerData(
            player.uniqueId,
            mapOf("lastWorld" to JsonPrimitive(world)),
        )
    }

    private fun persistWrites(
        player: Player,
        writes: List<ProfileWrite>,
    ) {
        writes.filter { it.shares.isNotEmpty() }.forEach { write ->
            val previous =
                repository.readProfile(write.location)
                    ?.get(write.profileType.name)
                    ?.asJsonObjectOrNull()
            val fields = linkedMapOf<String, JsonElement>()
            val stats = linkedMapOf<String, JsonElement>()
            write.shares.forEach { share ->
                captureShare(player, share, previous, write.lastLocation)?.let { captured ->
                    if (captured.first == "stats") {
                        stats[captured.second] = captured.third
                    } else {
                        fields[captured.first] = captured.third
                    }
                }
            }
            if (fields.isNotEmpty() || stats.isNotEmpty()) {
                repository.updateProfile(write.location, write.profileType, fields, stats)
            }
        }
    }

    private fun captureShare(
        player: Player,
        share: String,
        previous: JsonObject?,
        lastLocation: org.bukkit.Location?,
    ): Triple<String, String, JsonElement>? {
        val inventory = player.inventory
        return when (share) {
            "inventory_contents" ->
                Triple(
                    "inventoryContents",
                    "",
                    encodeItems(inventory.storageContents, previous?.get("inventoryContents")),
                )
            "armor_contents" ->
                Triple("armorContents", "", encodeItems(inventory.armorContents, previous?.get("armorContents")))
            "ender_chest" ->
                Triple("enderChestContents", "", encodeItems(player.enderChest.contents, previous?.get("enderChestContents")))
            "off_hand" ->
                Triple("offHandItem", "", encodeItem(inventory.itemInOffHand))
            "max_hit_points" ->
                stat(
                    "mhp",
                    player.getAttribute(Attribute.MAX_HEALTH)?.baseValue ?: 20.0,
                )
            "hit_points" ->
                stat("hp", player.health)
            "remaining_air" -> stat("ra", player.remainingAir)
            "maximum_air" -> stat("ma", player.maximumAir)
            "fall_distance" -> stat("fd", player.fallDistance)
            "fire_ticks" -> stat("ft", player.fireTicks)
            "xp" -> stat("xp", player.exp)
            "total_xp" -> stat("txp", player.totalExperience)
            "lvl" -> stat("el", player.level)
            "food_level" -> stat("fl", player.foodLevel)
            "exhaustion" -> stat("ex", player.exhaustion)
            "saturation" -> stat("sa", player.saturation)
            "bed_spawn" ->
                Triple(
                    "bedSpawnLocation",
                    "",
                    encodeLocation(player.bedSpawnLocation, previous?.get("bedSpawnLocation"), respawn = true),
                )
            "last_location" ->
                lastLocation?.let {
                    Triple(
                        "lastLocation",
                        "",
                        encodeLocation(it, previous?.get("lastLocation"), respawn = false),
                    )
                }
            "potion_effects" ->
                Triple("potions", "", encodePotionEffects(player, previous?.get("potions")))
            "game_statistics" ->
                Triple("game_statistics", "", encodeStatistics(player, previous?.get("game_statistics")))
            "advancements" ->
                Triple("advancements", "", encodeAdvancements(player, previous?.get("advancements")))
            "recipes" -> Triple("recipes", "", encodeRecipes(player, previous?.get("recipes")))
            "economy" -> error("The economy share requires a Vault-compatible economy provider")
            else -> error("Unsupported Multiverse-Inventories share: $share")
        }
    }

    private fun applyReads(
        player: Player,
        reads: List<ProfileRead>,
    ) {
        reads.filter { it.shares.isNotEmpty() }.forEach { read ->
            val profile =
                repository.readProfile(read.location)
                    ?.get(read.profileType.name)
                    ?.asJsonObjectOrNull()
            val shares = read.shares.sortedBy { applyOrder.indexOf(it).let { index -> if (index < 0) Int.MAX_VALUE else index } }
            shares.forEach { share -> applyShare(player, share, profile) }
        }
    }

    private fun applyShare(
        player: Player,
        share: String,
        profile: JsonObject?,
    ) {
        val stats = profile?.get("stats")?.asJsonObjectOrNull()
        when (share) {
            "inventory_contents" ->
                player.inventory.storageContents =
                    decodeItems(profile?.get("inventoryContents"), player.inventory.storageContents.size)
            "armor_contents" ->
                player.inventory.armorContents =
                    decodeItems(profile?.get("armorContents"), player.inventory.armorContents.size)
            "ender_chest" ->
                player.enderChest.contents =
                    decodeItems(profile?.get("enderChestContents"), player.enderChest.contents.size)
            "off_hand" ->
                player.inventory.setItemInOffHand(decodeItem(profile?.get("offHandItem")))
            "max_hit_points" -> {
                val attribute = player.getAttribute(Attribute.MAX_HEALTH)
                if (attribute != null) {
                    val default = attribute.defaultValue
                    attribute.baseValue = stats?.get("mhp")?.numberOrNull()?.toDouble() ?: default
                }
            }
            "hit_points" -> {
                val value = stats?.get("hp")?.numberOrNull()?.toDouble() ?: 20.0
                player.health = value.coerceIn(0.0, player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0)
            }
            "remaining_air" -> player.remainingAir = stats?.get("ra")?.numberOrNull()?.toInt() ?: 300
            "maximum_air" -> player.maximumAir = stats?.get("ma")?.numberOrNull()?.toInt() ?: 300
            "fall_distance" -> player.fallDistance = stats?.get("fd")?.numberOrNull()?.toFloat() ?: 0.0f
            "fire_ticks" -> player.fireTicks = stats?.get("ft")?.numberOrNull()?.toInt() ?: 0
            "xp" -> player.exp = (stats?.get("xp")?.numberOrNull()?.toFloat() ?: 0.0f).coerceIn(0.0f, 1.0f)
            "total_xp" -> player.totalExperience = stats?.get("txp")?.numberOrNull()?.toInt() ?: 0
            "lvl" -> player.level = stats?.get("el")?.numberOrNull()?.toInt() ?: 0
            "food_level" -> player.foodLevel = stats?.get("fl")?.numberOrNull()?.toInt() ?: 20
            "exhaustion" -> player.exhaustion = stats?.get("ex")?.numberOrNull()?.toFloat() ?: 0.0f
            "saturation" -> player.saturation = stats?.get("sa")?.numberOrNull()?.toFloat() ?: 5.0f
            "bed_spawn" -> applyBedSpawn(player, profile?.get("bedSpawnLocation"))
            "last_location" -> {
                if (config.applyLastLocationForAllTeleports) applyLastLocation(player, profile?.get("lastLocation"))
            }
            "potion_effects" -> applyPotionEffects(player, profile?.get("potions"))
            "game_statistics" -> applyStatistics(player, profile?.get("game_statistics"))
            "advancements" -> applyAdvancements(player, profile?.get("advancements"))
            "recipes" -> applyRecipes(player, profile?.get("recipes"))
            "economy" -> error("The economy share requires a Vault-compatible economy provider")
            else -> error("Unsupported Multiverse-Inventories share: $share")
        }
    }

    private fun encodeItems(
        items: Array<out ItemStack?>,
        existing: JsonElement?,
    ): JsonObject {
        val result =
            existing?.asJsonObjectOrNull()?.deepCopy()
                ?: JsonObject()
        items.forEachIndexed { index, item ->
            val key = index.toString()
            result.remove(key)
            val serialized = encodeItem(item)
            if (!serialized.isJsonNull) result.add(key, serialized)
        }
        return result
    }

    private fun encodeItem(item: ItemStack?): JsonElement {
        if (item == null || item.type.isAir || item.amount <= 0) return JsonNull.INSTANCE
        return if (config.useByteSerializationForInventoryData) {
            JsonPrimitive(Base64.getEncoder().encodeToString(item.serializeAsBytes()))
        } else {
            GSON.toJsonTree(item.serialize())
        }
    }

    private fun decodeItems(
        field: JsonElement?,
        size: Int,
    ): Array<ItemStack?> {
        val result = arrayOfNulls<ItemStack>(size)
        val values = field?.takeUnless { it.isJsonNull } ?: return result
        require(values.isJsonObject) { "Expected a slot map in Multiverse-Inventories profile" }
        values.asJsonObject.entrySet().forEach { (slot, value) ->
            val index = slot.toIntOrNull() ?: return@forEach
            if (index in result.indices) result[index] = decodeItem(value)
        }
        return result
    }

    private fun decodeItem(value: JsonElement?): ItemStack? {
        if (value == null || value.isJsonNull) return null
        if (value.isJsonPrimitive && value.asJsonPrimitive.isString) {
            val bytes =
                try {
                    Base64.getDecoder().decode(value.asString)
                } catch (failure: IllegalArgumentException) {
                    throw IllegalArgumentException("Invalid Base64 ItemStack in Multiverse-Inventories profile", failure)
                }
            return ItemStack.deserializeBytes(bytes)
        }
        require(value.isJsonObject) { "Unsupported ItemStack representation in Multiverse-Inventories profile" }
        val serialized = value.asJsonObject.toJavaMap()
        val deserialized =
            ConfigurationSerialization.deserializeObject(serialized)
                ?: throw IllegalArgumentException("Could not deserialize ItemStack from Multiverse-Inventories profile")
        return deserialized as? ItemStack
            ?: throw IllegalArgumentException("Profile item is not a Bukkit ItemStack")
    }

    private fun encodeStatistics(
        player: Player,
        previous: JsonElement?,
    ): JsonObject {
        val result = previous?.asJsonObjectOrNull()?.deepCopy() ?: JsonObject()
        Statistic.values().forEach { statistic ->
            when (statistic.type) {
                Statistic.Type.UNTYPED -> {
                    val value = player.getStatistic(statistic)
                    if (value == 0) result.remove(statistic.name) else result.addProperty(statistic.name, value)
                }
                Statistic.Type.ITEM, Statistic.Type.BLOCK -> {
                    val itemStats = result.get(statistic.name)?.asJsonObjectOrNull()?.deepCopy() ?: JsonObject()
                    Material.values().filter { material ->
                        if (statistic.type == Statistic.Type.ITEM) material.isItem else material.isBlock
                    }.forEach { material ->
                        val value = player.getStatistic(statistic, material)
                        val key = material.key.toString()
                        if (value == 0) itemStats.remove(key) else itemStats.addProperty(key, value)
                    }
                    if (itemStats.size() == 0) result.remove(statistic.name) else result.add(statistic.name, itemStats)
                }
                Statistic.Type.ENTITY -> {
                    val entityStats = result.get(statistic.name)?.asJsonObjectOrNull()?.deepCopy() ?: JsonObject()
                    EntityType.values().filter { it != EntityType.UNKNOWN }.forEach { entityType ->
                        val value = player.getStatistic(statistic, entityType)
                        val key = entityType.key.toString()
                        if (value == 0) entityStats.remove(key) else entityStats.addProperty(key, value)
                    }
                    if (entityStats.size() == 0) result.remove(statistic.name) else result.add(statistic.name, entityStats)
                }
            }
        }
        return result
    }

    private fun applyStatistics(
        player: Player,
        value: JsonElement?,
    ) {
        val data = value?.takeUnless { it.isJsonNull }?.asJsonObjectOrNull() ?: JsonObject()
        Statistic.values().forEach { statistic ->
            val stored = data.get(statistic.name)
            when (statistic.type) {
                Statistic.Type.UNTYPED ->
                    player.setStatistic(statistic, stored?.numberOrNull()?.toInt() ?: 0)
                Statistic.Type.ITEM, Statistic.Type.BLOCK -> {
                    val itemData = stored?.asJsonObjectOrNull()
                    Material.values().filter { material ->
                        if (statistic.type == Statistic.Type.ITEM) material.isItem else material.isBlock
                    }.forEach { material ->
                        val count = itemData?.get(material.key.toString())?.numberOrNull()?.toInt() ?: 0
                        player.setStatistic(statistic, material, count)
                    }
                }
                Statistic.Type.ENTITY -> {
                    val entityData = stored?.asJsonObjectOrNull()
                    EntityType.values().filter { it != EntityType.UNKNOWN }.forEach { entityType ->
                        val count = entityData?.get(entityType.key.toString())?.numberOrNull()?.toInt() ?: 0
                        player.setStatistic(statistic, entityType, count)
                    }
                }
            }
        }
    }

    private fun encodePotionEffects(
        player: Player,
        previous: JsonElement?,
    ): JsonArray {
        val effects =
            previous?.takeIf { it.isJsonArray }?.asJsonArray?.mapNotNull { element ->
                val serialized = element.asJsonObjectOrNull() ?: return@mapNotNull element.deepCopy()
                if (decodePotionEffect(serialized) == null) {
                    element.deepCopy()
                } else {
                    null
                }
            }?.toMutableList() ?: mutableListOf()
        player.activePotionEffects.forEach { effect ->
            effects += GSON.toJsonTree(effect.serialize())
        }
        return JsonArray().apply { effects.forEach(::add) }
    }

    private fun applyPotionEffects(
        player: Player,
        value: JsonElement?,
    ) {
        player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
        val effects = value?.takeUnless { it.isJsonNull } ?: return
        require(effects.isJsonArray) { "Expected a potion list in Multiverse-Inventories profile" }
        effects.asJsonArray.forEach { element ->
            require(element.isJsonObject) { "Expected a serialized potion effect in Multiverse-Inventories profile" }
            val effect =
                decodePotionEffect(element.asJsonObject)
                    ?: throw IllegalArgumentException("Could not deserialize potion effect from Multiverse-Inventories profile")
            player.addPotionEffect(effect)
        }
    }

    private fun decodePotionEffect(value: JsonObject): PotionEffect? {
        val decoded =
            ConfigurationSerialization.deserializeObject(value.toJavaMap())
                ?: return null
        return decoded as? PotionEffect
    }

    private fun encodeAdvancements(
        player: Player,
        previous: JsonElement?,
    ): JsonArray {
        val knownCriteria = mutableSetOf<String>()
        val awardedCriteria = mutableSetOf<String>()
        Bukkit.advancementIterator().forEachRemaining { advancement ->
            knownCriteria += advancement.criteria
            awardedCriteria += player.getAdvancementProgress(advancement).awardedCriteria
        }
        val unknownCriteria =
            previous?.takeIf { it.isJsonArray }?.asJsonArray
                ?.filter { element ->
                    !element.isJsonPrimitive || !element.asJsonPrimitive.isString || element.asString !in knownCriteria
                }
                ?.map(JsonElement::deepCopy)
                .orEmpty()
        return JsonArray().apply {
            val currentCriteria = awardedCriteria.map(::JsonPrimitive)
            (unknownCriteria + currentCriteria).distinct().forEach { add(it) }
        }
    }

    private fun applyAdvancements(
        player: Player,
        value: JsonElement?,
    ) {
        val awarded =
            value?.takeUnless { it.isJsonNull }
                ?.takeIf { it.isJsonArray }
                ?.asJsonArray
                ?.mapNotNull { it.takeIf(JsonElement::isJsonPrimitive)?.asString }
                ?.toSet()
                .orEmpty()
        Bukkit.advancementIterator().forEachRemaining { advancement ->
            val progress = player.getAdvancementProgress(advancement)
            advancement.criteria.forEach { criterion ->
                if (criterion in awarded) {
                    progress.awardCriteria(criterion)
                } else {
                    progress.revokeCriteria(criterion)
                }
            }
        }
    }

    private fun encodeRecipes(
        player: Player,
        previous: JsonElement?,
    ): JsonArray {
        val knownRecipes = mutableSetOf<String>()
        Bukkit.recipeIterator().forEachRemaining { recipe ->
            (recipe as? Keyed)?.key?.let { knownRecipes += it.toString() }
        }
        val unknownRecipes =
            previous?.takeIf { it.isJsonArray }?.asJsonArray
                ?.filter { element ->
                    !element.isJsonPrimitive || !element.asJsonPrimitive.isString || element.asString !in knownRecipes
                }
                ?.map(JsonElement::deepCopy)
                .orEmpty()
        return JsonArray().apply {
            val currentRecipes = player.discoveredRecipes.map { JsonPrimitive(it.toString()) }
            (unknownRecipes + currentRecipes)
                .distinct()
                .forEach { add(it) }
        }
    }

    private fun applyRecipes(
        player: Player,
        value: JsonElement?,
    ) {
        val discovered =
            value?.takeUnless { it.isJsonNull }
                ?.takeIf { it.isJsonArray }
                ?.asJsonArray
                ?.mapNotNull { it.takeIf(JsonElement::isJsonPrimitive)?.asString }
                ?.mapNotNull(NamespacedKey::fromString)
                ?.toSet()
                .orEmpty()
        val current = player.discoveredRecipes
        player.undiscoverRecipes(current - discovered)
        player.discoverRecipes(discovered - current)
    }

    private fun encodeLocation(
        location: org.bukkit.Location?,
        previous: JsonElement?,
        respawn: Boolean,
    ): JsonElement {
        if (location == null || location.world == null) return JsonNull.INSTANCE
        val result = previous?.asJsonObjectOrNull()?.deepCopy() ?: JsonObject()
        result.addProperty("==", if (respawn) "RespawnLocation" else "UnloadedWorldLocation")
        result.addProperty("world", location.world!!.name)
        result.addProperty("x", location.x)
        result.addProperty("y", location.y)
        result.addProperty("z", location.z)
        result.addProperty("yaw", location.yaw)
        result.addProperty("pitch", location.pitch)
        if (respawn) {
            val respawnType =
                when {
                    location.world!!.getBlockAt(location).blockData is Bed -> "BED"
                    location.world!!.getBlockAt(location).type == Material.RESPAWN_ANCHOR -> "ANCHOR"
                    else -> "UNKNOWN"
                }
            result.addProperty("respawnType", respawnType)
        }
        return result
    }

    private fun applyLastLocation(
        player: Player,
        value: JsonElement?,
    ) {
        val location = parseLocation(value) ?: return
        if (location.world == null || location == player.location) return
        applyingStoredLocation += player.uniqueId
        try {
            check(player.teleport(location)) {
                "Teleport to the Multiverse-Inventories last location was cancelled"
            }
        } finally {
            applyingStoredLocation -= player.uniqueId
        }
    }

    private fun applyBedSpawn(
        player: Player,
        value: JsonElement?,
    ) {
        val map = locationObject(value)
        val worldName =
            map?.get("world")?.takeIf { it.isJsonPrimitive }?.asString
                ?: map?.get("wo")?.takeIf { it.isJsonPrimitive }?.asString
        val world = worldName?.let(Bukkit::getWorld)
        if (world == null || map == null) {
            player.setBedSpawnLocation(player.world.spawnLocation, true)
            return
        }
        val location =
            org.bukkit.Location(
                world,
                map.get("x")?.numberOrNull()?.toDouble() ?: 0.0,
                map.get("y")?.numberOrNull()?.toDouble() ?: 0.0,
                map.get("z")?.numberOrNull()?.toDouble() ?: 0.0,
                (map.get("yaw") ?: map.get("ya"))?.numberOrNull()?.toFloat() ?: 0.0f,
                (map.get("pitch") ?: map.get("pi"))?.numberOrNull()?.toFloat() ?: 0.0f,
            )
        if (config.validateBedAnchorRespawnLocation) {
            val type = map.get("respawnType")?.takeIf { it.isJsonPrimitive }?.asString?.uppercase()
            val block = world.getBlockAt(location)
            val valid =
                when (type) {
                    "BED" -> block.blockData is Bed
                    "ANCHOR" -> block.type == Material.RESPAWN_ANCHOR
                    else -> true
                }
            if (!valid) {
                player.setBedSpawnLocation(player.world.spawnLocation, true)
                return
            }
        }
        player.setBedSpawnLocation(location, true)
    }

    private fun parseLocation(value: JsonElement?): org.bukkit.Location? {
        val map = locationObject(value) ?: return null
        val worldName =
            map.get("world")?.takeIf { it.isJsonPrimitive }?.asString
                ?: map.get("wo")?.takeIf { it.isJsonPrimitive }?.asString
                ?: return null
        val world = Bukkit.getWorld(worldName) ?: return null
        return org.bukkit.Location(
            world,
            map.get("x")?.numberOrNull()?.toDouble() ?: 0.0,
            map.get("y")?.numberOrNull()?.toDouble() ?: 0.0,
            map.get("z")?.numberOrNull()?.toDouble() ?: 0.0,
            (map.get("yaw") ?: map.get("ya"))?.numberOrNull()?.toFloat() ?: 0.0f,
            (map.get("pitch") ?: map.get("pi"))?.numberOrNull()?.toFloat() ?: 0.0f,
        )
    }

    private fun locationObject(value: JsonElement?): JsonObject? {
        if (value == null || value.isJsonNull) return null
        val parsed =
            if (value.isJsonPrimitive && value.asJsonPrimitive.isString) {
                JsonParser.parseString(value.asString)
            } else {
                value
            }
        require(parsed.isJsonObject) { "Expected a serialized location in Multiverse-Inventories profile" }
        return parsed.asJsonObject
    }

    private fun stat(
        key: String,
        value: Number,
    ) = Triple("stats", key, GSON.toJsonTree(value))

    private fun groupsFor(worldName: String): List<ResolvedInventoryGroup> =
        MultiverseInventoryShares.resolveGroups(config, repository.readWorldGroups(), worldName)

    private fun activeShares(): Set<String> = MultiverseInventoryShares.active(config)

    private fun worldProfileShares(groups: List<ResolvedInventoryGroup>): Set<String> =
        if (groups.isEmpty() && !config.useOptionalsForUngroupedWorlds) {
            activeShares().intersect(MultiverseInventoryShares.standard)
        } else {
            activeShares()
        }

    private fun worldReadShares(groups: List<ResolvedInventoryGroup>): Set<String> {
        return if (groups.isEmpty() && !config.useOptionalsForUngroupedWorlds) {
            activeShares().intersect(MultiverseInventoryShares.standard)
        } else {
            activeShares()
        }
    }

    private fun sharesForRead(shares: Set<String>): Set<String> {
        return if (config.applyLastLocationForAllTeleports) {
            shares
        } else {
            shares - "last_location"
        }
    }

    private fun rejectUnsupported(shares: Collection<String>) {
        val unsupported = shares.filter { it == "economy" }.toSet()
        check(unsupported.isEmpty()) {
            "PCBridge cannot apply configured Multiverse-Inventories shares: ${unsupported.sorted().joinToString()}"
        }
    }

    private fun profileType(gameMode: GameMode): InventoryProfileType {
        return if (config.enableGamemodeShareHandling) {
            InventoryProfileType.valueOf(gameMode.name)
        } else {
            InventoryProfileType.SURVIVAL
        }
    }

    private fun bypassesWorld(
        player: Player,
        worldName: String,
    ): Boolean {
        if (!config.enableBypassPermissions) return false
        val permission = InventoriesPermissions.BYPASS_WORLD.node
        return player.hasPermission(permission) || player.hasPermission("$permission.$worldName")
    }

    private fun bypassesGroup(
        player: Player,
        group: ResolvedInventoryGroup,
    ): Boolean {
        if (!config.enableBypassPermissions) return false
        val permission = InventoriesPermissions.BYPASS_GROUP.node
        return player.hasPermission(permission) || player.hasPermission("$permission.${group.name}")
    }

    private fun bypassesGameMode(
        player: Player,
        gameMode: GameMode,
    ): Boolean {
        if (!config.enableBypassPermissions) return false
        val permission = InventoriesPermissions.BYPASS_GAME_MODE.node
        return player.hasPermission(permission) || player.hasPermission("$permission.${gameMode.name.lowercase()}")
    }

    private fun groupProfile(
        group: ResolvedInventoryGroup,
        playerName: String,
    ) = InventoryProfileLocation("groups", group.name, playerName)

    private fun worldProfile(
        worldName: String,
        playerName: String,
    ) = InventoryProfileLocation("worlds", worldName, playerName)

    private fun requireActive(): Boolean {
        if (mode == InventoryCompatibilityMode.READ_ONLY) return false
        check(!multiverseInventoriesEnabled()) {
            "Disable Multiverse-Inventories before enabling PCBridge inventory transitions"
        }
        return true
    }

    private fun requirePrimaryThread() {
        check(Bukkit.isPrimaryThread()) {
            "Multiverse-Inventories transitions must run on the server thread"
        }
    }

    private fun JsonElement?.asJsonObjectOrNull(): JsonObject? {
        if (this == null || this.isJsonNull) return null
        require(this.isJsonObject) { "Expected an object in Multiverse-Inventories profile" }
        return this.asJsonObject
    }

    private fun JsonElement.numberOrNull(): Number? {
        if (!isJsonPrimitive || !asJsonPrimitive.isNumber) return null
        return asNumber
    }

    private fun JsonObject.toJavaMap(): Map<String, Any> = entrySet().associate { (key, value) -> key to value.toJavaValue() as Any }

    private fun JsonElement.toJavaValue(): Any? = when {
        isJsonNull -> null
        isJsonObject -> asJsonObject.entrySet().associate { (key, value) -> key to value.toJavaValue() }
        isJsonArray -> asJsonArray.map { it.toJavaValue() }
        asJsonPrimitive.isBoolean -> asBoolean
        asJsonPrimitive.isNumber -> asNumber
        else -> asString
    }

    private data class ProfileWrite(
        val location: InventoryProfileLocation,
        val profileType: InventoryProfileType,
        val shares: Set<String>,
        val lastLocation: org.bukkit.Location? = null,
    )

    private data class ProfileRead(
        val location: InventoryProfileLocation,
        val profileType: InventoryProfileType,
        val shares: Set<String>,
    )

    private companion object {
        val GSON = Gson()
        val applyOrder =
            listOf(
                "max_hit_points",
                "inventory_contents",
                "armor_contents",
                "ender_chest",
                "off_hand",
                "maximum_air",
                "remaining_air",
                "fall_distance",
                "fire_ticks",
                "xp",
                "total_xp",
                "lvl",
                "food_level",
                "exhaustion",
                "saturation",
                "hit_points",
                "bed_spawn",
                "potion_effects",
                "game_statistics",
                "advancements",
                "recipes",
            )
    }
}
