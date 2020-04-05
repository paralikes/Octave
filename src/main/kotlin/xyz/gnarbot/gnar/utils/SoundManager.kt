package xyz.gnarbot.gnar.utils

import org.slf4j.LoggerFactory
import java.io.File

class SoundManager {
    var map: HashMap<String, String> = HashMap()

    fun loadSounds() {
        val soundFiles = File("/home/gnar/data/sounds")

        if (!soundFiles.exists()) {
            log.info("Not loading sound files; directory does not exist.")
        }

        soundFiles.listFiles()?.forEach {
            log.info("Loading sound {}", it.name)
            map[it.name.replace(".mp3", "").replace("sounds\\", "")] = it.path
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SoundManager::class.java)
    }
}
