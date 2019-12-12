**Initial Results**

| Tests | okhttp | ktor numThreads=8 | ktor numThreads=1 |
| :---         |     :---:      |          :---: |          :---: |
| client init   | 0ms     | 0ms    | 0ms |
| init client + 10 status code requests     | 0ms       | 0ms      | 0ms |
| pre-initialized client + 10 status code requests | 0ms | 0ms | 0ms |
| read 1 medium-sized responses from same endpoint | 0ms | 0ms | 0ms |
| read 10 medium-sized responses from same endpoint | 0ms | 0ms | 0ms |
| read 100 medium-sized responses from same endpoint | 0ms | 0ms |  0ms |
| read 1,000 medium-sized responses from same endpoint | 0ms | 0ms | 0ms |
| read 10,000 medium-sized responses from same endpoint | 0ms | 0ms | 0ms |
| read 100,000 medium-sized responses from same endpoint | 0ms | 0ms | 0ms |

**Hypothesis 1**

Using the ktor client configured to a single thread should have similar performance to using okhttp launching a thread for each request.

**Hypothesis 2**

The ktor client performance should improve as we increase the number of threads it is configured to use.

**Hypothesis 3**

The number of running threads -- ceteris paribus -- should be lower in an application using the ktor client.

**Flaws**

-The mechanism for determining when 10 concurrent requests are complete is different in each experiment. The performance of CountDownLatch vs List<Deferred>:awaitAll() could have a significant impact. We should try to find a way to use the same mechanism for each test set.

-Running into an error at 100_000 concurrent requests for ktor

-Measurements are not guaranteed to include garbage collection

-Should test for when each request is for a different address

-Should compare 1 request vs 1 request

-Should stagger when execution starts for each request to mimic analytics calls
