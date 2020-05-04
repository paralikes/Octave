package gg.octave.bot.utils

import org.apache.commons.io.IOUtils

object OctaveBot {
    private val stream = OctaveBot::class.java.classLoader.getResourceAsStream("version.txt")
    private val versionTxt = IOUtils.toString(stream, Charsets.UTF_8).split('\n')

    val VERSION_STRING = versionTxt[0]
    val GIT_REVISION = versionTxt[1]
}
