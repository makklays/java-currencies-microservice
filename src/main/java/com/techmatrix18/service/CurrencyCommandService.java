package com.techmatrix18.service;

import com.techmatrix18.controller.CurrencyController;
import com.techmatrix18.model.Currency;
import com.techmatrix18.repository.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

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

    public CurrencyCommandService(CurrencyRepository currencyRepo) {
        this.currencyRepo = currencyRepo;
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
     * Реализация метода обновления курса для Саги
     */
    @Transactional
    public Mono<Void> updateCurrencyRate(String correlationId, String code, Double buyPrice, Double sellPrice, String updatedAt) {
        log.info("[Correlation-ID: {}] Сервис записи: Старт обновления курсов для {}", correlationId, code);

        return currencyRepo.findByCc(code)
            .flatMap(currencyEntity -> {
                // Прямое сохранение коммерческих курсов покупки и продажи
                currencyEntity.setBuyPrice(buyPrice);
                currencyEntity.setSellPrice(sellPrice);

                if (updatedAt != null) {
                    currencyEntity.setExchangedate(updatedAt);
                }

                return currencyRepo.save(currencyEntity);
            })
            .doOnSuccess(savedCurrency -> log.info("[Correlation-ID: {}] Сервис записи: Курсы buy/sell для {} успешно сохранены.", correlationId, code))
            .then();
    }
}

