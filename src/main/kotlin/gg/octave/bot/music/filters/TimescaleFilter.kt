package gg.octave.bot.music.filters

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

class TimescaleFilter : FilterConfig<TimescalePcmAudioFilter> {
    private var config: TimescalePcmAudioFilter.() -> Unit = {}

    override fun configure(transformer: TimescalePcmAudioFilter.() -> Unit): TimescaleFilter {
        config = transformer
        return this
    }

    override fun build(downstream: FloatPcmAudioFilter, format: AudioDataFormat): FloatPcmAudioFilter {
        return TimescalePcmAudioFilter(downstream, format.channelCount, format.sampleRate)
            .also(config)
    }
}
