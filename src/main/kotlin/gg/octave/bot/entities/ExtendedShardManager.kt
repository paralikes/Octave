package gg.octave.bot.entities

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory
import gg.octave.bot.Launcher
import gg.octave.bot.entities.sharding.BucketedController
import gg.octave.bot.utils.IntentHelper
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.util.*

class ExtendedShardManager(private val shardManager: ShardManager) : ShardManager by shardManager {
    fun openPrivateChannel(userId: Long): RestAction<PrivateChannel> {
        return shards.first { it != null }.openPrivateChannelById(userId)
    }

    companion object {
        fun create(token: String, apply: DefaultShardManagerBuilder.() -> Unit = {}): ExtendedShardManager {
            return DefaultShardManagerBuilder.create(token, IntentHelper.enabledIntents)
                .apply {
                    // General
                    setActivityProvider { Activity.playing(Launcher.configuration.game.format(it)) }

                    // Gateway
                    setSessionController(BucketedController(Launcher.configuration.bucketFactor, 215616923168276480L))
                    setShardsTotal(Launcher.credentials.totalShards)
                    setShards(Launcher.credentials.shardStart, Launcher.credentials.shardEnd - 1)
                    setMaxReconnectDelay(32)

                    // Audio
                    setAudioSendFactory(NativeAudioSendFactory(1000))

                    // Performance
                    setBulkDeleteSplittingEnabled(false)
                    disableCache(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS))
                    setMemberCachePolicy(MemberCachePolicy.VOICE)
                }
                .apply(apply)
                .build()
                .let(::ExtendedShardManager)
        }
    }
}
