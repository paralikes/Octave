package xyz.gnarbot.gnar.commands.music.dj

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import xyz.gnarbot.gnar.entities.framework.CheckVoiceState
import xyz.gnarbot.gnar.entities.framework.DJ
import xyz.gnarbot.gnar.entities.framework.MusicCog
import xyz.gnarbot.gnar.music.settings.BoostSetting
import xyz.gnarbot.gnar.utils.extensions.manager

class BassBoost : MusicCog(true, true, true) {
    @DJ
    @CheckVoiceState
    @Command(aliases = ["bb", "bassboost"])
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
