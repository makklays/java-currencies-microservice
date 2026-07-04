package com.techmatrix18.listener;

import com.techmatrix18.repository.CurrencyCqrsReadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Configuration
public class CurrencyKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(CurrencyKafkaListener.class);
    private final CurrencyCqrsReadRepository cqrsReadRepository;

    public CurrencyKafkaListener(CurrencyCqrsReadRepository cqrsReadRepository) {
        this.cqrsReadRepository = cqrsReadRepository;
    }

    /**
     * Современный функциональный Consumer для Spring Cloud Stream.
     * Автоматически подписывается на реактивный Flux из Kafka.
     */
    @Bean
    public Consumer<Flux<CurrencyUpdatedEvent>> consumeCurrencyUpdates() {
        return flux -> flux
            .flatMap(event -> {
                log.info("[Correlation-ID: {}] Консьюмер: Получено событие из Kafka. Синхронизируем CQRS Read-модель для {}",
                    event.getCorrelationId(), event.getCc());

                // Ищем запись в read-таблице currencies_cqrs_read
                return cqrsReadRepository.findByCc(event.getCc())
                    .flatMap(readEntity -> {
                        // Обновляем коммерческие цены и временную метку
                        readEntity.setBuyPrice(event.getBuyPrice());
                        readEntity.setSellPrice(event.getSellPrice());
                        readEntity.setExchangedate(event.getExchangeDate());
                        readEntity.setUpdatedAt(LocalDateTime.now());

                        return cqrsReadRepository.save(readEntity);
                    })
                    .doOnSuccess(savedRead -> log.info("[Correlation-ID: {}] Консьюмер: CQRS Read-модель для {} УСПЕШНО синхронизирована.",
                            event.getCorrelationId(), event.getCc()))
                    .onErrorResume(e -> {
                        log.error("[Correlation-ID: {}] Ошибка при асинхронном обновлении CQRS Read-таблицы: {}",
                                event.getCorrelationId(), e.getMessage());
                        return Mono.empty(); // Возвращаем пустой Mono, чтобы поток логов не упал (Fault Tolerance)
                    });
            })
            .subscribe(); // Запускаем реактивное прослушивание бесконечного Flux очереди
    }
}

