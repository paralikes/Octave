package xyz.gnarbot.gnar.entities.sharding

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.utils.SessionController.SessionConnectNode
import net.dv8tion.jda.api.utils.SessionController.ShardedGateway
import net.dv8tion.jda.api.utils.SessionControllerAdapter
import java.util.*
import java.util.concurrent.PriorityBlockingQueue
import javax.annotation.Nonnull

//created by napster (https://github.com/napstr)
class PrioritizingSessionController(private val homeGuildId: Long) : SessionControllerAdapter(), Comparator<SessionConnectNode> {
    init {
        connectQueue = PriorityBlockingQueue(1, this)
    }

    override fun compare(s1: SessionConnectNode, s2: SessionConnectNode): Int {
        //if one of the shards is containing the home guild, do it first always
        if (isHomeShard(s1))
            return -1

        if (isHomeShard(s2))
            return 1

        //if both or none are reconnecting, order by their shard ids
        if (s1.isReconnect && s2.isReconnect || !s1.isReconnect && !s2.isReconnect)
            return s1.shardInfo.shardId - s2.shardInfo.shardId

        //otherwise prefer the one that is reconnecting
        return when {
            s1.isReconnect -> -1
            else -> 1
        }
    }

    private fun getHomeShardId(shardTotal: Int): Long {
        return (homeGuildId shr 22) % shardTotal
    }

    private fun isHomeShard(node: SessionConnectNode): Boolean {
        return homeGuildId != -1L && getHomeShardId(node.shardInfo.shardTotal) == node.shardInfo.shardId.toLong()
    }

    @Nonnull
    override fun getShardedGateway(@Nonnull api: JDA): ShardedGateway {
        throw UnsupportedOperationException()
    }

    @Nonnull
    override fun getGateway(@Nonnull api: JDA): String {
        throw UnsupportedOperationException()
    }

    override fun getGlobalRatelimit(): Long {
        throw UnsupportedOperationException()
    }

    override fun setGlobalRatelimit(ratelimit: Long) {
        throw UnsupportedOperationException()
    }
}
