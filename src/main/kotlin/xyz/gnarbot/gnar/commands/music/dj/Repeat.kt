package xyz.gnarbot.gnar.commands.music.dj

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import xyz.gnarbot.gnar.entities.framework.CheckVoiceState
import xyz.gnarbot.gnar.entities.framework.DJ
import xyz.gnarbot.gnar.entities.framework.MusicCog
import xyz.gnarbot.gnar.music.settings.RepeatOption
import xyz.gnarbot.gnar.utils.extensions.manager
import java.lang.IllegalArgumentException

class Repeat : MusicCog(true, false, true) {

    @DJ
    @CheckVoiceState
    @Command(aliases = ["loop"], description = "Set if the music player should repeat")
    fun repeat(ctx: Context, arg: String) {
        val option = try {
            RepeatOption.valueOf(arg)
        } catch (e: IllegalArgumentException) {
            return ctx.send("Valid options are `${RepeatOption.values().joinToString()}`")
        }

        ctx.manager.scheduler.repeatOption = option
        val symbol = when (ctx.manager.scheduler.repeatOption) {
            RepeatOption.QUEUE -> "\uD83D\uDD01"
            RepeatOption.SONG -> "\uD83D\uDD02"
            RepeatOption.NONE -> "\u274C"
        }

        ctx.send("$symbol Music player was set to __**${ctx.manager.scheduler.repeatOption}**__.")
    }
}
