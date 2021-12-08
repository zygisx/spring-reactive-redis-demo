package com.example.demo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.ReactiveRedisTemplate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.toJavaDuration

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@OptIn(ExperimentalTime::class)
class DemoApplicationTests {

	@Autowired
	lateinit var redisTemplate: ReactiveRedisTemplate<String, String>

	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(DemoApplicationTests::class.java)
		const val NO_OF_WRITES = 1000
	}

	@BeforeAll
	fun warmUp() {
		runBlocking {
			(0..10).forEach { write() }
		}
	}


	@Test
	fun `sync redis template calls`() {
		runBlocking {
			val time = measureTime {
				(0..NO_OF_WRITES).forEach { write() }
			}

			LOGGER.info("$NO_OF_WRITES writes took ${time.inWholeMilliseconds} ms.")
		}
	}

	@Test
	fun `async in single thread pool redis template calls`() {
		runBlocking {
			val time = measureTime {
				withContext(newSingleThreadContext("single")) {
					val writesDef = (0..NO_OF_WRITES).map { async { write() } }


					writesDef.awaitAll()
				}
			}
			LOGGER.info("$NO_OF_WRITES writes took ${time.inWholeMilliseconds} ms.")
		}
	}

	@Test
	fun `async in defaults pool redis template calls`() {
		runBlocking {
			val time = measureTime {
				withContext(Dispatchers.Default) {
					val writesDef = (0..NO_OF_WRITES).map { async { write() } }
					writesDef.awaitAll()
				}
			}
			LOGGER.info("$NO_OF_WRITES writes took ${time.inWholeMilliseconds} ms.")
		}
	}

	suspend fun write() {
		redisTemplate.opsForValue()
			.set("key", "value")
			.doOnError { error -> LOGGER.error("Failed to get item from cache", error) }
			.onErrorReturn(false)
			.awaitFirst()
	}
}
