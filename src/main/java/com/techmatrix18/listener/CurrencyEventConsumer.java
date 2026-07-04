package com.techmatrix18.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmatrix18.model.Currency;
import com.techmatrix18.model.CurrencyCqrsRead;
import com.techmatrix18.repository.CurrencyCqrsReadRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

/**
 * Реактивный Kafka Консьюмер для обработки событий валют.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 05.07.2026
 */
@Component
public class CurrencyEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CurrencyEventConsumer.class);

    private final ReactiveKafkaConsumerTemplate<String, String> kafkaConsumerTemplate;
    private final CurrencyCqrsReadRepository cqrsReadRepo;
    private final ObjectMapper objectMapper;

    public CurrencyEventConsumer(ReactiveKafkaConsumerTemplate<String, String> kafkaConsumerTemplate,
                                 CurrencyCqrsReadRepository cqrsReadRepo,
                                 ObjectMapper objectMapper) {
        this.kafkaConsumerTemplate = kafkaConsumerTemplate;
        this.cqrsReadRepo = cqrsReadRepo;
        this.objectMapper = objectMapper;
    }

    /**
     * Метод автоматически запускается при старте приложения,
     * открывает бесконечный реактивный поток чтения из Kafka.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startConsuming() {
        log.info("Реактивный Kafka Консьюмер валют запущен и слушает топик...");

        kafkaConsumerTemplate.receiveAutoAck() // Читаем с авто-подтверждением (Auto Commit)
            // Изолируем обработку каждого сообщения внутри flatMap
            .flatMap(record -> processRecord(record)
                .onErrorResume(error -> {
                    // Перехватываем абсолютно любые ошибки (JSON, R2DBC, DB, Network)
                    log.error("Ошибка обработки сообщения из партиции {}: {}", record.partition(), error.getMessage());
                    // Возвращаем пустой Mono, чтобы корневой поток Kafka перешел к следующему сообщению
                    return Mono.empty();
                })
            )
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                null,
                error -> log.error("КРИТИЧЕСКАЯ НЕОБРАБОТАННАЯ ОШИБКА в потоке Kafka Consumer: {}", error.getMessage())
            );
    }

    /**
     * Обработка одного сообщения из Kafka (сразу мапим в сущность Currency)
     */
    private Mono<Void> processRecord(ConsumerRecord<String, String> record) {
        return Mono.fromCallable(() -> objectMapper.readValue(record.value(), Currency.class))
            .flatMap(this::updateCqrsReadView)
            .then();
    }

    /**
     * Метод обновления CQRS Read-витрины напрямую из write-модели Currency
     */
    private Mono<CurrencyCqrsRead> updateCqrsReadView(Currency writeModel) {
        log.info("Консьюмер: Получено событие. Синхронизируем Read-модель для {}", writeModel.getCc());

        return cqrsReadRepo.findByCc(writeModel.getCc())
            .defaultIfEmpty(new CurrencyCqrsRead())
            .flatMap(readModel -> {
                if (readModel.getId() == null) {
                    readModel.setCc(writeModel.getCc());
                    readModel.setR030(writeModel.getR030());
                    readModel.setTitle(writeModel.getTitle());
                }

                // Переносим коммерческие курсы из write-модели в read-модель
                readModel.setBuyPrice(writeModel.getBuyPrice());
                readModel.setSellPrice(writeModel.getSellPrice());
                readModel.setExchangedate(writeModel.getExchangedate());
                readModel.setUpdatedAt(LocalDateTime.now());

                return cqrsReadRepo.save(readModel);
            });
    }
}

