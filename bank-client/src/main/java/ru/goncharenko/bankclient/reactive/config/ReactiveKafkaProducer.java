package ru.goncharenko.bankclient.reactive.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "application.service.notification.communication-method", havingValue = "kafka")
@RequiredArgsConstructor
public class ReactiveKafkaProducer<K, V> {
	private final KafkaTemplate<K, V> kafkaTemplate;

	public Mono<SendResult<K, V>> send(String topic, V value) {
		return Mono.fromFuture(kafkaTemplate.send(topic, value));
	}

	public Mono<SendResult<K, V>> send(String topic, K key, V value) {
		return Mono.fromFuture(kafkaTemplate.send(topic, key, value));
	}

	public Mono<SendResult<K, V>> send(ProducerRecord<K, V> record) {
		return Mono.fromFuture(kafkaTemplate.send(record));
	}

/*	@Bean
	public <T extends Serializable> ReactiveKafkaProducerTemplate<String, T> reactiveKafkaProducerTemplate() {
		log.info("Creating ReactiveKafkaProducerTemplate bean");
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		props.put("allow.auto.create.topics", "true");

		SenderOptions<String, T> senderOptions = SenderOptions.create(props);

		return new ReactiveKafkaProducerTemplate<>(senderOptions);
	}*/
}
