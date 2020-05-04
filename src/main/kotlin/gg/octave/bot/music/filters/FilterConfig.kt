package gg.octave.bot.music.filters

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

interface FilterConfig<T> {
    fun configure(transformer: T.() -> Unit): FilterConfig<T>
    fun build(downstream: FloatPcmAudioFilter, format: AudioDataFormat): FloatPcmAudioFilter
}
