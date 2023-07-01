package com.projectcitybuild.pcbridge.core.contracts

import com.projectcitybuild.pcbridge.core.utils.AsyncTask

interface PlatformScheduler {

    fun <T> async(task: ((T) -> Unit) -> Unit): AsyncTask<T>
    fun sync(task: () -> Unit)
}
