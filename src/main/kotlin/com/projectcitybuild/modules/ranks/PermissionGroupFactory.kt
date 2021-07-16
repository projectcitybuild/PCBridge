package com.projectcitybuild.modules.ranks

import com.projectcitybuild.core.entities.BuildGroup
import com.projectcitybuild.core.entities.DonorGroup
import com.projectcitybuild.core.entities.Group
import com.projectcitybuild.core.entities.TrustGroup

class PermissionGroupFactory {

    /**
     * Converts a Group enum into its Permission plugin
     * (eg. LuckyPerms) group name
     */
    fun fromGroup(group: Group): String? {
        when (group) {
            is Group.BUILD -> return when (group.subgroup) {
                BuildGroup.NONE -> null
                BuildGroup.INTERN -> "intern"
                BuildGroup.BUILDER -> "builder"
                BuildGroup.PLANNER -> "planner"
                BuildGroup.ENGINEER -> "engineer"
                BuildGroup.ARCHITECT -> "architect"
            }
            is Group.TRUST -> return when (group.subgroup) {
                TrustGroup.GUEST -> null
                TrustGroup.MEMBER -> "member"
                TrustGroup.TRUSTED -> "trusted"
                TrustGroup.TRUSTED_PLUS -> "trusted+"
                TrustGroup.MODERATOR -> "moderator"
                TrustGroup.OPERATOR -> "op"
                TrustGroup.SENIOR_OPERATOR -> "sop"
                TrustGroup.ADMINISTRATOR -> "admin"
                TrustGroup.RETIRED -> "retired"
            }
            is Group.DONOR -> return when (group.subgroup) {
                DonorGroup.NONE -> null
                DonorGroup.DONOR -> "donator"
            }
        }
    }

    fun fromPermissionGroup(groupName: String): Group? {
        return when (groupName) {
            "intern" -> Group.BUILD(BuildGroup.INTERN)
            "builder" -> Group.BUILD(BuildGroup.BUILDER)
            "planner" -> Group.BUILD(BuildGroup.PLANNER)
            "engineer" -> Group.BUILD(BuildGroup.ENGINEER)
            "architect" -> Group.BUILD(BuildGroup.ARCHITECT)

            "member" -> Group.TRUST(TrustGroup.MEMBER)
            "trusted" -> Group.TRUST(TrustGroup.TRUSTED)
            "trusted+" -> Group.TRUST(TrustGroup.TRUSTED_PLUS)
            "moderator" -> Group.TRUST(TrustGroup.MODERATOR)
            "op" -> Group.TRUST(TrustGroup.OPERATOR)
            "sop" -> Group.TRUST(TrustGroup.SENIOR_OPERATOR)
            "admin" -> Group.TRUST(TrustGroup.ADMINISTRATOR)
            "retired" -> Group.TRUST(TrustGroup.RETIRED)

            "donator" -> Group.DONOR(DonorGroup.DONOR)

            else -> null
        }
    }

    fun fromAPIGroup(groupName: String): Group? {
        return when (groupName) {

            "intern" -> Group.BUILD(BuildGroup.INTERN)
            "builder" -> Group.BUILD(BuildGroup.BUILDER)
            "planner" -> Group.BUILD(BuildGroup.PLANNER)
            "engineer" -> Group.BUILD(BuildGroup.ENGINEER)
            "architect" -> Group.BUILD(BuildGroup.ARCHITECT)

            "member" -> Group.TRUST(TrustGroup.MEMBER)
            "trusted" -> Group.TRUST(TrustGroup.TRUSTED)
            "trusted plus" -> Group.TRUST(TrustGroup.TRUSTED_PLUS)
            "moderator" -> Group.TRUST(TrustGroup.MODERATOR)
            "operator" -> Group.TRUST(TrustGroup.OPERATOR)
            "senior operator" -> Group.TRUST(TrustGroup.SENIOR_OPERATOR)
            "administrator" -> Group.TRUST(TrustGroup.ADMINISTRATOR)
            "retired" -> Group.TRUST(TrustGroup.RETIRED)

            "donator" -> Group.DONOR(DonorGroup.DONOR)

            else -> null
        }
    }
}