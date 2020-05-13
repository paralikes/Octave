package gg.octave.bot.commands.admin

import gg.octave.bot.Launcher
import gg.octave.bot.utils.TextSplitter
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.entities.Cog
import net.dv8tion.jda.api.JDA

class ShardInfoCommand : Cog {
    @Command(aliases = ["shards", "shard"], description = "View shard information.", developerOnly = true)
    suspend fun shardinfo(ctx: Context) {
        val status = ctx.jda.shardManager!!.shards.joinToString("\n", transform = ::formatInfo)
        val pages = TextSplitter.split(status, 1920)

        for (page in pages) {
            ctx.sendAsync("```prolog\n ID |    STATUS |    PING | GUILDS |  USERS | REQUESTS |  VC\n$page```")
        }
    }

    private fun formatInfo(jda: JDA): String {
        val shardId = jda.shardInfo.shardId
        val totalShards = jda.shardInfo.shardTotal

        return "%3d | %9.9s | %7.7s | %6d | %6d | ---- WIP | %3d".format(
            shardId,
            jda.status,
            "${jda.gatewayPing}ms",
            jda.guildCache.size(),
            jda.userCache.size(),
            Launcher.players.registry.values.count { getShardIdForGuild(it.guildId, totalShards) == shardId }
        )
    }

    private fun getShardIdForGuild(guildId: String, shardCount: Int): Int {
        return ((guildId.toLong() shr 22) % shardCount).toInt()
    }
}
