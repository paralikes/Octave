package xyz.gnarbot.gnar.commands.music

import xyz.gnarbot.gnar.commands.*

@Command(
        aliases = ["resume"],
        description = "Resume the music queue."
)
@BotInfo(
        id = 476,
        category = Category.MUSIC,
        scope = Scope.VOICE,
        djLock = true
)
class ResumeCommand : CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<out String>) {
        val manager = context.bot.players.get(context.guild)
        val scheduler = manager.scheduler

        if(context.voiceChannel == null) {
            context.send().error("You need to be in a voice channel.").queue()
            return
        }

        if (scheduler.queue.isEmpty()) {
            context.send().issue("The queue is empty.\n$PLAY_MESSAGE").queue()
            return
        }

        if (scheduler.lastTrack != null) {
            context.send().error("There's nothing to resume as the player has been active here!").queue()
            return
        }

        //Poll next from queue and force that track to play.
        manager.openAudioConnection(context.voiceChannel, context)
        scheduler.nextTrack()

        context.send().info("Queue has been resumed.").queue()
    }
}
