package xyz.gnarbot.gnar.commands.general

import net.dv8tion.jda.api.Permission
import xyz.gnarbot.gnar.commands.BotInfo
import xyz.gnarbot.gnar.commands.Command
import xyz.gnarbot.gnar.commands.CommandExecutor
import xyz.gnarbot.gnar.commands.Context

@Command(
        aliases = ["invite", "invitebot"],
        description = "Get a link to invite the bot to your server."
)
@BotInfo(id = 17)
class InviteBotCommand : CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<String>) {
        val link = context.jda.getInviteUrl(Permission.ADMINISTRATOR)
        context.send().embed {
            title { "Get Octave on your server!" }
            description { "__**[Click to invite Octave to your server.]($link)**__" }
        }.action().queue()
    }
}