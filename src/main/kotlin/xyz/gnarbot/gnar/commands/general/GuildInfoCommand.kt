package xyz.gnarbot.gnar.commands.general

import net.dv8tion.jda.api.entities.MessageEmbed
import org.apache.commons.lang3.StringUtils
import xyz.gnarbot.gnar.commands.BotInfo
import xyz.gnarbot.gnar.commands.Command
import xyz.gnarbot.gnar.commands.CommandExecutor
import xyz.gnarbot.gnar.commands.Context
import xyz.gnarbot.gnar.utils.Utils
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

@Command(
        aliases = ["guild", "server"],
        description = "Get information this guild."
)
@BotInfo(id = 16)
class GuildInfoCommand : CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<String>) {
        val guild = context.guild
        val roleStr = guild.roleCache.stream().map { it.name }.collect(Collectors.joining(", "))

        context.send().embed {
            title { "Guild Information" }
            thumbnail { context.guild.iconUrl }
            field("Name", false, guild.name)
            field("ID", false, guild.id)
            //This will add it to the cache, so it'll only be complete()'d once.
            field("Owner", false, if (guild.owner == null) guild.retrieveOwner(false).complete().asMention else guild.owner!!.asMention)
            field("Region", true, guild.region.getName())
            field("Members", true, guild.memberCount)
            field("Text Channels", true, guild.textChannelCache.size())
            field("Voice Channels", true, guild.voiceChannelCache.size())
            field("Verification Level", true, guild.verificationLevel)
            field("Emotes", true, guild.emoteCache.size())
            field("Premium", true,
                    if (context.isGuildPremium || context.data.isPremium)
                        "Premium Server"
                    else
                        "This guild does not have the premium status.\nVisit our __**[Patreon](https://www.patreon.com/octavebot)**__ to find out more.")
            field("Creation Time", false, guild.timeCreated.format(DateTimeFormatter.RFC_1123_DATE_TIME))
            field("Roles", false, StringUtils.truncate(roleStr.toString(), 900))
        }.action().queue()
    }
}