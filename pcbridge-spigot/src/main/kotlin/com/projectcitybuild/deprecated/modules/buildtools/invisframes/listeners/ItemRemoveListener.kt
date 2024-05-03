// package com.projectcitybuild.modules.buildtools.invisframes.listeners
//
// import com.projectcitybuild.support.spigot.SpigotNamespace
// import com.projectcitybuild.support.spigot.listeners.SpigotListener
// import org.bukkit.entity.GlowItemFrame
// import org.bukkit.entity.ItemFrame
// import org.bukkit.event.EventHandler
// import org.bukkit.event.entity.EntityDamageByEntityEvent
// import org.bukkit.persistence.PersistentDataType
//
// class ItemRemoveListener(
//     private val spigotNamespace: SpigotNamespace,
// ) : SpigotListener<EntityDamageByEntityEvent> {
//
//     @EventHandler
//     override suspend fun handle(event: EntityDamageByEntityEvent) {
//         val entity = event.entity
//
//         val isItemFrame = entity is ItemFrame
//         if (!isItemFrame) return
//
//         val isInvisibleFrame = entity.persistentDataContainer.getOrDefault(
//             spigotNamespace.invisibleKey,
//             PersistentDataType.BYTE,
//             0,
//         ) == 1.toByte()
//
//         if (isInvisibleFrame) {
//             val itemFrame = entity as ItemFrame
//             itemFrame.isVisible = true
//             itemFrame.isGlowing = entity is GlowItemFrame
//         }
//     }
// }
