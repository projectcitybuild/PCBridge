package com.projectcitybuild.libs.errorreporting.outputs

import com.projectcitybuild.libs.errorreporting.ErrorOutput

class PrintStackTraceOutput : ErrorOutput {
    override fun start() {}

    override fun report(throwable: Throwable) {
        throwable.printStackTrace()
    }
}
