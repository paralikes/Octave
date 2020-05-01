package xyz.gnarbot.gnar.utils.commands

import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * This class defines a rate-limit that will be taken into account
 * when a key of type [K] is checked against this rate-limiter.
 *
 * @param <K> Key type.
</K> */
class RateLimiter<K>(private val threshold: Int, private val timeout: Long) {
    /**
     * Constructs a rate-limiter where the amount of checks
     * before triggering rate-limited status is 1, along with
     * pre-defined timeout amount and unit.
     *
     * @param amount Amount of the duration, in terms of the unit.
     * @param unit   Unit that the duration is measured in.
     */
    constructor(amount: Long, unit: TimeUnit) : this(1, amount, unit) {}

    /**
     * Constructs a rate-limiter with defined checks threshold
     * along with pre-defined timeout duration instance.
     *
     * @param threshold Checks allowed before rate-limit status.
     * @param duration  Duration instance, converted to milliseconds.
     */
    constructor(threshold: Int, duration: Duration) : this(threshold, duration.toMillis()) {}

    /**
     * Constructs a rate-limiter with defined checks threshold
     * along with pre-defined timeout amount and unit.
     *
     * @param threshold Checks allowed before rate-limit status.
     * @param amount    Amount of the duration, in terms of the unit.
     * @param unit      Unit that the duration is measured in.
     */
    constructor(threshold: Int, amount: Long, unit: TimeUnit) : this(threshold, unit.toMillis(amount)) {}

    /**
     * Executor service of which the rate-limiter uses
     * to remove the time-out for each key.
     */
    private val executor = Executors.newSingleThreadScheduledExecutor()

    /**
     * Backing map of which the rate-limiter keeps track
     * of the keys.
     */
    private val map: MutableMap<K, Pair<AtomicInteger, AtomicLong>> = ConcurrentHashMap()

    /**
     * Checks if the key is rate-limited.
     *
     *
     * The number of checks allowed before rate-limited status is defined
     * by the [.threshold]. How long the key will be timed out for
     * is defined by the [.timeout].
     *
     * @param key The key to check if it is rate-limited.
     * @return `true` if the key is not rate-limited.
     * `false` if the key is rate-limited.
     */
    fun check(key: K): Boolean {
        // Pair ( count to check against threshold, time until rate-limit entry is cleared )
        var entry = map[key]
        if (entry == null) {
            entry = Pair(AtomicInteger(), AtomicLong())
            map[key] = entry
        }

        val count = entry.first
        if (count.get() >= threshold)
            return false

        count.incrementAndGet()
        entry.second.set(System.currentTimeMillis() + timeout)
        executor.schedule({
            if (count.decrementAndGet() <= 0) {
                map.remove(key)
            }
        }, timeout, TimeUnit.MILLISECONDS)

        return true
    }

    /**
     * Checks the remaining time until rate-limit for the key is cleared.
     *
     * @param key The key to check for remaining time.
     * @return The remaining time of which the key's rate-limit entry is cleared.
     */
    fun remainingTime(key: K): Long {
        return when (val entry = map[key]) {
            null -> 0
            else -> entry.second.get() - System.currentTimeMillis()
        }
    }
}