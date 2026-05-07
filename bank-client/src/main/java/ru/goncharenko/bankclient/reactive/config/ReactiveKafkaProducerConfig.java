package ru.goncharenko.bankclient.reactive.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.SenderOptions;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "application.service.notification.communication-method", havingValue = "kafka")
public class ReactiveKafkaProducerConfig {
	@Value("${bootstrap.servers}")
	private String bootstrapServers;

	@Bean
	public <T extends Serializable> ReactiveKafkaProducerTemplate<String, T> reactiveKafkaProducerTemplate() {
		log.info("Creating ReactiveKafkaProducerTemplate bean");
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		props.put("allow.auto.create.topics", "true");

		SenderOptions<String, T> senderOptions = SenderOptions.create(props);

		return new ReactiveKafkaProducerTemplate<>(senderOptions);
	}
}
