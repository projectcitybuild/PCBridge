package com.projectcitybuild.pcbridge.http.pcb.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * A resource that represents just the id and name of another resource.
 *
 * For example, the "/names" endpoints for builds and homes returns a
 * list of this.
 */
@Serializable
data class NamedResource(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,
)
