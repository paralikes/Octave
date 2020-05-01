package xyz.gnarbot.gnar.commands.general

import xyz.gnarbot.gnar.commands.BotInfo
import xyz.gnarbot.gnar.commands.Command
import xyz.gnarbot.gnar.commands.CommandExecutor
import xyz.gnarbot.gnar.commands.Context

@Command(
        aliases = ["shardnumber", "activeshard"],
        description = "Display your shard number"
)
@BotInfo(id = 103)
class ActiveShardCommand : CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<String>) {
        context.send().embed {
            title { "Shard Checker" }
            description { "You're currently in shard ${context.jda.shardInfo.shardId}" }
        }.action().queue()
    }
}