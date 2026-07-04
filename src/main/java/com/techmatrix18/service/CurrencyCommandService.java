package com.techmatrix18.service;

import com.techmatrix18.model.Currency;
import com.techmatrix18.repository.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing Currency entities.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 10.06.2026
 */
@Service
public class CurrencyCommandService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyCommandService.class);
    private final CurrencyRepository currencyRepo;
    private final ReactiveKafkaProducerTemplate<String, Currency> kafkaProducerTemplate;

    @Value("${spring.kafka.topic.currency-updates:currency-rate-updates-topic}")
    private String topicName;

    public CurrencyCommandService(CurrencyRepository currencyRepo, ReactiveKafkaProducerTemplate<String, Currency> kafkaProducerTemplate) {
        this.currencyRepo = currencyRepo;
        this.kafkaProducerTemplate = kafkaProducerTemplate;
    }

    public Flux<Currency> findAll() {
        return currencyRepo.findAll();
    }
    public Flux<Currency> findByExchangedate(String date) {
        return currencyRepo.findByExchangedate(date);
    }

    public Mono<Currency> findById(Long id) {
        return currencyRepo.findById(id);
    }
    public Mono<Currency> findByCc(String code) {
        return currencyRepo.findByCc(code);
    }

    /**
     * Реализация метода обновления курса для Саги с отправкой в Kafka
     */
    @Transactional
    public Mono<Void> updateCurrencyRate(String correlationId, String code, Double buyPrice, Double sellPrice, String updatedAt) {
        log.info("[Correlation-ID: {}] Сервис записи: Старт обновления курсов для {}", correlationId, code);

        return currencyRepo.findByCc(code)
            .flatMap(currencyEntity -> {
                // Обновляем коммерческие курсы
                currencyEntity.setBuyPrice(buyPrice);
                currencyEntity.setSellPrice(sellPrice);

                if (updatedAt != null) {
                    currencyEntity.setExchangedate(updatedAt);
                }

                // Сохраняем в WRITE-таблицу (currencies)
                return currencyRepo.save(currencyEntity);
            })
            // Цепочка flatMap гарантирует: отправка в Kafka начнется только ПОСЛЕ успешной записи в БД
            .flatMap(savedCurrency -> {
                log.info("[Correlation-ID: {}] Продюсер: Отправка обновленной сущности {} в Kafka топик '{}'...", correlationId, code, topicName);

                // Асинхронно отправляем саму сущность Currency в Kafka.
                // В качестве ключа сообщения (Key) передаем код валюты (например, "USD") для сохранения порядка в партициях.
                return kafkaProducerTemplate.send(topicName, savedCurrency.getCc(), savedCurrency)
                    .doOnSuccess(senderResult -> log.info("[Correlation-ID: {}] Продюсер: Курс успешно опубликован в Kafka. Партиция: {}",
                            correlationId, senderResult.recordMetadata().partition()))
                    .doOnError(err -> log.error("[Correlation-ID: {}] Продюсер: КРИТИЧЕСКАЯ ОШИБКА отправки в Kafka: {}",
                            correlationId, err.getMessage()))
                    // Возвращаем сохраненную сущность дальше по цепочке
                    .thenReturn(savedCurrency);
            })
            .doOnSuccess(savedCurrency -> log.info("[Correlation-ID: {}] Сервис записи: Бизнес-процесс Саги для {} успешно завершен.", correlationId, code))
            .then(); // Сбрасываем результат в Mono<Void> для соответствия сигнатуре
    }
}

