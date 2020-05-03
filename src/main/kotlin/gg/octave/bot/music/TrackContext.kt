package gg.octave.bot.music

open class TrackContext(val requester: Long, val requestedChannel: Long) {
    val requesterMention = if (requester != -1L) "<@$requester>" else ""
    val channelMention = if (requestedChannel != -1L) "<#$requestedChannel>" else ""
}
