package gg.octave.bot.utils

import io.sentry.Sentry
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object Utils {
    val httpClient = OkHttpClient()
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
        val request = Request.Builder().url("https://hastebin.com/documents")
            .header("User-Agent", "Octave")
            .header("Content-Type", "text/plain")
            .post(RequestBody.create(null, content))
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body() ?: return null
                val json = body.byteStream().use { JSONObject(JSONTokener(it)) }
                return "https://hastebin.com/${json["key"]}"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Sentry.capture(e)
            return null
        }
    }
}
