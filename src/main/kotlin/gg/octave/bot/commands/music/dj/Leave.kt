package gg.octave.bot.commands.music.dj

import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.utils.extensions.existingManager
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command

class Leave : MusicCog {
    override fun requireManager() = false

    @DJ
    @Command(aliases = ["dc", "disconnect"], description = "Disconnects the bot from voice channel.")
    fun leave(ctx: Context) {
        val karen = ctx.existingManager
            ?: return ctx.send("Nothing is playing.")

        karen.destroy()
        ctx.send("I have left the voice channel. If you would like to clear the queue, run `${ctx.trigger}clearqueue`.")
    }
}
