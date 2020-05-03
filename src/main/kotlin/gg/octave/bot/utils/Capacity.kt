package gg.octave.bot.utils

class Capacity(
    val amount: Double,
    val unit: String
) {
    companion object {
        private val units = listOf("B", "kB", "MB", "GB")
        fun calculate(n: Long): Capacity {
            var amount = n.toDouble()
            var unitIndex = 0

            while (amount > 1000 && unitIndex < units.size - 1) {
                amount /= 1000
                unitIndex++
            }

            return Capacity(amount, units[unitIndex])
        }
    }
}