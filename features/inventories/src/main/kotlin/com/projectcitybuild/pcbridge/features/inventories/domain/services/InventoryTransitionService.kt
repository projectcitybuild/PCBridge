package com.projectcitybuild.pcbridge.features.inventories.domain.services

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryProfileLocation
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryProfileReadResult
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryProfileType
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryShareOwnership
import com.projectcitybuild.pcbridge.features.inventories.domain.data.MultiverseInventoryConfig
import com.projectcitybuild.pcbridge.features.inventories.domain.data.MultiverseInventoryShares
import com.projectcitybuild.pcbridge.features.inventories.domain.data.ResolvedInventoryGroup
import com.projectcitybuild.pcbridge.features.inventories.domain.repositories.InventoryRepository
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Keyed
import org.bukkit.Location
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
import org.bukkit.potion.PotionEffectType
import java.util.Base64
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger

class InventoryTransitionService(
    private val repository: InventoryRepository,
    private val config: MultiverseInventoryConfig,
    private val logger: Logger = Logger.getLogger("PCBridge.Multiverse-Inventories"),
) {
    val applyPlayerDataOnJoin: Boolean
        get() = config.applyPlayerDataOnJoin

    private val applyingStoredLocation = ConcurrentHashMap.newKeySet<UUID>()
    private val pendingRespawnWorlds = ConcurrentHashMap<UUID, String>()
    private val reportedUnsupportedShares = ConcurrentHashMap.newKeySet<String>()
    private val reportedShareConflicts = ConcurrentHashMap.newKeySet<String>()

    fun onWorldChanged(
        player: Player,
        fromWorld: String,
        toWorld: String,
    ) {
        if (player.uniqueId in applyingStoredLocation) return
        requirePrimaryThread()
        if (pendingRespawnWorlds[player.uniqueId] == toWorld) return
        if (fromWorld == toWorld) return

        val profileType = profileType(player.gameMode)
        val sourceGroups = groupsFor(fromWorld)
        val destinationGroups = groupsFor(toWorld)
        val sourceOwnership = shareOwnership(sourceGroups, fromWorld)
        val destinationOwnership = shareOwnership(destinationGroups, toWorld)
        val playerName = repository.profileName(player.uniqueId, player.name)
        val writes = mutableListOf<ProfileWrite>()
        val reads = mutableListOf<ProfileRead>()

        sourceOwnership.groupOwners.forEach { (share, group) ->
            if (destinationOwnership.groupOwners[share]?.name != group.name) {
                writes += ProfileWrite(groupProfile(group, playerName), profileType, setOf(share))
            }
        }
        val worldWrites = worldOwnedShares(sourceGroups, sourceOwnership)
        if (worldWrites.isNotEmpty()) {
            writes += ProfileWrite(worldProfile(fromWorld, playerName), profileType, worldWrites)
        }

        destinationOwnership.groupOwners.forEach { (share, group) ->
            if (sourceOwnership.groupOwners[share]?.name != group.name) {
                reads += ProfileRead(
                    groupProfile(group, playerName),
                    profileType,
                    sharesForRead(setOf(share)),
                )
            }
        }
        val worldReads = sharesForRead(worldOwnedShares(destinationGroups, destinationOwnership))
        if (worldReads.isNotEmpty()) {
            reads += ProfileRead(worldProfile(toWorld, playerName), profileType, worldReads)
        }

        persistWrites(player, writes)
        applyReads(player, reads)
        updateLastWorldMetadata(player, toWorld)
    }

    fun onPlayerRespawnScheduled(
        player: Player,
        destinationWorld: String,
    ) {
        requirePrimaryThread()
        pendingRespawnWorlds[player.uniqueId] = destinationWorld
    }

    fun onPlayerRespawned(
        player: Player,
        destinationWorld: String,
    ) {
        requirePrimaryThread()
        val pendingWorld = pendingRespawnWorlds.remove(player.uniqueId)
        if (pendingWorld != destinationWorld || !player.isOnline || player.world.name != destinationWorld) return

        val groups = groupsFor(destinationWorld)
        val ownership = shareOwnership(groups, destinationWorld)
        val playerName = repository.profileName(player.uniqueId, player.name)
        val profileType = profileType(player.gameMode)
        val reads = ownership.groupOwners.entries
            .groupBy({ it.value }, { it.key })
            .map { (group, shares) ->
                ProfileRead(
                    groupProfile(group, playerName),
                    profileType,
                    sharesForRead(shares.toSet()),
                )
            }.toMutableList()
        val worldShares = sharesForRead(worldOwnedShares(groups, ownership))
        if (worldShares.isNotEmpty()) {
            reads += ProfileRead(worldProfile(destinationWorld, playerName), profileType, worldShares)
        }

        applyReads(player, reads)
        updateLastWorldMetadata(player, destinationWorld)
    }

    fun onGameModeChanged(
        player: Player,
        fromGameMode: GameMode,
        toGameMode: GameMode,
    ): (() -> Unit)? {
        if (!config.enableGamemodeShareHandling) return null
        requirePrimaryThread()
        val fromType = profileType(fromGameMode)
        val toType = profileType(toGameMode)
        if (fromType == toType) return null

        val world = player.world.name
        val groups = groupsFor(world)
        val ownership = shareOwnership(groups, world)
        val playerName = repository.profileName(player.uniqueId, player.name)
        val reads = mutableListOf<ProfileRead>()
        val writes = mutableListOf<ProfileWrite>()

        ownership.groupOwners.entries
            .groupBy({ it.value }, { it.key })
            .forEach { (group, shares) ->
                writes += ProfileWrite(groupProfile(group, playerName), fromType, shares.toSet())
                reads += ProfileRead(groupProfile(group, playerName), toType, sharesForRead(shares.toSet()))
            }
        val worldShares = worldOwnedShares(groups, ownership)
        if (worldShares.isNotEmpty()) {
            reads += ProfileRead(worldProfile(world, playerName), toType, sharesForRead(worldShares))
        }
        if (worldShares.isNotEmpty()) {
            writes += ProfileWrite(worldProfile(world, playerName), fromType, worldShares)
        }

        persistWrites(player, writes)
        return {
            requirePrimaryThread()
            if (player.isOnline && player.gameMode == toGameMode && player.world.name == world) {
                applyReads(player, reads)
            }
        }
    }

    fun onPlayerJoined(player: Player) {
        if (!config.applyPlayerDataOnJoin) return
        requirePrimaryThread()

        val world = player.world.name
        val groups = groupsFor(world)
        val ownership = shareOwnership(groups, world)
        val profileType = profileType(player.gameMode)
        val playerName = repository.profileName(player.uniqueId, player.name)
        val reads = mutableListOf<ProfileRead>()
        ownership.groupOwners.entries
            .groupBy({ it.value }, { it.key })
            .forEach { (group, shares) ->
                reads += ProfileRead(
                    groupProfile(group, playerName),
                    profileType,
                    sharesForRead(shares.toSet()),
                )
            }
        val worldReads = sharesForRead(worldOwnedShares(groups, ownership))
        if (worldReads.isNotEmpty()) {
            reads += ProfileRead(worldProfile(world, playerName), profileType, worldReads)
        }

        applyReads(player, reads)
    }

    fun onPlayerQuit(player: Player) {
        requirePrimaryThread()
        pendingRespawnWorlds.remove(player.uniqueId)
        saveCurrentWorld(player)
    }

    fun saveOnlinePlayers(players: Collection<Player>) {
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
        location: Location,
    ) {
        if (player.uniqueId in applyingStoredLocation) return
        requirePrimaryThread()
        if (!config.applyLastLocationForAllTeleports &&
            "last_location" !in MultiverseInventoryShares.active(config)
        ) {
            return
        }

        val world = location.world?.name ?: return
        val groups = groupsFor(world)
        val ownership = shareOwnership(groups, world)
        val profileType = profileType(player.gameMode)
        val playerName = repository.profileName(player.uniqueId, player.name)
        val writes = mutableListOf<ProfileWrite>()
        ownership.groupOwners["last_location"]?.let { group ->
            writes += ProfileWrite(
                groupProfile(group, playerName),
                profileType,
                setOf("last_location"),
                lastLocation = location,
            )
        }
        if ("last_location" in worldOwnedShares(groups, ownership)) {
            writes += ProfileWrite(
                worldProfile(world, playerName),
                profileType,
                setOf("last_location"),
                lastLocation = location,
            )
        }
        persistWrites(player, writes)
    }

    private fun saveCurrentWorld(player: Player) {
        val world = player.world.name
        val groups = groupsFor(world)
        val ownership = shareOwnership(groups, world)
        val profileType = profileType(player.gameMode)
        val playerName = repository.profileName(player.uniqueId, player.name)
        val writes = mutableListOf<ProfileWrite>()
        ownership.groupOwners.entries
            .groupBy({ it.value }, { it.key })
            .forEach { (group, shares) ->
                writes += ProfileWrite(groupProfile(group, playerName), profileType, shares.toSet())
            }
        val worldShares = worldOwnedShares(groups, ownership)
        if (worldShares.isNotEmpty()) {
            writes += ProfileWrite(worldProfile(world, playerName), profileType, worldShares)
        }
        persistWrites(player, writes)
        updateLastWorldMetadata(player, world)
    }

    private fun updateLastWorldMetadata(
        player: Player,
        worldName: String,
    ) {
        try {
            repository.updateGlobalPlayerData(
                player.uniqueId,
                mapOf("lastWorld" to JsonPrimitive(worldName)),
            )
        } catch (failure: Exception) {
            logger.log(
                Level.SEVERE,
                "Could not update global inventory metadata players/${player.uniqueId}.json " +
                    "after transition to world '$worldName' for player ${player.name}",
                failure,
            )
            throw failure
        }
    }

    private fun persistWrites(
        player: Player,
        writes: List<ProfileWrite>,
    ) {
        writes
            .filter { it.shares.isNotEmpty() }
            .groupBy { it.location to it.profileType }
            .forEach { (key, profileWrites) ->
                val lastLocations = profileWrites.mapNotNull { it.lastLocation }.distinct()
                require(lastLocations.size <= 1) {
                    "Conflicting last locations were planned for ${key.first.directory}/${key.first.key}"
                }
                val write = ProfileWrite(
                    location = key.first,
                    profileType = key.second,
                    shares = profileWrites.flatMap { it.shares }.toSet(),
                    lastLocation = lastLocations.singleOrNull(),
                )
                val previous =
                    readProfileForTransition(
                        write.location,
                        write.profileType,
                        player,
                        write.shares,
                        "saving",
                    )?.get(write.profileType.name)
                        ?.asJsonObjectOrNull()
                val fields = linkedMapOf<String, JsonElement>()
                val stats = linkedMapOf<String, JsonElement>()
                write.shares.forEach { share ->
                    try {
                        captureShare(player, share, previous, write.lastLocation)?.let { captured ->
                            if (captured.first == "stats") {
                                stats[captured.second] = captured.third
                            } else {
                                fields[captured.first] = captured.third
                            }
                        }
                    } catch (failure: Exception) {
                        logger.log(
                            Level.SEVERE,
                            "Could not capture Multiverse-Inventories share '$share' for " +
                                "${write.location.directory}/${write.location.key}/" +
                                "${write.location.playerName}.json (${write.profileType}) for player ${player.name}",
                            failure,
                        )
                        throw failure
                    }
                }
                if (fields.isNotEmpty() || stats.isNotEmpty()) {
                    try {
                        repository.updateProfile(write.location, write.profileType, fields, stats)
                    } catch (failure: Exception) {
                        logger.log(
                            Level.SEVERE,
                            "Could not write Multiverse-Inventories profile " +
                                "${write.location.directory}/${write.location.key}/" +
                                "${write.location.playerName}.json (${write.profileType}) with shares " +
                                "${write.shares.sorted().joinToString()} for player ${player.name}",
                            failure,
                        )
                        throw failure
                    }
                }
            }
    }

    private fun captureShare(
        player: Player,
        share: String,
        previous: JsonObject?,
        lastLocation: Location?,
    ): Triple<String, String, JsonElement>? {
        val inventory = player.inventory
        return when (share) {
            "inventory_contents" -> {
                Triple(
                    "inventoryContents",
                    "",
                    encodeItems(inventory.contents, previous?.get("inventoryContents")),
                )
            }
            "armor_contents" -> {
                Triple("armorContents", "", encodeItems(inventory.armorContents, previous?.get("armorContents")))
            }
            "ender_chest" -> {
                Triple("enderChestContents", "", encodeItems(player.enderChest.contents, previous?.get("enderChestContents")))
            }
            "off_hand" -> {
                Triple("offHandItem", "", encodeItem(inventory.itemInOffHand))
            }
            "max_hit_points" -> {
                stat(
                    "mhp",
                    player.getAttribute(Attribute.MAX_HEALTH)?.baseValue ?: 20.0,
                )
            }
            "hit_points" -> {
                stat("hp", player.health)
            }
            "remaining_air" -> {
                stat("ra", player.remainingAir)
            }
            "maximum_air" -> {
                stat("ma", player.maximumAir)
            }
            "fall_distance" -> {
                stat("fd", player.fallDistance)
            }
            "fire_ticks" -> {
                stat("ft", player.fireTicks)
            }
            "xp" -> {
                stat("xp", player.exp)
            }
            "total_xp" -> {
                stat("txp", player.totalExperience)
            }
            "lvl" -> {
                stat("el", player.level)
            }
            "food_level" -> {
                stat("fl", player.foodLevel)
            }
            "exhaustion" -> {
                stat("ex", player.exhaustion)
            }
            "saturation" -> {
                stat("sa", player.saturation)
            }
            "bed_spawn" -> {
                Triple(
                    "bedSpawnLocation",
                    "",
                    encodeLocation(player.bedSpawnLocation, previous?.get("bedSpawnLocation"), respawn = true),
                )
            }
            "last_location" -> {
                lastLocation?.let {
                    Triple(
                        "lastLocation",
                        "",
                        encodeLocation(it, previous?.get("lastLocation"), respawn = false),
                    )
                }
            }
            "potion_effects" -> {
                Triple("potions", "", encodePotionEffects(player, previous?.get("potions")))
            }
            "game_statistics" -> {
                Triple("game_statistics", "", encodeStatistics(player, previous?.get("game_statistics")))
            }
            "advancements" -> {
                Triple("advancements", "", encodeAdvancements(player, previous?.get("advancements")))
            }
            "recipes" -> {
                Triple("recipes", "", encodeRecipes(player, previous?.get("recipes")))
            }
            "economy" -> {
                error("The economy share requires a Vault-compatible economy provider")
            }
            else -> {
                error("Unsupported Multiverse-Inventories share: $share")
            }
        }
    }

    private fun applyReads(
        player: Player,
        reads: List<ProfileRead>,
    ) {
        reads
            .filter { it.shares.isNotEmpty() }
            .groupBy { it.location to it.profileType }
            .forEach { (key, profileReads) ->
                val requestedShares = profileReads.flatMap { it.shares }.toSet()
                val document = readProfileForTransition(
                    key.first,
                    key.second,
                    player,
                    requestedShares,
                    "applying",
                )
                if (document == null) {
                    return@forEach
                }
                val modeProfile = document.get(key.second.name)
                if (modeProfile == null || modeProfile.isJsonNull) {
                    logger.fine(
                        "Inventory profile ${key.first.directory}/${key.first.key}/" +
                            "${key.first.playerName}.json has no ${key.second} section; preserving current shares",
                    )
                    return@forEach
                }
                val profile = try {
                    modeProfile.asJsonObjectOrNull()
                } catch (failure: Exception) {
                    logger.log(
                        Level.SEVERE,
                        "Malformed ${key.second} section in inventory profile " +
                            "${key.first.directory}/${key.first.key}/${key.first.playerName}.json " +
                            "while applying shares ${requestedShares.sorted().joinToString()} to player ${player.name}",
                        failure,
                    )
                    throw failure
                } ?: return@forEach
                val failure = mutableListOf<Exception>()
                requestedShares
                    .sortedBy { applyOrder.indexOf(it).let { index -> if (index < 0) Int.MAX_VALUE else index } }
                    .forEach { share ->
                        try {
                            if (hasShareData(profile, share)) {
                                applyShare(player, share, profile)
                            } else {
                                logger.fine(
                                    "Inventory profile ${key.first.directory}/${key.first.key}/" +
                                        "${key.first.playerName}.json has no ${key.second} data for '$share'; " +
                                        "preserving the current share",
                                )
                            }
                        } catch (shareFailure: Exception) {
                            logger.log(
                                Level.SEVERE,
                                "Could not apply Multiverse-Inventories share '$share' from " +
                                    "${key.first.directory}/${key.first.key}/${key.first.playerName}.json " +
                                    "for ${key.second} to player ${player.name}",
                                shareFailure,
                            )
                            failure += shareFailure
                        }
                    }
                failure.firstOrNull()?.let { first ->
                    failure.drop(1).forEach(first::addSuppressed)
                    throw IllegalStateException("One or more inventory shares could not be applied", first)
                }
            }
    }

    private fun hasShareData(
        profile: JsonObject,
        share: String,
    ): Boolean {
        val field = when (share) {
            "inventory_contents" -> "inventoryContents"
            "armor_contents" -> "armorContents"
            "ender_chest" -> "enderChestContents"
            "off_hand" -> "offHandItem"
            "bed_spawn" -> "bedSpawnLocation"
            "last_location" -> "lastLocation"
            "potion_effects" -> "potions"
            "game_statistics" -> "game_statistics"
            "advancements" -> "advancements"
            "recipes" -> "recipes"
            else -> null
        }
        if (field != null) {
            val value = profile.get(field) ?: return false
            if (value.isJsonNull) return false
            when (share) {
                "inventory_contents", "armor_contents", "ender_chest" -> {
                    require(value.isJsonObject) {
                        "Expected a slot map for '$share' in Multiverse-Inventories profile"
                    }
                }
                "bed_spawn", "last_location" -> {
                    locationObject(value)
                }
                "potion_effects" -> {
                    potionEntries(value).first.forEach { effect ->
                        require(effect.isJsonObject && decodePotionEffect(effect.asJsonObject) != null) {
                            "Could not deserialize potion effect from Multiverse-Inventories profile"
                        }
                    }
                }
                "game_statistics" -> {
                    validateStatistics(value)
                }
                "advancements", "recipes" -> {
                    require(
                        value.isJsonArray &&
                            value.asJsonArray.all { it.isJsonPrimitive && it.asJsonPrimitive.isString },
                    ) {
                        "Expected string entries for '$share' in Multiverse-Inventories profile"
                    }
                }
            }
            return true
        }

        val stat = when (share) {
            "max_hit_points" -> "mhp"
            "hit_points" -> "hp"
            "remaining_air" -> "ra"
            "maximum_air" -> "ma"
            "fall_distance" -> "fd"
            "fire_ticks" -> "ft"
            "xp" -> "xp"
            "total_xp" -> "txp"
            "lvl" -> "el"
            "food_level" -> "fl"
            "exhaustion" -> "ex"
            "saturation" -> "sa"
            else -> error("Unsupported Multiverse-Inventories share: $share")
        }
        val stats = profile.get("stats")?.asJsonObjectOrNull() ?: return false
        val value = stats.get(stat) ?: return false
        require(value.isJsonPrimitive && value.asJsonPrimitive.isNumber) {
            "Expected numeric '$stat' data for '$share' in Multiverse-Inventories profile"
        }
        return true
    }

    private fun applyShare(
        player: Player,
        share: String,
        profile: JsonObject?,
    ) {
        val stats = profile?.get("stats")?.asJsonObjectOrNull()
        when (share) {
            "inventory_contents" -> {
                player.inventory.contents = decodeItems(
                    field = profile?.get("inventoryContents"),
                    size = player.inventory.contents.size,
                )
            }
            "armor_contents" -> {
                player.inventory.armorContents = decodeItems(
                    field = profile?.get("armorContents"),
                    size = player.inventory.armorContents.size,
                )
            }
            "ender_chest" -> {
                player.enderChest.contents = decodeItems(
                    field = profile?.get("enderChestContents"),
                    size = player.enderChest.contents.size,
                )
            }
            "off_hand" -> {
                player.inventory.setItemInOffHand(
                    decodeItem(profile?.get("offHandItem")),
                )
            }
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
            "remaining_air" -> {
                player.remainingAir = stats?.get("ra")?.numberOrNull()?.toInt() ?: 300
            }
            "maximum_air" -> {
                player.maximumAir = stats?.get("ma")?.numberOrNull()?.toInt() ?: 300
            }
            "fall_distance" -> {
                player.fallDistance = stats?.get("fd")?.numberOrNull()?.toFloat() ?: 0.0f
            }
            "fire_ticks" -> {
                player.fireTicks = stats?.get("ft")?.numberOrNull()?.toInt() ?: 0
            }
            "xp" -> {
                player.exp = (stats?.get("xp")?.numberOrNull()?.toFloat() ?: 0.0f).coerceIn(0.0f, 1.0f)
            }
            "total_xp" -> {
                player.totalExperience = stats?.get("txp")?.numberOrNull()?.toInt() ?: 0
            }
            "lvl" -> {
                player.level = stats?.get("el")?.numberOrNull()?.toInt() ?: 0
            }
            "food_level" -> {
                player.foodLevel = stats?.get("fl")?.numberOrNull()?.toInt() ?: 20
            }
            "exhaustion" -> {
                player.exhaustion = stats?.get("ex")?.numberOrNull()?.toFloat() ?: 0.0f
            }
            "saturation" -> {
                player.saturation = stats?.get("sa")?.numberOrNull()?.toFloat() ?: 5.0f
            }
            "bed_spawn" -> {
                applyBedSpawn(player, profile?.get("bedSpawnLocation"))
            }
            "last_location" -> {
                if (config.applyLastLocationForAllTeleports) {
                    applyLastLocation(player, profile?.get("lastLocation"))
                }
            }
            "potion_effects" -> {
                applyPotionEffects(player, profile?.get("potions"))
            }
            "game_statistics" -> {
                applyStatistics(player, profile?.get("game_statistics"))
            }
            "advancements" -> {
                applyAdvancements(player, profile?.get("advancements"))
            }
            "recipes" -> {
                applyRecipes(player, profile?.get("recipes"))
            }
            "economy" -> {
                error("The economy share requires a Vault-compatible economy provider")
            }
            else -> {
                error("Unsupported Multiverse-Inventories share: $share")
            }
        }
    }

    private fun encodeItems(
        items: Array<out ItemStack?>,
        existing: JsonElement?,
    ): JsonObject {
        val result = existing?.asJsonObjectOrNull()?.deepCopy()
            ?: JsonObject()
        items.forEachIndexed { index, item ->
            val key = index.toString()
            result.remove(key)
            val serialized = encodeItem(item)
            if (!serialized.isJsonNull) {
                result.add(key, serialized)
            }
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

    internal fun decodeItems(
        field: JsonElement?,
        size: Int,
    ): Array<ItemStack?> {
        val result = arrayOfNulls<ItemStack>(size)
        val values = field?.takeUnless { it.isJsonNull } ?: return result

        require(values.isJsonObject) {
            "Expected a slot map in Multiverse-Inventories profile"
        }
        values.asJsonObject.entrySet().forEach { (slot, value) ->
            val index = slot.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid ItemStack slot '$slot' in Multiverse-Inventories profile")
            require(index in result.indices) {
                "ItemStack slot '$slot' exceeds the supported inventory size ${result.size}"
            }
            result[index] = decodeItem(value)
        }
        return result
    }

    internal fun decodeItem(value: JsonElement?): ItemStack? {
        if (value == null || value.isJsonNull) return null

        if (value.isJsonPrimitive && value.asJsonPrimitive.isString) {
            val bytes = try {
                Base64.getDecoder().decode(value.asString)
            } catch (failure: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid Base64 ItemStack in Multiverse-Inventories profile", failure)
            }
            return ItemStack.deserializeBytes(bytes)
        }
        require(value.isJsonObject) {
            "Unsupported ItemStack representation in Multiverse-Inventories profile"
        }
        val serialized = value.asJsonObject.toJavaMap()
        return ItemStack.deserialize(serialized)
    }

    private fun encodeStatistics(
        player: Player,
        previous: JsonElement?,
    ): JsonObject {
        val result = previous?.asJsonObjectOrNull()?.deepCopy() ?: JsonObject()
        Statistic.entries.forEach { statistic ->
            when (statistic.type) {
                Statistic.Type.UNTYPED -> {
                    val value = player.getStatistic(statistic)
                    if (value == 0) result.remove(statistic.name) else result.addProperty(statistic.name, value)
                }
                Statistic.Type.ITEM, Statistic.Type.BLOCK -> {
                    val itemStats = result.get(statistic.name)?.asJsonObjectOrNull()?.deepCopy() ?: JsonObject()
                    Material.entries
                        .filter { material ->
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
                    EntityType.entries.filter { it != EntityType.UNKNOWN }.forEach { entityType ->
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
        val data = validateStatistics(value ?: JsonNull.INSTANCE)
        Statistic.entries.forEach { statistic ->
            val stored = data.get(statistic.name)
            when (statistic.type) {
                Statistic.Type.UNTYPED -> {
                    stored?.numberOrNull()?.let { player.setStatistic(statistic, it.toInt()) }
                }
                Statistic.Type.ITEM, Statistic.Type.BLOCK -> {
                    val itemData = stored?.asJsonObjectOrNull()
                    Material.entries
                        .filter { material ->
                            if (statistic.type == Statistic.Type.ITEM) material.isItem else material.isBlock
                        }.forEach { material ->
                            itemData?.get(material.key.toString())?.numberOrNull()?.let { count ->
                                player.setStatistic(statistic, material, count.toInt())
                            }
                        }
                }
                Statistic.Type.ENTITY -> {
                    val entityData = stored?.asJsonObjectOrNull()
                    EntityType.entries.filter { it != EntityType.UNKNOWN }.forEach { entityType ->
                        entityData?.get(entityType.key.toString())?.numberOrNull()?.let { count ->
                            player.setStatistic(statistic, entityType, count.toInt())
                        }
                    }
                }
            }
        }
    }

    private fun validateStatistics(value: JsonElement): JsonObject {
        require(value.isJsonObject) {
            "Expected a statistics object in Multiverse-Inventories profile"
        }
        val data = value.asJsonObject
        Statistic.entries.forEach { statistic ->
            val stored = data.get(statistic.name) ?: return@forEach
            when (statistic.type) {
                Statistic.Type.UNTYPED -> {
                    require(stored.isJsonPrimitive && stored.asJsonPrimitive.isNumber) {
                        "Expected a numeric value for statistic '${statistic.name}'"
                    }
                }
                Statistic.Type.ITEM, Statistic.Type.BLOCK, Statistic.Type.ENTITY -> {
                    require(stored.isJsonObject) {
                        "Expected an object for statistic '${statistic.name}'"
                    }
                    val knownKeys = when (statistic.type) {
                        Statistic.Type.ITEM -> {
                            Material.entries.filter { it.isItem }.map { it.key.toString() }
                        }
                        Statistic.Type.BLOCK -> {
                            Material.entries.filter { it.isBlock }.map { it.key.toString() }
                        }
                        Statistic.Type.ENTITY -> {
                            EntityType.entries
                                .filter { it != EntityType.UNKNOWN }
                                .map { it.key.toString() }
                        }
                        Statistic.Type.UNTYPED -> {
                            emptyList()
                        }
                    }.toSet()
                    stored.asJsonObject.entrySet().forEach { (key, statisticValue) ->
                        if (key in knownKeys) {
                            require(
                                statisticValue.isJsonPrimitive &&
                                    statisticValue.asJsonPrimitive.isNumber,
                            ) {
                                "Expected a numeric value for statistic '${statistic.name}' entry '$key'"
                            }
                        }
                    }
                }
            }
        }
        return data
    }

    private fun encodePotionEffects(
        player: Player,
        previous: JsonElement?,
    ): JsonElement {
        val (previousEffects, useLegacyStringFormat) = potionEntries(previous)
        val effects = previousEffects
            .mapNotNull { element ->
                val serialized = element.takeIf(JsonElement::isJsonObject)?.asJsonObject
                    ?: return@mapNotNull element.deepCopy()
                if (decodePotionEffect(serialized) == null) {
                    element.deepCopy()
                } else {
                    null
                }
            }?.toMutableList() ?: mutableListOf()

        player.activePotionEffects.forEach { effect ->
            if (useLegacyStringFormat) {
                val typeId = effect.type.id
                require(typeId >= 0) {
                    "Potion effect '${effect.type.key}' cannot be represented in the legacy Multiverse-Inventories format"
                }
                effects += JsonObject().apply {
                    addProperty("type", typeId)
                    addProperty("duration", effect.duration)
                    addProperty("amplifier", effect.amplifier)
                }
            } else {
                effects += GSON.toJsonTree(effect.serialize())
            }
        }
        val result = JsonArray().apply { effects.forEach(::add) }
        return if (useLegacyStringFormat) JsonPrimitive(result.toString()) else result
    }

    private fun applyPotionEffects(
        player: Player,
        value: JsonElement?,
    ) {
        val effects = value?.takeUnless { it.isJsonNull } ?: return
        val decodedEffects = potionEntries(effects).first.map { element ->
            require(element.isJsonObject) {
                "Expected a serialized potion effect in Multiverse-Inventories profile"
            }
            decodePotionEffect(element.asJsonObject)
                ?: throw IllegalArgumentException("Could not deserialize potion effect from Multiverse-Inventories profile")
        }
        player.activePotionEffects.forEach {
            player.removePotionEffect(it.type)
        }
        decodedEffects.forEach(player::addPotionEffect)
    }

    private fun decodePotionEffect(value: JsonObject): PotionEffect? {
        val legacyTypeId = value.get("type")?.numberOrNull()?.toInt()
        if (legacyTypeId != null) {
            val type = PotionEffectType.getById(legacyTypeId) ?: return null
            val duration = value.get("duration")?.numberOrNull()?.toInt() ?: return null
            val amplifier = value.get("amplifier")?.numberOrNull()?.toInt() ?: return null
            return PotionEffect(type, duration, amplifier)
        }
        val decoded = ConfigurationSerialization.deserializeObject(value.toJavaMap())
            ?: return null
        return decoded as? PotionEffect
    }

    private fun potionEntries(value: JsonElement?): Pair<List<JsonElement>, Boolean> {
        if (value == null || value.isJsonNull) return emptyList<JsonElement>() to false
        if (value.isJsonArray) {
            return value.asJsonArray.map(JsonElement::deepCopy) to false
        }
        require(value.isJsonPrimitive && value.asJsonPrimitive.isString) {
            "Expected a potion list in Multiverse-Inventories profile"
        }
        val parsed = try {
            JsonParser.parseString(value.asString)
        } catch (failure: RuntimeException) {
            throw IllegalArgumentException("Invalid legacy potion list in Multiverse-Inventories profile", failure)
        }
        require(parsed.isJsonArray) {
            "Expected a potion list in Multiverse-Inventories profile"
        }
        return parsed.asJsonArray.map(JsonElement::deepCopy) to true
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
        val unknownCriteria = previous
            ?.takeIf { it.isJsonArray }
            ?.asJsonArray
            ?.filter { element ->
                !element.isJsonPrimitive || !element.asJsonPrimitive.isString || element.asString !in knownCriteria
            }?.map(JsonElement::deepCopy)
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
        val awarded = value
            ?.takeUnless { it.isJsonNull }
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
        val unknownRecipes = previous
            ?.takeIf { it.isJsonArray }
            ?.asJsonArray
            ?.filter { element ->
                !element.isJsonPrimitive || !element.asJsonPrimitive.isString || element.asString !in knownRecipes
            }?.map(JsonElement::deepCopy)
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
        val discovered = value
            ?.takeUnless { it.isJsonNull }
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
        location: Location?,
        previous: JsonElement?,
        respawn: Boolean,
    ): JsonElement {
        if (location?.world == null) {
            return JsonNull.INSTANCE
        }
        val result = previous?.asJsonObjectOrNull()?.deepCopy() ?: JsonObject()
        result.addProperty("==", if (respawn) "RespawnLocation" else "UnloadedWorldLocation")
        result.addProperty("world", location.world!!.name)
        result.addProperty("x", location.x)
        result.addProperty("y", location.y)
        result.addProperty("z", location.z)
        result.addProperty("yaw", location.yaw)
        result.addProperty("pitch", location.pitch)
        if (respawn) {
            val respawnType = when {
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
        val map = value?.takeUnless { it.isJsonNull }?.let(::locationObject) ?: return
        val worldName = map.get("world")?.takeIf { it.isJsonPrimitive }?.asString
            ?: map.get("wo")?.takeIf { it.isJsonPrimitive }?.asString
        if (worldName == null) {
            logger.warning("Stored Multiverse-Inventories respawn location has no world")
            return
        }
        val world = Bukkit.getWorld(worldName) ?: run {
            logger.warning("Cannot apply Multiverse-Inventories respawn location: world '$worldName' is unavailable")
            return
        }
        val location = locationFromMap(world, map)
        if (config.validateBedAnchorRespawnLocation) {
            val type = map
                .get("respawnType")
                ?.takeIf { it.isJsonPrimitive }
                ?.asString
                ?.uppercase()
            val block = world.getBlockAt(location)
            val valid = when (type) {
                "BED" -> block.blockData is Bed
                "ANCHOR" -> block.type == Material.RESPAWN_ANCHOR
                null, "UNKNOWN" -> true
                else -> false
            }
            if (!valid) {
                return
            }
        }
        player.setBedSpawnLocation(location, true)
    }

    private fun parseLocation(value: JsonElement?): Location? {
        val map = locationObject(value) ?: return null
        val worldName = map.get("world")?.takeIf { it.isJsonPrimitive }?.asString
            ?: map.get("wo")?.takeIf { it.isJsonPrimitive }?.asString
            ?: return null
        val world = Bukkit.getWorld(worldName) ?: run {
            logger.warning("Cannot apply Multiverse-Inventories last location: world '$worldName' is unavailable")
            return null
        }
        return locationFromMap(world, map)
    }

    private fun locationFromMap(
        world: org.bukkit.World,
        map: JsonObject,
    ): Location {
        val x = map.get("x")?.numberOrNull()
            ?: throw IllegalArgumentException("Stored Multiverse-Inventories location has no numeric x coordinate")
        val y = map.get("y")?.numberOrNull()
            ?: throw IllegalArgumentException("Stored Multiverse-Inventories location has no numeric y coordinate")
        val z = map.get("z")?.numberOrNull()
            ?: throw IllegalArgumentException("Stored Multiverse-Inventories location has no numeric z coordinate")
        return Location(
            world,
            x.toDouble(),
            y.toDouble(),
            z.toDouble(),
            (map.get("yaw") ?: map.get("ya"))?.numberOrNull()?.toFloat() ?: 0.0f,
            (map.get("pitch") ?: map.get("pi"))?.numberOrNull()?.toFloat() ?: 0.0f,
        )
    }

    private fun locationObject(value: JsonElement?): JsonObject? {
        if (value == null || value.isJsonNull) return null

        val parsed = if (value.isJsonPrimitive && value.asJsonPrimitive.isString) {
            JsonParser.parseString(value.asString)
        } else {
            value
        }
        require(parsed.isJsonObject) {
            "Expected a serialized location in Multiverse-Inventories profile"
        }
        return parsed.asJsonObject
    }

    private fun stat(
        key: String,
        value: Number,
    ) = Triple("stats", key, GSON.toJsonTree(value))

    private fun groupsFor(worldName: String): List<ResolvedInventoryGroup> {
        val configuredGroups = repository.readWorldGroups()
        val unsupported = (
            configuredGroups.flatMap { it.shares + it.disabledShares } +
                config.activeOptionalShares
        ).flatMap(MultiverseInventoryShares::expand)
            .filterNot { it in supportedShares }
            .toSet()
        warnUnsupportedShares(unsupported)

        return MultiverseInventoryShares
            .resolveGroups(config, configuredGroups, worldName)
            .map { group ->
                group.copy(
                    shares = group.shares intersect supportedShares,
                    disabledShares = group.disabledShares intersect supportedShares,
                )
            }
    }

    private fun readProfileForTransition(
        location: InventoryProfileLocation,
        profileType: InventoryProfileType,
        player: Player,
        shares: Set<String>,
        operation: String,
    ): JsonObject? {
        val result = try {
            repository.readProfileResult(location)
        } catch (failure: Exception) {
            logger.log(
                Level.SEVERE,
                "Could not read inventory profile ${location.directory}/${location.key}/" +
                    "${location.playerName}.json for $profileType while $operation shares " +
                    "${shares.sorted().joinToString()} for player ${player.name}",
                failure,
            )
            throw failure
        }
        return when (result) {
            InventoryProfileReadResult.Missing -> {
                logger.fine(
                    "Inventory profile ${location.directory}/${location.key}/${location.playerName}.json " +
                        "does not exist; preserving current player shares during $operation",
                )
                null
            }
            is InventoryProfileReadResult.Present -> {
                result.profile
            }
            is InventoryProfileReadResult.Malformed -> {
                logger.log(
                    Level.SEVERE,
                    "Malformed inventory profile ${location.directory}/${location.key}/" +
                        "${location.playerName}.json for $profileType while $operation shares " +
                        "${shares.sorted().joinToString()} for player ${player.name}",
                    result.failure,
                )
                throw result.failure
            }
            is InventoryProfileReadResult.Unreadable -> {
                logger.log(
                    Level.SEVERE,
                    "Unreadable inventory profile ${location.directory}/${location.key}/" +
                        "${location.playerName}.json for $profileType while $operation shares " +
                        "${shares.sorted().joinToString()} for player ${player.name}",
                    result.failure,
                )
                throw result.failure
            }
        }
    }

    private fun activeShares(): Set<String> {
        val active = MultiverseInventoryShares.active(config)
        warnUnsupportedShares(active - supportedShares)
        return active intersect supportedShares
    }

    private fun worldReadShares(groups: List<ResolvedInventoryGroup>): Set<String> =
        if (groups.isEmpty() && !config.useOptionalsForUngroupedWorlds) {
            activeShares().intersect(MultiverseInventoryShares.standard)
        } else {
            activeShares()
        }

    private fun shareOwnership(
        groups: List<ResolvedInventoryGroup>,
        worldName: String,
    ): InventoryShareOwnership {
        val ownership = MultiverseInventoryShares.resolveShareOwnership(groups)
        ownership.conflictingShares.forEach { (share, groupNames) ->
            val key = "$worldName:$share:${groupNames.sorted().joinToString(",")}"
            if (reportedShareConflicts.add(key)) {
                logger.warning(
                    "Skipping Multiverse-Inventories share '$share' in world '$worldName': " +
                        "multiple groups claim it (${groupNames.sorted().joinToString()})",
                )
            }
        }
        return ownership
    }

    private fun worldOwnedShares(
        groups: List<ResolvedInventoryGroup>,
        ownership: InventoryShareOwnership,
    ): Set<String> = worldReadShares(groups) -
        ownership.groupOwners.keys -
        ownership.disabledShares -
        ownership.conflictingShares.keys

    private fun sharesForRead(shares: Set<String>): Set<String> = if (config.applyLastLocationForAllTeleports) {
        shares
    } else {
        shares - "last_location"
    }

    private fun warnUnsupportedShares(shares: Collection<String>) {
        shares.forEach { share ->
            if (reportedUnsupportedShares.add(share)) {
                logger.warning(
                    "Multiverse-Inventories share '$share' is configured but unsupported by PCBridge; " +
                        "other inventory shares remain enabled",
                )
            }
        }
    }

    private fun profileType(gameMode: GameMode): InventoryProfileType = if (config.enableGamemodeShareHandling) {
        InventoryProfileType.valueOf(gameMode.name)
    } else {
        InventoryProfileType.SURVIVAL
    }

    private fun groupProfile(
        group: ResolvedInventoryGroup,
        playerName: String,
    ) = InventoryProfileLocation("groups", group.name, playerName)

    private fun worldProfile(
        worldName: String,
        playerName: String,
    ) = InventoryProfileLocation("worlds", worldName, playerName)

    private fun requirePrimaryThread() {
        check(Bukkit.isPrimaryThread()) {
            "Inventory transitions must run on the main server thread"
        }
    }

    private fun JsonElement?.asJsonObjectOrNull(): JsonObject? {
        if (this == null || this.isJsonNull) return null
        require(this.isJsonObject) {
            "Expected an object in Multiverse-Inventories profile"
        }
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
        val lastLocation: Location? = null,
    )

    private data class ProfileRead(
        val location: InventoryProfileLocation,
        val profileType: InventoryProfileType,
        val shares: Set<String>,
    )

    private companion object {
        val GSON = Gson()
        val supportedShares = MultiverseInventoryShares.all - "economy"
        val applyOrder = listOf(
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
