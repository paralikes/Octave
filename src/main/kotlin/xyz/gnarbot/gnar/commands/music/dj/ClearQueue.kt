package xyz.gnarbot.gnar.commands.music.dj

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.entities.Cog
import xyz.gnarbot.gnar.entities.framework.CheckVoiceState
import xyz.gnarbot.gnar.entities.framework.DJ
import xyz.gnarbot.gnar.utils.extensions.launcher

class ClearQueue : Cog {
    @DJ
    @CheckVoiceState
    @Command(aliases = ["cq"], description = "Clear the current queue.")
    fun clearqueue(ctx: Context) {
        val manager = ctx.launcher.players.get(ctx.guild)
        val queue = manager.scheduler.queue

        if (queue.isEmpty()) {
            return ctx.send("There's nothing to clear.")
        }

        queue.clear()
        ctx.send("Queue cleared.")

    }
}