package xyz.gnarbot.gnar

import com.jagrosh.jdautilities.waiter.EventWaiter
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary
import com.timgroup.statsd.NonBlockingStatsDClient
import io.sentry.Sentry
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.requests.RestAction
import org.slf4j.LoggerFactory
import xyz.gnarbot.gnar.db.Database
import xyz.gnarbot.gnar.entities.BotCredentials
import xyz.gnarbot.gnar.entities.Configuration
import xyz.gnarbot.gnar.entities.ExtendedShardManager
import xyz.gnarbot.gnar.listeners.BotListener
import xyz.gnarbot.gnar.listeners.VoiceListener
import xyz.gnarbot.gnar.music.PlayerRegistry
import java.io.File
import java.util.concurrent.Executors

object Launcher {
    private val log = LoggerFactory.getLogger(Launcher::class.java)

    val configuration = Configuration(File("bot.conf"))
    val credentials = BotCredentials(File("credentials.conf"))
    val database = Database("bot")
    val db = database

    val eventWaiter = EventWaiter()
    val datadog = NonBlockingStatsDClient("statsd", "localhost", 8125)

    val players = PlayerRegistry(this, Executors.newSingleThreadScheduledExecutor())

    lateinit var shardManager: ExtendedShardManager
        private set // begone foul modifications

    val loaded: Boolean
        get() = shardManager.shardsRunning == shardManager.shardsTotal

    @JvmStatic
    fun main(args: Array<String>) {
        println("+---------------------------------+")
        println("|           O c t a v e           |")
        println("| JDA: ${JDAInfo.VERSION}         |")
        println("| LP : ${PlayerLibrary.VERSION}   |")
        println("+---------------------------------+")

        RestAction.setPassContext(false)
        Sentry.init(configuration.sentryDsn)

        shardManager = ExtendedShardManager.create(credentials.token) {
            addEventListeners(eventWaiter, BotListener(), VoiceListener()/*, CommandClient */)
        }
    }
}
