rootProject.name = "pcbridge"

include("core:observability")
include("core:http")
include("core:utils")
include("core:storage")
include("core:localconfig")
include("core:discord")
include("core:datetime")
include("core:pagination")
include("core:support:component")
include("core:support:java")
include("core:l10n")
include("core:pcbridge-api")
include("core:permissions")
include("core:test-support")

include("platform:paper")
include("platform:web-server")

include("app:paper-plugin")

include("runtime")

include("integrations:dynmap")
include("integrations:luckperms")
include("integrations:essentials")

include(
    "features:announcements",
    "features:bans",
    "features:building",
    "features:builds",
    "features:chatbadge",
    "features:chatformatting",
    "features:config",
    "features:homes",
    "features:maintenance",
    "features:moderate",
    "features:onboarding",
    "features:pim",
    "features:randomteleport",
    "features:register",
    "features:roles",
    "features:serverlinks",
    "features:spawns",
    "features:staffchat",
    "features:stats",
    "features:sync",
    "features:warnings",
    "features:warps",
    "features:watchdog",
    "features:workstations",
)
