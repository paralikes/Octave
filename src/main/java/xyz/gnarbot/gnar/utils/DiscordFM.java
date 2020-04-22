package xyz.gnarbot.gnar.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.gnarbot.gnar.Bot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DiscordFM {
    private static final Logger log = LoggerFactory.getLogger(DiscordFM.class);
    public static final String[] LIBRARIES = {
            "electro hub", "chill corner", "korean madness",
            "japanese lounge", "classical", "retro renegade",
            "metal mix", "hip hop", "electro swing", "christmas", "halloween",
            "purely pop", "rock n roll", "coffee house jazz", "funk",
    };

    private static Map<String, List<String>> cache = new HashMap<>(LIBRARIES.length);

    public DiscordFM() {
        for (String lib : LIBRARIES) {
            try (InputStream is = DiscordFM.class.getResourceAsStream("/dfm/" + lib + ".txt")) {
                if (is == null) {
                    log.warn("Playlist {} does not exist, skipping...", lib);
                    continue;
                }

                List<String> collect = Arrays.stream(IOUtils.toString(is, StandardCharsets.UTF_8).split("\n"))
                        .parallel()
                        .filter(si -> si.startsWith("https://"))
                        .collect(Collectors.toList());
                cache.put(lib, collect);

                log.info("Added {} tracks from playlist {}", collect.size(), lib);
            } catch (IOException e) {
                log.error("Failed to load playlist {}", lib, e);
            }
        }
    }

    public String getRandomSong(String library) {
        try {
            List<String> urls = cache.get(library);
            return urls.get((int) (Math.random() * urls.size())).trim();
        } catch (Exception e) {
            Bot.getLogger().error("DiscordFM Error", e);
            return "https://www.youtube.com/watch?v=D7npse9n-Yw"; //Technical Difficulties video
        }
    }
}
