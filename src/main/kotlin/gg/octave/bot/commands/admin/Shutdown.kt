package gg.octave.bot.commands.admin

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.entities.Cog
import gg.octave.bot.Launcher
import kotlin.system.exitProcess

class Shutdown : Cog {
    @Command(description = "Shuts down the bot.", developerOnly = true)
    fun shutdown(ctx: Context) {
        Launcher.players.shutdown()
        ctx.jda.shardManager?.shutdown() ?: ctx.jda.shutdown()
        exitProcess(21)
    }
}
