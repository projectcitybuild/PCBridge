rootProject.name = "pcbridge"

include(
    "core:observability",
    "core:http",
    "core:utils",
    "core:file-storage",
    "core:local-config",
    "core:discord",
    "core:date-time",
    "core:pagination",
    "core:support:component",
    "core:support:java",
    "core:l10n",
    "core:pcbridge-api",
    "core:permissions",
    "core:support:tests",
    "core:validation",
)

include(
    "platform:paper",
    "platform:web-server",
)

include("app:paper-plugin")

include("runtime")

include(
    "integrations:dynmap",
    "integrations:luckperms",
    "integrations:essentials",
)

include(
    "features:announcements",
    "features:bans",
    "features:building",
    "features:builds",
    "features:chat-badge",
    "features:chat-formatting",
    "features:config",
    "features:homes",
    "features:maintenance",
    "features:moderate",
    "features:onboarding",
    "features:pim",
    "features:register",
    "features:roles",
    "features:server-links",
    "features:spawns",
    "features:staff-chat",
    "features:stats",
    "features:sync",
    "features:teleportation",
    "features:warnings",
    "features:warps",
    "features:watchdog",
    "features:workstations",
)
