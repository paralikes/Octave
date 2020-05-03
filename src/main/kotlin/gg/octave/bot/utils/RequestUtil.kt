package gg.octave.bot.utils

import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CompletableFuture

object RequestUtil {
    private val httpClient = OkHttpClient()

    /* Media Types */
    val APPLICATION_JSON = MediaType.get("application/json")

    fun request(options: Request.Builder.() -> Unit): PendingRequest {
        val request = Request.Builder()
            .apply(options)
            .build()

        return PendingRequest(request)
    }

    fun jsonObject(options: Request.Builder.() -> Unit) = jsonObject(options, false)

    fun jsonObject(options: Request.Builder.() -> Unit, checkStatus: Boolean): CompletableFuture<JSONObject> {
        return request(options).submit()
            .thenApply {
                if (checkStatus && !it.isSuccessful) {
                    val extra = if (it.header("content-type") == "application/json") {
                        it.body()?.string() ?: "{}"
                    } else {
                        "{}"
                    }
                    throw IllegalStateException("Received invalid status code from Patreon API: " +
                        "${it.code()} - $extra")
                }
                it
            }
            .thenApply { it.body()?.string() ?: throw IllegalStateException("ResponseBody was null!") }
            .thenApply { JSONObject(it) }
    }

    class PendingRequest(private val req: Request) {
        fun submit(): CompletableFuture<Response> {
            val future = CompletableFuture<Response>()

            httpClient.newCall(req).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    future.completeExceptionally(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    future.complete(response)
                }
            })

            return future
        }

//        suspend fun await(): RequestBody {
//            return submit().await()
//        }
        // Requires: kotlinx.coroutines
    }
}
