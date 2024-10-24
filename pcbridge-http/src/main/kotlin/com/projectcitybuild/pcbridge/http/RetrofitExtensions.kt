package com.projectcitybuild.pcbridge.http

import com.projectcitybuild.pcbridge.http.requests.PCBRequest
import retrofit2.Retrofit

internal fun Retrofit.pcb() = create(PCBRequest::class.java)
