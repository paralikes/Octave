package gg.octave.bot.apis.statsposter

import org.slf4j.LoggerFactory
import gg.octave.bot.Launcher
import gg.octave.bot.apis.statsposter.websites.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class StatsPoster(botId: String) {
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    val websites = listOf(
        BotsForDiscord(botId, Launcher.credentials.botsForDiscord ?: ""),
        BotsGg(botId, Launcher.credentials.botsGg ?: ""),
        BotsOnDiscord(botId, Launcher.credentials.botsOnDiscord ?: ""),
        TopGg(botId, Launcher.credentials.topGg ?: "")
    )

    fun update(count: Long) {
        for (website in websites.filter(Website::canPost)) {
            website.update(count)
                .thenApply { it.body()?.close() }
                .exceptionally {
                    log.error("Updating server count failed for ${website.name}: ", it)
                    return@exceptionally null
                }
        }
    }

    fun postEvery(time: Long, unit: TimeUnit) {
        scheduler.scheduleWithFixedDelay({
            update(Launcher.shardManager.guildCache.size())
        }, time, time, unit)
    }

    companion object {
        private val log = LoggerFactory.getLogger(StatsPoster::class.java)
    }
}
