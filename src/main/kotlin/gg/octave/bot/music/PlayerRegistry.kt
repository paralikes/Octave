package gg.octave.bot.music

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup
import com.sedmelluq.lava.extensions.youtuberotator.planner.AbstractRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingNanoIpRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block
import gg.octave.bot.Launcher
import gg.octave.bot.db.OptionsRegistry.ofGuild
import gg.octave.bot.music.sources.caching.CachingSourceManager
import gg.octave.bot.music.sources.spotify.SpotifyAudioSourceManager
import io.sentry.Sentry
import net.dv8tion.jda.api.entities.Guild
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class PlayerRegistry(private val bot: Launcher, val executor: ScheduledExecutorService) {
    private val log = LoggerFactory.getLogger("PlayerRegistry")

    val registry = ConcurrentHashMap<Long, MusicManager>(bot.configuration.musicLimit)
    val playerManager = DefaultAudioPlayerManager()

    @Throws(MusicLimitException::class)
    fun get(guild: Guild?): MusicManager {
        return registry.computeIfAbsent(guild!!.idLong) {
            if (size() >= bot.configuration.musicLimit && !ofGuild(guild).isPremium) {
                throw MusicLimitException()
            }

            MusicManager(bot, guild.id, this, playerManager)
        }
    }

    fun getExisting(id: Long) = registry[id]
    fun getExisting(guild: Guild?) = getExisting(guild!!.idLong)
    fun destroy(id: Long) {
        registry.remove(id)?.destroy()
    }

    fun destroy(guild: Guild?) = destroy(guild!!.idLong)
    fun contains(id: Long) = registry.containsKey(id)
    fun contains(guild: Guild) = registry.containsKey(guild.idLong)

    fun clear(force: Boolean) {
        log.info("Cleaning up players (forceful: $force)")
        val iterator = registry.entries.iterator()
        var count = 0

        while (iterator.hasNext()) {
            val entry = iterator.next()
            try {
                //Guild was long gone, dangling manager,
                val musicManager = entry.value

                if (musicManager.guild == null) {
                    return iterator.remove()
                }

                if (force || !musicManager.guild!!.selfMember.voiceState!!.inVoiceChannel() || musicManager.player.playingTrack == null) {
                    log.debug("Cleaning player {}", musicManager.guild!!.id)
                    musicManager.scheduler.queue.clear()
                    musicManager.destroy()
                    iterator.remove()
                    count++
                }
            } catch (e: Exception) {
                log.warn("Exception occured while trying to clean up id ${entry.key}", e)
            }
        }

        log.info("Finished cleaning up {} players.", count)
    }

    fun shutdown() = clear(true)
    fun size() = registry.size

    init {
        executor.scheduleAtFixedRate({ clear(false) }, 20, 10, TimeUnit.MINUTES)

        playerManager.frameBufferDuration = 5000
        playerManager.configuration.apply {
            isFilterHotSwapEnabled = true
            setFrameBufferFactory(::NonAllocatingAudioFrameBuffer)
        }

        val youtubeAudioSourceManager = YoutubeAudioSourceManager(true)
        val config = bot.configuration
        val credentials = bot.credentials

        if (config.ipv6Block.isNotEmpty()) {
            var planner: AbstractRoutePlanner
            val block = config.ipv6Block
            val blocks = listOf(Ipv6Block(block))

            if (config.ipv6Exclude.isEmpty()) {
                planner = RotatingNanoIpRoutePlanner(blocks)
            } else {
                try {
                    val blacklistedGW = InetAddress.getByName(config.ipv6Exclude)
                    planner = RotatingNanoIpRoutePlanner(blocks) { it != blacklistedGW }
                } catch (ex: Exception) {
                    planner = RotatingNanoIpRoutePlanner(blocks)
                    Sentry.capture(ex)
                    log.error("Error setting up IPv6 exclude GW, falling back to registering the whole block", ex)
                }
            }

            YoutubeIpRotatorSetup(planner)
                .forSource(youtubeAudioSourceManager)
                .setup()
        }

        val spotifyAudioSourceManager = SpotifyAudioSourceManager(
            credentials.spotifyClientId,
            credentials.spotifyClientSecret,
            youtubeAudioSourceManager
        )

        playerManager.registerSourceManager(CachingSourceManager())
        playerManager.registerSourceManager(spotifyAudioSourceManager)
        playerManager.registerSourceManager(youtubeAudioSourceManager)
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault())
        playerManager.registerSourceManager(GetyarnAudioSourceManager())
        playerManager.registerSourceManager(BandcampAudioSourceManager())
        playerManager.registerSourceManager(VimeoAudioSourceManager())
        playerManager.registerSourceManager(TwitchStreamAudioSourceManager())
        playerManager.registerSourceManager(BeamAudioSourceManager())
    }
}
