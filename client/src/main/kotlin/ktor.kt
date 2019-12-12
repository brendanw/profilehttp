import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

const val BASE_URL = "http://localhost:5003"

fun main(args: Array<String>) {
    runBlocking {
        statusCode(10)
        System.gc()
        println("===================")

        statusCode(10)
        System.gc()
        println("===================")

        mediumResponse(10)
        System.gc()
        println("===================")

        mediumResponse(100, 8)
        System.gc()
        println("===================")

        mediumResponse(1000, 8)
        System.gc()
        println("===================")

        mediumResponse(10_000, 8)
        System.gc()
        println("===================")
    }
}

private suspend fun mediumResponse(numRequests: Int, numThreads: Int = 3, reuseClient: Boolean = true) {
    println("medium-length response (n=$numRequests) START")
    val testClient = if (reuseClient) getClient(numThreads = numThreads) else reusableClient
    delay(1000) // Allow client to warmup
    coroutineScope {
        val time = measureTimeMillis {
            // Allocating and populating jobList might be more expensive than CountDownLatch#countDown
            // Is idiomatic https://github.com/Kotlin/kotlinx.coroutines/issues/58
            val jobList = MutableList(numRequests) {
                async {
                    testClient.get<String>("$BASE_URL/mediumlocations")
                    200
                }
            }
            jobList.awaitAll()
        }
        println("medium-length response (n=$numRequests): $time ms")
    }
}

val reusableClient: HttpClient = getClient(8)

@UseExperimental(KtorExperimentalAPI::class)
private fun getClient(numThreads: Int = 3): HttpClient {
    return HttpClient(CIO) {
        engine {
            /**
             * Maximum number of socket connections.
             */
            maxConnectionsCount = 1000

            threadsCount = numThreads

            endpoint {
                /**
                 * Maximum number of requests for a specific endpoint route.
                 */
                maxConnectionsPerRoute = 100

                /**
                 * Max size of scheduled requests per connection(pipeline queue size).
                 */
                pipelineMaxSize = 20

                /**
                 * Max number of milliseconds to keep iddle connection alive.
                 */
                keepAliveTime = 20000

                /**
                 * Number of milliseconds to wait trying to connect to the server.
                 */
                connectTimeout = 5000

                /**
                 * Maximum number of attempts for retrying a connection.
                 */
                connectRetryAttempts = 5
            }
        }
    }
}

private suspend fun statusCode(numRequests: Int, numThreads: Int = 3) {
    println("GET status code (n=$numRequests): START")
    val client = getClient(numThreads)
    delay(1000) // Allow client to warmup
    coroutineScope {
        val time = measureTimeMillis {
            val jobList = MutableList(numRequests) {
                async {
                    var status = 0
                    client.use { client ->
                        status = client.get<HttpStatusCode>("$BASE_URL/locations").value
                    }
                    status
                }
            }
            jobList.awaitAll()
        }
        println("GET status code (n=$numRequests): $time ms")
    }
}

private suspend fun simpleGetTest() {
    val time = measureTimeMillis {
        val status = HttpClient().use { client ->
            client.get<HttpStatusCode>("$BASE_URL/locations")
        }
        println(status)
    }
    println("ktor simple get: $time ms")
}
