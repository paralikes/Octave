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

    @SubCommand(description = "Sets the prefix for the server. Omit to reset.")
    fun prefix(ctx: Context, prefix: String?) {
        val data = ctx.data

        if (prefix == null) {
            data.command.prefix = prefix
            data.save()

            return ctx.send("The prefix has been reset to `${Bot.getInstance().configuration.prefix}`.")
        }

        if (prefix matches mention) {
            return ctx.send("The prefix cannot be set to a mention.")
        }

        if (data.command.prefix == prefix) {
            return ctx.send("The prefix is already set to `$prefix`.")
        }

        data.command.prefix = prefix
        data.save()

        ctx.send("Prefix has been set to `$prefix`.")
    }

    @SubCommand
    fun ignore(ctx: Context, type: String, @Greedy argument: String?) {
        if (type == "list") {
            return ignoreList(ctx)
        }

        if (type !in setOf("user", "channel", "role")) {
            return ctx.send("Invalid type: `$type`. You need to specify one of `user`, `channel`, `role` or `list`.")
        }

        if (argument == null) {
            return ctx.send("You need to mention a ${type.toLowerCase()}.")
        }

        val data = ctx.data
        val ignored = data.ignored

        when (type) {
            "user" -> {
                val parsed = MemberParser().parse(ctx, argument).takeIf { it.isPresent }?.get()
                    ?: return ctx.send("`${ctx.cleanContent(argument)}` is not a valid ${type.toLowerCase()}.")

                if (parsed.id in ignored.users) {
                    ignored.users.remove(parsed.id)
                    ctx.send("No longer ignoring user ${parsed.asMention}.")
                } else {
                    ignored.users.add(parsed.id)
                    ctx.send("Now ignoring user ${parsed.asMention}.")
                }
            }
            "channel" -> {
                val parsed = TextChannelParser().parse(ctx, argument).takeIf { it.isPresent }?.get()
                    ?: return ctx.send("`${ctx.cleanContent(argument)}` is not a valid ${type.toLowerCase()}.")

                if (parsed.id in ignored.channels) {
                    ignored.channels.remove(parsed.id)
                    ctx.send("No longer ignoring channel ${parsed.asMention}.")
                } else {
                    ignored.channels.add(parsed.id)
                    ctx.send("Now ignoring channel ${parsed.asMention}.")
                }
            }
            "role" -> {
                val parsed = RoleParser().parse(ctx, argument).takeIf { it.isPresent }?.get()
                    ?: return ctx.send("`${ctx.cleanContent(argument)}` is not a valid ${type.toLowerCase()}.")

                if (parsed.id in ignored.roles) {
                    ignored.roles.remove(parsed.id)
                    ctx.send("No longer ignoring role ${parsed.asMention}.")
                } else {
                    ignored.roles.add(parsed.id)
                    ctx.send("Now ignoring role ${parsed.asMention}.")
                }
            }
        }

        data.save()
    }

    fun <T: IMentionable> mapper(type: String, data: Set<String>, transform: (String) -> T?): String {
        if (data.isEmpty()) {
            return "No $type are ignored."
        }

        return data.mapNotNull(transform)
            .map(IMentionable::getAsMention)
            .joinToString("\n") { "• $it" }
    }

    fun ignoreList(ctx: Context) {
        val ignored = ctx.data.ignored

        ctx.send {
            setColor(0x9570D3)
            setTitle("Ignored Entities")
            addField("Users", mapper("users", ignored.users, ctx.guild!!::getMemberById), true)
            addField("Channel", mapper("channels", ignored.channels, ctx.guild!!::getTextChannelById), true)
            addField("Roles", mapper("roles", ignored.roles, ctx.guild!!::getRoleById), true)
        }
    }

    // ignore
    // manage commands
    // manage scope
    // music settings

    companion object {
        private val mention = Regex("<@!?(\\d+)>|<#(\\d+)>|<@&(\\d+)>")
    }
}
