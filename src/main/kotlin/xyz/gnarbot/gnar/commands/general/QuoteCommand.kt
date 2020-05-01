package xyz.gnarbot.gnar.commands.general

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.exceptions.ErrorResponseException
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
            return context.bot.commandDispatcher.sendHelp(context, info)
        }

        val targetChannel = context.message.mentionedChannels[0] ?: context.textChannel
        val messageIds = args.filter { it != targetChannel.asMention }.map { it.toLongOrNull()?.toString() }
        val invalid = messageIds.filter { it == null }

        if (invalid.isNotEmpty()) {
            return context.send().error("The following arguments are invalid:\n" +
                    invalid.joinToString("`, `", prefix = "`", postfix = "`")).queue()
        }

        for (messageId in args) {
            context.message.channel.retrieveMessageById(messageId).queue({
                targetChannel.sendMessage(EmbedBuilder()
                        .setAuthor(it.author.name, null, it.author.avatarUrl)
                        .setDescription(it.contentDisplay)
                        .addField("Sent At", fmt.format(it.timeCreated), false)
                        .setFooter("ID: ${it.id}")
                        .build()
                ).queue()
            }, {
                val message = if (it is ErrorResponseException) it.meaning else it.localizedMessage
                context.send().error(message).queue()
            })
        }

        context.send().info("Sent quotes to the ${targetChannel.name} channel!").queue()
    }
}