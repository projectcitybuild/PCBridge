package com.projectcitybuild.features.hub.usecases

import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.SerializableLocation
import com.projectcitybuild.repositories.HubRepository
import org.bukkit.Location
import javax.inject.Inject

class SetHubUseCase @Inject constructor(
    private val hubRepository: HubRepository,
) {
    fun execute(location: Location): Result<Unit, Unit> {
        hubRepository.set(SerializableLocation(
            location.world.name,
            location.x,
            location.y,
            location.z,
            location.pitch,
            location.yaw,
        ))
        return Success(Unit)
    }
}