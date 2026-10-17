package com.projectcitybuild.pcbridge.features.roles.domain

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Role
import com.projectcitybuild.pcbridge.features.roles.domain.data.RoleType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RolesFilterTest {
    private val rolesFilter = RolesFilter()

    @Test
    fun `returns no roles for an empty set`() {
        assertTrue(rolesFilter.filter(emptySet()).isEmpty())
    }

    @Test
    fun `selects the highest priority role for each role type`() {
        val lowPriorityDonor = role(name = "donor-low", roleType = "donor", priority = 1)
        val highPriorityDonor = role(name = "donor-high", roleType = "DONOR", priority = 5)
        val staff = role(name = "staff", roleType = "staff", priority = 2)

        val result = rolesFilter.filter(setOf(lowPriorityDonor, highPriorityDonor, staff))

        assertEquals(
            mapOf(
                RoleType.DONOR to highPriorityDonor,
                RoleType.STAFF to staff,
            ),
            result,
        )
    }

    @Test
    fun `ignores roles with missing fields or an unknown role type`() {
        val incompleteType = role(name = "no-type", roleType = null, priority = 1)
        val incompletePriority = role(name = "no-priority", roleType = "staff", priority = null)
        val unknownType = role(name = "unknown", roleType = "unknown", priority = 10)

        val result = rolesFilter.filter(setOf(incompleteType, incompletePriority, unknownType))

        assertTrue(result.isEmpty())
    }

    private fun role(
        name: String,
        roleType: String?,
        priority: Int?,
    ) = Role(
        id = name.hashCode(),
        name = name,
        minecraftName = null,
        hoverText = null,
        displayName = name,
        roleType = roleType,
        displayPriority = priority,
    )
}
