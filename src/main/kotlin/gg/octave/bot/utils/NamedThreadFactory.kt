package gg.octave.bot.utils

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class NamedThreadFactory(private val name: String) : ThreadFactory {
    private val threadCount = AtomicInteger()

    override fun newThread(r: Runnable): Thread {
        val threadId = "$name-Thread-${threadCount.getAndIncrement()}"
        return Thread(r, threadId)
    }
}
