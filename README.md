**Initial Results**


| Tests | okhttp | ktor numThreads=8 | ktor numThreads=1 |
| :---         |     :---:      |          :---: |          :---: |
| client init   |       |      |   |
| init client + 10 status code requests     | 676 ms       |  285ms      | 243ms  |
| pre-initialized client + 10 status code requests | 8ms | 8ms  |  19ms |
| read 1 medium-sized responses from same endpoint |   |   |   |
| read 10 medium-sized responses from same endpoint | 14ms | 50ms  | 3057ms  |
| read 100 medium-sized responses from same endpoint | 78ms | 153ms  |  2186ms  |
| read 1,000 medium-sized responses from same endpoint | 284ms | 553ms  | 1587ms  |
| read 10,000 medium-sized responses from same endpoint | 1967ms | crash  | 8815ms  |
| read 100,000 medium-sized responses from same endpoint |   |   |   |

**Run the tests**

You'll need gradle to build both the client and server projects:

on mac: `brew install gradle` (see https://brew.sh/ to install homebrew)

on windows: `choco install gradle` (see https://chocolatey.org/ to install chocolatey)

Before running the client tests, be sure to start up the server

`cd server && ./gradlew run`

You can run the ktor cio client performance test from within the client directory with

`cd client && ./gradlew -PmainClass=KtorKt run`

Run the okhttp client performance test with

`cd client && ./gradlew -PmainClass=OkhttpKt run`

Alternatively you can just run the shell script

`sh runtest.sh`

For opening the project and running tests within intellij, be sure that there are only two modules `client` and `server`. If you need to add either module, you can do so via "open module settings" => "+" => "new module" and then enter the location of the client or server folder.

**Background**

The goal is to reduce thread usage in the typical android app. A typical android app has 50-100 threads allocated at a given time. If yes, can we see performance improvements? 

Asked on the Kotlin Lang slack (https://kotlinlang.slack.com/archives/C0B8M7BUY/p1575401896314500):

> Dispatchers.IO defaults to being backed by an executor that can spawn up to 64 threads. Is it possible to architect an app with suspending non-blocking IO calls and we'd just use one thread for the IO dispatcher?
>
> Is it a reasonable expectation that migrating to coroutines+flow from rx will reduce the total number of threads an android application uses? This is the high-level question I am trying to answer.

Stumbled upon this comment by Roman Elizarov

> If you are using a library that provides asynchronous IO via NIO (like ktor.io for example), then you would not need withContext at all, since all your IO functions will be non-blocking. You need to use Dispatchers.IO only if you have blocking IO functions that you must to use without blocking.

Jesse Wilson suggested performance testing the two clients.

To see where okhttp does blocking network io for http1.1 requests visit
https://github.com/square/okhttp/blob/master/okhttp/src/main/java/okhttp3/internal/http1/Http1ExchangeCodec.kt

To see where ktor-client manages nio for http1.1 requests visit
https://github.com/ktorio/ktor/blob/master/ktor-network/jvm/src/io/ktor/network/selector/ActorSelectorManager.kt

A primer on nio
https://www.hellsoft.se/non-blocking-io/

**Hypothesis 1**

Using the ktor client configured to a single thread should have similar performance to using okhttp launching a thread for each request.

**Hypothesis 2**

The ktor client performance should improve as we increase the number of threads it is configured to use.

**Hypothesis 3**

The number of running threads -- ceteris paribus -- should be lower in an application using the ktor client.

**Flaws**

-The mechanism for determining when 10 concurrent requests are complete is different in each experiment. The performance of CountDownLatch vs List<Deferred>:awaitAll() could have a significant impact. We should try to find a way to use the same mechanism for each test set.

-Running into an error at 100_000 concurrent requests for ktor. Need to dig into to see if we are using the library wrong or its a bug.

-Measurements are not guaranteed to include garbage collection

-Should test for when each request is for a different address

-Should compare 1 request vs 1 request

-Should stagger when execution starts for each request to mimic analytics calls
