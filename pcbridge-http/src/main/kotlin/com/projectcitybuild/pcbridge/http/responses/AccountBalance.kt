package com.projectcitybuild.pcbridge.http.responses

import com.google.gson.annotations.SerializedName

data class AccountBalance(
    @SerializedName("balance") val balance: Int,
)
