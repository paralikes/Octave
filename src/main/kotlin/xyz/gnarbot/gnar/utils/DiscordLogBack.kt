package xyz.gnarbot.gnar.utils

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import club.minnced.discord.webhook.WebhookClient

class DiscordLogBack : AppenderBase<ILoggingEvent>() {
    private var patternLayout: PatternLayout? = null

    override fun append(event: ILoggingEvent) {
        if (client == null) {
            return
        }

        if (!event.level.isGreaterOrEqual(Level.INFO)) {
            return
        }

        var content = patternLayout!!.doLayout(event)

        if (!content.contains("UnknownHostException")) //Spams the shit out of console, not needed
            if (content.length > 2000) {
                val sb = StringBuilder(":warning: Received a message but it was too long. ")
                val url = Utils.hasteBin(content)
                sb.append(url ?: "Error while posting to HasteBin.")
                content = sb.toString()
            }

        client?.send(content)
    }

    override fun start() {
        patternLayout = PatternLayout()
        patternLayout!!.context = getContext()
        patternLayout!!.pattern = "`%d{HH:mm:ss}` `%t/%level` `%logger{0}` %msg"
        patternLayout!!.start()

        super.start()
    }

    companion object {
        private var client: WebhookClient? = null

        fun enable(webhookClient: WebhookClient) {
            client = webhookClient
        }
    }
}
