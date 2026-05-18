package ru.goncharenko.cash;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import ru.goncharenko.bankclient.common.model.NotificationDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.goncharenko.cash.NotificationKafkaProcessorTest.TEST_TOPIC_NAME;

@SpringBootTest
@EmbeddedKafka(topics = {TEST_TOPIC_NAME})
public class NotificationKafkaProcessorTest {
	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private EmbeddedKafkaBroker embeddedKafkaBroker;

	final static String TEST_TOPIC_NAME = "notification";
	private final String service = "cash-service";
	private final String message = "Пополнение / снятие средств со счёта %s успешно выполнено";

	@Test
	public void testNotificationProcessor() {
		try (var consumer = new DefaultKafkaConsumerFactory<String, NotificationDto>(
				KafkaTestUtils.consumerProps("notification-consumer", "true", embeddedKafkaBroker),
				new StringDeserializer(),
				new JsonDeserializer<>(NotificationDto.class)
		).createConsumer()) {
			consumer.subscribe(List.of(TEST_TOPIC_NAME));
			consumer.poll(Duration.ofMillis(100));

			NotificationDto notification = NotificationDto.builder()
					.service(service)
					.message(message)
					.created(LocalDateTime.now())
					.build();
			kafkaTemplate.send(TEST_TOPIC_NAME, service, notification).get();


			var record = KafkaTestUtils.getSingleRecord(consumer, TEST_TOPIC_NAME, Duration.ofSeconds(5));
			assertEquals(service, record.key());
			assertEquals(notification.getMessage(), record.value().getMessage());
		} catch (ExecutionException | InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}
}
