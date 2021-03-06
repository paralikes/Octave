package gg.octave.bot.commands.music.search

import gg.octave.bot.commands.music.PLAY_MESSAGE
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.utils.extensions.existingManager
import gg.octave.bot.utils.extensions.selfMember
import gg.octave.bot.utils.extensions.voiceChannel
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.entities.Cog

class PlayNext : Cog {
    @DJ
    @Command(aliases = ["pn"], description = "Adds a song at the start of the queue.")
    fun playnext(ctx: Context, @Greedy query: String) {
        val manager = ctx.existingManager
            ?: return ctx.send("There's no queue here.\n$PLAY_MESSAGE")

        val botChannel = ctx.selfMember!!.voiceState?.channel
            ?: return ctx.send("I'm not currently playing anything.\n$PLAY_MESSAGE")
        val userChannel = ctx.voiceChannel

        if (botChannel != userChannel) {
            return ctx.send("The bot is already playing music in another channel.")
        }

        val args = query.split(" +".toRegex())
        Play.smartPlay(ctx, manager, args, false, "", true)
    }
}
