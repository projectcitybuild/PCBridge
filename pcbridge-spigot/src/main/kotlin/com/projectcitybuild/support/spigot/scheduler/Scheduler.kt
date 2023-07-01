package com.projectcitybuild.support.spigot.scheduler

import com.projectcitybuild.core.utilities.AsyncTask

interface Scheduler {

    fun <T> async(task: ((T) -> Unit) -> Unit): AsyncTask<T>
    fun sync(task: () -> Unit)
}
