package gg.octave.bot.commands.admin

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.entities.Cog
import kotlin.system.exitProcess

class Shutdown : Cog {
    @Command(description = "Shuts down the bot.", developerOnly = true)
    fun shutdown(ctx: Context) {
        ctx.jda.shardManager?.shutdown() ?: ctx.jda.shutdown()
        exitProcess(21)
    }
}
