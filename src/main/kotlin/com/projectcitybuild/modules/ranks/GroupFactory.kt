package com.projectcitybuild.modules.ranks

import com.projectcitybuild.core.entities.TrustGroup
import com.projectcitybuild.core.entities.BuildGroup
import com.projectcitybuild.core.entities.DonorGroup
import com.projectcitybuild.core.entities.Group

class GroupFactory {

    fun fromAPI(groupName: String): Group? {
        return when (groupName) {
            "member" -> Group.TRUST(TrustGroup.MEMBER)
            "trusted" -> Group.TRUST(TrustGroup.TRUSTED)
            "trusted plus" -> Group.TRUST(TrustGroup.TRUSTED_PLUS)
            "retired" -> Group.TRUST(TrustGroup.RETIRED)
            "moderator" -> Group.TRUST(TrustGroup.MODERATOR)
            "operator" -> Group.TRUST(TrustGroup.OPERATOR)
            "senior operator" -> Group.TRUST(TrustGroup.SENIOR_OPERATOR)
            "administrator" -> Group.TRUST(TrustGroup.ADMINISTRATOR)
            "intern" -> Group.BUILD(BuildGroup.INTERN)
            "builder" -> Group.BUILD(BuildGroup.BUILDER)
            "planner" -> Group.BUILD(BuildGroup.PLANNER)
            "engineer" -> Group.BUILD(BuildGroup.ENGINEER)
            "architect" -> Group.BUILD(BuildGroup.ARCHITECT)
            "donator" -> Group.DONOR(DonorGroup.DONOR)
            else -> null
        }
    }
}