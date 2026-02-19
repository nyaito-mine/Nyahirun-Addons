package co.skyblock.utils.render

import co.skyblock.utils.render.listeners.Event
import co.skyblock.utils.render.listeners.Listener
import java.util.ArrayList
import java.util.HashMap

/**
 * An event manager that allows for Listeners to subscribe to their corresponding events,
 * as well as for calling and managing those events.
 *
 * @author GT3CH1
 * @version 01-26-2025
 * @since 03-02-2023
 */
class EventManager {

    companion object {
        /**
         * A map storing the event class and its corresponding listeners.
         */
        protected val eventMap: MutableMap<Class<out Listener>, ArrayList<Listener>> =
            HashMap()

        /**
         * Singleton instance.
         */
        val eventManager = EventManager()
    }

    /**
     * Adds a listener to the event manager.
     */
    @Suppress("UNCHECKED_CAST")
    fun <L : Listener> subscribe(event: Class<L>, listener: L) {
        eventMap.computeIfAbsent(event) { ArrayList() }
        (eventMap[event] as ArrayList<L>).add(listener)
    }

    /**
     * Removes a listener from the event manager.
     */
    @Suppress("UNCHECKED_CAST")
    fun <L : Listener> unsubscribe(event: Class<L>, listener: L) {
        try {
            (eventMap[event] as ArrayList<L>).remove(listener)
        } catch (e: NullPointerException) {
            throw IllegalArgumentException("Listener not found. Please report this error.")
        }
    }

    /**
     * Calls all listeners for the given event.
     */
    @Suppress("UNCHECKED_CAST")
    fun <L : Listener, E : Event<L>> call(event: E) {
        val listeners = eventMap[event.getEvent()] as? ArrayList<L> ?: return
        event.fire(listeners)
    }
}