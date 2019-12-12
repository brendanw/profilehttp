**Hypothesis 1**

Using the Ktor http client configured to a single thread should have similar performance to using okhttp launching a thread for each request.

**Hypothesis 2**

The Ktor http client performance should improve as we increase the number of threads it is configured to use.

**Hypothesis 3**

The number of running threads -- ceteris paribus -- should be lower in an application using the ktor client.

**Methodology flaws**

-The mechanism for determining when 10 concurrent requests are complete is different in each experiment. The performance of CountDownLatch vs List<Deferred>:awaitAll() could have a significant impact. We should try to find a way to use the same mechanism for each test set.

-Running into an error at 100_000 


