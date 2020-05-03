package xyz.gnarbot.gnar.commands.general

import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.entities.Cog
import xyz.gnarbot.gnar.utils.extensions.config
import xyz.gnarbot.gnar.utils.extensions.data

class Help : Cog {
    private val categoryAlias = mapOf("Search" to "Music", "Dj" to "Music")

    @Command
    fun help(ctx: Context, command: String?) {
        if (command == null) {
            return sendCommands(ctx)
        }

        val command = ctx.commandClient.commands.findCommandByName(command)
            ?: ctx.commandClient.commands.findCommandByAlias(command)

        if (command != null) {
            return sendCommandHelp(ctx, command)
        }
    }

    fun sendCommands(ctx: Context) {
        val guildTrigger = ctx.data.command.prefix ?: ctx.config.prefix
        val categories = ctx.commandClient.commands.values
            .groupBy { categoryAlias[it.category] ?: it.category }
            .filter { ctx.author.idLong in ctx.commandClient.ownerIds || it.key != "Admin" }

        ctx.send {
            setColor(0x9571D3)
            setTitle("Bot Commands")
            setDescription("The prefix of the bot on this server is `$guildTrigger`")
            for ((key, commands) in categories) {
                val fieldName = "$key — ${commands.size}"
                val commandList = commands.joinToString("`, `", prefix = "`", postfix = "`") { it.name }
                addField(fieldName, commandList, false)
            }
            setFooter("For more information try ${guildTrigger}help (command) " +
                "or ${guildTrigger}help (category), ex: ${guildTrigger}help bassboost or ${guildTrigger}help play")
        }
    }

    fun sendCommandHelp(ctx: Context, command: CommandFunction) {
        val description = buildString {
            appendln(command.properties.description)
            appendln()
            val triggerList = listOf(command.name, *command.properties.aliases)
            appendln("**Triggers:** ${triggerList.joinToString(", ")}")
            append("**Usage:** `${ctx.trigger}")
            append(command.name)
            if (command.arguments.isNotEmpty()) {
                appendln(" ${command.arguments.joinToString(" ") { it.format(false) }}`")
            } else {
                appendln("`")
            }
            appendln()
            appendln("**Subcommands:**")


            if (command.subcommands.isNotEmpty()) {
                val padEnd = command.subcommands.values.maxBy { it.name.length }?.name?.length ?: 15
                for (sc in command.subcommands.values.toSet()) {
                    appendln("`${sc.name.padEnd(padEnd, ' ')}:` ${sc.properties.description}")
                }
            } else {
                appendln("*None.*")
            }
        }

        ctx.send {
            setColor(0x9570D3)
            setTitle("Help | ${command.name}")
            setDescription(description)
        }
    }

    fun sendCategoryCommands(ctx: Context, commands: List<CommandFunction>) {

    }
}
