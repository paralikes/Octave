package xyz.gnarbot.gnar.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import club.minnced.discord.webhook.WebhookClient;

public class DiscordLogBack extends AppenderBase<ILoggingEvent> {
    private static WebhookClient client;

    private PatternLayout patternLayout;

    public static void disable() {
        client = null;
    }

    public static void enable(WebhookClient webhookClient) {
        client = webhookClient;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (client == null)
            return;

        if (!event.getLevel().isGreaterOrEqual(Level.INFO))
            return;

        String content = patternLayout.doLayout(event);

        if (!content.contains("UnknownHostException")) //Spams the shit out of console, not needed
            if (content.length() > 2000) {
                StringBuilder sb = new StringBuilder(":warning: Received a message but it was too long. ");

                String url = Utils.hasteBin(content);
                sb.append(url != null ? url : "Error while posting to HasteBin.");

                content = sb.toString();
            }

        client.send(content);
    }

    @Override
    public void start() {
        patternLayout = new PatternLayout();
        patternLayout.setContext(getContext());
        patternLayout.setPattern("`%d{HH:mm:ss}` `%t/%level` `%logger{0}` %msg");
        patternLayout.start();

        super.start();
    }
}