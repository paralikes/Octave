package xyz.gnarbot.gnar.commands.general

import xyz.gnarbot.gnar.commands.BotInfo
import xyz.gnarbot.gnar.commands.Command
import xyz.gnarbot.gnar.commands.CommandExecutor
import xyz.gnarbot.gnar.commands.Context

@Command(
        aliases = ["ping"],
        description = "Show the bot's current response time."
)
@BotInfo(id = 18)
class PingCommand : CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<String>) {
        context.jda.restPing.queue {
            context.send().embed {
                field("Rest", true, it)
                field("Web Socket", true, context.bot.shardManager.averageGatewayPing.toInt())
                footer { "Shard ID: ${context.jda.shardInfo.shardId}" }
            }.action().queue()
        }
    }
}