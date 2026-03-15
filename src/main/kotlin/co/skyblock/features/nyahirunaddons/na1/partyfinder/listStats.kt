package co.skyblock.features.nyahirunaddons.na1.partyfinder

import co.skyblock.events.core.ItemTooltipEvent
import co.skyblock.features.nyahirunaddons.na1.partyfinder.autoKick.runLater
import co.skyblock.utils.EventUtils
import co.skyblock.utils.dungeon.api.Manager
import co.skyblock.utils.dungeon.api.PlayerUtils
import co.stellarskys.stella.annotations.Module
import co.stellarskys.stella.events.core.ChatEvent
import co.stellarskys.stella.features.Feature
import dev.deftu.omnicore.api.client.client
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import java.util.concurrent.atomic.AtomicInteger
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object listStats : Feature("listStats") {

    val playerClass = mutableMapOf<String, String>()

    var partyLeader = ""
    val partyModerators = mutableListOf<String>()
    val partyMembers = mutableListOf<String>()

    private val canWrite = EventUtils.canWrite
    private val readOnly = EventUtils.readOnly

    override fun initialize() {
        on<ItemTooltipEvent.Line> { event ->
            val lines = event.lines
            canWrite.hasDungeon = false
            canWrite.hasFloor = false
            canWrite.hasMember = false

            for (i in lines.indices) {

                val line = lines[i]
                val str = line.string

                when {
                    str.startsWith(readOnly.dungeonPrefix) -> {
                        canWrite.hasDungeon = true
                    }

                    str.startsWith(readOnly.floorPrefix) -> {
                        canWrite.hasFloor = true
                    }

                    str.startsWith(readOnly.membersPrefix) -> {
                        canWrite.hasMember = true
                    }

                    str.contains(":") -> {

                        for (job in readOnly.jobs) {
                            if (str.contains(": $job") && canWrite.hasDungeon && canWrite.hasFloor && canWrite.hasMember) {
                                val playerName = PlayerUtils.extractPlayerName(str)

                                playerClass[playerName] = job
                            }
                        }
                    }
                }
            }
        }

        on<ChatEvent.Receive> { event ->
            val msg = event.message.stripped

            when {
                msg.startsWith("Party Members (") -> {
                    println(msg)
                    partyModerators.clear()
                    partyMembers.clear()
                    partyLeader = ""
                }

                msg.startsWith("Party Leader:") -> {
                    partyLeader = extractNames(msg, "Party Leader:").firstOrNull() ?: ""
                    println("Leader: $partyLeader")
                }

                msg.startsWith("Party Moderators:") -> {
                    partyModerators.clear()
                    partyModerators.addAll(extractNames(msg, "Party Moderators:"))
                    println("Moderators: $partyModerators")
                }

                msg.startsWith("Party Members:") -> {
                    partyMembers.clear()
                    partyMembers.addAll(extractNames(msg, "Party Members:"))
                    println("Members: $partyMembers")
                    handleMessage(partyLeader, partyModerators, partyMembers)
                }
            }
        }
    }

    private fun handleMessage(leader: String, mods: List<String>, members: List<String>) {
        val allPlayers = (listOf(leader) + mods + members).filter { it.isNotBlank() }

        for (name in allPlayers) {
            if (playerClass[name] == null) {
                Manager.getCachedSelectedClass(name)
                    ?.let(::normalizeSelectedClass)
                    ?.let { playerClass[name] = it }
            }
        }

        val missing = allPlayers.filter { playerClass[it] == null && !Manager.isFetching(it) }

        if (missing.isNotEmpty()) {
            val remaining = AtomicInteger(missing.size)
            for (name in missing) {
                Manager.fetchAsync(name) {
                    if (playerClass[name] == null) {
                        Manager.getCachedSelectedClass(name)
                            ?.let(::normalizeSelectedClass)
                            ?.let { playerClass[name] = it }
                    }
                    if (remaining.decrementAndGet() == 0) {
                        Minecraft.getInstance().execute {
                            handleMessage(leader, mods, members)
                        }
                    }
                }
            }
            return
        }

        fun getClass(name: String): String = playerClass[name] ?: "Unknown"

        fun clickableName(name: String): Component =
            Component.literal("§b§n$name§r: ${getClass(name)}")
                .withStyle { it.withClickEvent(ClickEvent.RunCommand("/nyahirunaddons pv $name")) }

        val message = Component.literal("━━━━━━━━━━ List Stats ━━━━━━━━━━")
            .append(Component.literal("\n"))

        message.append(Component.literal("§e[NA] §cLeader §f-> "))
            .append(clickableName(leader))
            .append(Component.literal("\n"))

        if (mods.isNotEmpty()) {
            message.append(Component.literal("§e[NA] §6Moderators §f-> "))
            mods.forEachIndexed { i, mod ->
                message.append(clickableName(mod))
                if (i < mods.lastIndex) message.append(Component.literal(", "))
            }
            message.append(Component.literal("\n"))
        }

        message.append(Component.literal("§e[NA] §aMembers §f-> "))
        members.forEachIndexed { i, member ->
            message.append(clickableName(member))
            if (i < members.lastIndex) message.append(Component.literal(", "))
        }
        message.append(Component.literal("\n"))

        message.append(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"))

        runLater(2) {
            client.player?.displayClientMessage(message, false)
        }
    }

    // "Party Leader: [MVP+] Cyqqn ●" -> ["Cyqqn"]
    // "Party Members: [MVP+] Foo ● [VIP] Bar ●" -> ["Foo", "Bar"]
    private fun extractNames(line: String, prefix: String): List<String> {
        return line.removePrefix(prefix)
            .split("●")
            .mapNotNull { segment ->
                segment.trim()
                    .split(" ")
                    .lastOrNull { it.isNotBlank() }
            }
            .filter { it.isNotBlank() }
    }

    private fun normalizeSelectedClass(rawClass: String): String {
        return when (rawClass.trim().lowercase()) {
            "archer" -> "Archer"
            "berserk" -> "Berserk"
            "mage" -> "Mage"
            "tank" -> "Tank"
            "healer" -> "Healer"
            else -> rawClass.trim()
        }
    }
}
