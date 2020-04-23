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
class ClearQueueCommand : MusicCommandExecutor(false, false, true) {
    override fun execute(context: Context, label: String, args: Array<String>, manager: MusicManager) {
        manager.scheduler.queue.clear()
        context.send().info("Queue cleared.").queue()
    }
}
