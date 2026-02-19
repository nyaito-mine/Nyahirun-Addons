package co.skyblock.utils

object ClientDelay {
    private var delayTicks = 0
    private var task: Runnable? = null

    @JvmStatic
    fun runLater(ticks: Int, runnable: Runnable?) {
        delayTicks = ticks
        task = runnable
    }

    @JvmStatic
    fun tick() {
        if (delayTicks > 0) {
            delayTicks--
            return
        }
        if (task != null) {
            task!!.run()
            task = null
        }
    }
}