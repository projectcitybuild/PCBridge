package com.projectcitybuild.entities

sealed class Result<T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure<T>(val error: Exception) : Result<T>()
}