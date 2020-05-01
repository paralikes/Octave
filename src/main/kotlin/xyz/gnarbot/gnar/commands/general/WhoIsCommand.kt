package xyz.gnarbot.gnar.commands.general

import org.apache.commons.lang3.StringUtils
import xyz.gnarbot.gnar.commands.BotInfo
import xyz.gnarbot.gnar.commands.Command
import xyz.gnarbot.gnar.commands.CommandExecutor
import xyz.gnarbot.gnar.commands.Context
import xyz.gnarbot.gnar.commands.template.parser.Parsers
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

@Command(
        aliases = ["user", "whois", "who"],
        usage = "[user]",
        description = "Get information on a user."
)
@BotInfo(id = 22)
class WhoIsCommand : CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<String>) {
        val member = when {
            args.isEmpty() -> context.member
            else -> Parsers.MEMBER.parse(context, args[0])
        }

        if (member == null) {
            context.send().issue("You did not mention a valid user.").queue()
            return
        }

        val roleStr = member.roles.stream().map { it.name }.collect(Collectors.joining(", "))

        context.send().embed {
            title { "User Information for ${member.effectiveName}" }
            color { member.color }
            thumbnail { member.user.effectiveAvatarUrl }
            field("Name", true, member.user.name)
            field("Discriminator", true, member.user.discriminator)
            field("ID", false, member.user.id)
            field("Status", true, StringUtils.capitalize(member.onlineStatus.key))
            field("Creation Time", false, member.user.timeCreated.format(DateTimeFormatter.RFC_1123_DATE_TIME))
            field("Join Date", false, member.timeJoined.format(DateTimeFormatter.RFC_1123_DATE_TIME))
            field("Nickname", true, if (member.nickname != null) member.nickname else "No nickname.")
            field("Roles", true, roleStr)
        }.action().queue()
    }
}