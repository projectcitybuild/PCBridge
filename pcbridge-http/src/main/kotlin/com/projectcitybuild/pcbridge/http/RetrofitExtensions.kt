package com.projectcitybuild.pcbridge.http

import com.projectcitybuild.pcbridge.http.requests.MojangRequest
import com.projectcitybuild.pcbridge.http.requests.PCBRequest
import retrofit2.Retrofit

internal fun Retrofit.pcb() = create(PCBRequest::class.java)

internal fun Retrofit.mojang() = create(MojangRequest::class.java)