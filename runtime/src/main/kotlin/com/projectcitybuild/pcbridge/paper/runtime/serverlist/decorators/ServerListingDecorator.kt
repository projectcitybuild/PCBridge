package com.projectcitybuild.pcbridge.paper.runtime.serverlist.decorators

interface ServerListingDecorator {
    suspend fun decorate(prev: ServerListing): ServerListing
}
