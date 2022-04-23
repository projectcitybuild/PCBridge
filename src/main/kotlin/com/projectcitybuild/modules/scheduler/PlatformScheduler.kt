package com.projectcitybuild.modules.scheduler

import com.projectcitybuild.core.utilities.AsyncTask

interface PlatformScheduler {

    fun <T> async(task: ((T) -> Unit) -> Unit): AsyncTask<T>
    fun sync(task: () -> Unit)
}
