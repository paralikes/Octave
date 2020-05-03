package gg.octave.bot.music.sources.spotify

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools
import com.sedmelluq.discord.lavaplayer.track.*
import org.apache.http.HttpStatus
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils
import org.json.JSONObject
import org.slf4j.LoggerFactory
import gg.octave.bot.music.sources.spotify.loaders.SpotifyAlbumLoader
import gg.octave.bot.music.sources.spotify.loaders.SpotifyPlaylistLoader
import gg.octave.bot.music.sources.spotify.loaders.SpotifyTrackLoader
import java.io.DataInput
import java.io.DataOutput
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SpotifyAudioSourceManager(
    private val clientId: String?,
    private val clientSecret: String?,
    private val youtubeAudioSourceManager: YoutubeAudioSourceManager
) : AudioSourceManager {
    private val sched = Executors.newSingleThreadScheduledExecutor()
    private val trackLoaderPool = Executors.newFixedThreadPool(10)

    private val httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager()!!
    internal var accessToken: String = ""
        private set

    val enabled: Boolean
        get() = "" != clientId && "" != clientSecret

    init {
        if (enabled) {
            refreshAccessToken()
        }
    }


    /**
     * Source manager shizzle
     */
    override fun getSourceName() = "spotify"

    override fun isTrackEncodable(track: AudioTrack) = false

    override fun decodeTrack(trackInfo: AudioTrackInfo, input: DataInput): AudioTrack {
        throw UnsupportedOperationException("${this::class.java.simpleName} does not support track decoding.")
    }

    override fun encodeTrack(track: AudioTrack, output: DataOutput) {
        throw UnsupportedOperationException("${this::class.java.simpleName} does not support track encoding.")
    }

    override fun shutdown() {
        httpInterfaceManager.close()
    }

    override fun loadItem(manager: DefaultAudioPlayerManager, reference: AudioReference): AudioItem? {
        if (accessToken.isEmpty()) {
            return null
        }

        return try {
            loadItemOnce(manager, reference.identifier)
        } catch (exception: FriendlyException) {
            // In case of a connection reset exception, try once more.
            if (HttpClientTools.isRetriableNetworkException(exception.cause)) {
                loadItemOnce(manager, reference.identifier)
            } else {
                throw exception
            }
        }
    }

    private fun loadItemOnce(manager: DefaultAudioPlayerManager, identifier: String): AudioItem? {
        for (loader in loaders) {
            val matcher = loader.pattern().matcher(identifier)

            if (matcher.find()) {
                return loader.load(manager, this, matcher)
            }
        }

        return null
    }

    internal fun doYoutubeSearch(manager: DefaultAudioPlayerManager, identifier: String): AudioItem? {
        val reference = AudioReference(identifier, null)
        return youtubeAudioSourceManager.loadItem(manager, reference)
    }

    internal fun queueYoutubeSearch(manager: DefaultAudioPlayerManager, identifier: String): CompletableFuture<AudioItem?> {
        val future = CompletableFuture<AudioItem?>()

        trackLoaderPool.submit {
            val reference = AudioReference(identifier, null)

            try {
                val result = youtubeAudioSourceManager.loadItem(manager, reference)
                future.complete(result)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }

        return future
    }


    /**
     * Spotify shizzle
     */
    private fun refreshAccessToken() {
        if (!enabled) {
            return
        }

        val base64Auth = Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())

        request(HttpPost.METHOD_NAME, "https://accounts.spotify.com/api/token") {
            addHeader("Authorization", "Basic $base64Auth")
            addHeader("Content-Type", "application/x-www-form-urlencoded")
            entity = StringEntity("grant_type=client_credentials")
        }.use {
            if (it.statusLine.statusCode != HttpStatus.SC_OK) {
                log.warn("Received code ${it.statusLine.statusCode} from Spotify while trying to update access token!")
                sched.schedule(::refreshAccessToken, 1, TimeUnit.MINUTES)
                return
            }

            val content = EntityUtils.toString(it.entity)
            val json = JSONObject(content)

            if (json.has("error") && json.getString("error").startsWith("invalid_")) {
                log.error("Spotify API access disabled (${json.getString("error")})")
                accessToken = ""
                return
            }

            val refreshIn = json.getInt("expires_in")
            accessToken = json.getString("access_token")
            sched.schedule(::refreshAccessToken, ((refreshIn * 1000) - 10000).toLong(), TimeUnit.MILLISECONDS)

            val snippet = accessToken.substring(0..4).padEnd(accessToken.length - 5, '*') // lol imagine printing the entire token
            log.info("Updated access token to $snippet")
        }
    }


    /**
     * Utils boiiii
     */
    internal fun request(url: String, requestBuilder: RequestBuilder.() -> Unit): CloseableHttpResponse {
        return request(HttpGet.METHOD_NAME, url, requestBuilder)
    }

    internal fun request(method: String, url: String, requestBuilder: RequestBuilder.() -> Unit): CloseableHttpResponse {
        return httpInterfaceManager.`interface`.use {
            it.execute(RequestBuilder.create(method).setUri(url).apply(requestBuilder).build())
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SpotifyAudioSourceManager::class.java)

        private val loaders = listOf(
            SpotifyAlbumLoader(),
            SpotifyPlaylistLoader(),
            SpotifyTrackLoader()
        )
    }

}
