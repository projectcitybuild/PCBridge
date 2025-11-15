package com.projectcitybuild.pcbridge.paper.integrations.dynmap

import org.dynmap.DynmapCommonAPI
import org.dynmap.DynmapCommonAPIListener

class DynmapDelegate: DynmapCommonAPIListener() {
    private var dynmap: DynmapCommonAPI? = null

    val instance: DynmapCommonAPI?
        get() = dynmap

    init {
        register(this)
    }

    override fun apiEnabled(p0: DynmapCommonAPI?) {
        dynmap = p0
    }

    override fun apiDisabled(api: DynmapCommonAPI?) {
        dynmap = null
    }
}