package com.projectcitybuild.pcbridge.paper.architecture.serverlist.decorators

interface ServerListingDecorator {
    suspend fun decorate(prev: ServerListing): ServerListing
}