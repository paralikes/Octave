package gg.octave.bot.apis.patreon

data class ResultPage(
    val pledges: List<PatreonUser>,
    val hasMore: Boolean,
    val offset: String?
)
