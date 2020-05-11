package gg.octave.bot.utils.extensions

import org.redisson.api.RQueue

fun RQueue<String>.removeAt(index: Int): String {
    var iterIndex = 0
    val iterator = this.iterator()
    var value = ""
    while (iterator.hasNext() && iterIndex <= index) {
        val currentValue = iterator.next()
        if (iterIndex == index) {
            value = currentValue
            iterator.remove()
        }

        iterIndex++
    }

    return value
}

fun <T> RQueue<T>.insertAt(index: Int, element: T) {
    val elements = this.readAll()
    elements.add(index, element)
    this.clear()
    this.addAll(elements)
}

/**
 * @return The element that was moved.
 */
fun <T> RQueue<T>.move(index: Int, to: Int): T {
    val elements = this.readAll()
    val temp = elements.removeAt(index)
    elements.add(to, temp)
    this.clear()
    this.addAll(elements)

    return temp
}
