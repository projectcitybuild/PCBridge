package com.projectcitybuild.pcbridge.paper.architecture.serverlist.decorators

class ServerListingDecoratorChain(
    private val decorators: MutableList<ServerListingDecorator> = mutableListOf(),
) {
    fun register(vararg decorators: ServerListingDecorator)
        = decorators.forEach { this.decorators.add(it) }

    suspend fun pipe(listing: ServerListing): ServerListing {
        var updated = listing
        for (decorator in decorators) {
            updated = decorator.decorate(updated)
        }
        return updated
    }
}
