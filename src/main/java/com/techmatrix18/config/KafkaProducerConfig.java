package com.techmatrix18.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmatrix18.model.Currency;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация Kafka Producer для реактивного отправления сообщений в топик.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.07.2026
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.topic.currency-updates:currency-rate-updates-topic}")
    private String topicName;

    /**
     * Автоматически создает топик в Kafka при запуске микросервиса валют
     */
    @Bean
    public org.apache.kafka.clients.admin.NewTopic currencyUpdatesTopic() {
        return org.springframework.kafka.config.TopicBuilder.name(topicName)
            .partitions(3)      // 3 партиции для параллельной обработки
            .replicas(1)         // 1 реплика (для dev-окружения)
            .build();
    }

    @Bean
    public ReactiveKafkaProducerTemplate<String, Currency> reactiveKafkaProducerTemplate(ObjectMapper objectMapper) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Используем существующий objectMapper проекта для сериализации сущности Currency
        JsonSerializer<Currency> valueSerializer = new JsonSerializer<>(objectMapper);

        SenderOptions<String, Currency> senderOptions = SenderOptions.<String, Currency>create(props)
            .withKeySerializer(new StringSerializer())
            .withValueSerializer(valueSerializer);

        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }
}

