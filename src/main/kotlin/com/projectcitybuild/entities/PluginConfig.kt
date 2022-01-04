package com.projectcitybuild.entities

sealed class PluginConfig {

    open class Pair<T>(val key: String, val defaultValue: T)

    sealed class API {
        companion object {
            val KEY: Pair<String>
                get() = Pair(
                    key = "api.key",
                    defaultValue = "FILL_THIS_IN"
                )

            val BASE_URL: Pair<String>
                get() = Pair(
                    key = "api.base_url",
                    defaultValue = "https://projectcitybuild.com/api/"
                )

            val IS_LOGGING_ENABLED: Pair<Boolean>
                get() = Pair(
                    key = "api.is_logging_enabled",
                    defaultValue = false
                )
        }
    }

    sealed class GROUPS {

        companion object {
            val GUEST: Pair<String>
                get() = Pair(
                        key = "groups.guest",
                        defaultValue = "guest"
                )

            val BUILD_PRIORITY: Pair<ArrayList<String>>
                get() = Pair(
                    key = "groups.build_priority",
                    defaultValue = arrayListOf(
                        "architect",
                        "engineer",
                        "planner",
                        "builder",
                        "intern"
                    )
                )

            val TRUST_PRIORITY: Pair<ArrayList<String>>
                get() = Pair(
                    key = "groups.trust_priority",
                    defaultValue = arrayListOf(
                        "admin",
                        "sop",
                        "op",
                        "moderator",
                        "trusted+",
                        "trusted",
                        "member"
                    )
                )

            val DONOR_PRIORITY: Pair<ArrayList<String>>
                get() = Pair(
                    key = "groups.donor_priority",
                    defaultValue = arrayListOf(
                        "donator",
                        "legacy-donator"
                    )
                )
        }
    }
}