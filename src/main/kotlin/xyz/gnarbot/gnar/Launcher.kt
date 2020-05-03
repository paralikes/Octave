package xyz.gnarbot.gnar

import com.jagrosh.jdautilities.waiter.EventWaiter
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary
import com.timgroup.statsd.NonBlockingStatsDClient
import io.sentry.Sentry
import me.devoxin.flight.api.CommandClient
import me.devoxin.flight.api.CommandClientBuilder
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.requests.RestAction
import org.slf4j.LoggerFactory
import xyz.gnarbot.gnar.apis.patreon.PatreonAPI
import xyz.gnarbot.gnar.apis.statsposter.StatsPoster
import xyz.gnarbot.gnar.db.Database
import xyz.gnarbot.gnar.entities.BotCredentials
import xyz.gnarbot.gnar.entities.Configuration
import xyz.gnarbot.gnar.entities.ExtendedShardManager
import xyz.gnarbot.gnar.entities.framework.DefaultPrefixProvider
import xyz.gnarbot.gnar.entities.framework.DurationParser
import xyz.gnarbot.gnar.listeners.BotListener
import xyz.gnarbot.gnar.listeners.FlightEventAdapter
import xyz.gnarbot.gnar.listeners.VoiceListener
import xyz.gnarbot.gnar.music.PlayerRegistry
import xyz.gnarbot.gnar.utils.DiscordFM
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object Launcher {
    private val log = LoggerFactory.getLogger(Launcher::class.java)

    val configuration = Configuration(File("bot.conf"))
    val credentials = BotCredentials(File("credentials.conf"))
    val database = Database("bot")
    val db = database

    val eventWaiter = EventWaiter()
    val datadog = NonBlockingStatsDClient("statsd", "localhost", 8125)
    val statsPoster = StatsPoster("201503408652419073")
    val patreon = PatreonAPI(credentials.patreonAccessToken)

    val players = PlayerRegistry(this, Executors.newSingleThreadScheduledExecutor())
    val discordFm = DiscordFM()

    lateinit var shardManager: ExtendedShardManager
        private set // begone foul modifications

    lateinit var commandClient: CommandClient
        private set // uwu

    val loaded: Boolean
        get() = shardManager.shardsRunning == shardManager.shardsTotal

    @ExperimentalStdlibApi
    @JvmStatic
    fun main(args: Array<String>) {
        println("+---------------------------------+")
        println("|           O c t a v e           |")
        println("| JDA: ${JDAInfo.VERSION.padEnd(27)}|")
        println("| LP : ${PlayerLibrary.VERSION.padEnd(27)}|")
        println("+---------------------------------+")

        Sentry.init(configuration.sentryDsn)
        RestAction.setPassContext(false)

        commandClient = CommandClientBuilder()
            .setPrefixProvider(DefaultPrefixProvider())
            .registerDefaultParsers()
            .addCustomParser(DurationParser())
            .setOwnerIds(*configuration.admins.toLongArray())
            .addEventListeners(FlightEventAdapter())
            .configureDefaultHelpCommand { enabled = false }
            .build()

        shardManager = ExtendedShardManager.create(credentials.token) {
            addEventListeners(eventWaiter, BotListener(), VoiceListener(), commandClient)
        }

        commandClient.commands.register("xyz.gnarbot.gnar.commands")
        statsPoster.postEvery(30, TimeUnit.MINUTES)
    }
}
