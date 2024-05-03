// package com.projectcitybuild.modules.buildtools.invisframes.listeners
//
// import com.projectcitybuild.support.spigot.SpigotNamespace
// import com.projectcitybuild.support.spigot.listeners.SpigotListener
// import org.bukkit.entity.ItemFrame
// import org.bukkit.event.EventHandler
// import org.bukkit.event.hanging.HangingPlaceEvent
// import org.bukkit.persistence.PersistentDataType
//
// class FramePlaceListener(
//     private val spigotNamespace: SpigotNamespace,
// ) : SpigotListener<HangingPlaceEvent> {
//
//     @EventHandler
//     override suspend fun handle(event: HangingPlaceEvent) {
//         val entity = event.entity
//
//         val isItemFrame = entity is ItemFrame
//         if (!isItemFrame) return
//
//         val invisibleValue = event.itemStack?.itemMeta?.persistentDataContainer?.getOrDefault(
//             spigotNamespace.invisibleKey,
//             PersistentDataType.BYTE,
//             0,
//         ) ?: 0.toByte()
//         val isInvisibleFrame = invisibleValue == 1.toByte()
//
//         if (isInvisibleFrame) {
//             event.entity.persistentDataContainer.set(
//                 spigotNamespace.invisibleKey,
//                 PersistentDataType.BYTE,
//                 1,
//             )
//         }
//     }
// }
