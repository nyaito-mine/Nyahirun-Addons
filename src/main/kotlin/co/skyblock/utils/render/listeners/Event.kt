package co.skyblock.utils.render.listeners

interface Event<T : Listener> {

    fun fire(listeners: ArrayList<T>)

    fun getEvent(): Class<T>
}