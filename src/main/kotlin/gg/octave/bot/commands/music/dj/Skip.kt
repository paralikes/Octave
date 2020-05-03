package gg.octave.bot.commands.music.dj

import gg.octave.bot.entities.framework.CheckVoiceState
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.utils.extensions.manager
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command

class Skip : MusicCog {
    override fun sameChannel() = true
    override fun requirePlayingTrack() = true
    override fun requirePlayer() = true

    @DJ
    @CheckVoiceState
    @Command(aliases = ["sk", "s"], description = "Skips the current track.")
    fun skip(ctx: Context) {
        ctx.manager.scheduler.nextTrack()
        ctx.send("Skipped current track.")
    }
}