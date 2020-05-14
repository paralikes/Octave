package gg.octave.bot.music

import gg.octave.bot.Launcher
import gg.octave.bot.db.OptionsRegistry.ofGuild
import net.dv8tion.jda.api.entities.Guild
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class PlayerRegistry(private val bot: Launcher) {
    private val log = LoggerFactory.getLogger("PlayerRegistry")

    val registry = ConcurrentHashMap<Long, MusicManager>(bot.configuration.musicLimit)
    val playerManager = ExtendedAudioPlayerManager()

    @Throws(MusicLimitException::class)
    fun get(guild: Guild?) = registry.computeIfAbsent(guild!!.idLong) {
        if (size() >= bot.configuration.musicLimit && !ofGuild(guild).isPremium) {
            throw MusicLimitException()
        }

        MusicManager(bot, guild.id, this, playerManager)
    }

    fun getExisting(id: Long) = registry[id]
    fun getExisting(guild: Guild?) = getExisting(guild!!.idLong)
    fun destroy(id: Long) { registry.remove(id)?.destroy() }

    fun destroy(guild: Guild?) = destroy(guild!!.idLong)
    fun contains(id: Long) = registry.containsKey(id)
    fun contains(guild: Guild) = registry.containsKey(guild.idLong)

    fun size() = registry.size
}
