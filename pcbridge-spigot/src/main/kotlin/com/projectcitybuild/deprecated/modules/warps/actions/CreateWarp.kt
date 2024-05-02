// package com.projectcitybuild.modules.warps.actions
//
// import com.projectcitybuild.data.SerializableLocation
// import com.projectcitybuild.features.warps.Warp
// import com.projectcitybuild.entities.events.WarpCreateEvent
// import com.projectcitybuild.pcbridge.core.modules.datetime.time.Time
// import com.projectcitybuild.pcbridge.core.utils.Failure
// import com.projectcitybuild.pcbridge.core.utils.Result
// import com.projectcitybuild.pcbridge.core.utils.Success
// import com.projectcitybuild.repositories.WarpRepository
// import com.projectcitybuild.support.spigot.eventbroadcast.LocalEventBroadcaster
//
// class CreateWarp(
//     private val warpRepository: WarpRepository,
//     private val localEventBroadcaster: LocalEventBroadcaster,
//     private val time: Time,
// ) {
//     enum class FailureReason {
//         WARP_ALREADY_EXISTS,
//     }
//
//     fun createWarp(name: String, location: SerializableLocation): Result<Unit, FailureReason> {
//         if (warpRepository.exists(name)) {
//             return Failure(FailureReason.WARP_ALREADY_EXISTS)
//         }
//         val warp = Warp(
//             name,
//             location,
//             time.now()
//         )
//         warpRepository.add(warp)
//
//         localEventBroadcaster.emit(WarpCreateEvent())
//
//         return Success(Unit)
//     }
// }
