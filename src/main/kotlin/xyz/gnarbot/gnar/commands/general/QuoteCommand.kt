package xyz.gnarbot.gnar.commands.general

import net.dv8tion.jda.api.EmbedBuilder
import xyz.gnarbot.gnar.commands.BotInfo
import xyz.gnarbot.gnar.commands.Command
import xyz.gnarbot.gnar.commands.CommandExecutor
import xyz.gnarbot.gnar.commands.Context
import java.time.format.DateTimeFormatter

@Command(
        aliases = ["quote", "quotemsg"],
        usage = "(message id) [#channel]",
        description = "Quote somebody else.."
)
@BotInfo(id = 19)
class QuoteCommand : CommandExecutor() {
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    override fun execute(context: Context, label: String, args: Array<String>) {
        if (args.isEmpty()) {
            context.bot.commandDispatcher.sendHelp(context, info)
            return
        }

        val targetChannel = when {
            context.message.mentionedChannels.size > 0 -> context.message.mentionedChannels[0]
            else -> context.message.textChannel
        }

        for (id in args) {
            try {
                if (id == targetChannel.asMention)
                    continue

                context.message.channel.retrieveMessageById(id).queue({
                    targetChannel.sendMessage(EmbedBuilder()
                            .setAuthor(it.author.name, null, it.author.avatarUrl)
                            .setDescription(it.contentDisplay)
                            .addField("Sent At", fmt.format(it.timeCreated), false)
                            .setFooter("ID: ${it.id}")
                            .build()
                    ).queue()
                }) { context.send().error("Invalid message ID `$id`.").queue() }
            } catch (e: IllegalArgumentException) {
                context.send().error("Invalid message ID `$id`.").queue()
            }
        }

        context.send().info("Sent quotes to the ${targetChannel.name} channel!").queue()
    }
}