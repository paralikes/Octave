package xyz.gnarbot.gnar.commands.settings

import net.dv8tion.jda.api.Permission
import xyz.gnarbot.gnar.commands.BotInfo
import xyz.gnarbot.gnar.commands.Category
import xyz.gnarbot.gnar.commands.Command
import xyz.gnarbot.gnar.commands.Context
import xyz.gnarbot.gnar.commands.template.CommandTemplate
import xyz.gnarbot.gnar.commands.template.annotations.Description

@Command(
        aliases = ["djonly"],
        description = "Make the bot DJ only, or not (enable/disable)."
)
@BotInfo(
        id = 58,
        category = Category.SETTINGS,
        toggleable = false,
        permissions = [Permission.MANAGE_SERVER]
)
class DJOnlyCommand : CommandTemplate() {
    @Description("Enable DJ Only mode.")
    fun enable(context: Context) {
        context.data.command.isDjOnlyMode = true
        context.send().info("Enabled DJ-only mode.").queue()
    }

    @Description("Disable DJ Only mode.")
    fun disable(context: Context) {
        context.data.command.isDjOnlyMode = false
        context.send().info("Disabled DJ-only mode.").queue()
    }
}
