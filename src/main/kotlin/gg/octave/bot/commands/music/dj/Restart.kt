package gg.octave.bot.commands.music.dj

import gg.octave.bot.commands.music.embedTitle
import gg.octave.bot.entities.framework.CheckVoiceState
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.utils.extensions.manager
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command

class Restart : MusicCog {
    override fun sameChannel() = true
    override fun requirePlayer() = true

    @DJ
    @CheckVoiceState
    @Command(aliases = ["replay"], description = "Restart the current song.")
    fun restart(ctx: Context) {
        val manager = ctx.manager

        val track = manager.player.playingTrack ?: manager.scheduler.lastTrack
        ?: return ctx.send("No track has been previously started.")

        ctx.send("Restarting track: `${track.info.embedTitle}`.")
        manager.player.playTrack(track.makeClone())
    }
}