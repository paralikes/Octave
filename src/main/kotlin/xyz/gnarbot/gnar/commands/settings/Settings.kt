package xyz.gnarbot.gnar.commands.settings

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.annotations.SubCommand
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.parsers.MemberParser
import me.devoxin.flight.internal.parsers.RoleParser
import me.devoxin.flight.internal.parsers.TextChannelParser
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Member
import xyz.gnarbot.gnar.Bot
import xyz.gnarbot.gnar.utils.extensions.DEFAULT_SUBCOMMAND
import xyz.gnarbot.gnar.utils.extensions.data
import kotlin.reflect.KFunction

class Settings : Cog {
    @Command(aliases = ["setting", "set", "config", "configuration", "configure", "opts", "options"], userPermissions = [Permission.MANAGE_SERVER])
    fun settings(ctx: Context) = DEFAULT_SUBCOMMAND(ctx)

    @SubCommand(description = "Resets the settings for the guild.")
    fun reset(ctx: Context) {
        ctx.data.apply { reset(); save() }
        ctx.send {
            setColor(0x9570D3)
            setTitle("Settings")
            setDescription("The settings for this server have been reset.")
        }
    }

    @SubCommand(aliases = ["autodel"], description = "Toggle whether the bot auto-deletes its responses.")
    fun autodelete(ctx: Context, toggle: Boolean) {
        val data = ctx.data

        data.command.isAutoDelete = toggle
        data.save()

        val send = if (!toggle) "The bot will no longer automatically delete messages after 10 seconds."
        else "The bot will now delete messages after 10 seconds."

        ctx.send(send)
    }

    // music settings
}
