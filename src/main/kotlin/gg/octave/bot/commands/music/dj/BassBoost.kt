package gg.octave.bot.commands.music.dj

import gg.octave.bot.entities.framework.CheckVoiceState
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.music.settings.BoostSetting
import gg.octave.bot.utils.extensions.manager
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command

class BassBoost : MusicCog {
    override fun sameChannel() = true
    override fun requirePlayingTrack() = true
    override fun requirePlayer() = true

    @DJ
    @CheckVoiceState
    @Command(aliases = ["bb", "bass", "boost"], description = "Applies bass boost to the music.")
    fun bassboost(ctx: Context, strength: BoostSetting) {
        ctx.manager.dspFilter.bassBoost = strength

        ctx.send {
            setTitle("Bass Boost")
            setDescription("Bass Boost strength is now set to `${strength.name.toLowerCase()}`")
        }
    }
}
