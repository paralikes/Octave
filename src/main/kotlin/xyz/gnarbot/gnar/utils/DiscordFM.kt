package xyz.gnarbot.gnar.utils

import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import java.io.IOException
import kotlin.collections.HashMap

class DiscordFM {
    fun getRandomSong(library: String): String? {
        return cache[library]?.random()?.trim { it <= ' ' }
                //?: "https://www.youtube.com/watch?v=D7npse9n-Yw"
    }

    companion object {
        private val log = LoggerFactory.getLogger(DiscordFM::class.java)
        val LIBRARIES = arrayOf(
                "electro hub", "chill corner", "korean madness",
                "japanese lounge", "classical", "retro renegade",
                "metal mix", "hip hop", "electro swing", "christmas", "halloween",
                "purely pop", "rock n roll", "coffee house jazz", "funk")
        private val cache = HashMap<String, List<String>>(LIBRARIES.size)
    }

    init {
        for (lib in LIBRARIES) {
            DiscordFM::class.java.getResourceAsStream("/dfm/$lib.txt").use {
                if (it == null) {
                    return@use log.warn("Playlist {} does not exist, skipping...", lib)
                }

                try {
                    val collect = IOUtils.toString(it, Charsets.UTF_8)
                        .split('\n')
                        .filter { s -> s.startsWith("https://") }

                    cache[lib] = collect
                    log.info("Added {} tracks from playlist {}", collect.size, lib)
                } catch (e: IOException) {
                    log.error("Failed to load playlist {}", lib, e)
                }
            }
        }
    }
}
