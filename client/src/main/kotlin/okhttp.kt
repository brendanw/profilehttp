import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.CountDownLatch
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    statusCode()
    System.gc()
    println("===================")

    statusCode()
    System.gc()
    println("===================")

    mediumResponse(10)
    System.gc()
    println("===================")

    mediumResponse(100)
    System.gc()
    println("===================")

    mediumResponse(1000)
    System.gc()
    println("===================")

    mediumResponse(10_000)
    System.gc()
    println("===================")
}

private val httpClient: OkHttpClient by lazy { OkHttpClient() }

private fun mediumResponse(numRequests: Int) {
    println("medium-length response (n=$numRequests) START")
    val countDownLatch = CountDownLatch(numRequests)
    val time = measureTimeMillis {
        for (i in 0 until numRequests) {
            Thread {
                val request = Request.Builder()
                    .url("$BASE_URL/mediumlocations")
                    .build()
                val response = httpClient.newCall(request).execute()
                response.use { response ->
                    response.body.use {
                        it?.string()
                        countDownLatch.countDown()
                    }
                }
            }.start()
        }
        countDownLatch.await()
    }
    println("medium-length response (n=$numRequests): $time ms")
}

private fun statusCode(numRequests: Int = 10) {
    println("GET status code (n=$numRequests): START")
    val countDownLatch = CountDownLatch(10)
    val time = measureTimeMillis {
        for (i in 0..9) {
            Thread {
                val request = Request.Builder()
                    .url("$BASE_URL/locations")
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    response.code
                }
                countDownLatch.countDown()
            }.start()
        }
        countDownLatch.await()
    }
    println("GET status code (n=$numRequests): $time ms")
}

private fun simpleGetTest() {
    var time = measureTimeMillis {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("$BASE_URL/locations")
            .build()
        val response = client.newCall(request).execute()
        println(response.code)
    }
    println("okhttp simple get: $time ms")
}
