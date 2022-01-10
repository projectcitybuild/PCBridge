package com.projectcitybuild.modules.scheduler

import com.projectcitybuild.core.utilities.AsyncTask

interface SchedulerProvider {

    fun <T> async(task: ((T) -> Unit) -> Unit): AsyncTask<T>
    fun sync(task: () -> Unit)
}