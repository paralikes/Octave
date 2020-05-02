package xyz.gnarbot.gnar.listeners

import io.sentry.Sentry
import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.exceptions.BadArgument
import me.devoxin.flight.api.hooks.DefaultCommandEventAdapter
import net.dv8tion.jda.api.Permission
import xyz.gnarbot.gnar.Bot
import xyz.gnarbot.gnar.utils.extensions.MusicCog

class FlightEvent : DefaultCommandEventAdapter() {

    override fun onBadArgument(ctx: Context, command: CommandFunction, error: BadArgument) {
        super.onBadArgument(ctx, command, error)
    }

    override fun onCommandCooldown(ctx: Context, command: CommandFunction, cooldown: Long) {
        super.onCommandCooldown(ctx, command, cooldown)
    }

    override fun onCommandError(ctx: Context, command: CommandFunction, error: Throwable) {
        Sentry.capture(error)
        super.onCommandError(ctx, command, error)
    }

    override fun onCommandPreInvoke(ctx: Context, command: CommandFunction): Boolean {
        if (ctx.guild == null) {
            return false
        }

        if(command.cog is MusicCog)
            return (command.cog as MusicCog).check(ctx)

        return super.onCommandPreInvoke(ctx, command)
    }

    override fun onCommandPostInvoke(ctx: Context, command: CommandFunction, failed: Boolean) {
        Bot.getInstance().datadog.incrementCounter("bot.commands_ran")
    }

    override fun onParseError(ctx: Context, command: CommandFunction, error: Throwable) {
        super.onParseError(ctx, command, error)
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
