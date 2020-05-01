package xyz.gnarbot.gnar.commands.settings

import net.dv8tion.jda.api.Permission
import xyz.gnarbot.gnar.commands.*

@Command(aliases = ["autodelete", "autodel"])
@BotInfo(
        id = 81,
        scope = Scope.GUILD,
        permissions = [Permission.MANAGE_SERVER],
        category = Category.SETTINGS
)
class AutoDeleteCommand : CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<String>) {
        val message: String
        if (context.data.command.isAutoDelete) {
            context.data.command.isAutoDelete = false
            context.data.save()
            message = "The bot will no longer automatically delete messages after 10 seconds."
        } else {
            context.data.command.isAutoDelete = true
            context.data.save()
            message = "The bot will now delete messages after 10 seconds."
        }
        context.send().info(message).queue()
    }
}