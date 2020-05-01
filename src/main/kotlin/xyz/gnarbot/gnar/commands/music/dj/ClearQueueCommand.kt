package xyz.gnarbot.gnar.commands.music.dj

import xyz.gnarbot.gnar.commands.*
import xyz.gnarbot.gnar.commands.music.MusicCommandExecutor
import xyz.gnarbot.gnar.music.MusicManager

@Command(
    aliases = ["clearqueue", "cq", "cleanqueue", "emptyqueue", "empty"],
    description = "Skip the current music track."
)
@BotInfo(
    id = 69420,
    category = Category.MUSIC,
    scope = Scope.VOICE,
    djLock = true
)
class ClearQueueCommand : CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<String>) {
        val manager = context.bot.players.get(context.guild)
        val queue = manager.scheduler.queue

        if (queue.isEmpty()) {
            return context.send().info("There's nothing to clear.").queue()
        }

        queue.clear()
        context.send().info("Queue cleared.").queue()
    }
}
