// package com.projectcitybuild.modules.ranksync.listener
//
// import com.projectcitybuild.entities.events.ConnectionPermittedEvent
// import com.projectcitybuild.modules.ranksync.actions.SyncPlayerGroupsWithAggregate
// import com.projectcitybuild.support.spigot.listeners.SpigotListener
// import org.bukkit.event.EventHandler
//
// class SyncPlayerGroupsOnJoinListener(
//     private val syncPlayerGroupsWithAggregate: SyncPlayerGroupsWithAggregate,
// ) : SpigotListener<ConnectionPermittedEvent> {
//
//     @EventHandler
//     override suspend fun handle(event: ConnectionPermittedEvent) {
//         syncPlayerGroupsWithAggregate.execute(
//             playerUUID = event.playerUUID,
//             aggregate = event.aggregate,
//         )
//     }
// }