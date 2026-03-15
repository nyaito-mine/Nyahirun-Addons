package co.skyblock.features.nyahirunaddons.na1.partyfinder

import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.events.core.ChatEvent
import co.stellarskys.stella.events.core.TickEvent
import co.stellarskys.stella.features.Feature
import co.stellarskys.stella.utils.config
import co.skyblock.events.core.PartyFinderEvent
import co.skyblock.utils.EventUtils
import co.skyblock.utils.dungeon.api.Manager
import net.minecraft.client.Minecraft
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object autoKick : Feature("autoKick") {

    val f7TimeConfig by config.property<String>("f7Time")
    val m7TimeConfig by config.property<String>("m7Time")
    val otherFloorTimeConfig by config.property<String>("otherFloorTime")

    private data class ScheduledTask(
        var ticksLeft: Int,
        val action: () -> Unit
    )

    private val canWrite = EventUtils.canWrite
    private val readOnly = EventUtils.readOnly

    private val scheduledTasks = mutableListOf<ScheduledTask>()

    override fun initialize() {
        on<TickEvent.Client> {
            val iterator = scheduledTasks.iterator()
            while (iterator.hasNext()) {
                val task = iterator.next()
                task.ticksLeft--
                if (task.ticksLeft <= 0) {
                    task.action.invoke()
                    iterator.remove()
                }
            }
        }

        on<PartyFinderEvent.Queue> { event ->
            canWrite.inQueue = true
            canWrite.canAutoKick = true
            canWrite.currentFloor = event.floor.toString()
        }

        on<PartyFinderEvent.Leave> {
            resetState()
        }

        on<PartyFinderEvent.PartyInfo> { event ->
            if (!event.inParty) {
                canWrite.canAutoKick = false
                canWrite.lastMembers = emptySet()
                return@on
            }

            val myUuid = Minecraft.getInstance().player?.uuid ?: return@on
            val members = event.members
            val memberCount = members.size

            if (memberCount >= 2) {
                val myMember = members[myUuid]
                val isLeader = myMember?.role?.name == "LEADER"
                canWrite.canAutoKick = isLeader
            }
        }

        on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped

            readOnly.regexPFEnter.find(msg)?.let {
                resetState()
            }

            if (!canWrite.inQueue) return@on
            if (!canWrite.canAutoKick) return@on

            readOnly.regexJoin.find(msg)?.let {
                val name = it.groupValues[1]
                checkPlayerByName(name)
            }
        }
    }

    private fun checkPlayerByName(name: String) {

        val floor = canWrite.currentFloor
        val floorKey = getFloorKey(floor)
        val dungeonName = getDungeonName(floor)

        val pbStr = Manager.getCachedPBString(name, dungeonName, floorKey)

        if (pbStr == null) {
            if (!Manager.hasValidCache(name) || !Manager.isFetching(name)) {
                Manager.fetchAsync(name) {
                    val newPb = Manager.getCachedPBString(name, dungeonName, floorKey)
                    Minecraft.getInstance().execute {
                        handleKick(name, newPb)
                    }
                }
            }
            return
        }

        handleKick(name, pbStr)
    }

    private fun handleKick(name: String, pbStr: String?) {

        if (pbStr == null) return

        if (pbStr == "No Times") {
            sendCommand("pc [NA] Kicking $name (${canWrite.currentFloor} - No S+ Time)")
            runLater(7) { sendCommand("p kick $name") }
            return
        }

        val pbSeconds = parseTimeToSeconds(pbStr)
        val reqSeconds = getRequiredSeconds()
        val reqStr = getRequiredTime()

        if (pbSeconds != null && reqSeconds != null && pbSeconds > reqSeconds) {
            sendCommand("pc [NA] Kicking $name (${canWrite.currentFloor} - PB: $pbStr | Req: $reqStr)")
            runLater(7) { sendCommand("p kick $name") }
        }
    }

    private fun getFloorKey(floor: String): String {
        return when {
            floor.contains("7") -> "floor_7"
            floor.contains("6") -> "floor_6"
            floor.contains("5") -> "floor_5"
            floor.contains("4") -> "floor_4"
            floor.contains("3") -> "floor_3"
            floor.contains("2") -> "floor_2"
            floor.contains("1") -> "floor_1"
            else -> ""
        }
    }

    private fun getDungeonName(floor: String): String {
        return if (floor.contains("M", true))
            "Master Mode The Catacombs"
        else
            "The Catacombs"
    }

    private fun getRequiredSeconds(): Int? {
        val floor = canWrite.currentFloor
        val floorKey = getFloorKey(floor)
        val dungeon = getDungeonName(floor)

        return when {
            floorKey != "floor_7" ->
                parseTimeToSeconds(otherFloorTimeConfig)
            dungeon == "The Catacombs" ->
                parseTimeToSeconds(f7TimeConfig)
            else ->
                parseTimeToSeconds(m7TimeConfig)
        }
    }

    private fun getRequiredTime(): String {
        val floor = canWrite.currentFloor
        val floorKey = getFloorKey(floor)
        val dungeon = getDungeonName(floor)

        return when {
            floorKey != "floor_7" ->
                otherFloorTimeConfig
            dungeon == "The Catacombs" ->
                f7TimeConfig
            else ->
                m7TimeConfig
        }
    }

    private fun parseTimeToSeconds(time: String?): Int? {
        if (time == null || time == "No Times") return null
        val parts = time.split(":")
        if (parts.size != 2) return null
        val minutes = parts[0].toIntOrNull() ?: return null
        val seconds = parts[1].toIntOrNull() ?: return null
        return minutes * 60 + seconds
    }

    fun runLater(ticks: Int, action: () -> Unit) {
        scheduledTasks.add(ScheduledTask(ticks, action))
    }

    private fun sendCommand(command: String) {
        Minecraft.getInstance().player?.connection?.sendCommand(command)
    }

    private fun resetState() {
        canWrite.inQueue = false
        canWrite.canAutoKick = false
        canWrite.currentFloor = ""
    }
}