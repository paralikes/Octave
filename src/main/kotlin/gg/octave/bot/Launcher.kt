package gg.octave.bot

import com.jagrosh.jdautilities.waiter.EventWaiter
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary
import com.timgroup.statsd.NonBlockingStatsDClient
import gg.octave.bot.apis.patreon.PatreonAPI
import gg.octave.bot.apis.statsposter.StatsPoster
import gg.octave.bot.db.Database
import gg.octave.bot.entities.BotCredentials
import gg.octave.bot.entities.Configuration
import gg.octave.bot.entities.ExtendedShardManager
import gg.octave.bot.entities.framework.DefaultPrefixProvider
import gg.octave.bot.entities.framework.parsers.*
import gg.octave.bot.listeners.BotListener
import gg.octave.bot.listeners.FlightEventAdapter
import gg.octave.bot.listeners.VoiceListener
import gg.octave.bot.music.PlayerRegistry
import gg.octave.bot.utils.DiscordFM
import gg.octave.bot.utils.OctaveBot
import gg.octave.bot.utils.extensions.registerAlmostAllParsers
import io.sentry.Sentry
import me.devoxin.flight.api.CommandClient
import me.devoxin.flight.api.CommandClientBuilder
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.requests.RestAction
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object Launcher {
    val configuration = Configuration(File("bot.conf"))
    val credentials = BotCredentials(File("credentials.conf"))
    val database = Database("bot")
    val db = database

    val eventWaiter = EventWaiter()
    val datadog = NonBlockingStatsDClient("statsd", "localhost", 8125)
    val statsPoster = StatsPoster("201503408652419073")
    val patreon = PatreonAPI(credentials.patreonAccessToken)

    val players = PlayerRegistry(this)
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
        println("|        Revision ${OctaveBot.GIT_REVISION}        |")
        println("| JDA   : ${JDAInfo.VERSION.padEnd(24)}|")
        //println("| Flight: ${FlightInfo.VERSION.padEnd(24)}|")
        println("| LP    : ${PlayerLibrary.VERSION.padEnd(24)}|")
        println("+---------------------------------+")

        Sentry.init(configuration.sentryDsn)
        RestAction.setPassContext(false)

        commandClient = CommandClientBuilder()
                .setPrefixProvider(DefaultPrefixProvider())
                .registerAlmostAllParsers()
                .addCustomParser(ExtendedMemberParser())
                .addCustomParser(DurationParser())
                .addCustomParser(KeyTypeParser())
                .addCustomParser(BoostSettingParser())
                .addCustomParser(RepeatOptionParser())
                .addCustomParser(AutoShuffleParser())
                .setOwnerIds(*configuration.admins.toLongArray())
                .addEventListeners(FlightEventAdapter())
                .configureDefaultHelpCommand { enabled = false }
                .build()

        shardManager = ExtendedShardManager.create(credentials.token) {
            addEventListeners(eventWaiter, BotListener(), VoiceListener(), commandClient)
        }

        commandClient.commands.register("gg.octave.bot.commands")
        statsPoster.postEvery(30, TimeUnit.MINUTES)
    }
}
