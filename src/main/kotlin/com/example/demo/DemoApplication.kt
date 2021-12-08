package com.example.demo

import io.lettuce.core.ClientOptions
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import java.time.Duration

@Configuration
class LettuceCustomizer : LettuceClientConfigurationBuilderCustomizer {

	@Override
	override fun customize(clientConfigurationBuilder: LettuceClientConfiguration.LettuceClientConfigurationBuilder) {
		clientConfigurationBuilder
			.clientOptions(ClientOptions.builder().publishOnScheduler(true).build())
	}
}

@Configuration
class RedisConnectionConfiguration {

	@Bean
	fun lettuceClientConfiguration(
		builderCustomizers: ObjectProvider<LettuceClientConfigurationBuilderCustomizer>,
	): LettuceClientConfiguration {
		val builder = LettuceClientConfiguration.builder().commandTimeout(Duration.ofSeconds(15))
		builderCustomizers.orderedStream().forEach {
				customizer: LettuceClientConfigurationBuilderCustomizer -> customizer.customize(builder) }
		return builder.build()
	}

	@Bean
	fun cacheRedisConnectionFactory(
		clientConfig: LettuceClientConfiguration,
	): LettuceConnectionFactory {
		val factory = LettuceConnectionFactory(getStandAloneConfiguration(), clientConfig)
		factory.eagerInitialization = true
		return factory
	}

	private fun getStandAloneConfiguration(): RedisStandaloneConfiguration {
		val config = RedisStandaloneConfiguration()
		config.hostName = "localhost"
		config.port = 6379
		return config
	}
}



@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}
