package gg.octave.bot.utils.extensions

import okhttp3.HttpUrl
import okhttp3.Request

fun Request.Builder.buildUrl(urlOpts: HttpUrl.Builder.() -> Unit): HttpUrl {
    return HttpUrl.Builder().apply(urlOpts).build()
}

fun Request.Builder.url(urlOpts: HttpUrl.Builder.() -> Unit) {
    url(buildUrl(urlOpts))
}
