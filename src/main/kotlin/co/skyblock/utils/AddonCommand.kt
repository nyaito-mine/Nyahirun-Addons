package co.skyblock.utils

import co.skyblock.features.nyahirunaddons.na1.partyfinder.utils.StatsGuiRenderer
import co.skyblock.utils.dungeon.api.Manager
import co.skyblock.utils.dungeon.api.Manager.hasValidCache
import co.skyblock.utils.dungeon.api.Manager.isFetching
import co.stellarskys.stella.utils.ChatUtils
import co.stellarskys.stella.utils.config
import dev.deftu.omnicore.api.client.client
import dev.deftu.omnicore.api.scheduling.TickSchedulers
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.getString

object AddonCommand {
    init {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            registerRoot(dispatcher, "nyahirunaddons")
            registerRoot(dispatcher, "nyahirun")
            registerRoot(dispatcher, "na")
            registerNaPv(dispatcher)
        }
    }

    private fun registerNaPv(
        dispatcher: CommandDispatcher<FabricClientCommandSource>
    ) {
        dispatcher.register(
            literal("napv")
                .executes {
                    val selfName = client.player?.name?.string ?: return@executes 0
                    openStatsScreen(selfName)
                    fetchStatsIfNeeded(selfName)
                    1
                }
                .then(
                    argument("mcid", StringArgumentType.word())
                        .executes { ctx ->
                            val playerName = getString(ctx, "mcid")
                            openStatsScreen(playerName)
                            fetchStatsIfNeeded(playerName)
                            1
                        }
                )
        )
    }

    private fun registerRoot(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        root: String,
    ) {
        dispatcher.register(
            literal(root)
                .executes {
                    config.open()
                    1
                }
                .then(
                    literal("pv")
                        .executes {
                            val selfName = client.player?.name?.string ?: return@executes 0
                            openStatsScreen(selfName)
                            fetchStatsIfNeeded(selfName)
                            1
                        }
                        .then(
                            argument("mcid", StringArgumentType.word())
                                .executes { ctx ->
                                    val playerName = getString(ctx, "mcid")
                                    openStatsScreen(playerName)
                                    fetchStatsIfNeeded(playerName)
                                    1
                                }
                        )
                )
        )
    }
}

private fun openStatsScreen(playerName: String) {
    TickSchedulers.client.post {
        client.setScreen(StatsGuiRenderer(playerName))
    }
}

private fun fetchStatsIfNeeded(playerName: String) {
    if (hasValidCache(playerName) || isFetching(playerName)) return

    Manager.fetchAsync(playerName) {
        openStatsScreen(playerName)
        ChatUtils.fakeMessage("§e[NA] Fetched stats for §b$playerName§e!")
    }
}

