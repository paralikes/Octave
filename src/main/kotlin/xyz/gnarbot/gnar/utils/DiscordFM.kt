package xyz.gnarbot.gnar.utils

import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import xyz.gnarbot.gnar.Bot
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.stream.Collectors

class DiscordFM {
    fun getRandomSong(library: String): String {
        return try {
            val urls = cache[library]!!
            urls[(Math.random() * urls.size).toInt()].trim { it <= ' ' }
        } catch (e: Exception) {
            Bot.getLogger().error("DiscordFM Error", e)
            "https://www.youtube.com/watch?v=D7npse9n-Yw" //Technical Difficulties video
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(DiscordFM::class.java)
        val LIBRARIES = arrayOf(
                "electro hub", "chill corner", "korean madness",
                "japanese lounge", "classical", "retro renegade",
                "metal mix", "hip hop", "electro swing", "christmas", "halloween",
                "purely pop", "rock n roll", "coffee house jazz", "funk")
        private val cache: MutableMap<String, List<String>> = HashMap(LIBRARIES.size)
    }

    init {
        for (lib in LIBRARIES) {
            try {
                DiscordFM::class.java.getResourceAsStream("/dfm/$lib.txt").use {
                    if (it == null) {
                        log.warn("Playlist {} does not exist, skipping...", lib)
                    } else {
                        val collect = Arrays
                                .stream(IOUtils.toString(it, StandardCharsets.UTF_8).split("\n").toTypedArray())
                                .parallel()
                                .filter { si: String -> si.startsWith("https://") }
                                .collect(Collectors.toList())

                        cache[lib] = collect
                        log.info("Added {} tracks from playlist {}", collect.size, lib)
                    }
                }
            } catch (e: IOException) {
                log.error("Failed to load playlist {}", lib, e)
            }
        }
    }
}