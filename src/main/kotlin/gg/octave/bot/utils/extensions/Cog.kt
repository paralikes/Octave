package gg.octave.bot.utils.extensions

import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.entities.Cog

fun Cog.DEFAULT_SUBCOMMAND(ctx: Context) {
    if (ctx.invokedCommand !is CommandFunction) { // HUH
        return
    }

    val command = ctx.invokedCommand as CommandFunction
    val subcommands = command.subcommands.values
    val longestName = subcommands.maxBy { it.name.length }?.name?.length?.plus(1) ?: 15
    val commandList = subcommands.joinToString("\n") { "`${it.name.padEnd(longestName)}:` ${it.properties.description}" }

    ctx.send {
        setColor(0x9570D3)
        setTitle("${command.name} | Sub-Commands")
        setDescription(commandList)
        setFooter("Example: ${ctx.trigger}${command.name} ${subcommands.first().name}")
    }
}