package gg.octave.bot.music.filters

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

class EqualizerFilter : FilterConfig<Equalizer> {
    private var config: Equalizer.() -> Unit = {}

    override fun configure(transformer: Equalizer.() -> Unit): EqualizerFilter {
        config = transformer
        return this
    }

    override fun build(downstream: FloatPcmAudioFilter, format: AudioDataFormat): FloatPcmAudioFilter {
        return Equalizer(format.channelCount, downstream, zero)
            .also(config)
    }

    companion object {
        private val zero = (0..14).map { 0.0f }.toFloatArray()
    }
}
