package xyz.gnarbot.gnar.listeners

import io.sentry.Sentry
import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.SubCommandFunction
import me.devoxin.flight.api.exceptions.BadArgument
import me.devoxin.flight.api.hooks.DefaultCommandEventAdapter
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import xyz.gnarbot.gnar.Launcher
import xyz.gnarbot.gnar.db.guilds.GuildData
import xyz.gnarbot.gnar.entities.framework.CheckVoiceState
import xyz.gnarbot.gnar.entities.framework.MusicCog
import xyz.gnarbot.gnar.entities.framework.DJ
import xyz.gnarbot.gnar.utils.extensions.config
import xyz.gnarbot.gnar.utils.extensions.data
import xyz.gnarbot.gnar.utils.extensions.selfMember
import xyz.gnarbot.gnar.utils.getDisplayValue
import xyz.gnarbot.gnar.utils.hasAnyRoleId
import xyz.gnarbot.gnar.utils.hasAnyRoleNamed
import kotlin.reflect.full.hasAnnotation

class FlightEventAdapter : DefaultCommandEventAdapter() {

    override fun onBadArgument(ctx: Context, command: CommandFunction, error: BadArgument) {
        val commandFormat = buildString {
            append(ctx.trigger)
            append(command.name)
            append(" ")

            if (ctx.invokedCommand is SubCommandFunction) {
                append((ctx.invokedCommand as SubCommandFunction).name)
                append(" ")
            }

            for (arg in ctx.invokedCommand.arguments) {
                append(arg.format(true))
                append(" ")
            }

            appendln()

            val badArgument = error.argument.format(true)
            val badArgumentIndex = this.indexOf(badArgument)

            append(" ".repeat(badArgumentIndex))
            appendln("^".repeat(badArgument.length))
            appendln()
            append("You provided an invalid argument for ${error.argument.name}.")
        }

        ctx.send("```\n$commandFormat```")
    }

    override fun onParseError(ctx: Context, command: CommandFunction, error: Throwable) {
        Sentry.capture(error)
        ctx.send("An error was encountered while parsing the arguments for this command.\n" +
            "The error has been logged. We apologise for any inconvenience caused!")
    }

    override fun onCommandError(ctx: Context, command: CommandFunction, error: Throwable) {
        Sentry.capture(error)
        ctx.send("The command encountered an error, which has been logged.\n" +
            "We apologise for any inconvenience caused!")
    }

    override fun onCommandCooldown(ctx: Context, command: CommandFunction, cooldown: Long) {
        ctx.send("This command is on cool-down. Wait ${getDisplayValue(cooldown, true)}.")
    }

    @ExperimentalStdlibApi
    override fun onCommandPreInvoke(ctx: Context, command: CommandFunction): Boolean {
        if (ctx.guild == null) {
            return false
        }

        if (ctx.member!!.hasPermission(Permission.ADMINISTRATOR) || ctx.member!!.hasPermission(Permission.MANAGE_SERVER)) {
            return true
        }

        val data = ctx.data

        //TODO: Add messages to all of this?
        if(isIgnored(ctx, data, ctx.member!!))
            return false

        if (command.cog is MusicCog) {
            if(command.method.hasAnnotation<CheckVoiceState>()) {
                if(ctx.member!!.voiceState?.channel == null)
                    return false

                if(ctx.member!!.voiceState?.channel == ctx.guild!!.afkChannel)
                    return false

                if(data.music.channels.isNotEmpty() && ctx.member!!.voiceState?.channel?.id !in data.music.channels)
                    return false
            }

            if (command.method.hasAnnotation<DJ>() || data.command.isDjOnlyMode) {
                return isDJ(ctx) || data.music.isDisableDj
            }

            return (command.cog as MusicCog).check(ctx)
        }

        return true
    }

    private fun isIgnored(ctx: Context, data: GuildData, member: Member): Boolean {
        return (data.ignored.users.contains(member.user.id)
                || data.ignored.channels.contains(ctx.textChannel!!.id)
                || data.ignored.roles.any { id -> member.roles.any { it.id == id } })
                && !member.hasPermission(Permission.ADMINISTRATOR)
                && member.user.idLong !in ctx.config.admins
    }


    override fun onCommandPostInvoke(ctx: Context, command: CommandFunction, failed: Boolean) {
        Launcher.datadog.incrementCounter("bot.commands_ran")
    }

    override fun onBotMissingPermissions(ctx: Context, command: CommandFunction, permissions: List<Permission>) {
        val formatted = permissions.joinToString("`\n`", prefix = "`", postfix = "`") { it.getName() }
        ctx.send("I need the following permissions:\n$formatted")
    }

    override fun onUserMissingPermissions(ctx: Context, command: CommandFunction, permissions: List<Permission>) {
        val formatted = permissions.joinToString("`\n`", prefix = "`", postfix = "`") { it.getName() }
        ctx.send("You need the following permissions:\n$formatted")
    }

    companion object {
        fun isDJ(context: Context): Boolean {
            val memberSize = context.selfMember!!.voiceState?.channel?.members?.size
            val djRole = context.data.command.djRole

            val djRolePresent = if (djRole != null) context.member!!.hasAnyRoleId(djRole) else false
            val memberAmount = if (memberSize != null) memberSize <= 2 else false
            val admin = context.member!!.permissions.contains(Permission.MANAGE_SERVER) || context.member!!.permissions.contains(Permission.ADMINISTRATOR)

            if (context.member!!.hasAnyRoleNamed("DJ") || djRolePresent || memberAmount || admin) {
                return true
            }

            return false
        }
    }
}
