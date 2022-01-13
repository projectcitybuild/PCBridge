package com.projectcitybuild.features.teleporthistory

import com.projectcitybuild.entities.TeleportRecord
import java.util.*
import kotlin.collections.ArrayDeque

class TeleportHistory(
    private val capacity: Int,
    private val history: ArrayDeque<TeleportRecord> = ArrayDeque(),
    private val future: Stack<TeleportRecord> = Stack()
) {
    init {
        assert(capacity > 0)
    }

    fun back(steps: Int = 1): TeleportRecord? {
        if (steps > history.size - 1) return null

        for (i in 1..steps) {
            val record = history.removeLast()
            future.push(record)

            if (i == steps) return record
        }
        return null
    }

    fun forward(steps: Int = 1): TeleportRecord? {
        if (steps > future.size) return null

        for (i in 0 until steps) {
            val record = future.removeLast()
            history.addLast(record)

            if (i == steps) return record
        }
        return null
    }

    fun visit(record: TeleportRecord) {
        future.clear()

        while (history.size > capacity) {
            history.removeFirst()
        }
        history.addLast(record)
    }
}
