package gg.octave.bot.music.sources.spotify.loaders

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import gg.octave.bot.music.sources.spotify.SpotifyAudioSourceManager
import java.util.regex.Matcher
import java.util.regex.Pattern

interface Loader {

    /**
     * Returns the pattern used to match URLs for this loader.
     */
    fun pattern(): Pattern

    /**
     * Loads an AudioItem from the given regex match.
     */
    fun load(manager: DefaultAudioPlayerManager, sourceManager: SpotifyAudioSourceManager, matcher: Matcher): AudioItem?

}
