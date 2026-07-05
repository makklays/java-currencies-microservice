package com.techmatrix18.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmatrix18.model.Currency;
import com.techmatrix18.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Фоновый реактивный воркер для реализации паттерна Transactional Outbox.
 * Выгребает события из БД и гарантированно доставляет их в Kafka.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.07.2026
 */
@Component
public class CurrencyOutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(CurrencyOutboxWorker.class);

    private final OutboxEventRepository outboxRepo;
    private final ReactiveKafkaProducerTemplate<String, Currency> kafkaProducerTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topic.currency-updates:currency-rate-updates-topic}")
    private String topicName;

    public CurrencyOutboxWorker(OutboxEventRepository outboxRepo,
                                ReactiveKafkaProducerTemplate<String, Currency> kafkaProducerTemplate,
                                ObjectMapper objectMapper) {
        this.outboxRepo = outboxRepo;
        this.kafkaProducerTemplate = kafkaProducerTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Опрос таблицы outbox каждые 2 секунды (можно настроить под ваши нужды)
     */
    @Scheduled(fixedDelay = 2000)
    public void processOutboxEvents() {
        outboxRepo.findAllUnprocessed() // Использует кастомный реактивный Flux запрос по индексу
            .flatMap(event -> {
                log.info("Outbox Воркер: Обнаружено необработанное событие {}. Отправка в Kafka...", event.getId());

                return deserializePayload(event.getPayload())
                    .flatMap(currencyEntity ->
                        // Отправляем в Kafka, используя код валюты в качестве ключа
                        kafkaProducerTemplate.send(topicName, currencyEntity.getCc(), currencyEntity)
                            .flatMap(senderResult -> {
                                log.info("Outbox Воркер: Событие успешно доставлено в Kafka. Партиция: {}. Помечаем в БД...",
                                        senderResult.recordMetadata().partition());

                                // Меняем статус на обработанный
                                event.setProcessed(true);
                                event.setProcessedAt(LocalDateTime.now());
                                event.setNewEntry(false); // Указываем R2DBC, что это UPDATE существующей записи

                                return outboxRepo.save(event);
                            })
                    )
                    .onErrorResume(err -> {
                        log.error("Outbox Воркер: Ошибка при обработке события {}: {}", event.getId(), err.getMessage());
                        // Возвращаем пустой Mono, чтобы сбой одного события не сломал обработку остальных
                        return Mono.empty();
                    });
            })
            // subscribe() необходим, так как метод шедулера возвращает void в реактивном WebFlux окружении
            .subscribe();
    }

    /**
     * Безопасная десериализация JSON-строки обратно в доменную модель Currency
     */
    private Mono<Currency> deserializePayload(String payload) {
        return Mono.fromCallable(() -> objectMapper.readValue(payload, Currency.class))
            .onErrorResume(e -> {
                log.error("Outbox Воркер: Ошибка десериализации payload JSON: {}", e.getMessage());
                return Mono.empty();
            });
    }
}

