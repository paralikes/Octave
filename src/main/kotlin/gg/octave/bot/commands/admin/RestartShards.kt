package gg.octave.bot.commands.admin

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.SubCommand
import me.devoxin.flight.api.entities.Cog
import net.dv8tion.jda.api.JDA

class RestartShards : Cog {
    @Command(aliases = ["rs"], description = "Restart bot shards.", developerOnly = true)
    fun restartshards(ctx: Context, shardId: Int) {
        if (shardId < 0 || shardId >= ctx.jda.shardInfo.shardTotal) {
            return ctx.send("**$shardId** should be equal to or higher than 0, and less than ${ctx.jda.shardInfo.shardTotal}.")
        }

        ctx.jda.shardManager?.restart(shardId)
        ctx.send("Rebooting shard **$shardId**...")
    }

    @SubCommand(description = "Restart any shards that aren't connected.")
    fun dead(ctx: Context) {
        val deadShards = ctx.jda.shardManager?.shards?.filter { it.status != JDA.Status.CONNECTED }?.takeIf { it.isNotEmpty() }
            ?: return ctx.send("There are no dead shards.")

        deadShards.forEach { it.shardManager?.restart(it.shardInfo.shardId) }
        ctx.send("Queued **${deadShards.size}** shards for reboot.")
    }

    @SubCommand(description = "Restart all shards.")
    fun all(ctx: Context) {
        ctx.send("Rebooting all **${ctx.jda.shardInfo.shardTotal}** shards...")
        ctx.jda.shardManager?.restart()
    }
}