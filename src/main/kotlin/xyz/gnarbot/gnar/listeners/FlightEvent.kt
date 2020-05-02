package xyz.gnarbot.gnar.listeners

import io.sentry.Sentry
import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.SubCommandFunction
import me.devoxin.flight.api.exceptions.BadArgument
import me.devoxin.flight.api.hooks.DefaultCommandEventAdapter
import net.dv8tion.jda.api.Permission
import xyz.gnarbot.gnar.Bot
import xyz.gnarbot.gnar.utils.commands.helpers.DJ
import xyz.gnarbot.gnar.utils.commands.helpers.MusicCog
import xyz.gnarbot.gnar.utils.commands.helpers.PermissionRequirement
import xyz.gnarbot.gnar.utils.commands.helpers.RoleRequirement
import xyz.gnarbot.gnar.utils.extensions.data
import xyz.gnarbot.gnar.utils.extensions.selfMember
import xyz.gnarbot.gnar.utils.getDisplayValue
import xyz.gnarbot.gnar.utils.hasAnyRoleId
import xyz.gnarbot.gnar.utils.hasAnyRoleNamed
import javax.management.relation.Role
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

class FlightEvent : DefaultCommandEventAdapter() {

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

        if(command.method.hasAnnotation<DJ>() || ctx.data.command.isDjOnlyMode) {
            return isDJ(ctx) || ctx.data.music.isDisableDj
        }

        if(command.method.hasAnnotation<RoleRequirement>()) {
            val requirement = command.method.findAnnotation<RoleRequirement>()!!.role
            return ctx.member!!.hasAnyRoleNamed(requirement)
        }

        if(command.method.hasAnnotation<PermissionRequirement>()) {
            val permission = command.method.findAnnotation<PermissionRequirement>()!!.permission
            return ctx.member!!.hasPermission(permission)
        }

        if (command.cog is MusicCog) {
            return (command.cog as MusicCog).check(ctx)
        }

        return true
    }

    override fun onCommandPostInvoke(ctx: Context, command: CommandFunction, failed: Boolean) {
        Bot.getInstance().datadog.incrementCounter("bot.commands_ran")
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

            val djRolePresent = if(djRole != null) context.member!!.hasAnyRoleId(djRole) else false
            val memberAmount = if(memberSize != null) memberSize <= 2 else false
            val admin = context.member!!.permissions.contains(Permission.MANAGE_SERVER) || context.member!!.permissions.contains(Permission.ADMINISTRATOR)

            if(context.member!!.hasAnyRoleNamed("DJ") || djRolePresent || memberAmount || admin) {
                return true
            }

            return false;
        }
    }
}
