package com.techmatrix18.service;

import com.techmatrix18.model.Currency;
import com.techmatrix18.repository.CurrencyRepository;
import org.springframework.stereotype.Service;
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
}

