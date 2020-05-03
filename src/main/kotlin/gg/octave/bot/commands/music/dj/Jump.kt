package gg.octave.bot.commands.music.dj

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import gg.octave.bot.entities.framework.CheckVoiceState
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.music.MusicManager
import gg.octave.bot.utils.Utils
import gg.octave.bot.utils.extensions.manager

class Jump : MusicCog {
    override fun sameChannel() = true
    override fun requirePlayingTrack() = true
    override fun requirePlayer() = true

    @DJ
    @CheckVoiceState
    @Command(aliases = ["seek"], description = "Set the time marker of the music playback.")
    fun jump(ctx: Context, where: String) {
        if (!ctx.manager.player.playingTrack.isSeekable) {
            return ctx.send("The current track doesn't support seeking.")
        }

        val seconds = where.toLongOrNull()
        when {
            seconds != null -> seekByMilliseconds(ctx, ctx.manager, seconds * 1000)
            ':' in where -> seekByTimestamp(ctx, ctx.manager, where)
            where.matches(timeFormat) -> seekByTimeShorthand(ctx, ctx.manager, where)
            else -> return ctx.send("You didn't specify a valid time format!\nRun the command without arguments to see usage examples.")
        }
    }

    fun seekByMilliseconds(ctx: Context, manager: MusicManager, milliseconds: Long) {
        val currentTrack = manager.player.playingTrack
        val position = (currentTrack.position + milliseconds).coerceIn(0, currentTrack.duration)
        currentTrack.position = position

        ctx.send("Seeked to **${Utils.getTimestamp(position)}**.")
    }

    fun seekByTimestamp(ctx: Context, manager: MusicManager, timestamp: String) {
        val parts = timestamp.split(':').mapNotNull(String::toLongOrNull)

        val millis = when (parts.size) {
            2 -> { // mm:ss
                val (minutes, seconds) = parts
                (minutes * 60000) + (seconds * 1000)
            }
            3 -> { // hh:mm:ss
                val (hours, minutes, seconds) = parts
                (hours * 3600000) + (minutes * 60000) + (seconds * 1000)
            }
            else -> return ctx.send("You need to format the timestamp as `hours:minutes:seconds` or `minutes:seconds`.")
        }

        val currentTrack = manager.player.playingTrack
        val absolutePosition = millis.coerceIn(0, currentTrack.duration)
        currentTrack.position = absolutePosition

        ctx.send("Seeked to **${Utils.getTimestamp(absolutePosition)}**.")
    }

    fun seekByTimeShorthand(ctx: Context, manager: MusicManager, shorthand: String) {
        val segments = timeSegment.findAll(shorthand).map(MatchResult::value)
        val milliseconds = segments.map(::parseSegment).sum()

        val currentTrack = manager.player.playingTrack
        val absolutePosition = (currentTrack.position + milliseconds).coerceIn(0, currentTrack.duration)
        currentTrack.position = absolutePosition

        ctx.send("Seeked to **${Utils.getTimestamp(absolutePosition)}**.")
    }

    private fun parseSegment(segment: String): Long {
        val unit = segment.last()
        val time = segment.take(segment.length - 1).toLong()

        return when (unit) {
            's' -> time * 1000
            'm' -> time * 60000
            'h' -> time * 3600000
            else -> 0
        }
    }

    companion object {
        private val timeSegment = "(\\d+[smh])".toRegex()
        private val timeFormat = "(\\d+[smh])+".toRegex()
    }
}