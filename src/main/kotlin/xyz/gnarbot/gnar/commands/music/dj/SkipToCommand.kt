package xyz.gnarbot.gnar.commands.music.dj

import xyz.gnarbot.gnar.commands.*
import xyz.gnarbot.gnar.commands.music.MusicCommandExecutor
import xyz.gnarbot.gnar.music.MusicManager

@Command(
    aliases = ["skipto", "skt"],
    description = "Skip the current music track."
)
@BotInfo(
    id = 69420,
    category = Category.MUSIC,
    scope = Scope.VOICE,
    djLock = true
)
class SkipToCommand : MusicCommandExecutor(true, true, true) {
    override fun execute(context: Context, label: String, args: Array<String>, manager: MusicManager) {
        val toIndex = args.firstOrNull()?.toIntOrNull()?.takeIf { it > 0 && it <= manager.scheduler.queue.size }
            ?: return context.send().info("You need to specify the position of the track in the queue that you want to skip to.").queue()

        if (toIndex - 1 == 0) {
            return context.send().info("Use the `${context.bot.configuration.prefix}skip` command to skip single tracks.").queue()
        }

        for (i in 0 until toIndex - 1) {
            manager.scheduler.removeQueueIndex(manager.scheduler.queue, 0)
        }

        manager.scheduler.nextTrack()
        context.send().info("Skipped **${toIndex - 1}** tracks.").queue()
    }
}
