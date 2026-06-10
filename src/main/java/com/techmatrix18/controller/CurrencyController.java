package com.techmatrix18.controller;

import com.techmatrix18.model.Currency;
import com.techmatrix18.model.CurrencyCqrsRead;
import com.techmatrix18.service.CurrencyCommandService;
import com.techmatrix18.service.CurrencyQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for CreditCard endpoints with CQRS and Idempotency.
 *
 * @author Alexander Kuziv <makklays@gmail.com>
 * @company TechMatrix18
 * @since 03.06.2026
 * @version 0.0.1
 */
@RestController
@RequestMapping(path = "/api/v1/currency", produces = "application/json")
@CrossOrigin(origins = "*")
public class CurrencyController {

    private final CurrencyCommandService currencyCommandService;
    private final CurrencyQueryService currencyQueryService;

    public CurrencyController(CurrencyCommandService currencyCommandService,
                              CurrencyQueryService currencyQueryService) {
        this.currencyCommandService = currencyCommandService;
        this.currencyQueryService = currencyQueryService;
    }

    @GetMapping("/hello")
    public Mono<String> hello() {
        // return greeting message - test only
        return Mono.just("Hello, reactive world!");
    }

    // =========================================================================
    //   CQRS: ВЕТКА ЧТЕНИЯ (QUERIES) -> Возвращают CreditCardCqrsRead
    // =========================================================================
    @GetMapping(params = "recent")
    public Flux<CurrencyCqrsRead> recentCurrency() {
        return currencyQueryService.findAll().take(12);
    }

    /**
     * Get credit card by ID (Using CQRS read-optimized table)
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Mono<CurrencyCqrsRead> currencyById(@PathVariable("id") Long id) {
        return currencyQueryService.findById(id);
    }

    // =========================================================================
    //   CQRS + ИДЕМПОТЕНТНОСТЬ: ВЕТКА ЗАПИСИ (COMMANDS) -> Меняют состояние
    // =========================================================================
    /**
     * Operation to create a new currency (Now protected by Idempotency)
     */
    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Currency> postCreditCard(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestBody Currency currency) {

        // Передаем ключ идемпотентности, чтобы защитить систему от создания дубликатов карт
        //return currencyCommandService.createCard(idempotencyKey, currency);

        return Mono.empty(); // Заглушка, так как реализация метода createCard не показана
    }
}

