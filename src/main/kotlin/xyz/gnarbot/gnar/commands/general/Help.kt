package xyz.gnarbot.gnar.commands.general

import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.entities.Cog

class Help : Cog {
    @Command
    suspend fun help(ctx: Context, command: String?) {
        if (command == null) {
            return sendCommands(ctx)
        }

        val command = ctx.commandClient.commands.findCommandByName(command)
            ?: ctx.commandClient.commands.findCommandByAlias(command)

        if (command != null) {
            return sendCommandHelp(ctx, command)
        }
    }

    suspend fun sendCommands(ctx: Context) {

    }

    suspend fun sendCommandHelp(ctx: Context, command: CommandFunction) {
        val description = buildString {
            appendln(command.properties.description)
            appendln()
            val triggerList = listOf(command.name, *command.properties.aliases)
            appendln("**Triggers:** ${triggerList.joinToString(", ")}")
            appendln("**Usage:** `${ctx.trigger}${command.name} ${command.arguments.joinToString(" ") { it.format(false) }}`")
            appendln()
            appendln("**Subcommands:**")

            val padEnd = command.subcommands.values.maxBy { it.name.length }?.name?.length ?: 15
            for (sc in command.subcommands.values) {
                appendln("`${sc.name.padEnd(padEnd, ' ')}:` ${sc.properties.description}")
            }
        }

        ctx.send {
            setColor(0x9570D3)
            setTitle("Help | ${command.name}")
            setDescription(description)
        }
    }

    suspend fun sendCategoryCommands(ctx: Context, commands: List<CommandFunction>) {

    }
}
