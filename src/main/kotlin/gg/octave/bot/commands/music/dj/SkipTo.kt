package gg.octave.bot.commands.music.dj

import gg.octave.bot.entities.framework.CheckVoiceState
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.utils.extensions.config
import gg.octave.bot.utils.extensions.manager
import gg.octave.bot.utils.extensions.removeAt
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command

class SkipTo : MusicCog {
    override fun sameChannel() = true
    override fun requirePlayingTrack() = true
    override fun requirePlayer() = true

    @DJ
    @CheckVoiceState
    @Command(aliases = ["skt"], description = "Skip the current music track.")
    fun skipTo(ctx: Context, where: Int?) {
        val manager = ctx.manager

        val toIndex = where?.takeIf { it > 0 && it <= manager.scheduler.queue.size }
            ?: return ctx.send("You need to specify the position of the track in the queue that you want to skip to.")

        if (toIndex - 1 == 0) {
            return ctx.send("Use the `${ctx.config.prefix}skip` command to skip single tracks.")
        }

        for (i in 0 until toIndex - 1) {
            manager.scheduler.queue.removeAt(0)
        }

        manager.scheduler.nextTrack()
        ctx.send("Skipped **${toIndex - 1}** tracks.")
    }
}