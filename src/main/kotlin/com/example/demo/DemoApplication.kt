package com.example.demo

import io.lettuce.core.ClientOptions
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration

@Configuration
class LettuceCustomizer : LettuceClientConfigurationBuilderCustomizer {

	@Override
	override fun customize(clientConfigurationBuilder: LettuceClientConfiguration.LettuceClientConfigurationBuilder) {
		clientConfigurationBuilder
			.clientOptions(ClientOptions.builder().publishOnScheduler(true).build())
	}
}



@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}
