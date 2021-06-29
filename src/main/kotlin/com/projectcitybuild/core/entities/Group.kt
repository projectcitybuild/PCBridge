package com.projectcitybuild.core.entities

enum class BuildGroup {
    NONE,
    INTERN,
    BUILDER,
    PLANNER,
    ENGINEER,
    ARCHITECT,
}

enum class TrustGroup {
    GUEST,
    MEMBER,
    TRUSTED,
    TRUSTED_PLUS,
    MODERATOR,
    OPERATOR,
    SENIOR_OPERATOR,
    ADMINISTRATOR,
    RETIRED,
}

enum class DonorGroup {
    NONE,
    DONOR,
}

sealed class Group {
    class BUILD(val subgroup: BuildGroup): Group()
    class TRUST(val subgroup: TrustGroup): Group()
    class DONOR(val subgroup: DonorGroup): Group()
}