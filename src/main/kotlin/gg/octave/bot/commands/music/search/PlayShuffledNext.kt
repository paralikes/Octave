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

class PlayShuffledNext : Cog {
    @DJ
    @Command(aliases = ["psn"], description = "Shuffles the songs before adding them to the front of the queue.")
    fun playshufflednext(ctx: Context, @Greedy query: String) {
        val manager = ctx.existingManager
            ?: return ctx.send("There's no queue here.\n$PLAY_MESSAGE")

        val botChannel = ctx.selfMember!!.voiceState?.channel
            ?: return ctx.send("I'm not currently playing anything.\n$PLAY_MESSAGE")
        val userChannel = ctx.voiceChannel

        if (botChannel != userChannel) {
            return ctx.send("The bot is already playing music in another channel.")
        }

        val args = query.split(" +".toRegex())
        Play.smartPlay(ctx, manager, args, false, "", isNext = true, shuffle = true)
    }
}