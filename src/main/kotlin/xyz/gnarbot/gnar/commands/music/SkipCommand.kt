package xyz.gnarbot.gnar.commands.music

import xyz.gnarbot.gnar.commands.*
import xyz.gnarbot.gnar.music.MusicManager

@Command(
        aliases = ["skip", "sk", "s"],
        description = "Skip the current music track."
)
@BotInfo(
        id = 73,
        category = Category.MUSIC,
        scope = Scope.VOICE,
        djLock = true
)
class SkipCommand : MusicCommandExecutor(true, true, true) {
    override fun execute(context: Context, label: String, args: Array<String>, manager: MusicManager) {
        manager.scheduler.nextTrack()

        context.send().info("The track was skipped.").queue()
    }
}
