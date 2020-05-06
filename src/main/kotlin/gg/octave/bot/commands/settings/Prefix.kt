package gg.octave.bot.commands.settings

import gg.octave.bot.Launcher
import gg.octave.bot.utils.extensions.data
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.SubCommand
import me.devoxin.flight.api.entities.Cog
import net.dv8tion.jda.api.Permission

class Prefix : Cog {
    @Command(description = "Sets the prefix for the server. Omit to reset.", userPermissions = [Permission.MANAGE_SERVER])
    fun prefix(ctx: Context, prefix: String?) {
        val data = ctx.data

        if (prefix == null) {
            val botPrefix = data.command.prefix ?: Launcher.configuration.prefix
            return ctx.send("My prefix is `$botPrefix`")
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

    @SubCommand(aliases = ["clear"], description = "Resets the bot's prefix back to default.")
    fun reset(ctx: Context) {
        ctx.data.let {
            it.command.prefix = null
            it.save()
        }

        return ctx.send("The prefix has been reset to `${Launcher.configuration.prefix}`.")
    }

    companion object {
        private val mention = Regex("<@!?(\\d+)>|<#(\\d+)>|<@&(\\d+)>")
    }
}
