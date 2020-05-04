package gg.octave.bot.music.filters

import com.github.natanbc.lavadsp.karaoke.KaraokePcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

class KaraokeFilter : FilterConfig<KaraokePcmAudioFilter> {
    private var config: KaraokePcmAudioFilter.() -> Unit = {}

    override fun configure(transformer: KaraokePcmAudioFilter.() -> Unit): KaraokeFilter {
        config = transformer
        return this
    }

    override fun build(downstream: FloatPcmAudioFilter, format: AudioDataFormat): FloatPcmAudioFilter {
        return KaraokePcmAudioFilter(downstream, format.channelCount, format.sampleRate)
            .also(config)
    }
}
