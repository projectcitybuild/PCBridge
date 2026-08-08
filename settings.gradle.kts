rootProject.name = "pcbridge"

include("pcbridge-http")
include("pcbridge-paper")
include("pcbridge-web-server")

include("pcbridge-core")
include("pcbridge-core:datetime")
include("pcbridge-core:storage")
include("pcbridge-core:observability")
include("pcbridge-core:localconfig")
include("pcbridge-core:pagination")
include("pcbridge-core:remoteconfig")
include("pcbridge-core:store")