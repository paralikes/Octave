package gg.octave.bot.commands.music.dj

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import gg.octave.bot.entities.framework.CheckVoiceState
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.music.settings.BoostSetting
import gg.octave.bot.utils.extensions.manager

class BassBoost : MusicCog {
    override fun sameChannel() = true
    override fun requirePlayingTrack() = true
    override fun requirePlayer() = true

    @DJ
    @CheckVoiceState
    @Command(aliases = ["bb", "bassboost"], description = "Sets the bass boost level of the music playing.")
    fun boost(ctx: Context, arg: String) {
        val manager = ctx.manager

        when (arg) {
            "off" -> manager.dspFilter.bassBoost = BoostSetting.OFF
            "soft" -> manager.dspFilter.bassBoost = BoostSetting.SOFT
            "hard" -> manager.dspFilter.bassBoost = BoostSetting.HARD
            "extreme" -> manager.dspFilter.bassBoost = BoostSetting.EXTREME
            "earrape" -> manager.dspFilter.bassBoost = BoostSetting.EARRAPE
            else -> return ctx.send("$arg is not an option.")
        }

        ctx.send {
            setTitle("Bass Boost")
            addField("Boost", "Bass Boost has been set to: $arg", false)
        }
    }
}
