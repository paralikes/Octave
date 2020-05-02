package xyz.gnarbot.gnar.listeners

import io.sentry.Sentry
import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.SubCommandFunction
import me.devoxin.flight.api.exceptions.BadArgument
import me.devoxin.flight.api.hooks.DefaultCommandEventAdapter
import net.dv8tion.jda.api.Permission
import xyz.gnarbot.gnar.Bot
import xyz.gnarbot.gnar.utils.Utils
import xyz.gnarbot.gnar.utils.extensions.MusicCog
import xyz.gnarbot.gnar.utils.getDisplayValue

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

    override fun onCommandPreInvoke(ctx: Context, command: CommandFunction): Boolean {
        if (ctx.guild == null) {
            return false
        }

        //TODO pre-invoke checks (djlock, etc)
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

}
