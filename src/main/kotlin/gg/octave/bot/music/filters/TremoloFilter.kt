package gg.octave.bot.music.filters

import com.github.natanbc.lavadsp.tremolo.TremoloPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

class TremoloFilter : FilterConfig<TremoloPcmAudioFilter> {
    private var config: TremoloPcmAudioFilter.() -> Unit = {}

    override fun configure(transformer: TremoloPcmAudioFilter.() -> Unit): TremoloFilter {
        config = transformer
        return this
    }

    override fun build(downstream: FloatPcmAudioFilter, format: AudioDataFormat): FloatPcmAudioFilter {
        return TremoloPcmAudioFilter(downstream, format.channelCount, format.sampleRate)
            .also(config)
    }
}
