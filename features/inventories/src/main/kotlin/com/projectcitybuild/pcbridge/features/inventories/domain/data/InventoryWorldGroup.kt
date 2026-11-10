package com.projectcitybuild.pcbridge.features.inventories.domain.data

data class InventoryWorldGroup(
    val name: String,
    val worlds: Set<String>,
    val shares: Set<String>,
    val disabledShares: Set<String>,
    val spawnWorld: String?,
    val spawnPriority: Int?,
)

data class InventoryGroupConflict(
    val world: String,
    val share: String,
    val groups: Set<String>,
)
