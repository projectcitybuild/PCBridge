package com.projectcitybuild.pcbridge.runtime.serverlist.decorators

interface ServerListingDecorator {
    suspend fun decorate(prev: ServerListing): ServerListing
}
