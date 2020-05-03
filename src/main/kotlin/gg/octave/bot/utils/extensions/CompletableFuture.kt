package gg.octave.bot.utils.extensions

import java.util.concurrent.CompletableFuture

/**
 * Runs the given check against the result. If the value is false,
 * an IllegalArgumentException will be thrown with the given message,
 * that can then be caught by exceptionally.
 */
fun <T> CompletableFuture<T>.assert(check: (T) -> Boolean, lazyMessage: (T) -> String): CompletableFuture<T> {
    thenAccept {
        if (!check(it)) {
            throw IllegalStateException(lazyMessage(it))
        }
    }
    return this
}
