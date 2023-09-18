package com.projectcitybuild.support.spigot.listeners

import com.projectcitybuild.pcbridge.core.architecture.events.EventPipeline
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.Plugin
import org.reflections.Reflections
import java.util.Arrays
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * A Spigot event listener that listens for every Bukkit server event,
 * and sends it down an EventPipeline
 *
 * Adapted from https://gist.github.com/MiniDigger/f369210813bd65d43a62468b8dafcaeb
 */
class SpigotEventHandler(
    private val plugin: Plugin,
    private val eventPipeline: EventPipeline,
    private val logger: PlatformLogger,
): Listener {

    // Particularly spammy events that we don't need to monitor
    private val ignoredEvents = arrayOf(
        "VehicleBlockCollisionEvent",
        "EntityAirChangeEvent",
        "EntitiesLoadEvent",
        "VehicleUpdateEvent",
        "ChunkUnloadEvent",
        "ChunkLoadEvent",
        "GenericGameEvent",
        "EnderDragonChangePhaseEvent",
    )

    fun register() {
        val reflections = Reflections("org.bukkit")

        val eventClasses: Set<Class<out Event>> = reflections
            .getSubTypesOf(Event::class.java)
            .stream()
            .filter { clazz ->
                Arrays
                    .stream(clazz.declaredFields)
                    .anyMatch { field -> field.type.name.endsWith("HandlerList") }
            }
            .collect(Collectors.toSet())

        logger.verbose("Watching " + eventClasses.size + " available events")

        val eventExecutor = EventExecutor { _, event -> handle(event) }
        eventClasses.forEach(Consumer { clazz: Class<out Event>? ->
            if (clazz == null) return@Consumer

            plugin.server.pluginManager.registerEvent(
                clazz,
                this,
                EventPriority.MONITOR,
                eventExecutor,
                plugin,
            )
        })
    }

    private fun handle(event: Event) {
        val isIgnoredEvent = Arrays
            .stream(ignoredEvents)
            .anyMatch { ignored -> event.eventName == ignored }

        if (!isIgnoredEvent) {
            logger.verbose("Handling event: ${event.eventName}")
            eventPipeline.emit(event)
        }
    }
}