package gg.octave.bot.utils

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object Utils {
    private val TIME_PATTERN = Pattern.compile("(-?\\d+)\\s*((?:d(?:ays?)?)|(?:h(?:ours?)?)|(?:m(?:in(?:utes?)?)?)|(?:s(?:ec(?:onds?)?)?))?")

    fun parseTime(time: String): Long {
        val s = time.toLowerCase()
        val matcher = TIME_PATTERN.matcher(s)
        var ms = 0L

        while (matcher.find()) {
            val numStr = matcher.group(1)
            val unit = when (matcher.group(2)) {
                "d", "day", "days" -> TimeUnit.DAYS
                "h", "hour", "hours" -> TimeUnit.HOURS
                "m", "min", "minute", "minutes" -> TimeUnit.MINUTES
                "s", "sec", "second", "seconds" -> TimeUnit.SECONDS
                else -> TimeUnit.SECONDS
            }
            ms += unit.toMillis(numStr.toLong())
        }

        return ms
    }

    fun getTimestamp(ms: Long): String {
        val s = ms / 1000
        val m = s / 60
        val h = m / 60

        return when {
            h > 0 -> String.format("%02d:%02d:%02d", h, m % 60, s % 60)
            else -> String.format("%02d:%02d", m, s % 60)
        }
    }

    fun hasteBin(content: String): String? {
        return RequestUtil.jsonObject {
            url("https://hastebin.com/documents")
            header("User-Agent", "Octave")
            post(RequestBody.create(MediaType.get("text/plain"), content))
        }.thenApply { "https://hastebin.com/${it["key"]}" }.get()
    }
}
