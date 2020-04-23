package xyz.gnarbot.gnar.commands.music.dj

import xyz.gnarbot.gnar.commands.*
import xyz.gnarbot.gnar.commands.music.MusicCommandExecutor
import xyz.gnarbot.gnar.music.MusicManager

@Command(
        aliases = ["pause"],
        description = "Pause or resume the music player."
)
@BotInfo(
        id = 68,
        category = Category.MUSIC,
        scope = Scope.VOICE,
        djLock = true
)
class PauseCommand : MusicCommandExecutor(true, true, true) {
    override fun execute(context: Context, label: String, args: Array<String>, manager: MusicManager) {
        manager.player.isPaused = !manager.player.isPaused

        context.send().embed {
            desc {
                if (manager.player.isPaused) {
                    "The player has been paused."
                } else {
                    "The player has resumed playing."
                }
            }
        }.action().queue()
    }
}
