package com.techmatrix18.service;

import com.techmatrix18.model.CurrencyCqrsRead;
import com.techmatrix18.repository.CurrencyCqrsReadRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing CurrencyCqrsRead entities (Query side of CQRS).
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @version 0.0.1
 * @since 11.06.2026
 */
@Service
public class CurrencyQueryService {
    private final CurrencyCqrsReadRepository currencyCqrsReadRepo;

    public CurrencyQueryService(CurrencyCqrsReadRepository currencyCqrsReadRepo) {
        this.currencyCqrsReadRepo = currencyCqrsReadRepo;
    }

    public Flux<CurrencyCqrsRead> findAll() {
        return currencyCqrsReadRepo.findAll();
    }

    public Flux<CurrencyCqrsRead> findByExchangedate(String date) {
        return currencyCqrsReadRepo.findByExchangedate(date);
    }

    public Mono<CurrencyCqrsRead> findById(Long id) {
        return currencyCqrsReadRepo.findById(id);
    }

    public Mono<CurrencyCqrsRead> findByCc(String code) {
        return currencyCqrsReadRepo.findByCc(code);
    }
}

