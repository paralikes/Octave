package gg.octave.bot.music.settings

enum class BoostSetting(val band1: Float, val band2: Float) {
    OFF(0.0F, 0.0F),
    SOFT(0.25F, 0.15F),
    HARD(0.50F, 0.25F),
    EXTREME(0.75F, 0.50F),
    EARRAPE(1F, 0.75F)
}
