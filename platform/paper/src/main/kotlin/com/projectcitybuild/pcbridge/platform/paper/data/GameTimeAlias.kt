package com.projectcitybuild.pcbridge.platform.paper.data

enum class GameTimeAlias(
    val argument: String,
    val ticks: Long,
) {
    SUNRISE("sunrise", 23_000L),
    DAWN("dawn", 23_000L),
    DAYSTART("daystart", 0L),
    DAY("day", 0L),
    MORNING("morning", 1_000L),
    MIDDAY("midday", 6_000L),
    NOON("noon", 6_000L),
    AFTERNOON("afternoon", 9_000L),
    SUNSET("sunset", 12_000L),
    DUSK("dusk", 12_000L),
    SUNDOWN("sundown", 12_000L),
    NIGHTFALL("nightfall", 12_000L),
    NIGHTSTART("nightstart", 14_000L),
    NIGHT("night", 14_000L),
    MIDNIGHT("midnight", 18_000L),
    ;

    companion object {
        fun from(argument: String): GameTimeAlias? = entries.firstOrNull {
            it.argument == argument
        }
    }
}
