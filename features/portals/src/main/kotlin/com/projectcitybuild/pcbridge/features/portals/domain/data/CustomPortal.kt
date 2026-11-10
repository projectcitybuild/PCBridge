package com.projectcitybuild.pcbridge.features.portals.domain.data

import org.bukkit.Location
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

data class PortalRegion(
    val worldName: String,
    val minX: Int,
    val minY: Int,
    val minZ: Int,
    val maxX: Int,
    val maxY: Int,
    val maxZ: Int,
) {
    fun contains(location: Location): Boolean {
        val world = location.world ?: return false
        return world.name == worldName &&
            location.blockX in minX..maxX &&
            location.blockY in minY..maxY &&
            location.blockZ in minZ..maxZ
    }

    fun serialize(): String = "$worldName:$minX,$minY,$minZ:$maxX,$maxY,$maxZ"

    companion object {
        fun fromCorners(
            worldName: String,
            first: Location,
            second: Location,
        ): PortalRegion {
            require(first.world?.name == worldName && second.world?.name == worldName) {
                "Portal selection must use one world"
            }
            return PortalRegion(
                worldName,
                min(first.blockX, second.blockX),
                min(first.blockY, second.blockY),
                min(first.blockZ, second.blockZ),
                max(first.blockX, second.blockX),
                max(first.blockY, second.blockY),
                max(first.blockZ, second.blockZ),
            )
        }

        fun parse(serialized: String): PortalRegion? {
            val parts = serialized.split(':')
            if (parts.size == 2) return null
            if (parts.size != 3 || parts[0].isBlank()) return null
            val first = parseVector(parts[1]) ?: return null
            val second = parseVector(parts[2]) ?: return null
            return PortalRegion(
                parts[0],
                min(first[0], second[0]),
                min(first[1], second[1]),
                min(first[2], second[2]),
                max(first[0], second[0]),
                max(first[1], second[1]),
                max(first[2], second[2]),
            )
        }

        private fun parseVector(value: String): List<Int>? {
            val coordinates = value.split(',')
            if (coordinates.size != 3) return null
            return coordinates.map { coordinate ->
                val parsed = coordinate.toDoubleOrNull()?.takeIf(Double::isFinite) ?: return null
                floor(parsed).toInt()
            }
        }
    }
}

data class CustomPortal(
    val name: String,
    val region: PortalRegion,
    val destination: String,
    val actionType: String,
    val owner: String,
    val safeTeleport: Boolean,
    val teleportNonPlayers: Boolean,
    val entryFee: Double,
    val noPermissionMessage: String,
    val actionSuccessMessage: String,
)
