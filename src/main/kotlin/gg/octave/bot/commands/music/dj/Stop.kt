package gg.octave.bot.commands.music.dj

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import gg.octave.bot.Launcher
import gg.octave.bot.entities.framework.CheckVoiceState
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.utils.extensions.manager

class Stop : MusicCog {
    @DJ
    @CheckVoiceState
    @Command(aliases = ["leave", "end", "st", "fuckoff"], description = "Stop and clear the music player.")
    fun stop(ctx: Context, clear: Boolean = false) {
        val karen = ctx.manager

        if (clear) {
            karen.scheduler.queue.clear()
        }

        karen.discordFMTrack = null
        ctx.guild!!.audioManager.closeAudioConnection()
        Launcher.players.destroy(ctx.guild!!.idLong)

        ctx.send("Playback has been completely stopped. If you want to clear the queue run `${ctx.trigger}clearqueue` or `${ctx.trigger}stop yes`")
    }
}
