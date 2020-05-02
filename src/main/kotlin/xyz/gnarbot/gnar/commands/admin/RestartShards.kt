package xyz.gnarbot.gnar.commands.admin

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.entities.Cog
import net.dv8tion.jda.api.JDA

class RestartShards : Cog {
    @Command(description = "Restart bot shards (all/dead/#)", developerOnly = true)
    fun restartshards(ctx: Context, filter: String) {
        when (filter) {
            "all" -> {
                ctx.send("Rebooting all **${ctx.jda.shardInfo.shardTotal}** shards...")
                ctx.jda.shardManager?.restart()
            }
            "dead" -> {
                val deadShards = ctx.jda.shardManager?.shards?.filter { it.status != JDA.Status.CONNECTED }

                if (deadShards == null || deadShards.isEmpty()) {
                    return ctx.send("There are no dead shards.")
                }

                deadShards.forEach { it.shardManager?.restart(it.shardInfo.shardId) }
                ctx.send("Queued **${deadShards.size}** shards for reboot.")
            }
            else -> {
                if (filter[0].isDigit()) {
                    val num = filter[0].toInt()

                    if (num < 0 || num >= ctx.jda.shardInfo.shardTotal) {
                        return ctx.send("**$num** should be equal to or higher than 0, and less than ${ctx.jda.shardInfo.shardTotal}.")
                    }

                    ctx.jda.shardManager?.restart(num)
                    return ctx.send("Rebooting shard **$filter**...")
                }

                ctx.send("c'mon man you know the rules. Gimme `all`, `dead` or `#`.")
            }
        }
    }

    // TODO: Subcommands
}