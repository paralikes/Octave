package gg.octave.bot.utils

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class LazyThreadPool : ThreadPoolExecutor(
    0,
    3,
    1, TimeUnit.MINUTES,
    LinkedBlockingQueue(10),
    NamedThreadFactory("Eval"),
    AbortPolicy()
)
