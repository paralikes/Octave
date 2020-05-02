package xyz.gnarbot.gnar.commands.general

import com.sun.management.OperatingSystemMXBean
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.entities.Cog
import net.dv8tion.jda.api.JDAInfo
import xyz.gnarbot.gnar.Launcher
import xyz.gnarbot.gnar.utils.Capacity
import java.lang.management.ManagementFactory
import java.text.DecimalFormat

class BotInfo : Cog {
    private val dpFormatter = DecimalFormat("0.00")

    @Command(aliases = ["about", "info", "stats"], description = "Show information about the bot.")
    fun botinfo(ctx: Context) {
        val commandSize = ctx.commandClient.commands.size

        // Uptime
        val s = ManagementFactory.getRuntimeMXBean().uptime / 1000
        val m = s / 60
        val h = m / 60
        val d = h / 24

        val osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)
        val procCpuUsage = dpFormatter.format(osBean.processCpuLoad * 100)
        val sysCpuUsage = dpFormatter.format(osBean.systemCpuLoad * 100)
        val ramUsedBytes = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
        val ramUsedCalculated = Capacity.calculate(ramUsedBytes)
        val ramUsedFormatted = dpFormatter.format(ramUsedCalculated.amount)
        val ramUsedPercent = dpFormatter.format(ramUsedBytes.toDouble() / Runtime.getRuntime().totalMemory() * 100)
        val guilds = ctx.jda.shardManager!!.guildCache.size()
        val users = ctx.jda.shardManager!!.userCache.size()

        ctx.send {
            setTitle("Bot Information")
            setThumbnail(ctx.jda.selfUser.avatarUrl)
            setDescription("Never miss a beat with Octave, a simple and easy to use Discord music bot delivering high quality audio to hundreds of thousands of servers. We support Youtube, Soundcloud, and more!")

            addField("CPU Usage", "${procCpuUsage}% JVM\n${sysCpuUsage}% SYS", true)
            addField("RAM Usage", "$ramUsedFormatted${ramUsedCalculated.unit} (${ramUsedPercent}%)", true)

            addField("Guilds", guilds.toString(), true)
            addField("Voice Connections", Launcher.players.size().toString(), true)

            addField("Cached Users", users.toString(), true)
            addField("Uptime", "${d}d ${h % 24}h ${m % 60}m ${s % 60}s", true)

            val general = buildString {
                append("Premium: **[Patreon](https://www.patreon.com/octavebot)**\n")
                append("Commands: **$commandSize**\n")
                append("Library: **[JDA ${JDAInfo.VERSION}](${JDAInfo.GITHUB})**\n")
            }
            addField("General", general, true)
            setFooter("${Thread.activeCount()} threads | Current Shard: ${ctx.jda.shardInfo.shardId}")
        }
    }
}
