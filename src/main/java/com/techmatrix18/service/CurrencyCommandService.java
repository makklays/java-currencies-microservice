package com.techmatrix18.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmatrix18.model.CurrenciesIdempotency;
import com.techmatrix18.model.OutboxEvent;
import com.techmatrix18.repository.CurrencyRepository;
import com.techmatrix18.repository.CurrenciesIdempotencyRepository;
import com.techmatrix18.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CurrencyCommandService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyCommandService.class);

    private final CurrencyRepository currencyRepo;
    private final CurrenciesIdempotencyRepository idempotencyRepo;
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;

    public CurrencyCommandService(CurrencyRepository currencyRepo,
                                  CurrenciesIdempotencyRepository idempotencyRepo,
                                  OutboxEventRepository outboxRepo,
                                  ObjectMapper objectMapper) {
        this.currencyRepo = currencyRepo;
        this.idempotencyRepo = idempotencyRepo;
        this.outboxRepo = outboxRepo;
        this.objectMapper = objectMapper;
    }

    /**
     * Обновление курса с Идемпотентностью и Outbox Pattern
     */
    @Transactional
    public Mono<Void> updateCurrencyRate(String correlationId, String code, Double buyPrice, Double sellPrice, String updatedAt) {
        log.info("[Correlation-ID: {}] Сервис записи: Старт транзакции.", correlationId);

        // 1. ПРОВЕРКА ИДЕМПОТЕНТНОСТИ
        return idempotencyRepo.findById(correlationId)
            .flatMap(idempotencyRecord -> {
                // Если ключ найден — это дубликат. Возвращаем пустой Mono (Идемпотентный успех)
                log.warn("[Correlation-ID: {}] Идемпотентность: Запрос уже обрабатывался со статусом {}. Пропускаем дубликат.",
                    correlationId, idempotencyRecord.getStatus());
                return Mono.empty();
            })
            // Если ключа нет — flatMap вернет empty, и сработает switchIfEmpty (основная транзакция)
            .switchIfEmpty(Mono.defer(() -> {

                // 2. РЕГИСТРИРУЕМ КЛЮЧ ИДЕМПОТЕНТНОСТИ (Статус PROCESSING)
                CurrenciesIdempotency idempotency = new CurrenciesIdempotency();
                idempotency.setIdempotencyKey(correlationId);
                idempotency.setStatus("PROCESSING");
                idempotency.setCreatedAt(LocalDateTime.now());

                return idempotencyRepo.save(idempotency)
                    // 3. ОБНОВЛЯЕМ ВАЛЮТУ В ОСНОВНОЙ ТАБЛИЦЕ
                    .then(currencyRepo.findByCc(code))
                    .flatMap(currencyEntity -> {
                        currencyEntity.setBuyPrice(buyPrice);
                        currencyEntity.setSellPrice(sellPrice);
                        if (updatedAt != null) {
                            currencyEntity.setExchangedate(updatedAt);
                        }
                        return currencyRepo.save(currencyEntity);
                    })
                    // 4. ЗАПИСЫВАЕМ СОБЫТИЕ В ТАБЛИЦУ OUTBOX (В той же транзакции!)
                    .flatMap(savedCurrency -> {
                        return Mono.fromCallable(() -> {
                                OutboxEvent outbox = new OutboxEvent();
                                outbox.setId(UUID.randomUUID());
                                outbox.setAggregateType("Currency");
                                outbox.setAggregateId(savedCurrency.getId().toString());
                                outbox.setEventType("CurrencyUpdated");
                                outbox.setProcessed(false);
                                outbox.setCreatedAt(LocalDateTime.now());

                                // Сериализуем сущность Currency в JSONB payload
                                String jsonPayload = objectMapper.writeValueAsString(savedCurrency);
                                outbox.setPayload(jsonPayload);

                                return outbox;
                            })
                            .flatMap(outboxRepo::save);
                    })
                    // 5. ПЕРЕВОДИМ ИДЕМПОТЕНТНОСТЬ В СТАТУС COMPLETED
                    .flatMap(savedOutbox -> idempotencyRepo.findById(correlationId))
                    .flatMap(completedIdempotency -> {
                        completedIdempotency.setStatus("COMPLETED");
                        completedIdempotency.setResponseBody("{\"status\":\"SUCCESS\"}");
                        return idempotencyRepo.save(completedIdempotency);
                    })
                    .doOnSuccess(v -> log.info("[Correlation-ID: {}] Сервис записи: Курс сохранен, Outbox записан, транзакция успешно завершена.", correlationId))
                    .then();
            }))
            .then();
    }
}

