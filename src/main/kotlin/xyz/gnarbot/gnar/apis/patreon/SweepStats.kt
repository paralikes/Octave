package xyz.gnarbot.gnar.apis.patreon

data class SweepStats(
    val total: Int,
    val changed: Int,
    val removed: Int
) {

    override fun toString(): String {
        return "SweepStats[total=$total, changed=$changed, removed=$removed]"
    }

}
