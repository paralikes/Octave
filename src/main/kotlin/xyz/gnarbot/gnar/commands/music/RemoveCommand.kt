package xyz.gnarbot.gnar.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import xyz.gnarbot.gnar.commands.*
import xyz.gnarbot.gnar.music.MusicManager
import xyz.gnarbot.gnar.utils.PlaylistUtils
import java.util.*
import java.util.regex.Pattern

@Command(
        aliases = ["remove", "removesong"],
        description = "Remove a song from the queue.",
        usage = "(first|last|all|start..end|#)"
)
@BotInfo(
        id = 79,
        category = Category.MUSIC,
        scope = Scope.VOICE,
        djLock = true
)
class RemoveCommand : MusicCommandExecutor(true, false, false) {
    private val pattern = Pattern.compile("(\\d+)?\\s*?\\.\\.\\s*(\\d+)?")

    override fun execute(context: Context, label: String, args: Array<String>, manager: MusicManager) {
        val queue = manager.scheduler.queue

        if (queue.isEmpty()) {
            context.send().error("The queue is empty.").queue()
            return
        }

        val track : String = when (args.firstOrNull()) {
            null -> return context.send().issue("You need to specify what to remove.").queue()
            "first" -> queue.remove() //Remove head
            "last" -> manager.scheduler.removeQueueIndex(queue, queue.size)
            "all" -> {
                queue.clear()
                context.send().info("Cleared the music queue.").queue()
                return
            }
            else -> {
                val arg = args.joinToString(" ")

                val matcher = pattern.matcher(arg)
                if (matcher.find()) {
                    if (matcher.group(1) == null && matcher.group(2) == null) {
                        return context.send().error("You must specify start range and/or end range.").queue()
                    }

                    val start = matcher.group(1).let {
                        if (it == null) 1
                        else it.toIntOrNull()?.coerceAtLeast(1)
                                ?: return context.send().error("Invalid start of range").queue()
                    }

                    val end = matcher.group(2).let {
                        if (it == null) queue.size
                        else it.toIntOrNull()?.coerceAtMost(queue.size)
                                ?: return context.send().error("Invalid end of range").queue()
                    }

                    for (i in end downTo start) {
                        manager.scheduler.removeQueueIndex(queue, i - 1)
                    }

                    context.send().info("Removed tracks `$start-$end` from the queue.").queue()
                    return
                }

                val num = arg.toIntOrNull()
                        ?.takeIf { it >= 1 && it <= queue.size }
                        ?: return context.send().error("That is not a valid track number. Try `1`, `1..${queue.size}`, `first`, or `last`.").queue()

                manager.scheduler.removeQueueIndex(queue, num - 1)
            }
        }

        val decodedTrack = PlaylistUtils.toAudioTrack(track)
        context.send().info("Removed __[${decodedTrack.info.embedTitle}](${decodedTrack.info.embedUri})__ from the queue.").queue()
    }
}