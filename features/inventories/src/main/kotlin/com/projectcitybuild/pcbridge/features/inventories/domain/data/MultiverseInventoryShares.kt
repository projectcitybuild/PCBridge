package com.projectcitybuild.pcbridge.features.inventories.domain.data

data class ResolvedInventoryGroup(
    val name: String,
    val worlds: Set<String>,
    val shares: Set<String>,
    val disabledShares: Set<String>,
)

object MultiverseInventoryShares {
    val standard = setOf(
        "inventory_contents",
        "armor_contents",
        "ender_chest",
        "off_hand",
        "max_hit_points",
        "hit_points",
        "remaining_air",
        "maximum_air",
        "fall_distance",
        "fire_ticks",
        "xp",
        "total_xp",
        "lvl",
        "food_level",
        "exhaustion",
        "saturation",
        "bed_spawn",
        "potion_effects",
    )

    val optional = setOf(
        "last_location",
        "economy",
        "advancements",
        "game_statistics",
        "recipes",
    )

    val all = standard + optional

    private val shareGroups = mapOf(
        "inventory" to setOf("inventory_contents", "armor_contents", "ender_chest", "off_hand"),
        "experience" to setOf("xp", "total_xp", "lvl"),
        "air" to setOf("remaining_air", "maximum_air"),
        "hunger" to setOf("food_level", "saturation", "exhaustion"),
        "health" to setOf(
            "hit_points",
            "max_hit_points",
            "remaining_air",
            "maximum_air",
            "fall_distance",
            "fire_ticks",
        ),
        "stats" to setOf(
            "hit_points",
            "max_hit_points",
            "food_level",
            "saturation",
            "exhaustion",
            "xp",
            "total_xp",
            "lvl",
            "remaining_air",
            "maximum_air",
            "fall_distance",
            "fire_ticks",
            "potion_effects",
            "game_statistics",
            "advancements",
        ),
        "all" to all,
        "*" to all,
    )

    private val aliases = mapOf(
        "inv" to "inventory",
        "inventories" to "inventory",
        "armor" to "armor_contents",
        "ender" to "ender_chest",
        "ender-chest" to "ender_chest",
        "offhand" to "off_hand",
        "shield" to "off_hand",
        "maxhealth" to "max_hit_points",
        "maxhp" to "max_hit_points",
        "maxhitpoints" to "max_hit_points",
        "hp" to "hit_points",
        "hitpoints" to "hit_points",
        "breath" to "air",
        "falling" to "fall_distance",
        "fire" to "fire_ticks",
        "burning" to "fire_ticks",
        "exp" to "experience",
        "level" to "experience",
        "food" to "food_level",
        "exhaust" to "exhaustion",
        "exh" to "exhaustion",
        "sat" to "saturation",
        "bedspawn" to "bed_spawn",
        "bed" to "bed_spawn",
        "beds" to "bed_spawn",
        "bedspawns" to "bed_spawn",
        "loc" to "last_location",
        "location" to "last_location",
        "pos" to "last_location",
        "position" to "last_location",
        "money" to "economy",
        "econ" to "economy",
        "cash" to "economy",
        "balance" to "economy",
        "potion" to "potion_effects",
        "potions" to "potion_effects",
        "achievements" to "advancements",
        "game_stats" to "game_statistics",
    )

    fun expand(name: String): Set<String> {
        val normalized = name.trim().lowercase()
        require(normalized.isNotEmpty()) { "Multiverse-Inventories share names must not be blank" }
        val canonical = aliases[normalized] ?: normalized
        return shareGroups[canonical] ?: setOf(canonical).also {
            require(canonical in all) { "Unsupported Multiverse-Inventories share: $name" }
        }
    }

    fun active(config: MultiverseInventoryConfig): Set<String> =
        standard + config.activeOptionalShares.flatMap(::expand).intersect(optional)

    fun resolveGroups(
        config: MultiverseInventoryConfig,
        groups: List<InventoryWorldGroup>,
        worldName: String,
    ): List<ResolvedInventoryGroup> {
        val active = active(config)
        val configured = groups.filter { group ->
            group.worlds.any { worldMatches(it, worldName) }
        }
        if (configured.isNotEmpty()) {
            return configured.map { group ->
                val groupShares = group.shares
                    .flatMap(::expand)
                    .toSet()
                    .intersect(active)
                val disabled = group.disabledShares
                    .flatMap(::expand)
                    .toSet()
                    .intersect(active)
                ResolvedInventoryGroup(
                    name = group.name,
                    worlds = group.worlds,
                    shares = groupShares - disabled,
                    disabledShares = disabled,
                )
            }
        }

        return if (config.defaultUngroupedWorlds) {
            listOf(
                ResolvedInventoryGroup(
                    name = "default",
                    worlds = setOf(worldName),
                    shares = active,
                    disabledShares = emptySet(),
                ),
            )
        } else {
            emptyList()
        }
    }

    private fun worldMatches(
        pattern: String,
        worldName: String,
    ): Boolean {
        if (pattern.equals(worldName, ignoreCase = true)) return true
        if (!pattern.contains('*') && !pattern.contains('?')) return false
        val regex = buildString {
            append('^')
            pattern.forEach { character ->
                when (character) {
                    '*' -> append(".*")
                    '?' -> append('.')
                    else -> append(Regex.escape(character.toString()))
                }
            }
            append('$')
        }
        return Regex(regex, RegexOption.IGNORE_CASE).matches(worldName)
    }
}
