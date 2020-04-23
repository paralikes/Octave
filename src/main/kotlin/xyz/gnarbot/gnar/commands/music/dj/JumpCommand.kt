package xyz.gnarbot.gnar.commands.music.dj

import xyz.gnarbot.gnar.commands.*
import xyz.gnarbot.gnar.commands.music.MusicCommandExecutor
import xyz.gnarbot.gnar.commands.music.PLAY_MESSAGE
import xyz.gnarbot.gnar.commands.template.CommandTemplate
import xyz.gnarbot.gnar.commands.template.annotations.Description
import xyz.gnarbot.gnar.music.MusicManager
import xyz.gnarbot.gnar.utils.Utils
import java.time.Duration

@Command(
        aliases = ["jump", "seek"],
        usage = "(time)",
        description = "Set the time marker of the music playback."
)
@BotInfo(
        id = 65,
        category = Category.MUSIC,
        scope = Scope.VOICE,
        djLock = true
)
class JumpCommand : MusicCommandExecutor(true, true, true) {
    override fun execute(context: Context, label: String, args: Array<String>, manager: MusicManager) {
        if (!manager.player.playingTrack.isSeekable) {
            return context.send().issue("The current track doesn't support seeking.").queue()
        }

        if (args.isEmpty()) {
            return context.send().issue(
                "You need to specify how many seconds to seek by, or a timestamp.\n" +
                    "**Examples:**\n" +
                    "`${context.bot.configuration.prefix}$label 30` (Jump ahead by 30 seconds)\n" +
                    "`${context.bot.configuration.prefix}$label -30` (Jump back by 30 seconds)\n" +
                    "`${context.bot.configuration.prefix}$label 02:29` (Jump to exactly 2:29)\n" +
                    "`${context.bot.configuration.prefix}$label 1m45s` (Jump ahead by 1 minute and 45 seconds)"
            ).queue()
        }

        val seconds = args[0].toLongOrNull()

        when {
            seconds != null -> seekByMilliseconds(context, manager, seconds * 1000)
            ':' in args[0] -> seekByTimestamp(context, manager, args[0])
            args[0].matches(timeFormat) -> seekByTimeShorthand(context, manager, args[0])
            else -> return context.send().issue(
                "You didn't specify a valid time format!\n" +
                    "Run the command without arguments to see usage examples."
            ).queue()
        }
    }

    fun seekByMilliseconds(ctx: Context, manager: MusicManager, milliseconds: Long) {
        val currentTrack = manager.player.playingTrack
        val position = (currentTrack.position + milliseconds).coerceIn(0, currentTrack.duration)
        currentTrack.position = position

        ctx.send().info("Seeked to **${Utils.getTimestamp(position)}**.").queue()
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
            else -> return ctx.send().issue("You need to format the timestamp as `hours:minutes:seconds` or `minutes:seconds`.").queue()
        }

        val currentTrack = manager.player.playingTrack
        val absolutePosition = millis.coerceIn(0, currentTrack.duration)
        currentTrack.position = absolutePosition

        ctx.send().info("Seeked to **${Utils.getTimestamp(absolutePosition)}**.").queue()
    }

    fun seekByTimeShorthand(ctx: Context, manager: MusicManager, shorthand: String) {
        val segments = timeSegment.findAll(shorthand).map(MatchResult::value)
        val milliseconds = segments.map(::parseSegment).sum()

        val currentTrack = manager.player.playingTrack
        val absolutePosition = (currentTrack.position + milliseconds).coerceIn(0, currentTrack.duration)
        currentTrack.position = absolutePosition

        ctx.send().info("Seeked to **${Utils.getTimestamp(absolutePosition)}**.").queue()
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
